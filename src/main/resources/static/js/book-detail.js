const likeProcessingMap = {};

async function handleReviewError(response, defaultMsg = "요청 처리에 실패했습니다.") {
    let msg = defaultMsg;
    try {
        const data = await response.json();
        if (data && data.message) msg = data.message;
    } catch (e) {
        switch (response.status) {
            case 400: msg = "입력 정보가 올바르지 않습니다."; break;
            case 401: msg = "로그인이 필요합니다."; break;
            case 403: msg = "권한이 없습니다."; break;
            case 404: msg = "대상을 찾을 수 없습니다."; break;
            case 413: msg = "업로드하려는 파일 크기가 너무 큽니다. (이미지 용량을 확인해주세요)"; break;
            case 500: msg = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."; break;
        }
    }
    alert(msg);
    if (response.status === 401) location.href = "/member/login.html";
}

function toggleReviewLike(bookId, reviewId, btn) {
    if (likeProcessingMap[reviewId]) return;

    likeProcessingMap[reviewId] = true;
    btn.style.opacity = "0.5";
    btn.style.cursor = "not-allowed";

    fetch(`/books/${bookId}/reviews/${reviewId}/like`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(async response => {
            if (response.status === 401) {
                if (confirm("로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?")) {
                    location.href = "/member/login.html";
                }
                return null;
            }
            if (!response.ok) {
                // 에러 핸들링 함수 호출 혹은 throw
                await handleReviewError(response, "좋아요 처리 중 오류가 발생했습니다.");
                return null;
            }
            return response.json();
        })
        .then(isLiked => {
            if (isLiked === null) return; // 위에서 에러 처리됨

            const countSpan = document.getElementById(`like-count-${reviewId}`);
            let currentCount = parseInt(countSpan.innerText) || 0;

            if (isLiked) {
                btn.classList.add('active');
                countSpan.innerText = currentCount + 1;
            } else {
                btn.classList.remove('active');
                countSpan.innerText = Math.max(0, currentCount - 1);
            }
        })
        .catch(err => {
            console.error(err);
            // 네트워크 에러 등 fetch 자체 실패 시
            alert("네트워크 상태가 불안정합니다. 잠시 후 다시 시도해주세요.");
        })
        .finally(() => {
            setTimeout(() => {
                likeProcessingMap[reviewId] = false;
                btn.style.opacity = "1";
                btn.style.cursor = "pointer";
            }, 500);
        });
}

// ===================================================================================//

document.addEventListener('DOMContentLoaded', () => {
    const heartBtn = document.getElementById('heart');
    if (heartBtn) {
        heartBtn.addEventListener('click', async function () {
            const bookId = this.dataset.bookId;
            const accessToken = localStorage.getItem('accessToken');

            const headers = { 'Content-Type': 'application/json' };
            if (accessToken) {
                headers['Authorization'] = `Bearer ${accessToken}`;
            }

            try {
                const response = await fetch(`/api/books/${bookId}/likes`, {
                    method: 'POST',
                    headers
                });

                if (response.ok) {
                    this.classList.toggle('active');
                    if (this.classList.contains('active')) {
                        if (confirm("관심 도서에 담았습니다!\n마이페이지 찜 목록으로 이동하시겠습니까?")) {
                            location.href = "/books/my-page/likes";
                        }
                    } else {
                        alert("관심 도서가 해제되었습니다.");
                    }
                } else {
                    await handleReviewError(response, "관심 도서 등록/해제에 실패했습니다.");
                }
            } catch (e) {
                console.error(e);
                alert("서버와 통신 중 오류가 발생했습니다.");
            }
        });
    }
});

// ===================================================================================//

async function submitUpdateReview(reviewId, bookId) {
    const form = document.getElementById("updateReviewForm");

    const rating = form.querySelector("select[name='rating']").value;
    const content = form.querySelector("textarea[name='content']").value;

    if (!content.trim()) {
        alert("리뷰 내용을 입력해주세요.");
        return;
    }

    // 내용 길이 제한 체크 예시
    if (content.length > 1000) {
        alert("리뷰 내용은 1000자 이내로 작성해주세요.");
        return;
    }

    const requestDto = {
        content,
        rating: Number(rating),
        deleteImageIds: []
    };

    const formData = new FormData();
    formData.append(
        "request",
        new Blob([JSON.stringify(requestDto)], { type: "application/json" })
    );

    const fileInput = form.querySelector("input[name='images']");
    if (fileInput && fileInput.files.length > 0) {
        // 파일 개수 제한 예시
        if (fileInput.files.length > 5) {
            alert("이미지는 최대 5장까지 업로드 가능합니다.");
            return;
        }
        for (const file of fileInput.files) {
            // 파일 용량 사전 체크 (예: 5MB)
            if(file.size > 5 * 1024 * 1024) {
                alert(`파일 '${file.name}'의 용량이 너무 큽니다. (5MB 이하만 가능)`);
                return;
            }
            formData.append("images", file);
        }
    }

    try {
        const response = await fetch(`/books/${bookId}/reviews/${reviewId}`, {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            await handleReviewError(response, "리뷰 수정에 실패했습니다.");
            return;
        }

        alert("리뷰가 수정되었습니다.");
        location.reload();

    } catch (e) {
        console.error(e);
        alert("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
}

// ===================================================================================//
// 탭 관련 로직은 기존 코드 유지 (생략 없음)
document.addEventListener('DOMContentLoaded', function () {
    const tabButtons = document.querySelectorAll('.book-detail-tabs button');
    const sections = document.querySelectorAll('.detail-tab-content');
    const navBar = document.querySelector('.book-detail-tabs');

    if(!navBar) return; // 네비바 없으면 종료

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.dataset.target;
            const targetSection = document.getElementById(targetId);
            if (targetSection) {
                const navHeight = navBar.offsetHeight;
                const targetPosition = targetSection.offsetTop - navHeight;
                window.scrollTo({ top: targetPosition, behavior: 'smooth' });
            }
        });
    });

    window.addEventListener('scroll', () => {
        let current = '';
        const navHeight = navBar.offsetHeight + 50;
        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            if (scrollY >= (sectionTop - navHeight)) {
                current = section.getAttribute('id');
            }
        });
        tabButtons.forEach(btn => {
            btn.classList.remove('active');
            if (btn.dataset.target === current) {
                btn.classList.add('active');
            }
        });
    });
});

