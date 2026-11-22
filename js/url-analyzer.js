document.addEventListener('DOMContentLoaded', () => {
    const urlForm = document.getElementById('url-search-form');
    const urlInput = document.getElementById('url-input');
    const btnDetails = document.getElementById('btn-url-details');
    const status = document.getElementById('url-analysis-status');
    const resDominio = document.getElementById('res-dominio');
    const resConfianza = document.getElementById('res-confianza');
    const resTema = document.getElementById('res-tema');
    const resMotivo = document.getElementById('res-motivo');
    const resApaCitation = document.getElementById('res-apa-citation');
    const resRelatedSources = document.getElementById('res-related-sources');

    let lastResult = null;

    function escapeHtml(value) {
        return String(value).replace(/[&<>"']/g, (char) => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
        }[char]));
    }

    function renderRelatedSources(sources = []) {
        if (!resRelatedSources) return;
        
        if (sources.length === 0) {
            resRelatedSources.innerHTML = '<p style="text-align:center;color:#666;">No se encontraron fuentes relacionadas.</p>';
            return;
        }

        resRelatedSources.innerHTML = sources.map((source, index) => {
            const year = source.fechaPublicacion 
                ? source.fechaPublicacion.substring(0, 4) 
                : 's.f.';
            
            return `<article class="related-source-card" style="--source-delay: ${index * 70}ms">
                <div class="source-number">${String(index + 1).padStart(2, '0')}</div>
                <div class="source-main">
                    <div class="source-topline">
                        <span class="source-topic">${escapeHtml(source.topico || 'Sin tópico')}</span>
                        <span class="source-year">${escapeHtml(year)}</span>
                    </div>
                    <h5>${escapeHtml(source.titulo || 'Sin título')}</h5>
                    <p class="source-author">${escapeHtml(source.autor || 'Sin autor')}</p>
                    <a class="source-url" href="${escapeHtml(source.url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(source.url)}</a>
                    <p class="source-apa"><strong>APA 7:</strong> ${escapeHtml(source.formatoApa || '')}</p>
                </div>
            </article>`;
        }).join('');
    }

    function actualizarPergamino(datos) {
        if (resDominio) resDominio.textContent = datos.dominio || 'No disponible';
        if (resConfianza) resConfianza.textContent = datos.confianza || '0%';
        if (resTema) resTema.textContent = datos.tema || 'Sin detectar';
        if (resMotivo) resMotivo.textContent = datos.motivo || 'Sin detalles adicionales';
        if (resApaCitation) resApaCitation.innerHTML = datos.citaApa || 'La referencia aparecerá al analizar una URL.';
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
                const apiUrl = `${window.APP_CONFIG.API_BASE_URL}/api/analizar-url`;
                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ url: urlToAnalyze })
                });

                const result = await response.json().catch(() => null);

                if (!result || result.exito === false) {
                    const mensaje = (result && result.mensaje) || 'La URL no pudo analizarse.';
                    setStatus(mensaje, 'error');
                    return;
                }

                const fuente = result.datos.fuente;
                const relacionadas = result.datos.relacionadas || [];
                const desdeCache = result.datos.desdeCache;
                const duracionSeg = (result.datos.duracionMs / 1000).toFixed(1);

                let motivo = desdeCache 
                    ? `Resultado servido desde caché (análisis previo disponible). Tiempo de respuesta: ${duracionSeg}s.`
                    : `Análisis completado en ${duracionSeg}s. Se encontraron ${relacionadas.length} fuentes relacionadas.`;

                if (fuente.puntajeConfianza >= 80) {
                    motivo += ' La fuente presenta alta confiabilidad.';
                } else if (fuente.puntajeConfianza >= 60) {
                    motivo += ' La fuente presenta confiabilidad moderada.';
                } else if (fuente.puntajeConfianza < 40) {
                    motivo += ' Se recomienda verificar con fuentes adicionales.';
                }

                lastResult = {
                    dominio: fuente.dominio || 'No disponible',
                    confianza: `${fuente.puntajeConfianza}%`,
                    tema: fuente.topico || 'Sin detectar',
                    motivo: motivo,
                    citaApa: fuente.formatoApa || 'La referencia aparecerá al analizar una URL.'
                };

                actualizarPergamino(lastResult);
                renderRelatedSources(relacionadas);
                setStatus('Análisis completado correctamente.', 'success');
                openParchment();
            } catch (error) {
                console.error('Error en el análisis de URL:', error);
                lastResult = null;
                setStatus('No fue posible completar el análisis. Verifica que el backend esté activo.', 'error');
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