

/* =========================================
   1. 메인 배너 슬라이드 로직
   ========================================= */
const slides = document.getElementById("slides");

// slides가 있을 때만 실행 (중복 제거됨)
if (slides) {
    const dots = Array.from(document.querySelectorAll(".dot"));
    let idx = 0;

    function go(n) {
        if (!dots.length) return;
        idx = (n + dots.length) % dots.length;
        slides.style.transform = `translateX(-${idx * 100}%)`;
        dots.forEach((d, i) => d.classList.toggle("active", i === idx));
    }

    dots.forEach((d) =>
        d.addEventListener("click", (e) => go(+e.target.dataset.idx))
    );

    setInterval(() => go(idx + 1), 5000);
}

/* =========================================
   2. 탭 메뉴 로직
   ========================================= */
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

if (tabBtns.length > 0) {
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
}

/* =========================================
   3. 카테고리 메뉴 (헤더) 로직
   ========================================= */
document.addEventListener("DOMContentLoaded", () => {
    const menuWrapper = document.querySelector('.all-menu-wrapper');
    const megaMenu = document.querySelector('.mega-menu');
    const menuBtn = document.querySelector('.all-menu-btn');
    let closeTimer = null;

    if (!menuWrapper || !megaMenu || !menuBtn) return;

    function openMenu() {
        if (closeTimer) {
            clearTimeout(closeTimer);
            closeTimer = null;
        }
        megaMenu.classList.add('active');
    }

    function scheduleCloseMenu() {
        if (closeTimer) return;
        closeTimer = setTimeout(() => {
            megaMenu.classList.remove('active');
            closeTimer = null;
        }, 100);
    }

    menuBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        if (megaMenu.classList.contains('active')) {
            scheduleCloseMenu();
        } else {
            openMenu();
        }
    });

    menuWrapper.addEventListener('mouseenter', openMenu);
    menuWrapper.addEventListener('mouseleave', scheduleCloseMenu);

    const menuLinks = megaMenu.querySelectorAll('a');
    menuLinks.forEach(link => {
        link.addEventListener('click', () => {
            megaMenu.classList.remove('active');
        });
    });
});