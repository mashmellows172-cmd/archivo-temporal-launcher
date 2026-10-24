# Archivo Temporal Launcher — Diseño

## Intención

Crear un launcher personal para Fire TV Stick 4K de segunda generación, usado en una televisión de 65 pulgadas. Debe sustituir la experiencia visual recargada por una pantalla doméstica limpia, rápida y controlable enteramente con el mando.

La dirección visual replica, para uso privado, el lenguaje gráfico de la plantilla TVA compartida por el usuario: negro casi total, retícula técnica, marco naranja fino, franja superior, tipografía administrativa retro, microtexto de archivo y textura analógica. Miss Minutes actúa como guía visual animada, sin audio.

## Contexto de uso

- Dispositivo objetivo: Fire TV Stick 4K, segunda generación, modelo AFTKM.
- Sistema objetivo: Fire OS 8 / Android API 30.
- Pantalla principal: televisión 4K de 65 pulgadas.
- Entrada primaria: mando Fire TV mediante D-pad, Select, Back y Home.
- Distribución: repositorio GitHub privado y uso personal exclusivo.
- Distancia de lectura de diseño: aproximadamente 3 metros.

## Objetivos

- Mostrar una pantalla inicial minimalista, sin publicidad.
- Abrir aplicaciones instaladas con pocos movimientos del D-pad.
- Mantener cinco destinos principales visibles en una única fila.
- Conservar foco visible y recorrido predecible.
- Integrar a Miss Minutes como presencia animada constante y reactiva.
- Mantener animaciones fluidas sin degradar la apertura de aplicaciones.
- Dar acceso rápido a configuración y personalización básica.

## No objetivos

- Reproducir contenido multimedia dentro del launcher.
- Crear recomendaciones, perfiles, cuentas o servicios en la nube.
- Añadir anuncios, noticias, compras o telemetría.
- Incorporar voz, diálogo hablado o sincronización labial.
- Publicar la aplicación en Amazon Appstore.
- Distribuir públicamente recursos de Marvel, Disney, TVA o Miss Minutes.

## Antiobjetivos

El resultado fracasa si presenta cualquiera de estas condiciones:

- Información amontonada o más de una fila principal de contenido.
- Animaciones que interrumpen, cubren el foco o retrasan el control remoto.
- Texto pequeño o contraste insuficiente desde el sofá.
- Navegación circular confusa o foco perdido.
- Fondos fotográficos, glassmorphism, tarjetas modernas redondeadas o estética genérica de dashboard.
- Dependencia de red para cargar la interfaz o las animaciones.

## Enfoques considerados

### Archivo Temporal horizontal

Una franja superior, amplio espacio negro y cinco accesos alineados abajo. Es el enfoque más rápido y legible. Su primera versión tenía poca personalidad interactiva.

### Archivo Temporal con guía de foco — elegido

Mantiene la fila única, pero Miss Minutes camina sobre ella, mira el elemento enfocado y lo señala. Conserva el minimalismo y aporta la personalidad de la serie sin llenar el fondo.

### Sala de Control modular

Distribuye aplicaciones alrededor de un reloj central. Es más icónico, pero requiere más saltos del D-pad y complica la previsibilidad espacial.

## Composición elegida

### Encabezado

- Franja naranja horizontal en el tercio superior inmediato.
- Título `ARCHIVO TEMPORAL` alineado a la izquierda.
- Hora grande `HH:mm` alineada a la derecha.
- Marco técnico fino alrededor de la zona segura.
- Pequeñas marcas de registro y microtexto únicamente en bordes.

### Fondo

- Negro carbón mate.
- Retícula técnica de muy bajo contraste.
- Ruido de impresión y scanlines apenas visibles.
- Al menos 60 % de espacio negativo.
- Sin fotografías, paisajes, muebles ni maquinaria.

### Navegación principal

Una sola fila inferior con cinco destinos:

1. Continuar.
2. Películas.
3. TV.
4. Música.
5. Aplicaciones.

Los cinco espacios son configurables. Sus nombres iniciales describen el uso sugerido, pero cada espacio puede apuntar a una aplicación instalada elegida por el usuario.

Cada destino usa forma rectangular, icono monocromo y etiqueta grande. El elemento enfocado usa relleno naranja, doble marco crema y negro, y contraste mínimo de 4.5:1 para el texto. Los elementos sin foco permanecen negros con borde naranja fino.

### Barra auxiliar

Una columna estrecha de iconos técnicos puede aparecer en el borde izquierdo. Solo ofrece Inicio, Buscar, Biblioteca y Ajustes. Permanece visualmente secundaria y no añade una segunda jerarquía cuando la fila principal tiene foco.

## Miss Minutes

### Papel

Miss Minutes comunica el estado del launcher, refuerza el foco y confirma acciones. Nunca funciona como decoración desconectada del uso.

### Estados visuales

- `Idle`: permanece visible, respira, parpadea y cambia levemente de postura.
- `MoveToFocus`: camina por encima de la fila hacia la aplicación enfocada.
- `Point`: se detiene, mira el icono y lo señala.
- `Confirm`: celebra brevemente cuando se abre una aplicación.
- `SystemNotice`: cambia expresión y presenta una alerta local.
- `Sleep`: reduce movimiento tras inactividad prolongada, pero no desaparece.

### Reglas de movimiento

