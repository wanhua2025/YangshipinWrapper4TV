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
    return rc4_crypt(RC4_KEY.encode('gbk'), plain.encode('gbk')).hex()

def rc4_decrypt(hex_str):
    try:
        return rc4_crypt(RC4_KEY.encode('gbk'), bytes.fromhex(hex_str.strip())).decode('gbk', errors='replace')
    except:
        return ''

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

def dec_msg(r):
    msg = r.get('msg', '')
    if isinstance(msg, str) and len(msg) > 10:
        return rc4_decrypt(msg)
    return msg

print('='*60)
print('1. LOGIN (markcode=device_id, with account)')
print('='*60)
ts = int(time.time())
device_id = 'tv_full_test_002'
r = call_api('user_logon', {'account': 'auto_' + device_id, 'password': 'tvpass01', 'markcode': device_id, 't': ts})
print('code:', r.get('code'))
print('msg raw:', str(r.get('msg', ''))[:60])
d = dec_msg(r)
print('DECRYPTED:', d[:400])

token = None
if r.get('code') == 200:
    try:
        dj = json.loads(d)
        token = dj.get('token')
        info = dj.get('info', {})
        print('Login OK! vip=' + str(info.get('vip')) + ' user=' + str(info.get('user')))
    except: pass

if not token and isinstance(r.get('msg'), str) and len(r['msg']) > 20:
    token = r['msg']
    print('Using raw token from msg')

print('\n' + '='*60)
print('2. GOODS LIST - FULL DECRYPT')
print('='*60)
ts = int(time.time())
r = call_api('goods', {'t': ts, 'token': token})
print('code:', r.get('code'))
d = dec_msg(r)
print('DECRYPTED GOODS:')
print(d[:1000])
try:
    j = json.loads(d)
    print('\nPARSED:')
    print(json.dumps(j, indent=2, ensure_ascii=False)[:1000])
except: pass

print('\n' + '='*60)
print('3. NOTICE')
print('='*60)
ts = int(time.time())
r = call_api('notice', {'t': ts, 'token': token})
print('code:', r.get('code'))
d = dec_msg(r)
print('DECRYPTED NOTICE:', d[:500])

print('\n' + '='*60)
print('4. PAY - try different params')
print('='*60)
for pid in ['1', '2', '3', '4', '5', '10']:
    for pt in ['wx', 'ali', 'wechat', 'alipay']:
        ts = int(time.time())
        r = call_api('pay', {'t': ts, 'token': token, 'product_id': pid, 'pay_type': pt})
        d = dec_msg(r)
        if r.get('code') not in [400, 130]:
            print('pid=%s pt=%s code=%d dec=%s' % (pid, pt, r.get('code'), d[:100]))

# Try pay with product_id + pay_type + price
for pid in ['1', '2', '3']:
    ts = int(time.time())
    r = call_api('pay', {'t': ts, 'token': token, 'product_id': pid, 'pay_type': 'wx', 'price': '25', 'device_id': device_id})
    d = dec_msg(r)
    print('pay pid=%s code=%d dec=%s' % (pid, r.get('code'), d[:150]))

print('\n' + '='*60)
print('5. INIT (public)')
print('='*60)
ts = int(time.time())
r = call_api('ini', {'t': ts})
print('code:', r.get('code'))
d = dec_msg(r)
if d:
    print('DECRYPTED:', d[:500])
else:
    print('msg:', str(r.get('msg', ''))[:500])