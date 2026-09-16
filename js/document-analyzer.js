/*
 * Analizador PDF — modo demostración frontend.
 *
 * El navegador recibe realmente el archivo PDF y valida su extensión/MIME.
 * La respuesta del análisis se simula localmente para la presentación.
 * El diseño de lastResult imita la estructura que posteriormente entregará
 * el backend en /api/analizar-documento.
 */
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

  const parchmentModal = document.getElementById('parchment-modal');
  const parchmentTitle = document.getElementById('parchment-title');
  const urlDetailsPanel = document.getElementById('url-details-panel');
  const documentDetailsPanel = document.getElementById('document-details-panel');
  const btnCloseParchment = document.getElementById('btn-close-parchment');

  if (!fileInput || !uploadArea || !selectedArea || !btnAnalyze || !resultArea) return;

  let selectedFile = null;
  let lastResult = null;

  const simulatedEvidence = [
    {
      type: 'Alta uniformidad sintáctica',
      label: 'Estructura repetitiva',
      text: 'En este contexto, resulta fundamental destacar que la implementación de estas herramientas permite mejorar significativamente la eficiencia del proceso.',
      reason: 'La oración concentra una estructura sintáctica muy regular y una formulación altamente genérica.'
    },
    {
      type: 'Patrón de formulación genérica',
      label: 'Expresión estandarizada',
      text: 'Es importante señalar que los resultados obtenidos demuestran claramente la relevancia de continuar desarrollando soluciones innovadoras para abordar esta problemática.',
      reason: 'Se detecta una construcción argumentativa frecuente: introducción genérica + afirmación amplia + recomendación sin evidencia específica.'
    },
    {
      type: 'Baja variación léxica',
      label: 'Repetición conceptual',
      text: 'La tecnología permite mejorar el aprendizaje. Estas tecnologías también permiten mejorar la enseñanza y permiten optimizar los procesos educativos.',
      reason: 'Se observa repetición de términos y estructuras cercanas sin una variación proporcional del vocabulario.'
    }
  ];

  function isPdf(file) {
    return file && (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf'));
  }

  function openDocumentDetails() {
    if (!parchmentModal || !lastResult) return;

    parchmentTitle.textContent = 'Análisis Detallado del Documento';
    urlDetailsPanel?.classList.add('hidden');
    documentDetailsPanel?.classList.remove('hidden');

    document.getElementById('res-document-name').textContent = lastResult.nombre;
    document.getElementById('res-document-score').textContent = lastResult.porcentaje;
    document.getElementById('res-document-interpretation').textContent = lastResult.interpretacion;
    document.getElementById('res-document-stats').innerHTML = lastResult.estadisticas.map((item) => `
      <div class="analysis-stat">
        <span>${item.label}</span><strong>${item.value}</strong>
      </div>`).join('');

    const indicators = document.getElementById('res-document-indicators');
    indicators.innerHTML = lastResult.indicadores.map((item) => `<li>${item}</li>`).join('');

    const evidence = document.getElementById('res-document-evidence');
    evidence.innerHTML = lastResult.evidencias.map((item, index) => `
      <article class="evidence-card" style="--evidence-delay:${index * 140}ms">
        <div class="evidence-heading">
          <span class="evidence-number">${String(index + 1).padStart(2, '0')}</span>
          <div><strong>${item.label}</strong><small>${item.type}</small></div>
        </div>
        <div class="evidence-text">${highlightEvidence(item.text)}</div>
        <p class="evidence-reason"><strong>¿Por qué?</strong> ${item.reason}</p>
      </article>`).join('');

    parchmentModal.classList.remove('hidden');
    requestAnimationFrame(() => parchmentModal.classList.add('details-ready'));
  }

  function highlightEvidence(text) {
    // Marcado controlado de segmentos para la demostración visual.
    const words = text.split(/(\s+)/);
    const pivot = Math.max(3, Math.floor(words.length * 0.32));
    const end = Math.min(words.length, pivot + Math.floor(words.length * 0.34));
    return words.map((part, index) => index >= pivot && index <= end && !/^\s+$/.test(part)
      ? `<mark class="ai-highlight">${part}</mark>` : part).join('');
  }

  function closeParchment() {
    parchmentModal?.classList.add('hidden');
    parchmentModal?.classList.remove('details-ready');
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
  }

  function showSelectedFile(file) {
    if (!isPdf(file)) {
      window.alert('Selecciona un archivo PDF válido.');
      resetDocument();
      return;
    }
    selectedFile = file;
    fileName.textContent = file.name;
    fileName.title = file.name;
    uploadArea.classList.add('hidden');
    selectedArea.classList.remove('hidden');
    resultArea.classList.add('hidden');
    lastResult = null;
  }

  async function analyzeDocument() {
    if (!selectedFile) return;

    const originalText = btnAnalyze.textContent;
    btnAnalyze.textContent = 'Analizando documento...';
    btnAnalyze.disabled = true;

    // Simulamos extracción, análisis heurístico y generación del informe.
    await new Promise((resolve) => setTimeout(resolve, 1600));

    lastResult = {
      nombre: selectedFile.name,
      porcentaje: '73%',
      interpretacion: 'El análisis simulado encontró varios patrones compatibles con texto generado o asistido mediante IA. El resultado representa una estimación heurística y no una prueba definitiva de autoría.',
      estadisticas: [
        { label: 'Uniformidad sintáctica', value: 'Alta' },
        { label: 'Variación léxica', value: 'Moderada-baja' },
        { label: 'Patrones repetitivos', value: 'Detectados' },
        { label: 'Estructura discursiva', value: 'Muy regular' }
      ],
      indicadores: [
        'Alta regularidad en la longitud y construcción de oraciones.',
        'Uso recurrente de formulaciones genéricas y conectores predecibles.',
        'Repetición de conceptos con poca variación léxica.',
        'Estructura argumentativa homogénea a lo largo de los fragmentos evaluados.'
      ],
      evidencias: simulatedEvidence
    };

    score.textContent = lastResult.porcentaje;
    summary.textContent = lastResult.interpretacion;
    resultArea.classList.remove('hidden');
    btnAnalyze.textContent = originalText;
    btnAnalyze.disabled = false;
  }

  fileInput.addEventListener('change', () => {
    const [file] = fileInput.files;
    if (file) showSelectedFile(file);
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
  btnCloseParchment?.addEventListener('click', closeParchment);
  parchmentModal?.addEventListener('click', (event) => {
    if (event.target === parchmentModal) closeParchment();
  });

  document.addEventListener('keydown', (event) => {
    if (event.code === 'Escape' && parchmentModal && !parchmentModal.classList.contains('hidden')) closeParchment();
  });
});
