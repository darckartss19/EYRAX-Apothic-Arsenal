# Fase 2 — Entrega 0.2.0: pruebas de integración y diagnóstico de sockets

La versión 0.2.0 añade diagnóstico de inserción y políticas configurables por tier.
La integración se ejecutó dentro de Minecraft con NeoForge, no solo mediante compilación.
El problema concreto de gemas rechazadas del usuario no se reprodujo en el entorno base;
no se cambió la aceptación de gemas para ocultarlo ni se declara corregido en su pack.

## Cambios

- `/eyrax_arsenal diagnose`: inspecciona arma en mano principal y gema en secundaria,
  muestra sockets reales de Apotheosis, categoría, familia, tier y motivo del rechazo.
  Si la combinación es compatible, muestra la receta seleccionada por el servidor.
- `ArsenalConfig`: archivo de servidor con límites de nuevas inserciones separados para
  Standard, Runic y Unique. Todos empiezan en -1 (reglas nativas sin límite adicional).
- `SocketingPolicy`: usa `CanSocketGemEvent`; nunca reescribe componentes, reduce sockets,
  elimina gemas existentes ni habilita una gema incompatible.
- Tag `weapons/socketing_blocked`: bloqueo por datapack, vacío por defecto.
- `src/gameTest`: pruebas separadas del código distribuido. No incluye clases ni
  estructuras de pruebas en el JAR instalable.

Se mantienen los grupos EYRAX internos y `apotheosis:melee_weapon` como categoría real.
No se añaden afijos exclusivos, generación natural de loot ni balance definitivo de combate.
La política controla inserciones, no el número de sockets generados por rareza.

## Ejecución y resultados

Comando ejecutado con Java 21 y caché dentro del proyecto:

```powershell
.\gradlew.bat build runGameTestServer --console=plain
```

Resultado final: **BUILD SUCCESSFUL in 34s**. Seis pruebas unitarias aprobadas y
**All 4 required tests passed** en Minecraft. El servidor de pruebas se cerró normalmente.
La última ejecución terminó el 19 de septiembre de 2026; el empaquetado se retomó después
de una interrupción, conservando esa evidencia sin presentarla como una ejecución nueva.

| Prueba de integración | Evidencia |
|---|---|
| Categorías | 133 armas reconocidas, 133 con categoría melee |
| Inserción | 13.566 combinaciones compatibles arma/gema/pureza |
| Receta real | Consulta a RecipeManager y ensamblado de la receta seleccionada |
| Reforja | 4 armas × 5 rarezas = 20 salidas con afijos |
| Reciclaje | Las 20 salidas tienen receta de salvaging y productos definidos |
| Coexistencia | Implícito, Gem Power con poderes registrados y flag de sockets de Simply Swords preservados |
| Persistencia | 4 round trips del codec ItemStack/NBT con ambos sistemas presentes |
| Diagnóstico | Socket de Simply Swords sin socket Apotheosis, gema inválida, socket lleno y combinación válida |
| Política | Límite Runic 0 bloquea la inserción real por evento sin invalidar la gema ya insertada |

Armas utilizadas para reforja/persistencia: `iron_rapier`, `runic_rapier`,
`bramblethorn` y `netherite_greathammer`. Las pruebas construyen fixtures con los
componentes reales de Simply Swords, usando sus poderes registrados `freeze` y `berserk`.
No representan una sesión de combate ni una obtención natural de esos poderes.

La matriz de inserción prueba gemas que declaran un bono compatible; no fuerza la
aceptación de gemas para armaduras u otras categorías. La pureza se normaliza mediante
la API del proveedor. Comprueba que el resultado contiene una gema válida y que no se
modifica el objeto original. La prueba de reciclaje verifica selección de receta y
productos definidos; no simula clics en la interfaz ni el sorteo de cantidades de materiales.

## Entorno

Minecraft 1.21.1, NeoForge 21.1.248, Apotheosis 8.8.0, Apothic Attributes 2.10.0,
Placebo 9.9.2, Simply Swords 1.70.2-1.21.1, Architectury 13.0.11,
Fzzy Config 0.7.6, Kotlin for Forge 5.12.0, Simply Tooltips 0.1.5,
Patchouli 1.21-87 y Curios 9.5.1 (resuelto transitivamente).
Java Oracle 21.0.12. Servidor GameTest aislado en `work/gametest`.
No se modificó ni arrancó la instalación Minecraft del usuario.

## Avisos observados

- Simply Swords intenta cargar dos recetas para contenido opcional ausente:
  `mythicmetals_compat/adamantite/adamantite_twinblade` y `eldritch_end/dreadtide`.
  Minecraft registra errores de lectura de esas recetas y continúa. Se dejan documentados;
  el addon no modifica las recetas ajenas para silenciar los mensajes.
- Hay avisos de mixins opcionales de Better Combat/Lootr/Quark y de refmaps de desarrollo.
- `decaying_relic` sigue reconocido como Unique con familia UNKNOWN, como estaba previsto.
- La primera ejecución creó `server.properties`; su ausencia inicial produjo un mensaje
  de Minecraft. No bloqueó las pruebas.
- Gradle informa de funcionalidades obsoletas para Gradle 10. El Wrapper sigue fijado a 9.4.1.

## Qué falta para el rechazo del usuario

Hace falta el resultado de `/eyrax_arsenal diagnose` con los objetos concretos o una
reproducción en el pack del usuario. Que un arma tenga un socket visible no identifica
por sí solo el sistema propietario del socket. El diagnóstico diferencia esa situación
de un bono incompatible, duplicado de gema única, política o bloqueo de otro mod.

No se probó cliente gráfico, interacción manual con mesas, conexión cliente/servidor,
combat effects, animaciones, Epic Fight ni el modpack completo. Tampoco se validó `/reload`
con cambios de un datapack externo; el mecanismo de actualización continúa usando el evento
de data maps de NeoForge.

## Entrega

- JAR: `build/libs/EYRAX-Apothic-Arsenal-0.2.0.jar`.
- Fuentes reproducibles y pruebas: `dist/EYRAX-Apothic-Arsenal-0.2.0-source.zip`.
- Log completo final: `docs/build-result.txt`.
- Inspección del JAR, ruta absoluta y SHA-256: `docs/jar-inspection.json`.

Sustituir la versión anterior, sin instalar ambas a la vez, en cliente y servidor.
Los valores por defecto no introducen restricciones adicionales de gemas.
