@echo off
echo Starting Full Stack Nazo Application...
echo.
echo Starting Backend (Java Spring Boot)...
start "Backend Server" cmd /k "start-dev.bat"
echo.
echo Waiting 5 seconds for backend to start...
timeout /t 5 /nobreak > nul
echo.
echo Starting Frontend (Vue + Vite + Tailwind CSS v4)...
start "Frontend Server" cmd /k "cd nazo-frontend & npm run dev"
echo.
echo Both servers are starting...
echo Backend: http://localhost:8080
echo Frontend: http://localhost:5173 (or the port shown in the frontend terminal)
echo.
echo Tetris game successfully migrated to Vue 3 with Tailwind CSS v4!
echo Use arrow keys to move, space to rotate, P to pause
echo.
pause 