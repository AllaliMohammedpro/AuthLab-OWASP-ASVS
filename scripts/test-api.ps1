# Prérequis : démarrer le backend dans un autre terminal sur http://localhost:8080
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api/auth'
$stamp = Get-Date -Format 'yyyyMMddHHmmss'
$email = "etudiant-$stamp@example.test"
$original = 'LonguePhrase-2026-' + [guid]::NewGuid().ToString('N')
$newPassword = 'EncoreUnePhrase-2026-' + [guid]::NewGuid().ToString('N')
Write-Host "1. Preparation CSRF..." -ForegroundColor Cyan
$csrf = Invoke-RestMethod "$base/csrf" -Method Get -SessionVariable sess
$headers = @{'X-CSRF-TOKEN'=$csrf.token}
Write-Host "2. Inscription... ($email)" -ForegroundColor Cyan
$body = @{email=$email;displayName='Etudiant TP';password=$original} | ConvertTo-Json
Invoke-RestMethod "$base/register" -Method Post -ContentType 'application/json' -Headers $headers -WebSession $sess -Body $body | Out-Null
Write-Host '3. Connexion...' -ForegroundColor Cyan
$body = @{email=$email;password=$original} | ConvertTo-Json
Invoke-RestMethod "$base/login" -Method Post -ContentType 'application/json' -Headers $headers -WebSession $sess -Body $body | Out-Null
Write-Host '4. Acces a la route privee...' -ForegroundColor Cyan
$me = Invoke-RestMethod "$base/me" -Method Get -WebSession $sess
if($me.email -ne $email) { throw 'Le profil retourné ne correspond pas.' }
Write-Host '5. Changement de mot de passe...' -ForegroundColor Cyan
$body = @{currentPassword=$original;newPassword=$newPassword} | ConvertTo-Json
Invoke-RestMethod "$base/change-password" -Method Post -ContentType 'application/json' -Headers $headers -WebSession $sess -Body $body | Out-Null
Write-Host '6. Deconnexion...' -ForegroundColor Cyan
Invoke-RestMethod "$base/logout" -Method Post -ContentType 'application/json' -Headers $headers -WebSession $sess | Out-Null
Write-Host '7. Verification que la route privee est inaccessible...' -ForegroundColor Cyan
$denied = $false
try { Invoke-RestMethod "$base/me" -Method Get -WebSession $sess | Out-Null } catch { $denied = $true }
if(-not $denied) { throw 'ERREUR: session encore active apres deconnexion.' }
Write-Host "TOUS LES TESTS PRINCIPAUX ONT REUSSI pour $email" -ForegroundColor Green
