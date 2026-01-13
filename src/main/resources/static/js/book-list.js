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

document.addEventListener("DOMContentLoaded", function() {
    // 1. 상단 AI 메인 요약 변환
    const mainSummaryContainer = document.querySelector('.ai-main-summary-body');
    if (mainSummaryContainer) {
        // 기존 <p> 태그 안에 있는 텍스트(마크다운)를 가져옵니다.
        const pTag = mainSummaryContainer.querySelector('p');
        if (pTag) {
            const rawMarkdown = pTag.textContent;
            // marked 라이브러리가 로드되었는지 확인 후 변환
            if (typeof marked !== 'undefined' && rawMarkdown.trim().length > 0) {
                // <p> 태그를 제거하고 변환된 HTML로 교체
                mainSummaryContainer.innerHTML = marked.parse(rawMarkdown);
            }
        }
    }

    // 2. 도서 카드 내부 AI 설명 변환 (전체보기 부분만)
    const bookSnippetScrolls = document.querySelectorAll('.ai-snippet-scroll');
    bookSnippetScrolls.forEach(scrollBox => {
        const pTag = scrollBox.querySelector('p');
        if (pTag) {
            const rawMarkdown = pTag.textContent;
            if (typeof marked !== 'undefined' && rawMarkdown.trim().length > 0) {
                scrollBox.innerHTML = marked.parse(rawMarkdown);
            }
        }
    });
});