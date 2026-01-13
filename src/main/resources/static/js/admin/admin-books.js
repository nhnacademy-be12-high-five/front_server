let editor;

document.addEventListener("DOMContentLoaded", function () {
    loadRootCategories(); // 페이지 로드 시 1차 카테고리 가져오기
    initEditor();         // Tui Editor 초기화

    // [2] 폼 제출 시 에디터 내용을 hidden input에 담기
    const form = document.getElementById('bookForm');
    if (form) {
        form.addEventListener('submit', function () {
            const descriptionInput = document.getElementById('description');
            // 에디터에 작성된 마크다운 텍스트를 가져와서 input 값으로 설정
            descriptionInput.value = editor.getMarkdown();
        });
    }
});

// [3] 에디터 초기화 함수
function initEditor() {
    const Editor = toastui.Editor;
    editor = new Editor({
        el: document.querySelector('#editor'), // html에 만든 <div id="editor">
        height: '500px',
        initialEditType: 'markdown',          // or 'wysiwyg'
        previewStyle: 'vertical',
        initialValue: ''
    });
}

// 1. 도서 검색 (관리자 DB 검색)
async function searchBooks() {
    const keyword = document.getElementById('bookKeyword').value;
    if (!keyword.trim()) {
        alert("검색어를 입력하세요.");
        return;
    }

    const resultArea = document.getElementById('searchResultArea');
    resultArea.style.display = 'block';
    resultArea.innerHTML = '<div style="padding:10px;">검색 중...</div>';

    try {
        const response = await fetch(`/admin/books/search?keyword=${encodeURIComponent(keyword)}`);
        const books = await response.json();

        resultArea.innerHTML = '';

        if (books.length === 0) {
            resultArea.innerHTML = '<div style="padding:10px;">검색 결과가 없습니다.</div>';
            return;
        }

        books.forEach(book => {
            const item = document.createElement('div');
            item.className = 'search-result-item';
            item.onclick = () => loadBookDetail(book.id); // 클릭 시 수정 모드

            // [수정 1] 리스트에서도 저자 목록이 잘 보이도록 수정
            const authorStr = Array.isArray(book.authors) ? book.authors.join(', ') : (book.author || '');

            item.innerHTML = `
                    <img src="${book.imageUrl || '/img/no-image.png'}" alt="표지">
                    <div class="book-info">
                        <div class="book-title">${book.title}</div>
                        <div class="book-meta">${authorStr} | ${book.price}원 | ISBN: ${book.isbn}</div>
                    </div>
                `;
            resultArea.appendChild(item);
        });

    } catch (error) {
        console.error(error);
        resultArea.innerHTML = '<div style="padding:10px; color:red;">검색 중 오류가 발생했습니다.</div>';
    }
}

// 2. 도서 상세 정보 로드 (수정 모드 전환)
async function loadBookDetail(bookId) {
    try {
        const response = await fetch(`/admin/books/${bookId}`);
        if (!response.ok) throw new Error('도서 정보를 불러올 수 없습니다.');

        const book = await response.json();
        fillForm(book, true); // true = 수정 모드 (읽기 전용)

        document.getElementById('searchResultArea').style.display = 'none';
    } catch (error) {
        alert(error.message);
    }
}

// 3. [추가] AI 도서 가져오기 (Google Books + Gemini)
async function fetchBookInfoByAi() {
    // HTML에 id="aiIsbnInput" 인 input 박스와 검색 버튼이 있다고 가정
    const isbnInput = document.getElementById('aiIsbnInput'); // 혹은 prompt 사용 가능

    let isbn = isbnInput ? isbnInput.value.trim().replace(/-/g, "") : "";

    if (!isbn || !isbn.trim()) {
        alert("ISBN을 입력해주세요.");
        return;
    }

    // 로딩 표시 (선택 사항)
    const btn = document.getElementById('aiSearchBtn'); // 버튼 ID 확인 필요
    if(btn) btn.innerText = "AI 검색 중...";

    try {
        // 백엔드 AI 검색 API 호출
        const response = await fetch(`/admin/books/search-api?isbn=${encodeURIComponent(isbn)}`);

        if (!response.ok) {
            throw new Error("도서 정보를 찾을 수 없습니다. (Google Books에 없거나 오류 발생)");
        }

        const bookData = await response.json();

        // 폼 초기화 후 데이터 채우기 (false = 신규 등록 모드라 수정 가능하게)
        resetForm();
        fillForm(bookData, false);

        alert("AI가 도서 정보와 추천 서평을 가져왔습니다! 내용을 확인해주세요.");

    } catch (error) {
        console.error(error);
        alert("실패: " + error.message);
    } finally {
        if(btn) btn.innerText = "ISBN 검색";
    }
}

