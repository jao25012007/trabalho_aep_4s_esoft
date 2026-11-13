param([Parameter(ValueFromRemainingArguments=$true)][string[]]$AppArgs)
$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    Write-Host 'Instale um JDK 17 ou superior e configure o PATH.'
    exit 1
}
New-Item -ItemType Directory -Force 'build/classes' | Out-Null
$fontes = Get-ChildItem 'src/main/java' -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' }
$utf8 = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines((Join-Path (Get-Location) 'build/fontes.txt'), [string[]]$fontes, $utf8)
& javac --release 17 -encoding UTF-8 -d build/classes '@build/fontes.txt'
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
# Use CP 850 para os acentos em portugues no console do Windows.
$educaplanInputAnterior = [Console]::InputEncoding
$educaplanOutputAnterior = [Console]::OutputEncoding
$educaplanExitCode = 1
try {
    [Console]::InputEncoding = [System.Text.Encoding]::GetEncoding(850)
    [Console]::OutputEncoding = [System.Text.Encoding]::GetEncoding(850)
    & java '-Dfile.encoding=UTF-8' -cp 'build/classes;lib/*' br.edu.educaplan.Main @AppArgs
    $educaplanExitCode = $LASTEXITCODE
} finally {
    [Console]::InputEncoding = $educaplanInputAnterior
    [Console]::OutputEncoding = $educaplanOutputAnterior
}
exit $educaplanExitCode
