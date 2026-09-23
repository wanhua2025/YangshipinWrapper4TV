<?php
require_once __DIR__ . '/include/global.php';
$app = Db::table('app')->where(['id'=>10000])->find();
if($app){
    echo '<pre>APP配置 (ID=10000):'.chr(10);
    foreach($app as $k=>$v){
        echo "  $k = $v".chr(10);
    }
    echo '</pre>';
}else{
    echo 'APP ID=10000 不存在!'.chr(10);
    echo '所有 APP:'.chr(10);
    $apps = Db::table('app')->select();
    foreach($apps as $a){ echo "  ID:$a[id] name:$a[name]".chr(10); }
}
