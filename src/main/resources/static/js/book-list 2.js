// 상단 AI 요약 토글
function toggleMainAiSummary(btn) {
    const wrapper = btn.closest('.ai-main-summary');
    if (!wrapper) return;

    const body = wrapper.querySelector('.ai-main-summary-body');
    if (!body) return;

    const isCollapsed = body.classList.contains('collapsed');

    if (isCollapsed) {
        body.classList.remove('collapsed');
        body.classList.add('expanded');
        btn.textContent = '간략히 보기 ▴';
    } else {
        body.classList.remove('expanded');
        body.classList.add('collapsed');
        btn.textContent = '자세히 보기 ▾';
    }
}

// 카드 안 AI 설명 토글 (상세페이지로 이동하지 않도록 이벤트 막기)
function toggleAiDesc(btn, e) {
    if (e) {
        e.preventDefault();    // 링크 이동 막기
        e.stopPropagation();   // <a> 클릭 이벤트 전파 막기
    }

    const targetId = btn.getAttribute('data-target');
    const box = document.getElementById(targetId);
    if (!box) return;

    const isOpen = box.style.display === 'block';
    box.style.display = isOpen ? 'none' : 'block';

    btn.textContent = isOpen ? '자세히 보기 ▾' : '간략히 보기 ▴';
}