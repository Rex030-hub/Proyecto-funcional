document.addEventListener('DOMContentLoaded', () => {
  const book = document.getElementById('book');
  const bookControls = document.getElementById('book-controls');
  const btnClose = document.getElementById('btn-close');
  const frontCover = document.getElementById('cover-front');

  const BOOK_STATES = Object.freeze({
    CLOSED: 'closed',
    OPENING: 'opening',
    OPEN: 'open',
    CLOSING: 'closing'
  });

  let state = BOOK_STATES.CLOSED;

  if (!book) return;

  function isTypingTarget(target) {
    return target instanceof HTMLInputElement ||
      target instanceof HTMLTextAreaElement ||
      target instanceof HTMLSelectElement ||
      target?.isContentEditable;
  }

  function getTransitionDuration(element) {
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

  function waitForTransformTransition(element) {
    if (!element) return Promise.resolve();

    const duration = getTransitionDuration(element);
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

  async function openBook() {
    if (state !== BOOK_STATES.CLOSED) return;

    state = BOOK_STATES.OPENING;
    book.classList.remove('closed');
    book.classList.add('open');
    book.setAttribute('aria-label', 'Libro de análisis bibliográfico abierto');
    document.body.classList.add('is-book-open');
    bookControls?.classList.remove('hidden');

    await waitForTransformTransition(frontCover);

    if (state === BOOK_STATES.OPENING) {
      state = BOOK_STATES.OPEN;
    }
  }

  async function closeBook() {
    if (state !== BOOK_STATES.OPEN) return;

    state = BOOK_STATES.CLOSING;

    // PRIMERA FASE: las hojas vuelven a su posición inicial mientras la
    // portada permanece abierta. No se usa un tiempo fijo: navegación
    // informa cuando termina la transición real de la hoja.
    bookControls?.classList.add('hidden');
    await window.navigationInstance?.resetToFirstPage();

    // SEGUNDA FASE: una vez cerradas las hojas, se cierra la portada.
    book.classList.remove('open');
    book.classList.add('closed');
    book.setAttribute('aria-label', 'Abrir libro de análisis bibliográfico');
    document.body.classList.remove('is-book-open');

    await waitForTransformTransition(frontCover);

    state = BOOK_STATES.CLOSED;
  }

  book.addEventListener('click', () => {
    if (state === BOOK_STATES.CLOSED) openBook();
  });

  btnClose?.addEventListener('click', (event) => {
    event.stopPropagation();
    closeBook();
  });

  book.addEventListener('keydown', (event) => {
    if ((event.code === 'Enter' || event.code === 'Space') && state === BOOK_STATES.CLOSED) {
      event.preventDefault();
      openBook();
    }
  });

  document.addEventListener('keydown', (event) => {
    if (isTypingTarget(event.target)) return;

    if ((event.code === 'Space' || event.code === 'Enter') && state === BOOK_STATES.CLOSED) {
      event.preventDefault();
      openBook();
      return;
    }

    if (state !== BOOK_STATES.OPEN) return;

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
