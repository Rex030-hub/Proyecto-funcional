/*
 * Analizador URL — modo demostración frontend.
 *
 * La URL se recibe y valida en el navegador, pero la respuesta es simulada
 * para la presentación. Cuando exista backend, este módulo puede volver a
 * consumir /api/analizar sin modificar la interfaz.
 */
document.addEventListener('DOMContentLoaded', () => {
  const urlForm = document.getElementById('url-search-form');
  const urlInput = document.getElementById('url-input');
  const btnDetails = document.getElementById('btn-url-details');
  const parchmentModal = document.getElementById('parchment-modal');
  const btnCloseParchment = document.getElementById('btn-close-parchment');
  const parchmentTitle = document.getElementById('parchment-title');
  const urlDetailsPanel = document.getElementById('url-details-panel');
  const documentDetailsPanel = document.getElementById('document-details-panel');

  const resDominio = document.getElementById('res-dominio');
  const resConfianza = document.getElementById('res-confianza');
  const resTema = document.getElementById('res-tema');
  const resMotivo = document.getElementById('res-motivo');

  let lastResult = null;

  function actualizarPergamino(datos) {
    if (resDominio) resDominio.textContent = datos.dominio || 'No disponible';
    if (resConfianza) resConfianza.textContent = datos.confianza || '0%';
    if (resTema) resTema.textContent = datos.tema || 'Sin detectar';
    if (resMotivo) resMotivo.textContent = datos.motivo || 'Sin detalles adicionales';
  }

  function openParchment() {
    if (!parchmentModal) return;
    parchmentTitle.textContent = 'Informe de Veracidad';
    urlDetailsPanel?.classList.remove('hidden');
    documentDetailsPanel?.classList.add('hidden');
    parchmentModal.classList.remove('hidden');
  }

  function closeParchment() {
    parchmentModal?.classList.add('hidden');
  }

  if (urlForm && urlInput) {
    urlForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const urlToAnalyze = urlInput.value.trim();
      if (!urlToAnalyze) return;

      const btnSubmit = urlForm.querySelector('button[type="submit"]');
      if (!btnSubmit) return;
      const originalText = btnSubmit.textContent;
      btnSubmit.textContent = 'Analizando...';
      btnSubmit.disabled = true;

      // Simulación deliberada para la entrega frontend.
      await new Promise((resolve) => setTimeout(resolve, 900));

      let dominio = 'fuente-ejemplo.edu';
      try {
        dominio = new URL(urlToAnalyze).hostname || dominio;
      } catch (_) {
        // El input type=url ya restringe el formato en navegadores modernos.
      }

      lastResult = {
        dominio,
        confianza: '87%',
        tema: 'Tecnología y educación',
        motivo: 'Resultado simulado para demostrar el flujo de análisis. En la versión con backend, este campo será generado a partir de las características y metadatos de la fuente analizada.'
      };

      actualizarPergamino(lastResult);
      openParchment();
      btnSubmit.textContent = originalText;
      btnSubmit.disabled = false;
    });
  }

  btnDetails?.addEventListener('click', () => {
    if (!lastResult) return;
    actualizarPergamino(lastResult);
    openParchment();
  });

  btnCloseParchment?.addEventListener('click', closeParchment);
  parchmentModal?.addEventListener('click', (event) => {
    if (event.target === parchmentModal) closeParchment();
  });
});
