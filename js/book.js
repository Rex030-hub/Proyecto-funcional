class Book3D {
  constructor(navigation) {
    this.bookEl = document.getElementById('book');
    this.coverEl = document.getElementById('cover-front');
    this.controlsEl = document.getElementById('book-controls');
    this.btnClose = document.getElementById('btn-close');
    this.navigation = navigation;
    this.isOpen = false;
  }

  init() {
    if (this.coverEl) this.coverEl.addEventListener('click', () => this.openBook());
    if (this.btnClose) this.btnClose.addEventListener('click', () => this.closeBook());
  }

  openBook() {
    if (this.isOpen) return;
    this.bookEl.classList.remove('closed');
    this.bookEl.classList.add('open');
    this.isOpen = true;

    setTimeout(() => {
      this.controlsEl.classList.remove('hidden');
    }, 600);
  }

  closeBook() {
    if (!this.isOpen) return;
    this.controlsEl.classList.add('hidden');
    this.bookEl.classList.remove('open');
    
    setTimeout(() => {
      this.bookEl.classList.add('closed');
      this.navigation.goToPage(1);
      this.isOpen = false;
    }, 600);
  }
}