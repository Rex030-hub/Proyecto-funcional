class BookNavigation {
  constructor() {
    this.currentPage = 0;
    this.totalPages = 3;
    this.isAnimating = false;

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

  getTransitionDuration(element) {
    if (!element) return 0;

    const styles = window.getComputedStyle(element);
    const durations = styles.transitionDuration
      .split(',')
      .map((value) => value.trim())
      .map((value) => {
        if (value.endsWith('ms')) return Number.parseFloat(value);
        if (value.endsWith('s')) return Number.parseFloat(value) * 1000;
        return 0;
      });

    return Math.max(...durations, 0);
  }

  waitForTransformTransition(element) {
    if (!element) return Promise.resolve();

    const duration = this.getTransitionDuration(element);
    if (duration <= 0) return Promise.resolve();

    return new Promise((resolve) => {
      const finish = () => {
        element.removeEventListener('transitionend', onTransitionEnd);
        element.removeEventListener('transitioncancel', finish);
        resolve();
      };

      const onTransitionEnd = (event) => {
        if (event.target !== element || event.propertyName !== 'transform') return;
        finish();
      };

      element.addEventListener('transitionend', onTransitionEnd);
      element.addEventListener('transitioncancel', finish);
    });
  }

  async changePage(nextPage) {
    if (this.isAnimating || nextPage < 0 || nextPage >= this.totalPages || nextPage === this.currentPage) {
      return;
    }

    const previousPage = this.currentPage;
    this.currentPage = nextPage;
    this.isAnimating = true;
    this.applyFlip();

    const transitionPaper = nextPage > previousPage
      ? (nextPage === 1 ? this.paper1 : this.paper2)
      : (previousPage === 1 ? this.paper1 : this.paper2);

    await this.waitForTransformTransition(transitionPaper);
    this.isAnimating = false;
    this.updateUI();
  }

  nextPage() {
    return this.changePage(this.currentPage + 1);
  }

  prevPage() {
    return this.changePage(this.currentPage - 1);
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

    const controlsLocked = this.isAnimating;
    if (this.btnPrev) this.btnPrev.disabled = controlsLocked || this.currentPage === 0;
    if (this.btnNext) this.btnNext.disabled = controlsLocked || this.currentPage === this.totalPages - 1;
  }

  async resetToFirstPage() {
    if (this.currentPage === 0 && !this.isAnimating) return;

    const previousPage = this.currentPage;
    this.currentPage = 0;
    this.isAnimating = true;
    this.applyFlip();

    // Al cerrar desde la sección 3, la hoja 2 (PÁGINA II / III) es la
    // primera referencia visual que debe terminar de cerrarse. Desde la
    // sección 2, la hoja 1 es la que realiza la transición.
    const transitionPaper = previousPage >= 2 ? this.paper2 : this.paper1;
    await this.waitForTransformTransition(transitionPaper);

    this.isAnimating = false;
    this.updateUI();
  }
}

document.addEventListener('DOMContentLoaded', () => {
  window.navigationInstance = new BookNavigation();
});
