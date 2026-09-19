/*
 * Controlador central de modales y vistas auxiliares.
 * Mantiene una única lógica para apertura/cierre, foco y teclado.
 */
const ISO_POLICIES_DATA = {
  security: {
    title: "Política de seguridad de la información",
    content: `
      <h3>Seguridad</h3>
      <p>Acá contendrá todos los aspectos a considerar sobre nuestra página para informar al usuario.</p>`
  },
  quality: {
    title: "Calidad de software y accesibilidad",
    content: `
      <h3>Calidad</h3>
      <p>Se clasificará e informará todo lo relacionado con calidad de software, accesibilidad y transparencia.</p>`
  },
  terms: {
    title: "Términos y servicios",
    content: `
      <h3>Terminos</h3>
      <p>Se dispondrá de información de la gestión de calidad de software, cumplimientos legales, etc.</p>`
  }
};

document.addEventListener('DOMContentLoaded', () => {
  const mainBookView = document.getElementById('main-book-view');
  const isoView = document.getElementById('iso-policy-view');
  const isoTitle = document.getElementById('iso-section-title');
  const isoContent = document.getElementById('iso-section-content');
  const btnBack = document.getElementById('btn-back-to-book');
  const drawerLinks = document.querySelectorAll('.drawer-link');
  const modal = document.getElementById('parchment-modal');
  const modalTitle = document.getElementById('parchment-title');
  const closeButton = document.getElementById('btn-close-parchment');
  let lastFocusedElement = null;

  const getFocusable = () => {
    if (!modal) return [];
    return [...modal.querySelectorAll('button:not([disabled]), a[href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])')]
      .filter((element) => element.offsetParent !== null);
  };

  function closeParchment() {
    if (!modal || modal.classList.contains('hidden')) return;
    modal.classList.add('hidden');
    modal.classList.remove('details-ready');
    modal.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('modal-open');
    const target = lastFocusedElement;
    lastFocusedElement = null;
    if (target && document.contains(target)) {
      requestAnimationFrame(() => target.focus({ preventScroll: true }));
    }
  }

  function openParchment({ title, panelId }) {
    if (!modal) return;
    lastFocusedElement = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    if (modalTitle && title) modalTitle.textContent = title;
    modal.querySelectorAll('[data-parchment-panel]').forEach((panel) => panel.classList.add('hidden'));
    const panel = panelId ? document.getElementById(panelId) : null;
    if (panel) panel.classList.remove('hidden');
    modal.classList.remove('hidden');
    modal.setAttribute('aria-hidden', 'false');
    document.body.classList.add('modal-open');
    requestAnimationFrame(() => {
      const target = closeButton || getFocusable()[0];
      target?.focus({ preventScroll: true });
    });
  }

  window.ParchmentModal = { open: openParchment, close: closeParchment };

  closeButton?.addEventListener('click', closeParchment);
  modal?.addEventListener('click', (event) => {
    if (event.target === modal) closeParchment();
  });

  document.addEventListener('keydown', (event) => {
    if (!modal || modal.classList.contains('hidden')) return;
    if (event.code === 'Escape') {
      event.preventDefault();
      closeParchment();
      return;
    }
    if (event.key !== 'Tab') return;
    const focusable = getFocusable();
    if (!focusable.length) return;
    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  });

  function navigateToIsoSection(sectionKey) {
    if (!ISO_POLICIES_DATA[sectionKey]) return;
    isoTitle.textContent = ISO_POLICIES_DATA[sectionKey].title;
    isoContent.innerHTML = ISO_POLICIES_DATA[sectionKey].content;
    mainBookView?.classList.add('hidden');
    isoView?.classList.remove('hidden');
  }

  function returnToBookView() {
    isoView?.classList.add('hidden');
    mainBookView?.classList.remove('hidden');
    history.replaceState(null, '', window.location.pathname + window.location.search);
  }

  drawerLinks.forEach((link) => {
    link.addEventListener('click', (event) => {
      event.preventDefault();
      const sectionKey = link.getAttribute('data-iso');
      navigateToIsoSection(sectionKey);
      history.replaceState(null, '', `#iso-${sectionKey}`);
    });
  });

  btnBack?.addEventListener('click', returnToBookView);

  const initialHash = window.location.hash.replace('#', '');
  if (initialHash) {
    const matchedKey = Object.keys(ISO_POLICIES_DATA).find((key) => initialHash.includes(key));
    if (matchedKey) navigateToIsoSection(matchedKey);
  }
});
