// ==============================
// 리뷰 좋아요 처리
// ==============================
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
                await handleReviewError(response, "좋아요 처리 중 오류가 발생했습니다.");
                return null;
            }
            return response.json();
        })
        .then(isLiked => {
            if (isLiked === null) return;

            const countSpan = document.getElementById(`like-count-${reviewId}`);
            if (!countSpan) return;

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


// ==============================
// 도서 좋아요(하트) 처리
// ==============================
document.addEventListener('DOMContentLoaded', () => {
    const heartBtn = document.getElementById('heart');
    if (!heartBtn) return;

    const bookId = heartBtn.dataset.bookId;
    if (!bookId) return;

    function buildHeaders() {
        const headers = {
            'Content-Type': 'application/json'
        };

        // 1. HTML의 meta 태그에서 user-id 값을 찾음
        const userIdMeta = document.querySelector('meta[name="user-id"]');

        // 2. 값이 존재하면 헤더에 추가
        if (userIdMeta && userIdMeta.content) {
            headers['X-USER-ID'] = userIdMeta.content;
        }

        return headers;
    }

    async function redirectToLoginIfNeeded(res) {
        if (res.status === 401 || res.status === 403) {
            alert("로그인이 필요합니다.");
            location.href = "/member/login";
            return true;
        }
        return false;
    }

    // 초기 상태 조회
    (async function loadLikeStatus() {
        try {
            const response = await fetch(`/books/${bookId}/likes/status`, {
                method: 'GET',
                headers: buildHeaders(),
                credentials: 'include'
            });

            if (await redirectToLoginIfNeeded(response)) return;
            if (!response.ok) return;

            const isLiked = await response.json();
            heartBtn.classList.toggle('active', !!isLiked);
        } catch (e) {
            console.warn("좋아요 상태 조회 실패", e);
        }
    })();

    // 토글
    heartBtn.addEventListener('click', async (e) => {
        e.preventDefault();
        e.stopPropagation();

        try {
            const response = await fetch(`/books/${bookId}/likes`, {
                method: 'POST',
                headers: buildHeaders(),
                credentials: 'include'
            });

            if (await redirectToLoginIfNeeded(response)) return;

            if (response.ok) {
                // 성공 후 상태 재조회
                const statusRes = await fetch(`/books/${bookId}/likes/status`, {
                    method: 'GET',
                    headers: buildHeaders(),
                    credentials: 'include'
                });

                if (statusRes.ok) {
                    const isLiked = await statusRes.json();
                    heartBtn.classList.toggle('active', !!isLiked);
                } else {
                    heartBtn.classList.toggle('active');
                }
            } else {
                alert("관심 도서 처리에 실패했습니다.");
            }
        } catch (e) {
            if(confirm("로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?")) {
                location.href = "/member/login";
            }
        }
    });
});


// ==============================
// 탭(도서소개/목차/리뷰/Q&A/배송반품) 처리 - 핵심 수정본
// 1) 클릭 시: active 설정 + (선택) 섹션 표시/숨김 + 스크롤 이동
// 2) 스크롤 시: 현재 섹션에 맞게 active 갱신 (currentId 있을 때만)
// ==============================
document.addEventListener('DOMContentLoaded', function () {
    const navBar = document.querySelector('.book-detail-tabs');
    if (!navBar) return;

    const tabButtons = Array.from(navBar.querySelectorAll('button[data-target]'));
    const sections = Array.from(document.querySelectorAll('.detail-tab-content'));
    if (tabButtons.length === 0 || sections.length === 0) return;

    function setActive(targetId) {
        tabButtons.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.target === targetId);
        });
    }

    function scrollToTarget(targetId) {
        const target = document.getElementById(targetId);
        if (!target) return;

        // CSS scroll-margin-top이 적용되어 "딱 위로" 올라오게 됨
        target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    tabButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();

            const targetId = btn.dataset.target;
            if (!targetId) return;

            setActive(targetId);
            scrollToTarget(targetId);
        });
    });

    // 스크롤 시 현재 섹션에 따라 active 갱신
    window.addEventListener('scroll', () => {
        // 헤더/탭바 가림 보정: 기준선을 화면 상단에서 약간 아래로 둠
        const 기준선 = 160;

        let currentId = null;
        for (const section of sections) {
            const rect = section.getBoundingClientRect();
            if (rect.top <= 기준선 && rect.bottom > 기준선) {
                currentId = section.id;
                break;
            }
        }

        if (currentId) setActive(currentId);
    });

    // 초기 active 세팅
    const initialBtn = tabButtons.find(b => b.classList.contains('active')) || tabButtons[0];
    if (initialBtn?.dataset?.target) setActive(initialBtn.dataset.target);
});


