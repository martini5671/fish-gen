@echo off
setlocal
rem Generates the Angular API client into frontend/src/app/api from the backend's OpenAPI spec.
rem The backend must be running on http://localhost:8080.

cd /d "%~dp0..\frontend" || exit /b 1

if not exist node_modules (
    echo Installing frontend dependencies...
    call npm install || exit /b 1
)

echo Generating API client from http://localhost:8080/v3/api-docs ...
call npm run generate:api
if errorlevel 1 (
    echo.
    echo API generation failed. Is the backend running on http://localhost:8080?
    exit /b 1
)

echo API client generated in frontend\src\app\api
endlocal
