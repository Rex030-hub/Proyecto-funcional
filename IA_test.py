import os
from exa_py import Exa
from urllib.parse import urlparse

# 1. Configura tu API Key
# Reemplaza con la clave que copiaste del Dashboard
EXA_API_KEY = "78e60621-ee04-4ba5-a772-49c1ba2b4f18" 

# Inicializar cliente
exa = Exa(api_key=EXA_API_KEY)

# 2. URL fuente enviada por el usuario 
#https://es.wikipedia.org/wiki/Dioses_olímpicos
#https://techcrunch.com/2024/01/15/what-is-artificial-intelligence/
#https://www.google.com/search?q=dioses+griegos&client=opera-gx&hs=aPJ&sca_esv=77e7d99cf23041a7&sxsrf=APpeQns95pUhZBtsWkUcqcQ2APhDIVzppQ%3A1788309391688&ei=j2-XauHOKf_51sQP3MOjqAk&biw=1398&bih=822&oq=DIos&gs_lp=Egxnd3Mtd2l6LXNlcnAiBERJb3MqAggAMgQQIxgnMgoQABiABBiKBRhDMgoQABiABBiKBRhDMgoQABiABBiKBRhDMg0QABiABBiKBRhDGLEDMg0QABiABBiKBRhDGLEDMgoQABiABBiKBRhDMggQLhiABBixAzIFEAAYgAQyBRAAGIAESNoZUM4LWK8PcAF4AZABAJgBXaABvgKqAQE0uAEDyAEA-AEBmAIFoALdAqgCFMICBxAjGOoCGCfCAg0QLhjHARjRAxjqAhgnwgIQEC4YAxiPARjqAhi0AtgBAcICEBAAGAMYjwEY6gIYtALYAQHCAhAQABiABBiKBRhDGLEDGIMBwgINEC4YgAQYigUYQxixA8ICChAuGIAEGIoFGEOYAwriAwUSATEgQPEFiqdGjDI4w0K6BgYIARABGAqSBwM0LjGgB5sssgcDMy4xuAfSAsIHBTItNC4xyAcZgAgB&sclient=gws-wiz-serp
#https://es.wikipedia.org/wiki/Inteligencia_artificial
url_semilla = "https://es.wikipedia.org/wiki/Inteligencia_artificial"

print(f"Buscando páginas con contenido similar a: {url_semilla}\n")
print("-----------------------------------------------------------")

def Detectar_tema(url: str) -> str:
    respuesta = exa.get_contents(
        urls=url,
        summary={
            "query": "Escribe el tema principal del texto, si es posible, en un maximo de 15 palabras."
        }
    )
    
    if respuesta.results:
        resultado = respuesta.results[0]
        return resultado.summary
    else:
        return "No se pudo extraer el contenido de la URL."
    
def buscar_paginas_similares_por_tema(tema_detectado: str, cantidad_resultados: int = 5):
    
    busqueda = exa.search(
        query=tema_detectado,          # Usamos la descripción del tema como consulta
        type="neural",                 # Activa la búsqueda por significado/IA
        num_results=cantidad_resultados,
        
        #contents={
        #    "summary": {               # Solicitamos un breve resumen de cada nueva página encontrada
        #        "query": "Resumen de lo que trata esta página"
        #    }
        #}
    )
    
    print(f"--- SE ENCONTRARON {len(busqueda.results)} PÁGINAS SIMILARES ---\n")
    for i, resultado in enumerate(busqueda.results, 1):
        print(f"[{i}] Título: {resultado.title}")
        print(f"    URL: {resultado.url}")
        #if hasattr(resultado, 'summary') and resultado.summary:
        #    print(f"    Resumen: {resultado.summary}")
        print("-" * 50)

def Buscar_web_x_tema (Excluir: str, Tema: str, n: int):
    
    busqueda = exa.search(
        query= Tema,
        type="neural",
        exclude_domains=Excluir,
        num_results=1
    )
    for i, resultado in enumerate(busqueda.results, 1):
        parsed_url = urlparse(resultado.url)
        print(f"Resultado {n}: {resultado.title}")
        print(f"URL: {resultado.url}")
    return parsed_url.netloc

# --- PRUEBA DEL CÓDIGO ---
tema = Detectar_tema(url_semilla)
dominios = []

print("--- TEMA DETECTADO POR EXA ---")
print(tema)
while len(dominios) < 5:
    dominios.append(Buscar_web_x_tema(dominios, tema, len(dominios)))
print(f"Dominios detectados: {dominios}")
#buscar_paginas_similares_por_tema(tema,cantidad_resultados=3)

