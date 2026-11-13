@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\executar.ps1" %*
set "EDUCAPLAN_EXIT=%ERRORLEVEL%"
if not "%EDUCAPLAN_NO_PAUSE%"=="1" pause
exit /b %EDUCAPLAN_EXIT%