// 장바구니 담기 버튼
document.addEventListener("DOMContentLoaded", function () {
    const addToCartBtn = document.getElementById("add-to-cart-btn");
    if (addToCartBtn) {
        addToCartBtn.addEventListener("click", async () => {
            const bookId = addToCartBtn.getAttribute("data-book-id");

            const requestBody = { bookId: Number(bookId), quantity: 1 };

            try {
                const response = await fetch("/cart/items", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(requestBody)
                });

                if (!response.ok) {
                    if (response.status === 401) {
                        if(confirm("로그인이 필요합니다.\n로그인 페이지로 이동하시겠습니까?")) {
                            window.location.href = "/member/login.html";
                        }
                        return;
                    } else if (response.status === 409) { // 재고 부족 등의 상황 가정
                        alert("재고가 부족하여 장바구니에 담을 수 없습니다.");
                        return;
                    }

                    await handleReviewError(response, "장바구니 담기에 실패했습니다.");
                    return;
                }

                if (confirm("장바구니에 상품이 담겼습니다.\n장바구니로 이동하시겠습니까?")) {
                    window.location.href = "/cart";
                }

            } catch (error) {
                console.error(error);
                alert("서버 연결 상태가 원활하지 않습니다.");
            }
        });
    }
});

// ===================================================================================//

function startEditReview(btn) {
    const reviewId = btn.dataset.reviewId;
    const bookId = btn.dataset.bookId;
    const content = btn.dataset.content;
    const rating = btn.dataset.rating;

    const container = document.querySelector(".my-review-card");

    // HTML 주입 부분은 동일
    container.innerHTML = `
        <h3>리뷰 수정</h3>
        <form id="updateReviewForm" enctype="multipart/form-data">
            <div class="star-rating">
                <span class="rating-label">별점</span>
                <select name="rating" class="form-select">
                    <option value="5" ${rating == 5 ? "selected" : ""}>★★★★★ (5점)</option>
                    <option value="4" ${rating == 4 ? "selected" : ""}>★★★★☆ (4점)</option>
                    <option value="3" ${rating == 3 ? "selected" : ""}>★★★☆☆ (3점)</option>
                    <option value="2" ${rating == 2 ? "selected" : ""}>★★☆☆☆ (2점)</option>
                    <option value="1" ${rating == 1 ? "selected" : ""}>★☆☆☆☆ (1점)</option>
                </select>
            </div>
            <div class="input-group">
                <textarea name="content" required>${content}</textarea>
            </div>
            <div class="file-group">
                <input type="file" name="images" multiple accept="image/*">
            </div>
            <button type="button" class="btn-primary full-width"
                    onclick="submitUpdateReview(${reviewId}, ${bookId})">
                수정 완료
            </button>
            <button type="button" class="btn-secondary full-width" onclick="location.reload()">
                취소
            </button>
        </form>
    `;
}

// ===================================================================================//

async function submitReview(bookId) {
    const form = document.getElementById("reviewForm");

    const content = form.querySelector("textarea[name='content']").value;
    const rating = form.querySelector("select[name='rating']").value;
    const fileInput = form.querySelector("input[name='images']");

    if (!content.trim()) {
        alert("리뷰 내용을 입력해주세요.");
        return;
    }

    // 유효성 검사 추가
    if(content.length < 5) {
        alert("리뷰는 최소 5자 이상 작성해주세요.");
        return;
    }

    const formData = new FormData();
    formData.append("rating", rating);
    formData.append("content", content);

    if (fileInput && fileInput.files.length > 0) {
        if (fileInput.files.length > 5) {
            alert("이미지는 최대 5장까지만 등록 가능합니다.");
            return;
        }
        for (const file of fileInput.files) {
            // 10MB 제한 가정
            if(file.size > 10 * 1024 * 1024) {
                alert("이미지 파일 크기는 10MB를 초과할 수 없습니다.");
                return;
            }
            formData.append("images", file);
        }
    }

    try {
        const response = await fetch(`/books/${bookId}/reviews`, {
            method: "POST",
            body: formData
        });

        if (response.redirected) {
            window.location.href = response.url;
            return;
        }

        if (!response.ok) {
            await handleReviewError(response, "리뷰 등록에 실패했습니다.");
            return;
        }

        alert("리뷰가 성공적으로 등록되었습니다!");
        location.reload();

    } catch (error) {
        console.error(error);
        alert("리뷰 등록 중 통신 오류가 발생했습니다.");
    }
}