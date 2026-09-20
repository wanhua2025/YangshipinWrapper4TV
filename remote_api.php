<?php
require __DIR__ . '/include/class/RuyiParser.php';
require __DIR__ . '/include/class/RuyiMerger.php';

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

$action = isset($_GET['action']) ? $_GET['action'] : 'status';

function jsonOut($code, $data = null, $msg = '') {
    echo json_encode(['code' => $code, 'msg' => $msg, 'data' => $data], JSON_UNESCAPED_UNICODE);
    exit;
}

function getConfig() {
    $path = __DIR__ . '/ruyi.json';
    if (!file_exists($path)) {
        return ['version' => '1.0', 'sources' => []];
    }
    $cfg = json_decode(file_get_contents($path), true);
    return $cfg ?: ['version' => '1.0', 'sources' => []];
}

function saveConfig($cfg) {
    file_put_contents(__DIR__ . '/ruyi.json', json_encode($cfg, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
}

if ($action === 'status') {
    $cfg = getConfig();
    $channelFile = __DIR__ . '/channel.json';
    $channelCount = 0;
    $genTime = 0;
    if (file_exists($channelFile)) {
        $ch = json_decode(file_get_contents($channelFile), true);
        if (is_array($ch)) {
            $channelCount = isset($ch['channels']) ? count($ch['channels']) : 0;
            $genTime = isset($ch['generated_at']) ? $ch['generated_at'] : 0;
        }
    }
    jsonOut(200, [
        'version' => isset($cfg['version']) ? $cfg['version'] : '1.0',
        'sources' => count($cfg['sources']),
        'channels_count' => $channelCount,
        'generated_at' => $genTime,
        'epg_url' => isset($cfg['epg_url']) ? $cfg['epg_url'] : ''
    ]);
}

if ($action === 'list') {
    $cfg = getConfig();
    $list = [];
    foreach ($cfg['sources'] as $s) {
        $list[] = [
            'id' => $s['id'],
            'name' => $s['name'],
            'type' => $s['type'],
            'url' => isset($s['url']) ? $s['url'] : '',
            'file' => isset($s['file']) ? $s['file'] : '',
            'format' => isset($s['format']) ? $s['format'] : '',
            'enabled' => isset($s['enabled']) ? ($s['enabled'] ? 1 : 0) : 1,
            'state' => isset($s['enabled']) ? ($s['enabled'] ? 1 : 0) : 1,
        ];
    }
    jsonOut(200, ['sources' => $list], 'ok');
}

if ($action === 'sync') {
    $cfg = getConfig();
    $enabledSources = array_values(array_filter($cfg['sources'], function($s) {
        return isset($s['enabled']) && $s['enabled'];
    }));
    if (count($enabledSources) === 0) {
        jsonOut(201, null, 'no enabled sources');
    }
    try {
        $result = RuyiMerger::mergeSources($enabledSources);
        $channelList = RuyiMerger::buildChannelList($result['merged']);
        RuyiMerger::writeAll($channelList, $result['epg_url'], $cfg['sources']);
        jsonOut(200, [
            'channels_count' => count($channelList),
            'sources_count' => count($enabledSources),
            'epg_url' => $result['epg_url']
        ], 'sync done');
    } catch (Exception $e) {
        jsonOut(201, null, 'sync error: ' . $e->getMessage());
    }
}

if ($action === 'sources') {
    $chFile = __DIR__ . '/channel.json';
    if (!file_exists($chFile)) {
        jsonOut(201, null, 'channel.json not found, please run sync first');
    }
    header('Content-Type: application/json; charset=utf-8');
    echo file_get_contents($chFile);
    exit;
}

if ($action === 'add_source') {
    $name = isset($_POST['name']) ? trim($_POST['name']) : '';
    $url = isset($_POST['url']) ? trim($_POST['url']) : '';
    $type = isset($_POST['type']) ? $_POST['type'] : 'remote';
    $format = isset($_POST['format']) ? $_POST['format'] : 'auto';
    if ($name === '') jsonOut(201, null, 'name required');
    if ($type === 'remote' && $url === '') jsonOut(201, null, 'url required');
    $cfg = getConfig();
    $id = 'src_' . substr(md5($name . microtime(true)), 0, 10);
    $newSource = [
        'id' => $id,
        'name' => $name,
        'type' => $type,
        'format' => $format,
        'enabled' => true,
        'priority' => count($cfg['sources']) + 1
    ];
    if ($type === 'remote') {
        $newSource['url'] = $url;
    } else {
        $newSource['file'] = isset($_POST['file']) ? $_POST['file'] : '';
    }
    $cfg['sources'][] = $newSource;
    saveConfig($cfg);
    jsonOut(200, ['id' => $id], '添加成功');
}

if ($action === 'del_source') {
    $id = isset($_POST['id']) ? $_POST['id'] : (isset($_GET['id']) ? $_GET['id'] : '');
    if (!$id) jsonOut(201, null, 'id required');
    $cfg = getConfig();
    $found = false;
    $cfg['sources'] = array_values(array_filter($cfg['sources'], function($s) use ($id, &$found) {
        if ($s['id'] === $id) { $found = true; return false; }
        return true;
    }));
    if (!$found) jsonOut(201, null, 'source not found');
    saveConfig($cfg);
    jsonOut(200, null, '删除成功');
}

if ($action === 'toggle_source') {
    $id = isset($_POST['id']) ? $_POST['id'] : (isset($_GET['id']) ? $_GET['id'] : '');
    $state = isset($_POST['state']) ? intval($_POST['state']) : -1;
    if (!$id) jsonOut(201, null, 'id required');
    $cfg = getConfig();
    $found = false;
    foreach ($cfg['sources'] as &$s) {
        if ($s['id'] === $id) {
            if ($state >= 0) {
                $s['enabled'] = ($state == 1);
            } else {
                $s['enabled'] = !$s['enabled'];
            }
            $found = true;
            break;
        }
    }
    unset($s);
    if (!$found) jsonOut(201, null, 'source not found');
    saveConfig($cfg);
    jsonOut(200, null, '切换成功');
}

if ($action === 'upload_file' || $action === 'upload') {
    if (!isset($_FILES['file']) || $_FILES['file']['error'] !== UPLOAD_ERR_OK) {
        $err = isset($_FILES['file']) ? $_FILES['file']['error'] : 'no file';
        jsonOut(201, null, 'upload error: ' . $err);
    }
    $file = $_FILES['file'];
    $ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    $allow = ['m3u', 'txt', 'xml'];
    if (!in_array($ext, $allow)) jsonOut(201, null, 'only m3u/txt/xml allowed');
    $importsDir = __DIR__ . '/imports';
    if (!is_dir($importsDir)) @mkdir($importsDir, 0755, true);
    $fileName = date('Ymd_His') . '_' . preg_replace('/[^a-zA-Z0-9_.-]/', '_', $file['name']);
    $dest = $importsDir . '/' . $fileName;
    if (!move_uploaded_file($file['tmp_name'], $dest)) {
        jsonOut(201, null, 'save failed');
    }
    $name = pathinfo($file['name'], PATHINFO_FILENAME);
    $cfg = getConfig();
    $id = 'src_' . substr(md5($name . microtime(true)), 0, 10);
    $cfg['sources'][] = [
        'id' => $id,
        'name' => '本地: ' . $name,
        'type' => 'local',
        'format' => $ext,
        'file' => $fileName,
        'enabled' => true,
        'priority' => count($cfg['sources']) + 1
    ];
    saveConfig($cfg);
    jsonOut(200, ['file' => $fileName, 'id' => $id], '上传成功并已添加到订阅源');
}

jsonOut(201, null, 'unknown action: ' . $action);