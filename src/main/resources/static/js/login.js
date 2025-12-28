const signupButton = document.querySelector(".login2-btn");

if (signupButton) {
    signupButton.addEventListener("click", function (event) {
        event.preventDefault();
        window.location.href = "/member/signup";
    });
}

const loginBtnTop = document.querySelector(".login-btn-top");
if (loginBtnTop) {
    loginBtnTop.addEventListener("click", function (event) {
        event.preventDefault();
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
    const inputField = document.getElementById(id);
    if (!inputField) return;

    if (inputField.type === "password") {
        inputField.type = "text";
        icon.src = "/img/free-icon-closed-eyes.png";
    } else {
        inputField.type = "password";
        icon.src = "/img/free-icon-eye.png";
    }
}

const tabMember = document.getElementById("tabMember");
const tabGuest = document.getElementById("tabGuest");
const formMember = document.getElementById("formMember");
const formGuest = document.getElementById("formGuest");

if (tabMember) {
    tabMember.addEventListener("click", function () {
        tabMember.classList.add("active");
        tabMember.classList.remove("unactive");
        tabGuest.classList.add("unactive");
        tabGuest.classList.remove("active");
        formMember.style.display = "block";
        formGuest.style.display = "none";
    });
}

if (tabGuest) {
    tabGuest.addEventListener("click", function () {
        tabGuest.classList.add("active");
        tabGuest.classList.remove("unactive");
        tabMember.classList.add("unactive");
        tabMember.classList.remove("active");
        formMember.style.display = "none";
        formGuest.style.display = "block";
    });
}
const dormantDialog = document.getElementById('dormantDialog');

function openDormantDialog() {
    const dialog = document.getElementById('dormantDialog');

    if (dialog) {
        const msg = document.getElementById('dormantMsg');
        if (msg) {
            msg.innerText = "";
            msg.style.color = "blue";
        }

        const idInput = document.getElementById('dormantLoginId');
        const emailInput = document.getElementById('dormantEmail');
        const codeInput = document.getElementById('dormantAuthCode');

        if (idInput) idInput.value = "";
        if (emailInput) emailInput.value = "";
        if (codeInput) codeInput.value = "";

        const step2 = document.getElementById('dormantStep2');
        if (step2) step2.style.display = 'none';

        dialog.showModal();
    } else {
        console.error("dormantDialog를 찾을 수 없습니다.");
    }
}

function closeDormantDialog() {
    const dialog = document.getElementById('dormantDialog');
    if (dialog) {
        dialog.close();
    }
}

function sendDormantCode() {
    const loginId = document.getElementById('dormantLoginId').value;
    const email = document.getElementById('dormantEmail').value;

    if (!loginId || !email) {
        alert("아이디와 이메일을 모두 입력해주세요.");
        return;
    }

    const msg = document.getElementById('dormantMsg');
    if (msg) {
        msg.style.color = "black";
        msg.innerText = "정보 확인 및 발송 중...";
    }

    fetch('/auth/dormant/send', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            loginId: loginId,
            email: email,
            type: 'ACTIVATE'
        })
    }).then(async res => {
        if (res.ok) {
            alert("인증번호가 발송되었습니다.");
            if (msg) {
                msg.style.color = "blue";
                msg.innerText = "이메일로 전송된 인증번호를 입력하세요.";
            }

            // [추가] 성공 시 Step2(인증번호 입력란) 보이기
            const step2 = document.getElementById('dormantStep2');
            if (step2) step2.style.display = 'block';

        } else {
            const errorText = await res.text();
            if (msg) {
                msg.style.color = "red";
                msg.innerText = errorText;
            }
        }
    }).catch(err => {
        console.error(err);
        if (msg) msg.innerText = "서버 통신 오류";
    });
}

async function submitDormantActivation() {
    const loginId = document.getElementById('dormantLoginId').value;
    const email = document.getElementById('dormantEmail').value;
    const code  = document.getElementById('dormantAuthCode').value;

    if (!code) {
        alert("인증번호를 입력해주세요.");
        return;
    }

    try {
        const response = await fetch('/auth/dormant/verify', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                loginId: loginId,
                email: email,
                authCode: code,
                type: 'ACTIVATE'
            })
        });

        if (response.ok) {
            alert("휴면 상태가 해제되었습니다! 로그인해주세요.");
            closeDormantDialog();
            window.location.reload();
        } else {
            const errorText = await response.text();
            const msg = document.getElementById('dormantMsg');
            if (msg) {
                msg.style.color = "red";
                msg.innerText = errorText;
            }
        }
    } catch (e) {
        console.error(e);
        alert("시스템 오류가 발생했습니다.");
    }
}