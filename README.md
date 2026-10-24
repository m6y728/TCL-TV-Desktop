# TCL TV Desktop

<p align="center">
  <img src="https://github.com/m6y728/TCL-TV-Desktop/raw/main/docs/banner.png" alt="TCL TV Desktop" width="800" />
</p>

<p align="center">
  <a href="https://github.com/m6y728/TCL-TV-Desktop/stargazers"><img src="https://img.shields.io/github/stars/m6y728/TCL-TV-Desktop?style=flat&color=cfffcf&labelColor=15181e" alt="Stars"></a>
  <a href="https://github.com/m6y728/TCL-TV-Desktop/network/members"><img src="https://img.shields.io/github/forks/m6y728/TCL-TV-Desktop?style=flat&color=cfffcf&labelColor=15181e" alt="Forks"></a>
  <a href="https://github.com/m6y728/TCL-TV-Desktop/issues"><img src="https://img.shields.io/github/issues/m6y728/TCL-TV-Desktop?style=flat&color=cfffcf&labelColor=15181e" alt="Issues"></a>
  <a href="https://github.com/m6y728/TCL-TV-Desktop/blob/main/LICENSE"><img src="https://img.shields.io/github/license/m6y728/TCL-TV-Desktop?style=flat&color=cfffcf&labelColor=15181e" alt="License"></a>
</p>

---

**一款专为老 TCL 电视（MT5882 / Android 4.4.3 KitKat）打造的极简桌面。**

A minimalist launcher for old TCL TVs running Android 4.4.3 (KitKat). It replaces the ad-filled stock home screen with a clean, fast launcher that takes over on boot and survives reboots.

> 本项目借鉴 [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv) 的开源理念二次创作，但走了一条完全不同的路线：cleanyourtv 主打 No root，覆盖新款 Android TV / Google TV；本项目**需要 Root**，专啃 TCL 老电视——那些连安装器都拒绝第三方桌面的机器。

---

<img width="2198" height="1279" alt="addd6e57fb40551a2d4eb0be0f162a3a" src="https://github.com/user-attachments/assets/150b50ea-1c42-4a88-8347-81dabea41607" />

## 为什么做这个 / Why this exists

TCL 国行老电视在系统安装器里做了拦截：正常安装一个声明了桌面（HOME 类）的应用，一律报"安装异常"。当贝桌面、Emotn UI 都是这么死的——不是装不上，是**不让装**。

本项目不跟安装器纠缠，换了一条路：

| 步骤 | 发生了什么 |
|---|---|
| **推送** | 把 APK 从电脑传到电视的临时目录 |
| **写入系统分区** | 用 Root 权限把 `/system` 重新挂载为可写，把 APK 拷进 `/system/app/` 并改名 |
| **开机加载** | 安卓开机时自动扫描 `/system/app/`，把里面的 APK 当作系统自带应用加载 |

**核心逻辑：拦截只发生在"安装"这一步；直接把文件放进系统目录，安装器全程没有参与，也就没有机会拦截。**

---

## 特性 / Features

| 特性 | 说明 |
|---|---|
| **极简纯净** | 无广告、无臃肿组件，打开就是应用列表 |
| **老硬件优化** | 针对 MT5882 + 768MB 内存的老机器，包体小、启动快 |
| **开机自动接管** | 以系统应用方式存在，开机直接进入，无需手动操作 |
| **重启不掉** | 不依赖任何"默认桌面"设置，重启后照常加载 |
| **APK 直装** | 不用重新编译，下载 Release 的 APK 即可安装 |

## 其他应用 / Other Apps

