document.addEventListener("DOMContentLoaded", () => {
    // 1. 서버 에러 메시지 확인 및 Alert
    const errorInput = document.getElementById('server-error-msg');
    const errorMessage = errorInput ? errorInput.value : null;

    if (errorMessage) {
        alert(errorMessage);
    }

    // 2. [추가] 헤더 로그인 버튼 이벤트 연결
    const loginButton = document.querySelector(".login-btn-top");
    if (loginButton) {
        loginButton.addEventListener("click", function () {
            window.location.href = "/member/login";
        });
    }
});

// --- 변수 선언 ---
const sendBtn = document.getElementById('btn-send-email');
const verifyBtn = document.getElementById('btn-verify-code');
const editEmailBtn = document.getElementById('btn-edit-email');

const verifyBox = document.getElementById('email-verify-box');
const emailInput = document.getElementById('regEmail');
const verifiedInput = document.getElementById('emailVerified');

const signupForm = document.getElementById('signup-form');

const idCheckBtn = document.getElementById('btn-check-id');
const editIdBtn = document.getElementById('btn-edit-id');
const idInput = document.getElementById('regId');
const idCheckMsg = document.getElementById('id-check-msg');
const idCheckedInput = document.getElementById('idChecked');

// --- CSRF 설정 ---
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

const getHeaders = (type = 'application/json') => {
    const headers = { 'Content-Type': type };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    return headers;
};

// --- 함수 정의 ---
function resetEmailVerification() {
    verifiedInput.value = "false";
    emailInput.readOnly = false;
    emailInput.classList.remove('input-readonly');

    verifyBox.style.display = 'none';
    sendBtn.disabled = false;
    sendBtn.innerText = "인증번호 받기";

    editEmailBtn.disabled = true;
    document.getElementById('email-code').value = "";
}

function resetIdCheck() {
    idCheckedInput.value = "false";
    idInput.readOnly = false;
    idInput.classList.remove('input-readonly');

    idCheckBtn.disabled = false;
    editIdBtn.disabled = true;

    idCheckMsg.innerText = "";
    idCheckMsg.className = "verify-msg";
}

// --- 이벤트 리스너 ---

// 1. 이메일 인증번호 발송
sendBtn.addEventListener('click', () => {
    const email = emailInput.value;
    if (!email) {
        alert("이메일을 입력해주세요.");
        return;
    }

    sendBtn.disabled = true;
    sendBtn.innerText = "전송 중...";

    fetch('/auth/email/send', {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify({ email })
    })
        .then(async res => {
            if (res.ok) {
                alert("인증번호가 발송되었습니다.");
                verifyBox.style.display = 'block';
                sendBtn.innerText = "재전송";
                sendBtn.disabled = false;
            } else {
                const errorMessage = await res.text();
                alert(errorMessage);
                sendBtn.disabled = false;
                sendBtn.innerText = "인증번호 받기";
            }
        });
});

// 2. 이메일 인증번호 확인
verifyBtn.addEventListener('click', () => {
    const email = emailInput.value;
    const code = document.getElementById('email-code').value;

    if (!code) {
        alert("인증번호를 입력해주세요.");
        return;
    }

    fetch('/auth/email/verify', {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify({ email, code, type: 'SIGNUP' })
    })
        .then(async res => {
            if (res.ok) {
                alert("인증 성공!");
                verifiedInput.value = "true";
                emailInput.readOnly = true;
                emailInput.classList.add('input-readonly');
                sendBtn.disabled = true;
                sendBtn.innerText = "인증 완료";
                editEmailBtn.disabled = false;
                verifyBox.style.display = 'none';
            } else {
                alert("인증 실패");
            }
        });
});

// 3. 이메일 변경
editEmailBtn.addEventListener('click', () => {
    if (!confirm("이메일을 변경하면 다시 인증해야 합니다. 변경할까요?")) return;
    resetEmailVerification();
    emailInput.focus();
});

// 4. 아이디 변경
editIdBtn.addEventListener('click', () => {
    if (!confirm("아이디를 변경하면 다시 중복 확인을 해야 합니다. 변경할까요?")) return;
    resetIdCheck();
    idInput.value = "";
    idInput.focus();
});

// 5. 아이디 중복 확인
idCheckBtn.addEventListener('click', () => {
    const loginId = idInput.value;
    if (!loginId) {
        alert("아이디를 입력해주세요.");
        return;
    }

    fetch(`/auth/check-id?loginId=${encodeURIComponent(loginId)}`, {
        method: 'GET',
        headers: getHeaders('application/json')
    })
        .then(async res => {
            if (!res.ok) throw new Error("통신 오류");
            const isDuplicate = await res.json();

            if (isDuplicate === false) {
                alert("사용 가능한 아이디입니다.");
                idCheckMsg.innerText = "사용 가능한 아이디입니다.";
                idCheckMsg.className = "verify-msg success-text";
                idCheckedInput.value = "true";

                idInput.readOnly = true;
                idInput.classList.add('input-readonly');
                idCheckBtn.disabled = true;
                editIdBtn.disabled = false;
            } else {
                alert("이미 사용 중인 아이디입니다.");
                idCheckMsg.innerText = "이미 사용 중인 아이디입니다.";
                idCheckMsg.className = "verify-msg error-text";
                idCheckedInput.value = "false";
                resetIdCheck();
                idInput.focus();
            }
        })
        .catch(err => {
            console.error(err);
            alert("중복 확인 중 오류가 발생했습니다.");
        });
});

// 6. 아이디 입력 시 중복확인 초기화
idInput.addEventListener('input', () => {
    resetIdCheck();
});

// 7. 폼 제출 시 최종 검증
signupForm.addEventListener('submit', (e) => {
    if (verifiedInput.value !== "true") {
        e.preventDefault();
        alert("이메일 인증을 완료해주세요!");
        return;
    }

    if (idCheckedInput.value !== "true") {
        e.preventDefault();
        alert("아이디 중복 확인을 해주세요!");
        return;
    }

    const pw = document.getElementById('regPw')?.value;
    const pwConfirm = document.getElementById('regPwConfirm')?.value;
    if (pw !== pwConfirm) {
        e.preventDefault();
        alert("비밀번호가 일치하지 않습니다.");
    }
});

// 8. [추가] 전화번호 자동 하이픈 (-) 처리
const phoneInput = document.getElementById('phoneNumber');

const autoHyphen = (target) => {
    target.value = target.value
        .replace(/[^0-9]/g, '')
        .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3")
        .replace(/(\-{1,2})$/g, "");
};

if (phoneInput) {
    phoneInput.addEventListener('input', (e) => {
        autoHyphen(e.target);
    });
}