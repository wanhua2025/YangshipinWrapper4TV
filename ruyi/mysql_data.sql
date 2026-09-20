-- MySQL dump 10.13  Distrib 5.6.50, for Linux (x86_64)
--
-- Host: localhost    Database: kaiyuan_ruyi
-- ------------------------------------------------------
-- Server version	5.6.50-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `yyys_ad`
--

DROP TABLE IF EXISTS `yyys_ad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_ad` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `url` text NOT NULL COMMENT '广告地址',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_ad`
--

LOCK TABLES `yyys_ad` WRITE;
/*!40000 ALTER TABLE `yyys_ad` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_ad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_analysis`
--

DROP TABLE IF EXISTS `yyys_analysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_analysis` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '标识名称',
  `keyword` varchar(255) NOT NULL COMMENT '线路',
  `url` varchar(255) DEFAULT '' COMMENT '接口id',
  `header` varchar(10000) DEFAULT '' COMMENT 'Header',
  `Client` int(1) DEFAULT '0' COMMENT 'ClientID',
  `state` enum('0','1') DEFAULT '0' COMMENT '状态',
  `Core` enum('99','4','3','2','1','0') DEFAULT '99',
  `Ad_block` enum('1','0') DEFAULT '0',
  `position` enum('6','5','4','3','2','1','0') DEFAULT '0',
  `Safe` enum('1','0') DEFAULT '0',
  `Ewmsize` enum('350','300','250','200','150','100') DEFAULT '250',
  `Ewm_Width_Height` varchar(255) NOT NULL DEFAULT '260|85',
  `Moviesize` int(11) DEFAULT '30',
  `Tvplaysize` int(11) DEFAULT '30',
  `headposition` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `name` (`name`),
  KEY `keyword` (`keyword`),
  KEY `url` (`url`),
  KEY `Client` (`Client`),
  KEY `state` (`state`),
  KEY `header` (`header`(333))
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_analysis`
--

LOCK TABLES `yyys_analysis` WRITE;
/*!40000 ALTER TABLE `yyys_analysis` DISABLE KEYS */;
INSERT INTO `yyys_analysis` VALUES (1,'ffm3u8','ffm3u8','','',1,'0','99','1','0','0','250','260|85',30,200,0),(2,'360zy','360zy','0','',1,'0','99','1','0','0','250','260|85',30,30,0);
/*!40000 ALTER TABLE `yyys_analysis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_analysis_connect`
--

DROP TABLE IF EXISTS `yyys_analysis_connect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_analysis_connect` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '接口名字',
  `url` varchar(255) NOT NULL COMMENT '接口地址',
  `header` varchar(10000) DEFAULT NULL COMMENT '解析前header',
  `type` enum('1','0') DEFAULT '0' COMMENT '解析类型',
  `Timeout` int(11) DEFAULT '0' COMMENT '超时时间',
  `Exclude_keywords` varchar(255) DEFAULT 'm3u8.pw' COMMENT '排除关键字',
  `Sniffing_rules` varchar(255) DEFAULT '{"m3u8":".m3u8","mp4":".mp4","flv":".flv","mkv":".mkv"}' COMMENT '嗅探规则',
  `Sniffing_header` varchar(255) DEFAULT '{"User-Agent":" Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36"}' COMMENT '嗅探前header',
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `name` (`name`),
  KEY `url` (`url`),
  KEY `type` (`type`),
  KEY `Timeout` (`Timeout`),
  KEY `header` (`header`(333)),
  KEY `Exclude_keywords` (`Exclude_keywords`),
  KEY `Sniffing_rules` (`Sniffing_rules`),
  KEY `Sniffing_header` (`Sniffing_header`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_analysis_connect`
--

