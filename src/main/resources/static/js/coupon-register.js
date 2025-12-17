document.addEventListener('DOMContentLoaded', () => {
    const err = window.PAGE_ERROR_MESSAGE;
    const msg = window.PAGE_MESSAGE;

    // 값이 있을 때만 alert (null/undefined/빈문자열이면 스킵)
    if (typeof err === 'string' && err.trim().length > 0) {
        alert(err);
        return;
    }

    if (typeof msg === 'string' && msg.trim().length > 0) {
        alert(msg);
    }
});
