# Yuyu Life Assistant

一个简洁、离线优先的 Android 生活助手，包含待办、记账和统一设置功能。应用使用“ai桶桶”云朵猫图片作为启动封面，并使用“桶桶图标”作为桌面启动图标。

## 已实现功能

### 待办

- 添加待办并选择精确到分钟的截止日期与时间（deadline）
- 左滑事务卡片，在右侧点击删除
- 长按进入多选模式，可一次删除多项；返回键退出多选
- 按截止日期排序，并醒目标记已逾期待办
- 根据设置的提前时间发送系统通知；删除待办会自动取消提醒
- 显示事务总数量
- 数据保存在本机 Room 数据库

### 记账

- 添加收入或支出，并可选择记账日期
- 金额、分类和备注
- 可选择任意一天，查看当日明细与收支汇总
- 可选择任意月份，查看当月流水与总体收支
- 支持前后切换日期、月份
- 删除流水
- 金额以“分”为单位存储，避免浮点误差

### 设置

- 独立的统一设置页，作为所有管理功能的入口
- 当前可设置待办在截止前多久提醒：到点、提前 5/15/30 分钟、1/3 小时或 1 天
- 修改选项后会自动重新安排全部待办的提醒
- 可从手机相册或文件选择待办、账本页面的背景图
- 关闭自定义背景后恢复纯色界面，同时保留已选择图片

## 技术方案

- Kotlin
- Jetpack Compose + Material 3
- ViewModel + StateFlow
- Room
- Gradle Wrapper
- GitHub Actions 自动测试并构建固定签名的 Release APK

代码按职责拆分为 `domain`、`data`、`ui` 和 `util`，待办与记账功能各自拥有独立的状态、ViewModel、页面和 UI 组件。

## 云端构建 APK

推送到 `main` 后，GitHub Actions 会运行：

```bash
./gradlew testDebugUnitTest assembleRelease
```

构建成功后，在仓库的 **Actions → Build Android APK → Artifacts** 中下载 APK。

## 固定签名与 GitHub Secrets

Release APK 使用长期 JKS 密钥签名，后续版本必须始终使用同一密钥才能覆盖升级。密钥不得提交到公开仓库，并且必须妥善备份；密钥丢失后无法发布可覆盖安装的新版。

在仓库的 **Settings → Secrets and variables → Actions** 中添加：

- `YUYU_SIGNING_KEY_BASE64`：JKS 文件的 Base64 文本
- `YUYU_KEYSTORE_PASSWORD`：密钥库密码
- `YUYU_KEY_ALIAS`：密钥别名
- `YUYU_KEY_PASSWORD`：私钥密码

本机生成的密钥和待填写内容保存在仓库外的 `Documents/yuyu-life-assistant-signing` 文件夹中。Secrets 未配置完整时，云端构建会明确失败，不会回退为临时 Debug 签名。

`0.3.0` 及更早版本使用了无法恢复的临时签名，因此安装 `0.4.0` 前需要最后卸载一次旧版；从 `0.4.0` 开始可以直接覆盖升级。

## 调研参考

开发前调研了以下 Apache-2.0 开源项目，用于确认 Compose、Room、Flow 与 ViewModel 的架构组合和常见交互方式。本项目代码根据自身需求独立实现，没有直接复制其业务代码。

- [Compose-ToDo](https://github.com/wisnukurniawan/Compose-ToDo)
- [Compose-Expense](https://github.com/wisnukurniawan/Compose-Expense)

构建与数据存储方案以 Android 官方文档为准。