// ==============================
// 장바구니 담기
// ==============================
document.addEventListener("DOMContentLoaded", function () {
    const addToCartBtn = document.getElementById("add-to-cart-btn");
    if (!addToCartBtn) return;

    addToCartBtn.addEventListener("click", async () => {
        const bookId = addToCartBtn.getAttribute("data-book-id");
        const requestBody = { bookId: Number(bookId), quantity: 1 };

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
                } else if (response.status === 409) {
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
});


// ==============================
// 리뷰 이미지/파일 업로드 및 모달
// ==============================

// 전역 변수로 선택된 파일 관리
let selectedFiles = [];
let editSelectedFiles = [];
let deleteImageIds = [];

// 이미지 모달
function openImageModal(src) {
    const modal = document.getElementById('imageModal');
    const modalImg = document.getElementById('modalImage');
    if (!modal || !modalImg) return;
    modal.style.display = "flex";
    modalImg.src = src;
}

function closeImageModal() {
    const modal = document.getElementById('imageModal');
    if (!modal) return;
    modal.style.display = "none";
}

// 리뷰 작성 파일 관리
function handleFileSelect(input) {
    const files = Array.from(input.files);

    if (selectedFiles.length + files.length > 5) {
        alert("이미지는 최대 5장까지 업로드 가능합니다.");
        input.value = "";
        return;
    }

    files.forEach(file => {
        const isDuplicate = selectedFiles.some(f => f.name === file.name && f.size === file.size);
        if (isDuplicate) return;

        if (file.size > 10 * 1024 * 1024) {
            alert(`파일 '${file.name}'의 크기가 10MB를 초과합니다.`);
            return;
        }
        selectedFiles.push(file);
    });

    renderFileList();
    input.value = "";
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
    if (!form) return;

    const content = form.querySelector("textarea[name='content']").value;
    const rating = form.querySelector("select[name='rating']").value;

    if (!content.trim()) {
        alert("리뷰 내용을 입력해주세요.");
        return;
    }
    if (content.length < 10) {
        alert("리뷰는 최소 10자 이상 작성해주세요.");
        return;
    }
    if (content.length > 1000) {
        alert("리뷰는 최대 1000자 미만 작성해주세요.");
        return;
    }

    const formData = new FormData();
    formData.append("rating", rating);
    formData.append("content", content);

    selectedFiles.forEach(file => formData.append("images", file));

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
            return;
        }

        alert("리뷰가 성공적으로 등록되었습니다!");
        location.reload();

    } catch (error) {
        console.error(error);
        alert("리뷰 등록 중 통신 오류가 발생했습니다.");
    }
}


// ==============================
// 리뷰 수정
// ==============================
function startEditReview(btn) {
    const reviewId = btn.dataset.reviewId;
    const bookId = btn.dataset.bookId;
    const content = btn.dataset.content;
    const rating = btn.dataset.rating;

    const imagesDataString = btn.dataset.reviewImages || "";
    const existingImages = imagesDataString
        ? imagesDataString.split(',').map(str => {
            const parts = str.split('|');
            return { id: parts[0], url: parts[1] };
        })
        : [];

    editSelectedFiles = [];
    deleteImageIds = [];

    const container = document.querySelector(".my-review-card");
    if (!container) return;

    let existingImagesHtml = '';
    if (existingImages.length > 0) {
        existingImagesHtml = `<div class="existing-images-list">`;
        existingImages.forEach((img, idx) => {
            if (!img.url) return;
            existingImagesHtml += `
                <div class="existing-image-item" id="existing-img-${idx}">
                    <img src="${img.url}">
                    <button type="button" class="btn-remove-image" onclick="markImageAsDeleted('${img.id}', ${idx})">X</button>
                </div>
            `;
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
                <input type="file" id="editFileInput" name="images" multiple accept="image/*"
                       style="display: none;" onchange="handleEditFileSelect(this)">
                <button type="button" class="btn-small"
                        onclick="document.getElementById('editFileInput').click()"
                        style="background-color: #6c757d;">
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
    const el = document.getElementById(`existing-img-${index}`);
    if (el) el.style.display = 'none';
    deleteImageIds.push(parseInt(imageId));
}

function handleEditFileSelect(input) {
    const files = Array.from(input.files);

    files.forEach(file => {
        if (file.size > 10 * 1024 * 1024) {
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
    if (!container) return;

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
    if (!form) return;

    const rating = form.querySelector("select[name='rating']").value;
    const content = form.querySelector("textarea[name='content']").value;

    if (!content.trim()) {
        alert("리뷰 내용을 입력해주세요.");
        return;
    }
    if (content.length < 10) {
        alert("리뷰는 최소 10자 이상 작성해주세요.");
        return;
    }
    if (content.length > 1000) {
        alert("리뷰는 최대 1000자 미만 작성해주세요.");
        return;
    }

    const requestDto = {
        content,
        rating: Number(rating),
        deleteImageIds: deleteImageIds
    };

    const formData = new FormData();
    formData.append("request", new Blob([JSON.stringify(requestDto)], { type: "application/json" }));

    editSelectedFiles.forEach(file => formData.append("images", file));

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
        alert("네트워크 오류가 발생했습니다.");
    }
}


// ==============================
// 태그 클릭 토글
// ==============================
document.addEventListener("DOMContentLoaded", function () {
    const tags = document.querySelectorAll(".book-tag");
    tags.forEach(tag => {
        tag.addEventListener("click", function (e) {
            e.preventDefault();
            tag.classList.toggle("active");
        });
    });
});
