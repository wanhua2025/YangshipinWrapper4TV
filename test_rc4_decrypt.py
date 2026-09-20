import urllib.request, hashlib, time, json, re

BASE = 'http://ry.400239.com'
API = '/api.php'
APPID = 10000
APPKEY = '4d86cdb33aa6f9dd27c4e6adee49995e'
RC4_KEY = 'GN8ZGa4DmaHQrHhSTyQ3FwnhCQt68EXQ'

def rc4_crypt(key, data):
    S = list(range(256))
    j = 0
    for i in range(256):
        j = (j + S[i] + key[i % len(key)]) & 0xFF
        S[i], S[j] = S[j], S[i]
    i = j = 0
    out = bytearray()
    for b in data:
        i = (i + 1) & 0xFF
        j = (j + S[i]) & 0xFF
        S[i], S[j] = S[j], S[i]
        out.append(b ^ S[(S[i] + S[j]) & 0xFF])
    return bytes(out)

def rc4_encrypt(plain):
    data = plain.encode('gbk')
    key = RC4_KEY.encode('gbk')
    return rc4_crypt(key, data).hex()

def rc4_decrypt(hex_str):
    data = bytes.fromhex(hex_str.strip())
    key = RC4_KEY.encode('gbk')
    return rc4_crypt(key, data).decode('gbk', errors='replace')

def arr_sign(data_str):
    return hashlib.md5((data_str + '&' + APPKEY).encode('utf-8')).hexdigest()

def call_api(act, params_dict):
    plain_parts = [k + '=' + str(v) for k, v in params_dict.items()]
    plain = '&'.join(plain_parts)
    rc4_hex = rc4_encrypt(plain)
    sign = arr_sign(plain)
    url = BASE + API + '?app=' + str(APPID) + '&act=' + act + '&data=' + rc4_hex + '&sign=' + sign
    try:
        req = urllib.request.urlopen(url, timeout=10)
        body = req.read().decode('utf-8', errors='replace')
        m = re.search(r'\{.*\}', body, re.DOTALL)
        if m: body = m.group()
        return json.loads(body)
    except Exception as e:
        return {'error': str(e)}

# 1. Login - match Android client exactly
print('=== LOGIN (exact client format) ===')
ts = int(time.time())
r = call_api('user_logon', {'t': ts, 'device_id': 'tv_full_test_001'})
print('code:', r.get('code'))
msg = r.get('msg', '')
if isinstance(msg, str):
    print('msg raw:', msg[:60])
    if len(msg) > 20:
        try:
            dec = rc4_decrypt(msg)
            print('msg decrypted:', dec[:300])
        except: pass

token = None
if r.get('code') == 106 and isinstance(msg, str) and len(msg) > 20:
    token = msg
elif r.get('code') == 200 and isinstance(msg, dict):
    token = msg.get('token')
    vip = msg.get('vip')
    print('Login: token=' + str(token)[:30] + ' vip=' + str(vip))

# 2. motion verify
if token:
    ts = int(time.time())
    r = call_api('motion', {'t': ts, 'token': token})
    print('\n=== MOTION ===')
    print('code:', r.get('code'))
    msg = r.get('msg', '')
    if isinstance(msg, str) and len(msg) > 20:
        print('decrypted:', rc4_decrypt(msg)[:300])

# 3. goods FULL decrypt
if token:
    ts = int(time.time())
    r = call_api('goods', {'t': ts, 'token': token})
    print('\n=== GOODS (FULL DECRYPT) ===')
    print('code:', r.get('code'))
    msg = r.get('msg', '')
    if isinstance(msg, str):
        decrypted = rc4_decrypt(msg)
        print('DECRYPTED GOODS:')
        print(decrypted)
        # Try parse as JSON
        try:
            j = json.loads(decrypted)
            print('\nPARSED JSON:')
            print(json.dumps(j, indent=2, ensure_ascii=False))
        except: pass

# 4. notice
if token:
    ts = int(time.time())
    r = call_api('notice', {'t': ts, 'token': token})
    print('\n=== NOTICE (FULL DECRYPT) ===')
    print('code:', r.get('code'))
    msg = r.get('msg', '')
    if isinstance(msg, str) and len(msg) > 10:
        decrypted = rc4_decrypt(msg)
        print('DECRYPTED NOTICE:')
        print(decrypted[:500])

# 5. pay - need more params
if token:
    ts = int(time.time())
    r = call_api('pay', {'t': ts, 'token': token, 'product_id': '1', 'pay_type': 'wx'})
    print('\n=== PAY (product_id=1, wx) ===')
    print('code:', r.get('code'))
    msg = r.get('msg', '')
    if isinstance(msg, str) and len(msg) > 10:
        print('msg hex:', msg[:80])
        try:
            dec = rc4_decrypt(msg)
            print('decrypted:', dec[:500])
        except Exception as e:
            print('decrypt err:', e)