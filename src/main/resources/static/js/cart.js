const fmt = (n) => n.toLocaleString("ko-KR");

// 공통 에러 처리 함수
async function handleFetchError(response) {
    if (response.ok) return true;

    let msg = "요청 처리 중 문제가 발생했습니다.";
    try {
        const errorData = await response.json(); // 서버에서 에러 메시지를 JSON으로 준다면 사용
        if (errorData && errorData.message) msg = errorData.message;
    } catch (e) {
        // JSON 파싱 실패 시 기본 상태 코드별 메시지 사용
        switch (response.status) {
            case 400: msg = "잘못된 요청입니다. 입력 값을 확인해주세요."; break;
            case 401: msg = "로그인이 필요한 서비스입니다."; break;
            case 403: msg = "접근 권한이 없습니다."; break;
            case 404: msg = "해당 상품을 찾을 수 없습니다."; break;
            case 409: msg = "재고가 부족하거나 상태가 변경되어 처리할 수 없습니다."; break;
            case 500: msg = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."; break;
            case 502: case 503: msg = "서버 점검 중이거나 연결이 원활하지 않습니다."; break;
        }
    }

    alert(msg);
    if (response.status === 401) {
        location.href = "/member/login.html";
    }
    return false;
}

// 초기화
document.addEventListener("DOMContentLoaded", () => {
    updateTotals();
    updateCartBadge();
    checkAndMergeCart();
});

function checkAndMergeCart() {
    const hasGuestCart = window.HAS_GUEST_CART || false;

    if (hasGuestCart) {
        if (confirm("로그인 전 담아둔 장바구니 내역이 있습니다.\n회원 장바구니와 합치시겠습니까?")) {
            fetch('/cart/merge', { method: 'POST', credentials: 'include'})
                .then(async res => {
                    if(await handleFetchError(res)) {
                        alert("장바구니가 통합되었습니다.");
                        location.reload();
                    }
                })
                .catch(err => alert("서버와 통신 중 오류가 발생했습니다."));
        } else {
            if(confirm("그럼 이전 장바구니 내역을 삭제할까요?\n(취소 시 유지됩니다)")) {
                fetch('/cart/guest', { method: 'DELETE', credentials: 'include'})
                    .then(res => { if(res.ok) location.reload(); });
            }
        }
    }
}

// 뒤로가기 대응
window.addEventListener("pageshow", (event) => {
    if (event.persisted || (window.performance && window.performance.navigation.type === 2)) {
        updateCartBadge();
    }
});

// 뱃지 업데이트
function updateCartBadge() {
    fetch('/cart/count', {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include'
    })
        .then(res => res.ok ? res.json() : 0)
        .then(count => {
            const badge = document.getElementById('cartBadge');
            if (badge) {
                badge.textContent = count || 0;
                badge.style.display = 'block';
            }
        })
        .catch(err => console.error("뱃지 업데이트 실패:", err));
}

// 1. 수량 변경 (에러 처리 강화)
function changeQuantity(btn, change) {
    const itemRow = btn.closest(".item");
    const input = itemRow.querySelector(".num");
    const priceEl = itemRow.querySelector(".price .p");

    // 1. 현재 값과 변동 예정 값 계산
    const originalQty = parseInt(input.value);
    const price = parseInt(itemRow.getAttribute("data-price"));
    let newQty = originalQty + change;

    // 최소 수량 방어
    if (newQty < 1) {
        alert("최소 수량은 1개입니다.");
        return;
    }

    // 2. [Optimistic UI] 서버 응답 대기 없이 UI 먼저 업데이트!
    input.value = newQty;
    priceEl.textContent = fmt(price * newQty);
    updateTotals(); // 총 합계도 즉시 재계산

    // 3. 백그라운드 요청 전송
    const bookId = itemRow.getAttribute("data-book-id");

    fetch('/cart/items', {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        body: JSON.stringify({bookId: bookId, quantity: newQty})
    }).then(async res => {
        if (!res.ok) {
            throw new Error("서버 응답 실패");
        }
        // 성공 시: 이미 UI는 바뀌어 있으므로 뱃지만 갱신하면 됨
        updateCartBadge();
    }).catch(err => {
        // 4. 실패 시: UI 롤백 (원래 값으로 복구)
        console.error("수량 변경 실패:", err);
        alert("일시적인 오류로 수량 변경에 실패했습니다.");

        input.value = originalQty;
        priceEl.textContent = fmt(price * originalQty);
        updateTotals(); // 합계도 원상복구
    });
}

