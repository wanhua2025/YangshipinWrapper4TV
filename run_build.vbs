Set WshShell = CreateObject("WScript.Shell")
WshShell.CurrentDirectory = "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main"
WshShell.Environment("PROCESS")("JAVA_HOME") = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
returnCode = WshShell.Run("cmd /c gradlew.bat assembleRelease --no-daemon > build_vbs.log 2>&1", 0, True)
WScript.Echo "Gradle Exit Code: " & returnCode
Set fso = CreateObject("Scripting.FileSystemObject")
apkPath = "e:\YangshipinWrapper4TV-main20260926完美版\YangshipinWrapper4TV-main\app\build\outputs\apk\release\app-release.apk"
If fso.FileExists(apkPath) Then
    Set f = fso.GetFile(apkPath)
    WScript.Echo "APK SUCCESS! Size=" & f.Size & " bytes"
Else
    WScript.Echo "APK NOT FOUND!"
    If fso.FileExists("build_vbs.log") Then
        Set ts = fso.OpenTextFile("build_vbs.log", 1)
        log = ts.ReadAll
        ts.Close
        lines = Split(log, vbCrLf)
        For i = UBound(lines) To Max(0, UBound(lines) - 30) Step -1
            If i >= 0 Then WScript.Echo lines(i)
        Next
    End If
End If

Function Max(a, b)
    If a > b Then Max = a Else Max = b
End Function