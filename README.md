# EYRAX Apothic Arsenal — Fase 2: compatibilidad y diagnóstico

Addon independiente para Minecraft 1.21.1, NeoForge 21.1.248 y Java 21.
Mod ID: `eyrax_apothic_arsenal`. Versión: `0.2.0`.

Reconoce armas de Simply Swords, clasifica familia/grupo/tier y las vincula a
`apotheosis:melee_weapon` mediante el data map oficial de Apotheosis.
Los cuatro grupos EYRAX son **categorías internas**, no nuevas entradas del registro
LootCategory: así se conservan los afijos y bonos de gemas nativos durante el MVP.

No introduce afijos, worldgen, loot aleatorio, cambios de attack speed ni mixins.
No modifica componentes, implícitos, sockets ni archivos de los otros mods.
Runic y Unique tienen políticas de inserción configurables, sin restricciones adicionales
por defecto. Esta entrega no establece un balance definitivo ni introduce afijos propios.

## Resultado de las pruebas reales

`build runGameTestServer`: **BUILD SUCCESSFUL**, 6 pruebas unitarias y 4 GameTests aprobados.
Servidor NeoForge 21.1.248 con los mods reales, aislado dentro de `work/gametest`.

- 133 armas reconocidas, todas con categoría melee válida.
- 13.566 combinaciones compatibles de arma/gema/pureza insertadas mediante la receta
  de herrería, comprobando también la receta elegida por el servidor.
- 20 combinaciones de arma/rareza con afijos y receta de reciclaje válida.
- Cuatro armas de muestra conservaron sus componentes de implícitos, Gem Power y
  sockets adicionales de Simply Swords tras insertar gemas, reforjar y serializar/deserializar.
- Diagnóstico de sockets independientes, gema inválida, socket lleno y bloqueo configurable;
  activar el bloqueo no elimina ni invalida una gema ya insertada.

El fallo de inserción comunicado por el usuario **no se reprodujo** en este entorno.
No se afirma que esté resuelto en su modpack. No se probó la interfaz gráfica del cliente,
combate, animaciones, el pack completo del usuario ni conexión de un cliente remoto.

## Diagnosticar una gema rechazada

Instala el JAR 0.2.0 sustituyendo la versión anterior, en cliente y servidor.
Sostén el arma en la mano principal y la gema de Apotheosis en la secundaria y ejecuta:

```text
/eyrax_arsenal diagnose
```

El comando muestra categoría real, familia, tier, sockets de Apotheosis y motivo del rechazo.
Cuando la combinación es válida, también muestra la receta de herrería seleccionada por el servidor.
No requiere permisos de operador y solo inspecciona los objetos del jugador.

Un socket Runefused/Netherfused de Simply Swords no cuenta como socket de Apotheosis.
La inserción de Apotheosis usa la **mesa de herrería**, con la plantilla vacía, el arma
en el hueco base y la gema en el hueco de adición. El texto «Armas ligeras» en sus gemas
incluye la categoría melee; no representa exclusivamente el grupo interno Swift.

## Configuración de servidor

NeoForge genera `eyrax_apothic_arsenal-server.toml` en el directorio `serverconfig` del mundo:

```toml
[socketing]
standardMaxInsertedGems = -1
runicMaxInsertedGems = -1
uniqueMaxInsertedGems = -1
```

`-1` conserva las reglas de Apotheosis; `0` bloquea nuevas inserciones; `1..16` limita
el número de gemas válidas insertadas. No agrega sockets, no cambia sus componentes,
no retira gemas existentes y no afecta los sockets de Simply Swords.
Un tier desconocido conserva las reglas de Apotheosis.

El tag opcional `eyrax_apothic_arsenal:weapons/socketing_blocked`, inicialmente vacío,
permite bloquear nuevas inserciones en armas reconocidas mediante datapacks.

## Compilar

