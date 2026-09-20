<?php
if(!isset($islogin))header("Location: /");
if($act == 'apk' || $act == 'upload_apk'){
    if(!$_FILES || !isset($_FILES['apkfile'])){
        echo json_encode(['code'=>201, 'msg'=>'No file uploaded']);exit;
    }
    $file = $_FILES['apkfile'];
    if($file['error'] != 0){
        echo json_encode(['code'=>201, 'msg'=>'Upload error code:'.$file['error']]);exit;
    }
    $maxSize = 500 * 1024 * 1024;
    if($file['size'] > $maxSize){
        echo json_encode(['code'=>201, 'msg'=>'File too large (max 500MB)']);exit;
    }
    $ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    if($ext != 'apk'){
        echo json_encode(['code'=>201, 'msg'=>'Only APK files allowed']);exit;
    }
    $root = FCPATH;
    if(!is_dir($root.'/upload')){
        @mkdir($root.'/upload', 0755, true);
    }
    $uploadDir = $root.'/upload/apk';
    if(!is_dir($uploadDir)){
        @mkdir($uploadDir, 0755, true);
        @chmod($uploadDir, 0755);
    }
    if(!is_writable($uploadDir)){
        @chmod($uploadDir, 0777);
        if(!is_writable($uploadDir)){
            echo json_encode(['code'=>201, 'msg'=>'Directory not writable: '.$uploadDir.' Please chmod 777 or check owner. PHP user: '.get_current_user().' Root: '.$root]);exit;
        }
    }
    $appid = isset($_POST['appid']) ? intval($_POST['appid']) : 0;
    if($appid <= 0){
        echo json_encode(['code'=>201, 'msg'=>'App ID missing']);exit;
    }
    $appname = isset($_POST['appname']) ? trim($_POST['appname']) : '';
    $version = isset($_POST['version']) ? trim($_POST['version']) : '';
    $appname = preg_replace('/[^a-zA-Z0-9_\-]/', '', strtolower($appname));
    $version = preg_replace('/[^a-zA-Z0-9_.]/', '', $version);
    if($appname && $version){
        $filename = $appname.'_v'.$version.'.apk';
    } elseif($version) {
        $filename = 'app_v'.$version.'.apk';
    } else {
        $filename = 'app_'.$appid.'.apk';
    }
    $target = $uploadDir.'/'.$filename;
    if(file_exists($target)) @unlink($target);
    if(!move_uploaded_file($file['tmp_name'], $target)){
        $err = error_get_last();
        echo json_encode(['code'=>201, 'msg'=>'Save failed. target='.$target.' error='.($err?$err['message']:'unknown').' uploadDir writable='.(is_writable($uploadDir)?'yes':'no')]);exit;
    }
    @chmod($target, 0644);
    $proto = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? 'https' : 'http';
    $host = $_SERVER['HTTP_HOST'];
    $url = $proto.'://'.$host.'/upload/apk/'.$filename;
    @Db::table('app')->where('id', $appid)->update(['android_url'=>$url, 'downloadtype1'=>'0']);
    echo json_encode(['code'=>200, 'msg'=>'Upload OK', 'url'=>$url, 'size'=>$file['size'], 'filename'=>$filename]);
    exit;
}
echo json_encode(['code'=>201, 'msg'=>'No such action:'.$act]);
?>