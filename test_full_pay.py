import urllib.request, hashlib, time, json, re, random

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
    return rc4_crypt(RC4_KEY.encode('gbk'), plain.encode('gbk')).hex()

def rc4_decrypt(hex_str):
    try: return rc4_crypt(RC4_KEY.encode('gbk'), bytes.fromhex(hex_str.strip())).decode('gbk', errors='replace')
    except: return ''

def arr_sign(data_str):
    return hashlib.md5((data_str + '&' + APPKEY).encode('utf-8')).hexdigest()

def call_api(act, pd):
    pp = [k + '=' + str(v) for k, v in pd.items()]
    plain = '&'.join(pp)
    url = BASE + API + '?app=' + str(APPID) + '&act=' + act + '&data=' + rc4_encrypt(plain) + '&sign=' + arr_sign(plain)
    try:
        req = urllib.request.urlopen(url, timeout=10)
        body = req.read().decode('utf-8', errors='replace')
        m = re.search(r'\{.*\}', body, re.DOTALL)
        if m: body = m.group()
        return json.loads(body)
    except Exception as e:
        return {'error': str(e)}

did = 'tv_full_pay_002'
acc = 'auto_' + did

print('=== REGISTER ===')
r = call_api('user_reg', {'account': acc, 'password': 'tvpass01', 'markcode': did, 't': int(time.time())})
print('reg code:', r.get('code'), 'raw_msg:', str(r.get('msg',''))[:60], 'dec:', rc4_decrypt(str(r.get('msg','')))[:200])

print('\n=== LOGIN ===')
r = call_api('user_logon', {'account': acc, 'password': 'tvpass01', 'markcode': did, 't': int(time.time())})
print('login code:', r.get('code'), 'raw_msg:', str(r.get('msg',''))[:80])
dec = rc4_decrypt(str(r.get('msg','')))
print('login dec:', dec[:400])
token = None
try:
    dj = json.loads(dec)
    token = dj.get('token')
    print('TOKEN:', str(token)[:40] if token else 'NONE')
except Exception as e:
    print('parse err:', e)
    token = r.get('msg') if isinstance(r.get('msg'), str) and len(r['msg']) > 30 else None

if not token:
    print('FAILED to get token!')
    exit(1)

print('\n=== GOODS ===')
r = call_api('goods', {'t': int(time.time()), 'token': token})
print('goods code:', r.get('code'))
dec = rc4_decrypt(str(r.get('msg','')))
print('goods dec:', dec[:400])

print('\n=== PAY ===')
order_no = time.strftime('%Y%m%d%H%M%S') + str(random.randint(10000, 99999))
print('order_no:', order_no)
r = call_api('pay', {'t': int(time.time()), 'token': token, 'order': order_no, 'account': acc, 'way': 'wx', 'gid': 1, 'ua': 2})
print('pay code:', r.get('code'))
raw_msg = str(r.get('msg',''))
print('pay raw_msg hex:', raw_msg[:80])
dec = rc4_decrypt(raw_msg)
print('pay DECRYPTED:', dec[:1500])

try:
    pd = json.loads(dec)
    print('PARSED:', json.dumps(pd, indent=2, ensure_ascii=False)[:800])
except Exception as e:
    print('parse err:', e)

print('\n=== PAY_RES ===')
for i in range(3):
    r = call_api('pay_res', {'t': int(time.time()), 'token': token, 'oid': order_no})
    dec = rc4_decrypt(str(r.get('msg','')))
    print('  poll %d: code=%s dec=%s' % (i, r.get('code'), dec[:200]))
    time.sleep(1)