function safeSetText(elementId, text) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerText = text;
    } else {
        console.warn(`ID가 '${elementId}'인 요소를 찾을 수 없습니다.`); // 디버깅용 로그
    }
}

// 4. 폼 채우기 (공통 로직)
// isReadOnly: 기존 DB 조회 시 true, AI 신규 등록 시 false
function fillForm(book, isReadOnly) {
    // 타이틀 설정
    const modeTitle = isReadOnly ? "도서 정보 수정" : "신규 도서 등록 (AI 자동완성)";
    safeSetText('formTitle', modeTitle);

    // 버튼 설정
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.value = isReadOnly ? "수정 내용 저장" : "도서 등록";

    // 삭제 버튼은 수정 모드일 때만 노출
    const deleteBtn = document.getElementById('deleteBtn');
    if(deleteBtn) deleteBtn.style.display = isReadOnly ? 'block' : 'none';

    // 폼 Action 설정
    const form = document.getElementById('bookForm');
    if (isReadOnly && book.id) {
        form.action = `/admin/books/${book.id}/update`;
        document.getElementById('bookId').value = book.id;
    } else {
        form.action = "/admin/books"; // 신규 등록 경로
        document.getElementById('bookId').value = '';
    }

    // [데이터 매핑]
    document.getElementById('isbn').value = book.isbn || book.isbn13 || '';
    document.getElementById('title').value = book.title || '';
    document.getElementById('publisher').value = book.publisher || '';

    let rawDate = book.publishedDate || book.pubDate || '';
    let finalDate = '';

    if (rawDate) {
        // 1. 문자열로 확실하게 변환
        rawDate = String(rawDate);

        // 2. 'T'가 있으면(ISO형식) 자르고, 없으면 그대로 사용 (2025-01-10 대응)
        if (rawDate.includes('T')) {
            finalDate = rawDate.split('T')[0];
        } else {
            finalDate = rawDate; // "2025-01-10"은 여기로 들어옴
        }
    }

    // 3. ID가 'publishedDate'인 요소에 값 넣기
    const dateEl = document.getElementById('publishedDate');
    if (dateEl) {
        dateEl.value = finalDate;
    } else {
        // 만약 HTML ID가 다르면 여기서 에러를 잡을 수 있음
        console.error("❌ HTML에 id='publishedDate'인 태그가 없습니다. HTML파일의 input id를 확인해주세요!");
    }
    document.getElementById('price').value = book.price || 0;
    if (book.parentId) {
        document.getElementById('parentCategory').value = book.parentId;
        // 비동기로 2차 불러오고, 완료되면 2차 값 세팅
        loadSubCategories(book.parentId, book.categoryId);
    } else {
        // 부모 ID가 없으면 그냥 최종 ID만 히든 필드에 넣고 (화면엔 표시 안됨)
        document.getElementById('categoryId').value = book.categoryId || "";
    }

    // 이미지 처리
    const imageUrl = book.image || book.imageUrl || '';
    document.getElementById('image').value = imageUrl;
    previewImage(imageUrl);

    if (Array.isArray(book.authors)) {
        document.getElementById('author').value = book.authors.join(', ');
    } else {
        document.getElementById('author').value = book.author || '';
    }

    // 설명 (WYSIWYG 에디터 대응)
    const desc = book.description || book.content || '';
    editor.setMarkdown(desc);
    document.getElementById('description').value = desc;

    // 필드 잠금 설정 (수정 모드면 잠금, AI 모드면 해제)
    setFormReadOnly(isReadOnly);

    form.scrollIntoView({ behavior: 'smooth' });
}

// 5. 폼 초기화
function resetForm() {
    safeSetText('formTitle', "신규 도서 등록");
    document.getElementById('submitBtn').value = "도서 등록";

    const deleteBtn = document.getElementById('deleteBtn');
    if(deleteBtn) deleteBtn.style.display = 'none';

    const form = document.getElementById('bookForm');
    form.action = "/admin/books";
    form.reset();

    editor.setMarkdown('');

    document.getElementById('bookId').value = '';
    document.getElementById('imgPreview').style.display = 'none';

    setFormReadOnly(false); // 잠금 해제
}

