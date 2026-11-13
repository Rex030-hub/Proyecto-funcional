CREATE TABLE IF NOT EXISTS web_metadata (
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url            TEXT NOT NULL,                          
    domain         VARCHAR(255) NOT NULL,
    title          VARCHAR(512),
    author         VARCHAR(255),
    published_date VARCHAR(100),                           -- se queda TEXT: datos sucios de Exa
    word_count     INTEGER,    
    topic          VARCHAR(255),                            
    score          INTEGER,                               
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Unicidad de URL sin riesgo de límite de tamaño de índice
CREATE UNIQUE INDEX IF NOT EXISTS idx_web_metadata_url_md5 ON web_metadata (md5(url));

-- Índices útiles para tus consultas reales
CREATE INDEX IF NOT EXISTS idx_web_metadata_domain ON web_metadata(domain);