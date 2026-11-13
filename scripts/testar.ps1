$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
New-Item -ItemType Directory -Force 'build/test-classes' | Out-Null
$fontes = Get-ChildItem 'src/main/java','src/test/java' -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' }
$utf8 = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines((Join-Path (Get-Location) 'build/test-fontes.txt'), [string[]]$fontes, $utf8)
& javac --release 17 -encoding UTF-8 -d build/test-classes '@build/test-fontes.txt'
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
$educaplanInputAnterior = [Console]::InputEncoding
$educaplanOutputAnterior = [Console]::OutputEncoding
$educaplanExitCode = 1
try {
    [Console]::InputEncoding = [System.Text.Encoding]::GetEncoding(850)
    [Console]::OutputEncoding = [System.Text.Encoding]::GetEncoding(850)
    & java '-Dfile.encoding=UTF-8' -cp 'build/test-classes;lib/*' br.edu.educaplan.TesteIntegracao
    $educaplanExitCode = $LASTEXITCODE
} finally {
    [Console]::InputEncoding = $educaplanInputAnterior
    [Console]::OutputEncoding = $educaplanOutputAnterior
}
exit $educaplanExitCode
