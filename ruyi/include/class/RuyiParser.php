<?php
class RuyiParser {

    public static function detectFormat($content) {
        if (strpos(trim($content), '#EXTM3U') === 0) return 'm3u';
        if (strpos(trim($content), '<?xml') === 0 || strpos(trim($content), '<tv') !== false) return 'xml';
        return 'txt';
    }

    public static function parseM3U($content) {
        $groups = [];
        $currentGroup = '';
        $lines = preg_split('/\r\n|\r|\n/', $content);
        $i = 0;
        $groupTitle = '';
        while ($i < count($lines)) {
            $line = trim($lines[$i]);
            if ($line === '') { $i++; continue; }
            if (strpos($line, '#EXTINF:') === 0) {
                $info = $line;
                $name = '';
                $group = $groupTitle;
                $logo = '';
                $tvgId = '';
                if (preg_match('/group-title="([^"]*)"/', $info, $gm)) $group = $gm[1];
                if (preg_match('/tvg-logo="([^"]*)"/', $info, $lm)) $logo = $lm[1];
                if (preg_match('/tvg-id="([^"]*)"/', $info, $tm)) $tvgId = $tm[1];
                $lastComma = strrpos($info, ',');
                if ($lastComma !== false) $name = trim(substr($info, $lastComma + 1));
                if ($name === '' && preg_match('/,([^,]*)$/', $info, $nm)) $name = trim($nm[1]);
                $i++;
                if ($i < count($lines)) {
                    $url = trim($lines[$i]);
                    if ($url !== '' && strpos($url, '#') !== 0) {
                        $groups[$group][] = [
                            'name' => $name,
                            'url' => $url,
                            'logo' => $logo,
                            'tvg_id' => $tvgId,
                        ];
                    }
                }
            } elseif (strpos($line, '#EXTM3U') === 0) {
                if (preg_match('/x-tvg-url="([^"]*)"/', $line, $em)) {
                    $_SESSION['ruyi_epg_url'] = $em[1];
                }
            } elseif (strpos($line, '#EXTGRP:') === 0) {
                $groupTitle = trim(substr($line, 8));
            }
            $i++;
        }
        return $groups;
    }

    public static function parseTXT($content) {
        $groups = [];
        $currentGroup = '';
        $lines = preg_split('/\r\n|\r|\n/', $content);
        foreach ($lines as $line) {
            $line = trim($line);
            if ($line === '' || strpos($line, '#') === 0) continue;
            if (strpos($line, '#genre#') !== false) {
                $currentGroup = trim(str_replace('#genre#', '', $line));
                continue;
            }
            $parts = explode(',', $line, 2);
            if (count($parts) >= 2) {
                $name = trim($parts[0]);
                $url = trim($parts[1]);
                if ($name !== '' && $url !== '') {
                    $groups[$currentGroup][] = [
                        'name' => $name,
                        'url' => $url,
                        'logo' => '',
                        'tvg_id' => '',
                    ];
                }
            }
        }
        return $groups;
    }

    public static function parse($content, $format = null) {
        if ($format === null) $format = self::detectFormat($content);
        if ($format === 'm3u') return self::parseM3U($content);
        return self::parseTXT($content);
    }

    public static function groupsToFlat($groups) {
        $result = [];
        foreach ($groups as $group => $channels) {
            foreach ($channels as $ch) {
                $ch['group'] = $group;
                $result[] = $ch;
            }
        }
        return $result;
    }
}