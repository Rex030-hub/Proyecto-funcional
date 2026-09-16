class BookNavigation {
  constructor() {
    this.currentPage = 0;
    this.totalPages = 3;

    this.btnPrev = document.getElementById('btn-prev');
    this.btnNext = document.getElementById('btn-next');
    this.pageIndicator = document.getElementById('page-indicator');
    this.paper1 = document.getElementById('paper-1');
    this.paper2 = document.getElementById('paper-2');

    this.init();
  }

  init() {
    this.btnNext?.addEventListener('click', (event) => {
      event.stopPropagation();
      this.nextPage();
    });

    this.btnPrev?.addEventListener('click', (event) => {
      event.stopPropagation();
      this.prevPage();
    });

    this.applyFlip();
  }

  nextPage() {
    if (this.currentPage >= this.totalPages - 1) return;
    this.currentPage += 1;
    this.applyFlip();
  }

  prevPage() {
    if (this.currentPage <= 0) return;
    this.currentPage -= 1;
    this.applyFlip();
  }

  applyFlip() {
    this.paper1?.classList.toggle('flipped', this.currentPage >= 1);
    this.paper2?.classList.toggle('flipped', this.currentPage >= 2);
    this.updateUI();
  }

  updateUI() {
    if (this.pageIndicator) {
      this.pageIndicator.textContent = `Sección ${this.currentPage + 1} de ${this.totalPages}`;
    }
    if (this.btnPrev) this.btnPrev.disabled = this.currentPage === 0;
    if (this.btnNext) this.btnNext.disabled = this.currentPage === this.totalPages - 1;
  }

  resetToFirstPage() {
    this.currentPage = 0;
    this.applyFlip();
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.navigationInstance = new BookNavigation();
});
