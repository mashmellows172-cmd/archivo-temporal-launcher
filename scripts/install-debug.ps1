[CmdletBinding()]
param(
    [string]$Serial = $env:ANDROID_SERIAL,
    [switch]$AllowOtherDevice,
    [string]$AdbPath = 'adb',
    [string]$GradlePath
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($GradlePath)) {
    $GradlePath = Join-Path $repoRoot 'gradlew.bat'
}

$checkArguments = @{
    AdbPath = $AdbPath
    Serial = $Serial
    AllowOtherDevice = $AllowOtherDevice
}
$device = & (Join-Path $PSScriptRoot 'check-adb-device.ps1') @checkArguments

& $GradlePath ':app:assembleDebug'
if ($LASTEXITCODE -ne 0) {
    throw "Gradle assembleDebug failed with exit code $LASTEXITCODE."
}

$apkPath = Join-Path $repoRoot 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path -LiteralPath $apkPath -PathType Leaf)) {
    throw "Debug APK not found: $apkPath"
}

& $AdbPath -s $device.Serial install -r $apkPath
if ($LASTEXITCODE -ne 0) {
    throw "adb install failed with exit code $LASTEXITCODE."
}

& $AdbPath -s $device.Serial shell am start -n 'com.archivotemporal.launcher/.MainActivity'
if ($LASTEXITCODE -ne 0) {
    throw "MainActivity launch failed with exit code $LASTEXITCODE."
}

$adbDisplay = if ($AdbPath -eq 'adb') { 'adb' } else { "`"$AdbPath`"" }
Write-Host "Installed and launched on $($device.Serial) ($($device.Model))."
Write-Host 'Open Amazon Developer Tools, then enable System X-Ray:'
Write-Host "  $adbDisplay -s $($device.Serial) shell am start -n com.amazon.ssm/.ControlPanel"
Write-Host 'Reset and inspect Android frame timing:'
Write-Host "  $adbDisplay -s $($device.Serial) shell dumpsys gfxinfo com.archivotemporal.launcher reset"
Write-Host "  $adbDisplay -s $($device.Serial) shell dumpsys gfxinfo com.archivotemporal.launcher framestats"
Write-Host 'Home defaults were not read or changed.'
