const ISO_POLICIES_DATA = {
  security: {
    title: "Política de Seguridad de la Información — ISO/IEC 27001 & 27701",
    content: `
      <h3>1. Sistema de Gestión de Seguridad de la Información (SGSI)</h3>
      <p>Bibliography Search opera bajo los lineamientos de la norma ISO/IEC 27001. Toda entrada de datos (direcciones URL o documentos subidos) se procesa en un entorno aislado con cifrado de extremo a extremo (TLS 1.3).</p>

      <h3>2. Protección de Datos Personales y Privacidad (ISO/IEC 27701)</h3>
      <p>Cumpliendo con los estándares de gestión de información de privacidad, la plataforma garantiza el principio de minimización de datos. No se conservan registros persistentes ni se perfila el comportamiento del usuario durante las consultas bibliográficas.</p>

      <h3>3. Análisis en Memoria Reversible</h3>
      <p>Las evaluaciones heurísticas se ejecutan en memoria volátil. Una vez que la sesión expira o el resultado es desplegado, la huella digital del recurso analizado es purgada permanentemente de los nodos de cómputo.</p>
    `
  },
  quality: {
    title: "Calidad de Software y Accesibilidad — ISO/IEC 25010 & WCAG",
    content: `
      <h3>1. Modelo de Calidad de Software (ISO/IEC 25010)</h3>
      <p>Nuestra arquitectura garantiza altos niveles de eficiencia de desempeño, compatibilidad, usabilidad y fiabilidad. Los algoritmos de estimación operan bajo márgenes de tolerancia rigurosamente validados.</p>

      <h3>2. Accesibilidad e Interacción Humano-Computadora</h3>
      <p>El diseño visual respeta las pautas W3C / WCAG 2.1 Nivel AA, asegurando contrastes adecuados, estructuras legibles y navegación fluida adaptable a múltiples dispositivos.</p>

      <h3>3. Transparencia Algorítmica</h3>
      <p>Los indicadores de veracidad resultantes corresponden a modelos analíticos de soporte, promoviendo la revisión técnica del usuario para mantener la integridad de las fuentes académicas.</p>
    `
  },
  terms: {
    title: "Términos del Servicio y SLA — ISO 9001",
    content: `
      <h3>1. Sistema de Gestión de Calidad (ISO 9001)</h3>
      <p>Bibliography Search mantiene un compromiso de continuidad de servicio con disponibilidad garantizada. Toda actualización en el motor analítico sigue controles de calidad y auditoría de software.</p>

      <h3>2. Condición de Publicación y Uso Aceptable</h3>
      <p>Esta plataforma es apta para su publicación y distribución global al cumplir estrictamente con los requisitos legales y de protección de propiedad intelectual vigentes.</p>

      <h3>3. Acuerdos de Responsabilidad</h3>
      <p>Los reportes generados constituyen herramientas auxiliares de investigación. El usuario es responsable de la cita definitiva y verificación primaria de los documentos analizados.</p>
    `
  }
};

document.addEventListener('DOMContentLoaded', () => {
  const mainBookView = document.getElementById('main-book-view');
  const isoView = document.getElementById('iso-policy-view');
  const isoTitle = document.getElementById('iso-section-title');
  const isoContent = document.getElementById('iso-section-content');
  const btnBack = document.getElementById('btn-back-to-book');
  const drawerLinks = document.querySelectorAll('.drawer-link');

  function navigateToIsoSection(sectionKey) {
    if (ISO_POLICIES_DATA[sectionKey]) {
      isoTitle.textContent = ISO_POLICIES_DATA[sectionKey].title;
      isoContent.innerHTML = ISO_POLICIES_DATA[sectionKey].content;

      if (mainBookView) mainBookView.classList.add('hidden');
      if (isoView) isoView.classList.remove('hidden');
    }
  }

  function returnToBookView() {
    if (isoView) isoView.classList.add('hidden');
    if (mainBookView) mainBookView.classList.remove('hidden');
    history.replaceState(null, '', window.location.pathname + window.location.search);
  }

  drawerLinks.forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const sectionKey = link.getAttribute('data-iso');
      navigateToIsoSection(sectionKey);
      history.replaceState(null, '', `#iso-${sectionKey}`);
    });
  });

  if (btnBack) {
    btnBack.addEventListener('click', () => {
      returnToBookView();
    });
  }

  const initialHash = window.location.hash.replace('#', '');
  if (initialHash) {
    const matchedKey = Object.keys(ISO_POLICIES_DATA).find(k => initialHash.includes(k));
    if (matchedKey) {
      navigateToIsoSection(matchedKey);
    }
  }
});