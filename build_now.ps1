$ErrorActionPreference = 'Continue'
Set-Location "e:\YangshipinWrapper4TV-main"

Write-Host "=== Build Started at $(Get-Date) ===" | Out-File "build_result.txt" -Encoding utf8

$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = ".\gradlew.bat"
$psi.Arguments = "assembleDebug"
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true

$proc = New-Object System.Diagnostics.Process
$proc.StartInfo = $psi
$proc.Start() | Out-Null

$stdoutTask = $proc.StandardOutput.ReadToEndAsync()
$stderrTask = $proc.StandardError.ReadToEndAsync()

$proc.WaitForExit()

$stdout = $stdoutTask.Result
$stderr = $stderrTask.Result

"STDOUT:" | Out-File -Append "build_result.txt" -Encoding utf8
$stdout | Out-File -Append "build_result.txt" -Encoding utf8
"" | Out-File -Append "build_result.txt" -Encoding utf8
"STDERR:" | Out-File -Append "build_result.txt" -Encoding utf8
$stderr | Out-File -Append "build_result.txt" -Encoding utf8
"" | Out-File -Append "build_result.txt" -Encoding utf8
"EXIT CODE: $($proc.ExitCode)" | Out-File -Append "build_result.txt" -Encoding utf8

Get-ChildItem "app\build\outputs\apk\debug\" -ErrorAction SilentlyContinue | Out-File -Append "build_result.txt" -Encoding utf8

Write-Host "=== Build Finished at $(Get-Date), exit code: $($proc.ExitCode) ==="