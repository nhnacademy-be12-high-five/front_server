const slides = document.getElementById("slides");

const dots = Array.from(document.querySelectorAll(".dot"));

let idx = 0;

function go(n) {
    idx = (n + dots.length) % dots.length;
    slides.style.transform = `translateX(-${idx * 100}%)`;
    dots.forEach((d, i) => d.classList.toggle("active", i === idx));
}
dots.forEach((d) =>
    d.addEventListener("click", (e) => go(+e.target.dataset.idx))
);
setInterval(() => go(idx + 1), 5000);

// 탭 (현재는 탭 버튼이 없어도 에러 나지 않도록 방어)
const tabBtns = document.querySelectorAll(".tab-btn");

const panels = {
    domestic: document.getElementById("panel-domestic"),
    foreign: document.getElementById("panel-foreign"),
    ebook: document.getElementById("panel-ebook"),
    kids: document.getElementById("panel-kids"),
    it: document.getElementById("panel-it"),
    economy: document.getElementById("panel-economy"),
    human: document.getElementById("panel-human"),
    self: document.getElementById("panel-self"),
};

const subCategories = {
    domestic: [
        { name: "소설", link: "#", thumb: "소설" },
        { name: "시/에세이", link: "#", thumb: "시/에세이" },
        { name: "인문", link: "#", thumb: "인문" },
        { name: "사회/정치", link: "#", thumb: "사회/정치" },
        { name: "경제/경영", link: "#", thumb: "경제/경영" },
        { name: "자기계발", link: "#", thumb: "자기계발" },
        { name: "과학", link: "#", thumb: "과학" },
        { name: "여행", link: "#", thumb: "여행" },
    ],
    foreign: [
        { name: "영미소설", link: "#", thumb: "영미소설" },
        { name: "일본도서", link: "#", thumb: "일본도서" },
    ]
};

tabBtns.forEach((btn) => {
    btn.addEventListener("click", () => {
        tabBtns.forEach((b) => b.setAttribute("aria-selected", "false"));
        btn.setAttribute("aria-selected", "true");
        Object.values(panels).forEach((p) => p && (p.hidden = true));

        const key = btn.dataset.tab;
        const panel = panels[key];

        if (panel) {
            panel.hidden = false;
            if (key !== 'domestic' && subCategories[key] && !panel.dataset.filled) {
                panel.innerHTML = '';
                subCategories[key].forEach(cat => {
                    const a = document.createElement("a");
                    a.className = "cat";
                    a.href = cat.link;
                    a.innerHTML = `<div class="thumb">${cat.thumb}</div><span>${cat.name}</span>`;
                    panel.appendChild(a);
                });
                panel.dataset.filled = "1";
            }
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const menuWrapper = document.querySelector('.all-menu-wrapper');
    const megaMenu = document.querySelector('.mega-menu');
    const menuBtn = document.querySelector('.all-menu-btn');
    let closeTimer = null;

    // 메뉴 열기 함수
    function openMenu() {
        // 기존에 닫으려는 타이머가 돌고 있다면 취소 (메뉴 유지)
        if (closeTimer) {
            clearTimeout(closeTimer);
            closeTimer = null;
        }
        megaMenu.classList.add('active');
    }

    // 메뉴 닫기 예약 함수 (10초 뒤)
    function scheduleCloseMenu() {
        // 이미 타이머가 있다면 중복 설정 방지
        if (closeTimer) return;

        closeTimer = setTimeout(() => {
            megaMenu.classList.remove('active');
            closeTimer = null;
        }, 100); // 10000ms = 10초
    }

    // 1. 버튼 클릭 시 토글 (열려있으면 닫기 예약, 닫혀있으면 열기)
    menuBtn.addEventListener('click', (e) => {
        e.stopPropagation(); // 이벤트 버블링 방지
        if (megaMenu.classList.contains('active')) {
            // 이미 열려있다면 바로 닫지 않고 10초 뒤 닫기 예약 시작 (또는 즉시 닫기를 원하면 바로 remove)
            scheduleCloseMenu();
        } else {
            openMenu();
        }
    });

    // 2. 마우스가 영역(버튼+메뉴)에 들어오면 -> 타이머 취소하고 계속 보여줌
    menuWrapper.addEventListener('mouseenter', openMenu);

    // 3. 마우스가 영역을 벗어나면 -> 10초 카운트다운 시작
    menuWrapper.addEventListener('mouseleave', scheduleCloseMenu);

    // (선택 사항) 메뉴 내부의 링크를 클릭하면 즉시 닫혀야 깔끔함
    const menuLinks = megaMenu.querySelectorAll('a');
    menuLinks.forEach(link => {
        link.addEventListener('click', () => {
            megaMenu.classList.remove('active');
        });
    });
});

document.addEventListener('DOMContentLoaded', function() {
    // 1. 로그인 상태 확인 (body 태그의 data-is-login 속성 값을 가져옴)
    const body = document.querySelector('body');
    const isLogin = body.getAttribute('data-is-login') === 'true';

    // 2. 'require-login' 클래스를 가진 모든 요소 선택
    const loginRequiredLinks = document.querySelectorAll('.require-login');

    // 3. 각 링크에 클릭 이벤트 리스너 등록
    loginRequiredLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // 로그인이 되어있지 않다면
            if (!isLogin) {
                e.preventDefault(); // 원래 이동하려던 링크 동작(href)을 막음

                // 알림창 띄우기
                if (confirm('로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하시겠습니까?')) {
                    location.href = '/member/login'; // 로그인 페이지 URL로 이동
                }
                // 취소를 누르면 아무 일도 일어나지 않음 (현재 페이지 유지)
            }
            else{
                location.href = link.href;
            }
        });
    });
});