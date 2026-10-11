$log = "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main\build_log.txt"
Remove-Item $log -ErrorAction SilentlyContinue
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main\gradlew.bat"
$psi.Arguments = "assembleRelease --no-daemon --console=plain"
$psi.WorkingDirectory = "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main"
$psi.EnvironmentVariables["JAVA_HOME"] = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true
$p = [System.Diagnostics.Process]::Start($psi)
$out = $p.StandardOutput
$err = $p.StandardError
while (-not $p.HasExited) {
    Start-Sleep -Seconds 2
}
$outStr = $out.ReadToEnd()
$errStr = $err.ReadToEnd()
$outStr | Out-File $log -Encoding utf8
$errStr | Out-File $log -Append -Encoding utf8
Write-Host "EXIT=$($p.ExitCode)"
if (Test-Path "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main\app\build\outputs\apk\release\app-release.apk") {
    Write-Host "APK_OK"
} else {
    Write-Host "APK_MISSING"
    Write-Host "--- LOG TAIL ---"
    Get-Content $log -Tail 40
}