- Sin audio.
- Presencia continua mientras el launcher está visible.
- Transición de foco iniciada inmediatamente al recibir el evento del D-pad.
- Duración objetivo de desplazamiento: 220–360 ms según distancia.
- Animación de señalar: 160–220 ms.
- Parpadeos y cambios de postura espaciados y no sincronizados para evitar repetición mecánica.
- Solo se animan transformación, opacidad y recursos prerenderizados; no se recalcula el layout durante cada cuadro.
- Objetivo: 60 fps. Ninguna interacción debe quedar por debajo de 30 fps de forma sostenida.
- El foco cambia de inmediato aunque Miss Minutes continúe alcanzando su nueva posición.
- Nunca cruza por delante de etiquetas ni oculta la selección.

### Mensajes escritos

Los mensajes son opcionales, breves y contextuales. Máximo una línea. Ejemplos:

- `Selecciona para abrir` al primer uso.
- `Aplicación no disponible` cuando falla un intent.
- `Sin conexión` solo donde la red sea necesaria.

No debe repetir mensajes conocidos ni mostrar globos durante la navegación normal.

## Interacción con el mando

- Izquierda y derecha recorren la fila principal.
- Arriba mueve el foco a la barra auxiliar.
- Abajo devuelve el foco a la fila principal cuando corresponda.
- Select abre el destino enfocado.
- Back cierra paneles secundarios; en Inicio no abandona accidentalmente el launcher.
- Una pulsación siempre produce respuesta visual inmediata.
- `Continuar` abre la última aplicación iniciada desde Archivo Temporal. No intenta reanudar contenido interno de aplicaciones de terceros.
- El foco inicial es `Continuar` si existe una aplicación reciente; de lo contrario, `Aplicaciones`.

## Funcionalidad

### Inicio

- Carga lista local de favoritos.
- Consulta las aplicaciones instaladas sin servicios externos.
- Muestra hora y fecha del sistema.
- Conserva el último foco válido durante la sesión.

### Aplicaciones

- Rejilla secundaria con aplicaciones instaladas.
- Orden configurable localmente.
- Posibilidad de añadir o quitar favoritos.
- Regreso con Back sin perder la posición anterior.

### Buscar y Biblioteca

- Buscar filtra únicamente aplicaciones instaladas en el dispositivo.
- Biblioteca muestra y permite administrar los accesos favoritos locales.

### Ajustes

- Intensidad de textura: apagada, baja o media.
- Animación de Miss Minutes: completa o reducida.
- Mostrar u ocultar mensajes escritos.
- Orden de favoritos.
- Inicio automático experimental, separado del funcionamiento básico.

## Arquitectura propuesta

- Aplicación Android nativa en Kotlin.
- Jetpack Compose para la interfaz y navegación por foco.
- Capa de dominio pequeña para aplicaciones instaladas, favoritos y estado del launcher.
- Persistencia local con DataStore.
- Recursos y animaciones empaquetados; funcionamiento sin red.
- Máquina de estados independiente para Miss Minutes. El runtime de animación se elegirá mediante un benchmark corto entre Rive y sprites prerenderizados. Rive es preferido si mantiene 60 fps y consumo estable en AFTKM; sprites son el fallback seguro.
- Adaptación por densidad y zona segura, sin diseñar con píxeles absolutos de 4K.

## Rendimiento y ergonomía

- Tiempo objetivo hasta interfaz utilizable: menos de 1.5 segundos tras proceso caliente y menos de 3 segundos en arranque frío.
- Respuesta visual al D-pad: dentro del siguiente cuadro renderizado.
- Máximo cinco elementos en la fila inicial.
- Texto principal equivalente a 32 sp o mayor; etiquetas de tarjeta equivalentes a 24 sp o mayor.
- Márgenes seguros mínimos del 5 % por lado para overscan.
- Recursos gráficos comprimidos y cargados fuera del hilo principal.
- Sin reproducción de vídeo en el fondo.
- Sin bucles que despierten CPU cuando la pantalla no está visible.

## Launcher real frente a aplicación tipo launcher

La primera entrega será una aplicación de inicio completa que abre aplicaciones y puede iniciarse manualmente. Convertirla en el Home permanente depende de restricciones de Fire OS. Esa capacidad se validará aparte en el AFTKM mediante intents, configuración permitida y, si el usuario lo decide, ADB. El producto sigue siendo útil aunque Amazon conserve su Home predeterminado.

## Propiedad intelectual y privacidad

- El usuario confirmó uso personal exclusivo.
- El repositorio debe ser privado.
- Los recursos de Miss Minutes y TVA no se redistribuirán ni se incluirán en una publicación pública.
- Los recursos animados de Miss Minutes se producirán como parte del proyecto a partir de las referencias privadas aportadas por el usuario.
- No se recopilan datos ni se realizan llamadas de telemetría.

## Validación física

El plan asume acceso posterior al Fire TV AFTKM con ADB habilitado. Las pruebas automatizadas y de emulador preceden esa validación. Rendimiento, arranque y comportamiento experimental como Home solo se aceptan definitivamente tras probarlos en el dispositivo físico.

## Referencias visuales

- Plantilla principal: https://dstory.co/templates/loki-tva-miss-minutes-template
- Imagen seleccionada: https://framerusercontent.com/images/OcTvCIaEPiGT32ubFtKAMO8LbA.jpg?height=1080&width=1920
- Referencias de personaje aportadas por el usuario en esta conversación.
- Referencia de producción: https://www.cantinacreative.com/series/loki

## Decisiones delegadas al constructor

- Nombre final del paquete Android y estructura exacta de módulos.
- Elección entre Rive y sprites después del benchmark en el dispositivo.
- Librería concreta para iconos, siempre que conserve un trazo uniforme.
- Valores finales de easing dentro de las duraciones aprobadas.
- Herramientas internas para preparar los recursos de animación.

