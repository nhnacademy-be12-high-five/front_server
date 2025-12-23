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

function formatMobilePhone(el) {
    let v = (el.value || "").replace(/\D/g, "").slice(0, 11);

    if (v.length <= 3) el.value = v;
    else if (v.length <= 7) el.value = v.replace(/^(\d{3})(\d+)/, "$1-$2");
    else el.value = v.replace(/^(\d{3})(\d{4})(\d{0,4}).*/, "$1-$2-$3").replace(/-$/, "");
}

function isValidMobilePhone(value) {
    const digits = (value || "").replace(/\D/g, "");
    return /^(010|011|016|017|018|019)\d{7,8}$/.test(digits);
}