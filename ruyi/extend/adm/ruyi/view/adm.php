<?php
/*
Sort:1
Hidden:false
Name:节目源管理
Url:ruyi_adm
Right:
Version:1.0
*/
if(!isset($islogin))header("Location: /");
require_once FCPATH.'include/class/RuyiParser.php';
require_once FCPATH.'include/class/RuyiMerger.php';

function getRuyiCfg() {
    $path = FCPATH . 'ruyi.json';
    if (!file_exists($path)) return ['version' => '1.0', 'sources' => [], 'epg_url' => ''];
    $cfg = json_decode(file_get_contents($path), true);
    return $cfg ?: ['version' => '1.0', 'sources' => []];
}
function saveRuyiCfg($cfg) {
    file_put_contents(FCPATH . 'ruyi.json', json_encode($cfg, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
}

$cfg = getRuyiCfg();
$action = isset($_GET['act']) ? $_GET['act'] : '';

if ($action === 'sync') {
    $enabled = array_filter($cfg['sources'], function($s) { return isset($s['enabled']) && $s['enabled']; });
    $result = RuyiMerger::mergeSources($enabled);
    $channelList = RuyiMerger::buildChannelList($result['merged']);
    RuyiMerger::writeAll($channelList, $result['epg_url'], $cfg['sources']);
    echo "<script>alert('同步完成！频道数：".count($channelList)."');location.href='./?ruyi_adm';</script>";
    exit;
}

if ($action === 'add') {
    $cfg['sources'][] = [
        'id' => 'src_' . substr(md5(time().rand()), 0, 10),
        'name' => isset($_POST['name']) ? trim($_POST['name']) : '',
        'type' => isset($_POST['type']) ? $_POST['type'] : 'remote',
        'format' => isset($_POST['format']) ? $_POST['format'] : 'm3u',
        'url' => isset($_POST['url']) ? trim($_POST['url']) : '',
        'file' => isset($_POST['file']) ? $_POST['file'] : '',
        'enabled' => true,
        'priority' => count($cfg['sources']) + 1,
    ];
    saveRuyiCfg($cfg);
    echo "<script>location.href='./?ruyi_adm';</script>";
    exit;
}

if ($action === 'del') {
    $id = $_GET['id'];
    $cfg['sources'] = array_values(array_filter($cfg['sources'], function($s) use ($id) { return $s['id'] !== $id; }));
    saveRuyiCfg($cfg);
    echo "<script>location.href='./?ruyi_adm';</script>";
    exit;
}

if ($action === 'toggle') {
    $id = $_GET['id'];
    foreach ($cfg['sources'] as &$s) { if ($s['id'] === $id) { $s['enabled'] = !$s['enabled']; break; } }
    unset($s);
    saveRuyiCfg($cfg);
    echo "<script>location.href='./?ruyi_adm';</script>";
    exit;
}

if ($action === 'upload' && $_FILES && isset($_FILES['ruyi_file'])) {
    $f = $_FILES['ruyi_file'];
    if ($f['error'] === UPLOAD_ERR_OK) {
        if (!is_dir(FCPATH . 'imports')) @mkdir(FCPATH . 'imports', 0755, true);
        $dest = FCPATH . 'imports/' . date('Ymd_His') . '_' . basename($f['name']);
        move_uploaded_file($f['tmp_name'], $dest);
        $ext = strtolower(pathinfo($f['name'], PATHINFO_EXTENSION));
        $name = pathinfo($f['name'], PATHINFO_FILENAME);
        $cfg['sources'][] = [
            'id' => 'src_' . substr(md5(time().rand()), 0, 10),
            'name' => '本地: ' . $name,
            'type' => 'local',
            'format' => in_array($ext, ['m3u','txt','xml']) ? $ext : 'm3u',
            'url' => '',
            'file' => basename($dest),
            'enabled' => true,
            'priority' => count($cfg['sources']) + 1,
        ];
        saveRuyiCfg($cfg);
        echo "<script>alert('上传成功！');location.href='./?ruyi_adm';</script>";
        exit;
    }
}

$channelCount = 0;
$genTime = 0;
if (file_exists(FCPATH . 'channel.json')) {
    $ch = json_decode(file_get_contents(FCPATH . 'channel.json'), true);
    if (is_array($ch)) { $channelCount = isset($ch['channels']) ? count($ch['channels']) : 0; $genTime = isset($ch['generated_at']) ? $ch['generated_at'] : 0; }
}
?>

<div class="row">
    <div class="col-12">
        <div class="page-title-box">
            <h4 class="page-title">节目源管理</h4>
            <ol class="breadcrumb m-0">
                <li class="breadcrumb-item"><a href="javascript: void(0);">首页</a></li>
                <li class="breadcrumb-item active"><?php echo $title; ?></li>
            </ol>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-md-3">
        <div class="card mini-stat bg-primary text-white">
            <div class="card-body">
                <div class="mb-3">
                    <h6 class="text-uppercase mb-0">订阅源数量</h6>
                </div>
                <div class="row">
                    <div class="col-12">
                        <h2 class="mb-0"><?php echo count($cfg['sources']); ?></h2>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card mini-stat bg-success text-white">
            <div class="card-body">
                <div class="mb-3">
                    <h6 class="text-uppercase mb-0">合并频道数</h6>
                </div>
                <div class="row">
                    <div class="col-12">
                        <h2 class="mb-0"><?php echo $channelCount; ?></h2>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-body text-center">
                <a href="./?ruyi_adm&act=sync" class="btn btn-lg btn-primary"><i class="mdi mdi-sync"></i> 一键同步所有订阅源</a>
                <span class="text-muted ml-3">上次同步: <?php echo $genTime ? date('Y-m-d H:i:s', $genTime) : '从未'; ?></span>
            </div>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span class="font-weight-bold">订阅源列表</span>
                <button class="btn btn-sm btn-success" data-toggle="modal" data-target="#addModal"><i class="mdi mdi-plus"></i> 添加远程源</button>
                <button class="btn btn-sm btn-info" data-toggle="modal" data-target="#uploadModal"><i class="mdi mdi-upload"></i> 上传本地文件</button>
            </div>
            <div class="card-body">
                <table class="table table-bordered table-hover">
                    <thead class="table-light">
                        <tr>
                            <th style="width:40px">#</th>
                            <th>名称</th>
                            <th style="width:80px">类型</th>
                            <th style="width:80px">格式</th>
                            <th>地址/文件</th>
                            <th style="width:80px">状态</th>
                            <th style="width:200px">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                    <?php if (empty($cfg['sources'])): ?>
                        <tr><td colspan="7" class="text-center text-muted py-4">暂无订阅源，请先添加</td></tr>
                    <?php else: ?>
                        <?php foreach ($cfg['sources'] as $idx => $s): ?>
                        <tr>
                            <td><?php echo $idx+1; ?></td>
                            <td><?php echo htmlspecialchars($s['name']); ?></td>
                            <td><span class="badge badge-<?php echo $s['type']==='remote'?'primary':'secondary'; ?>"><?php echo $s['type']==='remote'?'远程':'本地'; ?></span></td>
                            <td><code><?php echo isset($s['format'])?$s['format']:'m3u'; ?></code></td>
                            <td class="text-truncate" style="max-width:300px">
                                <?php echo $s['type']==='remote' ? htmlspecialchars($s['url']) : htmlspecialchars($s['file']); ?>
                            </td>
                            <td>
                                <a href="./?ruyi_adm&act=toggle&id=<?php echo $s['id']; ?>"
                                   class="badge badge-<?php echo !empty($s['enabled'])?'success':'secondary'; ?>"><?php echo !empty($s['enabled'])?'启用':'禁用'; ?></a>
                            </td>
                            <td>
                                <a href="./?ruyi_adm&act=del&id=<?php echo $s['id']; ?>" class="btn btn-sm btn-danger" onclick="return confirm('确定删除?')"><i class="mdi mdi-delete"></i></a>
                            </td>
                        </tr>
                        <?php endforeach; ?>
                    <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- 当前线路列表 -->
<?php
$channels = [];
$channelFile = FCPATH . 'channel.json';
if (file_exists($channelFile)) {
    $chData = json_decode(file_get_contents($channelFile), true);
    if (is_array($chData) && isset($chData['channels'])) {
        $channels = $chData['channels'];
    }
}
?>
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header font-weight-bold d-flex justify-content-between align-items-center">
                <span>当前线路列表（共 <?php echo count($channels); ?> 个频道）</span>
                <div>
                    <a href="./?ruyi_adm&act=sync" class="btn btn-sm btn-primary"><i class="mdi mdi-sync"></i> 同步更新</a>
                </div>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered table-hover table-sm">
                        <thead class="table-light">
                            <tr>
                                <th style="width:40px">#</th>
                                <th>频道名称</th>
                                <th>可用线路</th>
                                <th style="width:120px">状态</th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php if (empty($channels)): ?>
                            <tr><td colspan="4" class="text-center text-muted py-4">暂无频道数据，请先添加订阅源并同步</td></tr>
                        <?php else: ?>
                            <?php foreach ($channels as $cidx => $ch): ?>
                            <tr>
                                <td><?php echo $cidx+1; ?></td>
                                <td><?php echo htmlspecialchars(isset($ch['name'])?$ch['name']:'未知'); ?></td>
                                <td>
                                    <?php
                                    $urls = isset($ch['urls']) ? $ch['urls'] : [];
                                    if (!empty($urls)) {
                                        foreach ($urls as $ui => $u) {
                                            $label = '线路' . ($ui+1);
                                            if (isset($u['from'])) $label = $u['from'];
                                            $urlStr = isset($u['url']) ? $u['url'] : '';
                                            echo '<span class="badge badge-info mr-1" title="'.htmlspecialchars($urlStr).'">'.$label.'</span> ';
                                        }
                                    } else {
                                        echo '<span class="text-muted">无可用线路</span>';
                                    }
                                    ?>
                                </td>
                                <td>
                                    <?php
                                    $urlCount = !empty($urls) ? count($urls) : 0;
                                    if ($urlCount > 0) {
                                        echo '<span class="badge badge-success">可用</span>';
                                    } else {
                                        echo '<span class="badge badge-danger">失效</span>';
                                    }
                                    ?>
                                </td>
                            </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header font-weight-bold">公开 API 地址</div>
            <div class="card-body">
                <table class="table table-sm">
                    <tr><td><code>/ruyi/interface.m3u</code></td><td>M3U 格式合并播放列表（标准，播放器可直接用）</td></tr>
                    <tr><td><code>/ruyi/txt</code></td><td>TXT 格式合并播放列表</td></tr>
                    <tr><td><code>/ruyi/channel.json</code></td><td>JSON 格式（含多线路信息，推荐客户端使用）</td></tr>
                    <tr><td><code>/ruyi/ruyi.json</code></td><td>订阅源配置元信息</td></tr>
                    <tr><td><code>/ruyi/ruyi_api.php?action=sources</code></td><td>自动缓存的频道列表（同 channel.json，首次访问会触发同步）</td></tr>
                    <tr><td><code>/ruyi/ruyi_api.php?action=sync</code></td><td>触发同步所有订阅源并重建输出文件</td></tr>
                </table>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="addModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <form method="post" action="./?ruyi_adm&act=add">
                <div class="modal-header"><h5 class="modal-title">添加远程订阅源</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
                <div class="modal-body">
                    <div class="mb-3"><label class="form-label">名称</label><input type="text" name="name" class="form-control" required placeholder="例如：央视影音接口"></div>
                    <div class="mb-3"><label class="form-label">URL</label><input type="url" name="url" class="form-control" required placeholder="http://.../interface.m3u"></div>
                    <div class="mb-3">
                        <label class="form-label">格式</label>
                        <select name="format" class="form-control">
                            <option value="m3u">M3U</option>
                            <option value="txt">TXT</option>
                            <option value="xml">XML (EPG)</option>
                        </select>
                    </div>
                    <input type="hidden" name="type" value="remote">
                </div>
                <div class="modal-footer"><button type="button" class="btn btn-secondary" data-dismiss="modal">取消</button><button type="submit" class="btn btn-primary">添加</button></div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="uploadModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <form method="post" action="./?ruyi_adm&act=upload" enctype="multipart/form-data">
                <div class="modal-header"><h5 class="modal-title">上传本地文件</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">选择文件 (m3u / txt / xml)</label>
                        <input type="file" name="ruyi_file" class="form-control" required accept=".m3u,.txt,.xml">
                    </div>
                </div>
                <div class="modal-footer"><button type="button" class="btn btn-secondary" data-dismiss="modal">取消</button><button type="submit" class="btn btn-primary">上传</button></div>
            </form>
        </div>
    </div>
</div>