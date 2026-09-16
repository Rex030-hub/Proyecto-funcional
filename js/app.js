document.addEventListener('DOMContentLoaded', () => {
  const themeBtn = document.getElementById('btn-theme-toggle');
  const themeLabel = document.getElementById('theme-label');

  const themes = [
    { className: 'theme-default', name: 'Tema Estándar' },
    { className: 'theme-high-contrast', name: 'Alto Contraste' },
    { className: 'theme-parchment', name: 'Tono Pergamino' }
  ];

  let currentThemeIndex = 0;

  function applyTheme(index) {
    themes.forEach((theme) => document.body.classList.remove(theme.className));
    currentThemeIndex = index;
    const currentTheme = themes[currentThemeIndex];
    document.body.classList.add(currentTheme.className);
    if (themeLabel) themeLabel.textContent = currentTheme.name;
  }

  function cycleTheme() {
    applyTheme((currentThemeIndex + 1) % themes.length);
  }

  themeBtn?.addEventListener('click', cycleTheme);

  document.addEventListener('keydown', (event) => {
    if (event.altKey && event.key.toLowerCase() === 't') {
      event.preventDefault();
      cycleTheme();
    }
  });
});
