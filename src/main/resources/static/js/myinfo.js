/* myinfo.js */

const autoHyphen = (target) => {
    if (!target) return;

    let val = target.value !== undefined ? target.value : target.innerText;

    val = val.replace(/[^0-9]/g, '')
        .substring(0, 11)
        .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3")
        .replace(/(\-{1,2})$/g, "");

    if (target.value !== undefined) {
        target.value = val;
    } else {
        target.innerText = val;
    }
}


document.addEventListener("DOMContentLoaded", () => {
    const viewPhone = document.getElementById("view-phone");
    if (viewPhone) autoHyphen(viewPhone);

    const editPhone = document.querySelector("input[name='phone']");
    if (editPhone) autoHyphen(editPhone);

    handleAlertMessage();
});

function handleAlertMessage() {
    const serverMsgInput = document.getElementById('server-alert-msg');
    const modelMessage = serverMsgInput ? serverMsgInput.value : null;

    const urlParams = new URLSearchParams(window.location.search);
    const urlMessage = urlParams.get('errorMessage');

    const finalMessage = modelMessage || urlMessage;

    if (finalMessage) {
        alert(finalMessage);

        if (finalMessage.includes("형식") || finalMessage.includes("필수") || finalMessage.includes("입력")) {
            setTimeout(() => toggleEditMode(true), 100);
        }

        if (urlMessage) {
            history.replaceState(null, "", location.pathname);
        }
    }
}

function toggleEditMode(isEdit) {
    const viewMode = document.getElementById('view-mode');
    const editForm = document.getElementById('edit-form');

    if (viewMode && editForm) {
        viewMode.style.display = isEdit ? 'none' : 'block';
        editForm.style.display = isEdit ? 'block' : 'none';

        if (!isEdit) {
            editForm.reset();
            const editPhone = document.querySelector("input[name='phone']");
            if (editPhone) autoHyphen(editPhone);
        }
    }
}

const withdrawBtn = document.getElementById("btn-withdraw");
const withdrawForm = document.getElementById("withdraw-form");

if (withdrawBtn) {
    withdrawBtn.addEventListener("click", e => {
        e.preventDefault();
        if (confirm("정말 탈퇴하시겠습니까?")) {
            withdrawForm.submit();
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("edit-form");
    if (!form) return;

    const phoneInput = form.querySelector("input[name='phone']");
    if (!phoneInput) return;

    phoneInput.addEventListener("input", () => autoHyphen(phoneInput));

    form.addEventListener("submit", (e) => {
        if (!isValidMobilePhone(phoneInput.value)) {
            e.preventDefault();
            alert("휴대폰 번호 형식이 올바르지 않습니다.\n예) 010-1234-5678");
            phoneInput.focus();
        }
    });
});