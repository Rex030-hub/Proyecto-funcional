<div align="center">

# 📚 Bibliography Search

**El libro del análisis**

> *"De una página dudosa a la investigación perfecta en un solo clic."*

Evalúa la confiabilidad de fuentes web, detecta patrones compatibles con generación por IA en documentos PDF y produce citas APA 7 listas para usar.

[![Estado](https://img.shields.io/badge/estado-en%20producci%C3%B3n-2f6f4f?style=flat-square)](#)
[![Java](https://img.shields.io/badge/Java-25-b07219?style=flat-square&logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](#)
[![PDFBox](https://img.shields.io/badge/Apache%20PDFBox-3.0.3-d22128?style=flat-square&logo=apache&logoColor=white)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-producci%C3%B3n-4169E1?style=flat-square&logo=postgresql&logoColor=white)](#)
[![SQLite](https://img.shields.io/badge/SQLite-desarrollo-003B57?style=flat-square&logo=sqlite&logoColor=white)](#)
[![Licencia](https://img.shields.io/badge/licencia-MIT-c9a227?style=flat-square)](#-licencia)

[Demo en vivo](#-despliegue) · [API](#-api-rest) · [Arquitectura](#-arquitectura) · [Instalación](#-instalación-local) · [Informe académico](#-alcance-y-limitaciones)

</div>

---

## 📑 Tabla de contenidos

1. [Descripción](#-descripción)
2. [El problema que abordamos](#-el-problema-que-abordamos)
3. [Funcionalidades](#-funcionalidades)
4. [Arquitectura](#-arquitectura)
5. [Stack tecnológico](#-stack-tecnológico)
6. [Estructura del repositorio](#-estructura-del-repositorio)
7. [API REST](#-api-rest)
8. [Modelo de puntaje — Fuentes web](#-modelo-de-puntaje--fuentes-web)
9. [Modelo forense — Documentos PDF](#-modelo-forense--documentos-pdf)
10. [Modelo de datos](#-modelo-de-datos)
11. [Seguridad](#-seguridad)
12. [Observabilidad](#-observabilidad)
13. [Instalación local](#-instalación-local)
14. [Variables de entorno](#-variables-de-entorno)
15. [Despliegue](#-despliegue)
16. [Pruebas](#-pruebas)
17. [Alcance y limitaciones](#-alcance-y-limitaciones)
18. [Hoja de ruta](#-hoja-de-ruta)
19. [Contribución](#-contribución)
20. [Licencia](#-licencia)
21. [Autores](#-autores)

---

## 📖 Descripción

**Bibliography Search** es una plataforma web que asiste a estudiantes e investigadores en la evaluación crítica de sus fuentes. Se presenta como un *libro de análisis* de cuatro páginas: dos dedicadas al análisis (URL y PDF) y dos a las instrucciones de uso, más un módulo de cumplimientos normativos.

El sistema se compone de dos piezas desacopladas:

| Pieza | Responsabilidad | Tecnología |
|---|---|---|
| **Frontend** | Interfaz "libro", validación de entrada, presentación de informes | HTML · CSS · JavaScript (sin framework) |
| **Backend** | Extracción, cálculo forense, persistencia y generación de citas | Spring Boot 4.1.1 · Java 25 |

Ambas se comunican exclusivamente por HTTP/JSON bajo un contrato uniforme de respuesta.

---

## 🎯 El problema que abordamos

En el trabajo académico cotidiano se consultan sitios de procedencia dudosa sin criterios objetivos para distinguir una fuente confiable de una que no lo es. A esto se suma la dificultad de determinar si un documento fue redactado por una persona o generado con asistencia de IA, y el tiempo que consume formatear cada referencia en APA.

Bibliography Search automatiza esos tres frentes:

- Convierte características **verificables** de una fuente en un puntaje cuantificable y explicable.
- Analiza la **estructura estadística y la huella digital** de un PDF, mostrando las evidencias que motivan cada observación.
- Genera la **cita APA 7** y sugiere hasta cinco fuentes relacionadas de mayor calidad académica.

> **Principio rector:** el sistema no reemplaza el criterio del investigador. Entrega evidencia trazable para que la decisión final siga siendo humana.

---

## ✨ Funcionalidades

### Módulo I — Análisis de fuentes web

- **Extracción en vivo** mediante la API de Exa (`livecrawl: always`): título, autoría, fecha de publicación y contenido.
- **Caché idempotente por URL.** Una fuente ya analizada se sirve desde la base de datos sin volver a consumir créditos externos. La respuesta declara `desdeCache: true|false` y el tiempo total del análisis.
- **Puntaje de confianza (0–100)** calculado con seis criterios ponderados y persistido al momento del análisis.
- **Cita APA 7 automática**, con normalización de fechas al español (`(2025, junio 28).`, `(2025).`, `(s. f.).`).
- **Hasta 5 fuentes relacionadas** por afinidad de tópico, extraídas y persistidas automáticamente.
- **CRUD completo** sobre las fuentes consultadas.

### Módulo II — Análisis forense de PDF

- Recepción de archivos de hasta **25 MB** con validación de firma binaria `%PDF-`.
- Extracción de texto y metadatos con **Apache PDFBox 3**.
- **5 pilares votantes + 3 testigos** y una **huella digital** de metadata que ajusta el veredicto.
- **Veredicto porcentual con interpretación en lenguaje natural**, estadísticas por pilar, indicadores y **evidencias citadas** con su justificación.
- **Puertas de calidad:** rechaza documentos escaneados, con texto insuficiente, en idioma no soportado o con extracción fragmentada.

### Capacidades transversales

- **Contrato uniforme** `{ exito, datos, mensaje }` en todos los endpoints.
- **Errores honestos:** los `4xx` explican el problema al usuario; los `5xx` nunca filtran configuración interna.
- **Control de concurrencia** con semáforos por módulo.
- **Id de correlación** por petición, presente en cada línea de log y en el header `X-Request-Id`.
- **Modo demostración declarado:** si el backend no responde, la interfaz lo informa explícitamente en lugar de fingir un resultado.

---

## 🏗 Arquitectura

```
┌──────────────────────┐        HTTPS / JSON        ┌────────────────────────┐
│      FRONTEND        │ ─────────────────────────► │   BACKEND (Spring)     │
│  Vercel · estático   │ ◄───────────────────────── │   Render · Docker      │
│  api-config.js       │                            │   /api/**              │
└──────────────────────┘                            └───────────┬────────────┘
                                                                │
                        ┌───────────────────────┬───────────────┴──────────┐
                        ▼                       ▼                          ▼
                 ┌─────────────┐        ┌──────────────┐          ┌──────────────┐
                 │ PostgreSQL  │        │   Exa API    │          │ Apache       │
                 │ (Render)    │        │  (externa)   │          │ PDFBox       │
                 │ web_metadata│        │ contents +   │          │ (en proceso) │
                 │             │        │ search       │          │              │
                 └─────────────┘        └──────────────┘          └──────────────┘
```

### Capas del backend

| Capa | Piezas | Responsabilidad |
|---|---|---|
| **Controller** | `FuenteController`, `DocumentoController`, `HealthController`, `GlobalExceptionHandler` | Entrada HTTP, códigos de estado, traducción de errores |
| **Service** | `UrlAnalysisService`, `AnalisisService`, `ExaClient`, `ExtractorPdf` | Orquestación, caché, concurrencia, integración externa |
| **Dominio** | `ScoreCalculator`, `ApaFormatter`, `TopicExtractor`, `JsonMini`, `Pilares`, `HuellaDigital`, `Parametros`, `Diccionarios`, `Idioma`, `TextoProcesado`, `Sanitizador` | Lógica pura, sin dependencias de infraestructura |
| **Repository** | `FuenteRepository` | Persistencia SQL y mapeo de filas |
| **DTO** | `RespuestaApi`, `FuenteAnalisis`, `FuenteDatos`, `AnalisisRespuesta`, `DatosAnalisis`, `Estadistica`, `Evidencia`, `UrlRequest`, `UrlUpdateRequest` | Contrato de entrada/salida |
| **Config** | `RequestLoggingFilter` | Correlación y trazabilidad |

Los módulos pueden activarse o desactivarse de forma independiente mediante `app.modulo.url.enabled` y `app.modulo.pdf.enabled`.

---

## 🧰 Stack tecnológico

**Backend**
- Java 25 · Spring Boot 4.1.1 · Maven (wrapper incluido)
- Apache PDFBox 3.0.3 — extracción de texto y metadatos
- JDBC directo (sin ORM) — PostgreSQL en producción, SQLite en desarrollo
- SLF4J + Logback — logging con MDC
- `java.net.http.HttpClient` — cliente de la API externa
- Lombok

**Frontend**
- HTML5 semántico · CSS3 · JavaScript ES6+ sin dependencias
- `fetch` + `FormData` para consumo de la API
- Modales de pergamino, tema claro/oscuro (`Alt + T`), arrastrar y soltar

**Infraestructura**
- Render (Web Service Docker + PostgreSQL gestionado)
- Vercel (sitio estático)
- Docker multi-stage sobre Eclipse Temurin 25

---

## 📂 Estructura del repositorio

```
.
├── backend/
│   ├── src/main/java/com/bibliography/analyzer/
│   │   ├── AnalyzerBackendApplication.java
│   │   ├── controller/
│   │   │   ├── FuenteController.java
│   │   │   ├── DocumentoController.java
│   │   │   ├── HealthController.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── service/
│   │   │   ├── UrlAnalysisService.java
│   │   │   ├── AnalisisService.java
│   │   │   ├── ExaClient.java
│   │   │   ├── ExtractorPdf.java
│   │   │   └── ServiceSaturedException.java
│   │   ├── dominio/url/
│   │   │   ├── WebMetadata.java
│   │   │   ├── ScoreCalculator.java
│   │   │   ├── ApaFormatter.java
│   │   │   ├── TopicExtractor.java
│   │   │   └── JsonMini.java
│   │   ├── analisis/
│   │   │   ├── Pilares.java
│   │   │   ├── HuellaDigital.java
│   │   │   ├── Parametros.java
│   │   │   ├── Diccionarios.java
│   │   │   ├── TextoProcesado.java
│   │   │   ├── Metrica.java
│   │   │   ├── ReporteForense.java
│   │   │   ├── Idioma.java
│   │   │   └── Sanitizador.java
│   │   ├── repository/FuenteRepository.java
│   │   ├── config/RequestLoggingFilter.java
│   │   └── dto/
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── schema.sql
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw / mvnw.cmd
│
├── frontend/
│   ├── index.html
│   ├── api-config.js
│   ├── url-analyzer.js
│   ├── document-analyzer.js
│   └── (hojas de estilo y módulos del libro)
│
├── docs/
│   └── demo-api-rest.postman_collection.json
├── LICENSE
└── README.md
```

> `config.properties`, `metadata.db` y `logs/` están excluidos del control de versiones.

---

## 🔌 API REST

**Base URL:** `https://<tu-servicio>.onrender.com`

| Método | Endpoint | Descripción | Éxito |
|---|---|---|---|
| `GET` | `/api/health` | Estado del servicio y módulos activos | `200` |
| `POST` | `/api/analizar-url` | Analiza una URL (extrae o sirve desde caché) | `200` |
| `GET` | `/api/fuentes` | Lista todas las fuentes persistidas | `200` |
| `GET` | `/api/fuentes/{id}` | Consulta una fuente por identificador | `200` |
| `PUT` | `/api/fuentes/{id}` | Actualiza título, autor o tópico | `200` |
| `DELETE` | `/api/fuentes/{id}` | Elimina una fuente | `204` |
| `POST` | `/api/analizar-documento` | Análisis forense de un PDF (`multipart/form-data`) | `200` |

### Códigos de error

| Código | Situación |
|---|---|
| `400` | URL vacía o con formato inválido · archivo vacío · JSON mal formado |
| `404` | Fuente inexistente o módulo deshabilitado |
| `405` | Método HTTP no soportado |
| `413` | El archivo supera los 25 MB |
| `415` | La firma binaria no corresponde a un PDF |
| `422` | PDF escaneado, texto insuficiente, idioma no soportado o extracción fragmentada |
| `423` | PDF protegido con contraseña |
| `500` | Error interno (mensaje genérico; detalle en logs con `X-Request-Id`) |
| `502` | El motor de búsqueda externo no está disponible |
| `503` | Módulo saturado: demasiados análisis en curso |
| `504` | Tiempo de espera agotado con el servicio externo |

### Ejemplo — analizar una URL

```bash
curl -X POST https://<tu-servicio>.onrender.com/api/analizar-url \
     -H "Content-Type: application/json" \
     -d '{"url":"https://es.wikipedia.org/wiki/Inteligencia_artificial"}'
```

```json
{
  "exito": true,
  "datos": {
    "fuente": {
      "id": 1,
      "url": "https://es.wikipedia.org/wiki/Inteligencia_artificial",
      "dominio": "es.wikipedia.org",
      "titulo": "Inteligencia artificial",
      "autor": "Sin autor",
      "fechaPublicacion": "2025-06-28T22:18:00.000Z",
      "topico": "Inteligencia / Artificial / Aprendizaje",
      "puntajeConfianza": 62,
      "formatoApa": "Inteligencia artificial. (2025, junio 28). es.wikipedia.org. https://...",
      "cantidadPalabras": 5835,
      "fechaAnalisis": "2026-11-13 15:48:16"
    },
    "desdeCache": false,
    "relacionadas": [ ],
    "duracionMs": 24297
  },
  "mensaje": null
}
```

### Ejemplo — analizar un PDF

```bash
curl -X POST https://<tu-servicio>.onrender.com/api/analizar-documento \
     -F "documento=@documento.pdf"
```

```json
{
  "exito": true,
  "datos": {
    "nombre": "documento.pdf",
    "porcentaje": "28%",
    "interpretacion": "El análisis forense no encontró indicios significativos...",
    "estadisticas": [ { "label": "Anclaje empírico", "value": "1.84 · Humano" } ],
    "indicadores": [ "Fuerte anclaje empírico: cifras, fechas y citas verificables." ],
    "evidencias": [
      {
        "type": "Señal humana",
        "label": "Voz autoral",
        "text": "Voz autoral: valor medido 0.62 (score de sospecha 12/100).",
        "reason": "La presencia de primera persona y preguntas imprime una perspectiva humana verificable."
      }
    ]
  },
  "mensaje": null
}
```

---

## 🧮 Modelo de puntaje — Fuentes web

El puntaje de confianza es un **promedio ponderado de seis criterios**, cada uno evaluado de 0 a 100:

| Criterio | Peso | Regla |
|---|:--:|---|
| **Dominio** | 25 % | `.edu` / `.gov` / `.gob` (y variantes `.edu.` etc.) → 100 · `.org` → 50 · resto → 25 |
| **Autoría** | 25 % | Autor declarado → 100 · sin autor → 25 |
| **Fecha de publicación** | 15 % | Fecha válida → penalización progresiva por antigüedad · ausente o ilegible → 25 |
| **Extensión del contenido** | 15 % | > 5000 caracteres → 100 · > 1500 → 50 · > 500 → 25 · menor → 0 |
| **Presencia de referencias** | 15 % | Menciona bibliografía, citas o DOI → 100 · ausencia → 0 |
| **Calidad de puntuación** | 5 % | Base 100 con descuentos por espaciado incorrecto, signos repetidos, capitalización indebida, cierre sin punto y tramos sin pausas |

```
puntaje = Σ (evaluación_i × peso_i) / 100     (acotado a 100)
```

**Interpretación en la interfaz**

| Rango | Veredicto |
|---|---|
| ≥ 80 | La fuente presenta alta confiabilidad |
| 60 – 79 | La fuente presenta confiabilidad moderada |
| < 60 | Se recomienda verificar con fuentes adicionales |

> **Persistencia como fotografía.** El puntaje se calcula y guarda en el momento del análisis. Las lecturas posteriores desde caché devuelven ese mismo valor: es el veredicto del criterio vigente cuando la fuente fue examinada, no un cálculo retroactivo.

---

## 🔬 Modelo forense — Documentos PDF

> ⚠️ **Dirección del porcentaje:** en este módulo el valor expresa **patrones compatibles con generación por IA**. Un porcentaje **alto** indica mayor coincidencia con patrones automáticos; un valor **bajo** es compatible con escritura humana. Es la dirección opuesta al puntaje de confianza del módulo URL.

### Pilares votantes

| Pilar | Peso | Qué mide |
|---|:--:|---|
| Anclaje empírico | 0.35 | Densidad de cifras, fechas, unidades y citas verificables |
| Complejidad sintáctica | 0.25 | Incisos, dos puntos, paréntesis, comillas y rayas |
| Burstiness | 0.15 | Desviación en la longitud de las oraciones |
| Voz autoral | 0.10 | Primera persona, preguntas retóricas y analogías propias |
| Intensificadores vacíos | 0.10 | Adverbios de énfasis sin aporte semántico |

Cada pilar devuelve un score de sospecha 0–100 y un flag de confiabilidad. Los pilares sin datos suficientes **no votan**. Existen además *puertas de concentración*: si los marcadores aparecen en muy pocos párrafos, el pilar registra la anomalía en lugar de promediarla.

### Testigos (peso 0)

Repetición de bigramas · Conectores vacíos · Uniformidad de párrafos. Se reportan como contexto y matizan los indicadores, pero **no alteran el porcentaje**.

### Huella digital del archivo

Ajuste acotado a **±10 puntos** sobre el veredicto textual:

| Señal | Efecto |
|---|:--:|
| Autoría declarada por un modelo (coincidencia exacta) | +10 |
| Documento "nacido completo" (> 800 palabras, creado y modificado en < 15 min) | +8 |
| Cadena editorial profesional (pdfTeX, Distiller, etc.) | −5 |
| Historial de edición extendido (> 24 h) | −8 |
| Documento re-procesado (qpdf, Ghostscript, etc.) | 0 — metadata no confiable |

### Cálculo y bandas

```
scoreTexto = Σ (peso_i × score_i) / Σ pesos_i   (solo pilares confiables)
índice     = clamp(scoreTexto + ajusteMetadata, 0, 100)
```

| Índice | Interpretación |
|---|---|
| < 35 | Sin indicios significativos de generación automática |
| 35 – 64 | **Zona gris:** patrones mixtos, se recomienda revisión manual |
| 65 – 84 | Varios patrones compatibles con generación por IA |
| ≥ 85 | Múltiples pilares y huella digital coinciden en patrones de IA |

### Puertas de calidad (rechazo con `422`)

- Menos de 100 palabras → posible documento escaneado.
- Idioma distinto del español → el modelo está calibrado únicamente para español.
- Extracción fragmentada (fuentes con codificación no estándar) → calidad insuficiente.

---

## 💾 Modelo de datos

Tabla `web_metadata` — clave natural: `url` única.

| Columna | Tipo (PostgreSQL) | Contenido |
|---|---|---|
| `id` | `IDENTITY` | Identificador autogenerado |
| `url` | `TEXT` | Dirección analizada |
| `domain` | `VARCHAR(255)` | Dominio extraído |
| `title` | `VARCHAR(512)` | Título de la página |
| `author` | `VARCHAR(255)` | Autoría declarada |
| `published_date` | `VARCHAR(100)` | Fecha tal como la entrega la fuente externa |
| `topic` | `VARCHAR(255)` | Tópico inferido por frecuencia léxica |
| `word_count` | `INTEGER` | Cantidad de palabras del contenido analizado |
| `score` | `INTEGER` | Puntaje de confianza persistido |
| `created_at` | `TIMESTAMP` | Marca temporal del análisis |

**Índices**
- Único sobre `md5(url)` — evita el límite de tamaño de fila de índice B-Tree con URLs largas.
- Simple sobre `domain`.

### Decisión de diseño documentada

> **El texto completo extraído no se persiste.** Se consume en memoria para calcular tópico, puntaje y conteo de palabras, y se descarta al terminar la extracción. La cita APA se recalcula en cada lectura porque solo requiere metadatos ya guardados.
>
> Criterio aplicado: *persistir lo que no puede reconstruirse y reconstruir lo barato*. Esto reduce cada fila de decenas de kilobytes a aproximadamente uno, elimina la retención innecesaria de contenido de terceros y mantiene la caché fully funcional.

Las fechas se almacenan como texto porque la fuente externa entrega formatos heterogéneos (ISO completo, solo año, valores nulos o cadenas no parseables). Tiparlas como `DATE` provocaría fallos de inserción con datos reales.

---

## 🔐 Seguridad

| Medida | Implementación |
|---|---|
| **Gestión de credenciales** | Precedencia: variable de entorno → propiedad JVM → `config.properties` (excluido del repositorio) |
| **No filtración de errores internos** | Los `5xx` devuelven un mensaje genérico; el detalle y el stack trace quedan solo en logs |
| **Validación de archivos** | Firma binaria `%PDF-`, tamaño máximo, rechazo de PDF cifrados |
| **Sanitización** | Todo texto proveniente del exterior (metadata, nombres de archivo) se escapa antes de llegar al informe |
| **Inyección SQL** | Uso exclusivo de `PreparedStatement` con parámetros vinculados |
| **Control de recursos** | Semáforos: 3 análisis URL y 2 PDF simultáneos; el exceso responde `503` |
| **Timeouts** | 10 s de conexión y 30 s de petición hacia el servicio externo |
| **CORS** | Orígenes permitidos configurables por variable de entorno |
| **Transporte** | HTTPS obligatorio en producción (Vercel y Render) |

**Reporte de vulnerabilidades:** si encuentras un problema de seguridad, por favor abre un *issue* privado o contacta directamente a alguno de los autores antes de divulgarlo.

---

## 👁 Observabilidad

Cada petición recibe un identificador de correlación de 8 caracteres que acompaña toda su traza:

```
15:48:09.971 [5ae7b37d] INFO  RequestLoggingFilter - INICIO POST /api/analizar-url desde 127.0.0.1
15:48:09.973 [5ae7b37d] INFO  UrlAnalysisService  - [url] consulta de caché ... -> miss
15:48:16.706 [5ae7b37d] INFO  ExaClient          - Exa /contents respondió 200 en 6732 ms
15:48:16.725 [5ae7b37d] INFO  UrlAnalysisService  - [url] fuente persistida en 5 ms
15:48:28.880 [5ae7b37d] INFO  RequestLoggingFilter - FIN POST /api/analizar-url status=200 total=18909 ms
```

- El id se expone al cliente en el header **`X-Request-Id`**, de modo que un incidente reportado desde el frontend puede localizarse en los logs sin ambigüedad.
- Se registran **tiempos por etapa**: consulta de caché, llamada externa, persistencia y resolución de relacionadas.
- El módulo PDF emite una línea de **calibración** con el score de cada pilar, el aporte de la metadata y el índice final.
- Salida simultánea a consola y a `logs/analyzer-backend.log`.

---

## 💻 Instalación local

### Requisitos

- **JDK 25**
- **Maven** (o el wrapper `mvnw` incluido)
- Una **clave de API de Exa** ([exa.ai](https://exa.ai))
- Navegador moderno

### Backend

```bash
# 1. Clonar
git clone https://github.com/<organizacion>/Biblography_Search.git
cd Biblography_Search/backend

# 2. Configurar la clave (elige UNA de las tres vías)
export EXA_API_KEY=tu_clave                 # variable de entorno
#   o bien: -Dexa.api.key=tu_clave          # propiedad JVM
#   o bien: config.properties en la raíz    # exa.api.key=tu_clave

# 3. Compilar y ejecutar
./mvnw clean spring-boot:run                # Windows: .\mvnw.cmd clean spring-boot:run
```

El servicio queda en `http://localhost:8080`. En desarrollo se crea automáticamente `metadata.db` (SQLite); no requiere instalación de motor de base de datos.

Verificación rápida:

```bash
curl http://localhost:8080/api/health
# {"status":"UP","servicio":"analyzer-backend","moduloPdf":"true","moduloUrl":"true"}
```

### Frontend

El sitio es estático: basta con servirlo desde cualquier servidor local (por ejemplo *Live Server* de VS Code).

En `frontend/api-config.js`:

```javascript
window.APP_CONFIG = Object.freeze({
    API_BASE_URL: 'http://127.0.0.1:8080'
});
```

> ⚠️ Si cambias el esquema de la base de datos entre versiones, elimina `metadata.db` antes de reiniciar. `CREATE TABLE IF NOT EXISTS` no migra tablas existentes.

---

## ⚙️ Variables de entorno

| Variable | Obligatoria | Descripción | Valor por defecto |
|---|:--:|---|---|
| `EXA_API_KEY` | Sí (módulo URL) | Credencial del motor de búsqueda externo | — |
| `DB_URL` | No | Cadena JDBC de conexión | `jdbc:sqlite:metadata.db` |
| `PORT` | No | Puerto HTTP del servicio | `8080` |
| `APP_CORS_ORIGINS` | No | Orígenes permitidos, separados por coma | `*` |
| `APP_MODULO_URL_ENABLED` | No | Habilita el módulo de fuentes web | `true` |
| `APP_MODULO_PDF_ENABLED` | No | Habilita el módulo forense de PDF | `true` |

### Formato de `DB_URL` para PostgreSQL

El driver JDBC **no acepta credenciales incrustadas** con el formato `usuario:password@host`. Deben ir como parámetros de consulta:

```
jdbc:postgresql://<host>:5432/<base_de_datos>?user=<usuario>&password=<password>
```

Si la contraseña contiene caracteres reservados (`@`, `#`, `?`, `/`), deben codificarse en formato percent-encoding.

---

## 🚀 Despliegue

### Backend — Render

1. Crear una base de datos **PostgreSQL** en Render.
2. Crear un **Web Service** desde el repositorio con entorno **Docker** (el `Dockerfile` usa Eclipse Temurin 25 en construcción multi-stage).
3. Definir las variables de entorno: `EXA_API_KEY`, `DB_URL`, `APP_CORS_ORIGINS`.
4. El esquema se inicializa en el arranque; `schema.sql` documenta la estructura de referencia.
5. Verificar con `GET /api/health`.

> **Nota sobre el plan gratuito:** el servicio se suspende tras un período de inactividad. La primera petición después de la pausa puede tardar varias decenas de segundos mientras el contenedor reinicia. Es comportamiento esperado de la plataforma, no un fallo de la aplicación.

### Frontend — Vercel

1. Importar el repositorio en Vercel apuntando al directorio del frontend.
2. Ajustar `API_BASE_URL` en `api-config.js` con la URL pública del backend.
3. Desplegar. Al ser un sitio estático, no requiere paso de compilación.

### Comunicación entre ambos

El frontend consume la API del backend por HTTPS. El backend autoriza el origen de Vercel mediante `APP_CORS_ORIGINS`. No existe acoplamiento de despliegue: cada pieza puede actualizarse de forma independiente mientras se respete el contrato de la API.

---

## 🧪 Pruebas

El repositorio incluye una colección de Postman en `docs/demo-api-rest.postman_collection.json` que cubre:

| Caso | Resultado esperado |
|---|---|
| PDF con patrones de IA | `200` + informe con evidencias |
| PDF de escritura humana | `200` + veredicto bajo |
| Archivo vacío | `400` |
| PDF escaneado | `422` |
| PDF en idioma no soportado | `422` |
| URL nueva (extracción en vivo) | `200`, `desdeCache: false` |
| URL repetida (caché) | `200`, `desdeCache: true`, mismos valores |
| Listar fuentes | `200` + colección |
| Actualizar fuente | `200` + registro modificado |
| Eliminar fuente | `204` |

**Guion de regresión recomendado:** miss → hit → listar → actualizar → eliminar. El segundo análisis de la misma URL debe devolver idénticos `puntajeConfianza` y `cantidadPalabras`, lo que verifica que ambos valores se persistieron y se leen correctamente.

Antes de importar la colección, define la variable `baseUrl` y reemplaza las rutas de los archivos PDF por rutas locales válidas.

---

## 📏 Alcance y limitaciones

Este proyecto se desarrolla con fines académicos y su alcance está deliberadamente acotado:

- **El puntaje web mide solidez documental**, no veracidad semántica. Una fuente puede tener metadatos impecables y contener afirmaciones falsas.
- **El análisis de PDF es estadístico y heurístico**, no una prueba de autoría. Los patrones que detecta también pueden aparecer en textos escritos por personas, especialmente en prosa formal o técnica.
- **Calibración limitada al español.** El modelo forense fue ajustado con un corpus en español; otros idiomas se rechazan explícitamente en lugar de emitir un veredicto no confiable.
- **Dependencia de un proveedor externo.** La extracción de contenido web depende de la disponibilidad y los límites de la API de Exa.
- **Sin autenticación de usuarios.** La API es pública y abierta; el control de abuso se realiza mediante semáforos y CORS.
- **Los veredictos son apoyo al criterio, no sustituto.** Toda decisión académica derivada de estos informes debe pasar por revisión humana de las evidencias citadas.

---

## 🗺 Hoja de ruta

**Completado**
- [x] Módulo URL: extracción en vivo, caché idempotente, puntaje ponderado, APA 7, relacionadas y CRUD
- [x] Módulo PDF: análisis forense con pilares, testigos, huella digital y evidencias
- [x] Rediseño de persistencia sin retención de texto completo
- [x] Observabilidad con id de correlación y tiempos por etapa
- [x] Manejo unificado y no filtrante de errores
- [x] Frontend conectado a la API real con modo demostración declarado
- [x] Despliegue productivo: Render + PostgreSQL y Vercel

**En curso**
- [ ] Restricción de CORS al origen exacto del frontend
- [ ] Revisión y ampliación del corpus de calibración del módulo forense
- [ ] Colección Postman portable (rutas relativas y variables de entorno)

**Próximos pasos**
- [ ] Pruebas automatizadas (JUnit + MockMvc) para servicios y repositorio
- [ ] Documentación interactiva de la API (OpenAPI / Swagger)
- [ ] Integración continua con verificación de build en cada *pull request*
- [ ] Endpoint de re-evaluación de puntajes históricos ante cambios de calibración
- [ ] Soporte de idiomas adicionales en el módulo forense

---

## 🤝 Contribución

Este es un proyecto académico con desarrollo activo. Para proponer cambios:

1. Crea una rama a partir de `main` con el prefijo correspondiente: `feat/`, `fix/`, `docs/`, `refactor/`.
2. Mantén los cambios acotados a una responsabilidad. En particular, las modificaciones al modelo de puntaje deben quedar aisladas en la capa de dominio.
3. Verifica que `./mvnw clean compile` finalice sin errores y que el guion de regresión siga en verde.
4. Documenta el cambio en el mensaje de commit y, si afecta el comportamiento observable, actualiza este README.
5. Abre un *pull request* describiendo el problema que resuelve y cómo se verificó.

**Convención de commits:** `tipo(alcance): descripción en imperativo`
Ejemplo: `fix(repository): alinear el upsert con el índice único de PostgreSQL`

---

## 📄 Licencia

Distribuido bajo la **Licencia MIT**. Consulta el archivo [`LICENSE`](LICENSE) para el texto completo.

```
MIT License — Copyright (c) 2026 Rodrigo Ayelef, Felipe Bravo, Ignacio Aliaga
```

En resumen: se permite el uso, la copia, la modificación, la fusión, la publicación, la distribución, la sublicencia y la venta de copias del software, siempre que se conserve el aviso de copyright y el texto de la licencia. El software se entrega **"tal cual", sin garantía de ningún tipo**.

### Licencias de terceros

| Componente | Licencia |
|---|---|
| Spring Boot | Apache License 2.0 |
| Apache PDFBox | Apache License 2.0 |
| PostgreSQL JDBC Driver | BSD-2-Clause |
| SQLite JDBC (Xerial) | Apache License 2.0 |
| Project Lombok | MIT |

El uso de la **API de Exa** se rige por sus propios términos de servicio; este proyecto no redistribuye ni modifica su contenido más allá de los derivados necesarios para el análisis.

---

## 👥 Autores

| Nombre | GitHub |
|---|---|
| **Rodrigo Ayelef** | [@Rex030-hub](https://github.com/Rex030-hub) |
| **Felipe Bravo** | [@ndlr423](https://github.com/ndlr423) |
| **Ignacio Aliaga** | [@Ialiaga-pin](https://github.com/Ialiaga-pin) |

---

## 🙏 Agradecimientos

A los docentes que guiaron el desarrollo iterativo del proyecto y a los compañeros que participaron en las corridas de calibración del módulo forense.

---

<div align="center">

**Bibliography Search** — *El libro del análisis*

Proyecto académico en evolución · 2026

</div>