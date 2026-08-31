# Yuyu Life Assistant

一个简洁、离线优先的 Android 生活助手，包含待办、记账和统一设置功能。应用使用“ai桶桶”云朵猫图片作为启动封面，并使用“桶桶图标”作为桌面启动图标。

## 已实现功能

### 待办

- 添加待办并选择精确到分钟的截止日期与时间（deadline）
- 标记完成或恢复未完成
- 删除待办
- 按截止日期排序，并醒目标记已逾期待办
- 根据设置的提前时间发送系统通知；完成或删除待办会自动取消提醒
- 显示剩余与总数量
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
- 修改选项后会自动重新安排全部未完成待办的提醒

## 技术方案

- Kotlin
- Jetpack Compose + Material 3
- ViewModel + StateFlow
- Room
- Gradle Wrapper
- GitHub Actions 自动测试并构建 Debug APK

代码按职责拆分为 `domain`、`data`、`ui` 和 `util`，待办与记账功能各自拥有独立的状态、ViewModel、页面和 UI 组件。

## 云端构建 APK

推送到 `main` 后，GitHub Actions 会运行：

```bash
./gradlew testDebugUnitTest assembleDebug
```

构建成功后，在仓库的 **Actions → Build Android APK → Artifacts** 中下载 APK。

## 调研参考

开发前调研了以下 Apache-2.0 开源项目，用于确认 Compose、Room、Flow 与 ViewModel 的架构组合和常见交互方式。本项目代码根据自身需求独立实现，没有直接复制其业务代码。

- [Compose-ToDo](https://github.com/wisnukurniawan/Compose-ToDo)
- [Compose-Expense](https://github.com/wisnukurniawan/Compose-Expense)

构建与数据存储方案以 Android 官方文档为准。
