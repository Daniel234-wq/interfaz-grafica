@echo off
chcp 65001 > nul
echo ========================================================
echo    Iniciando Gestor de Usuarios (JavaFX + CSS)
echo ========================================================
echo Compilando codigo fuente...

if not exist bin (
    mkdir bin
)

javac --module-path "javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml -d bin src\modelo\Usuario.java src\controlador\UsuarioController.java src\aplicacion\Main.java src\aplicacion\Launcher.java

if %ERRORLEVEL% NEQ 0 (
    echo Error durante la compilacion.
    pause
    exit /b %ERRORLEVEL%
)

echo Copiando archivos FXML y estilos CSS...
if not exist bin\vista mkdir bin\vista
copy /Y "src\vista\MainView.fxml" "bin\vista\" > nul
copy /Y "resources\estilos.css" "bin\" > nul

echo Ejecutando aplicacion...
java --module-path "javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "bin" aplicacion.Main
