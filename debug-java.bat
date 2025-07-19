@echo off
echo Starting Nazo application in debug mode...
echo Debug port: 5005
echo You can attach debugger to localhost:5005
echo.

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

pause 