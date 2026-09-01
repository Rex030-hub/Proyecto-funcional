document.addEventListener('DOMContentLoaded', () => {
  const book = document.getElementById('book');
  const bookControls = document.getElementById('book-controls');
  const btnClose = document.getElementById('btn-close');

  // Evento para ABRIR el libro al hacer clic sobre la portada
  if (book) {
    book.addEventListener('click', () => {
      if (book.classList.contains('closed')) {
        book.classList.remove('closed');
        book.classList.add('open');

        if (bookControls) {
          bookControls.classList.remove('hidden');
        }
      }
    });
  }

  // Evento para CERRAR el libro de forma secuencial
  if (btnClose) {
    btnClose.addEventListener('click', (e) => {
      e.stopPropagation();
      closeBook();
    });
  }
});

function closeBook() {
  const book = document.getElementById('book');
  const bookControls = document.getElementById('book-controls');
  const coverBack = document.querySelector('.cover-back-face');
  const paper1 = document.getElementById('paper-1');

  // 1. Ocultar los controles de interfaz inmediatamente
  if (bookControls) {
    bookControls.classList.add('hidden');
  }

  // 2. Comprobar si la hoja estaba dada vuelta (Páginas III y IV)
  const isPaperFlipped = paper1 && paper1.classList.contains('flipped');

  if (isPaperFlipped) {
    // A) Restaurar la portada interna antes de rotar la hoja
    if (coverBack) {
      coverBack.style.visibility = 'visible';
    }

    // B) Regresar la hoja a su posición original
    paper1.classList.remove('flipped');

    // C) Esperar 750ms a que la hoja termine de caer antes de cerrar la portada
    setTimeout(() => {
      if (book) {
        book.classList.remove('open');
        book.classList.add('closed');
      }
      resetNavigationState();
    }, 750);

  } else {
    // Si ya estaba en Páginas I y II, cerrar la portada directamente
    if (coverBack) {
      coverBack.style.visibility = 'visible';
    }
    if (book) {
      book.classList.remove('open');
      book.classList.add('closed');
    }
    resetNavigationState();
  }
}

// Función auxiliar para reiniciar el contador global
function resetNavigationState() {
  if (window.navigationInstance) {
    window.navigationInstance.resetToFirstPage();
  }
}