const fmt = (n) => n.toLocaleString("ko-KR");

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
                .then(res => {
                    if(res.ok) {
                        alert("장바구니가 통합되었습니다.");
                        location.reload();
                    }
                });
        } else {
            if(confirm("그럼 이전 장바구니 내역을 삭제할까요?\n(취소 시 유지됩니다)")) {
                fetch('/cart/guest', { method: 'DELETE', credentials: 'include'})
                    .then(res => {
                        if(res.ok) location.reload();
                    });
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
        .catch(err => console.error(err));
}

// 1. 수량 변경
function changeQuantity(btn, change) {
    const itemRow = btn.closest(".item");
    const bookId = itemRow.getAttribute("data-book-id");
    const price = parseInt(itemRow.getAttribute("data-price"));

    const input = itemRow.querySelector(".num");
    const currentQty = parseInt(input.value);
    let newQty = currentQty + change;

    if (newQty < 1) return;

    fetch('/cart/items', {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        body: JSON.stringify({bookId: bookId, quantity: newQty})
    }).then(res => {
        if (res.ok) {
            input.value = newQty;
            const itemTotalEl = itemRow.querySelector(".price .p");
            itemTotalEl.textContent = fmt(price * newQty);
            updateTotals();
            updateCartBadge();
        } else {
            alert("수량 변경 실패");
            input.value = currentQty;
        }
    });
}

// 2. 단건 삭제
function deleteItem(btn) {
    if (!confirm("삭제하시겠습니까?")) return;

    const itemRow = btn.closest(".item");
    const bookId = itemRow.getAttribute("data-book-id");

    fetch(`/cart/items/${bookId}`, {method: 'DELETE', credentials: 'include'})
        .then(res => {
            if (res.ok) {
                itemRow.remove();
                checkEmptyCart();
                updateTotals();
                updateCartBadge();
            } else {
                alert("삭제 실패");
            }
        });
}

// 3. 전체 비우기
function clearCart() {
    if (!confirm("장바구니를 비우시겠습니까?")) return;

    fetch('/cart/items', {method: 'DELETE', credentials: 'include'})
        .then(res => {
            if (res.ok) {
                const cartList = document.getElementById("cartList");
                cartList.innerHTML = '<div class="empty">장바구니에 담긴 상품이 없습니다.</div>';
                updateTotals();
                updateCartBadge();
            } else {
                alert("실패");
            }
        });
}

// 4. 선택 삭제
async function deleteSelectedItems() {
    const checkboxes = document.querySelectorAll('.item .chk:checked');
    if (checkboxes.length === 0) {
        alert("선택된 상품이 없습니다.");
        return;
    }
    if (!confirm(`${checkboxes.length}개 상품을 삭제하시겠습니까?`)) return;

    const promises = Array.from(checkboxes).map(chk =>
        fetch(`/cart/items/${chk.value}`, {method: 'DELETE', credentials: 'include'})
            .then(res => {
                if(res.ok) {
                    chk.closest('.item').remove();
                }
                return res;
            })
    );

    try {
        await Promise.all(promises);
        checkEmptyCart();
        updateTotals();
        updateCartBadge();
    } catch (e) {
        console.error(e);
        alert("일부 삭제 실패. 다시 시도해주세요.");
        location.reload();
    }
}

function checkEmptyCart() {
    const cartList = document.getElementById("cartList");
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
    if (items.length === 0) {
        document.getElementById("sumProducts").textContent = "0원";
        document.getElementById("sumShipping").textContent = "0원";
        document.getElementById("sumTotal").textContent = "0원";
        return;
    }

    const selected = items.filter(it => it.querySelector(".chk").checked);

    const sum = selected.reduce((acc, it) => {
        const price = Number(it.getAttribute("data-price"));
        const qtyInput = it.querySelector(".num");
        const qty = qtyInput ? Number(qtyInput.value) : 0;
        return acc + (price * qty);
    }, 0);

    let shipping = (sum > 0 && sum < 20000) ? 3000 : 0;

    document.getElementById("sumProducts").textContent = fmt(sum) + "원";
    document.getElementById("sumShipping").textContent = fmt(shipping) + "원";
    document.getElementById("sumTotal").textContent = fmt(sum + shipping) + "원";
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
// [수정됨] 주문 페이지 이동 로직
// ==========================================
function orderSelected() {
    // 1. 체크된 아이템만 선택
    const selectedCheckboxes = document.querySelectorAll('.item .chk:checked');

    if (selectedCheckboxes.length === 0) {
        alert("주문할 상품을 선택해주세요.");
        return;
    }

    // 2. bookIds와 quantities 배열 수집
    let bookIds = [];
    let quantities = [];

    selectedCheckboxes.forEach(chk => {
        const itemRow = chk.closest('.item');
        const bookId = itemRow.getAttribute('data-book-id');
        const qty = itemRow.querySelector('.num').value;

        bookIds.push(bookId);
        quantities.push(qty);
    });

    // 3. GET 파라미터로 전송 (/orders/sheet?bookIds=1,2&quantities=1,2)
    // Spring Controller는 콤마로 구분된 리스트를 자동으로 파싱합니다.
    const url = `/orders/sheet?bookIds=${bookIds.join(",")}&quantities=${quantities.join(",")}`;

    // 4. 페이지 이동
    location.href = url;
}