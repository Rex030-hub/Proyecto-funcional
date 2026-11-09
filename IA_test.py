import os
from exa_py import Exa
from urllib.parse import urlparse
import re

# 1. Configura tu API Key
# Reemplaza con la clave que copiaste del Dashboard
EXA_API_KEY = 

# Inicializar cliente
exa = Exa(api_key=EXA_API_KEY)

# 2. URL fuente enviada por el usuario 
#https://es.wikipedia.org/wiki/Dioses_olímpicos
#https://techcrunch.com/2024/01/15/what-is-artificial-intelligence/
#https://www.google.com/search?q=dioses+griegos&client=opera-gx&hs=aPJ&sca_esv=77e7d99cf23041a7&sxsrf=APpeQns95pUhZBtsWkUcqcQ2APhDIVzppQ%3A1788309391688&ei=j2-XauHOKf_51sQP3MOjqAk&biw=1398&bih=822&oq=DIos&gs_lp=Egxnd3Mtd2l6LXNlcnAiBERJb3MqAggAMgQQIxgnMgoQABiABBiKBRhDMgoQABiABBiKBRhDMgoQABiABBiKBRhDMg0QABiABBiKBRhDGLEDMg0QABiABBiKBRhDGLEDMgoQABiABBiKBRhDMggQLhiABBixAzIFEAAYgAQyBRAAGIAESNoZUM4LWK8PcAF4AZABAJgBXaABvgKqAQE0uAEDyAEA-AEBmAIFoALdAqgCFMICBxAjGOoCGCfCAg0QLhjHARjRAxjqAhgnwgIQEC4YAxiPARjqAhi0AtgBAcICEBAAGAMYjwEY6gIYtALYAQHCAhAQABiABBiKBRhDGLEDGIMBwgINEC4YgAQYigUYQxixA8ICChAuGIAEGIoFGEOYAwriAwUSATEgQPEFiqdGjDI4w0K6BgYIARABGAqSBwM0LjGgB5sssgcDMy4xuAfSAsIHBTItNC4xyAcZgAgB&sclient=gws-wiz-serp
#https://es.wikipedia.org/wiki/Inteligencia_artificial
#https://www.unesco.org/es/articles/inteligencia-artificial-y-desinformacion
#https://oficinac.es/es/informes-c/desinformacion-era-digital
url_semilla = "https://oficinac.es/es/informes-c/desinformacion-era-digital"

print(f"Buscando páginas con contenido similar a: {url_semilla}\n")
print("-----------------------------------------------------------")

def Extraer_datos(Texto: str) -> dict:
    Match_Porcentaje = re.search(r'Confiabilidad:\s*(\d+%)', Texto, re.IGNORECASE)
    confianza: str = str(Match_Porcentaje.group(1)) if Match_Porcentaje else "No encontrado"
    Match_Tema = re.search(r'Tema:\s*(.*?)(?=\n|Confiabilidad:|$)', Texto, re.IGNORECASE)
    Tema: str = str(Match_Tema.group(1).strip()) if Match_Tema else "No encontrado"
    Match_Fecha_Pub = re.search(r'Fecha de Publicación:\s*(.*?)(?=\n|$)', Texto, re.IGNORECASE)
    Fecha_pub: str = str(Match_Fecha_Pub.group(1)).strip() if Match_Fecha_Pub else "No encontrada"
    Match_Fecha_Mod = re.search(r'Fecha de Modificación:\s*(.*?)(?=\n|$)', Texto, re.IGNORECASE)
    fecha_mod: str = str(Match_Fecha_Mod.group(1)).strip() if Match_Fecha_Mod else "No encontrada"
    Match_Motivo= re.search(r'MOTIVO:\s*(.*?)(?=\n|$)', Texto, re.IGNORECASE)
    Motivo: str = str(Match_Motivo.group(1).strip()) if Match_Motivo else "No encontrado"
    return {
        "confianza": confianza,
        "Tema": Tema,
        "Fecha_pub": Fecha_pub,
        "Fecha_mod": fecha_mod,
        "Motivo": Motivo
    }

def Detectar_tema(url: str) -> str:
    prompt_principal= (
        "Solo escribre la confiabilidad y el motivo, ni una letra más"
        "Tema: [Escribe el tema principal del texto, si es posible, en un maximo de 15 palabras.]\n"
        "Fecha de Publicación: [Escribe la fecha de publicación original si existe, o 'No especificada']\n"
        "Fecha de Modificación: [Escribe la fecha de última modificación si existe, o 'No especificada']\n"
        "Confiabilidad: [Porcentaje de 0% a 100% basado en si es una fuente académica, oficial, periodística reconocida o blog sin citas.]\n"
        "MOTIVO: [Explicación de una frase de por qué tiene ese porcentaje]"
    )
    respuesta = exa.get_contents(
        urls=url,
        summary={
            "query": prompt_principal
        }
    )
    parse_ulr = urlparse(url)
    dominio = parse_ulr.netloc
    
    for resultado in respuesta.results:
        if hasattr(resultado, 'summary') and resultado.summary:
            datos_separados = Extraer_datos(resultado.summary)
            
            porcentaje = datos_separados['confianza']
            Tema = datos_separados['Tema']
            Fecha_pub = datos_separados['Fecha_pub']
            Fecha_mod = datos_separados['Fecha_mod']
            Motivo = datos_separados['Motivo']
    
    return [dominio,Tema,Fecha_pub,Fecha_mod,porcentaje,Motivo]

def Buscar_web_x_tema (Excluir: str, Tema: str, n: int):
    
    prompt_confiabilidad = (
        "Solo escribre la confiabilidad y el motivo, ni una letra más"
        "Fecha de Publicación: [Escribe la fecha de publicación original si existe, o 'No especificada']\n"
        "Fecha de Modificación: [Escribe la fecha de última modificación si existe, o 'No especificada']\n"
        "CONFIABILIDAD: [Porcentaje de 0% a 100% basado en si es una fuente académica, oficial, periodística reconocida o blog sin citas]\n"
        "MOTIVO: [Explicación de una frase de por qué tiene ese porcentaje]"
    )
    
    busqueda = exa.search(
        query= Tema,
        type="neural",
        exclude_domains=Excluir,
        num_results=n,
        contents={
            "summary":{
                "query": prompt_confiabilidad
            }
        }
    )
    
    dominios = []
    
    for i, resultado in enumerate(busqueda.results, 1):
        parsed_url = urlparse(resultado.url)
        
        print(f"Resultado {i}: {resultado.title}")
        print(f"Dominio: {parsed_url.netloc}")
        print(f"URL: {resultado.url}")
        if hasattr(resultado,'summary') and resultado.summary:
            print(resultado.summary)
        print ("-"*50)
        dominios.append(parsed_url.netloc)
    return dominios

# --- PRUEBA DEL CÓDIGO ---
Pagina = Detectar_tema(url_semilla)
Texto_pagina = ["Dominio: ","Tema: ", "Publicación: ", "Modificación: ","Confianza: ","MOTIVO: "]
parsed_url = urlparse(url_semilla)
dominios = [parsed_url.netloc]

print("--- TEMA DETECTADO POR EXA ---")
for i in range(len(Pagina)):
    print(f"{Texto_pagina[i]}{Pagina[i]}")
print("-"*100)
N_repeticiones = 10
Resultados = Buscar_web_x_tema(dominios, Pagina[1], N_repeticiones)
i = 0
while i < len(Resultados):
    dominios.append(Resultados[i])
    i += 1

print(f"Dominios detectados: {dominios}")
#buscar_paginas_similares_por_tema(tema,cantidad_resultados=3)

