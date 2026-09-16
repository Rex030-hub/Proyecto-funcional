# Bibliography Search — Frontend

Frontend del prototipo interactivo de **Bibliography Search**.

## Estructura

- `index.html`: estructura y contenido de la interfaz.
- `css/base.css`: estilos globales, temas y entorno.
- `css/book.css`: libro 3D, páginas y componentes de los analizadores.
- `css/navigation.css`: controles de navegación del libro.
- `css/modal.css`: modal de resultados tipo pergamino.
- `js/app.js`: cambio de temas.
- `js/book.js`: apertura, cierre y atajos del libro.
- `js/navigation.js`: cambio entre las tres secciones del libro.
- `js/modal.js`: vista de políticas ISO.
- `js/url-analyzer.js`: conexión preparada con el endpoint de análisis URL y botón de detalles.
- `js/document-analyzer.js`: flujo de selección, análisis, resultado resumido y detalles del documento.
- `js/api-config.js`: punto único para configurar la URL base de la futura API.

## Página III — Analizador de PDF

La **Página III no es un lector PDF**. Su flujo visual está pensado para imitar el analizador URL:

1. El usuario selecciona o arrastra un PDF.
2. Aparece el archivo seleccionado y el botón **Analizar Documento**.
3. Al finalizar el análisis se muestra un **resultado resumido dentro de la misma página**.
4. El usuario puede pulsar **Detalles**.
5. El botón abre el mismo tipo de pergamino/modal utilizado por el análisis URL, pero con el informe documental completo.

El informe está preparado para mostrar un porcentaje aproximado de patrones compatibles con uso o asistencia de IA, interpretación e indicadores detectados. El frontend no presenta este porcentaje como una prueba definitiva de autoría.

## Backend futuro

El análisis URL espera:

`POST /api/analizar`

con JSON:

```json
{ "url": "https://ejemplo.com" }
```

El analizador documental está preparado para:

`POST /api/analizar-documento`

con `multipart/form-data` y el campo:

`documento=<archivo PDF>`

La respuesta puede entregar, por ejemplo:

```json
{
  "data": {
    "nombre_archivo": "trabajo.pdf",
    "porcentaje_ia": 72,
    "interpretacion": "Se detectaron varios patrones compatibles con asistencia de IA.",
    "indicadores": [
      "Baja variación sintáctica",
      "Distribución léxica muy uniforme",
      "Patrones repetitivos de estructura"
    ]
  }
}
```

La URL base está centralizada en `js/api-config.js` para facilitar el cambio cuando se incorpore el backend real.

## Modo demostración para presentación

La entrega actual funciona completamente como frontend y no requiere un backend para demostrar el flujo.

- **Análisis URL:** recibe la URL introducida y genera una respuesta simulada consistente. El dominio mostrado se obtiene de la URL ingresada para que la interacción sea visible durante la presentación.
- **Análisis PDF:** recibe realmente un archivo mediante el selector/arrastre, valida que sea PDF y luego ejecuta una simulación de análisis heurístico.
- **Detalles PDF:** después de analizar aparece el botón **Detalles**, que abre un informe tipo pergamino con porcentaje simulado, indicadores, evidencias textuales resaltadas y explicación de por qué cada fragmento fue marcado.
- Los resaltados y evidencias son **demostrativos**. No representan un detector de IA real y no deben interpretarse como una prueba de autoría.
- La estructura de datos de ambos módulos queda separada para facilitar la sustitución de la simulación por llamadas al backend posteriormente.
