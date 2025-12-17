// 장바구니 뱃지
document.addEventListener("DOMContentLoaded", () => {
    updateCartBadge();
});
window.addEventListener("pageshow", (event) => {
    if (event.persisted || (window.performance && window.performance.navigation.type === 2)) {
        updateCartBadge();
    }
});
function updateCartBadge() {
    fetch('/cart/count', {
        method: 'GET',
        headers: {'Content-Type': 'application/json'}
    })
        .then(res => {
            if (res.ok) return res.json();
            throw new Error('fail');
        })
        .then(count => {
            const badge = document.getElementById('cartBadge');
            if (badge) {
                badge.textContent = count || 0;
                badge.style.display = 'block';
            }
        })
        .catch(err => {
            console.error(err);
            const badge = document.getElementById('cartBadge');
            if (badge) {
                badge.textContent = 0;
                badge.style.display = 'block';
            }
        });
}