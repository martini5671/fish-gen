@echo off
setlocal
rem Starts the Angular dev server (http://localhost:4200) with the API proxy enabled.

cd /d "%~dp0..\frontend" || exit /b 1

if not exist node_modules (
    echo Installing frontend dependencies...
    call npm install || exit /b 1
)

if not exist src\app\api (
    echo Warning: frontend\src\app\api is missing. Run scripts\generate-frontend-api.bat first.
)

call npm run start -- --configuration development
endlocal
