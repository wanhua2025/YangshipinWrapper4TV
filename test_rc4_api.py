import urllib.request, hashlib, time, json, re, sys

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
    out = rc4_crypt(key, data)
    return out.hex()

def rc4_decrypt(hex_str):
    data = bytes.fromhex(hex_str.strip())
    key = RC4_KEY.encode('gbk')
    raw = rc4_crypt(key, data)
    return raw.decode('gbk', errors='replace')

def arr_sign(data_str):
    src = data_str + '&' + APPKEY
    return hashlib.md5(src.encode('utf-8')).hexdigest()

def call_api(act, params_dict):
    plain_parts = []
    for k, v in params_dict.items():
        plain_parts.append(k + '=' + str(v))
    plain = '&'.join(plain_parts)
    
    rc4_hex = rc4_encrypt(plain)
    sign = arr_sign(plain)
    
    url = BASE + API + '?app=' + str(APPID) + '&act=' + act + '&data=' + rc4_hex + '&sign=' + sign
    try:
        req = urllib.request.urlopen(url, timeout=10)
        body = req.read().decode('utf-8', errors='replace')
        raw = body
        m = re.search(r'\{.*\}', body, re.DOTALL)
        if m: body = m.group()
        j = json.loads(body)
        return j, raw
    except Exception as e:
        return {'error': str(e)}, ''

print('='*60)
print('1. LOGIN (device_id only)')
print('='*60)
ts = int(time.time())
r, raw = call_api('user_logon', {'t': ts, 'device_id': 'tv_rc4_test_001'})
print('code:', r.get('code'))
print('msg type:', type(r.get('msg')).__name__)
print('raw snippet:', raw[:200])
token = None
if r.get('code') == 106 and isinstance(r.get('msg'), str) and len(r['msg']) > 20:
    token = r['msg']
    print('TOKEN:', token[:40] + '...')
elif r.get('code') == 200:
    print('Login OK!', json.dumps(r, indent=2, ensure_ascii=False)[:500])
    if isinstance(r.get('msg'), dict):
        token = r['msg'].get('token')
print()

print('='*60)
print('2. HEARTBEAT / MOTION (verify token works)')
print('='*60)
ts = int(time.time())
r, raw = call_api('motion', {'t': ts, 'token': token})
print('code:', r.get('code'), 'msg:', str(r.get('msg'))[:200])
print()

print('='*60)
print('3. GOODS (product list) with RC4 protocol')
print('='*60)
ts = int(time.time())
r, raw = call_api('goods', {'t': ts, 'token': token})
print('code:', r.get('code'))
print('raw:', raw[:500])
if isinstance(r.get('msg'), str) and len(r['msg']) > 20 and r.get('code') == 106:
    decrypted = rc4_decrypt(r['msg'])
    print('DECRYPTED msg:', decrypted[:500])
print()

print('='*60)
print('4. ALL possible API acts')
print('='*60)
acts = ['goods', 'goods_list', 'products', 'shop', 'pay', 'order', 'order_list', 
        'create_order', 'user_info', 'vip_info', 'notice', 'announce', 'config',
        'version', 'check_update']
for act in acts:
    ts = int(time.time())
    r, _ = call_api(act, {'t': ts, 'token': token})
    code = r.get('code')
    msg = r.get('msg', '')
    if isinstance(msg, str) and len(msg) > 20 and code == 106:
        msg_short = rc4_decrypt(msg)[:80]
    else:
        msg_short = str(msg)[:80]
    print('  act=%-18s code=%-5s msg=%s' % (act, code, msg_short))