[CmdletBinding()]
param(
    [string]$Serial = $env:ANDROID_SERIAL,
    [switch]$AllowOtherDevice,
    [string]$AdbPath = 'adb'
)

$ErrorActionPreference = 'Stop'

function Invoke-CheckedAdb {
    param([string[]]$Arguments)

    $output = & $AdbPath @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed ($LASTEXITCODE): $($output -join [Environment]::NewLine)"
    }
    return $output
}

$deviceLines = Invoke-CheckedAdb -Arguments @('devices', '-l')
$connected = @(
    $deviceLines |
        ForEach-Object {
            if ($_ -match '^([^\s]+)\s+device(?:\s|$)') { $Matches[1] }
        }
)

if ($connected.Count -eq 0) {
    throw 'No connected ADB device is ready.'
}

if ([string]::IsNullOrWhiteSpace($Serial)) {
    if ($connected.Count -ne 1) {
        throw "Multiple ADB devices are ready. Pass -Serial explicitly: $($connected -join ', ')"
    }
    $Serial = $connected[0]
} elseif ($Serial -notin $connected) {
    throw "ADB device '$Serial' is not connected and ready."
}

$model = (Invoke-CheckedAdb -Arguments @('-s', $Serial, 'shell', 'getprop', 'ro.product.model') | Out-String).Trim()
if ([string]::IsNullOrWhiteSpace($model)) {
    throw "ADB device '$Serial' did not report ro.product.model."
}

if ($model -ne 'AFTKM' -and -not $AllowOtherDevice) {
    throw "Refusing model '$model' on '$Serial'. Expected AFTKM. Use -AllowOtherDevice only for explicit emulator or lab testing."
}

[pscustomobject]@{
    Serial = $Serial
    Model = $model
    IsAftkm = $model -eq 'AFTKM'
}
