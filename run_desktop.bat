@echo off
title LandscapePro AI - Desktop Launcher
echo ======================================================
echo          LandscapePro AI - Desktop Launcher
echo ======================================================
echo.
echo Starting the Python desktop application...
echo.
python "%~dp0landscape_pro.py"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Failed to start the application.
    echo Please make sure Python is installed, and run:
    echo python -m pip install customtkinter pillow
    echo.
    pause
)
