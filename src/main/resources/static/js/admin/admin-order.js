/**
 * 주문 상태 변경 및 송장번호 업데이트
 * @param {number} orderId - 주문 ID
 */
function updateStatus(orderId) {
    const statusSelect = document.getElementById(`status-${orderId}`);
    const trackingInput = document.getElementById(`tracking-${orderId}`);

    const newStatus = statusSelect.value;
    const trackingNumber = trackingInput.value ? trackingInput.value.trim() : null;

    // 유효성 검사: 배송 중(DELIVERING) 상태로 변경 시 송장번호 필수
    if (newStatus === 'DELIVERING') {
        if (!trackingNumber) {
            alert('배송 중 상태로 변경하려면 반드시 송장 번호를 입력해야 합니다.');
            trackingInput.focus();
            return;
        }
    }

    // 확인 메시지
    if (!confirm(`주문번호 [${orderId}]의 상태를 '${getSelectedText(statusSelect)}'(으)로 변경하시겠습니까?`)) {
        return;
    }

    // 서버로 전송할 데이터
    const requestData = {
        status: newStatus,
        trackingNumber: trackingNumber
    };

    // AJAX 요청 (PUT)
    fetch(`/admin/orders/${orderId}/status`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (response.ok) {
                alert('주문 상태가 성공적으로 변경되었습니다.');
                location.reload(); // 페이지 새로고침하여 변경사항 반영
            } else {
                // 에러 응답 처리
                return response.text().then(text => { throw new Error(text || '상태 변경 실패'); });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('처리 중 오류가 발생했습니다: ' + error.message);
        });
}

/**
 * 선택된 옵션의 텍스트 가져오기 (알림 메시지용)
 */
function getSelectedText(selectElement) {
    if (selectElement.selectedIndex === -1) return '';
    return selectElement.options[selectElement.selectedIndex].text;
}