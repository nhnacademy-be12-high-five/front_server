// 프론트 단에서 한번 더 확인만 띄우고, 실제 처리는 백엔드 API 연동 예정
const confirmBtn = document.getElementById("btn-confirm");
if (confirmBtn) {
    confirmBtn.addEventListener("click", () => {
        if (confirmBtn.disabled) return;
        const ok = confirm("이 주문을 구매확정 처리하시겠습니까?");
        if (!ok) return;

        // TODO: fetch('/orders/{id}/confirm', { method: 'POST' ... }) 등으로 연동
        alert("구매확정 처리되었습니다. (백엔드 연동 시 실제 처리)");
        confirmBtn.disabled = true;
        confirmBtn.textContent = "구매확정 완료";
    });
}