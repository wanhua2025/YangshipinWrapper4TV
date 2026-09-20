<?php
include("include/global.php");

echo "<h3>模拟支付成功回调 - 订单处理工具</h3>";

$order = isset($_GET['order']) ? purge($_GET['order']) : '';
$action = isset($_GET['action']) ? $_GET['action'] : 'show';

if ($action == 'mark_paid' && !empty($order)) {
    $order_res = Db::table('goods_order','as O')->field('O.*,G.type,G.amount')->JOIN("goods","as G",'O.gid=G.id')->where(['O.order'=>$order])->find();
    if (!$order_res) {
        echo "订单不存在！";
    } elseif ($order_res['state'] != 0) {
        echo "订单已处理，状态: " . $order_res['state'];
    } else {
        $res_user = Db::table('user')->where(['id'=>$order_res['uid']])->find();
        if (!$res_user) {
            echo "用户不存在！";
        } else {
            if ($order_res['type'] == 'vip') {
                if ($res_user['vip'] == 999999999) {
                    $vip = 999999999;
                } elseif ($order_res['amount'] == 9999) {
                    $vip = 999999999;
                } else {
                    if ($res_user['vip'] > time()) {
                        $vip = $res_user['vip'] + $order_res['amount'] * 86400;
                    } else {
                        $vip = time() + $order_res['amount'] * 86400;
                    }
                }
                $res = Db::table('user')->where('id', $res_user['id'])->update(['vip'=>$vip]);
                echo "用户VIP更新: " . date('Y-m-d H:i:s', $vip) . " (amount=" . $order_res['amount'] . "天)<br>";
            } elseif ($order_res['type'] == 'fen') {
                $fen = $res_user['fen'] + $order_res['amount'];
                $res = Db::table('user')->where('id', $res_user['id'])->update(['fen'=>$fen]);
                echo "用户积分更新: +" . $order_res['amount'] . "<br>";
            }
            
            Db::table('goods_order')->where('id', $order_res['id'])->update([
                'state' => 2,
                'p_time' => time(),
                'data' => 'mock_pay_success'
            ]);
            echo "订单已标记为支付成功！<br>";
        }
    }
    echo "<p><a href='mock_pay.php'>返回</a></p>";
    exit;
}

echo "<h3>最近订单列表</h3>";
$orders = Db::table('goods_order')->order('id desc')->limit(10)->select();
if ($orders) {
    echo "<table border='1'><tr><th>ID</th><th>订单号</th><th>用户ID</th><th>商品</th><th>金额</th><th>类型</th><th>状态</th><th>时间</th><th>操作</th></tr>";
    foreach ($orders as $o) {
        $state_text = ['待支付', '失败', '已支付'][$o['state']] ?? '未知';
        $status_color = ['orange', 'red', 'green'][$o['state']] ?? 'gray';
        echo "<tr>";
        echo "<td>{$o['id']}</td>";
        echo "<td>{$o['order']}</td>";
        echo "<td>{$o['uid']}</td>";
        echo "<td>{$o['name']}</td>";
        echo "<td>{$o['money']}</td>";
        echo "<td>{$o['p_type']}</td>";
        echo "<td style='color:$status_color'>$state_text</td>";
        echo "<td>" . date('Y-m-d H:i:s', $o['o_time']) . "</td>";
        echo "<td><a href='?order={$o['order']}&action=mark_paid'>标记已支付</a></td>";
        echo "</tr>";
    }
    echo "</table>";
} else {
    echo "暂无订单";
}

echo "<h3>指定订单操作</h3>";
echo "<form method='get'>
订单号: <input type='text' name='order' required>
<input type='hidden' name='action' value='mark_paid'>
<input type='submit' value='模拟支付成功'>
</form>";
echo "<p><a href='index.php'>返回首页</a></p>";