// 2. 단건 삭제
function deleteItem(btn) {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    const itemRow = btn.closest(".item");
    const bookId = itemRow.getAttribute("data-book-id");

    itemRow.style.display = 'none';

    // 체크박스가 체크되어 있었다면 합계에서 빠져야 함
    const chk = itemRow.querySelector(".chk");
    const wasChecked = chk.checked;
    chk.checked = false; // 계산에서 제외
    updateTotals();

    fetch(`/cart/items/${bookId}`, {method: 'DELETE', credentials: 'include'})
        .then(res => {
            if (res.ok) {
                itemRow.remove(); // 진짜 삭제
                checkEmptyCart();
                updateCartBadge();
            } else {
                throw new Error("삭제 실패");
            }
        })
        .catch(err => {
            // 실패 시 롤백 (다시 보여줌)
            alert("삭제 처리에 실패했습니다.");
            itemRow.style.display = ''; // 다시 보임
            if(wasChecked) chk.checked = true; // 체크 상태 복구
            updateTotals();
        });
}

// 3. 전체 비우기
function clearCart() {
    if (!confirm("장바구니를 모두 비우시겠습니까?")) return;

    const cartList = document.getElementById("cartList");
    // 1. [Backup] 롤백을 위해 현재 상태 저장
    const previousHTML = cartList.innerHTML;
    const previousProducts = document.getElementById("sumProducts")?.textContent;
    const previousShipping = document.getElementById("sumShipping")?.textContent;
    const previousTotal = document.getElementById("sumTotal")?.textContent;

    // 2. [Optimistic UI] 즉시 UI 비우기
    cartList.innerHTML = '<div class="empty">장바구니에 담긴 상품이 없습니다.</div>';
    updateTotals(); // 0원으로 갱신
    // 뱃지는 서버 응답 후 갱신하거나, 여기서 0으로 만들어도 됨 (여기선 0으로 즉시 반영)
    const badge = document.getElementById('cartBadge');
    if(badge) badge.textContent = '0';

    // 3. 서버 요청
    fetch('/cart/items', {method: 'DELETE', credentials: 'include'})
        .then(res => {
            if (!res.ok) throw new Error("전체 삭제 실패");
            // 성공 시 별도 작업 필요 없음 (이미 UI는 비워져 있음)
        })
        .catch(err => {
            // 4. [Rollback] 실패 시 원상 복구
            console.error(err);
            alert("장바구니 비우기에 실패했습니다. 잠시 후 다시 시도해주세요.");

            cartList.innerHTML = previousHTML;
            if(document.getElementById("sumProducts")) document.getElementById("sumProducts").textContent = previousProducts;
            if(document.getElementById("sumShipping")) document.getElementById("sumShipping").textContent = previousShipping;
            if(document.getElementById("sumTotal")) document.getElementById("sumTotal").textContent = previousTotal;

            updateCartBadge();
        });
}

