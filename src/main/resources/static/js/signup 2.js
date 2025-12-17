// 1. 헤더의 로그인 버튼 요소를 찾습니다.
const loginButton = document.querySelector(".login-btn-top");

// 2. 버튼이 존재하면 클릭 이벤트를 연결합니다.
if (loginButton) {
    loginButton.addEventListener("click", function () {
        // 3. 'login.html'로 페이지를 이동시킵니다.
        window.location.href = "member/login";
    });
}