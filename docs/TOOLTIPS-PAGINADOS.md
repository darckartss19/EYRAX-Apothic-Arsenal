# Tooltip normal con páginas — addon 0.3.0

Sustituye `EYRAX-Apothic-Arsenal-0.2.0.jar` por el JAR 0.3.0 en mods. No dejes ambas versiones instaladas. Desactiva los paquetes de tooltip 1.0 y 1.1: la regla de presentación normal ya está incluida en el addon. Mantén Simply Tooltips y las demás dependencias instaladas.

Todas las armas de Simply Swords conservan el tooltip normal. Cuando el contenido supera el alto disponible (como máximo 180 píxeles de cuerpo), se reparte en páginas. Mantén el cursor sobre el objeto y pulsa AvPág para avanzar o RePág para volver. El pie indica la página actual. Los tooltips cortos no cambian. El nombre se repite en cada página; las líneas restantes conservan su orden, estilos y contenido. La navegación vuelve a la primera página al cambiar de objeto o de pantalla.

Se usan eventos de cliente de NeoForge, sin mixins ni dependencias en las clases internas del renderer de Simply Tooltips. Los datos y mecánicas de armas, gemas y afijos no cambian. La paginación solo afecta a objetos del namespace simplyswords. Las teclas solo se consumen mientras se muestra un tooltip paginado; no se interceptan dentro de campos de texto.

Se conserva la información que los mods entregan al tooltip normal, incluidas las líneas adicionales que dependan de Alt, Ctrl u otros modificadores. No se inventa información que otro mod oculte. Los componentes gráficos de otros mods se mantienen completos: un componente individual más alto que la pantalla no puede dividirse automáticamente.

Otros paquetes que habiliten explícitamente el panel moderno pueden anular la presentación normal; especialmente el perfil 1.1 anterior. Este cambio no usa las pestañas del panel moderno: añade páginas al renderer normal.

## Validación

Pruebas unitarias de partición: conservación y orden de todas las entradas, límite de altura y componentes grandes indivisibles.
Las pruebas unitarias de compilación y partición pasan. La vista previa automática del cliente se detuvo porque Apotheosis requiere un jugador en un mundo para construir ciertos tooltips; por eso la comprobación final de AvPág/RePág debe hacerse dentro de una partida real. La apariencia con los afijos y mods concretos del modpack debe comprobarse en esa instancia.
