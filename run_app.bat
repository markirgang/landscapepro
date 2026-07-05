@echo off
title LandscapePro AI Launcher
echo ======================================================
echo              LandscapePro AI Launcher
echo ======================================================
echo.
echo Starting the application via Python server...
echo.
python "%~dp0run_app.py"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Failed to start the application launcher.
    echo Please make sure Python is installed and added to your PATH.
    echo.
    pause
)