// 6. 이미지 미리보기
function previewImage(url) {
    const img = document.getElementById('imgPreview');
    if (img) {
        if (url) {
            img.src = url;
            img.style.display = 'block';
        } else {
            img.style.display = 'none';
        }
    }
}

// 7. 필드 잠금/해제
function setFormReadOnly(isUpdateMode) {
    // description은 에디터를 쓸 경우 readOnly 속성이 안 먹힐 수 있음 (에디터 API 사용 필요)
    const fields = ['isbn', 'title', 'author', 'publisher', 'publishedDate', 'image', 'price', 'description'];

    fields.forEach(fieldId => {
        const el = document.getElementById(fieldId);
        if (!el) return;

        if (isUpdateMode) {
            // === 수정 모드 (기존 도서 불러옴) ===
            if (fieldId === 'isbn') {
                // 1. ISBN은 절대 수정 불가
                el.readOnly = true;
                el.style.backgroundColor = "#e9ecef"; // 회색 배경 (잠김 표시)
            } else {
                // 2. 나머지 필드는 수정 가능하도록 활성화
                el.readOnly = false;
                el.style.backgroundColor = "#fff";    // 흰색 배경
            }
        } else {
            // === 신규 등록 모드 ===
            // 모든 필드 입력 가능
            el.readOnly = false;
            el.style.backgroundColor = "#fff";
        }
    });
}

// 1차 카테고리 로드
function loadRootCategories() {
    fetch('/categories/root')
        .then(res => res.json())
        .then(data => {
            const parentSelect = document.getElementById('parentCategory');
            if(!parentSelect) return; // 요소가 없으면 중단

            // 기존 옵션 유지 (1차 카테고리 선택) 외에 추가
            parentSelect.innerHTML = '<option value="">1차 카테고리 선택</option>';
            data.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.categoryId;
                option.text = cat.categoryName;
                parentSelect.appendChild(option);
            });
        })
        .catch(err => console.error('카테고리 로드 실패:', err));
}

// 2차 카테고리 로드 (1차 선택 시 호출)
// selectedSubId: (선택사항) 로딩 후 자동으로 선택할 2차 카테고리 ID (수정 모드용)
function loadSubCategories(parentId, selectedSubId = null) {
    const subSelect = document.getElementById('subCategory');
    const finalInput = document.getElementById('categoryId');

    if (!parentId) {
        subSelect.innerHTML = '<option value="">2차 카테고리 선택</option>';
        subSelect.disabled = true;
        finalInput.value = "";
        return;
    }

    fetch(`/categories/${parentId}/children`)
        .then(res => res.json())
        .then(data => {
            subSelect.innerHTML = '<option value="">2차 카테고리 선택</option>';

            if (data.length > 0) {
                subSelect.disabled = false;
                data.forEach(cat => {
                    const option = document.createElement('option');
                    option.value = cat.categoryId;
                    option.text = cat.categoryName;
                    subSelect.appendChild(option);
                });

                // [수정 모드 지원] 기존 2차 카테고리 값이 있다면 선택
                if (selectedSubId) {
                    subSelect.value = selectedSubId;
                    setFinalCategory(selectedSubId); // 최종 ID 설정
                } else {
                    // 하위가 있는데 선택 안 했으면 초기화
                    finalInput.value = "";
                }
            } else {
                // 하위 카테고리가 없으면 1차 카테고리가 최종값
                subSelect.disabled = true;
                finalInput.value = parentId;
            }
        });
}

// 최종 카테고리 ID 설정 (히든 필드에 값 주입)
function setFinalCategory(subId) {
    if (subId) {
        document.getElementById('categoryId').value = subId;
    }
}
async function deleteBook() {
    const bookId = document.getElementById('bookId').value;

    if (!bookId) {
        alert("삭제할 도서가 선택되지 않았습니다.");
        return;
    }

    if (!confirm("정말로 이 도서를 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.")) {
        return;
    }

    try {
        const response = await fetch(`/admin/books/${bookId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            alert("도서가 성공적으로 삭제되었습니다.");
            window.location.reload(); // 목록 갱신을 위해 새로고침
        } else {
            const errorMsg = await response.text();
            throw new Error(errorMsg);
        }
    } catch (error) {
        console.error(error);
        alert("삭제 실패: " + error.message);
    }
}
