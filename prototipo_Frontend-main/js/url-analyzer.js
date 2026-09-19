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
  const urlDetailsPanel = document.getElementById('url-details-panel');
  const documentDetailsPanel = document.getElementById('document-details-panel');
  const status = document.getElementById('url-analysis-status');

  const resDominio = document.getElementById('res-dominio');
  const resConfianza = document.getElementById('res-confianza');
  const resTema = document.getElementById('res-tema');
  const resMotivo = document.getElementById('res-motivo');
  const resApaCitation = document.getElementById('res-apa-citation');
  const resRelatedSources = document.getElementById('res-related-sources');

  const relatedSources = [
    {
      title: 'Inteligencia artificial en la educación: oportunidades y desafíos',
      author: 'Centro de Estudios Tecnológicos',
      year: '2026',
      url: 'https://fuentes-ejemplo.edu/ia-educacion',
      topic: 'IA y educación'
    },
    {
      title: 'Transformación digital y nuevas herramientas de aprendizaje',
      author: 'Instituto de Innovación Digital',
      year: '2026',
      url: 'https://biblioteca-digital.example.org/transformacion-aprendizaje',
      topic: 'Tecnología educativa'
    },
    {
      title: 'Uso responsable de inteligencia artificial en contextos académicos',
      author: 'Observatorio de Tecnología y Sociedad',
      year: '2025',
      url: 'https://observatorio-tec.example.com/ia-academia',
      topic: 'Uso responsable de IA'
    },
    {
      title: 'Alfabetización digital y pensamiento crítico en estudiantes',
      author: 'Red de Investigación Educativa',
      year: '2025',
      url: 'https://red-educativa.example.net/alfabetizacion-digital',
      topic: 'Alfabetización digital'
    },
    {
      title: 'Tecnologías emergentes y evolución de los procesos educativos',
      author: 'Fundación para la Innovación Educativa',
      year: '2024',
      url: 'https://fundacion-innovacion.example.org/tecnologias-emergentes',
      topic: 'Innovación educativa'
    }
  ];

  let lastResult = null;

  function escapeHtml(value) {
    return String(value).replace(/[&<>'"]/g, (char) => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
    }[char]));
  }

  function formatApa(author, year, title, url) {
    return `${escapeHtml(author)}. (${escapeHtml(year)}). <em>${escapeHtml(title)}</em>. ${escapeHtml(url)}`;
  }

  function renderRelatedSources() {
    if (!resRelatedSources) return;
    resRelatedSources.innerHTML = relatedSources.map((source, index) => `
      <article class="related-source-card" style="--source-delay: ${index * 70}ms">
        <div class="source-number">${String(index + 1).padStart(2, '0')}</div>
        <div class="source-main">
          <div class="source-topline">
            <span class="source-topic">${escapeHtml(source.topic)}</span>
            <span class="source-year">${escapeHtml(source.year)}</span>
          </div>
          <h5>${escapeHtml(source.title)}</h5>
          <p class="source-author">${escapeHtml(source.author)}</p>
          <a class="source-url" href="${escapeHtml(source.url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(source.url)}</a>
          <p class="source-apa"><strong>APA 7:</strong> ${formatApa(source.author, source.year, source.title, source.url)}</p>
        </div>
      </article>
    `).join('');
  }

  function actualizarPergamino(datos) {
    if (resDominio) resDominio.textContent = datos.dominio || 'No disponible';
    if (resConfianza) resConfianza.textContent = datos.confianza || '0%';
    if (resTema) resTema.textContent = datos.tema || 'Sin detectar';
    if (resMotivo) resMotivo.textContent = datos.motivo || 'Sin detalles adicionales';
    if (resApaCitation) resApaCitation.innerHTML = datos.citaApa || 'La referencia aparecerá al analizar una URL.';
    renderRelatedSources();
  }

  function setStatus(message = '', type = '') {
    if (!status) return;
    status.textContent = message;
    status.className = `analysis-status ${type}`.trim();
  }

  function openParchment() {
    if (!window.ParchmentModal) return;
    window.ParchmentModal.open({ title: 'Informe de Veracidad', panelId: 'url-details-panel' });
  }

  if (urlForm && urlInput) {
    urlForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const urlToAnalyze = urlInput.value.trim();
      if (!urlToAnalyze) return;

      const btnSubmit = urlForm.querySelector('button[type="submit"]');
      if (!btnSubmit) return;
      if (!urlInput.checkValidity()) {
        urlInput.reportValidity();
        setStatus('Ingresa una dirección web válida.', 'error');
        return;
      }

      const originalText = btnSubmit.textContent;
      btnSubmit.textContent = 'Analizando...';
      btnSubmit.disabled = true;
      setStatus('Analizando la fuente…', 'loading');

      try {
        // Simulación deliberada para la entrega frontend.
        await new Promise((resolve) => setTimeout(resolve, 900));

        const dominio = new URL(urlToAnalyze).hostname || 'fuente-ejemplo.edu';
        lastResult = {
          dominio,
          confianza: '87%',
          tema: 'Tecnología y educación',
          motivo: 'Resultado simulado para demostrar el flujo de análisis. En la versión con backend, este campo será generado a partir de las características y metadatos de la fuente analizada.',
          citaApa: formatApa(
            'Fuente demostrativa',
            '2026',
            'Artículo analizado sobre tecnología y educación',
            urlToAnalyze
          )
        };

        actualizarPergamino(lastResult);
        setStatus('Análisis completado correctamente.', 'success');
        openParchment();
      } catch (error) {
        console.error('Error en el análisis de URL:', error);
        lastResult = null;
        setStatus('No fue posible completar el análisis. Intenta nuevamente.', 'error');
      } finally {
        btnSubmit.textContent = originalText;
        btnSubmit.disabled = false;
      }
    });
  }

  btnDetails?.addEventListener('click', () => {
    if (!lastResult) {
      setStatus('Primero analiza una URL para consultar sus detalles.', 'error');
      return;
    }
    actualizarPergamino(lastResult);
    openParchment();
  });

});
