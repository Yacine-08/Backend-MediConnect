import json
import os
from uuid import uuid4

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
INPUT_FILE = os.path.join(BASE_DIR, "hopitaux.json")
OUTPUT_FILE = os.path.join(BASE_DIR, "hopitaux.sql")

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    data = json.load(f)

values = []

# 🔥 GEOJSON FORMAT (IMPORTANT)
features = data.get("features", [])

for feature in features:
    props = feature.get("properties", {})
    geometry = feature.get("geometry", {})

    name = props.get("name")
    if not name:
        continue

    coords = geometry.get("coordinates")

    if not coords:
        continue

    # GeoJSON = [longitude, latitude]
    lon, lat = coords[0], coords[1]

    name = name.replace("'", "''")

    values.append(
        f"('{uuid4()}', '{name}', {lat}, {lon}, 'PUBLIC')"
    )

sql = """INSERT INTO hopitaux (id, nom, latitude, longitude, type_etablissement)
VALUES
"""

sql += ",\n".join(values) + ";"

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write(sql)

print(f"✔ {len(values)} hôpitaux générés dans hopitaux.sql")