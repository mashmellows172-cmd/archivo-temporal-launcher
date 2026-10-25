# Validación en Fire TV

Objetivo físico: Fire TV Stick 4K de segunda generación, modelo `AFTKM`. El flujo rechaza cualquier otro modelo por defecto y nunca cambia el launcher/Home del sistema.

## Preparación

1. Instala JDK 17 y Android SDK Platform 36 con `adb` disponible.
2. En Fire TV, activa opciones de desarrollador y depuración ADB.
3. Conecta desde el equipo: `adb connect DIRECCION_IP:5555`.
4. Confirma que solo aparece el Fire TV: `adb devices -l`.

Si hay más de un dispositivo, fija el objetivo con `-Serial` o `ANDROID_SERIAL`. El script no elige uno al azar.

## Verificar el dispositivo

```powershell
powershell -ExecutionPolicy Bypass -File scripts/check-adb-device.ps1
```

Resultado requerido: `Model` es `AFTKM` e `IsAftkm` es `True`. Para un emulador o laboratorio consciente:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/check-adb-device.ps1 -Serial emulator-5554 -AllowOtherDevice
```

`-AllowOtherDevice` no equivale a validar AFTKM. Solo elimina el bloqueo para pruebas explícitas.

## Compilar, instalar y abrir

```powershell
powershell -ExecutionPolicy Bypass -File scripts/install-debug.ps1
```

El script ejecuta `:app:assembleDebug`, instala con `adb install -r` y abre `MainActivity`. No ejecuta comandos para leer ni cambiar el Home predeterminado.

## Rendimiento manual

El script imprime comandos listos para el serial validado:

```text
adb -s SERIAL shell am start -n com.amazon.ssm/.ControlPanel
adb -s SERIAL shell dumpsys gfxinfo com.archivotemporal.launcher reset
adb -s SERIAL shell dumpsys gfxinfo com.archivotemporal.launcher framestats
```

En Developer Tools, activa **System X-Ray**. Recorre los cinco destinos con el mando durante al menos 60 segundos. Criterio: no hay interacción sostenida bajo 30 fps. `framestats` es evidencia para inspección; el emulador no impone un umbral FPS automático.

Amazon documenta el menú Developer Tools y System X-Ray en <https://developer.amazon.com/docs/fire-tv/system-xray.html>. El comando ADB para abrir el panel es `com.amazon.ssm/.ControlPanel`.

## Lista de aceptación AFTKM

- El modelo reportado es `AFTKM` sin usar `-AllowOtherDevice`.
- El APK se instala y Home abre sin ANR ni excepción no controlada.
- Treinta entradas alternadas izquierda/derecha conservan foco determinista.
- D-pad, Select y Back responden desde todas las pantallas.
- Volver desde otra app restaura el foco previo.
- Miss Minutes sigue visible y no recibe foco.
- El modo de movimiento reducido evita ciclos no esenciales y conserva respuesta de foco.
- Texto aumentado no recorta `Aplicaciones`.
- A 4K, ningún rótulo queda fuera del área segura.
- System X-Ray no muestra interacción sostenida bajo 30 fps.

La validación física queda pendiente hasta disponer del AFTKM. Un emulador Android TV solo verifica instalación, foco, accesibilidad funcional y ausencia de fallos evidentes.
