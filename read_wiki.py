"""Download and parse seesaawiki Gakumas card data (EUC-JP)"""
import urllib.request, re, html, json, os

URL = "https://seesaawiki.jp/gakumasu/d/%A5%B9%A5%AD%A5%EB%A5%AB%A1%BC%A5%C9%B0%EC%CD%F7"

# Download as bytes
req = urllib.request.Request(URL, headers={"User-Agent": "Mozilla/5.0"})
data = urllib.request.urlopen(req, timeout=30).read()

# Decode as EUC-JP
text = data.decode('euc-jp', errors='replace')

# Save decoded HTML for inspection
with open('wiki_decoded.html', 'w', encoding='utf-8') as f:
    f.write(text[:50000])
print(f"Downloaded {len(data)} bytes, decoded to {len(text)} chars")
print(f"First 500 chars: {text[:500]}")
print(f"\nContains 'スキルカード': {'スキルカード' in text}")
print(f"Contains 'センス': {'センス' in text}")
print(f"Contains '好調': {'好調' in text}")

# Extract table rows
# Remove scripts/styles/noscript
text = re.sub(r'<script[^>]*>.*?</script>', '', text, flags=re.DOTALL)
text = re.sub(r'<style[^>]*>.*?</style>', '', text, flags=re.DOTALL)
text = re.sub(r'<noscript[^>]*>.*?</noscript>', '', text, flags=re.DOTALL)

rows = re.findall(r'<tr>(.*?)</tr>', text, re.DOTALL)
print(f"\nFound {len(rows)} table rows")

# Extract cells from each row
cards = []
for row in rows:
    cells = re.findall(r'<(?:td|th)[^>]*>(.*?)</(?:td|th)>', row, re.DOTALL)
    if cells:
        clean = []
        for c in cells:
            c = re.sub(r'<br\s*/?>', ' / ', c)
            c = re.sub(r'<[^>]+>', '', c)
            c = html.unescape(c).strip()
            clean.append(c)
        if any(len(c) > 1 for c in clean[:2]):
            cards.append(clean)

print(f"Extracted {len(cards)} card-like rows")

# Show some examples
for c in cards[:20]:
    preview = [x[:50] for x in c[:5]]
    print(f"  {' | '.join(preview)}")

# Save as JSON
with open('cards_data.json', 'w', encoding='utf-8') as f:
    json.dump(cards, f, ensure_ascii=False, indent=2)
print(f"\nSaved {len(cards)} entries to cards_data.json")
