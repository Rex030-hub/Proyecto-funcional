class NavigationController {
  constructor() {
    this.currentLocation = 1;
    this.totalLocations = 2;
    this.btnPrev = document.getElementById('btn-prev');
    this.btnNext = document.getElementById('btn-next');
    this.indicator = document.getElementById('page-indicator');
    this.isAnimating = false;
  }

  init() {
    if (this.btnPrev) this.btnPrev.addEventListener('click', () => this.prevPage());
    if (this.btnNext) this.btnNext.addEventListener('click', () => this.nextPage());
    this.updateUI();
  }

  nextPage() {
    if (this.currentLocation >= this.totalLocations || this.isAnimating) return;

    this.isAnimating = true;
    const paper1 = document.getElementById('paper-1');
    const coverBack = document.querySelector('.cover-back-face');

    if (paper1) paper1.classList.add('flipped');

    this.currentLocation++;
    this.updateUI();

    setTimeout(() => {
      if (coverBack) {
        coverBack.style.visibility = 'hidden';
      }
      this.isAnimating = false;
    }, 900);
  }

  prevPage() {
    if (this.currentLocation <= 1 || this.isAnimating) return;

    this.isAnimating = true;
    const paper1 = document.getElementById('paper-1');
    const coverBack = document.querySelector('.cover-back-face');

    if (coverBack) {
      coverBack.style.visibility = 'visible';
    }

    if (paper1) paper1.classList.remove('flipped');

    this.currentLocation--;
    this.updateUI();

    setTimeout(() => {
      this.isAnimating = false;
    }, 900);
  }

  // Método para reiniciar el estado de la página al cerrar el libro
  resetToFirstPage() {
    this.currentLocation = 1;
    this.isAnimating = false;
    this.updateUI();
  }

  updateUI() {
    if (this.indicator) {
      this.indicator.textContent = `Sección ${this.currentLocation} de ${this.totalLocations}`;
    }
    if (this.btnPrev) {
      this.btnPrev.disabled = this.currentLocation === 1;
    }
    if (this.btnNext) {
      this.btnNext.disabled = this.currentLocation === this.totalLocations;
    }
  }
}

// Inicializar y asignar la instancia de forma global
document.addEventListener('DOMContentLoaded', () => {
  window.navigationInstance = new NavigationController();
  window.navigationInstance.init();
});