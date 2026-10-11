$adb = "F:\devtools\android-sdk\platform-tools\adb.exe"
$dev = "127.0.0.1:21533"
$out = Join-Path $PSScriptRoot "full_log.txt"
$err = Join-Path $PSScriptRoot "full_err.txt"
& $adb -s $dev logcat -d -v time *:V 2>$null | Out-File -Encoding UTF8 $out
Write-Host "DONE SIZE=$((Get-Item $out).Length)"