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

print('=== 1. ini ===')
r = call('ini')
print(json.dumps(r, indent=2, ensure_ascii=False))

print('\n=== 2. user_logon ===')
r = call('user_logon', {'device_id': 'tv_test_mac001'})
print('code:', r.get('code'), 'keys:', list(r.keys()))

print('\n=== 3. Try all goods-related acts ===')
for act_name in ['goods_list', 'goods', 'vip_list', 'pay_list', 'product_list', 'shop_list', 'app_goods']:
    r = call(act_name)
    msg = r.get('msg')
    if isinstance(msg, dict):
        print('  act=%s code=%d msg_keys=%s' % (act_name, r.get('code',0), list(msg.keys())))
    else:
        print('  act=%s code=%d msg=%s' % (act_name, r.get('code',0), str(msg)[:120]))

print('\n=== 4. Try app_info / notice ===')
for act_name in ['app_info', 'notice', 'announce', 'config']:
    r = call(act_name)
    print('  act=%s code=%d' % (act_name, r.get('code',0)))
    if isinstance(r.get('msg'), dict):
        print('    msg:', json.dumps(r['msg'], ensure_ascii=False)[:200])