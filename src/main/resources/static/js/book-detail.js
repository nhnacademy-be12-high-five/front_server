const likeProcessingMap = {};

async function handleReviewError(response, defaultMsg = "요청 처리에 실패했습니다.") {
    let msg = defaultMsg;
    try {
        const data = await response.json();
        if (data && data.message) msg = data.message;
    } catch (e) {
        switch (response.status) {
            case 400:
                msg = "입력 정보가 올바르지 않습니다.";
                break;
            case 401:
                msg = "로그인이 필요합니다.";
                break;
            case 403:
                msg = "권한이 없습니다.";
                break;
            case 404:
                msg = "대상을 찾을 수 없습니다.";
                break;
            case 413:
                msg = "업로드하려는 파일 크기가 너무 큽니다. (이미지 용량을 확인해주세요)";
                break;
            case 500:
                msg = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
                break;
        }
    }
    alert(msg);
    if (response.status === 401) location.href = "/member/login";
}

function toggleReviewLike(bookId, reviewId, btn) {
    if (likeProcessingMap[reviewId]) return;

    likeProcessingMap[reviewId] = true;
    btn.style.opacity = "0.5";
    btn.style.cursor = "not-allowed";

    fetch(`/books/${bookId}/reviews/${reviewId}/like`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    })
        .then(async response => {
            if (response.redirected) {
                if (confirm("로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?")) {
                    location.href = "/member/login";
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

            const headers = {'Content-Type': 'application/json'};
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
// 탭 관련 로직은 기존 코드 유지 (생략 없음)
document.addEventListener('DOMContentLoaded', function () {
    const tabButtons = document.querySelectorAll('.book-detail-tabs button');
    const sections = document.querySelectorAll('.detail-tab-content');
    const navBar = document.querySelector('.book-detail-tabs');

    if (!navBar) return;

    /* ======================
       탭 클릭 → 스크롤 이동
       ====================== */
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.dataset.target;
            const targetSection = document.getElementById(targetId);
            if (!targetSection) return;

            const navHeight = navBar.offsetHeight;

            const y =
                targetSection.getBoundingClientRect().top +
                window.pageYOffset -
                navHeight - 10;

            window.scrollTo({
                top: y,
                behavior: 'smooth'
            });
        });
    });

    /* ======================
       스크롤 → 탭 active 갱신
       ====================== */
    window.addEventListener('scroll', () => {
        let currentId = null;
        const navHeight = navBar.offsetHeight + 20;

        sections.forEach(section => {
            const rect = section.getBoundingClientRect();
            if (rect.top <= navHeight && rect.bottom > navHeight) {
                currentId = section.id;
            }
        });

        tabButtons.forEach(btn => {
            btn.classList.toggle(
                'active',
                btn.dataset.target === currentId
            );
        });
    });
});




