-- Esquema OBJETIVO PostgreSQL (despliegue). En local, FuenteRepository crea
-- el esquema equivalente en SQLite automáticamente. Mantener ambos sincronizados.
-- Corrección histórica: se elimina el índice sobre reliability_score (columna retirada).
CREATE TABLE IF NOT EXISTS web_metadata (
    id             SERIAL PRIMARY KEY,
    url            VARCHAR(2048) NOT NULL UNIQUE,
    domain         VARCHAR(255) NOT NULL,
    title          VARCHAR(512),
    author         VARCHAR(255),
    published_date VARCHAR(100),
    text_content   TEXT,
    topic          VARCHAR(255),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_web_metadata_domain ON web_metadata(domain);