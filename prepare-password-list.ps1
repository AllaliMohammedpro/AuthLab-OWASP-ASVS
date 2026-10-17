# A lancer dans PowerShell a la racine du projet : .\prepare-password-list.ps1
$ErrorActionPreference = 'Stop'
$source = 'https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/Common-Credentials/xato-net-10-million-passwords-100000.txt'
$dest = Join-Path $PSScriptRoot 'backend\config\common-passwords.txt'
$folder = Split-Path -Parent $dest
New-Item -ItemType Directory -Force -Path $folder | Out-Null
Write-Host 'Telechargement de la liste SecLists (source MIT)...' -ForegroundColor Cyan
$temp = Join-Path $env:TEMP ('authlab-common-' + [guid]::NewGuid().ToString('N') + '.txt')
try {
    Invoke-WebRequest -Uri $source -OutFile $temp -UseBasicParsing
   $filtered = @(
    Get-Content -LiteralPath $temp -Encoding UTF8 |
    Where-Object { $_.Length -ge 8 -and $_.Length -le 128 } |
    ForEach-Object { $_.ToLowerInvariant() } |
    Select-Object -Unique |
    Select-Object -First 3000
)
    if ($filtered.Count -lt 3000) {
        throw "Seulement $($filtered.Count) entrees valides. Le referentiel en exige au moins 3000 : choisir une liste SecLists plus grande."
    }
    [System.IO.File]::WriteAllLines($dest, [string[]]$filtered, ([System.Text.UTF8Encoding]::new($false)))
    Write-Host "OK - $($filtered.Count) mots de passe courants filtres dans $dest" -ForegroundColor Green
} finally {
    Remove-Item -LiteralPath $temp -ErrorAction SilentlyContinue
}