PowerShell, desde esta carpeta:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
$env:GRADLE_USER_HOME = Join-Path (Get-Location) 'work\gradle-home'
.\gradlew.bat build
.\gradlew.bat runGameTestServer
```

Linux/macOS con JDK 21:

```sh
GRADLE_USER_HOME="$PWD/work/gradle-home" ./gradlew build
GRADLE_USER_HOME="$PWD/work/gradle-home" ./gradlew runGameTestServer
```

Gradle Wrapper 9.4.1 y ModDevGradle 2.0.141. No requiere compilar los otros mods ni
descargar JAR manualmente. Los tags ya están incluidos; Python/javap solo se necesitan
para regenerar el inventario de desarrollo.

Resultado: `build/libs/EYRAX-Apothic-Arsenal-0.2.0.jar`.
El archivo `-sources.jar` contiene fuentes, no es el mod instalable.

## Dependencias verificadas

| Mod | Versión fijada para desarrollo | Motivo |
|---|---|---|
| NeoForge | 21.1.248 | Loader |
| Apotheosis | 1.21.1-8.8.0 | Afijos, rarezas, gemas, reforja |
| Apothic Attributes | 1.21.1-2.10.0 | Dependencia requerida de Apotheosis |
| Placebo | 1.21.1-9.9.2 | Dependencia requerida de Apotheosis/Attributes |
| Simply Swords | NeoForge 1.70.2-1.21.1 | Armas; versión Modrinth `nS0Yahr5` |
| Architectury | NeoForge 13.0.11 | Requerido por Simply Swords |
| Fzzy Config | 0.7.6+1.21+neoforge | Requerido por Simply Swords |
| Kotlin for Forge | 5.12.0 | Requerido por Fzzy Config; incluye sus bibliotecas |
| Simply Tooltips | 0.1.5 | Requerido por Simply Swords en cliente |
| Patchouli | 1.21-87-NEOFORGE | Incluido en desarrollo para el libro; no figura como dependencia obligatoria en el TOML de Apotheosis 8.8.0 |

Apothic Enchanting, Apothic Spawners, Curios y Gateways son **opcionales** según el
TOML del JAR de Apotheosis 8.8.0. No se exige instalar el conjunto completo de Apothic.
No se incluyen JAR de terceros dentro del addon ni del ZIP de fuentes.

Se exige Apotheosis 8.8.x y Simply Swords 1.70.2 hasta antes de 1.71; versiones
posteriores necesitan revisar las APIs. NeoForge admite versiones posteriores a
21.1.248 dentro de la rama 21.1. Ambos mods principales son requeridos: si falta uno,
NeoForge informa la dependencia faltante antes de instanciar el addon.

## Cobertura

El inventario de tags contiene 134 IDs base y 17 familias. El registro del JAR contiene
referencias a 133 de ellos: `dreadtide` está solamente como referencia opcional.
La columna base coincide con el total observado en el servidor de GameTests:

| Grupo | Registro base esperado | Incluyendo referencia opcional |
|---|---:|---:|
| Swift | 43 | 44 |
| Heavy | 36 | 36 |
| Polearm | 25 | 25 |
| Special | 15 | 15 |
| Sin grupo | 14 | 14 |
| Total | 133 | 134 |

Tiers esperados: 61 Standard, 15 Runic, 57 Unique; el catálogo cuenta 58 Unique al
incluir `dreadtide`. Los 14 sin grupo son 13 longswords y `decaying_relic`.
Longsword conserva su familia pero no se asigna un grupo que el brief no definió.
`decaying_relic` sí se reconoce como Unique, pero no tiene tipo en el registro de
implícitos revisado: se informa como UNKNOWN, sin inventar su familia.

Las armas opcionales de compatibilidad de materiales se reconocen por tags y se
clasifican por sufijo cuando procede; no se cuentan en esta base. Los logs de servidor
son la autoridad para el pack instalado. Un arma futura sin tags pero con la capacidad
vanilla `SWORD_DIG` y namespace `simplyswords` también entra en el diagnóstico.
Un tier futuro sin tag queda UNKNOWN; no se presume que toda arma nueva sea Standard.

## Arquitectura

- `EyraxApothicArsenal`: detección de mods y diagnóstico al iniciar servidor y recargar data maps.
- `compat/SimplySwordsCompat`: reconocimiento por tags y capacidad pública NeoForge.
- `weapon/WeaponClassifier`: lectura sin mutación; devuelve `WeaponProfile`.
- `weapon/WeaponFamily`, `WeaponGroup`, `WeaponTier`: taxonomía separada del balance.
- `registry/EyraxTags`: tags de familias, grupos, tiers y elegibilidad.
- `registry/EyraxLootCategories`, `compat/ApotheosisCompat`: puente y comprobación de la categoría nativa.
- `data/eyrax_apothic_arsenal/tags/item`: 17 tags de familia y 10 de agrupación/integración/política.
- `data/apotheosis/data_maps/item/loot_category_overrides.json`: conexión oficial con Apotheosis.
- `tools/generate_tags.py`: genera datos factuales del JAR exacto, comprobando SHA-512.
- `docs/weapon-inventory.json`: inventario completo por ID.
- `src/test`: seis pruebas de clasificación y consistencia del conjunto de datos.
- `src/gameTest`: cuatro pruebas reales en servidor; estas clases no se distribuyen en el JAR.
- `command/ArsenalCommands`, `compat/SocketingDiagnosis`: diagnóstico del arma y gema del jugador.
- `config/ArsenalConfig`, `compat/SocketingPolicy`: política de nuevas inserciones por tier.

Los logs de diagnóstico requieren tags/data maps cargados: aparecen al abrir un mundo
o iniciar un servidor, y tras `/reload`; no en el menú principal del cliente.
El arranque puede imprimir el diagnóstico en el evento del data map y de nuevo al
confirmarse el inicio del servidor. No se mantiene caché de tags que pueda quedar obsoleta.

## Personalización por datapack

Todos los tags propios usan `replace: false`. Para reasignar una familia existente,
el datapack debe quitarla de su tag anterior mediante un reemplazo de ese tag y
añadirla al nuevo; pertenecer a dos familias/grupos produce diagnóstico de conflicto.
No se modifica el tag `simplyswords:implicit/*` del proveedor.

Los tags `families/*` incluyen referencias opcionales a `simplyswords:implicit/*` y
el inventario generado del JAR. Esto permite añadir armas por datapack sin recompilar.
Las referencias externas son opcionales para que un ID ausente no invalide el tag.

`weapons/integrated` controla el conjunto con override explícito a melee. Retirar un
arma de ese tag **no la bloquea**: la detección nativa de Apotheosis puede reconocerla.
Una restricción global de afijos requeriría una política adicional o un override a
`apotheosis:none`. La nueva configuración de servidor y el tag `socketing_blocked`
controlan únicamente inserciones de gemas, sin bloquear reforja ni afijos.

## Verificación pendiente en el pack del usuario

En una instancia de pruebas separada, con el addon en cliente y servidor:

1. Iniciar cliente y servidor dedicado con las versiones indicadas; comprobar logs y ausencia de errores de data maps.
2. Confirmar el total real, tiers, las 17 familias y el aviso de `decaying_relic`; `dreadtide` ausente no debe fallar.
3. Reforjar una Standard de cada grupo y una longsword; revisar rareza, afijos y tooltips.
4. Probar sockets y gemas de Apotheosis en copias de esas armas.
5. En copias Runic/Unique, comprobar antes/después de reforjar y guardar/cargar que se conservan
   el implícito y los componentes Runefused/Netherfused/Gem Power. Las dos clases de gemas no deben cruzar sockets.
6. Reciclar un arma con afijos y verificar los materiales correspondientes a su rareza;
   no asumir que un arma sin afijos es reciclable.
7. Probar `/reload` con un datapack que añada/reclasifique un arma; confirmar que logs y categorías se actualizan.
8. Verificar que las habilidades y animaciones originales siguen funcionando. No hay integración Epic Fight en esta entrega.

Las pruebas automáticas del servidor no sustituyen las comprobaciones visuales y de
interacción con el pack completo. La generación natural de loot, afijos exclusivos,
balance definitivo por familia y Epic Fight quedan fuera de esta entrega de Fase 2.

Consulta `docs/INFORME-FASE-2.md` para resultados, límites de validación y evidencias.
`docs/INFORME-FASE-1.md` se conserva como informe histórico.


## Actualización 0.3.0: tooltip normal paginado

Las armas de Simply Swords conservan el aspecto normal con AvPág/RePág cuando el texto es largo. Desactiva los perfiles de recursos anteriores. Instrucciones y límites: [TOOLTIPS-PAGINADOS](docs/TOOLTIPS-PAGINADOS.md). Los informes de fase 1 y 2 documentan sus versiones anteriores.