// 4. 선택 삭제
async function deleteSelectedItems() {
    const checkboxes = document.querySelectorAll('.item .chk:checked');
    if (checkboxes.length === 0) {
        alert("삭제할 상품을 선택해주세요.");
        return;
    }
    if (!confirm(`선택한 ${checkboxes.length}개 상품을 삭제하시겠습니까?`)) return;

    // 1. [Backup & UI Update] 선택된 항목 숨기기 (삭제된 척)
    const itemsToDelete = []; // 나중에 복구를 위해 저장

    checkboxes.forEach(chk => {
        const itemRow = chk.closest('.item');
        // 상태 저장
        itemsToDelete.push({
            row: itemRow,
            wasChecked: chk.checked
        });

        // UI에서 즉시 안 보이게 처리 (remove 대신 display: none 권장 -> 복구 쉬움)
        itemRow.style.display = 'none';
        chk.checked = false; // 합계 계산에서 빠지도록 체크 해제
    });

    // 합계 즉시 갱신
    updateTotals();

    // 2. 서버 요청 (병렬 처리)
    const promises = itemsToDelete.map(item => {
        const bookId = item.row.getAttribute("data-book-id"); // 혹은 chk.value
        return fetch(`/cart/items/${bookId}`, {method: 'DELETE', credentials: 'include'})
            .then(res => {
                if (!res.ok) throw new Error(bookId); // 실패한 ID 던짐
                return { success: true, bookId };
            })
            .catch(err => {
                return { success: false, bookId };
            });
    });

    try {
        const results = await Promise.all(promises);

        // 3. 결과 확인 및 후처리
        const failedItems = results.filter(r => !r.success);

        if (failedItems.length > 0) {
            // 실패한 항목이 있으면 그 항목들만 다시 보이게 롤백
            let rollbackCount = 0;
            itemsToDelete.forEach(item => {
                const bookId = item.row.getAttribute("data-book-id");
                // 실패 목록에 있는 아이템만 복구
                if (failedItems.some(f => f.bookId === bookId)) {
                    item.row.style.display = ''; // 다시 보임
                    item.row.querySelector('.chk').checked = true; // 체크 상태 복구
                    rollbackCount++;
                } else {
                    // 성공한 항목은 아예 DOM에서 제거
                    item.row.remove();
                }
            });

            alert(`${rollbackCount}개 상품 삭제에 실패하여 목록에 복구되었습니다.`);
            updateTotals(); // 복구된 항목 포함하여 합계 재계산
        } else {
            // 전부 성공 시 DOM 완전히 정리
            itemsToDelete.forEach(item => item.row.remove());
            checkEmptyCart(); // 다 지워서 비었는지 확인
        }

        // 최종 뱃지 상태 동기화 (서버 기준)
        updateCartBadge();

    } catch (e) {
        console.error(e);
        // 예상치 못한 에러 시 전체 리로드 (안전장치)
        location.reload();
    }
}

function checkEmptyCart() {
    const cartList = document.getElementById("cartList");
    // 요소가 없으면(이미 비워진 상태) 에러 나지 않게 체크
    if(!cartList) return;

    const items = cartList.querySelectorAll(".item");
    if (items.length === 0) {
        cartList.innerHTML = '<div class="empty">장바구니에 담긴 상품이 없습니다.</div>';
    }
}

// 5. 합계 계산
function updateTotals() {
    const listEl = document.getElementById("cartList");
    if (!listEl) return;

    const items = [...listEl.querySelectorAll(".item")];
    // 아이템이 없으면 0원 처리
    if (items.length === 0) {
        if(document.getElementById("sumProducts")) document.getElementById("sumProducts").textContent = "0원";
        if(document.getElementById("sumShipping")) document.getElementById("sumShipping").textContent = "0원";
        if(document.getElementById("sumTotal")) document.getElementById("sumTotal").textContent = "0원";
        return;
    }

    const selected = items.filter(it => it.querySelector(".chk").checked);

    const sum = selected.reduce((acc, it) => {
        const price = Number(it.getAttribute("data-price"));
        const qtyInput = it.querySelector(".num");
        let qty = qtyInput ? Number(qtyInput.value) : 0;

        // 비정상적인 값 방어 코드
        if(isNaN(qty) || qty < 1) qty = 1;

        return acc + (price * qty);
    }, 0);

    let shipping = (sum > 0 && sum < 20000) ? 3000 : 0;

    if(document.getElementById("sumProducts")) document.getElementById("sumProducts").textContent = fmt(sum) + "원";
    if(document.getElementById("sumShipping")) document.getElementById("sumShipping").textContent = fmt(shipping) + "원";
    if(document.getElementById("sumTotal")) document.getElementById("sumTotal").textContent = fmt(sum + shipping) + "원";
}

