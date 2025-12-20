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
        // BookAdminController의 검색 API 호출
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
            // 클릭 시 해당 도서 정보를 폼에 로드
            item.onclick = () => loadBookDetail(book.id);

            item.innerHTML = `
                    <img src="${book.image || '/img/no-image.png'}" alt="표지">
                    <div class="book-info">
                        <div class="book-title">${book.title}</div>
                        <div class="book-meta">${book.author} | ${book.price}원 | ISBN: ${book.isbn}</div>
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
        fillForm(book);

        // 검색 결과 닫기
        document.getElementById('searchResultArea').style.display = 'none';
    } catch (error) {
        alert(error.message);
    }
}

// 3. 폼 채우기
function fillForm(book) {
    document.getElementById('formTitle').innerText = "도서 정보 수정";
    document.getElementById('submitBtn').value = "수정 내용 저장";
    document.getElementById('deleteBtn').style.display = 'block';

    // 폼 Action 변경 (Update API 경로)
    const form = document.getElementById('bookForm');
    form.action = `/admin/books/${book.id}/update`;

    document.getElementById('bookId').value = book.id;
    document.getElementById('isbn').value = book.isbn || book.isbn13;
    document.getElementById('title').value = book.title;
    if (Array.isArray(book.authors)) {
        document.getElementById('author').value = book.author.join(',');
    } else {
        document.getElementById('author').value = book.author || ''; // author 필드명 확인 필요
    }
    document.getElementById('publisher').value = book.publisher;
    document.getElementById('publishedDate').value = book.publishedDate;
    document.getElementById('price').value = book.price;
    document.getElementById('image').value = book.image;
    document.getElementById('description').value = book.description || book.content; // 필드명 확인 필요

    previewImage(book.image);

    setFormReadOnly(true);

    // 화면 스크롤을 폼으로 이동
    form.scrollIntoView({ behavior: 'smooth' });
}

// 4. 폼 초기화 (신규 등록 모드)
function resetForm() {
    document.getElementById('formTitle').innerText = "신규 도서 등록";
    document.getElementById('submitBtn').value = "도서 등록";
    document.getElementById('deleteBtn').style.display = 'none';

    const form = document.getElementById('bookForm');
    form.action = "/admin/books";
    form.reset();

    document.getElementById('bookId').value = '';
    document.getElementById('imgPreview').style.display = 'none';

    setFormReadOnly(false);
}

// 5. 이미지 미리보기
function previewImage(url) {
    const img = document.getElementById('imgPreview');
    if (url) {
        img.src = url;
        img.style.display = 'block';
    } else {
        img.style.display = 'none';
    }
}

// 6. 필드 잠금/해제 함수
function setFormReadOnly(isReadOnly) {
    const fields = ['isbn', 'title', 'author', 'publisher', 'publishedDate', 'image', 'description'];

    fields.forEach(fieldId => {
        const el = document.getElementById(fieldId);
        if (el) {
            el.readOnly = isReadOnly;
            el.style.backgroundColor = isReadOnly ? "#e9ecef" : "#fff";
            el.style.cursor = isReadOnly ? "not-allowed" : "text";
        }
    });
}
