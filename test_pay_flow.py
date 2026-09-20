import sys
sys.path.insert(0, 'ruyi')
from mi_api import MiApi
import time

api = MiApi()

r = api.login('ceshi123456', '123456')
print('1. login: ok=%s vip=%s' % (r.ok, api.user.vip))

r = api.fetch_products()
print('2. products:')
for p in r.data:
    print('  gid=%s name=%s price=%s payAli=%s payWx=%s' % (p.gid, p.name, p.price, p.payAliEnabled, p.payWxEnabled))

# 尝试创建支付订单
if r.data:
    gid = r.data[0].gid
    print('\n3. 尝试为 gid=%s 创建支付订单...' % gid)
    try:
        pr = api.create_pay_order(gid, pay_way='ali')
        print('   result: ok=%s' % (pr.ok,))
        if pr.ok:
            print('   orderNo=%s qrCodeUrl=%s payUrl=%s' % (pr.data.orderNo, pr.data.qrCodeUrl, pr.data.payUrl))
            
            # 尝试查询支付状态
            print('\n4. 查询支付状态...')
            qr = api.query_pay_result(pr.data.orderNo)
            print('   result: ok=%s data=%s' % (qr.ok, qr.data if qr.ok else qr.msg))
        else:
            print('   error: %s' % pr.msg)
    except Exception as e:
        print('   exception: %s' % e)

# 检查vip
r = api.call_api('vip_info', {})
print('\n5. vip_info: ok=%s data=%s' % (r.ok, r.data if r.ok else r.msg))