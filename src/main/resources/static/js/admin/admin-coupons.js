/**
 * /static/js/admin/admin-coupons.js
 * 통합 버전: 도서 검색 + 카테고리 1차/2차 선택
 */

(() => {
    "use strict";

    // ====== [Helper] 요소 선택 및 토스트 메시지 ======
    function $(id) {
        return document.getElementById(id);
    }

    function showToast(message, type = "info") {
        let container = $("adminToastContainer");
        if (!container) {
            container = document.createElement("div");
            container.id = "adminToastContainer";
            container.style.cssText = "position:fixed; top:16px; right:16px; z-index:9999; display:flex; flex-direction:column; gap:10px;";
            document.body.appendChild(container);
        }

        const toast = document.createElement("div");
        toast.textContent = message;
        const bg = type === "success" ? "#16a34a" : type === "error" ? "#dc2626" : type === "warn" ? "#f59e0b" : "#2563eb";
        toast.style.cssText = `padding:12px 14px; border-radius:10px; color:#fff; font-size:14px; font-weight:600; background:${bg}; box-shadow:0 10px 25px rgba(0,0,0,.18); opacity:0; transform:translateY(-4px); transition:all .18s ease;`;

        container.appendChild(toast);
        requestAnimationFrame(() => {
            toast.style.opacity = "1";
            toast.style.transform = "translateY(0)";
        });
        setTimeout(() => {
            toast.style.opacity = "0";
            toast.style.transform = "translateY(-4px)";
            setTimeout(() => toast.remove(), 220);
        }, 3000);
    }

    // ====== [Logic 1] 카테고리 선택 (쿠폰용) ======

    // 1차 카테고리 로드
    function loadCouponRootCategories() {
        const select = $("category-select-1");
        if (!select) return; // 카테고리 선택 태그가 없으면 종료

        console.log("1차 카테고리 로딩 시작..."); // [디버깅용 로그]

        fetch('/api/categories/root')
            .then(res => {
                if (!res.ok) throw new Error(`카테고리 조회 실패 (${res.status})`);
                return res.json();
            })
            .then(data => {
                console.log("1차 카테고리 데이터 수신:", data); // [디버깅용 로그]
                select.innerHTML = '<option value="">1차 카테고리 선택</option>';
                data.forEach(cat => {
                    const option = document.createElement('option');
                    option.value = cat.categoryId;
                    option.text = cat.categoryName;
                    select.appendChild(option);
                });
            })
            .catch(err => {
                console.error("카테고리 로드 에러:", err);
                showToast("카테고리 목록을 불러오지 못했습니다.", "error");
            });
    }

    // 2차 카테고리 로드
    function loadCouponSubCategories(parentId) {
        const subSelect = $("category-select-2");
        const finalInput = $("categoryId");

        if (!parentId) {
            if (subSelect) {
                subSelect.innerHTML = '<option value="">2차 카테고리 선택</option>';
                subSelect.disabled = true;
            }
            if (finalInput) finalInput.value = "";
            return;
        }

        console.log("2차 카테고리 요청: parentId=" + parentId); // [디버깅용 로그]

        fetch(`/api/categories/${parentId}/children`)
            .then(res => res.json())
            .then(data => {
                if (!subSelect) return;
                subSelect.innerHTML = '<option value="">2차 카테고리 선택</option>';

                if (data.length > 0) {
                    subSelect.disabled = false;
                    data.forEach(cat => {
                        const option = document.createElement('option');
                        option.value = cat.categoryId;
                        option.text = cat.categoryName;
                        subSelect.appendChild(option);
                    });
                    if (finalInput) finalInput.value = ""; // 2차 선택 대기
                } else {
                    subSelect.disabled = true;
                    // 하위 카테고리가 없으면 1차 카테고리 ID를 최종값으로 사용
                    if (finalInput) finalInput.value = parentId;
                }
            })
            .catch(err => console.error(err));
    }

    // 최종 카테고리 ID 설정
    function setFinalCouponCategory(subId) {
        const finalInput = $("categoryId");
        if (subId && finalInput) {
            finalInput.value = subId;
            console.log("최종 카테고리 선택됨:", subId);
        }
    }


    // ====== [Logic 2] 도서 검색 (쿠폰용) ======
    const selected = new Map();

    function renderSearchResults(list) {
        const area = $("searchResultArea");
        if (!area) return;

        area.style.display = "block";
        area.innerHTML = "";

        if (!list || list.length === 0) {
            area.innerHTML = "<div style='padding:10px; color:#666;'>검색 결과가 없습니다.</div>";
            return;
        }

        list.forEach(b => {
            const id = b.id || b.bookId;
            const div = document.createElement("div");
            div.style.cssText = "padding:10px; border-bottom:1px solid #eee; display:flex; justify-content:space-between; align-items:center;";
            div.innerHTML = `<span><strong>${b.title}</strong> (${b.author})</span>`;

            const btn = document.createElement("button");
            btn.innerText = "추가";
            btn.style.cssText = "padding:4px 8px; background:#007bff; color:white; border:none; border-radius:4px; cursor:pointer;";
            btn.onclick = () => {
                if(!selected.has(String(id))) {
                    selected.set(String(id), b);
                    renderSelectedList();
                }
            };
            div.appendChild(btn);
            area.appendChild(div);
        });
    }

    function renderSelectedList() {
        const box = $("selectedBookList");
        const hidden = $("targetBookIds");
        if (!box) return;

        box.innerHTML = "";
        const ids = [];

        selected.forEach((b, key) => {
            ids.push(key);
            const li = document.createElement("li");
            li.innerHTML = `${b.title} <button type="button" style="margin-left:10px; color:red; border:none; background:none; cursor:pointer;">[삭제]</button>`;
            li.querySelector("button").onclick = () => {
                selected.delete(key);
                renderSelectedList();
            };
            box.appendChild(li);
        });

        if (hidden) hidden.value = ids.join(",");
    }

    async function searchBooks() {
        const keyword = $("bookKeyword").value.trim();
        if (!keyword) { showToast("검색어를 입력하세요", "warn"); return; }

        try {
            const res = await fetch(`/admin/books/search?keyword=${encodeURIComponent(keyword)}`);
            if(!res.ok) throw new Error("검색 실패");
            const data = await res.json();
            renderSearchResults(Array.isArray(data) ? data : data.content);
        } catch(e) {
            console.error(e);
            showToast("검색 중 오류 발생", "error");
        }
    }

    // ====== [Init] 초기화 실행 ======
    function init() {
        console.log("Admin Coupons JS Initialized!"); // [디버깅용 로그]

        // 1. 카테고리 로드 시도
        loadCouponRootCategories();

        // 2. 도서 선택 목록 초기화
        renderSelectedList();
    }

    // DOM 로드 완료 시 실행
    document.addEventListener("DOMContentLoaded", init);

    // HTML에서 호출 가능하도록 전역 스코프에 등록
    window.searchBooks = searchBooks;
    window.loadCouponSubCategories = loadCouponSubCategories;
    window.setFinalCouponCategory = setFinalCouponCategory;

})();