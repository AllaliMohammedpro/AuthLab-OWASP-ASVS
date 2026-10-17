"""Version cross-platform : python scripts/prepare_password_list.py (racine du projet)."""
from pathlib import Path
from urllib.request import urlopen

SOURCE = "https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/Common-Credentials/10k-most-common.txt"
TARGET = Path(__file__).resolve().parents[1] / "backend" / "config" / "common-passwords.txt"
print("Téléchargement de SecLists (licence MIT)...")
with urlopen(SOURCE, timeout=30) as response:
    words = response.read().decode("utf-8-sig").splitlines()
seen = set()
selected = []
for item in words:
    if 8 <= len(item) <= 128 and item.lower() not in seen:
        seen.add(item.lower())
        selected.append(item)
    if len(selected) == 3000:
        break
if len(selected) < 3000:
    raise RuntimeError(f"Liste insuffisante : {len(selected)} mots de passe admissibles.")
TARGET.parent.mkdir(parents=True, exist_ok=True)
TARGET.write_text("\n".join(selected) + "\n", encoding="utf-8")
print(f"OK : {len(selected)} mots de passe enregistrés dans {TARGET}")
