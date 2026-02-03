@echo off
setlocal

cd /d %~dp0

if not exist out mkdir out

rem Compile (requires JDK on PATH)
dir /s /b src\main\java\edu\*.java > out\sources.txt
javac -cp "lib\weka.jar" -d out @out\sources.txt

rem Run UI
java --add-opens java.base/java.lang=ALL-UNNAMED -cp "out;lib\weka.jar;src\main\resources" edu.spp.app.SwingApp

endlocal

