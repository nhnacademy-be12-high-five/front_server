async function updateStatus(orderId) {
    const statusSelect = document.getElementById(`status-${orderId}`);
    const trackingInput = document.getElementById(`tracking-${orderId}`);

    const newStatus = statusSelect.value;
    const trackingNumber = trackingInput.value;

    if (newStatus === 'DELIVERING' && !trackingNumber) {
        alert("배송 중 상태로 변경하려면 운송장 번호를 입력해야 합니다.");
        return;
    }

    if (!confirm(`주문번호 ${orderId}의 상태를 ${newStatus}로 변경하시겠습니까?`)) {
        return;
    }

    try {
        // AdminOrderFrontController의 @PutMapping("/{orderId}/status") 경로 호출
        const response = await fetch(`/admin/orders/${orderId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                status: newStatus,
                trackingNumber: trackingNumber
            })
        });

        if (response.ok) {
            alert("상태가 변경되었습니다.");
            location.reload(); // 페이지 새로고침하여 상태 반영
        } else {
            const error = await response.text();
            alert("변경 실패: " + error);
        }
    } catch (e) {
        console.error(e);
        alert("오류가 발생했습니다.");
    }
}