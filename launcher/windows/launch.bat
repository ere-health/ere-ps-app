chcp 65001
@Echo off
call config.bat
echo %url%

%SystemRoot%\System32\reg.exe query "HKLM\Software\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe" >nul 2>&1
IF  errorlevel 1 GOTO :ERROR
start "" chrome.exe --kiosk --fullscreen %url%
goto :EOF

:ERROR
call messages.bat
SET msgboxTitle=%title%
SET msgboxBody=%body%
SET tmpmsgbox=%temp%\~tmpmsgbox.vbs
IF EXIST "%tmpmsgbox%" DEL /F /Q "%tmpmsgbox%"
ECHO msgbox "%msgboxBody%",16,"%msgboxTitle%">"%tmpmsgbox%"
WSCRIPT "%tmpmsgbox%"
:END