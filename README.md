# TCL TV Desktop

Custom Android TV launcher for TCL MT5882 / Android 4.4.3 (KitKat).

- Package: `com.clean.kitkat.launcher`
- Target TV: TCL Android 4.4.3, `armeabi-v7a`
- Install path on TV: `/system/app/KitkatLauncher.apk`

## Build

```powershell
$env:JAVA_HOME=(Resolve-Path '..\jdk17x\jdk-17.0.20.1+1').Path
$env:ANDROID_HOME=(Resolve-Path '..\android-sdk').Path
.\gradlew.bat assembleDebug
```

## Install to TV

Root is required on the TV:

```powershell
adb connect 192.168.1.3:5555
adb push app\build\outputs\apk\debug\app-debug.apk /data/local/tmp/launcher.apk
adb shell su 0 mount -o remount,rw /system
adb shell su 0 cp /data/local/tmp/launcher.apk /system/app/KitkatLauncher.apk
adb shell su 0 chmod 644 /system/app/KitkatLauncher.apk
adb shell su 0 sync
adb shell su 0 reboot
```
