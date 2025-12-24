(function () {
    const state = {
        redirectUrl: "/member/login?pwChanged=true",
        loginIdRequired: true,
        showLoginId: true,
        sending: false,
        submitting: false,
    };

    const $ = (id) => document.getElementById(id);

    function setDisabled(id, disabled) {
        const el = $(id);
        if (el) el.disabled = disabled;
    }

    function resetUi() {
        const msg = $("pwResetMsg");
        if (msg) { msg.innerText = ""; msg.style.color = "blue"; }

        const area = $("pwResetArea");
        if (area) area.style.display = "none";

        if ($("pwResetAuthCode")) $("pwResetAuthCode").value = "";
        if ($("pwResetNewPw")) $("pwResetNewPw").value = "";
        if ($("pwResetNewPwConfirm")) $("pwResetNewPwConfirm").value = "";

        state.sending = false;
        state.submitting = false;
        setDisabled("pwResetSendBtn", false);
        setDisabled("pwResetSubmitBtn", false);
    }

    function applyLoginIdUi() {
        const loginIdInput = $("pwResetLoginId");
        const loginIdRow = $("pwResetLoginIdRow");
        const loginIdLabel = $("pwResetLoginIdLabel");

        if (loginIdLabel) {
            loginIdLabel.innerText = state.loginIdRequired ? "아이디" : "아이디(선택)";
        }

        if (loginIdRow) {
            loginIdRow.style.display = state.showLoginId ? "block" : "none";
        } else if (loginIdInput) {
            loginIdInput.style.display = state.showLoginId ? "block" : "none";
        }

        if (loginIdInput) {
            loginIdInput.required = !!state.loginIdRequired;
            loginIdInput.placeholder = state.loginIdRequired ? "아이디" : "아이디(선택)";
        }
    }

    window.openPasswordResetDialog = function (opts) {
        const dialog = $("passwordResetDialog");
        if (!dialog) return;

        const o = opts || {};
        state.redirectUrl = o.redirectUrl || "/member/login?pwChanged=true";

        state.loginIdRequired = (o.loginIdRequired !== undefined) ? !!o.loginIdRequired : true;
        state.showLoginId = (o.showLoginId !== undefined) ? !!o.showLoginId : true;

        if ($("pwResetEmail") && typeof o.email === "string") {
            $("pwResetEmail").value = o.email;
        }

        if ($("pwResetEmail")) {
            const readonly = !!o.emailReadonly;
            $("pwResetEmail").readOnly = readonly;
            if (readonly) {
                $("pwResetEmail").style.background = "#f0f0f0";
                $("pwResetEmail").style.color = "#555";
            } else {
                $("pwResetEmail").style.background = "";
                $("pwResetEmail").style.color = "";
            }
        }

        if ($("pwResetLoginId") && typeof o.loginId === "string") {
            $("pwResetLoginId").value = o.loginId;
        } else if ($("pwResetLoginId")) {
            $("pwResetLoginId").value = "";
        }

        applyLoginIdUi();
        resetUi();
        dialog.showModal();
    };

    window.closePasswordResetDialog = function () {
        const dialog = $("passwordResetDialog");
        if (dialog) dialog.close();
    };

    window.sendPasswordResetCode = async function () {
        if (state.sending) return;

        const email = ($("pwResetEmail")?.value || "").trim();
        if (!email) return alert("이메일을 입력해주세요.");

        state.sending = true;
        setDisabled("pwResetSendBtn", true);

        const msg = $("pwResetMsg");
        if (msg) { msg.style.color = "black"; msg.innerText = "전송 중..."; }

        try {
            const res = await fetch("/auth/email/password-reset", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email }),
            });

            if (res.ok) {
                alert("인증번호가 발송되었습니다.");
                if (msg) msg.innerText = "";
                if ($("pwResetArea")) $("pwResetArea").style.display = "block";
            } else {
                const text = await res.text();
                if (msg) msg.innerText = "";
                alert("발송 실패: " + text);
                if ($("pwResetArea")) $("pwResetArea").style.display = "none";
            }
        } catch (e) {
            if (msg) msg.innerText = "";
            alert("발송 중 오류가 발생했습니다. 네트워크 상태를 확인해주세요.");
            if ($("pwResetArea")) $("pwResetArea").style.display = "none";
        } finally {
            state.sending = false;
            setDisabled("pwResetSendBtn", false);
        }
    };

    async function tryLogoutPost() {
        try {
            await fetch("/auth/logout", { method: "POST", credentials: "same-origin" });
        } catch (e) { /* ignore */ }
    }

    window.submitPasswordReset = async function () {
        if (state.submitting) return;

        const loginId = ($("pwResetLoginId")?.value || "").trim();
        const email = ($("pwResetEmail")?.value || "").trim();
        const authCode = ($("pwResetAuthCode")?.value || "").trim();
        const newPw = ($("pwResetNewPw")?.value || "").trim();
        const newPw2 = ($("pwResetNewPwConfirm")?.value || "").trim();

        if (!email) return alert("이메일을 입력해주세요.");

        if (state.loginIdRequired && !loginId) {
            return alert("아이디를 입력해주세요.");
        }

        if (!authCode || !newPw || !newPw2) return alert("인증번호와 새 비밀번호를 모두 입력해주세요.");
        if (newPw !== newPw2) return alert("새 비밀번호가 일치하지 않습니다.");

        state.submitting = true;
        setDisabled("pwResetSubmitBtn", true);

        const msg = $("pwResetMsg");
        if (msg) { msg.style.color = "black"; msg.innerText = "처리 중..."; }

        const payload = { email, authCode, newPassword: newPw };
        if (loginId) payload.loginId = loginId;

        try {
            const res = await fetch("/account/api/find/password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (res.ok) {
                alert("비밀번호가 변경되었습니다.\n보안을 위해 다시 로그인해주세요.");
                await tryLogoutPost();
                location.href = state.redirectUrl;
            } else {
                const errorText = await res.text();
                alert("변경 실패: " + errorText);
                if (msg) { msg.style.color = "red"; msg.innerText = errorText; }
            }
        } catch (e) {
            alert("비밀번호 변경 중 오류가 발생했습니다. 네트워크 상태를 확인해주세요.");
            if (msg) { msg.style.color = "red"; msg.innerText = "요청 실패(네트워크/서버 오류)"; }
        } finally {
            state.submitting = false;
            setDisabled("pwResetSubmitBtn", false);
        }
    };
})();