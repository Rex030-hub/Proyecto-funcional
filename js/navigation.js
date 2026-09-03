class NavigationController {
  constructor() {
    this.currentPage = 1;
    this.totalPages = 3;
    this.btnPrev = document.getElementById('btn-prev');
    this.btnNext = document.getElementById('btn-next');
    this.indicator = document.getElementById('page-indicator');
  }

  init() {
    if (this.btnPrev) this.btnPrev.addEventListener('click', () => this.prevPage());
    if (this.btnNext) this.btnNext.addEventListener('click', () => this.nextPage());
    this.updateUI();
  }

  goToPage(pageNumber) {
    if (pageNumber < 1 || pageNumber > this.totalPages) return;

    const currentEl = document.getElementById(`page-${this.currentPage}`);
    if (currentEl) currentEl.classList.remove('active');

    this.currentPage = pageNumber;

    const nextEl = document.getElementById(`page-${this.currentPage}`);
    if (nextEl) nextEl.classList.add('active');

    this.updateUI();
  }

  nextPage() {
    if (this.currentPage < this.totalPages) this.goToPage(this.currentPage + 1);
  }

  prevPage() {
    if (this.currentPage > 1) this.goToPage(this.currentPage - 1);
  }

  updateUI() {
    if (this.indicator) this.indicator.textContent = `Página ${this.currentPage} de ${this.totalPages}`;
    if (this.btnPrev) this.btnPrev.disabled = this.currentPage === 1;
    if (this.btnNext) this.btnNext.disabled = this.currentPage === this.totalPages;
  }
}