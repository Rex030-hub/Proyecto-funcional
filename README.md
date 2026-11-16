<div align="center">

#  Bibliography Search

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


##  Descripción

**Bibliography Search** es una plataforma web que asiste a estudiantes e investigadores en la evaluación crítica de sus fuentes. Se presenta como un *libro de análisis* de cuatro páginas: dos dedicadas al análisis (URL y PDF) y dos a las instrucciones de uso, más un módulo de cumplimientos normativos.

El sistema se compone de dos piezas desacopladas:

| Pieza | Responsabilidad | Tecnología |
|---|---|---|
| **Frontend** | Interfaz "libro", validación de entrada, presentación de informes | HTML · CSS · JavaScript (sin framework) |
| **Backend** | Extracción, cálculo forense, persistencia y generación de citas | Spring Boot 4.1.1 · Java 25 |

Ambas se comunican exclusivamente por HTTP/JSON bajo un contrato uniforme de respuesta.



##  Arquitectura

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

##  Stack tecnológico

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


##  API REST

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


### Decisión de diseño documentada

> **El texto completo extraído no se persiste.** Se consume en memoria para calcular tópico, puntaje y conteo de palabras, y se descarta al terminar la extracción. La cita APA se recalcula en cada lectura porque solo requiere metadatos ya guardados.
>
> Criterio aplicado: *persistir lo que no puede reconstruirse y reconstruir lo barato*. Esto reduce cada fila de decenas de kilobytes a aproximadamente uno, elimina la retención innecesaria de contenido de terceros y mantiene la caché fully funcional.

Las fechas se almacenan como texto porque la fuente externa entrega formatos heterogéneos (ISO completo, solo año, valores nulos o cadenas no parseables). Tiparlas como `DATE` provocaría fallos de inserción con datos reales.

---

##  Seguridad

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

##  Instalación local

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
---

### Comunicación entre ambos

El frontend consume la API del backend por HTTPS. El backend autoriza el origen de Vercel mediante `APP_CORS_ORIGINS`. No existe acoplamiento de despliegue: cada pieza puede actualizarse de forma independiente mientras se respete el contrato de la API.

---

##  Pruebas

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

## 📄 Licencia


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

##  Autores

| Nombre | GitHub |
|---|---|
| **Rodrigo Ayelef** | [@Rex030-hub](https://github.com/Rex030-hub) |
| **Felipe Bravo** | [@ndlr423](https://github.com/ndlr423) |
| **Ignacio Aliaga** | [@Ialiaga-pin](https://github.com/Ialiaga-pin) |

---


<div align="center">

**Bibliography Search** — *El libro del análisis*

Proyecto académico en evolución · 2026

</div>
