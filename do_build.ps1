$ErrorActionPreference = "Continue"
Set-Location "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main"
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"

Write-Host "=== Starting Gradle Build (clean + assembleRelease) ==="
Write-Host "Time: $(Get-Date -Format 'HH:mm:ss')"

& ".\gradlew.bat" clean assembleRelease --no-build-cache --stacktrace 2>&1 | Tee-Object -FilePath "build_full.log"

Write-Host "=== Build Exit Code: $LASTEXITCODE ==="
Write-Host "Time: $(Get-Date -Format 'HH:mm:ss')"

$apkPath = "app\build\outputs\apk\release\app-release.apk"
if (Test-Path $apkPath) {
    $f = Get-Item $apkPath
    Write-Host "APK SUCCESS! Size: $([math]::Round($f.Length/1MB,2)) MB"
    Copy-Item $apkPath "app-release-final.apk" -Force
} else {
    Write-Host "APK NOT FOUND!"
}