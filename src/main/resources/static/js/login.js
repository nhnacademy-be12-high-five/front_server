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
function togglePasswordVisibility(id, icon) {
    // 1. 넘겨받은 ID로 해당 input 찾기
    const inputField = document.getElementById(id);

    // 요소가 없으면 중단
    if (!inputField) {
        console.error("해당 ID의 input을 찾을 수 없습니다: " + id);
        return;
    }

    // 2. 타입 토글 및 아이콘 변경
    if (inputField.type === "password") {
        inputField.type = "text"; // 비밀번호 보이기
        // 클릭된 그 아이콘의 이미지를 변경
        icon.src = "/img/free-icon-closed-eyes.png";
    } else {
        inputField.type = "password"; // 비밀번호 숨기기
        icon.src = "/img/free-icon-eye.png";
    }
}

const tabMember = document.getElementById("tabMember");
const tabGuest = document.getElementById("tabGuest");
const formMember = document.getElementById("formMember");
const formGuest = document.getElementById("formGuest");

// 회원 로그인 탭 클릭 시
if (tabMember) {
    tabMember.addEventListener("click", function () {

        // 탭 스타일 변경
        tabMember.classList.add("active");
        tabMember.classList.remove("unactive");
        tabGuest.classList.add("unactive");
        tabGuest.classList.remove("active");

        console.log("회원 선택");

        // 폼 표시/숨김
        formMember.style.display = "block";
        formGuest.style.display = "none";
    });
}

// 비회원 주문조회 탭 클릭 시
if (tabGuest) {
    tabGuest.addEventListener("click", function () {

        console.log("비회원 선택");

        // 탭 스타일 변경
        tabGuest.classList.add("active");
        tabGuest.classList.remove("unactive");
        tabMember.classList.add("unactive");
        tabMember.classList.remove("active");

        // 폼 표시/숨김
        formMember.style.display = "none";
        formGuest.style.display = "block";
    });
}
