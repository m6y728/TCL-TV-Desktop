# TCL TV Desktop

<img width="2301" height="1279" alt="ba176c478adbbf50665a0bb793cf5182" src="https://github.com/user-attachments/assets/d22841cd-2fb8-4ed1-9b14-6c5d640aa918" />
<img width="2198" height="1279" alt="addd6e57fb40551a2d4eb0be0f162a3a" src="https://github.com/user-attachments/assets/150b50ea-1c42-4a88-8347-81dabea41607" />

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
