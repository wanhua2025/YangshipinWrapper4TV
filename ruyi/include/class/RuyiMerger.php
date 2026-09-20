<?php
class RuyiMerger {

    public static function basePath() {
        if (defined('FCPATH')) return FCPATH;
        return str_replace('\\', '/', dirname(dirname(__DIR__))) . '/';
    }

    public static function fetchSource($url, $timeout = 10) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
        curl_setopt($ch, CURLOPT_TIMEOUT, $timeout);
        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
        curl_setopt($ch, CURLOPT_USERAGENT, 'RuyiTV/1.0');
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        $content = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $error = curl_error($ch);
        curl_close($ch);
        if ($httpCode === 200 && $content !== false) return ['ok' => true, 'content' => $content, 'code' => $httpCode];
        return ['ok' => false, 'code' => $httpCode, 'error' => $error];
    }

    public static function mergeSources($sources) {
        $merged = [];
        $epgUrl = '';
        foreach ($sources as $source) {
            $content = '';
            if ($source['type'] === 'remote') {
                $result = self::fetchSource($source['url']);
                if (!$result['ok']) continue;
                $content = $result['content'];
                @file_put_contents(self::basePath() . 'feeds/' . $source['id'] . '.txt', $content);
            } elseif ($source['type'] === 'local') {
                $path = self::basePath() . 'imports/' . $source['file'];
                if (!file_exists($path)) continue;
                $content = file_get_contents($path);
            }
            if ($content === '') continue;
            $format = isset($source['format']) ? $source['format'] : null;
            $groups = RuyiParser::parse($content, $format);
            if (isset($_SESSION['ruyi_epg_url']) && $_SESSION['ruyi_epg_url'] !== '') {
                $epgUrl = $_SESSION['ruyi_epg_url'];
                unset($_SESSION['ruyi_epg_url']);
            }
            foreach ($groups as $group => $channels) {
                foreach ($channels as $ch) {
                    $key = self::normalizeName($ch['name']);
                    if (!isset($merged[$key])) {
                        $merged[$key] = [
                            'name' => $ch['name'],
                            'group' => $group,
                            'logo' => $ch['logo'],
                            'tvg_id' => $ch['tvg_id'],
                            'sources' => [],
                        ];
                    }
                    $already = false;
                    foreach ($merged[$key]['sources'] as $s) {
                        if ($s['url'] === $ch['url']) { $already = true; break; }
                    }
                    if (!$already) {
                        $merged[$key]['sources'][] = [
                            'url' => $ch['url'],
                            'source_id' => $source['id'],
                        ];
                    }
                }
            }
        }
        if ($epgUrl !== '') {
            $cfgPath = self::basePath() . 'ruyi.json';
            $cfg = [];
            if (file_exists($cfgPath)) {
                $d = json_decode(file_get_contents($cfgPath), true);
                if (is_array($d)) $cfg = $d;
            }
            $cfg['epg_url'] = $epgUrl;
            file_put_contents($cfgPath, json_encode($cfg, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
        }
        return ['merged' => $merged, 'epg_url' => $epgUrl];
    }

    public static function normalizeName($name) {
        $name = preg_replace('/\s+/', '', $name);
        $name = str_replace(['高清', 'HD', '流畅', '标清', '4K', '8K', '超清'], '', $name);
        return $name;
    }

    public static function buildChannelList($merged) {
        $result = [];
        foreach ($merged as $key => $ch) {
            $result[] = [
                'name' => $ch['name'],
                'group' => $ch['group'],
                'logo' => $ch['logo'],
                'tvg_id' => $ch['tvg_id'],
                'sources' => $ch['sources'],
                'main_url' => isset($ch['sources'][0]) ? $ch['sources'][0]['url'] : '',
            ];
        }
        usort($result, function($a, $b) {
            if ($a['group'] !== $b['group']) return strcmp($a['group'], $b['group']);
            return strcmp($a['name'], $b['name']);
        });
        return $result;
    }

    public static function buildM3U($channelList, $epgUrl = '') {
        $header = '#EXTM3U';
        if ($epgUrl !== '') $header .= ' x-tvg-url="' . $epgUrl . '"';
        $lines = [$header];
        foreach ($channelList as $ch) {
            $attrs = '';
            if ($ch['tvg_id'] !== '') $attrs .= ' tvg-id="' . $ch['tvg_id'] . '"';
            if ($ch['tvg_id'] !== '') $attrs .= ' tvg-name="' . $ch['tvg_id'] . '"';
            if ($ch['logo'] !== '') $attrs .= ' tvg-logo="' . $ch['logo'] . '"';
            $attrs .= ' group-title="' . $ch['group'] . '"';
            $lines[] = '#EXTINF:-1' . $attrs . ',' . $ch['name'];
            $lines[] = $ch['main_url'];
        }
        return implode("\n", $lines);
    }

    public static function buildTXT($channelList) {
        $lines = [];
        $lastGroup = '';
        foreach ($channelList as $ch) {
            if ($ch['group'] !== $lastGroup) {
                $lines[] = $ch['group'] . '#genre#';
                $lastGroup = $ch['group'];
            }
            $lines[] = $ch['name'] . ',' . $ch['main_url'];
        }
        return implode("\n", $lines);
    }

    public static function writeAll($channelList, $epgUrl, $sources) {
        self::ensureDirs();
        $base = self::basePath();
        $json = [
            'version' => '1.0',
            'generated_at' => time(),
            'epg_url' => $epgUrl,
            'channels' => $channelList,
            'sources_config' => $sources,
        ];
        file_put_contents($base . 'channel.json', json_encode($json, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
        file_put_contents($base . 'interface.m3u', self::buildM3U($channelList, $epgUrl));
        file_put_contents($base . 'txt', self::buildTXT($channelList));
        self::updateRuyiJsonMeta($epgUrl, count($channelList));
    }

    public static function ensureDirs() {
        $base = self::basePath();
        if (!is_dir($base . 'feeds')) @mkdir($base . 'feeds', 0755, true);
        if (!is_dir($base . 'imports')) @mkdir($base . 'imports', 0755, true);
    }

    public static function updateRuyiJsonMeta($epgUrl, $count) {
        $base = self::basePath();
        $cfg = [];
        $path = $base . 'ruyi.json';
        if (file_exists($path)) {
            $decoded = json_decode(file_get_contents($path), true);
            if (is_array($decoded)) $cfg = $decoded;
        }
        $cfg['epg_url'] = $epgUrl;
        $cfg['channels_count'] = $count;
        $cfg['generated_at'] = time();
        file_put_contents($path, json_encode($cfg, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    }
}