/**
 * /static/js/admin/admin-coupons.js
 * - 특정 도서 쿠폰 정책 페이지 진입 시 alert("null") 방지
 * - 도서 검색/선택(검색 결과 렌더링, 선택 목록/hidden 동기화)
 * - onsubmit="return prepareSubmit()" / onclick="searchBooks()" 등 인라인 호출 지원(전역 함수 노출)
 */

(() => {
    "use strict";

    // ====== Safe helpers ======
    function normalizeMsg(v) {
        // Thymeleaf에서 null이면 JS null로 들어오거나, "null" 문자열로 들어올 수도 있어서 둘 다 방어
        if (v === null || v === undefined) return "";
        const s = String(v).trim();
        if (s === "" || s.toLowerCase() === "null" || s.toLowerCase() === "undefined") return "";
        return s;
    }

    function showToast(message, type = "info") {
        const msg = normalizeMsg(message);
        if (!msg) return;

        // 이미 토스트 컨테이너 있으면 재사용
        let container = document.getElementById("adminToastContainer");
        if (!container) {
            container = document.createElement("div");
            container.id = "adminToastContainer";
            container.style.position = "fixed";
            container.style.top = "16px";
            container.style.right = "16px";
            container.style.zIndex = "9999";
            container.style.display = "flex";
            container.style.flexDirection = "column";
            container.style.gap = "10px";
            document.body.appendChild(container);
        }

        const toast = document.createElement("div");
        toast.textContent = msg;
        toast.style.padding = "12px 14px";
        toast.style.borderRadius = "10px";
        toast.style.color = "#fff";
        toast.style.fontSize = "14px";
        toast.style.fontWeight = "600";
        toast.style.boxShadow = "0 10px 25px rgba(0,0,0,.18)";
        toast.style.maxWidth = "360px";
        toast.style.lineHeight = "1.35";
        toast.style.opacity = "0";
        toast.style.transform = "translateY(-4px)";
        toast.style.transition = "all .18s ease";

        const bg =
            type === "success" ? "#16a34a" :
                type === "error"   ? "#dc2626" :
                    type === "warn"    ? "#f59e0b" :
                        "#2563eb";
        toast.style.background = bg;

        container.appendChild(toast);

        requestAnimationFrame(() => {
            toast.style.opacity = "1";
            toast.style.transform = "translateY(0)";
        });

        setTimeout(() => {
            toast.style.opacity = "0";
            toast.style.transform = "translateY(-4px)";
            setTimeout(() => toast.remove(), 220);
        }, 2800);
    }

    function $(id) {
        return document.getElementById(id);
    }

    // ====== State ======
    const selected = new Map(); // bookId -> { id, title, author, isbn }

    // ====== PAGE_ENV 메시지 처리 (null 팝업 방지 핵심) ======
    function showEnvMessagesIfAny() {
        // PAGE_ENV 자체가 없는 페이지에서도 에러 없이 지나가게 처리
        const env = window.PAGE_ENV || {};
        const message = normalizeMsg(env.message);
        const errorMessage = normalizeMsg(env.errorMessage);

        if (message) showToast(message, "success");
        if (errorMessage) showToast(errorMessage, "error");
    }

    // ====== Selected list UI ======
    function syncHiddenTargetBookIds() {
        const hidden = $("targetBookIds");
        if (!hidden) return;
        hidden.value = Array.from(selected.keys()).join(",");
    }

    function renderSelectedList() {
        const box = $("selectedBookList");
        if (!box) return;

        box.innerHTML = "";
        if (selected.size === 0) {
            const empty = document.createElement("span");
            empty.style.color = "#999";
            empty.style.fontSize = "13px";
            empty.textContent = "검색 후 도서를 추가해주세요.";
            box.appendChild(empty);
            syncHiddenTargetBookIds();
            return;
        }

        const ul = document.createElement("ul");
        ul.style.listStyle = "none";
        ul.style.padding = "0";
        ul.style.margin = "0";
        ul.style.display = "flex";
        ul.style.flexDirection = "column";
        ul.style.gap = "8px";

        for (const [id, b] of selected.entries()) {
            const li = document.createElement("li");
            li.style.display = "flex";
            li.style.alignItems = "center";
            li.style.justifyContent = "space-between";
            li.style.gap = "10px";
            li.style.padding = "10px 12px";
            li.style.border = "1px solid #e5e7eb";
            li.style.borderRadius = "10px";
            li.style.background = "#fff";

            const left = document.createElement("div");
            left.style.display = "flex";
            left.style.flexDirection = "column";
            left.style.gap = "2px";

            const title = document.createElement("div");
            title.style.fontWeight = "800";
            title.style.color = "#111827";
            title.textContent = b.title || `(ID: ${id})`;

            const meta = document.createElement("div");
            meta.style.fontSize = "12px";
            meta.style.color = "#6b7280";
            const parts = [];
            if (b.author) parts.push(b.author);
            if (b.isbn) parts.push(`ISBN: ${b.isbn}`);
            parts.push(`ID: ${id}`);
            meta.textContent = parts.join(" · ");

            left.appendChild(title);
            left.appendChild(meta);

            const btn = document.createElement("button");
            btn.type = "button";
            btn.textContent = "삭제";
            btn.style.border = "none";
            btn.style.background = "#ef4444";
            btn.style.color = "#fff";
            btn.style.padding = "8px 10px";
            btn.style.borderRadius = "10px";
            btn.style.cursor = "pointer";
            btn.style.fontWeight = "700";
            btn.onclick = () => {
                selected.delete(id);
                renderSelectedList();
                showToast("선택 도서에서 제거했습니다.", "info");
            };

            li.appendChild(left);
            li.appendChild(btn);
            ul.appendChild(li);
        }

        box.appendChild(ul);
        syncHiddenTargetBookIds();
    }

    // ====== Search ======
    async function fetchJsonWithFallback(urls) {
        let lastErr = null;
        for (const url of urls) {
            try {
                const res = await fetch(url, { headers: { "Accept": "application/json" } });
                if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`);
                return await res.json();
            } catch (e) {
                lastErr = e;
            }
        }
        throw lastErr || new Error("검색 API 호출 실패");
    }

    function renderSearchResults(list) {
        const area = $("searchResultArea");
        if (!area) return;

        area.style.display = "block";
        area.innerHTML = "";

        if (!Array.isArray(list) || list.length === 0) {
            const empty = document.createElement("div");
            empty.style.padding = "10px";
            empty.style.color = "#6b7280";
            empty.style.fontSize = "13px";
            empty.textContent = "검색 결과가 없습니다.";
            area.appendChild(empty);
            return;
        }

        const wrap = document.createElement("div");
        wrap.style.display = "flex";
        wrap.style.flexDirection = "column";
        wrap.style.gap = "8px";

        list.forEach((b) => {
            const id = b.id ?? b.bookId ?? b.book_id;
            const title = b.title ?? b.bookTitle ?? "";
            const author = b.author ?? b.authors ?? "";
            const isbn = b.isbn ?? b.isbn13 ?? "";

            const row = document.createElement("div");
            row.style.display = "flex";
            row.style.justifyContent = "space-between";
            row.style.alignItems = "center";
            row.style.gap = "12px";
            row.style.padding = "10px 12px";
            row.style.border = "1px solid #e5e7eb";
            row.style.borderRadius = "10px";
            row.style.background = "#fff";

            const left = document.createElement("div");
            left.style.display = "flex";
            left.style.flexDirection = "column";
            left.style.gap = "2px";

            const t = document.createElement("div");
            t.style.fontWeight = "800";
            t.style.color = "#111827";
            t.textContent = title || `(ID: ${id})`;

            const meta = document.createElement("div");
            meta.style.fontSize = "12px";
            meta.style.color = "#6b7280";
            const parts = [];
            if (author) parts.push(author);
            if (isbn) parts.push(`ISBN: ${isbn}`);
            if (id !== undefined && id !== null) parts.push(`ID: ${id}`);
            meta.textContent = parts.join(" · ");

            left.appendChild(t);
            left.appendChild(meta);

            const btn = document.createElement("button");
            btn.type = "button";
            btn.textContent = selected.has(String(id)) ? "추가됨" : "추가";
            btn.disabled = selected.has(String(id));
            btn.style.border = "none";
            btn.style.padding = "8px 10px";
            btn.style.borderRadius = "10px";
            btn.style.cursor = btn.disabled ? "not-allowed" : "pointer";
            btn.style.fontWeight = "800";
            btn.style.background = btn.disabled ? "#9ca3af" : "var(--color-primary, #2563eb)";
            btn.style.color = "#fff";

            btn.onclick = () => {
                if (id === undefined || id === null || String(id).trim() === "") {
                    showToast("도서 ID를 확인할 수 없어 추가할 수 없습니다.", "error");
                    return;
                }
                const key = String(id);
                if (selected.has(key)) return;

                selected.set(key, { id: key, title, author, isbn });
                renderSelectedList();
                btn.textContent = "추가됨";
                btn.disabled = true;
                btn.style.background = "#9ca3af";
                showToast("도서를 선택 목록에 추가했습니다.", "success");
            };

            row.appendChild(left);
            row.appendChild(btn);
            wrap.appendChild(row);
        });

        area.appendChild(wrap);
    }

    // ====== Exposed (inline handlers) ======
    async function searchBooks() {
        const keywordEl = $("bookKeyword");
        const keyword = keywordEl ? keywordEl.value.trim() : "";

        if (!keyword) {
            showToast("검색어를 입력해주세요.", "warn");
            return;
        }

        try {
            // 기존 백엔드가 무엇인지 확정 못하므로 2개 후보를 순차 시도
            const encoded = encodeURIComponent(keyword);
            const candidates = [
                `/admin/books/search?keyword=${encoded}`,
                `/api/books/search?keyword=${encoded}`
            ];

            const data = await fetchJsonWithFallback(candidates);

            // 응답이 {items:[...]} 형태일 수도 있으니 방어
            const list = Array.isArray(data) ? data : (data.items || data.content || []);
            renderSearchResults(list);
        } catch (e) {
            console.error(e);
            showToast("도서 검색 중 오류가 발생했습니다. (API 경로/응답 확인 필요)", "error");
        }
    }

    function prepareSubmit() {
        // 특정 도서 쿠폰 정책 생성 폼 onsubmit에서 호출됨 :contentReference[oaicite:1]{index=1}
        if (selected.size === 0) {
            showToast("적용할 도서를 1개 이상 선택해주세요.", "warn");
            return false;
        }

        // hidden 값 최종 동기화
        syncHiddenTargetBookIds();
        return true;
    }

    // ====== Init ======
    function init() {
        showEnvMessagesIfAny();

        // 초기 렌더 (선택 목록 박스 초기 상태)
        renderSelectedList();

        // 검색 결과 영역이 있으면 기본적으로 숨겨져 있을 수 있음
        const area = $("searchResultArea");
        if (area && !area.style.display) area.style.display = "none";
    }

    // DOM 준비 후 실행 (script가 head에 있어도 안전)
    document.addEventListener("DOMContentLoaded", init);

    // 인라인 이벤트에서 접근할 수 있도록 전역 노출
    window.searchBooks = searchBooks;
    window.prepareSubmit = prepareSubmit;

})();
