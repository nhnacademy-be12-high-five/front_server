// ===========================
// 1. 서버 메시지 처리
// ===========================
const { message, errorMessage } = window.PAGE_ENV || {};

if (message) alert(message);
if (errorMessage) alert(errorMessage);

// ===========================
// 2. 초기 로딩
// ===========================
document.addEventListener("DOMContentLoaded", function () {
    loadParentCategories();
});

// ===========================
// 3. 카테고리 로딩
// ===========================
function loadParentCategories() {
    fetch('/admin/coupons/categories/parent')
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('parentCategory');
            if (!select) return;

            data.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.categoryId;
                option.textContent = cat.categoryName;
                select.appendChild(option);
            });
        })
        .catch(err => console.error("카테고리 로드 실패:", err));
}

function loadChildCategories(parentId) {
    const childSelect = document.getElementById('childCategory');
    if (!childSelect) return;

    childSelect.innerHTML = '<option value="">2차 선택</option>';
    if (!parentId) return;

    fetch(`/admin/coupons/categories/${parentId}/child`)
        .then(res => res.json())
        .then(data => {
            data.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.categoryId;
                option.textContent = cat.categoryName;
                childSelect.appendChild(option);
            });
        });
}

// ===========================
// 4. 카테고리 추가
// ===========================
function addCategory() {
    const parentSelect = document.getElementById('parentCategory');
    const childSelect = document.getElementById('childCategory');
    const list = document.getElementById('selectedCategoryList');
    const input = document.getElementById('targetCategoryIds');

    if (!parentSelect || !input || !list) return;

    let selectedId = childSelect?.value;
    let selectedName = childSelect?.selectedOptions[0]?.text;

    if (!selectedId) {
        selectedId = parentSelect.value;
        selectedName = parentSelect.selectedOptions[0]?.text;
    }

    if (!selectedId) {
        alert("카테고리를 선택해주세요.");
        return;
    }

    let currentIds = input.value
        ? input.value.split(',').map(s => s.trim())
        : [];

    if (currentIds.includes(selectedId)) {
        alert("이미 추가된 카테고리입니다.");
        return;
    }

    const li = document.createElement('li');
    li.textContent = `[${selectedId}] ${selectedName} `;

    const delBtn = document.createElement('button');
    delBtn.textContent = "x";
    delBtn.style.cssText = "margin-left:10px;border:none;background:none;color:red;cursor:pointer;";
    delBtn.onclick = () => {
        list.removeChild(li);
        currentIds = currentIds.filter(id => id !== selectedId);
        input.value = currentIds.join(',');
    };

    li.appendChild(delBtn);
    list.appendChild(li);

    currentIds.push(selectedId);
    input.value = currentIds.join(',');
}
