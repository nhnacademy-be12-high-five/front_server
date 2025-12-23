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

// 반품 모달 열기
function openReturnModal(orderId) {
    document.getElementById('modalOrderId').value = orderId;
    document.getElementById('returnReason').value = '';
    document.getElementById('returnDesc').value = '';
    document.getElementById('feeWarning').style.display = 'none';

    document.getElementById('returnModal').style.display = 'flex';
}

// 반품 모달 닫기
function closeReturnModal() {
    document.getElementById('returnModal').style.display = 'none';
}

// 단순 변심 선택 시 경고 문구 표시
function checkReturnFee() {
    const reason = document.getElementById('returnReason').value;
    const warning = document.getElementById('feeWarning');

    if (reason === 'SIMPLE_CHANGE') {
        warning.style.display = 'block';
    } else {
        warning.style.display = 'none';
    }
}

// 반품 신청 제출 (AJAX)
function submitReturnRequest() {
    const orderId = document.getElementById('modalOrderId').value;
    const reason = document.getElementById('returnReason').value;
    const desc = document.getElementById('returnDesc').value;

    if (!reason) {
        alert('반품 사유를 선택해주세요.');
        return;
    }

    if (!confirm('반품 신청을 하시겠습니까?')) {
        return;
    }

    const data = {
        returnReason: reason,
        description: desc
    };

    fetch(`/mypage/orders/${orderId}/return`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (response.ok) {
                alert('반품 신청이 완료되었습니다.');
                closeReturnModal();
                window.location.reload(); // 상태 반영을 위해 새로고침
            } else {
                return response.text().then(text => { throw new Error(text) });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('반품 신청 중 오류가 발생했습니다.\n' + error.message);
        });
}