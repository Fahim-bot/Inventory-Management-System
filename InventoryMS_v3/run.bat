@echo off
cd /d "%~dp0"

if not exist out  mkdir out
if not exist data mkdir data
if not exist lib  mkdir lib

echo ====================================================
echo   Inventory Management System — Build ^& Run
echo ====================================================

:: Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found. Please install JDK 11 or later.
    pause & exit /b 1
)

echo [1/2] Compiling sources...

:: Compile all sources; use lib/* for optional FlatLaf
if exist "lib\*.jar" (
    javac -d out -cp "lib\*" src\*.java src\model\*.java src\service\*.java src\ui\*.java
) else (
    javac -d out src\*.java src\model\*.java src\service\*.java src\ui\*.java
)

if errorlevel 1 (
    echo [ERROR] Compilation failed. See messages above.
    pause & exit /b 1
)

echo [2/2] Launching application...

if exist "lib\*.jar" (
    java -cp "out;lib\*" InventoryApp
) else (
    java -cp "out" InventoryApp
)

pause
