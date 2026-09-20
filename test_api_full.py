import urllib.request, hashlib, time, json, re, sys

BASE = 'http://ry.400239.com'
API = '/api.php'
APPID = 10000
APPKEY = '4d86cdb33aa6f9dd27c4e6adee49995e'

def call(act, params=None, token=None):
    t = int(time.time())
    data = 't=' + str(t)
    if params:
        for k,v in params.items():
            data += '&' + k + '=' + str(v)
    if token:
        data += '&token=' + token
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

print('='*60)
print('STEP 1: Login with test device')
print('='*60)
login = call('user_logon', {'device_id': 'tv_test_pay001'})
print(json.dumps(login, indent=2, ensure_ascii=False))
token = None
if login.get('code') == 106 and isinstance(login.get('msg'), str) and len(login['msg']) > 20:
    token = login['msg']
    print(f'TOKEN: {token[:30]}...')

print()
print('='*60)
print('STEP 2: Try ALL goods/product acts WITH token')
print('='*60)
for act_name in ['goods', 'goods_list', 'products', 'product_list', 'shop', 'shop_list', 'vip', 'vip_list', 'pay', 'pay_list', 'order', 'order_list']:
    r = call(act_name, token=token)
    msg = r.get('msg', '')
    if isinstance(msg, dict):
        print(f'  act={act_name:20s} code={r.get("code")} msg_keys={list(msg.keys())[:6]}')
    elif isinstance(msg, list):
        print(f'  act={act_name:20s} code={r.get("code")} list_len={len(msg)}')
    else:
        print(f'  act={act_name:20s} code={r.get("code")} msg={str(msg)[:80]}')

print()
print('='*60)
print('STEP 3: Check ini for ALL fields')
print('='*60)
ini = call('ini')
print(json.dumps(ini, indent=2, ensure_ascii=False))

print()
print('='*60)
print('STEP 4: Try create_order / pay_order')
print('='*60)
for act_name in ['create_order', 'pay_order', 'submit_order', 'create_pay', 'get_pay_url', 'wx_pay', 'ali_pay']:
    r = call(act_name, {'product_id': 1, 'price': 25}, token=token)
    msg = r.get('msg', '')
    print(f'  act={act_name:20s} code={r.get("code")} msg={str(msg)[:120]}')

print()
print('='*60)
print('STEP 5: Check remote server paths (pay.php, admin, etc)')
print('='*60)
for path in ['/pay.php', '/admin/', '/shop/', '/api.php?act=help', '/api.php?act=info']:
    try:
        req = urllib.request.urlopen(BASE + path, timeout=5)
        print(f'  {path:30s} -> HTTP {req.status}')
    except urllib.error.HTTPError as e:
        print(f'  {path:30s} -> HTTP {e.code}')
    except Exception as e:
        print(f'  {path:30s} -> ERR {str(e)[:40]}')