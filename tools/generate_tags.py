"""Regenerate factual item/type tags from the unmodified 1.70.2 JAR.

Usage: python tools/generate_tags.py path/to/simplyswords.jar
Requires Java 21 javap on PATH. No upstream implementation or assets are copied.
"""
import collections
import hashlib
import json
import pathlib
import re
import subprocess
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
MOD = 'eyrax_apothic_arsenal'
FAMILIES = 'rapier cutlass sai dagger claymore longsword greathammer hammer katana spear glaive halberd warglaive chakram scythe greataxe twinblade'.split()
GROUPS = {
    'swift': 'rapier sai dagger katana twinblade warglaive cutlass'.split(),
    'heavy': 'claymore greataxe greathammer hammer'.split(),
    'polearm': 'spear glaive halberd'.split(),
    'special': 'chakram scythe'.split(),
}

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')

def optional(value):
    return {'id': value, 'required': False}

def generate(jar):
    digest = hashlib.sha512(jar.read_bytes()).hexdigest()
    if digest != '9a231c0068318e7f62e3d5dfd81660b3f8efdcae9bcf83cfc3637c897fed29aac1941ef8007e8b29f0badfdce0c8047cfb73cd1ea486f25c69197384adfc8dcd':
        raise ValueError('Expected the verified NeoForge 1.70.2-1.21.1 release; review changes before updating the generator.')
    with zipfile.ZipFile(jar) as archive:
        def tag(name):
            values = json.loads(archive.read(f'data/simplyswords/tags/item/{name}.json'))['values']
            ids = [v if isinstance(v, str) else v['id'] for v in values]
            # Optional material compatibility packs are detected at runtime through the broad tag.
            return {v for v in ids if not v.startswith('#') and '/' not in v}
        swords, uniques, runic = tag('swords'), tag('uniques'), tag('runic_gear')
        # Include upstream unique and runic tags even if the broad swords tag omits an item.
        weapons = swords | uniques | runic
    bytecode = subprocess.check_output(['javap', '-p', '-c', '-classpath', str(jar),
        'net.sweenus.simplyswords.api.WeaponImplicitRegistry'], text=True)
    block = bytecode.split('private static void registerUniqueOverrides();')[1].split('private static void registerPath(')[0]
    overrides = {'simplyswords:' + item: family.lower() for item, family in
                 re.findall(r'// String (\w+)\s+\d+: getstatic\s+#\d+\s+// Field (\w+):', block)}
    if len(overrides) != 58:
        raise ValueError(f'Unexpected unique type mapping count: {len(overrides)}')
    # Type overrides are not tier declarations: sword_on_a_stick is a wooden standard spear.
    registry_bytecode = subprocess.check_output(['javap', '-p', '-c', '-classpath', str(jar),
        'net.sweenus.simplyswords.registry.ItemsRegistry'], text=True)
    registry_ids = {'simplyswords:' + value for value in re.findall(r'// String ([a-z0-9_]+)\s*\n', registry_bytecode)}
    rows = []
    for item in sorted(weapons):
        path = item.split(':', 1)[1]
        family = overrides.get(item) or next((f for f in FAMILIES if path == f or path.endswith('_' + f)), 'unknown')
        tier = 'unique' if item in uniques else 'runic' if item in runic else 'standard'
        group = next((g for g, fs in GROUPS.items() if family in fs), 'unassigned')
        rows.append({'item': item, 'family': family, 'group': group, 'tier': tier,
                     'base_registry_reference': item in registry_ids})
    root = ROOT / 'src/main/resources/data' / MOD / 'tags/item'
    for family in FAMILIES:
        values = [optional('#simplyswords:implicit/' + family)]
        values += [optional(r['item']) for r in rows if r['family'] == family]
        write(root / f'families/{family}.json', {'replace': False, 'values': values})
    for group, families in GROUPS.items():
        write(root / f'weapons/{group}.json', {'replace': False, 'values': ['#' + MOD + ':families/' + f for f in families]})
    for tier in ['standard', 'runic', 'unique']:
        values = [optional(r['item']) for r in rows if r['tier'] == tier] if tier == 'standard' else [optional('#simplyswords:' + ('uniques' if tier == 'unique' else 'runic_gear'))]
        write(root / f'weapons/{tier}.json', {'replace': False, 'values': values})
    write(root / 'weapons/all.json', {'replace': False, 'values': [optional('#simplyswords:swords'),
        '#' + MOD + ':weapons/runic', '#' + MOD + ':weapons/unique',
        *['#' + MOD + ':families/' + f for f in FAMILIES]]})
    write(root / 'weapons/integrated.json', {'replace': False, 'values': ['#' + MOD + ':weapons/all']})
    write(ROOT / 'src/main/resources/data/apotheosis/data_maps/item/loot_category_overrides.json',
          {'replace': False, 'values': {'#' + MOD + ':weapons/integrated': 'apotheosis:melee_weapon'}})
    report = {'simplyswords_sha512': digest, 'weapons': len(rows),
        'base_registry_references': sum(r['base_registry_reference'] for r in rows),
        'optional_unregistered_references': [r['item'] for r in rows if not r['base_registry_reference']],
        'families': dict(collections.Counter(r['family'] for r in rows)),
        'groups': dict(collections.Counter(r['group'] for r in rows)),
        'tiers': dict(collections.Counter(r['tier'] for r in rows)), 'items': rows}
    write(ROOT / 'docs/weapon-inventory.json', report)
    print(json.dumps({k: v for k, v in report.items() if k != 'items'}, indent=2))

if __name__ == '__main__':
    generate(pathlib.Path(sys.argv[1]).resolve())