// 장바구니 담기 버튼
document.addEventListener("DOMContentLoaded", function () {
    const addToCartBtn = document.getElementById("add-to-cart-btn");
    if (addToCartBtn) {
        addToCartBtn.addEventListener("click", async () => {
            const bookId = addToCartBtn.getAttribute("data-book-id");

            const requestBody = {bookId: Number(bookId), quantity: 1};

            try {
                const response = await fetch("/cart/items", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(requestBody)
                });

                if (!response.ok) {
                    if (response.status === 401) {
                        if (confirm("로그인이 필요합니다.\n로그인 페이지로 이동하시겠습니까?")) {
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

// 전역 변수로 선택된 파일 관리
let selectedFiles = [];
let editSelectedFiles = [];
let deleteImageIds = [];

// ========== 이미지 모달 기능 ==========
function openImageModal(src) {
    const modal = document.getElementById('imageModal');
    const modalImg = document.getElementById('modalImage');
    modal.style.display = "flex";
    modalImg.src = src;
}

function closeImageModal() {
    const modal = document.getElementById('imageModal');
    modal.style.display = "none";
}

// ========== 리뷰 작성 파일 관리 ==========
function handleFileSelect(input) {
    const files = Array.from(input.files);

    // 최대 개수 제한 (기존 + 새 파일)
    if (selectedFiles.length + files.length > 5) {
        alert("이미지는 최대 5장까지 업로드 가능합니다.");
        input.value = ""; // 초기화
        return;
    }

    files.forEach(file => {
        // 중복 방지 (파일명과 사이즈로 비교)
        const isDuplicate = selectedFiles.some(f => f.name === file.name && f.size === file.size);
        if (!isDuplicate) {
            // 10MB 제한 체크
            if(file.size > 10 * 1024 * 1024) {
                alert(`파일 '${file.name}'의 크기가 10MB를 초과합니다.`);
                return;
            }
            selectedFiles.push(file);
        }
    });

    renderFileList();
    input.value = ""; // 같은 파일 다시 선택 가능하도록 input 초기화
}

function renderFileList() {
    const fileListContainer = document.getElementById('file-list');
    if (!fileListContainer) return;

    fileListContainer.innerHTML = "";

    selectedFiles.forEach((file, index) => {
        const item = document.createElement('div');
        item.className = 'file-upload-item';
        item.innerHTML = `
            <span>${file.name}</span>
            <button type="button" class="btn-remove-file" onclick="removeFile(${index})">삭제 -</button>
        `;
        fileListContainer.appendChild(item);
    });
}

function removeFile(index) {
    selectedFiles.splice(index, 1);
    renderFileList();
}

async function submitReview(bookId) {
    const form = document.getElementById("reviewForm");
    const content = form.querySelector("textarea[name='content']").value;
    const rating = form.querySelector("select[name='rating']").value;

    if (!content.trim()) {
        alert("리뷰 내용을 입력해주세요.");
        return;
    }
    if(content.length < 10) {
        alert("리뷰는 최소 10자 이상 작성해주세요.");
        return;
    }

    if(content.length > 1000) {
        alert("리뷰는 최댜 1000자 미만 작성해주세요.");
        return;
    }

    const formData = new FormData();
    formData.append("rating", rating);
    formData.append("content", content);

    // 관리 중인 파일 배열을 FormData에 추가
    selectedFiles.forEach(file => {
        formData.append("images", file);
    });

    try {
        const response = await fetch(`/books/${bookId}/reviews`, {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            const error = await response.json().catch(() => null);

            if (response.status === 403 && error?.code === "R003") {
                alert("해당 책을 구매한 분만 리뷰를 작성할 수 있습니다.");
                return;
            }

            alert(error?.message ?? "리뷰 등록에 실패했습니다.");
            return;
        }

        if (response.redirected) {
            if (confirm("로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?")) {
                location.href = "/member/login";
            }
            return null;
        }


        alert("리뷰가 성공적으로 등록되었습니다!");
        location.reload();

    } catch (error) {
        console.error(error);
        alert("리뷰 등록 중 통신 오류가 발생했습니다.");
    }
}


// ========== 리뷰 수정 관련 로직 ==========

function startEditReview(btn) {
    const reviewId = btn.dataset.reviewId;
    const bookId = btn.dataset.bookId;
    const content = btn.dataset.content;
    const rating = btn.dataset.rating;

    // [수정] 'ID|URL,ID|URL' 형태의 문자열을 파싱해서 객체 배열로 변환
    const imagesDataString = btn.dataset.reviewImages || "";
    const existingImages = imagesDataString ? imagesDataString.split(',').map(str => {
        const parts = str.split('|');
        return { id: parts[0], url: parts[1] }; // {id: "10", url: "http://..."}
    }) : [];

    // 초기화
    editSelectedFiles = [];
    deleteImageIds = [];

    const container = document.querySelector(".my-review-card");

    // 기존 이미지 리스트 HTML 생성
    let existingImagesHtml = '';
    if (existingImages.length > 0) {
        existingImagesHtml = `<div class="existing-images-list">`;
        existingImages.forEach((img, idx) => {
            if (img.url) {
                // [수정] 삭제 버튼에 ID 전달 (img.id)
                existingImagesHtml += `
                    <div class="existing-image-item" id="existing-img-${idx}">
                        <img src="${img.url}">
                        <button type="button" class="btn-remove-image" onclick="markImageAsDeleted('${img.id}', ${idx})">X</button>
                    </div>
                `;
            }
        });
        existingImagesHtml += `</div>`;
    }

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
                <label style="font-weight:bold; display:block; margin-bottom:5px;">기존 이미지</label>
                ${existingImagesHtml}
                
                <label style="font-weight:bold; display:block; margin-top:15px; margin-bottom:5px;">이미지 추가</label>
                <input type="file" id="editFileInput" name="images" multiple accept="image/*" style="display: none;" onchange="handleEditFileSelect(this)">
                <button type="button" class="btn-small" onclick="document.getElementById('editFileInput').click()" style="background-color: #6c757d;">
                    📷 사진 추가하기
                </button>
                <div id="edit-file-list" class="file-upload-list"></div>
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

function markImageAsDeleted(imageId, index) {
    // UI에서 숨김
    const el = document.getElementById(`existing-img-${index}`);
    if (el) el.style.display = 'none';

    // [중요] 삭제할 ID 배열에 추가 (DTO의 deleteImageIds와 매핑됨)
    deleteImageIds.push(parseInt(imageId));
}
// 수정 모드: 새 파일 추가 핸들러
function handleEditFileSelect(input) {
    const files = Array.from(input.files);
    // 개수 제한 로직은 기존 이미지 개수를 고려해야 하나, 여기선 단순화함
    files.forEach(file => {
        if(file.size > 10 * 1024 * 1024) {
            alert("10MB 이하의 파일만 업로드 가능합니다.");
            return;
        }
        editSelectedFiles.push(file);
    });
    renderEditFileList();
    input.value = "";
}

function renderEditFileList() {
    const container = document.getElementById('edit-file-list');
    container.innerHTML = "";
    editSelectedFiles.forEach((file, idx) => {
        container.innerHTML += `
            <div class="file-upload-item">
                <span>${file.name}</span>
                <button type="button" class="btn-remove-file" onclick="removeEditFile(${idx})">삭제 -</button>
            </div>
        `;
    });
}

function removeEditFile(index) {
    editSelectedFiles.splice(index, 1);
    renderEditFileList();
}

async function submitUpdateReview(reviewId, bookId) {
    const form = document.getElementById("updateReviewForm");
    const rating = form.querySelector("select[name='rating']").value;
    const content = form.querySelector("textarea[name='content']").value;

    if (!content.trim()) {
        alert("리뷰 내용을 입력해주세요.");
        return;
    }

    // [수정] DTO 생성 시 deleteImageIds 포함
    const requestDto = {
        content,
        rating: Number(rating),
        deleteImageIds: deleteImageIds // 여기에 수집된 ID들이 들어감
    };

    const formData = new FormData();
    formData.append(
        "request",
        new Blob([JSON.stringify(requestDto)], { type: "application/json" })
    );

    // 새로 추가한 파일들
    editSelectedFiles.forEach(file => {
        formData.append("images", file);
    });

    try {
        const response = await fetch(`/books/${bookId}/reviews/${reviewId}`, {
            method: "POST", // 또는 "PUT"
            body: formData
        });

        if(content.length < 10) {
            alert("리뷰는 최소 10자 이상 작성해주세요.");
            return;
        }

        if(content.length > 1000) {
            alert("리뷰는 최댜 1000자 미만 작성해주세요.");
            return;
        }

        if (!response.ok) {
            await handleReviewError(response, "리뷰 수정에 실패했습니다.");
            return;
        }

        alert("리뷰가 수정되었습니다.");
        location.reload();

    } catch (e) {
        console.error(e);
        alert("네트워크 오류가 발생했습니다.");
    }
}