LOCK TABLES `yyys_analysis_connect` WRITE;
/*!40000 ALTER TABLE `yyys_analysis_connect` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_analysis_connect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_analysis_set`
--

DROP TABLE IF EXISTS `yyys_analysis_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_analysis_set` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Weather_switch` enum('1','0') DEFAULT '0' COMMENT '天气开关',
  `Weather_Appid` varchar(255) DEFAULT '43656176' COMMENT '天气ID',
  `Weather_Appsecret` varchar(255) DEFAULT 'I42og6Lm' COMMENT '天气密钥',
  `UA` varchar(255) DEFAULT 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.125 Safari/537.36' COMMENT '全局User-Agent',
  `Encrypt` enum('1','0') DEFAULT '0' COMMENT '解析加密',
  `Analysis_log` enum('2','1','0') DEFAULT '1' COMMENT '解析日志',
  `Timeout` int(3) DEFAULT '15' COMMENT '超时时间',
  `Submission` enum('1','0') DEFAULT '0' COMMENT '提交方式',
  `Try` enum('1','0') DEFAULT '0' COMMENT '试看状态',
  `Trytime` int(11) DEFAULT '6' COMMENT '试看时间',
  `Force_Upgrade` enum('1','0') DEFAULT '0' COMMENT '应用强升',
  `Resource_Key` varchar(255) DEFAULT '' COMMENT '资源密钥',
  `Offsite_value` int(11) DEFAULT '9' COMMENT '异地阈值',
  `Risk_value` int(11) DEFAULT '4' COMMENT '风险阈值',
  `Play_Value` int(11) DEFAULT '300' COMMENT '播放阈值',
  `Overrun_Value` int(11) DEFAULT '5' COMMENT '超限阈值',
  `IP_blacklist` varchar(255) DEFAULT '' COMMENT 'IP黑名单',
  `Machine_blacklist` varchar(255) DEFAULT '' COMMENT '设备黑名单',
  `Account_does_not_exist` varchar(255) DEFAULT 'https://txmov2.a.kwimgs.com/upic/2021/12/08/18/BMjAyMTEyMDgxODU4NTNfMzM1MDU2NzNfNjIzNTg1NTIxMjBfMF8z_b_Bf178d61bbefa697d2db1bb25637fb64c.mp4?clientCacheKey=3xs2x9vrs2frgr4_b.mp4&tt=b&di=71072c02&bp=10000' COMMENT '账户不存在',
  `Account_expiration` varchar(255) DEFAULT 'https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAyMjBfMzM1MDU2NzNfNjIzNTg4MzY0NTdfMF8z_b_Bd5d08fb1475278148e58ba60554a11c7.mp4?clientCacheKey=3xz3xh6z7ngxn3y_b.mp4&tt=b&di=71072c02&bp=10000' COMMENT '账户过期',
  `Password_error` varchar(255) DEFAULT 'https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAwMzNfMzM1MDU2NzNfNjIzNTg2OTA2MTJfMF8z_b_Bf9fb1db112e680249d2593dd8f7342fd.mp4?clientCacheKey=3xviumptxqrkmu2_b.mp4&tt=b&di=71072c02&bp=10000' COMMENT '密码错误',
  `Offsite_notify` varchar(255) DEFAULT 'https://txmov2.a.kwimgs.com/upic/2022/01/22/16/BMjAyMjAxMjIxNjIxNDhfMzM1MDU2NzNfNjU0NzIzODU2MjdfMF8z_b_B0a8b2ea146d20dcc1b2a67c2057c0df2.mp4?clientCacheKey=3xgw42gv5hdu8wy_b.mp4&tt=b&di=11b80a6&bp=13380' COMMENT '异地提醒',
  `Risk_notify` varchar(255) DEFAULT 'https://alimov2.a.kwimgs.com/upic/2022/05/25/23/BMjAyMjA1MjUyMzU1MzdfMzM1MDU2NzNfNzUyNTgwNjg3NTNfMF8z_b_B481077f8ae36a5f708fb321140b4fb08.mp4?clientCacheKey=3xt6smqnwrgiabm_b.mp4&tt=b&di=75b3efda&bp=13380' COMMENT '风险提醒',
  `Hotlink_notify` varchar(255) DEFAULT '' COMMENT '盗链提醒',
  `Ban_notify` varchar(255) DEFAULT 'https://alimov2.a.kwimgs.com/upic/2022/03/19/18/BMjAyMjAzMTkxODI5NDRfMzM1MDU2NzNfNjk5OTk2NjEzMjNfMF8z_b_Bc628c5389c2892e81c9f5549f9a9af11.mp4?clientCacheKey=3xswm6n97vd3njm_b.mp4&tt=b&di=71072c29&bp=13380' COMMENT '封禁提醒',
  `Overrun_notify` varchar(255) DEFAULT 'https://alimov2.a.kwimgs.com/upic/2022/03/31/02/BMjAyMjAzMzEwMjE2MzVfMzM1MDU2NzNfNzA4NDU2OTAxMjRfMF8z_b_B65b1642f5834806432518b0b36ecf990.mp4?clientCacheKey=3xi6tj5gieddm9k_b.mp4&tt=b&di=71072c24&bp=13380' COMMENT '超限提醒',
  `Version_high` varchar(255) DEFAULT 'https://txmov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU4MjVfMzM1MDU2NzNfNzQ2NTU5NTQ2OTlfMF8z_b_B328e5c4d5779067aaeed496cd5c8e96b.mp4?clientCacheKey=3xr9sd65rfd5ism_b.mp4&tt=b&di=7102fc1b&bp=13380' COMMENT '版本高',
  `Version_low` varchar(255) DEFAULT 'https://alimov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU2MDJfMzM1MDU2NzNfNzQ2NTU3NjE3MjFfMF8z_b_B3676197db6cfefd533efff26860d187a.mp4?clientCacheKey=3x5e8y6u6w6en2q_b.mp4&tt=b&di=7102fc1b&bp=13380' COMMENT '版本低',
  `Version_error` varchar(255) DEFAULT 'https://alimov2.a.kwimgs.com/upic/2022/05/17/22/BMjAyMjA1MTcyMjAwMzhfMzM1MDU2NzNfNzQ2NTYxMzA5NTNfMF8z_b_B9ebf4f3e5e70b05e3a1b3805d033ead7.mp4?clientCacheKey=3xjabdphaawdrqw_b.mp4&tt=b&di=7102fc1b&bp=13380' COMMENT '版本错误',
  `Anti_theft` enum('2','1','0') DEFAULT '1' COMMENT '防盗状态',
  `Url_blacklist` text COMMENT '屏蔽连接',
  `disconnect` varchar(255) DEFAULT 'https://www.baidu.com/diaoxian.m3u8' COMMENT '掉线提醒',
  `Log_Redis_Address` varchar(255) NOT NULL DEFAULT '127.0.0.1' COMMENT 'Log_Redis地址',
  `Log_Redis_Port` int(11) NOT NULL DEFAULT '6379' COMMENT 'Log_Redis端口',
  `Log_RedisDB` varchar(255) NOT NULL DEFAULT '2' COMMENT 'Log_RedisDB',
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_analysis_set`
--

LOCK TABLES `yyys_analysis_set` WRITE;
/*!40000 ALTER TABLE `yyys_analysis_set` DISABLE KEYS */;
INSERT INTO `yyys_analysis_set` VALUES (1,'0','43656176','I42og6Lm','Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.125 Safari/537.36','1','1',15,'0','0',6,'0','3C423A5FF0A9052ADCDFA73DBEB8EC1C',9,4,300,5,'','','https://txmov2.a.kwimgs.com/upic/2021/12/08/18/BMjAyMTEyMDgxODU4NTNfMzM1MDU2NzNfNjIzNTg1NTIxMjBfMF8z_b_Bf178d61bbefa697d2db1bb25637fb64c.mp4?clientCacheKey=3xs2x9vrs2frgr4_b.mp4&tt=b&di=71072c02&bp=10000','https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAyMjBfMzM1MDU2NzNfNjIzNTg4MzY0NTdfMF8z_b_Bd5d08fb1475278148e58ba60554a11c7.mp4?clientCacheKey=3xz3xh6z7ngxn3y_b.mp4&tt=b&di=71072c02&bp=10000','https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAwMzNfMzM1MDU2NzNfNjIzNTg2OTA2MTJfMF8z_b_Bf9fb1db112e680249d2593dd8f7342fd.mp4?clientCacheKey=3xviumptxqrkmu2_b.mp4&tt=b&di=71072c02&bp=10000','https://txmov2.a.kwimgs.com/upic/2022/01/22/16/BMjAyMjAxMjIxNjIxNDhfMzM1MDU2NzNfNjU0NzIzODU2MjdfMF8z_b_B0a8b2ea146d20dcc1b2a67c2057c0df2.mp4?clientCacheKey=3xgw42gv5hdu8wy_b.mp4&tt=b&di=11b80a6&bp=13380','https://alimov2.a.kwimgs.com/upic/2022/05/25/23/BMjAyMjA1MjUyMzU1MzdfMzM1MDU2NzNfNzUyNTgwNjg3NTNfMF8z_b_B481077f8ae36a5f708fb321140b4fb08.mp4?clientCacheKey=3xt6smqnwrgiabm_b.mp4&tt=b&di=75b3efda&bp=13380','','https://alimov2.a.kwimgs.com/upic/2022/03/19/18/BMjAyMjAzMTkxODI5NDRfMzM1MDU2NzNfNjk5OTk2NjEzMjNfMF8z_b_Bc628c5389c2892e81c9f5549f9a9af11.mp4?clientCacheKey=3xswm6n97vd3njm_b.mp4&tt=b&di=71072c29&bp=13380','https://alimov2.a.kwimgs.com/upic/2022/03/31/02/BMjAyMjAzMzEwMjE2MzVfMzM1MDU2NzNfNzA4NDU2OTAxMjRfMF8z_b_B65b1642f5834806432518b0b36ecf990.mp4?clientCacheKey=3xi6tj5gieddm9k_b.mp4&tt=b&di=71072c24&bp=13380','https://txmov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU4MjVfMzM1MDU2NzNfNzQ2NTU5NTQ2OTlfMF8z_b_B328e5c4d5779067aaeed496cd5c8e96b.mp4?clientCacheKey=3xr9sd65rfd5ism_b.mp4&tt=b&di=7102fc1b&bp=13380','https://alimov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU2MDJfMzM1MDU2NzNfNzQ2NTU3NjE3MjFfMF8z_b_B3676197db6cfefd533efff26860d187a.mp4?clientCacheKey=3x5e8y6u6w6en2q_b.mp4&tt=b&di=7102fc1b&bp=13380','https://alimov2.a.kwimgs.com/upic/2022/05/17/22/BMjAyMjA1MTcyMjAwMzhfMzM1MDU2NzNfNzQ2NTYxMzA5NTNfMF8z_b_B9ebf4f3e5e70b05e3a1b3805d033ead7.mp4?clientCacheKey=3xjabdphaawdrqw_b.mp4&tt=b&di=7102fc1b&bp=13380','1','','https://www.baidu.com/diaoxian.m3u8','127.0.0.1',6379,'2');
/*!40000 ALTER TABLE `yyys_analysis_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app`
--

DROP TABLE IF EXISTS `yyys_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT 'APP名称',
  `appkey` varchar(32) NOT NULL COMMENT 'APPKEY',
  `mode` enum('y','n') DEFAULT 'y' COMMENT '运营模式',
  `state` enum('y','n') DEFAULT 'y' COMMENT 'APP状态',
  `notice` varchar(255) DEFAULT NULL COMMENT '关闭通知',
  `reg_state` enum('y','n') DEFAULT 'y' COMMENT '注册状态',
  `reg_notice` varchar(255) DEFAULT NULL COMMENT '注册关闭通知',
  `reg_ipon` int(10) DEFAULT '0' COMMENT '注册IP限制',
  `reg_inon` int(10) DEFAULT '0' COMMENT '注册信息限制',
  `logon_state` enum('y','n') DEFAULT 'y' COMMENT '登录状态',
  `logon_notice` varchar(255) DEFAULT NULL COMMENT '登录关闭通知',
  `logon_check_in` enum('y','n') DEFAULT 'y' COMMENT '登录校验设备信息',
  `logon_check_t` int(10) DEFAULT '0' COMMENT '登录换绑机器码间隔',
  `logon_num` int(10) DEFAULT '1' COMMENT '多设备登录',
  `logon_way` int(10) DEFAULT '0' COMMENT '登录方式',
  `reg_award` enum('vip','fen') DEFAULT 'vip' COMMENT '注册奖励',
  `reg_award_num` int(10) DEFAULT '5' COMMENT '注册奖励数',
  `inv_award` enum('vip','fen') DEFAULT 'vip' COMMENT '邀请奖励',
  `inv_award_num` int(10) DEFAULT '0' COMMENT '邀请奖励数',
  `diary_award` enum('vip','fen') DEFAULT 'fen' COMMENT '签到奖励',
  `diary_award_num` int(10) DEFAULT '0' COMMENT '签到奖励数',
  `android_state` enum('y','n') DEFAULT 'y' COMMENT '神马端状态',
  `android_bb` varchar(10) DEFAULT '1.0' COMMENT '神马版本号',
  `android_show` text COMMENT '神马更新内容',
  `android_url` varchar(255) DEFAULT NULL COMMENT '神马更新地址',
  `ios_state` enum('y','n') DEFAULT 'y' COMMENT '293应用状态',
  `ios_bb` varchar(10) DEFAULT '1.0' COMMENT '293版本号',
  `ios_show` text COMMENT '293更新内容',
  `ios_url` varchar(255) DEFAULT NULL COMMENT '293更新连接',
  `mi_state` enum('y','n') DEFAULT 'y' COMMENT '加密控制',
  `mi_type` int(10) DEFAULT '1' COMMENT '加密类型',
  `mi_sign` enum('y','n') DEFAULT 'y' COMMENT '是否签名',
  `mi_time` int(10) DEFAULT '100' COMMENT '是否校验时间',
  `mi_rc4_key` varchar(255) DEFAULT 'GN8ZGa4DmaHQrHhSTyQ3FwnhCQt68EXQ' COMMENT '加密密钥',
  `mi_rsa_public_key` text COMMENT '加密公钥',
  `mi_rsa_private_key` text COMMENT '加密私钥',
  `mi_aes_key` varchar(255) DEFAULT NULL,
  `mi_aes_iv` varchar(255) DEFAULT NULL,
  `smtp_state` enum('y','n') DEFAULT 'n' COMMENT '邮箱状态',
  `smtp_host` varchar(255) DEFAULT 'smtp.qq.com' COMMENT '邮箱服务器',
  `smtp_user` varchar(255) DEFAULT NULL COMMENT '邮箱账户',
  `smtp_pass` varchar(255) DEFAULT NULL COMMENT '邮箱密码',
  `smtp_port` int(10) DEFAULT '25' COMMENT '邮箱端口',
  `sms_state` enum('y','n') DEFAULT 'n' COMMENT '短信模块',
  `sms_key` varchar(255) DEFAULT NULL COMMENT '短信秘钥',
  `pay_ali_state` enum('y','n') DEFAULT 'n' COMMENT '支付宝状态',
  `pay_ali_type` int(10) DEFAULT '0' COMMENT '支付宝通道',
  `pay_ali_eurl` varchar(255) DEFAULT '',
  `pay_ali_eid` varchar(125) DEFAULT NULL,
  `pay_ali_ekey` varchar(255) DEFAULT NULL,
  `pay_ali_notify` varchar(255) DEFAULT '' COMMENT '支付宝异步通知地址',
  `pay_wx_state` enum('y','n') DEFAULT 'n' COMMENT '微信状态',
  `pay_wx_type` int(10) DEFAULT '0' COMMENT '微信支付通道',
  `pay_wx_eurl` varchar(255) DEFAULT '',
  `pay_wx_eid` varchar(125) DEFAULT NULL,
  `pay_wx_ekey` varchar(255) DEFAULT NULL,
  `pay_wx_notify` varchar(255) DEFAULT '' COMMENT '微信异步通知地址',
  `pay_qq_state` enum('y','n') DEFAULT 'n' COMMENT 'QQ钱包状态',
  `pay_qq_type` int(10) DEFAULT '0' COMMENT 'QQ钱包支付通道',
  `pay_qq_eurl` varchar(255) DEFAULT '',
  `pay_qq_eid` varchar(125) DEFAULT NULL,
  `pay_qq_ekey` varchar(255) DEFAULT NULL,
  `pay_qq_notify` varchar(255) DEFAULT '' COMMENT 'QQ钱包异步通知地址',
  `pay_ali_pid` varchar(20) DEFAULT NULL COMMENT '支付宝合作身份者ID',
  `pay_ali_account` varchar(255) DEFAULT NULL COMMENT '支付宝收款账号',
  `pay_ali_md5` varchar(255) DEFAULT NULL COMMENT '支付宝安全校验码',
  `pay_ali_appid` varchar(20) DEFAULT NULL COMMENT '支付宝当面付appid',
  `pay_ali_public_key` text COMMENT '支付宝当面付公钥',
  `pay_ali_private_key` text COMMENT '支付宝当面付私钥',
  `pay_wx_appid` varchar(20) DEFAULT NULL COMMENT '微信支付APPID',
  `pay_wx_mchid` varchar(10) DEFAULT NULL COMMENT '微信支付MCHID',
  `pay_wx_key` varchar(255) DEFAULT NULL COMMENT '微信支付KEY',
  `pay_wx_appsecret` varchar(255) DEFAULT NULL COMMENT '微信支付APPSECRET',
  `pay_qq_mchid` varchar(255) DEFAULT NULL COMMENT 'QQ钱包MCHID',
  `pay_qq_mchkey` varchar(255) DEFAULT NULL COMMENT 'QQ钱包MCHKEY',
  `downloadtype1` enum('2','1','0') DEFAULT '0' COMMENT '神马更新类型',
  `downloadpwd1` varchar(10) DEFAULT '' COMMENT '神马网盘密码',
  `downloadtype2` enum('2','1','0') DEFAULT '0' COMMENT '293更新类型',
  `downloadpwd2` varchar(10) DEFAULT '' COMMENT '293网盘密码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key` (`appkey`),
  KEY `id` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app`
--

LOCK TABLES `yyys_app` WRITE;
/*!40000 ALTER TABLE `yyys_app` DISABLE KEYS */;
INSERT INTO `yyys_app` VALUES (10000,'神马影视','4d86cdb33aa6f9dd27c4e6adee49995e','y','y',NULL,'y',NULL,0,0,'y',NULL,'y',0,1,0,'vip',5,'vip',0,'fen',0,'y','1.8',NULL,NULL,'n','1.8',NULL,NULL,'y',1,'y',100,'GN8ZGa4DmaHQrHhSTyQ3FwnhCQt68EXQ',NULL,NULL,NULL,NULL,'n','smtp.qq.com',NULL,NULL,25,'n',NULL,'n',0,'',NULL,NULL,'','n',0,'',NULL,NULL,'','n',0,'',NULL,NULL,'',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0','','0','');
/*!40000 ALTER TABLE `yyys_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app_ac_notice`
--

DROP TABLE IF EXISTS `yyys_app_ac_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app_ac_notice` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL COMMENT '内容',
  `erweimaurl` text NOT NULL,
  `appid` varchar(255) NOT NULL COMMENT '应用id',
  `time` int(10) NOT NULL COMMENT '时间戳',
  `adm` varchar(255) NOT NULL COMMENT '发布人',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app_ac_notice`
--

LOCK TABLES `yyys_app_ac_notice` WRITE;
/*!40000 ALTER TABLE `yyys_app_ac_notice` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_app_ac_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app_category_notice`
--

DROP TABLE IF EXISTS `yyys_app_category_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app_category_notice` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL COMMENT '内容',
  `appid` varchar(255) NOT NULL COMMENT '应用id',
  `time` int(10) NOT NULL COMMENT '时间戳',
  `adm` varchar(255) NOT NULL COMMENT '发布人',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app_category_notice`
--

LOCK TABLES `yyys_app_category_notice` WRITE;
/*!40000 ALTER TABLE `yyys_app_category_notice` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_app_category_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app_exten`
--

DROP TABLE IF EXISTS `yyys_app_exten`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app_exten` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `variable` varchar(128) NOT NULL COMMENT '变量',
  `data` text NOT NULL COMMENT '数据',
  `appid` int(10) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app_exten`
--

LOCK TABLES `yyys_app_exten` WRITE;
/*!40000 ALTER TABLE `yyys_app_exten` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_app_exten` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app_live_notice`
--

DROP TABLE IF EXISTS `yyys_app_live_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app_live_notice` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL COMMENT '内容',
  `appid` varchar(255) NOT NULL COMMENT '应用id',
  `time` int(10) NOT NULL COMMENT '时间戳',
  `adm` varchar(255) NOT NULL COMMENT '发布人',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app_live_notice`
--

LOCK TABLES `yyys_app_live_notice` WRITE;
/*!40000 ALTER TABLE `yyys_app_live_notice` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_app_live_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app_notice`
--

DROP TABLE IF EXISTS `yyys_app_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app_notice` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL COMMENT '内容',
  `appid` varchar(255) NOT NULL COMMENT '应用id',
  `time` int(10) NOT NULL COMMENT '时间戳',
  `adm` varchar(255) NOT NULL COMMENT '发布人',
  `noticename` varchar(255) NOT NULL COMMENT '公告名字',
  PRIMARY KEY (`id`),
  KEY `content` (`content`),
  KEY `appid` (`appid`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app_notice`
--

LOCK TABLES `yyys_app_notice` WRITE;
/*!40000 ALTER TABLE `yyys_app_notice` DISABLE KEYS */;
INSERT INTO `yyys_app_notice` VALUES (1,'测试应用公告','10000',1721150606,'管理员','9827803d62226856378600eaba584a02');
/*!40000 ALTER TABLE `yyys_app_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app_notices`
--

DROP TABLE IF EXISTS `yyys_app_notices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app_notices` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL COMMENT '内容',
  `appid` varchar(255) NOT NULL COMMENT '应用id',
  `time` int(10) NOT NULL COMMENT '时间戳',
  `adm` varchar(255) NOT NULL COMMENT '发布人',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app_notices`
--

LOCK TABLES `yyys_app_notices` WRITE;
/*!40000 ALTER TABLE `yyys_app_notices` DISABLE KEYS */;
INSERT INTO `yyys_app_notices` VALUES (1,'测试跑马公告','10000',1721150599,'管理员');
/*!40000 ALTER TABLE `yyys_app_notices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app_pay_text`
--

DROP TABLE IF EXISTS `yyys_app_pay_text`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app_pay_text` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL COMMENT '内容',
  `appid` varchar(255) NOT NULL COMMENT '应用id',
  `time` int(10) NOT NULL COMMENT '时间戳',
  `adm` varchar(255) NOT NULL COMMENT '发布人',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app_pay_text`
--

LOCK TABLES `yyys_app_pay_text` WRITE;
/*!40000 ALTER TABLE `yyys_app_pay_text` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_app_pay_text` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_app_voice_notice`
--

DROP TABLE IF EXISTS `yyys_app_voice_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_app_voice_notice` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL COMMENT '内容',
  `appid` varchar(255) NOT NULL COMMENT '应用id',
  `time` int(10) NOT NULL COMMENT '时间戳',
  `adm` varchar(255) NOT NULL COMMENT '发布人',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_app_voice_notice`
--

LOCK TABLES `yyys_app_voice_notice` WRITE;
/*!40000 ALTER TABLE `yyys_app_voice_notice` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_app_voice_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_captcha`
--

DROP TABLE IF EXISTS `yyys_captcha`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_captcha` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `email` varchar(32) DEFAULT NULL COMMENT '邮箱',
  `phone` bigint(11) DEFAULT NULL COMMENT '手机号',
  `code` int(6) NOT NULL COMMENT '验证码',
  `new` enum('y','n') DEFAULT 'y' COMMENT '是否可被使用',
  `appid` int(10) NOT NULL COMMENT '应用ID',
  `time` int(10) NOT NULL COMMENT '时间戳',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_captcha`
--

LOCK TABLES `yyys_captcha` WRITE;
/*!40000 ALTER TABLE `yyys_captcha` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_captcha` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_feedback`
--

DROP TABLE IF EXISTS `yyys_feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `time` varchar(300) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '反馈时间',
  `series` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '反馈剧集',
  `url` longtext CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT '反馈地址',
  `ip` varchar(15) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '反馈IP',
  `user` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT '' COMMENT '反馈用户',
  `domain` varchar(255) DEFAULT '' COMMENT '反馈线路',
  `count` int(5) DEFAULT '1' COMMENT '计次',
  PRIMARY KEY (`id`),
  KEY `url` (`time`),
  KEY `expiretime` (`ip`),
  KEY `time` (`time`),
  KEY `series` (`series`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_feedback`
--

LOCK TABLES `yyys_feedback` WRITE;
/*!40000 ALTER TABLE `yyys_feedback` DISABLE KEYS */;
INSERT INTO `yyys_feedback` VALUES (1,'1721177085','全资进组-第02集','https://vod.lyhuicheng.com/20240715/9gxqjKvZ/index.m3u8','14.26.224.243','5465465','360zy',1);
/*!40000 ALTER TABLE `yyys_feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_fen`
--

DROP TABLE IF EXISTS `yyys_fen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_fen` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `appid` int(10) NOT NULL COMMENT '应用ID',
  `name` varchar(255) NOT NULL COMMENT '积分事件名称',
  `fen_num` int(10) NOT NULL COMMENT '积分数',
  `vip_num` int(10) DEFAULT '0' COMMENT '获得VIP天数',
  `state` enum('y','n') DEFAULT 'y' COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_fen`
--

LOCK TABLES `yyys_fen` WRITE;
/*!40000 ALTER TABLE `yyys_fen` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_fen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_fen_order`
--

DROP TABLE IF EXISTS `yyys_fen_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_fen_order` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `fid` int(10) NOT NULL COMMENT '积分事件ID',
  `uid` int(10) NOT NULL COMMENT '用户ID',
  `mark` varchar(255) DEFAULT NULL COMMENT '扣分标记',
  `time` int(10) NOT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_fen_order`
--

LOCK TABLES `yyys_fen_order` WRITE;
/*!40000 ALTER TABLE `yyys_fen_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_fen_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_goods`
--

DROP TABLE IF EXISTS `yyys_goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_goods` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '商品名称',
  `type` enum('fen','vip') NOT NULL COMMENT '商品类型',
  `money` float(10,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `amount` int(10) NOT NULL COMMENT '到账数',
  `jie` text COMMENT '商品介绍',
  `appid` int(10) DEFAULT '0' COMMENT '应用',
  `state` enum('y','n') DEFAULT 'y' COMMENT '商品状态',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_goods`
--

LOCK TABLES `yyys_goods` WRITE;
/*!40000 ALTER TABLE `yyys_goods` DISABLE KEYS */;
INSERT INTO `yyys_goods` VALUES (1,'神马影视','vip',11.00,111,'111',10000,'y');
/*!40000 ALTER TABLE `yyys_goods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_goods_order`
--

DROP TABLE IF EXISTS `yyys_goods_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_goods_order` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `order` varchar(64) NOT NULL COMMENT '订单号',
  `uid` int(10) NOT NULL COMMENT '用户ID',
  `gid` int(10) NOT NULL COMMENT '商品ID',
  `name` varchar(255) NOT NULL COMMENT '商品名称',
  `money` float(10,2) NOT NULL DEFAULT '0.00' COMMENT '支付金额',
  `o_time` int(10) NOT NULL COMMENT '订单时间',
  `p_time` int(10) DEFAULT '0' COMMENT '支付时间',
  `p_type` enum('ali','wx','qq') NOT NULL COMMENT '支付类型',
  `state` int(10) DEFAULT '0' COMMENT '订单状态',
  `data` text COMMENT '回调数据',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order` (`order`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_goods_order`
--

LOCK TABLES `yyys_goods_order` WRITE;
/*!40000 ALTER TABLE `yyys_goods_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_goods_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_kami`
--

DROP TABLE IF EXISTS `yyys_kami`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_kami` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `tid` int(10) NOT NULL COMMENT '卡密类型ID',
  `appid` int(10) NOT NULL COMMENT '应用ID',
  `kami` varchar(32) NOT NULL COMMENT '卡密',
  `type` enum('vip','fen') DEFAULT 'vip' COMMENT '卡密类型',
  `amount` int(10) NOT NULL COMMENT '数量',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `user` varchar(32) DEFAULT NULL COMMENT '使用者',
  `use_time` int(10) DEFAULT '0' COMMENT '使用时间',
  `end_time` int(10) DEFAULT '0' COMMENT '结束时间',
  `new` enum('y','n') DEFAULT 'n' COMMENT '是否导出',
  `state` enum('y','n') DEFAULT 'y' COMMENT '状态',
  PRIMARY KEY (`id`),
  UNIQUE KEY `kami` (`kami`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_kami`
--

LOCK TABLES `yyys_kami` WRITE;
/*!40000 ALTER TABLE `yyys_kami` DISABLE KEYS */;
INSERT INTO `yyys_kami` VALUES (1,1,10000,'1302663881','vip',999999999,'','32323131',1721149629,0,'n','y'),(2,1,10000,'9169584569','vip',999999999,'','5465464',1721172697,0,'n','y');
/*!40000 ALTER TABLE `yyys_kami` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_kami_type`
--

DROP TABLE IF EXISTS `yyys_kami_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_kami_type` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '类型名',
  `type` enum('vip','fen') DEFAULT 'vip' COMMENT '类型',
  `amount` int(10) NOT NULL COMMENT '面值',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_kami_type`
--

LOCK TABLES `yyys_kami_type` WRITE;
/*!40000 ALTER TABLE `yyys_kami_type` DISABLE KEYS */;
INSERT INTO `yyys_kami_type` VALUES (1,'永久','vip',999999999);
/*!40000 ALTER TABLE `yyys_kami_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_live`
--

DROP TABLE IF EXISTS `yyys_live`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_live` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '显示名称',
  `url` int(10) NOT NULL DEFAULT '1' COMMENT '接口id',
  `data` text,
  `appid` int(11) DEFAULT '0',
  `empty` enum('1','0') NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `name` (`name`),
  KEY `url` (`url`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_live`
--

LOCK TABLES `yyys_live` WRITE;
/*!40000 ALTER TABLE `yyys_live` DISABLE KEYS */;
INSERT INTO `yyys_live` VALUES (1,'CCTV1',1,'CCTV1,http://ayolee.com:5253/udp/239.93.0.184:5140',0,'1');
/*!40000 ALTER TABLE `yyys_live` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_live_analysis`
--

DROP TABLE IF EXISTS `yyys_live_analysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_live_analysis` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '标识名称',
  `keyword` varchar(255) NOT NULL COMMENT '线路',
  `url` varchar(255) DEFAULT '' COMMENT '接口id',
  `header` varchar(10000) DEFAULT '' COMMENT 'Header',
  `state` enum('0','1') DEFAULT '0' COMMENT '状态',
  `Core` enum('99','5','4','3','2','1','0') DEFAULT '99',
  `Safe` enum('1','0') DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `name` (`name`),
  KEY `keyword` (`keyword`),
  KEY `url` (`url`),
  KEY `state` (`state`),
  KEY `header` (`header`(333))
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_live_analysis`
--

LOCK TABLES `yyys_live_analysis` WRITE;
/*!40000 ALTER TABLE `yyys_live_analysis` DISABLE KEYS */;
/*!40000 ALTER TABLE `yyys_live_analysis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_live_channeltype`
--

DROP TABLE IF EXISTS `yyys_live_channeltype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_live_channeltype` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '分类名字',
  `state` enum('1','0') NOT NULL DEFAULT '0',
  `appid` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_live_channeltype`
--

LOCK TABLES `yyys_live_channeltype` WRITE;
/*!40000 ALTER TABLE `yyys_live_channeltype` DISABLE KEYS */;
INSERT INTO `yyys_live_channeltype` VALUES (1,'央视频道','1',0);
/*!40000 ALTER TABLE `yyys_live_channeltype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_log`
--

DROP TABLE IF EXISTS `yyys_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_log` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `group` enum('adm','agent','user') DEFAULT 'user' COMMENT '用户组',
  `type` varchar(255) DEFAULT 'no' COMMENT '日志类型',
  `uid` int(10) DEFAULT '0' COMMENT '用户ID，管理员=0',
  `vip` int(10) DEFAULT '0' COMMENT 'VIP变化',
  `fen` int(10) DEFAULT '0' COMMENT '积分变化',
  `ip` varchar(15) DEFAULT '127.0.0.1' COMMENT '操作IP',
  `time` int(10) NOT NULL COMMENT '操作时间',
  `appid` int(10) DEFAULT '0' COMMENT '应用ID',
  `data` text COMMENT '数据',
  `status` int(10) DEFAULT '200' COMMENT '状态码',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=79 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_log`
--

LOCK TABLES `yyys_log` WRITE;
/*!40000 ALTER TABLE `yyys_log` DISABLE KEYS */;
INSERT INTO `yyys_log` VALUES (1,'adm','logon',0,0,0,'14.26.224.243',1721148819,0,'{\"user\":\"admin\",\"pwd\":\"123456\"}',200),(2,'adm','app_add',0,0,0,'14.26.224.243',1721148830,0,'{\"bb\":\"1.8\",\"name\":\"u795eu9a6cu5f71u89c6\",\"android_state\":\"y\",\"ios_state\":\"n\"}',200),(3,'user','user_logon',1,0,0,'14.26.224.243',1721149099,10000,NULL,200),(4,'user','user_logon',2,0,0,'14.26.224.243',1721149104,10000,NULL,200),(5,'user','user_logon',3,0,0,'14.26.224.243',1721149152,10000,NULL,200),(6,'user','user_logon',3,0,0,'14.26.224.243',1721149362,10000,NULL,200),(7,'user','user_logon',3,0,0,'14.26.224.243',1721149397,10000,NULL,200),(8,'user','user_logon',3,0,0,'14.26.224.243',1721149508,10000,NULL,200),(9,'adm','add_kami_type',0,0,0,'14.26.224.243',1721149617,0,'{\"type\":\"vip\",\"amount\":\"999999999\",\"name\":\"u6c38u4e45\"}',200),(10,'adm','kami_add',0,0,0,'14.26.224.243',1721149623,0,'{\"tid\":\"1\",\"num\":\"1\",\"out\":\"0\",\"k_length\":\"10\",\"note\":\"\",\"appid\":\"10000\"}',200),(11,'user','card',3,999999999,0,'14.26.224.243',1721149629,10000,NULL,200),(12,'user','user_logon',3,0,0,'14.26.224.243',1721150081,10000,NULL,200),(13,'user','user_logon',3,0,0,'14.26.224.243',1721150294,10000,NULL,200),(14,'adm','add_analysis',0,0,0,'14.26.224.243',1721150416,0,'{\"name\":\"ffm3u8\",\"add_keyword\":\"ffm3u8\",\"Client_type\":\"one_way\",\"one_way_connect\":\"\",\"two_way_main_connect\":\"\",\"two_way_deputy_connect\":\"\",\"Core\":\"99\",\"Ad_block\":\"1\"}',200),(15,'adm','edit_analysis',0,0,0,'14.26.224.243',1721150427,0,'{\"state\":\"0\",\"name\":\"ffm3u8\",\"keyword\":\"ffm3u8\",\"url\":\"\",\"header\":\"\",\"Core\":\"99\",\"Ad_block\":\"1\",\"position\":\"0\",\"Safe\":\"0\",\"Ewmsize\":\"250\",\"Ewm_Width_Height\":\"260|85\",\"Moviesize\":\"30\",\"Tvplaysize\":\"100\",\"headposition\":\"0\"}',200),(16,'user','user_logon',3,0,0,'14.26.224.243',1721150437,10000,NULL,200),(17,'adm','edit_analysis',0,0,0,'14.26.224.243',1721150524,0,'{\"state\":\"0\",\"name\":\"ffm3u8\",\"keyword\":\"ffm3u8\",\"url\":\"\",\"header\":\"\",\"Core\":\"99\",\"Ad_block\":\"1\",\"position\":\"0\",\"Safe\":\"0\",\"Ewmsize\":\"250\",\"Ewm_Width_Height\":\"260|85\",\"Moviesize\":\"30\",\"Tvplaysize\":\"200\",\"headposition\":\"0\"}',200),(18,'user','user_logon',3,0,0,'14.26.224.243',1721150534,10000,NULL,200),(19,'adm','notices_add',0,0,0,'14.26.224.243',1721150599,0,'{\"content\":\"u6d4bu8bd5u8dd1u9a6cu516cu544a\",\"appid\":\"10000\"}',200),(20,'adm','notice_add',0,0,0,'14.26.224.243',1721150606,0,'{\"content\":\"u6d4bu8bd5u5e94u7528u516cu544a\",\"appid\":\"10000\"}',200),(21,'user','user_logon',3,0,0,'14.26.224.243',1721150613,10000,NULL,200),(22,'user','user_logon',3,0,0,'14.26.224.243',1721150937,10000,NULL,200),(23,'user','user_logon',3,0,0,'14.26.224.243',1721150980,10000,NULL,200),(24,'user','user_logon',3,0,0,'14.26.224.243',1721150997,10000,NULL,200),(25,'user','user_logon',3,0,0,'14.26.224.243',1721151081,10000,NULL,200),(26,'user','user_logon',3,0,0,'14.26.224.243',1721151090,10000,NULL,200),(27,'user','user_logon',3,0,0,'14.26.224.243',1721151253,10000,NULL,200),(28,'user','user_logon',3,0,0,'14.26.224.243',1721151273,10000,NULL,200),(29,'user','user_logon',3,0,0,'14.26.224.243',1721151290,10000,NULL,200),(30,'user','user_logon',3,0,0,'14.26.224.243',1721151308,10000,NULL,200),(31,'user','user_logon',3,0,0,'14.26.224.243',1721151327,10000,NULL,200),(32,'user','user_logon',3,0,0,'14.26.224.243',1721151345,10000,NULL,200),(33,'adm','goods_add',0,0,0,'14.26.224.243',1721151377,0,'{\"name\":\"u795eu9a6cu5f71u89c6\",\"type\":\"vip\",\"amount\":\"111\",\"money\":\"11\",\"jie\":\"111\",\"appid\":\"10000\"}',200),(34,'user','user_logon',3,0,0,'14.26.224.243',1721151433,10000,NULL,200),(35,'user','user_logon',4,0,0,'14.26.224.243',1721151441,10000,NULL,200),(36,'user','user_logon',4,0,0,'14.26.224.243',1721151486,10000,NULL,200),(37,'adm','modify_global',0,0,0,'14.26.224.243',1721151661,0,'{\"Weather_switch\":\"0\",\"Weather_Appid\":\"43656176\",\"Weather_Appsecret\":\"I42og6Lm\",\"UA\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.125 Safari/537.36\",\"Encrypt\":\"0\",\"Analysis_log\":\"1\",\"Timeout\":\"15\",\"Submission\":\"0\",\"Try\":\"0\",\"Trytime\":\"6\",\"Force_Upgrade\":\"0\",\"Resource_Key\":\"dsadsadsad213213213\",\"Offsite_value\":\"9\",\"Risk_value\":\"4\",\"Play_Value\":\"300\",\"Overrun_Value\":\"5\",\"IP_blacklist\":\"\",\"Machine_blacklist\":\"\",\"Account_does_not_exist\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/18/BMjAyMTEyMDgxODU4NTNfMzM1MDU2NzNfNjIzNTg1NTIxMjBfMF8z_b_Bf178d61bbefa697d2db1bb25637fb64c.mp4?clientCacheKey=3xs2x9vrs2frgr4_b.mp4&tt=b&di=71072c02&bp=10000\",\"Account_expiration\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAyMjBfMzM1MDU2NzNfNjIzNTg4MzY0NTdfMF8z_b_Bd5d08fb1475278148e58ba60554a11c7.mp4?clientCacheKey=3xz3xh6z7ngxn3y_b.mp4&tt=b&di=71072c02&bp=10000\",\"Password_error\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAwMzNfMzM1MDU2NzNfNjIzNTg2OTA2MTJfMF8z_b_Bf9fb1db112e680249d2593dd8f7342fd.mp4?clientCacheKey=3xviumptxqrkmu2_b.mp4&tt=b&di=71072c02&bp=10000\",\"Offsite_notify\":\"https://txmov2.a.kwimgs.com/upic/2022/01/22/16/BMjAyMjAxMjIxNjIxNDhfMzM1MDU2NzNfNjU0NzIzODU2MjdfMF8z_b_B0a8b2ea146d20dcc1b2a67c2057c0df2.mp4?clientCacheKey=3xgw42gv5hdu8wy_b.mp4&tt=b&di=11b80a6&bp=13380\",\"Risk_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/05/25/23/BMjAyMjA1MjUyMzU1MzdfMzM1MDU2NzNfNzUyNTgwNjg3NTNfMF8z_b_B481077f8ae36a5f708fb321140b4fb08.mp4?clientCacheKey=3xt6smqnwrgiabm_b.mp4&tt=b&di=75b3efda&bp=13380\",\"Hotlink_notify\":\"\",\"Ban_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/03/19/18/BMjAyMjAzMTkxODI5NDRfMzM1MDU2NzNfNjk5OTk2NjEzMjNfMF8z_b_Bc628c5389c2892e81c9f5549f9a9af11.mp4?clientCacheKey=3xswm6n97vd3njm_b.mp4&tt=b&di=71072c29&bp=13380\",\"Overrun_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/03/31/02/BMjAyMjAzMzEwMjE2MzVfMzM1MDU2NzNfNzA4NDU2OTAxMjRfMF8z_b_B65b1642f5834806432518b0b36ecf990.mp4?clientCacheKey=3xi6tj5gieddm9k_b.mp4&tt=b&di=71072c24&bp=13380\",\"Version_high\":\"https://txmov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU4MjVfMzM1MDU2NzNfNzQ2NTU5NTQ2OTlfMF8z_b_B328e5c4d5779067aaeed496cd5c8e96b.mp4?clientCacheKey=3xr9sd65rfd5ism_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Version_low\":\"https://alimov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU2MDJfMzM1MDU2NzNfNzQ2NTU3NjE3MjFfMF8z_b_B3676197db6cfefd533efff26860d187a.mp4?clientCacheKey=3x5e8y6u6w6en2q_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Version_error\":\"https://alimov2.a.kwimgs.com/upic/2022/05/17/22/BMjAyMjA1MTcyMjAwMzhfMzM1MDU2NzNfNzQ2NTYxMzA5NTNfMF8z_b_B9ebf4f3e5e70b05e3a1b3805d033ead7.mp4?clientCacheKey=3xjabdphaawdrqw_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Anti_theft\":\"1\",\"Url_blacklist\":\"\",\"disconnect\":\"https://www.baidu.com/diaoxian.m3u8\",\"Log_Redis_Address\":\"127.0.0.1\",\"Log_Redis_Port\":\"6379\",\"Log_RedisDB\":\"2\"}',200),(38,'adm','modify_global',0,0,0,'14.26.224.243',1721151672,0,'{\"Weather_switch\":\"0\",\"Weather_Appid\":\"43656176\",\"Weather_Appsecret\":\"I42og6Lm\",\"UA\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.125 Safari/537.36\",\"Encrypt\":\"1\",\"Analysis_log\":\"1\",\"Timeout\":\"15\",\"Submission\":\"0\",\"Try\":\"0\",\"Trytime\":\"6\",\"Force_Upgrade\":\"0\",\"Resource_Key\":\"dsadsadsad213213213\",\"Offsite_value\":\"9\",\"Risk_value\":\"4\",\"Play_Value\":\"300\",\"Overrun_Value\":\"5\",\"IP_blacklist\":\"\",\"Machine_blacklist\":\"\",\"Account_does_not_exist\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/18/BMjAyMTEyMDgxODU4NTNfMzM1MDU2NzNfNjIzNTg1NTIxMjBfMF8z_b_Bf178d61bbefa697d2db1bb25637fb64c.mp4?clientCacheKey=3xs2x9vrs2frgr4_b.mp4&tt=b&di=71072c02&bp=10000\",\"Account_expiration\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAyMjBfMzM1MDU2NzNfNjIzNTg4MzY0NTdfMF8z_b_Bd5d08fb1475278148e58ba60554a11c7.mp4?clientCacheKey=3xz3xh6z7ngxn3y_b.mp4&tt=b&di=71072c02&bp=10000\",\"Password_error\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAwMzNfMzM1MDU2NzNfNjIzNTg2OTA2MTJfMF8z_b_Bf9fb1db112e680249d2593dd8f7342fd.mp4?clientCacheKey=3xviumptxqrkmu2_b.mp4&tt=b&di=71072c02&bp=10000\",\"Offsite_notify\":\"https://txmov2.a.kwimgs.com/upic/2022/01/22/16/BMjAyMjAxMjIxNjIxNDhfMzM1MDU2NzNfNjU0NzIzODU2MjdfMF8z_b_B0a8b2ea146d20dcc1b2a67c2057c0df2.mp4?clientCacheKey=3xgw42gv5hdu8wy_b.mp4&tt=b&di=11b80a6&bp=13380\",\"Risk_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/05/25/23/BMjAyMjA1MjUyMzU1MzdfMzM1MDU2NzNfNzUyNTgwNjg3NTNfMF8z_b_B481077f8ae36a5f708fb321140b4fb08.mp4?clientCacheKey=3xt6smqnwrgiabm_b.mp4&tt=b&di=75b3efda&bp=13380\",\"Hotlink_notify\":\"\",\"Ban_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/03/19/18/BMjAyMjAzMTkxODI5NDRfMzM1MDU2NzNfNjk5OTk2NjEzMjNfMF8z_b_Bc628c5389c2892e81c9f5549f9a9af11.mp4?clientCacheKey=3xswm6n97vd3njm_b.mp4&tt=b&di=71072c29&bp=13380\",\"Overrun_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/03/31/02/BMjAyMjAzMzEwMjE2MzVfMzM1MDU2NzNfNzA4NDU2OTAxMjRfMF8z_b_B65b1642f5834806432518b0b36ecf990.mp4?clientCacheKey=3xi6tj5gieddm9k_b.mp4&tt=b&di=71072c24&bp=13380\",\"Version_high\":\"https://txmov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU4MjVfMzM1MDU2NzNfNzQ2NTU5NTQ2OTlfMF8z_b_B328e5c4d5779067aaeed496cd5c8e96b.mp4?clientCacheKey=3xr9sd65rfd5ism_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Version_low\":\"https://alimov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU2MDJfMzM1MDU2NzNfNzQ2NTU3NjE3MjFfMF8z_b_B3676197db6cfefd533efff26860d187a.mp4?clientCacheKey=3x5e8y6u6w6en2q_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Version_error\":\"https://alimov2.a.kwimgs.com/upic/2022/05/17/22/BMjAyMjA1MTcyMjAwMzhfMzM1MDU2NzNfNzQ2NTYxMzA5NTNfMF8z_b_B9ebf4f3e5e70b05e3a1b3805d033ead7.mp4?clientCacheKey=3xjabdphaawdrqw_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Anti_theft\":\"1\",\"Url_blacklist\":\"\",\"disconnect\":\"https://www.baidu.com/diaoxian.m3u8\",\"Log_Redis_Address\":\"127.0.0.1\",\"Log_Redis_Port\":\"6379\",\"Log_RedisDB\":\"2\"}',200),(39,'user','user_logon',4,0,0,'14.26.224.243',1721152731,10000,NULL,200),(40,'user','user_logon',4,0,0,'14.26.224.243',1721152842,10000,NULL,200),(41,'user','user_logon',4,0,0,'14.26.224.243',1721152854,10000,NULL,200),(42,'user','user_logon',4,0,0,'14.26.224.243',1721152905,10000,NULL,200),(43,'user','user_logon',4,0,0,'14.26.224.243',1721153239,10000,NULL,200),(44,'adm','logon',0,0,0,'14.26.224.243',1721153569,0,'{\"user\":\"admin\",\"pwd\":\"123456\"}',200),(45,'user','user_logon',4,0,0,'14.26.224.243',1721153773,10000,NULL,200),(46,'user','user_logon',4,0,0,'14.26.224.243',1721171560,10000,NULL,200),(47,'user','user_logon',4,0,0,'14.26.224.243',1721172420,10000,NULL,200),(48,'user','user_logon',4,0,0,'14.26.224.243',1721172432,10000,NULL,200),(49,'user','user_logon',4,0,0,'14.26.224.243',1721172658,10000,NULL,200),(50,'adm','kami_add',0,0,0,'14.26.224.243',1721172685,0,'{\"tid\":\"1\",\"num\":\"1\",\"out\":\"0\",\"k_length\":\"10\",\"note\":\"\",\"appid\":\"10000\"}',200),(51,'user','card',4,999999999,0,'14.26.224.243',1721172697,10000,NULL,200),(52,'adm','add_analysis',0,0,0,'14.26.224.243',1721172768,0,'{\"name\":\"360zy\",\"add_keyword\":\"360zy\",\"Client_type\":\"one_way\",\"one_way_connect\":\"\",\"two_way_main_connect\":\"\",\"two_way_deputy_connect\":\"\",\"Core\":\"99\",\"Ad_block\":\"1\"}',200),(53,'user','user_logon',4,0,0,'14.26.224.243',1721172799,10000,NULL,200),(54,'adm','modify_global',0,0,0,'14.26.224.243',1721173128,0,'{\"Weather_switch\":\"0\",\"Weather_Appid\":\"43656176\",\"Weather_Appsecret\":\"I42og6Lm\",\"UA\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.125 Safari/537.36\",\"Encrypt\":\"1\",\"Analysis_log\":\"1\",\"Timeout\":\"15\",\"Submission\":\"0\",\"Try\":\"0\",\"Trytime\":\"6\",\"Force_Upgrade\":\"0\",\"Resource_Key\":\"3C423A5FF0A9052ADCDFA73DBEB8EC1C\",\"Offsite_value\":\"9\",\"Risk_value\":\"4\",\"Play_Value\":\"300\",\"Overrun_Value\":\"5\",\"IP_blacklist\":\"\",\"Machine_blacklist\":\"\",\"Account_does_not_exist\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/18/BMjAyMTEyMDgxODU4NTNfMzM1MDU2NzNfNjIzNTg1NTIxMjBfMF8z_b_Bf178d61bbefa697d2db1bb25637fb64c.mp4?clientCacheKey=3xs2x9vrs2frgr4_b.mp4&tt=b&di=71072c02&bp=10000\",\"Account_expiration\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAyMjBfMzM1MDU2NzNfNjIzNTg4MzY0NTdfMF8z_b_Bd5d08fb1475278148e58ba60554a11c7.mp4?clientCacheKey=3xz3xh6z7ngxn3y_b.mp4&tt=b&di=71072c02&bp=10000\",\"Password_error\":\"https://txmov2.a.kwimgs.com/upic/2021/12/08/19/BMjAyMTEyMDgxOTAwMzNfMzM1MDU2NzNfNjIzNTg2OTA2MTJfMF8z_b_Bf9fb1db112e680249d2593dd8f7342fd.mp4?clientCacheKey=3xviumptxqrkmu2_b.mp4&tt=b&di=71072c02&bp=10000\",\"Offsite_notify\":\"https://txmov2.a.kwimgs.com/upic/2022/01/22/16/BMjAyMjAxMjIxNjIxNDhfMzM1MDU2NzNfNjU0NzIzODU2MjdfMF8z_b_B0a8b2ea146d20dcc1b2a67c2057c0df2.mp4?clientCacheKey=3xgw42gv5hdu8wy_b.mp4&tt=b&di=11b80a6&bp=13380\",\"Risk_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/05/25/23/BMjAyMjA1MjUyMzU1MzdfMzM1MDU2NzNfNzUyNTgwNjg3NTNfMF8z_b_B481077f8ae36a5f708fb321140b4fb08.mp4?clientCacheKey=3xt6smqnwrgiabm_b.mp4&tt=b&di=75b3efda&bp=13380\",\"Hotlink_notify\":\"\",\"Ban_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/03/19/18/BMjAyMjAzMTkxODI5NDRfMzM1MDU2NzNfNjk5OTk2NjEzMjNfMF8z_b_Bc628c5389c2892e81c9f5549f9a9af11.mp4?clientCacheKey=3xswm6n97vd3njm_b.mp4&tt=b&di=71072c29&bp=13380\",\"Overrun_notify\":\"https://alimov2.a.kwimgs.com/upic/2022/03/31/02/BMjAyMjAzMzEwMjE2MzVfMzM1MDU2NzNfNzA4NDU2OTAxMjRfMF8z_b_B65b1642f5834806432518b0b36ecf990.mp4?clientCacheKey=3xi6tj5gieddm9k_b.mp4&tt=b&di=71072c24&bp=13380\",\"Version_high\":\"https://txmov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU4MjVfMzM1MDU2NzNfNzQ2NTU5NTQ2OTlfMF8z_b_B328e5c4d5779067aaeed496cd5c8e96b.mp4?clientCacheKey=3xr9sd65rfd5ism_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Version_low\":\"https://alimov2.a.kwimgs.com/upic/2022/05/17/21/BMjAyMjA1MTcyMTU2MDJfMzM1MDU2NzNfNzQ2NTU3NjE3MjFfMF8z_b_B3676197db6cfefd533efff26860d187a.mp4?clientCacheKey=3x5e8y6u6w6en2q_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Version_error\":\"https://alimov2.a.kwimgs.com/upic/2022/05/17/22/BMjAyMjA1MTcyMjAwMzhfMzM1MDU2NzNfNzQ2NTYxMzA5NTNfMF8z_b_B9ebf4f3e5e70b05e3a1b3805d033ead7.mp4?clientCacheKey=3xjabdphaawdrqw_b.mp4&tt=b&di=7102fc1b&bp=13380\",\"Anti_theft\":\"1\",\"Url_blacklist\":\"\",\"disconnect\":\"https://www.baidu.com/diaoxian.m3u8\",\"Log_Redis_Address\":\"127.0.0.1\",\"Log_Redis_Port\":\"6379\",\"Log_RedisDB\":\"2\"}',200),(55,'user','user_logon',4,0,0,'14.26.224.243',1721173584,10000,NULL,200),(56,'user','user_logon',4,0,0,'14.26.224.243',1721175444,10000,NULL,200),(57,'user','user_logon',4,0,0,'14.26.224.243',1721175579,10000,NULL,200),(58,'user','user_logon',4,0,0,'14.26.224.243',1721175842,10000,NULL,200),(59,'user','user_logon',4,0,0,'14.26.224.243',1721175890,10000,NULL,200),(60,'user','user_logon',4,0,0,'14.26.224.243',1721176067,10000,NULL,200),(61,'user','user_logon',4,0,0,'14.26.224.243',1721176109,10000,NULL,200),(62,'user','user_logon',5,0,0,'14.26.224.243',1721176604,10000,NULL,200),(63,'user','user_logon',5,0,0,'14.26.224.243',1721177342,10000,NULL,200),(64,'user','user_logon',5,0,0,'14.26.224.243',1721177348,10000,NULL,200),(65,'user','user_logon',5,0,0,'14.26.224.243',1721177355,10000,NULL,200),(66,'user','user_logon',5,0,0,'14.26.224.243',1721177513,10000,NULL,200),(67,'user','user_logon',5,0,0,'14.26.224.243',1721182240,10000,NULL,200),(68,'user','user_logon',5,0,0,'14.26.224.243',1721183868,10000,NULL,200),(69,'adm','user_edit',0,0,0,'14.26.224.243',1721183893,0,'{\"id\":\"5\",\"pwd\":\"\",\"email\":\"\",\"phone\":\"\",\"fen\":\"0\",\"vip\":\"1815784604\",\"ban\":\"0\",\"ban_notice\":\"\",\"openid_qq\":\"\",\"openid_wx\":\"\"}',200),(70,'user','user_logon',5,0,0,'14.26.224.243',1721184856,10000,NULL,200),(71,'user','user_logon',5,0,0,'14.26.224.243',1721185003,10000,NULL,200),(72,'user','user_logon',5,0,0,'223.104.84.91',1721185902,10000,NULL,200),(73,'user','user_logon',5,0,0,'223.104.84.91',1721185911,10000,NULL,200),(74,'user','user_logon',5,0,0,'223.104.84.91',1721185918,10000,NULL,200),(75,'user','user_logon',5,0,0,'223.104.84.91',1721186385,10000,NULL,200),(76,'user','user_logon',5,0,0,'223.104.84.91',1721186681,10000,NULL,200),(77,'user','user_logon',5,0,0,'223.104.84.91',1721187252,10000,NULL,200),(78,'user','user_logon',5,0,0,'223.104.84.91',1721187682,10000,NULL,200);
/*!40000 ALTER TABLE `yyys_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_main`
--

DROP TABLE IF EXISTS `yyys_main`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_main` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `uid` int(10) NOT NULL COMMENT 'Uid',
  `User_url` varchar(255) DEFAULT NULL COMMENT '易如意地址',
  `Api_url` varchar(255) DEFAULT NULL COMMENT '苹果地址',
  `Fb_url` varchar(255) DEFAULT 'feedback/' COMMENT '反馈地址',
  `Ad_url` varchar(255) DEFAULT NULL COMMENT '广告地址',
  `Base_host` varchar(255) DEFAULT 'smtv' COMMENT '苹果文件名',
  `Home_text_shadow` enum('1','0') DEFAULT '0' COMMENT '热门推荐文字及阴影',
  `Ad_time` int(2) DEFAULT '3' COMMENT '开屏广告时间',
  `Ad_jump` enum('1','0') DEFAULT '0' COMMENT '跳过广告',
  `play_jump` enum('300秒','240秒','180秒','150秒','120秒','90秒','60秒','30秒','20秒','15秒','10秒','0秒') DEFAULT '0秒' COMMENT '初装跳片头时间',
  `play_jump_end` enum('300秒','240秒','180秒','150秒','120秒','90秒','60秒','30秒','20秒','15秒','10秒','0秒') DEFAULT '0秒' COMMENT '初装跳片尾时间',
  `Login_control` enum('1','0') DEFAULT '1' COMMENT '隐藏扫码和找回密码',
  `Exit_Message` varchar(255) DEFAULT '播放失败,请尝试切换其他线路!',
  `Client` varchar(255) DEFAULT 'Client' COMMENT '解析客户端地址',
  `Fb_type` enum('1','0') DEFAULT '0' COMMENT '反馈类型',
  `Vod_Notice_starting_time` int(2) DEFAULT '10' COMMENT '视频跑马公告起始时间',
  `Vod_Notice_end_time` int(5) DEFAULT '60' COMMENT '视频跑马公告停留时间',
  `Logo_url` varchar(255) DEFAULT '' COMMENT '节日/远程Logo地址',
  `EpisodesNumber` enum('1','0') DEFAULT '1' COMMENT '剧集数量显示',
  `Force_Style` enum('1','0') DEFAULT '0' COMMENT '强制UI样式',
  `Interface_Style` enum('4','3','2','1','0') DEFAULT '0' COMMENT '用户界面样式',
  `Allow_changing_styles` enum('1','0') DEFAULT '1' COMMENT '允许用户更改主题',
  `Topic` enum('1','0') DEFAULT '1' COMMENT '专题显示',
  `vod_Logo` enum('1','0') DEFAULT '1' COMMENT '视频logo',
  `CornerLabelView` enum('1','0') DEFAULT '0' COMMENT '角标模式',
  `Pwd_text` varchar(255) DEFAULT '类目维护暂不对外' COMMENT '类目密码提示语',
  `play_core` enum('自动','系统','IJK','EXO','阿里') DEFAULT 'IJK' COMMENT '初装播放器内核',
  `play_decode` enum('软解码','硬解码') DEFAULT '硬解码' COMMENT '显示初装解码',
  `play_ratio` enum('全屏裁剪','等比缩放','全屏拉伸','16:9缩放','4:3 缩放','原始比例') DEFAULT '等比缩放' COMMENT '初装画面比例',
  `packet_buffering` enum('0','1') DEFAULT '1' COMMENT '显示预加载',
  `videotimeout` enum('50000000','40000000','30000000','20000000','15000000','10000000','5000000','3000000') DEFAULT '20000000' COMMENT '视频连接超时跳帧',
  `http_detect_range_support` enum('1','0') DEFAULT '1' COMMENT '播放资源是否支持续传',
  `reconnect` enum('1','0') DEFAULT '1' COMMENT '资源重试',
  `resources_reconnect` int(11) DEFAULT '5' COMMENT '播放重连次数',
  `live_streaming` enum('1','0') DEFAULT '1' COMMENT '直播流媒体优化',
  `skip_loop_filter` enum('48','32','16','8','0','-16') DEFAULT '48' COMMENT '画面质量',
  `Navigation_mode` enum('1','0') DEFAULT '0' COMMENT '系统导航',
  `Sniff_debug_mode` enum('1','0') DEFAULT '0' COMMENT '嗅探调试模式',
  `Play_timeout_debug` enum('1','0') DEFAULT '0' COMMENT '解析调试',
  `Ijk_log_debug` enum('1','0') DEFAULT '1' COMMENT 'IJK_DEBUG日志',
  `Vpn_check` enum('1','0') DEFAULT '0' COMMENT '抓包检测',
  `Xp_check` enum('1','0') DEFAULT '0' COMMENT 'XP检测',
  `Verifysign` varchar(255) DEFAULT 'e89b158e4bcf988ebd09eb83f5378e87' COMMENT 'APK前面',
  `Verifysign_check` enum('1','0') DEFAULT '0' COMMENT '签名验证',
  `Name_check` enum('1','0') DEFAULT '0' COMMENT '应用名校验',
  `Random_ad` enum('1','0') DEFAULT '1' COMMENT '随机广告',
  `Settings_page` enum('1','0') DEFAULT '0' COMMENT '设置页',
  `Auto_Source` enum('1','0') DEFAULT '1' COMMENT '自动换源',
  `UpdateNumber` enum('1','0') DEFAULT '0' COMMENT '更新集数',
  `vod_caton_check` enum('1','0') DEFAULT '0',
  `seizing_time` int(11) DEFAULT '8',
  `framedrop` int(11) DEFAULT '120',
  `start_on_prepared` enum('1','0') DEFAULT '1',
  `async_init_decoder` enum('1','0') DEFAULT '1',
  `live_core` enum('自动','系统','IJK','EXO','阿里') DEFAULT 'IJK',
  `caton_check` enum('1','0') DEFAULT '0',
  `check_time` int(11) DEFAULT '10',
  `Tackle_mode` enum('1','0') DEFAULT '1',
  `networkspeed` enum('1','0') DEFAULT '1',
  `epg` enum('2','1','0') DEFAULT '1',
  `switchs` enum('1','0') DEFAULT '1',
  `memory_source` enum('1','0') DEFAULT '1',
  `memory_channel` enum('1','0') DEFAULT '1',
  `no_time_adjust` enum('1','0') DEFAULT '0',
  `infbuf` enum('1','0') DEFAULT '0',
  `live_no_source` enum('1','0') DEFAULT '0',
  `xuanjitype` enum('1','0') DEFAULT '1',
  `xuanjinumber` int(11) DEFAULT '20',
  `reverse` enum('1','0') DEFAULT '0',
  `remember_source` enum('1','0') DEFAULT '1',
  `Same_source_search` enum('1','0') DEFAULT '0',
  `search` enum('1','0') DEFAULT '1',
  `searchport` int(11) DEFAULT '9978',
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `uid` (`uid`)
) ENGINE=MyISAM AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_main`
--

LOCK TABLES `yyys_main` WRITE;
/*!40000 ALTER TABLE `yyys_main` DISABLE KEYS */;
INSERT INTO `yyys_main` VALUES (10000,10000,'http://124.222.142.65:7752/','http://124.222.142.65:7751/','feedback/','','smtv','0',3,'0','0秒','0秒','1','播放失败,请尝试切换其他线路!','Client','1',10,60,'','1','0','0','1','1','1','0','类目维护暂不对外','IJK','硬解码','等比缩放','1','20000000','1','1',5,'1','48','0','0','0','1','0','0','e89b158e4bcf988ebd09eb83f5378e87','0','0','1','0','1','0','0',8,120,'1','1','IJK','0',10,'1','1','1','1','1','1','0','0','0','1',20,'0','1','0','1',9978);
/*!40000 ALTER TABLE `yyys_main` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_user`
--

DROP TABLE IF EXISTS `yyys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_user` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `user` varchar(32) DEFAULT NULL COMMENT '账号',
  `email` varchar(32) DEFAULT NULL COMMENT '邮箱',
  `phone` bigint(11) DEFAULT NULL COMMENT '手机号',
  `pwd` varchar(32) DEFAULT NULL COMMENT '密码',
  `name` varchar(255) DEFAULT '这个人没有名字！' COMMENT '昵称',
  `pic` varchar(255) DEFAULT '0.png' COMMENT '头像',
  `vip` int(10) DEFAULT '0' COMMENT 'VIP时间戳',
  `fen` int(10) DEFAULT '0' COMMENT '积分',
  `inv` int(10) DEFAULT '0' COMMENT '邀请人',
  `reg_time` int(10) NOT NULL COMMENT '注册时间',
  `reg_ip` varchar(15) DEFAULT '127.0.0.1' COMMENT '注册IP',
  `reg_in` varchar(255) DEFAULT NULL COMMENT '注册信息',
  `openid_wx` varchar(128) DEFAULT NULL COMMENT '微信openid',
  `openid_qq` varchar(128) DEFAULT NULL COMMENT 'QQ互联openid',
  `ban` int(10) DEFAULT '0' COMMENT '禁用到期时间戳',
  `ban_notice` varchar(255) DEFAULT NULL COMMENT '禁用通知',
  `appid` int(10) NOT NULL COMMENT '应用ID',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_user`
--

LOCK TABLES `yyys_user` WRITE;
/*!40000 ALTER TABLE `yyys_user` DISABLE KEYS */;
INSERT INTO `yyys_user` VALUES (1,'213213',NULL,NULL,'165c468905fa4e852e23d2ab8ab2c33a','213213','0.png',1721149398,0,0,1721149098,'14.26.224.243','4b409bd5611ad8c2',NULL,NULL,0,NULL,10000),(2,'2132131',NULL,NULL,'165c468905fa4e852e23d2ab8ab2c33a','2132131','0.png',1721149404,0,0,1721149104,'14.26.224.243','4b409bd5611ad8c2',NULL,NULL,0,NULL,10000),(3,'32323131',NULL,NULL,'4a33c5788dfdec5f25436b716447ab13','32323131','0.png',999999999,0,6739416,1721149152,'14.26.224.243','4b409bd5611ad8c2',NULL,NULL,0,NULL,10000),(4,'5465464',NULL,NULL,'6f1ff56945d0c68f173d81a0798f3f8e','5465464','0.png',999999999,0,8197563,1721151441,'14.26.224.243','4b409bd5611ad8c2',NULL,NULL,0,NULL,10000),(5,'5465465','',0,'c1b191dcf1a05502c53901c6f681e263','5465465','0.png',1815784604,0,5946130,1721176604,'14.26.224.243','4b409bd5611ad8c2','','',0,'',10000);
/*!40000 ALTER TABLE `yyys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yyys_user_logon`
--

DROP TABLE IF EXISTS `yyys_user_logon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yyys_user_logon` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `uid` int(10) NOT NULL COMMENT '用户ID',
  `type` enum('user','kami') DEFAULT 'user' COMMENT '登录类型',
  `token` varchar(32) NOT NULL COMMENT '用户TOKEN',
  `log_time` int(10) NOT NULL COMMENT '登录时间',
  `log_ip` varchar(15) DEFAULT '127.0.0.1' COMMENT '登录IP',
  `log_in` varchar(255) DEFAULT NULL COMMENT '登录信息',
  `last_t` int(10) NOT NULL COMMENT '最后活动时间',
  `appid` int(10) NOT NULL COMMENT 'appid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yyys_user_logon`
--

LOCK TABLES `yyys_user_logon` WRITE;
/*!40000 ALTER TABLE `yyys_user_logon` DISABLE KEYS */;
INSERT INTO `yyys_user_logon` VALUES (1,1,'user','ab71b52bf6af8ffada03f594af6527ba',1721149099,'14.26.224.243','4b409bd5611ad8c2',1721149099,10000),(2,2,'user','d5de7d436a6f378c9427e46ee095b33e',1721149104,'14.26.224.243','4b409bd5611ad8c2',1721149104,10000),(3,3,'user','03d0257efd707daf83eca1f5c0daa0b4',1721151433,'14.26.224.243','4b409bd5611ad8c2',1721151433,10000),(4,4,'user','965c476c3eb951905b80812b8e0f45fe',1721176109,'14.26.224.243','4b409bd5611ad8c2',1721176109,10000),(5,5,'user','812a4316689fa39f3efc74929514253e',1721187682,'223.104.84.91','4b409bd5611ad8c2',1721187682,10000);
/*!40000 ALTER TABLE `yyys_user_logon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'kaiyuan_ruyi'
--

--
-- Dumping routines for database 'kaiyuan_ruyi'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-07-17 11:46:35
