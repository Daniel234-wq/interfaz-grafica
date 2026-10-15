Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   Iniciando Gestor de Usuarios (JavaFX + CSS)" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
if (-not (Test-Path "bin\vista")) { New-Item -ItemType Directory -Path "bin\vista" | Out-Null }

Write-Host "Compilando fuentes..." -ForegroundColor Yellow
$archivos = Get-ChildItem -Path src -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
javac --module-path "javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml -d bin $archivos

if ($LASTEXITCODE -eq 0) {
    Write-Host "Copiando recursos..." -ForegroundColor Yellow
    Copy-Item "src\vista\MainView.fxml" "bin\vista\" -Force
    Copy-Item "resources\estilos.css" "bin\" -Force

    Write-Host "Lanzando aplicacion..." -ForegroundColor Green
    java --module-path "javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "bin" aplicacion.Main
} else {
    Write-Host "Error en la compilación" -ForegroundColor Red
}
