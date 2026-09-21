# Informe de entrega — Fase 1

## Entorno y alcance

Trabajo independiente dentro de `outputs/EYRAX-Apothic-Arsenal`, sin cambios en la
instalación Minecraft, repositorios originales ni JAR de los proveedores.
JDK Oracle 21.0.12, Gradle 9.4.1, ModDevGradle 2.0.141, NeoForge 21.1.248.
Se consultó GitHub y se obtuvo una copia de referencia de EYRAX Apothic Animals.
No se publicó un repositorio ni se creó un PR: esta entrega es local.

Referencia de arquitectura: [EYRAX Apothic Animals, commit 1e1bdc0](https://github.com/darckartss19/EYRAX-Apothic-Animals/tree/1e1bdc0f61eee11b5bf5cb1e45ecdaff0ee558ec).
Se reutilizó la estructura de Gradle/Wrapper como referencia, no la lógica de armaduras.

## Investigación y APIs

Fuente de Apotheosis 8.8.0: [commit e825cd9](https://github.com/Shadows-of-Fire/Apotheosis/tree/e825cd9dcb9a6fff5e163659812ff32390e343a6),
contrastada con el JAR 1.21.1-8.8.0 descargado de su Maven oficial.
Simply Swords: [release NeoForge exacta](https://modrinth.com/mod/simply-swords/version/nS0Yahr5).
La rama pública consultada ya tenía cambios posteriores; las firmas, tags, metadata y
asignaciones factuales se verificaron en el binario 1.70.2 mediante lectura ZIP y `javap`.
Su SHA-512 queda registrado en `weapon-inventory.json` y en el generador.

APIs usadas en producción:

- NeoForge `@Mod`, `ModList`, `NeoForge.EVENT_BUS`, `ServerStartedEvent`, `DataMapsUpdatedEvent` y `ItemAbilities.SWORD_DIG`.
- Minecraft `BuiltInRegistries.ITEM`, `TagKey<Item>`, `ResourceLocation`, `ItemStack.is`.
- Apotheosis `LootCategory.forItem`, `LootCategory.isNone`, `Apoth.LootCategories.MELEE_WEAPON`.
- Data map sincronizado `apotheosis:loot_category_overrides` sobre el registro de items.
- Tags Simply Swords `swords`, `uniques`, `runic_gear`, `implicit/<tipo>`.

APIs investigadas pero no invocadas por el addon:

- `LootCategory` es final y se registra en `Apoth.BuiltInRegs.LOOT_CATEGORY`. Su constructor recibe
  predicado, `EntitySlotGroup` de Apothic Attributes y prioridad. `forItem` consulta primero el
  data map y luego los predicados ordenados. No existe herencia de una categoría melee personalizada.
- En 8.8.0 no se utilizan las antiguas clases sugeridas `AffixItem`/`AffixLootManager`:
  el flujo real utiliza `ItemAffixes`, `AffixHelper`, `LootController`, `AffixLootRegistry`,
  `AffixLootEntry`, `RarityRegistry`, `LootRarity` y `RarityOverrideRegistry`.
- `LootController` determina los afijos aplicables por categoría. `LootRarity.getRules` permite
  overrides por categoría. No se agregan entradas a los pools aleatorios en esta fase.
- Reforging acepta categorías distintas de NONE y genera la salida desde `input.copy()`.
  Salvaging consulta ingredientes/recetas de objetos con afijos y rareza, no simplemente una familia EYRAX.
- `SocketHelper` utiliza componentes propios `apotheosis:sockets` y `apotheosis:socketed_gems`.
  La selección de bonos de gemas usa las categorías de `GemClass`; existen `ExtraGemBonusRegistry`
  y eventos de sockets para extensiones futuras.
- Apothic Attributes 2.10.0 expone grupos de slots y eventos de modificadores. El addon no aplica
  modificadores de atributos ni depende directamente de clases de armas.
- `SimplySwordsAPI` permite registrar tipos e implícitos. Su operación pública
  `getOrCreateWeaponImplicit` puede escribir un componente y depende de la configuración.
  `WeaponImplicitRegistry` mantiene el resolvedor de tipo como método privado.
  No se llama al generador de implícitos para clasificar, ni se usa reflexión sobre ese resolvedor.
- Los sistemas Gem Power / Runefused / Netherfused de Simply Swords quedan intactos:
  el addon no escribe ningún componente ni llama a su lógica de socketing.

## Decisiones y workarounds

1. **Grupos internos y categoría melee nativa.** Crear cuatro LootCategory activas sin
   los datasets correspondientes perdería afijos/bonos de gemas. La infraestructura de
   clasificación queda preparada; las categorías registradas independientes se posponen.
2. **Tipos desde tags y datos generados.** Los tags de implícitos del proveedor están vacíos
   para muchas armas base. Se genera el inventario por sufijo para tipos convencionales y
   desde las asignaciones factuales del registro para nombres especiales. No se copian
   implementaciones, recursos gráficos ni cientos de IDs mantenidos manualmente.
3. **Unique antes que Runic.** `runic_weapons` contiene `watching_warglaive`, que es Unique.
   Se usa `runic_gear` y se mantiene prioridad explícita de Unique.
4. **No confundir tipo con tier.** `sword_on_a_stick` está en los overrides de tipo pero
   es un arma Standard de madera: se clasifica por tags, no por el nombre del método de overrides.
5. **Casos sin inventar datos.** Longsword conserva grupo UNASSIGNED. `decaying_relic`
   conserva tier UNIQUE y familia UNKNOWN. `dreadtide` es opcional y no está registrado en el JAR.
6. **Futuras armas.** Tags recargables y fallback conservador de sufijo; capacidad vanilla
   para detectar armas aún no etiquetadas. Un nuevo tipo queda UNKNOWN y se registra sin crash.
7. **Dependencias exactas.** Se examinaron TOML reales, incluyendo Fzzy Config → Kotlin for Forge.
   Simply Tooltips se resuelve para desarrollo y para cliente. Enchanting/Spawners no se fuerzan.
8. **Rutas Windows.** Git notificó rutas largas en algunos recursos de las copias de investigación;
   no se emplearon esos recursos. La verificación de versión de Simply Swords se basó en su JAR,
   evitando depender del checkout de una rama cambiante.

## Cobertura

17 familias conocidas. Catálogo de 134 IDs; 133 aparecen en el registro base del binario,
con 132 familias resueltas y un caso UNKNOWN (`decaying_relic`).
Recuento esperado sin mods de materiales: Swift 43, Heavy 36, Polearm 25, Special 15,
Longsword 13 y familia desconocida 1. Tiers: Standard 61, Runic 15, Unique 57.
`dreadtide`, si otro contenido lo registra, suma uno a Swift y Unique.
No se ejecutó Minecraft para certificar un total observado; los logs lo verificarán en el pack real.

## Verificación

Se ejecuta `gradlew.bat build`, equivalente Windows a `./gradlew build`, con Java 21.
Las pruebas comprueban precedencia de tiers, nombres ambiguos, tipos futuros desconocidos,
longsword sin grupo, cobertura del catálogo, duplicados de familia, referencias entre tags
y data map a la categoría nativa sin reemplazar datos ajenos.

La evidencia final de compilación queda en `docs/build-result.txt`; la inspección del
JAR, sus metadatos y hash en `docs/jar-inspection.json`.
Gradle presenta una advertencia de características obsoletas para Gradle 10; se mantiene
el Wrapper fijado a 9.4.1. No implica compatibilidad probada con Gradle 10.

No se arrancó un cliente ni un servidor Minecraft. Quedan pendientes las pruebas de
reforja, reciclaje, gems, implícitos, persistencia de componentes, multiplayer y `/reload`
descritas en README. No se ha iniciado Fase 2.

## Archivos de entrega

- `build.gradle`, `settings.gradle`, `gradle.properties`, Gradle Wrapper y `.gitignore`.
- 10 clases Java distribuidas en entrada, compatibilidad, taxonomía y registro.
- `META-INF/neoforge.mods.toml`, `pack.mcmeta`, 26 tags y un data map.
- Dos clases de pruebas, README, informe, inventario y generador de datos.
- JAR instalable `build/libs/EYRAX-Apothic-Arsenal-0.1.0.jar` y JAR de fuentes.
- ZIP de fuentes reproducibles en `dist/`, sin caches ni JAR de terceros.

La ruta absoluta del JAR se registra en `docs/jar-inspection.json`.
