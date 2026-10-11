Set-Location "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main"
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
Write-Host "=== Starting Gradle assembleRelease ==="
& ".\gradlew.bat" assembleRelease --no-daemon --console=plain 2>&1
$exitCode = $LASTEXITCODE
Write-Host "=== Gradle Exit Code: $exitCode ==="
if (Test-Path ".\app\build\outputs\apk\release\app-release.apk") {
    $f = Get-Item ".\app\build\outputs\apk\release\app-release.apk"
    Write-Host "=== APK SUCCESS: $($f.Length) bytes ==="
} else {
    Write-Host "=== APK NOT FOUND ==="
}
exit $exitCode