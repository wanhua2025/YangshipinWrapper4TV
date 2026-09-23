<?php
require_once __DIR__ . '/include/global.php';
echo '<h3>app_version 所有记录</h3>';
$vers = Db::table('app_version')->where(['state'=>'y'])->order('sort asc,id asc')->select();
foreach($vers as $v){
    echo "[ID:$v['id']] sort=$v['sort'] version=$v['version'] tag=$v['tag'] min_sdk=$v['min_sdk'] max_sdk=$v['max_sdk'] is_tv=$v['is_tv'] is_phone=$v['is_phone']<br>";
}
echo '<h3>模拟 TV 设备 SDK=33 的匹配</h3>';
$device_sdk = 33; $is_tv = 1; $is_phone = 0;
$matched = null;
foreach($vers as $v){
    $ok = true;
    $reason = '';
    if($device_sdk > 0 && $v['min_sdk'] > 0 && $device_sdk < $v['min_sdk']){ $ok=false; $reason = 'SDK too low'; }
    if($device_sdk > 0 && $v['max_sdk'] > 0 && $device_sdk > $v['max_sdk']){ $ok=false; $reason = 'SDK too high ('.$device_sdk.' > max '.$v['max_sdk'].')'; }
    if($is_tv && $v['is_phone'] == '1' && $v['is_tv'] != '1'){ $ok=false; $reason = 'TV but version only phone'; }
    if($is_phone && $v['is_tv'] == '1' && $v['is_phone'] != '1'){ $ok=false; $reason = 'Phone but version only TV'; }
    echo "check ID:$v['id']] v=$v['version'] ok=".($ok?'Y':'N').($reason?' ('.$reason.')':'').'<br>';
    if($matched===null && $ok){ $matched = $v; }
}
echo '<br>最终选中: '.($matched?$matched['version']:'none');
