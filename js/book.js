document.addEventListener('DOMContentLoaded', () => {
  const book = document.getElementById('book');
  const bookControls = document.getElementById('book-controls');
  const btnClose = document.getElementById('btn-close');
  let closeTimer = null;
  let isClosing = false;
  const BOOK_ANIMATION_MS = 1200;

  if (!book) return;

  function isTypingTarget(target) {
    return target instanceof HTMLInputElement ||
      target instanceof HTMLTextAreaElement ||
      target instanceof HTMLSelectElement ||
      target?.isContentEditable;
  }

  function openBook() {
    if (!book.classList.contains('closed') || isClosing) return;

    if (closeTimer) {
      clearTimeout(closeTimer);
      closeTimer = null;
    }

    book.classList.remove('closed');
    book.classList.add('open');
    book.setAttribute('aria-label', 'Libro de análisis bibliográfico abierto');
    document.body.classList.add('is-book-open');
    bookControls?.classList.remove('hidden');
  }

  function closeBook() {
    if (!book.classList.contains('open') || isClosing) return;

    isClosing = true;

    // PRIMERA FASE: las hojas regresan a su posición inicial.
    // La portada permanece abierta durante toda esta animación.
    // Esto hace que, al cerrar desde PÁGINA III, la hoja PÁGINA II / III
    // sea la primera en cerrarse y no desaparezca de golpe.
    window.navigationInstance?.resetToFirstPage();
    bookControls?.classList.add('hidden');

    closeTimer = window.setTimeout(() => {
      // SEGUNDA FASE: cuando las hojas ya terminaron de cerrarse,
      // recién ahora se cierra la portada.
      book.classList.remove('open');
      book.classList.add('closed');
      book.setAttribute('aria-label', 'Abrir libro de análisis bibliográfico');
      document.body.classList.remove('is-book-open');

      closeTimer = null;
      isClosing = false;
    }, BOOK_ANIMATION_MS);
  }

  book.addEventListener('click', () => {
    if (book.classList.contains('closed')) openBook();
  });

  btnClose?.addEventListener('click', (event) => {
    event.stopPropagation();
    closeBook();
  });

  book.addEventListener('keydown', (event) => {
    if ((event.code === 'Enter' || event.code === 'Space') && book.classList.contains('closed')) {
      event.preventDefault();
      openBook();
    }
  });

  document.addEventListener('keydown', (event) => {
    const isClosed = book.classList.contains('closed');

    if (isTypingTarget(event.target)) return;

    if ((event.code === 'Space' || event.code === 'Enter') && isClosed) {
      event.preventDefault();
      openBook();
      return;
    }

    if (isClosed || isClosing) return;

    if (event.code === 'ArrowRight' || event.code === 'PageDown') {
      event.preventDefault();
      window.navigationInstance?.nextPage();
    } else if (event.code === 'ArrowLeft' || event.code === 'PageUp') {
      event.preventDefault();
      window.navigationInstance?.prevPage();
    } else if (event.code === 'Escape') {
      event.preventDefault();
      closeBook();
    }
  });
});
