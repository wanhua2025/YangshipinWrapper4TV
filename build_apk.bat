@echo off
chcp 65001 >nul
cd /d "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main"
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
echo === Gradle build start ===
call gradlew.bat assembleRelease --no-daemon --console=plain > "%TEMP%\gradle_build_log.txt" 2>&1
echo === Gradle exit code: %ERRORLEVEL% ===
if exist "app\build\outputs\apk\release\app-release.apk" (
    echo === APK BUILD SUCCESS ===
    for %%A in ("app\build\outputs\apk\release\app-release.apk") do echo APK size: %%~zA bytes
) else (
    echo === APK NOT FOUND, tail of log: ===
    type "%TEMP%\gradle_build_log.txt" | findstr /i "FAILURE ERROR BUILD"
)