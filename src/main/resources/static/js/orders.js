function cancelOrder(orderId) {
    if (!confirm('정말 주문을 취소하시겠습니까?')) return;

    fetch(`/orders/${orderId}/cancel`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    })
        .then(response => {
            if (response.ok) {
                alert('주문이 취소되었습니다.');
                window.location.reload();
            } else {
                alert('주문 취소에 실패했습니다.');
            }
        })
        .catch(error => console.error('Error:', error));
}

function confirmPurchase(orderId) {
    if (!confirm('구매를 확정하시겠습니까? (구매 확정 후에는 반품이 불가합니다)')) return;

    fetch(`/orders/${orderId}/confirm`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    })
        .then(response => {
            if (response.ok) {
                alert('구매가 확정되었습니다!');
                window.location.reload();
            } else {
                alert('구매 확정에 실패했습니다.');
            }
        })
        .catch(error => console.error('Error:', error));
}

// 반품 신청 함수
function requestReturn(orderId) {
    if (confirm("반품을 신청하시겠습니까?\n(출고일로부터 10일 이내, 파손/파본은 30일 이내 가능)")) {
        fetch(`/orders/${orderId}/return`, { // 백엔드 API 주소에 맞게 수정
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
            // 필요하다면 반품 사유 등을 body에 추가
        })
            .then(response => {
                if (response.ok) {
                    alert("반품 요청이 접수되었습니다.");
                    window.location.reload();
                } else {
                    alert("반품 신청에 실패했습니다.");
                }
            });
    }
}