@echo off
set ADB=C:\Users\Administrator\AppData\Local\Android\Sdk\platform-tools\adb.exe
set DEV=127.0.0.1:21503

echo === 重启 adb server ===
%ADB% kill-server
timeout /t 2 /nobreak >nul
%ADB% start-server
timeout /t 2 /nobreak >nul
%ADB% connect %DEV%
%ADB% devices

echo.
echo === Step1: 清 logcat ===
%ADB% -s %DEV% logcat -c

echo === Step2: 启动 App 写配置 ===
%ADB% -s %DEV% shell am start -n com.lwtdzh.yangshipinwrapper4tv/.MainActivity
timeout /t 4 /nobreak >nul

echo === Step3: force-stop ===
%ADB% -s %DEV% shell am force-stop com.lwtdzh.yangshipinwrapper4tv
timeout /t 2 /nobreak >nul

echo === Step4: 确认进程没了 ===
%ADB% -s %DEV% shell "ps -A | grep -i yang"

echo === Step5: 清 logcat + 发 BOOT_COMPLETED ===
%ADB% -s %DEV% logcat -c
%ADB% -s %DEV% shell "am broadcast -a android.intent.action.BOOT_COMPLETED"

echo === Step6: 等 8 秒 ===
timeout /t 8 /nobreak >nul

echo === Step7: 检查进程 ===
%ADB% -s %DEV% shell "ps -A | grep -i yang"

echo === Step8: 检查 Top Activity ===
%ADB% -s %DEV% shell dumpsys activity activities | findstr /i "mResumedActivity yangshipin"

echo === Step9: YSPTV 日志 ===
%ADB% -s %DEV% logcat -d -v time -s YSPTV:I

echo === Step10: 完整 boot 相关日志 ===
%ADB% -s %DEV% logcat -d -v time | findstr /i "YSPTV BootReceiver BroadcastQueue ActivityTaskManager.*START"

echo DONE.