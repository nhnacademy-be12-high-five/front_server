document.addEventListener("click", async (e) => {
    const btn = e.target.closest(".btn-danger");
    if (!btn) return;

    const bookId = btn.getAttribute("data-book-id");
    if (!bookId) return;

    try {
        const res = await fetch(`/books/${bookId}/likes`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-USER-ID": "1"
            }
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        // 토글 결과(boolean)로 오지만, 여기서는 UI만 반영
        const card = btn.closest(".like-card");
        if (card) card.remove();

        // 카드 다 지워지면 빈 상태 노출(간단 처리)
        const grid = document.querySelector(".like-grid");
        if (grid && grid.children.length === 0) location.reload();

    } catch (err) {
        alert("찜 해제 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        console.error(err);
    }
});
