/**
 * LANDING PAGE - Carrossel e animações
 */

document.addEventListener('DOMContentLoaded', () => {
    const slides = document.querySelectorAll('.slide');
    const dots = document.querySelectorAll('.carousel-dots button');
    let current = 0;
    let timer;

    function goTo(idx) {
        slides[current].classList.remove('active');
        dots[current].classList.remove('active');
        current = idx;
        slides[current].classList.add('active');
        dots[current].classList.add('active');
    }

    function autoPlay() {
        timer = setInterval(() => {
            goTo((current + 1) % slides.length);
        }, 5000);
    }

    dots.forEach(btn => {
        btn.addEventListener('click', () => {
            clearInterval(timer);
            goTo(parseInt(btn.dataset.idx));
            autoPlay();
        });
    });

    autoPlay();
});