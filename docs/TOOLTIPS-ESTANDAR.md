# Presentación estándar para Simply Swords

Paquete: `dist/EYRAX-Apotheosis-Tooltips-1.0.zip`.
Minecraft 1.21.1; comprobado contra Simply Tooltips 0.1.5 y Simply Swords 1.70.2.
Se puede usar con EYRAX Apothic Arsenal 0.2.0; no requiere otro JAR del addon.

## Instalación

1. Copia el ZIP, sin descomprimir, a la carpeta `resourcepacks` de tu instancia.
2. Actívalo en Opciones → Paquetes de recursos y colócalo arriba de los paquetes que personalicen Simply Tooltips.
3. Recarga los recursos con F3+T si fuera necesario.

Es un cambio de cliente. No necesitas instalarlo en el servidor ni quitar Simply Tooltips,
que sigue siendo dependencia de Simply Swords. Para revertirlo, desactiva el paquete.

## Qué cambia

Desactiva el panel personalizado de Simply Tooltips para los objetos `simplyswords:*`.
Se usa la presentación normal de Minecraft y de los mods que añaden información al
tooltip, incluido Apotheosis. No reproduce artificialmente un marco de rareza: los
estilos y textos de Apotheosis dependen de los datos reales del objeto y de su configuración.

La regla contiene únicamente:

```json
{"namespaces":{"simplyswords":{"enabled":false}}}
```

No modifica habilidades, estadísticas, afijos, gemas, sockets ni guardados. Tampoco
borra líneas de información: el renderer normal recibe el tooltip que producen los mods.
Los elementos visuales exclusivos del panel moderno, como pestañas, marcos e iconos,
dejan de dibujarse. Las descripciones extensas pueden ocupar más pantalla en modo normal.

Los sockets de Simply Swords y Apotheosis siguen siendo independientes. Este paquete
no cambia qué gemas puede aceptar un arma.

## Compatibilidad y límites

La opción por namespace existe en el JAR 0.1.5 inspeccionado. El renderer comprueba
`ItemThemeRegistry.isEnabledForStack` antes de sustituir el tooltip normal. La configuración
global `enableTooltipRendering` se mantiene sin cambios para no desactivar otros mods.

Una regla personalizada por ID con `enabled: true` tiene prioridad sobre la exclusión
por namespace. Si un objeto conserva su panel moderno, revisar las reglas específicas
de otros paquetes o de `config/simplytooltips`.

Referencia del proveedor: https://github.com/Sweenus/simplytooltips#2-item--tag-mappings

La prueba de cliente se ejecutó con `gradlew.bat runClientTooltipProbe` en una instancia
de desarrollo aislada, con el paquete activado. Resultado: PASS para los 140 objetos
registrados de Simply Swords; el diamante vanilla conserva su elegibilidad original.
La captura `eyrax-standard-tooltip.png` muestra el estoque de hierro con su habilidad
implícita y estadísticas en la presentación normal. Resultado: `tooltip-probe-result.txt`.
La vista previa no carga un mundo ni un arma con afijos; queda pendiente comprobar
la apariencia de esos objetos en el modpack del usuario. La primera ejecución se cerró
antes de validar; la segunda completó la comprobación y guardó la captura.
