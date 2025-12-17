const signupButton = document.querySelector(".login2-btn");

// 2. 버튼이 존재하면 클릭 이벤트를 연결합니다.
if (signupButton) {
    signupButton.addEventListener("click", function (event) {
        // 기본 폼 제출 동작을 막고 페이지 이동만 수행
        event.preventDefault();

        // 회원가입 페이지 경로로 이동
        window.location.href = "/member/signup";
    });
}

const loginBtnTop = document.querySelector(".login-btn-top");

// 2. 버튼이 존재하면 클릭 이벤트를 연결합니다.
if (loginBtnTop) {
    loginBtnTop.addEventListener("click", function (event) {
        event.preventDefault();

        // 회원가입 페이지 경로로 이동
        window.location.href = "/login.html";
    });
}

const mypageLinkInHeader = document.querySelector('a[title="마이페이지"]');

if (mypageLinkInHeader) {
    mypageLinkInHeader.addEventListener("click", function (event) {
        event.preventDefault();
        window.location.href = "myinfo.html";
    });
}

// ==== 비밀번호 보이기/숨기기 함수 ====
function togglePasswordVisibility() {
    // 1. 비밀번호 필드와 아이콘 요소 가져오기
    // HTML의 ID인 'loginPw'를 사용
    const passwordField = document.getElementById("pwd");
    const eyeIcon = document.getElementById("free-icon-eye");

    if (!passwordField) {
        console.error("Error: passwordField element not found.");
        return;
    }

    // 2. 입력 필드의 타입 전환 및 아이콘 변경
    if (passwordField.type === "password") {
        passwordField.type = "text";
        // 아이콘을 닫힌 눈 모양 이미지로 변경 (경로 확인 필요)
        if (eyeIcon) {
            // 브라우저가 인식할 수 있는 절대 경로 사용
            // free-icon-closed-eyes.png 파일이 /static/img/에 있어야 함
            eyeIcon.src = "/img/free-icon-closed-eyes.png";
        }
    } else {
        passwordField.type = "password";
        // 아이콘을 열린 눈 모양 이미지로 변경
        if (eyeIcon) {
            eyeIcon.src = "/img/free-icon-eye.png";
        }
    }
}