- **全视界** — 电视应用主界面，普通应用，无需 Root：[下载 全视界-v1.0.apk](https://github.com/m6y728/TCL-TV-Desktop/releases/download/%E5%85%A8%E8%A7%86%E7%95%8C-tv-v1.0/jiandesk.tv-v1.0.apk)(下载失败，刷新即可)

---

## 效果预览 / Screenshots

<img width="3814" height="2274" alt="F4897BE01369ECB1C136E3C0BCB9AACA" src="https://github.com/user-attachments/assets/3d2f14bd-75e4-4c5e-8984-823213c76891" />
<img width="3792" height="2179" alt="DECFCE369FD65F0D9AF435F88B8DADF7" src="https://github.com/user-attachments/assets/65e0f268-db21-4acb-97f4-175009593432" />
<img width="3858" height="2258" alt="6BF832C36D0C00DC3CFBF84F7EA0AD5E" src="https://github.com/user-attachments/assets/f70a640a-c6bd-43b0-a09d-530e6ec7ad68" />

---

## 安装前必读 / Before you install

**本项目需要同时满足两个前提，缺一不可：**

1. **ADB 能连上电视**：电脑和电视在同一局域网，`adb connect <电视IP>:5555` 能通
2. **电视上有可用的 su**：先跑下面这条验证，返回 `uid=0` 才算有 Root，没有就停手：

```powershell
adb shell su 0 id
```

> ⚠️ **风险提示：** 修改 `/system` 分区是高风险操作，写一半中断（断电、断网）可能导致无法开机。操作全程保持电视通电、网络稳定。**与 TCL 官方无任何关联，变砖风险自担。**

---

## 安装指南 / Installation

### 方式一：下载 Release APK（推荐）

1. 从 [Releases](https://github.com/m6y728/TCL-TV-Desktop/releases) 下载 `TCL-TV-Desktop-v1.0.0.apk`
2. **校验 SHA256，防下载被掉包**（对不上就停手，别装）：

```powershell
certutil -hashfile TCL-TV-Desktop-v1.0.0.apk SHA256
```

正确值：

```
ef29a849c2eeb878a8535d97b1b39450049a8edb2da70d86bc2aa4ec45230fe6
```

3. 执行安装（先确认连上的是自家电视）：

```powershell
# 1. 连接电视
adb connect <电视IP>:5555

# 2. 确认连上的是自家电视，再继续
adb shell

# 3. 推送 APK 到电视临时目录
adb push TCL-TV-Desktop-v1.0.0.apk /data/local/tmp/launcher.apk

# 4. 获取 Root 权限并写入系统分区
adb shell su 0 mount -o remount,rw /system
adb shell su 0 cp /data/local/tmp/launcher.apk /system/app/KitkatLauncher.apk
adb shell su 0 chmod 644 /system/app/KitkatLauncher.apk
adb shell su 0 sync

# 5. 重启电视
adb shell su 0 reboot
```

### 方式二：从源码编译

```powershell
$env:JAVA_HOME=(Resolve-Path '..\jdk17x\jdk-17.0.20.1+1').Path
$env:ANDROID_HOME=(Resolve-Path '..\android-sdk').Path
.\gradlew.bat assembleDebug
```

编译完成后，APK 位于 `app\build\outputs\apk\debug\app-debug.apk`，再按方式一的第 3 步安装。

---

## 恢复原厂桌面 / Restoring your TV

**只要 ADB 还连得上，就能救回来：**

```powershell
adb connect <电视IP>:5555
adb shell su 0 mount -o remount,rw /system
adb shell su 0 rm /system/app/KitkatLauncher.apk
adb shell su 0 reboot
```

删掉系统目录里的包、重启后，原厂桌面即恢复。

> 注意：如果之前已用 `pm disable-user` 禁用过原厂桌面，删包后还需把它重新启用，否则开机可能没有可用桌面。

---

## 常见问题 / FAQ

**Q: 为什么不能像普通应用一样直接安装？**

A: TCL 老电视的安装器会拦截所有声明了桌面（HOME 类）的第三方应用，U盘安装、当贝市场下载都会报"安装异常"。本项目绕开安装器，不是"装"进去的，是"放"进系统目录的。

**Q: 一定要 Root 吗？**

A: 是。`/system` 分区默认只读，没有 su 就写不进去。没有 Root 的电视用不了本项目。

**Q: ADB 连不上 5555 怎么办？**

A: 先确认电视 IP 没变（路由器 DHCP 会重新分配），再确认电脑和电视在同一局域网。部分 TCL 的 5555 端口不是标准 ADB，换台电脑或换种连接方式再试，硬来没用。

**Q: 安装后黑屏怎么办？**

A: 按上面"恢复原厂桌面"一节，ADB 删掉 `/system/app/KitkatLauncher.apk` 并重启。

**Q: 更新新版本怎么办？**

A: 重新下载新 APK，校验 SHA256 后按安装步骤覆盖 `/system/app/KitkatLauncher.apk`，重启即可。

**Q: 新电视能用吗？**

A: **基本不能。** Android 6.0 起强制 Verified Boot（验证启动），`/system` 被改过一个字节就拒绝开机。本项目是老电视特供，新电视改系统分区=变砖，别试。

---

## 兼容性 / Compatibility

| 项目 | 详情 |
|---|---|
| **已验证机型** | TCL MT5882 / Android 4.4.3（V8-0MT0701 平台） |
| **架构** | armeabi-v7a（32 位） |
| **应用包名** | com.clean.kitkat.launcher |
| **安装路径** | /system/app/KitkatLauncher.apk |
| **最低系统** | Android 4.4（API 19） |

> **不适用范围：** Android 6.0 及以上机型（Verified Boot 强制校验）；没有 su 的电视；ADB 无法连通的电视。其他 TCL 型号未经验证，需自行测试 ADB 和 su 是否可用。

---

## 免责声明 / Disclaimer

本项目为个人开源作品，**与 TCL 官方无任何关联**。使用本项目存在风险：

- 修改系统分区可能导致电视无法开机
- 因操作不当导致的设备故障，开发者不承担任何责任
- 本项目仅供技术学习和交流使用

**使用本项目即表示您已同意以上条款。**

---

## 许可证 / License

本项目基于 [MIT License](LICENSE) 开源。

---

## 致谢 / Credits

- [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv) — 灵感来源
- Android Open Source Project — 基础框架
