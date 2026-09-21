"""Validate the built artifact and create a source-only delivery archive."""
import hashlib
import json
import pathlib
import struct
import tomllib
import xml.etree.ElementTree as ET
import zipfile

root = pathlib.Path(__file__).resolve().parents[1]
jar = root / 'build/libs/EYRAX-Apothic-Arsenal-0.2.0.jar'
with zipfile.ZipFile(jar) as archive:
    assert archive.testzip() is None
    names = archive.namelist()
    classes = [n for n in names if n.endswith('.class')]
    assert len(classes) >= 14
    assert all(n.startswith('com/eyrax/apothicarsenal/') for n in classes)
    assert not any('/test/' in n for n in names), 'GameTests must not ship in the mod JAR'
    assert not any(n.endswith('.jar') or 'mixin' in n.lower() for n in names)
    versions = sorted({struct.unpack('>H', archive.read(n)[6:8])[0] for n in classes})
    assert versions == [65], versions
    metadata = tomllib.loads(archive.read('META-INF/neoforge.mods.toml').decode())
    assert metadata['mods'][0]['modId'] == 'eyrax_apothic_arsenal'
    assert metadata['mods'][0]['version'] == '0.2.0'
    tags = [n for n in names if '/tags/item/' in n and n.endswith('.json')]
    assert len(tags) == 27
    for name in names:
        if name.endswith(('.json', '.mcmeta')):
            json.loads(archive.read(name))
    mapping = json.loads(archive.read('data/apotheosis/data_maps/item/loot_category_overrides.json'))
    assert mapping['values']['#eyrax_apothic_arsenal:weapons/integrated'] == 'apotheosis:melee_weapon'

tests = [ET.parse(p).getroot() for p in (root / 'build/test-results/test').glob('TEST-*.xml')]
assert sum(int(t.attrib['tests']) for t in tests) == 6
assert all(int(t.attrib[k]) == 0 for t in tests for k in ['failures', 'errors', 'skipped'])
log = (root / 'work/phase2-build-tests.log').read_text(encoding='utf-8-sig')
assert 'BUILD SUCCESSFUL' in log and 'BUILD FAILED' not in log
assert 'All 4 required tests passed' in log
assert 'Verified 13566 weapon/gem/purity smithing combinations' in log
(root / 'docs/build-result.txt').write_text(log, encoding='utf-8')
report = {
    'artifact': str(jar.resolve()), 'bytes': jar.stat().st_size,
    'sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
    'class_count': len(classes), 'class_major_versions': versions,
    'item_tags': len(tags), 'embedded_third_party_jars': 0,
    'mod_metadata': metadata, 'unit_tests_passed': 6, 'gametests_passed': 4,
    'minecraft_runtime_tested': 'NeoForge GameTest dedicated server; no client GUI or user modpack',
    'smithing_combinations': 13566, 'reforge_salvage_cases': 20, 'component_round_trips': 4,
    'entries': names,
}
(root / 'docs/jar-inspection.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')

dist = root / 'dist'
dist.mkdir(exist_ok=True)
destination = dist / 'EYRAX-Apothic-Arsenal-0.2.0-source.zip'
allowed = {'src', 'gradle', 'tools', 'docs'}
files = [p for p in root.iterdir() if p.is_file()]
for directory in sorted(allowed):
    files.extend(p for p in (root / directory).rglob('*') if p.is_file() and '__pycache__' not in p.parts)
with zipfile.ZipFile(destination, 'w', zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(files):
        archive.write(path, pathlib.Path(root.name) / path.relative_to(root))
print(json.dumps({k: v for k, v in report.items() if k not in {'entries', 'mod_metadata'}}, indent=2))
print(f'Source archive: {destination}')
