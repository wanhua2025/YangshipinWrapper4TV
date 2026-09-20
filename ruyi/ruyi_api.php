<?php
require __DIR__ . '/include/class/RuyiParser.php';
require __DIR__ . '/include/class/RuyiMerger.php';

header('Content-Type: application/json; charset=utf-8');

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

if ($action === 'sync') {
    $cfg = getConfig();
    $enabledSources = array_filter($cfg['sources'], function($s) { return isset($s['enabled']) && $s['enabled']; });
    $result = RuyiMerger::mergeSources($enabledSources);
    $channelList = RuyiMerger::buildChannelList($result['merged']);
    RuyiMerger::writeAll($channelList, $result['epg_url'], $cfg['sources']);
    jsonOut(200, ['channels' => count($channelList), 'sources' => count($enabledSources), 'epg_url' => $result['epg_url']], 'sync done');
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
        'version' => $cfg['version'],
        'sources' => count($cfg['sources']),
        'sources_detail' => array_map(function($s) {
            return ['id' => $s['id'], 'name' => $s['name'], 'type' => $s['type'], 'enabled' => $s['enabled']];
        }, $cfg['sources']),
        'channels_count' => $channelCount,
        'generated_at' => $genTime,
        'epg_url' => isset($cfg['epg_url']) ? $cfg['epg_url'] : '',
    ]);
}

if ($action === 'sources') {
    $cfg = getConfig();
    $enabledSources = array_filter($cfg['sources'], function($s) { return isset($s['enabled']) && $s['enabled']; });
    $chFile = __DIR__ . '/channel.json';
    if (!file_exists($chFile) || filemtime($chFile) < time() - 3600) {
        $result = RuyiMerger::mergeSources($enabledSources);
        $channelList = RuyiMerger::buildChannelList($result['merged']);
        RuyiMerger::writeAll($channelList, $result['epg_url'], $cfg['sources']);
    }
    $data = json_decode(file_get_contents($chFile), true);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'add_source') {
    $name = isset($_POST['name']) ? trim($_POST['name']) : '';
    $url = isset($_POST['url']) ? trim($_POST['url']) : '';
    $type = isset($_POST['type']) ? $_POST['type'] : 'remote';
    $format = isset($_POST['format']) ? $_POST['format'] : 'm3u';
    $enabled = isset($_POST['enabled']) ? intval($_POST['enabled']) : 1;
    $file = isset($_POST['file']) ? $_POST['file'] : '';

    if ($name === '') jsonOut(201, null, 'name required');
    if ($type === 'remote' && $url === '') jsonOut(201, null, 'url required');
    if ($type === 'local' && $file === '') jsonOut(201, null, 'file required');

    $cfg = getConfig();
    $id = 'src_' . substr(md5($name . time()), 0, 10);
    $cfg['sources'][] = [
        'id' => $id,
        'name' => $name,
        'type' => $type,
        'format' => $format,
        'url' => $url,
        'file' => $file,
        'enabled' => $enabled ? true : false,
        'priority' => count($cfg['sources']) + 1,
    ];
    saveConfig($cfg);
    jsonOut(200, ['id' => $id], 'added');
}

if ($action === 'del_source') {
    $id = isset($_POST['id']) ? $_POST['id'] : $_GET['id'];
    if (!$id) jsonOut(201, null, 'id required');
    $cfg = getConfig();
    $cfg['sources'] = array_values(array_filter($cfg['sources'], function($s) use ($id) { return $s['id'] !== $id; }));
    saveConfig($cfg);
    jsonOut(200, null, 'deleted');
}

if ($action === 'toggle_source') {
    $id = isset($_POST['id']) ? $_POST['id'] : $_GET['id'];
    if (!$id) jsonOut(201, null, 'id required');
    $cfg = getConfig();
    foreach ($cfg['sources'] as &$s) {
        if ($s['id'] === $id) {
            $s['enabled'] = !$s['enabled'];
            break;
        }
    }
    unset($s);
    saveConfig($cfg);
    jsonOut(200, null, 'toggled');
}

if ($action === 'upload') {
    if (!isset($_FILES['file']) || $_FILES['file']['error'] !== UPLOAD_ERR_OK) {
        jsonOut(201, null, 'upload failed');
    }
    $file = $_FILES['file'];
    $ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    $allow = ['m3u', 'txt', 'xml'];
    if (!in_array($ext, $allow)) jsonOut(201, null, 'only m3u/txt/xml allowed');
    $dest = __DIR__ . '/imports/' . date('Ymd_His') . '_' . basename($file['name']);
    move_uploaded_file($file['tmp_name'], $dest);
    $fileName = basename($dest);
    jsonOut(200, ['file' => $fileName], 'uploaded');
}

jsonOut(201, null, 'unknown action');