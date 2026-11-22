document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('pdf-file-input');
    const uploadArea = document.getElementById('pdf-upload-area');
    const selectedArea = document.getElementById('document-selected');
    const fileName = document.getElementById('pdf-file-name');
    const btnAnalyze = document.getElementById('btn-pdf-analyze');
    const btnRemove = document.getElementById('btn-pdf-remove');
    const resultArea = document.getElementById('document-result');
    const score = document.getElementById('document-ai-score');
    const summary = document.getElementById('document-result-summary');
    const btnDetails = document.getElementById('btn-document-details');
    const pdfStatus = document.getElementById('pdf-analysis-status');

    if (!fileInput || !uploadArea || !selectedArea || !btnAnalyze || !resultArea) return;

    let selectedFile = null;
    let lastResult = null;

    function isPdf(file) {
        return Boolean(file && (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')));
    }

    function validatePdf(file) {
        if (!isPdf(file)) return 'Selecciona un archivo PDF válido.';
        if (file.size > MAX_FILE_SIZE) return 'El PDF supera el límite de 15 MB.';
        return '';
    }

    function setPdfStatus(message = '', type = '') {
        if (!pdfStatus) return;
        pdfStatus.textContent = message;
        pdfStatus.className = `analysis-status ${type}`.trim();
    }

    function resetDocument() {
        selectedFile = null;
        lastResult = null;
        fileInput.value = '';
        fileName.textContent = 'Sin archivo';
        uploadArea.classList.remove('hidden');
        selectedArea.classList.add('hidden');
        resultArea.classList.add('hidden');
        score.textContent = '--%';
        summary.textContent = 'Selecciona y analiza un documento para obtener el resultado.';
        setPdfStatus('');
        document.getElementById('error-banner')?.classList.add('hidden');
    }

    function showSelectedFile(file) {
        const validationError = validatePdf(file);
        if (validationError) {
            setPdfStatus(validationError, 'error');
            resetDocument();
            return;
        }
        selectedFile = file;
        setPdfStatus(`Archivo seleccionado: ${file.name}`, 'success');
        fileName.textContent = file.name;
        fileName.title = file.name;
        uploadArea.classList.add('hidden');
        selectedArea.classList.remove('hidden');
        resultArea.classList.add('hidden');
        lastResult = null;
    }

    async function analyzeDocument() {
        if (!selectedFile) {
            setPdfStatus('Selecciona un PDF antes de analizar.', 'error');
            return;
        }

        const originalText = btnAnalyze.textContent;
        btnAnalyze.textContent = 'Analizando documento...';
        btnAnalyze.disabled = true;
        setPdfStatus('Analizando documento…', 'loading');

        const formData = new FormData();
        formData.append('documento', selectedFile);

        try {
            const apiUrl = `${window.APP_CONFIG.API_BASE_URL}/api/analizar-documento`;
            const response = await fetch(apiUrl, { method: 'POST', body: formData });
            const result = await response.json().catch(() => null);

            const errorBanner = document.getElementById('error-banner');
            errorBanner.classList.add('hidden');  // limpiar banner al iniciar cada análisis

            // Error honesto del backend: se muestra el mensaje REAL en banner rojo
            if (!result || result.exito === false) {
                const mensaje = (result && result.mensaje) || 'El documento no pudo analizarse.';
                errorBanner.textContent = mensaje;
                errorBanner.classList.remove('hidden');
                setPdfStatus(mensaje, 'error');
                resultArea.classList.add('hidden');
                return;
            }

            // Camino feliz: ocultar banner y chip de demo
            errorBanner.classList.add('hidden');
            document.getElementById('demo-chip-pdf')?.classList.add('hidden');
            lastResult = result.datos;
            score.textContent = lastResult.porcentaje;
            summary.textContent = lastResult.interpretacion;
            resultArea.classList.remove('hidden');
            setPdfStatus('Análisis forense completado.', 'success');
        } catch (error) {
            // SOLO backend apagado o inalcanzable: demo explícita con chip visible y sin banner
            document.getElementById('error-banner')?.classList.add('hidden');
            document.getElementById('demo-chip-pdf')?.classList.remove('hidden');
            await new Promise((resolve) => setTimeout(resolve, 1200));
            const datos = typeof datosDemoSimulados !== 'undefined' ? datosDemoSimulados : null;
            score.textContent = lastResult.porcentaje;
            summary.textContent = lastResult.interpretacion;
            resultArea.classList.remove('hidden');
            setPdfStatus('Backend no disponible: mostrando demostración.', 'success');
        } finally {
            btnAnalyze.textContent = originalText;
            btnAnalyze.disabled = false;
        }
        
    }

    function openDocumentDetails() {
        if (!lastResult || !window.ParchmentModal) return;

        document.getElementById('res-document-name').textContent = lastResult.nombre;
        document.getElementById('res-document-score').textContent = lastResult.porcentaje;
        document.getElementById('res-document-interpretation').textContent = lastResult.interpretacion;

        document.getElementById('res-document-stats').innerHTML = lastResult.estadisticas.map((item) => `
            <div class="analysis-stat">
                <span>${item.label}</span>
                <strong>${item.value}</strong>
            </div>
        `).join('');

        document.getElementById('res-document-indicators').innerHTML = lastResult.indicadores.map((item) => 
            `<li>${item}</li>`
        ).join('');

        document.getElementById('res-document-evidence').innerHTML = lastResult.evidencias.map((item, index) => `
            <article class="evidence-card" style="--evidence-delay:${index * 140}ms">
                <div class="evidence-heading">
                    <span class="evidence-number">${String(index + 1).padStart(2, '0')}</span>
                    <div>
                        <strong>${item.label}</strong>
                        <small>${item.type}</small>
                    </div>
                </div>
                <div class="evidence-text">${highlightEvidence(item.text)}</div>
                <p class="evidence-reason">
                    <strong>¿Por qué?</strong> ${item.reason}
                </p>
            </article>
        `).join('');

        window.ParchmentModal.open({ 
            title: 'Análisis Forense Detallado', 
            panelId: 'document-details-panel' 
        });
        requestAnimationFrame(() => 
            document.getElementById('parchment-modal')?.classList.add('details-ready')
        );
    }

    function highlightEvidence(text) {
        const words = text.split(/(\s+)/);
        const pivot = Math.max(3, Math.floor(words.length * 0.32));
        const end = Math.min(words.length, pivot + Math.floor(words.length * 0.34));
        return words.map((part, index) => 
            index >= pivot && index <= end && !/^\s+$/.test(part)
                ? `<mark class="ai-highlight">${part}</mark>` 
                : part
        ).join('');
    }

     const MAX_FILE_SIZE = 25 * 1024 * 1024;  

    fileInput.addEventListener('change', () => {
        const [file] = fileInput.files;
        if (!file) return;

        // Bloqueo de tamaño antes de cualquier otra cosa
        if (file.size > MAX_FILE_SIZE) {
            const errorBanner = document.getElementById('error-banner');
            if (errorBanner) {
                errorBanner.textContent = 'Archivo demasiado grande: el límite es 25 MB.';
                errorBanner.classList.remove('hidden');
            }
            setPdfStatus('El archivo supera el límite de 25 MB.', 'error');
            resetDocument();
            return;
        }

        showSelectedFile(file);
    });

    ['dragenter', 'dragover'].forEach((eventName) => {
        uploadArea.addEventListener(eventName, (event) => {
            event.preventDefault();
            uploadArea.classList.add('drag-over');
        });
    });

    ['dragleave', 'drop'].forEach((eventName) => {
        uploadArea.addEventListener(eventName, (event) => {
            event.preventDefault();
            uploadArea.classList.remove('drag-over');
        });
    });

    uploadArea.addEventListener('drop', (event) => {
        const [file] = event.dataTransfer.files;
        if (file) showSelectedFile(file);
    });

    btnAnalyze.addEventListener('click', analyzeDocument);
    btnDetails?.addEventListener('click', openDocumentDetails);
    btnRemove?.addEventListener('click', resetDocument);
});