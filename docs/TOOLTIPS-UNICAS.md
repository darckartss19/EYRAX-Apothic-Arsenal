# Pestañas para armas únicas — perfil 1.1

Paquete: `dist/EYRAX-Apotheosis-Tooltips-1.1.zip`.

Sustituye el perfil 1.0 en la lista de paquetes activos: desactiva el anterior y activa el 1.1 arriba de otros paquetes que personalicen Simply Tooltips. No requiere cambiar el JAR 0.2.0 del addon.

Las armas únicas recuperan el panel completo de Simply Tooltips, incluidas sus pestañas. Las armas estándar y rúnicas mantienen el tooltip normal. El perfil usa excepciones por ID tomadas del tag `simplyswords:uniques` del JAR 1.70.2; incluye 58 IDs, de los que 57 están registrados en la instancia de prueba (dreadtide es opcional).

Simply Tooltips debe permanecer instalado y sus opciones `enableTooltipRendering` y `tooltipTabs` activadas. Este paquete no modifica esas opciones globales. No altera sockets, gemas, habilidades ni afijos. Recupera todo el panel original en las únicas, no solo el dibujo de las pestañas.

La prueba de cliente valida la selección del renderer en los 140 objetos registrados y genera una vista previa de Shadowsting sin mundo cargado. La apariencia con los afijos concretos del modpack del usuario requiere comprobarse en su partida.

Resultado: PASS (57 únicas con panel moderno y 83 objetos con presentación normal). Captura revisada: eyrax-unique-tooltip.png; Shadowsting muestra la descripción sin texto superpuesto y el indicador de páginas con la tecla G. Esta es la tecla de la instancia de prueba y puede variar según la configuración del usuario.
