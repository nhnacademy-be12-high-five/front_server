function toggleEditMode(isEdit) {
    document.getElementById('view-mode').style.display = isEdit ? 'none' : 'block';
    document.getElementById('edit-form').style.display = isEdit ? 'block' : 'none';
    if (!isEdit) document.getElementById('edit-form').reset();
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