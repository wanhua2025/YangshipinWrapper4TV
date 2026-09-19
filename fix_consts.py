import sys

filepath = r"f:\YangshipinWrapper4TV-main\app\src\main\java\com\lwtdzh\yangshipinwrapper4tv\MainActivity.java"

with open(filepath, 'r', encoding='utf-8') as f:
    c = f.read()

replacements = [
    ("MENU_PAGE_MAIN", "MENU_PAGE_CHANNELS"),
    ("MAIN_MENU_SETTINGS", "SETTINGS_IDX_DECODER"),
    ("MAIN_MENU_CHANNELS", "currentIndex"),
    ("MAIN_MENU_ITEMS.length", "SETTINGS_ITEM_COUNT"),
    ('MAIN_MENU_ITEMS[position]', '"Settings"'),
    ("mainMenuSelection", "settingsSelection"),
]

for old, new in replacements:
    c = c.replace(old, new)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(c)

print("Done. File size:", len(c))