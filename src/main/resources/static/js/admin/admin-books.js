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
                    <img src="${book.image || '/img/no-image.png'}" alt="표지">
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
        const response = await fetch(`/admin/books`);
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
    // document.getElementById('publishedDate').value = book.publishedDate || '';

    // pubDate(AI 검색 결과) 또는 publishedDate(DB 조회 결과)를 가져옴
    let pDate = book.pubDate || book.publishedDate || '';

// 날짜가 T00:00... 처럼 길면 앞 10자리만 자름
    if (pDate.length > 10) {
        pDate = pDate.substring(0, 10);
    }
    document.getElementById('publishedDate').value = pDate;

    document.getElementById('price').value = book.price || 0;
    document.getElementById('categoryId').value = book.categoryId || "";

    // 이미지 처리
    const imageUrl = book.image || book.imageUrl || '';
    document.getElementById('image').value = imageUrl;
    previewImage(imageUrl);

    // [수정 2] 저자 처리 버그 수정 (book.author -> book.authors)
    if (Array.isArray(book.authors)) {
        document.getElementById('author').value = book.authors.join(', ');
    } else {
        document.getElementById('author').value = book.author || '';
    }

    // 설명 (WYSIWYG 에디터 대응)
    const desc = book.description || book.content || '';
    document.getElementById('description').value = desc;
    const descField = document.getElementById('description');
    descField.value = desc;

    // 만약 Toast UI Editor나 Summernote를 쓴다면 여기서 값 주입 필요
    // 예: editor.setHTML(desc);

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
function setFormReadOnly(isReadOnly) {
    // description은 에디터를 쓸 경우 readOnly 속성이 안 먹힐 수 있음 (에디터 API 사용 필요)
    const fields = ['isbn', 'title', 'author', 'publisher', 'publishedDate', 'image', 'price', 'description'];

    fields.forEach(fieldId => {
        const el = document.getElementById(fieldId);
        if (el) {
            el.readOnly = isReadOnly;
            el.style.backgroundColor = isReadOnly ? "#e9ecef" : "#fff";
            // 가격 등은 수정 모드에서도 고칠 수 있게 하려면 예외 처리 필요
            // 여기서는 원본 로직 유지
        }
    });
}