// 이벤트 리스너들
const listEl = document.getElementById("cartList");
if (listEl) {
    listEl.addEventListener("change", (e) => {
        if (e.target.classList.contains("chk")) updateTotals();
    });
}

function toggleAllCheckboxes(checked) {
    document.querySelectorAll(".item .chk").forEach(chk => chk.checked = checked);
    updateTotals();
}

const chkAll = document.getElementById("chkAll");
if (chkAll) {
    chkAll.addEventListener("change", (e) => toggleAllCheckboxes(e.target.checked));
}

const chkAllBottom = document.getElementById("chkAllBottom");
if (chkAllBottom) {
    chkAllBottom.addEventListener("change", (e) => toggleAllCheckboxes(e.target.checked));
}

// ==========================================
// 주문 페이지 이동 로직 (유효성 검사 강화)
// ==========================================
function orderSelected() {
    const selectedCheckboxes = document.querySelectorAll('.item .chk:checked');

    if (selectedCheckboxes.length === 0) {
        alert("주문할 상품을 하나 이상 선택해주세요.");
        return;
    }

    let bookIds = [];
    let quantities = [];
    let hasInvalidQty = false;

    selectedCheckboxes.forEach(chk => {
        const itemRow = chk.closest('.item');
        const bookId = itemRow.getAttribute('data-book-id');
        const qtyVal = itemRow.querySelector('.num').value;
        const qty = parseInt(qtyVal);

        // 수량 유효성 검사 한 번 더 수행
        if (isNaN(qty) || qty < 1) {
            hasInvalidQty = true;
        }

        bookIds.push(bookId);
        quantities.push(qty);
    });

    if (hasInvalidQty) {
        alert("수량이 올바르지 않은 상품이 있습니다. 확인 후 다시 시도해주세요.");
        return;
    }

    const url = `/orders/sheet?bookIds=${bookIds.join(",")}&quantities=${quantities.join(",")}`;
    location.href = url;
}

document.addEventListener("DOMContentLoaded", function() {
    loadAiRecommendations();
});

function loadAiRecommendations() {
    // [수정 1] HTML에 작성된 클래스명(.item-title)으로 변경
    const titles = Array.from(document.querySelectorAll('.item-title'))
        .map(el => el.textContent.trim());

    // 장바구니가 비어있으면 추천 영역 숨김
    if (titles.length === 0) {
        document.querySelector('.ai-recommend-container').style.display = 'none';
        return;
    }

    fetch('/books/recommendations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(titles)
    })
        .then(response => response.json())
        .then(books => {
            const loadingDiv = document.getElementById('ai-loading');
            const listDiv = document.getElementById('ai-book-list');

            loadingDiv.style.display = 'none';

            if (books.length > 0) {
                listDiv.style.display = 'flex';

                books.forEach(book => {
                    // [수정 2] CSS 파일에 정의된 클래스(.ai-card 등)를 사용하도록 마크업 변경
                    // 기존 Bootstrap 클래스(card, col 등) 대신 작성하신 CSS 클래스 적용
                    const cardHtml = `
                    <div class="ai-card">
                        <img src="${book.thumbnail}" alt="${book.title}">
                        <div class="ai-card-body">
                            <h5 class="ai-card-title">${book.title}</h5>
                            <p class="card-text text-muted" style="font-size:13px; margin-bottom:10px;">${book.author}</p>
                            <a href="/books/${book.id}" class="btn btn-primary btn-sm" style="display:block; text-align:center;">상세보기</a>
                        </div>
                    </div>
                `;
                    listDiv.insertAdjacentHTML('beforeend', cardHtml);
                });
            } else {
                document.querySelector('.ai-recommend-container').style.display = 'none';
            }
        })
        .catch(error => {
            console.error("AI 추천 로드 실패:", error);
            document.querySelector('.ai-recommend-container').style.display = 'none';
        });
}