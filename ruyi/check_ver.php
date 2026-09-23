<?php
require_once __DIR__ . '/include/global.php';

echo '<h3>当前应用列表</h3>';
$apps = Db::table('app')->select();
foreach($apps as $a){
    echo "App ID:{$a['id']} Name:{$a['name']}<br>";
}

echo '<h3>当前版本列表 (app_version表)</h3>';
if(@Db::table('app_version')->exist()){
    $vers = Db::table('app_version')->order('sort asc,id desc')->select();
    foreach($vers as $v){
        echo "[ID:$v['id']] appid:$v['appid'] version:$v['version'] tag:$v['tag'] name:$v['name'] state:$v['state'] sort:$v['sort']<br>";
    }
} else {
    echo '❌ app_version 表不存在';
}
