<?php
include("include/global.php");

echo "<h3>当前应用(appid=10000)支付配置：</h3>";
$app = Db::table('app')->where('id', 10000)->find();
if ($app) {
    echo "<pre>";
    foreach ($app as $k => $v) {
        if (strpos($k, 'pay') !== false || strpos($k, 'name') !== false || strpos($k, 'return') !== false) {
            echo "$k => $v\n";
        }
    }
    echo "</pre>";
} else {
    echo "未找到appid=10000的应用！<br>";
}

echo "<h3>当前商品列表：</h3>";
$goods = Db::table('goods')->where('appid', 10000)->select();
echo "<table border='1'><tr><th>ID</th><th>名称</th><th>类型</th><th>价格</th><th>天数</th><th>状态</th></tr>";
if ($goods) {
    foreach ($goods as $g) {
        echo "<tr><td>{$g['id']}</td><td>{$g['name']}</td><td>{$g['type']}</td><td>{$g['money']}</td><td>{$g['amount']}</td><td>{$g['state']}</td></tr>";
    }
}
echo "</table>";

echo "<h3>添加新套餐商品...</h3>";

$new_goods = [
    ['name' => '月卡会员', 'type' => 'vip', 'money' => '29.90', 'amount' => 30,  'jie' => '30天VIP会员',     'appid' => 10000, 'state' => 'y'],
    ['name' => '季卡会员', 'type' => 'vip', 'money' => '79.90', 'amount' => 90,  'jie' => '90天VIP会员',     'appid' => 10000, 'state' => 'y'],
    ['name' => '年卡会员', 'type' => 'vip', 'money' => '299.00','amount' => 365, 'jie' => '365天VIP会员',    'appid' => 10000, 'state' => 'y'],
    ['name' => '永久会员', 'type' => 'vip', 'money' => '999.00','amount' => 9999,'jie' => '终身VIP会员',      'appid' => 10000, 'state' => 'y'],
];

$added = 0;
foreach ($new_goods as $g) {
    $exists = Db::table('goods')->where(['name' => $g['name'], 'appid' => 10000])->find();
    if ($exists) {
        echo "跳过已存在: {$g['name']}<br>";
    } else {
        $res = Db::table('goods')->add($g);
        if ($res) {
            echo "成功添加: {$g['name']} (ID=$res)<br>";
            $added++;
        } else {
            echo "添加失败: {$g['name']}<br>";
        }
    }
}

echo "<p>共添加 $added 个新商品</p>";

echo "<h3>最终商品列表：</h3>";
$goods = Db::table('goods')->where('appid', 10000)->select();
echo "<table border='1'><tr><th>ID</th><th>名称</th><th>类型</th><th>价格</th><th>天数</th><th>状态</th></tr>";
if ($goods) {
    foreach ($goods as $g) {
        echo "<tr><td>{$g['id']}</td><td>{$g['name']}</td><td>{$g['type']}</td><td>{$g['money']}</td><td>{$g['amount']}</td><td>{$g['state']}</td></tr>";
    }
}
echo "</table>";
echo "<p><a href='index.php'>返回首页</a></p>";