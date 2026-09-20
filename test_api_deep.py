import urllib.request, hashlib, time, json, re

BASE = 'http://ry.400239.com'
API = '/api.php'
APPID = 10000
APPKEY = '4d86cdb33aa6f9dd27c4e6adee49995e'

def call_get(act, data_dict):
    t = int(time.time())
    data = 't=' + str(t)
    for k,v in data_dict.items():
        data += '&' + k + '=' + str(v)
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

login = call_get('user_logon', {'device_id': 'tv_test003'})
token = login.get('msg', '') if login.get('code') == 106 else None
print('Login code=' + str(login.get('code')) + ' token=' + str(token)[:20])

print('\n=== goods with token GET ===')
r = call_get('goods', {'token': token})
print(json.dumps(r, indent=2, ensure_ascii=False)[:500])

print('\n=== pay with token GET ===')
r = call_get('pay', {'token': token, 'product_id': '1', 'price': '25'})
print(json.dumps(r, indent=2, ensure_ascii=False)[:500])

print('\n=== user_info ===')
r = call_get('user_info', {'token': token})
print(json.dumps(r, indent=2, ensure_ascii=False)[:500])

print('\n=== heartbeat ===')
r = call_get('motion', {'token': token})
print(json.dumps(r, indent=2, ensure_ascii=False)[:500])

print('\n=== admin panel HTML ===')
try:
    req = urllib.request.urlopen(BASE + '/admin/', timeout=8)
    body = req.read().decode('utf-8', errors='replace')
    print(body[:1000])
except Exception as e:
    print('ERR:', e)

print('\n=== help act ===')
r = call_get('help', {})
print(json.dumps(r, indent=2, ensure_ascii=False)[:1500])