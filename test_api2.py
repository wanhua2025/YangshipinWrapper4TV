import urllib.request, hashlib, time, json, re

BASE = 'http://ry.400239.com'
API = '/api.php'
APPID = 10000
APPKEY = '4d86cdb33aa6f9dd27c4e6adee49995e'

def call(act, params=None):
    t = int(time.time())
    data = 't=' + str(t)
    if params:
        for k,v in params.items():
            data += '&' + k + '=' + v
    sign = hashlib.md5((data + '&' + APPKEY).encode()).hexdigest()
    url = BASE + API + '?app=' + str(APPID) + '&act=' + act + '&data=' + data + '&sign=' + sign
    try:
        req = urllib.request.urlopen(url, timeout=8)
        body = req.read().decode('utf-8', errors='replace')
        m = re.search(r'\{.*\}', body, re.DOTALL)
        if m: body = m.group()
        return json.loads(body)
    except Exception as e:
        return {'error': str(e)}

token = None
# 1. Login
r = call('user_logon', {'device_id': 'tv_test_mac001', 'passwd': ''})
print('=== Login ===')
print('code:', r.get('code'))
if isinstance(r.get('msg'), str) and len(r['msg']) > 50:
    print('msg is encrypted token (correct)')
    token = r['msg']
elif isinstance(r.get('msg'), dict):
    print(json.dumps(r, indent=2, ensure_ascii=False))

# 2. ini - check all fields
print('\n=== ini (all remote config) ===')
r = call('ini')
print(json.dumps(r, indent=2, ensure_ascii=False))

# 3. notice with token
print('\n=== notice with token ===')
if token:
    r = call('notice', {'token': token})
    print('code:', r.get('code'))
    print('msg:', str(r.get('msg'))[:200])
else:
    r = call('notice')
    print('code:', r.get('code'), 'msg:', str(r.get('msg'))[:200])

# 4. goods with token
print('\n=== goods with token ===')
if token:
    r = call('goods', {'token': token})
    print('code:', r.get('code'))
    print('msg:', str(r.get('msg'))[:200])

# 5. pay page raw
print('\n=== pay.php raw content ===')
try:
    req = urllib.request.urlopen(BASE + '/pay.php', timeout=8)
    body = req.read().decode('utf-8', errors='replace')
    print(body[:500])
except Exception as e:
    print('Error:', e)