let searchTimerInterval;

// 장바구니 뱃지
document.addEventListener("DOMContentLoaded", () => {
    updateCartBadge();

    const aiSearchBtn = document.querySelector('.search-btn.ai');
    const loadingOverlay = document.getElementById('aiLoadingOverlay');
    const searchForm = document.querySelector('.search-box');
    const timerSpan = document.getElementById('aiTimer'); // 숫자 들어갈 태그

    if (aiSearchBtn && loadingOverlay && searchForm) {
        aiSearchBtn.addEventListener('click', function(event) {
            const input = document.querySelector('input[name="keyword"]');

            // 검색어가 있을 때만 실행
            if(input && input.value.trim() !== "") {
                event.preventDefault(); // 폼 제출 잠시 멈춤

                // 1. 오버레이 띄우기
                loadingOverlay.style.display = 'flex';

                // 2. 타이머 시작 (0초부터 1초씩 증가)
                if(timerSpan) {
                    let seconds = 0;
                    timerSpan.innerText = seconds; // 초기화

                    // 기존 타이머가 있다면 제거 (안전장치)
                    if (searchTimerInterval) clearInterval(searchTimerInterval);

                    searchTimerInterval = setInterval(() => {
                        seconds++;
                        timerSpan.innerText = seconds;
                    }, 1000); // 1000ms = 1초
                }

                // 3. 약간의 딜레이 후 폼 제출 (화면 렌더링 보장)
                setTimeout(() => {
                    const targetUrl = aiSearchBtn.getAttribute('formaction');
                    if (targetUrl) {
                        searchForm.action = targetUrl;
                    }
                    searchForm.submit();
                }, 50);
            }
        });
    }
});
window.addEventListener("pageshow", (event) => {
    if (event.persisted || (window.performance && window.performance.navigation.type === 2)) {
        updateCartBadge();

        const loadingOverlay = document.getElementById('aiLoadingOverlay');
        if (loadingOverlay) {
            loadingOverlay.style.display = 'none';
        }

        // 중요: 돌아왔을 때 타이머 멈추기
        if (searchTimerInterval) {
            clearInterval(searchTimerInterval);
            searchTimerInterval = null;
        }
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