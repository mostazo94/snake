# Genera un paquete portable para Windows (.rar y .zip) con JRE minimo incluido
# Uso: .\build-dist.ps1

$ErrorActionPreference = "Stop"

$jdkBin = $null
if (Get-Command javac -ErrorAction SilentlyContinue) {
    $jdkBin = Split-Path (Get-Command javac).Source
} else {
    $adoptium = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if ($adoptium) {
        $jdkBin = Join-Path $adoptium.FullName "bin"
    }
}

if (-not $jdkBin) {
    throw "No se encontro JDK 21+. Asegurate de tener javac en el PATH o Eclipse Adoptium instalado."
}

$javac = Join-Path $jdkBin "javac.exe"
$jar   = Join-Path $jdkBin "jar.exe"
$jlink = Join-Path $jdkBin "jlink.exe"

$root = $PSScriptRoot
$distDir = Join-Path $root "dist"
$classesDir = Join-Path $distDir "classes"
$appDir = Join-Path $distDir "Snake-Java"
$runtimeDir = Join-Path $appDir "runtime"

if (Test-Path $distDir) {
    Remove-Item $distDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $classesDir, $appDir | Out-Null

Write-Host "Compilando codigo fuente..."
$sources = Get-ChildItem (Join-Path $root "src\com\snake\*.java") | ForEach-Object { $_.FullName }
& $javac -encoding UTF-8 -d $classesDir @sources

Write-Host "Creando snake-java.jar..."
& $jar --create --file (Join-Path $appDir "snake-java.jar") --main-class com.snake.Main -C $classesDir .

Write-Host "Generando entorno portable con jlink (java.base + java.desktop)..."
& $jlink --add-modules java.base,java.desktop --strip-debug --no-header-files --no-man-pages --compress=zip-6 --output $runtimeDir

@"
@echo off
start "" "%~dp0runtime\bin\javaw.exe" -jar "%~dp0snake-java.jar"
"@ | Set-Content -Path (Join-Path $appDir "Jugar Snake.bat") -Encoding ASCII

@"
========================================
          SNAKE EN JAVA (SWING)
========================================

COMO JUGAR:
1. Descomprime toda la carpeta "Snake-Java" en cualquier lugar de tu PC.
2. Haz doble clic en "Jugar Snake.bat".
   (No necesitas instalar Java: el juego ya incluye su propio entorno portable).

CONTROLES:
- Flechas / W A S D : Mover la serpiente (o navegar el menu)
- Enter / Espacio   : Iniciar partida / Reiniciar en Game Over
- Espacio / P       : Pausar o reanudar
- T                 : Cambiar de tema visual en cualquier momento
- M                 : Silenciar o activar sonido
- Esc               : Volver al menu principal

Repositorio: https://github.com/mostazo94/snake
"@ | Set-Content -Path (Join-Path $appDir "LEEME.txt") -Encoding UTF8

$rarExe = "C:\Program Files\WinRAR\Rar.exe"
if (Test-Path $rarExe) {
    $rarOut = Join-Path $distDir "Snake-Java-Windows.rar"
    Write-Host "Empaquetando $rarOut..."
    Push-Location $distDir
    & $rarExe a -r -ep1 -m5 -s "Snake-Java-Windows.rar" "Snake-Java" | Out-Null
    Pop-Location
    Write-Host "Listo: $rarOut"
} else {
    $zipOut = Join-Path $distDir "Snake-Java-Windows.zip"
    Write-Host "WinRAR no encontrado; empaquetando $zipOut..."
    Compress-Archive -Path $appDir -DestinationPath $zipOut -Force
    Write-Host "Listo: $zipOut"
}
