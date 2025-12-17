const likeProcessingMap = {};

function toggleReviewLike(bookId, reviewId, btn) {
    if (likeProcessingMap[reviewId]) return;

    likeProcessingMap[reviewId] = true;
    btn.style.opacity = "0.5";
    btn.style.cursor = "not-allowed";

    fetch(`/books/${bookId}/reviews/${reviewId}/like`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.status === 401) {
                if (confirm("로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?")) {
                    location.href = "/member/login.html";
                }
                return null;
            }
            if (!response.ok) throw new Error("좋아요 처리 실패");
            return response.json();
        })
        .then(isLiked => {
            if (isLiked === null) return;

            const countSpan = document.getElementById(`like-count-${reviewId}`);
            let currentCount = parseInt(countSpan.innerText);

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
            alert("일시적인 오류가 발생했습니다.");
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
    if (!heartBtn) return;

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

            } else if (response.status === 401) {
                if (confirm("로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하시겠습니까?")) {
                    location.href = "/member/login.html";
                }
            } else {
                throw new Error("요청 실패");
            }
        } catch (e) {
            console.error(e);
            alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    });
});

// ===================================================================================//

async function submitUpdateReview(reviewId, bookId) {
    const form = document.getElementById("updateReviewForm");

    const rating = form.querySelector("select[name='rating']").value;
    const content = form.querySelector("textarea[name='content']").value;

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
    for (const file of fileInput.files) {
        formData.append("images", file);
    }

    const response = await fetch(`/books/${bookId}/reviews/${reviewId}`, {
        method: "POST",
        body: formData
    });

    if (!response.ok) {
        alert("리뷰 수정 실패");
        return;
    }

    alert("리뷰가 수정되었습니다.");
    location.reload();
}

// ===================================================================================//

document.addEventListener('DOMContentLoaded', function () {
    const tabButtons = document.querySelectorAll('.book-detail-tabs button');
    const sections = document.querySelectorAll('.detail-tab-content');
    const navBar = document.querySelector('.book-detail-tabs');

    // 1. 버튼 클릭 시 해당 위치로 스크롤 이동 (Smooth Scroll)
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.dataset.target;
            const targetSection = document.getElementById(targetId);

            if (targetSection) {
                // 네비게이션 바 높이만큼 빼고 이동해야 제목이 안 가려짐
                const navHeight = navBar.offsetHeight;
                const targetPosition = targetSection.offsetTop - navHeight;

                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });
            }
        });
    });

    // 2. 스크롤 시 현재 보고 있는 섹션 버튼 활성화 (Scroll Spy)
    window.addEventListener('scroll', () => {
        let current = '';
        const navHeight = navBar.offsetHeight + 50; // 여유값

        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            const sectionHeight = section.clientHeight;

            // 현재 스크롤 위치가 섹션 범위 안에 들어왔는지 확인
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
document.addEventListener("DOMContentLoaded", function () {

    const addToCartBtn = document.getElementById("add-to-cart-btn");
    addToCartBtn.addEventListener("click", async () => {
        const bookId = addToCartBtn.getAttribute("data-book-id");

        // 요청 Body 구성
        const requestBody = {
            bookId: Number(bookId),
            quantity: 1
        };

        try {
            const response = await fetch("/cart/items", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(requestBody)
            });

            if (!response.ok) {
                if (response.status === 401) {
                    alert("로그인이 필요합니다.");
                    window.location.href = "/member/login.html";
                    return;
                }
                throw new Error("장바구니 추가 실패");
            }

            if (confirm("장바구니에 상품이 담겼습니다.\n장바구니로 이동하시겠습니까?")) {
                window.location.href = "/cart";
            }

        } catch (error) {
            console.error(error);
            alert("장바구니 담기 중 오류가 발생했습니다.");
        }
    });
});

// ===================================================================================//

function startEditReview(btn) {
    const reviewId = btn.dataset.reviewId;
    const bookId = btn.dataset.bookId;
    const content = btn.dataset.content;
    const rating = btn.dataset.rating;

    const container = document.querySelector(".my-review-card");

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

    const formData = new FormData();

    // [핵심 수정 1] JSON Blob 대신 일반 폼 필드로 데이터 추가 (@ModelAttribute 대응)
    formData.append("rating", rating);
    formData.append("content", content);

    // 이미지 파일 추가
    if (fileInput && fileInput.files.length > 0) {
        for (const file of fileInput.files) {
            formData.append("images", file);
        }
    }

    try {
        // [핵심 수정 2] URL에서 '/api' 제거 (Front Server Controller 경로인 /books/... 로 요청)
        const response = await fetch(`/books/${bookId}/reviews`, {
            method: "POST",
            body: formData
        });

        // 리다이렉트 응답(로그인 페이지 등) 체킹
        if (response.redirected) {
            window.location.href = response.url;
            return;
        }

        if (!response.ok) {
            throw new Error("리뷰 등록 실패");
        }

        alert("리뷰가 등록되었습니다!");
        location.reload();

    } catch (error) {
        console.error(error);
        alert("리뷰 등록 중 오류가 발생했습니다.");
    }
}