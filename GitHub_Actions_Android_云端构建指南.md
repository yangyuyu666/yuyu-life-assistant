# 使用 GitHub Actions 云端构建 Android APK

> 适用场景：**本机只负责写代码和上传 GitHub，不安装 Android Studio / Android SDK，让 GitHub Actions 在云端完成 Android 编译并把 APK 提供给你下载。**
>
> 更新日期：2026-08-31

---

## 1. GitHub Actions 是什么

GitHub Actions 可以理解为：

> **GitHub 根据你写好的自动化配置，临时创建一台云端电脑，然后自动执行构建、测试、打包等命令。**

它不是 AI，也不需要 AI 阅读你的整个项目。

对于 Android 项目，真正负责理解和构建项目的是：

- Gradle
- Android Gradle Plugin（AGP）
- Kotlin / Java 编译器
- Android SDK / Build Tools

GitHub Actions 主要负责：

1. 检测触发条件，例如 `git push`
2. 创建临时 Runner（云端构建机器）
3. 下载你的 GitHub 仓库
4. 准备 Java / Gradle 等环境
5. 执行你指定的命令，例如：

```bash
./gradlew assembleDebug
```

6. 保存生成的 APK
7. 构建结束后销毁临时 Runner

整个过程可以理解为：

```text
本机写代码
    ↓
git push
    ↓
GitHub 仓库
    ↓
GitHub Actions 被触发
    ↓
创建临时 Ubuntu Runner
    ↓
下载项目源码
    ↓
准备 JDK / Gradle / Android 环境
    ↓
./gradlew assembleDebug
    ↓
生成 APK
    ↓
上传 Artifact
    ↓
网页下载 APK
    ↓
Runner 销毁
```

---

# 2. 对本机有什么要求

如果所有 Android 编译都交给 GitHub Actions，本机要求非常低。

## 最低需要

```text
代码编辑器
+
Git
+
浏览器
+
网络
```

例如：

- VS Code
- Zed
- IntelliJ IDEA（不装 Android SDK 也可以单纯编辑）
- 甚至直接使用 GitHub 网页编辑器

本机**不强制需要**：

```text
Android Studio
Android SDK
Android Emulator
Gradle
JDK
```

因此一台配置比较低的电脑也能使用这种方式开发 Android。

例如：

```text
4 GB / 8 GB RAM
普通 CPU
没有独立显卡
```

对于“编辑源码 + Git 上传”通常已经足够。

---

# 3. 一个重要限制

如果本机完全没有 Android 开发环境，那么本机不能真正编译 Android 项目。

工作方式会变成：

```text
修改代码
 ↓
git push
 ↓
Actions 编译
 ↓
查看是否成功
 ↓
有编译错误 → 本地修改
 ↓
再次 push
```

因此：

- 小型项目：这种方式非常可行
- 大型项目：频繁等待 CI 会降低开发效率
- 需要大量 UI 实时预览：更适合 Android Studio 或云端完整 IDE
- 只需要写代码、打 APK、真机测试：非常适合 Actions

---

# 4. GitHub 仓库需要包含什么

一个正常 Android 项目大概类似：

```text
MyAndroidApp/
├── .github/
│   └── workflows/
│       └── android-build.yml
│
├── app/
│   ├── src/
│   └── build.gradle.kts
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── .gitignore
```

其中非常重要的是：

```text
gradlew
gradlew.bat
gradle/wrapper/
```

这就是 **Gradle Wrapper**。

推荐把 Wrapper 一起提交到 GitHub。

Actions 就可以直接执行：

```bash
./gradlew assembleDebug
```

而不需要你手动指定 GitHub 应该下载安装哪个 Gradle 版本。

---

# 5. 不应该上传到 GitHub 的内容

Android 项目通常应该通过 `.gitignore` 排除：

```gitignore
.gradle/
.idea/
local.properties
**/build/
*.iml
.DS_Store
```

尤其注意：

## `local.properties`

里面经常包含本机 Android SDK 路径，例如：

```properties
sdk.dir=C\:\\Users\\xxx\\AppData\\Local\\Android\\Sdk
```

这是你本机特有的配置，不应该提交。

## `build/`

这里是编译产生的文件。

例如：

```text
app/build/
build/
```

不应该长期提交进 Git。

## `.gradle/`

Gradle 缓存也不应该提交。

---

# 6. 创建 GitHub Actions Workflow

在项目根目录创建：

```text
.github/workflows/android-build.yml
```

完整推荐配置：

```yaml
name: Build Android APK

on:
  push:
    branches:
      - main

  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      # 1. 下载仓库源码
      - name: Checkout source
        uses: actions/checkout@v6

      # 2. 配置 Java
      - name: Setup Java
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 17

      # 3. 配置 Gradle，并利用 Actions Cache
      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v6

      # 4. 给 Linux 下的 gradlew 添加执行权限
      - name: Make gradlew executable
        run: chmod +x gradlew

      # 5. 编译 Debug APK
      - name: Build Debug APK
        run: ./gradlew assembleDebug

      # 6. 保存 APK，供网页下载
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7
```

---

# 7. 上面的配置每一部分是什么意思

## 7.1 `name`

```yaml
name: Build Android APK
```

只是这个 Workflow 在 GitHub 网页上显示的名字。

可以随便修改。

---

## 7.2 `on`

```yaml
on:
  push:
    branches:
      - main

  workflow_dispatch:
```

表示 Workflow 的触发方式。

### `push`

```yaml
push:
  branches:
    - main
```

表示：

> 有代码 Push 到 `main` 分支时自动构建。

例如你本地执行：

```bash
git add .
git commit -m "update"
git push
```

Push 完成后，GitHub Actions 就会自动开始。

### `workflow_dispatch`

表示允许你在 GitHub 网页上手动点击：

```text
Run workflow
```

进行一次构建。

所以即使没有新的 Push，你也可以手动重新构建。

---

# 8. `runs-on` 是什么意思

```yaml
runs-on: ubuntu-latest
```

表示：

> 使用 GitHub 提供的 Ubuntu Runner。

对于普通 Android 编译，Linux 通常是最合适的选择。

GitHub 当前标准 Runner 规格大致如下。

## 公开仓库

`ubuntu-latest`：

```text
4 CPU
16 GB RAM
14 GB SSD
```

## 私有仓库

`ubuntu-latest`：

```text
2 CPU
8 GB RAM
14 GB SSD
```

注意：

> 这里的 14 GB SSD 是本次 Runner 的临时磁盘，不是你的 GitHub 长期存储额度。

每次构建结束，Runner 会被销毁。

---

# 9. Checkout 是干什么的

```yaml
- uses: actions/checkout@v6
```

刚创建出来的 Runner 并没有你的代码。

`checkout` 会把当前 GitHub 仓库下载到 Runner。

可以大致理解成自动完成：

```bash
git clone ...
```

之后 Runner 才能看到：

```text
app/
build.gradle.kts
gradlew
settings.gradle.kts
...
```

---

# 10. Setup Java 是干什么的

```yaml
- uses: actions/setup-java@v5
  with:
    distribution: temurin
    java-version: 17
```

Android Gradle 构建需要 Java。

这里指定：

```text
JDK = Temurin 17
```

具体应该使用 Java 17、21 或其他版本，需要看你的：

- Android Gradle Plugin
- Gradle
- 项目要求

如果你的项目明确要求 Java 21，可以改成：

```yaml
java-version: 21
```

不要盲目升级，应该保持与项目 Gradle / AGP 兼容。

---

# 11. Setup Gradle 和缓存

```yaml
- uses: gradle/actions/setup-gradle@v6
```

这个步骤会帮助 Actions 配置 Gradle，并缓存可以重复使用的 Gradle 内容。

第一次构建时可能需要下载：

```text
Android Gradle Plugin
Kotlin 插件
Compose 依赖
AndroidX
Retrofit
OkHttp
Room
其他 Maven 依赖
```

如果每次 Runner 销毁后全部重新下载，会非常浪费。

因此 GitHub Actions 使用 Cache 保存可复用内容。

流程类似：

```text
第一次构建
    ↓
下载 Gradle 依赖
    ↓
编译
    ↓
保存 Cache

第二次构建
    ↓
创建新 Runner
    ↓
恢复 Cache
    ↓
只下载变化的依赖
    ↓
编译
```

所以：

> Runner 虽然每次都是新的，但并不意味着所有项目依赖都必须每次从零重新下载。

---

# 12. 为什么需要 `chmod`

```yaml
- run: chmod +x gradlew
```

Windows 本地项目上传 GitHub 后，有时 `gradlew` 的 Linux 执行权限没有正确保存。

Linux 执行：

```bash
./gradlew
```

需要它有执行权限。

所以加：

```bash
chmod +x gradlew
```

可以减少：

```text
Permission denied
```

这一类错误。

---

# 13. 真正编译 APK 的命令

```bash
./gradlew assembleDebug
```

这是最关键的一句。

它会调用 Gradle 构建 Android Debug APK。

通常生成位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

如果项目模块名字不是 `app`，路径可能会不同。

例如模块叫：

```text
mobile
```

可能变成：

```text
mobile/build/outputs/apk/debug/
```

---

# 14. `assembleDebug` 和 `assembleRelease`

## Debug

```bash
./gradlew assembleDebug
```

特点：

- 适合自己测试
- 自动使用 Debug 签名
- 不需要自己配置正式签名
- 最适合刚开始使用 Actions

## Release

```bash
./gradlew assembleRelease
```

特点：

- 用于正式发布
- 通常需要 Release Keystore
- 需要安全保存签名信息
- 不应该把密码直接写进仓库

刚开始建议只做：

```text
assembleDebug
```

---

# 15. APK 是怎么给你的

Actions 构建完成之后，Runner 会被销毁。

所以必须先把 APK 保存出来。

这一步：

```yaml
- name: Upload APK
  uses: actions/upload-artifact@v4
  with:
    name: app-debug
    path: app/build/outputs/apk/debug/*.apk
    retention-days: 7
```

会把 APK 上传为 GitHub Actions 的 **Artifact**。

---

# 16. 怎么下载 APK

打开：

```text
GitHub
 ↓
你的 Repository
 ↓
Actions
 ↓
Build Android APK
 ↓
选择某一次成功的构建
 ↓
Artifacts
 ↓
app-debug
```

点击 Artifact 下载。

通常 GitHub 下载下来会是一个 ZIP：

```text
app-debug.zip
```

解压后：

```text
app-debug.apk
```

就可以传到 Android 手机上安装测试。

---

# 17. Artifact 为什么设置 7 天

配置：

```yaml
retention-days: 7
```

表示 APK Artifact 保留 7 天。

原因是 GitHub Free 的 Artifact 存储不是无限的。

如果每次构建都永久保存 APK，例如：

```text
APK = 50 MB

10 次构建
≈ 500 MB
```

很快就会占用大量 Artifact 存储。

对于测试版本，建议：

```text
1～7 天
```

例如只希望保留一天：

```yaml
retention-days: 1
```

正式版本建议放到 GitHub Releases，而不是长期依赖 Actions Artifact。

---

# 18. GitHub Actions 免费额度

以下为 GitHub 官方文档在 **2026-08-31** 显示的 GitHub Free 标准额度。

## 私有仓库

GitHub Free：

```text
Actions：2,000 分钟 / 月
Artifact storage：500 MB
Actions Cache：默认每个仓库 10 GB
```

每月 Actions 分钟数会重置。

例如一次 Android 构建花：

```text
5 分钟
```

理论计算：

```text
2000 ÷ 5
= 400 次/月
```

如果一次：

```text
10 分钟
```

则：

```text
2000 ÷ 10
= 200 次/月
```

普通个人 Android 项目一般够用。

---

# 19. 公开仓库 Actions 免费吗

对于 **Public Repository**：

> GitHub 的标准 GitHub-hosted Runner 免费使用，不消耗私有仓库那种按月分钟额度。

例如：

```yaml
runs-on: ubuntu-latest
```

属于标准 Runner。

所以公开项目非常适合：

```text
自动构建
自动测试
自动生成 APK
自动发布 Release
```

但是：

> “免费且不计标准 Runner 分钟”并不等于可以把 Runner 当成永久 VPS。

---

# 20. 为什么不能拿 Actions 当 VPS

GitHub Actions 是 CI/CD 系统，不是长期服务器。

GitHub-hosted Runner：

- 每个 Job 都是临时环境
- Job 结束后环境销毁
- 标准 GitHub-hosted Job 最长运行约 6 小时
- GitHub Free 标准 Runner 有并发限制
- 还有 GitHub Actions 使用政策限制

因此适合：

```text
编译
测试
Lint
生成 APK
生成 AAB
发布 Release
部署网站
自动化脚本
```

不适合：

```text
长期挂机
长期运行 Web Server
游戏服务器
下载机
代理服务器
挖矿
把它当免费 VPS
```

---

# 21. GitHub Free 并发限制

GitHub 官方当前对 Free 计划的标准 GitHub-hosted Runner：

```text
最多 20 个并发 Job
```

普通个人开发基本完全不会碰到这个限制。

---

# 22. 避免每次 Push 重复浪费构建

如果你连续 Push：

```text
push 1
push 2
push 3
push 4
```

可能产生多次构建。

可以加：

```yaml
concurrency:
  group: android-build-${{ github.ref }}
  cancel-in-progress: true
```

完整例子：

```yaml
name: Build Android APK

on:
  push:
    branches:
      - main
  workflow_dispatch:

concurrency:
  group: android-build-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v6

      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 17

      - uses: gradle/actions/setup-gradle@v6

      - run: chmod +x gradlew

      - run: ./gradlew assembleDebug

      - uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7
```

效果：

```text
第 1 次 Push → 开始构建
       ↓
第 2 次 Push
       ↓
旧构建取消
       ↓
只构建最新代码
```

这对于私有仓库尤其有用，可以节约 Actions 分钟。

---

# 23. 如何只在 Android 源码变化时构建

如果仓库里还有大量与 Android 无关的文件，可以设置：

```yaml
on:
  push:
    branches:
      - main
    paths:
      - "app/**"
      - "gradle/**"
      - "*.gradle"
      - "*.gradle.kts"
      - "gradle.properties"
      - "settings.gradle"
      - "settings.gradle.kts"
```

这样修改 README：

```text
README.md
```

就不会白白触发 APK 构建。

---

# 24. 推荐的本地工作流程

第一次：

```bash
git clone <你的仓库>
cd <项目目录>
```

之后正常开发：

```bash
git status
git add .
git commit -m "完成登录页面"
git push
```

然后：

```text
GitHub → Actions
```

查看编译状态。

---

# 25. Actions 失败后怎么看错误

打开：

```text
Repository
 ↓
Actions
 ↓
失败的 Workflow Run
 ↓
build
 ↓
Build Debug APK
```

这里会显示 Gradle 输出。

常见错误可能是：

```text
Compilation error
Unresolved reference
SDK version missing
Dependency resolution failed
Gradle version incompatible
AGP / JDK version incompatible
AndroidManifest.xml error
Resource linking failed
```

Actions 并不会像 AI 一样自动理解并修复这些错误。

它只是把构建日志显示出来。

你可以：

1. 复制错误日志
2. 本地修改代码
3. 再次 Push
4. Actions 自动重新构建

---

# 26. Runner 每次都销毁，为什么不会特别浪费

GitHub 的 Ubuntu Runner 并不是一台什么都没有的裸 Linux。

GitHub 维护自己的 Runner Image，其中预装大量常用开发工具。

对于 Android 构建，一般不会变成：

```text
每次下载整个 Ubuntu
每次从零安装所有 Linux 软件
每次从零准备整套基础开发环境
```

真正比较容易重复下载的是：

```text
Gradle dependencies
Maven dependencies
项目构建依赖
```

这部分可以通过：

```yaml
gradle/actions/setup-gradle@v6
```

提供的缓存机制显著减少重复工作。

---

# 27. 本地源码通常有多大

普通 Android 项目的“源码”其实通常不大。

大致可能：

| 内容 | 常见大小 |
|---|---:|
| Kotlin / Java 源码 | 数 MB～几十 MB |
| XML / 配置 | 几 MB |
| 图片 / 字体 | 几十～几百 MB |
| 普通完整源码仓库 | 约 10～200 MB 很常见 |

真正占空间的是：

```text
Android SDK
Gradle Cache
Android Emulator
System Image
NDK
CMake
build 输出
```

这些可能轻松占：

```text
10 GB
20 GB
甚至几十 GB
```

因此：

> **本地只保存源码 + Actions 负责构建**

确实可以大幅降低本机磁盘和性能要求。

---

# 28. 哪些东西会让 Git 仓库突然变得很大

不要直接向 Git 提交：

```text
视频
超大图片资源
AI 模型
APK
AAB
ZIP
数据库数据集
build 输出
Gradle Cache
Android SDK
```

例如一个：

```text
2 GB 的 AI 模型
```

如果直接提交 Git，项目仓库自然会非常大。

大文件应该考虑：

- Git LFS
- Release Assets
- 对象存储
- CDN
- 应用首次运行后下载

---

# 29. 要不要把 APK 提交回 Git 仓库

一般不要。

不建议：

```text
git add app-debug.apk
git commit
```

原因：

- APK 是二进制文件
- 每次编译都会产生新的完整文件
- Git 历史会不断膨胀
- 源代码和构建产物应该分离

推荐：

```text
测试 APK → Actions Artifact
正式 APK → GitHub Release
源码 → Git Repository
```

---

# 30. 正式版本应该用 GitHub Release

开发阶段：

```text
Actions
 ↓
Artifact
 ↓
app-debug.apk
```

正式发布可以做成：

```text
git tag v1.0.0
 ↓
Push tag
 ↓
GitHub Actions
 ↓
assembleRelease
 ↓
签名
 ↓
GitHub Release
 ↓
app-release.apk
```

这样 Release 页面会长期保留：

```text
v1.0.0
v1.1.0
v2.0.0
```

比从历史 Actions Run 中寻找 APK 更方便。

---

# 31. Release APK 的签名不能直接写密码

正式 Android APK / AAB 一般需要 Keystore。

绝对不要把下面这些内容直接公开：

```text
keystore 密码
key alias 密码
API Key
Token
服务器密码
私钥
```

GitHub 提供：

```text
Repository
 ↓
Settings
 ↓
Secrets and variables
 ↓
Actions
 ↓
Repository secrets
```

可以把敏感数据放进 Secrets。

Workflow 通过类似：

```yaml
${{ secrets.KEYSTORE_PASSWORD }}
```

读取。

这样密码不会直接写在 Git 仓库中。

---

# 32. Public Repository 要特别注意 Secrets

公开仓库意味着：

> **任何人都可以查看你的源码和 Git 历史。**

所以绝对不要提交：

```text
API Key
OpenAI Key
数据库密码
云服务器密码
Keystore 私钥
支付相关凭证
OAuth Secret
```

即使后来删除并重新 Commit：

> 旧值仍然可能存在于 Git History 中。

如果 Secret 已经提交到公开仓库，应优先：

1. 立即撤销旧密钥
2. 生成新密钥
3. 改用 GitHub Secrets
4. 必要时清理 Git 历史

不要只删除当前文件就认为安全了。

---

# 33. Public 和 Private 怎么选

## 公开仓库适合

```text
开源项目
课程 Demo
练习项目
不包含私人代码
不包含商业机密
```

优势：

```text
标准 GitHub-hosted Runner 免费
公开协作方便
别人可以查看源码
```

## 私有仓库适合

```text
个人未公开项目
课程作业不希望公开
商业项目
包含业务逻辑
还没准备公开的 App
```

GitHub Free 当前提供：

```text
2,000 Actions 分钟/月
```

对普通个人 Android 项目通常已经比较充足。

---

# 34. Debug APK 推荐的完整 Workflow

如果只是想实现：

> **本地写代码 → Push → 自动得到 APK**

建议直接使用下面这一份：

```yaml
name: Build Android APK

on:
  push:
    branches:
      - main

  workflow_dispatch:

concurrency:
  group: android-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout source
        uses: actions/checkout@v6

      - name: Setup Java
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 17

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v6

      - name: Make Gradle Wrapper executable
        run: chmod +x gradlew

      - name: Build APK
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7
```

---

# 35. 第一次配置的完整操作步骤

## 第一步：建立 GitHub Repository

GitHub：

```text
New repository
```

选择：

```text
Public
```

或者：

```text
Private
```

---

## 第二步：把 Android 项目放进去

本地：

```bash
git init
git add .
git commit -m "Initial Android project"
git branch -M main
git remote add origin <你的仓库地址>
git push -u origin main
```

如果已经是 Git 仓库，就不需要重新 `git init`。

---

## 第三步：创建 Workflow

创建目录：

```text
.github/workflows/
```

创建：

```text
android-build.yml
```

把前面的完整 YAML 放进去。

---

## 第四步：提交 Workflow

```bash
git add .github/workflows/android-build.yml
git commit -m "Add Android GitHub Actions build"
git push
```

---

## 第五步：GitHub 自动开始构建

网页打开：

```text
Repository
 ↓
Actions
```

应该看到：

```text
Build Android APK
```

黄色：

```text
正在运行
```

绿色：

```text
成功
```

红色：

```text
失败
```

---

## 第六步：下载 APK

成功后：

```text
Actions
 ↓
此次 Workflow Run
 ↓
Artifacts
 ↓
app-debug
```

下载 ZIP。

解压：

```text
app-debug.apk
```

---

## 第七步：真机测试

把 APK 发送到 Android 手机。

然后安装：

```text
app-debug.apk
```

Android 可能要求你为文件管理器 / 浏览器允许：

```text
安装未知来源应用
```

具体名称根据 Android 厂商和版本有所不同。

---

# 36. 最推荐的开发架构

如果电脑性能或磁盘空间有限：

```text
┌────────────────────────────┐
│ 本机                       │
│                            │
│ VS Code / Zed              │
│ Git                        │
│ Android 源码               │
│                            │
│ 不安装 Android SDK         │
│ 不安装 Emulator            │
└────────────┬───────────────┘
             │
           git push
             │
             ▼
┌────────────────────────────┐
│ GitHub                     │
│ Repository                 │
└────────────┬───────────────┘
             │
       触发 GitHub Actions
             │
             ▼
┌────────────────────────────┐
│ GitHub-hosted Runner       │
│                            │
│ Ubuntu                     │
│ Java                       │
│ Android SDK                │
│ Gradle                     │
│                            │
│ ./gradlew assembleDebug    │
└────────────┬───────────────┘
             │
             ▼
       app-debug.apk
             │
             ▼
       Actions Artifact
             │
             ▼
          下载 ZIP
             │
             ▼
         Android 真机
```

---

# 37. Actions 和 Codespaces 的区别

| 功能 | GitHub Actions | Codespaces |
|---|---|---|
| 本质 | 自动执行服务器 | 云端开发电脑 |
| 浏览器里写代码 | 不适合 | 适合 |
| 交互式终端 | 不是主要用途 | 是 |
| 自动构建 | 非常适合 | 可以但不是核心用途 |
| Push 后自动执行 | 是 | 不是核心功能 |
| Runner / VM 长期保存 | 否 | Codespace 可以保存 |
| Android 编译 | 是 | 是 |
| Gradle Cache | 是 | 环境本身可长期保存 |
| 本机要求 | 极低 | 极低 |
| 适合本地写代码 + 云编译 | **非常适合** | 没必要 |
| 适合完全云端写代码 | 不适合 | **非常适合** |

如果你愿意本地使用 VS Code：

> **本地 VS Code + GitHub + GitHub Actions**

通常比 Codespaces 更省额度。

---

# 38. 推荐方案总结

对于“本机性能有限，但仍然想开发 Android”的情况：

## 推荐方案

```text
本机：
VS Code
Git
源码

        ↓

GitHub：
Private / Public Repository

        ↓

GitHub Actions：
JDK
Android SDK
Gradle
编译 APK

        ↓

Artifact：
保存 APK

        ↓

Android 真机：
安装测试
```

优势：

- 本机不用跑 Gradle
- 本机不用安装 Android Emulator
- 可以不安装 Android SDK
- 大幅减少本机磁盘占用
- 本机 CPU / RAM 要求低
- Actions 自动构建
- Gradle 依赖可以缓存
- APK 可以直接网页下载

主要缺点：

- 每次验证代码需要 Push
- 编译错误需要看 Actions 日志
- 没有 Android Studio 的完整实时预览体验
- 网络不好时体验下降
- 私有仓库需要注意 Actions 免费分钟额度

---

# 39. 代码组织与文件拆分规范（开发强制约定）

为了方便阅读、定位问题、单元测试和后续维护，项目开发时应按职责拆分代码，避免把界面、业务逻辑、网络请求和数据存储全部写进同一个文件。

## 39.1 基本原则

- 一个文件只承担一种主要职责
- 页面、可复用组件、状态管理、业务逻辑和数据访问分别放置
- 不在 `MainActivity` 或单个 Compose 页面中堆放整个应用逻辑
- 可复用 UI 组件单独建文件，不复制粘贴相同代码
- 数据模型、接口定义、Repository 和本地存储代码分开
- 通用常量、扩展函数和工具类按用途分文件，避免建立无边界的万能工具类
- 文件和类使用能表达用途的名称，不使用 `Utils2`、`Temp`、`TestNew` 等含糊命名
- 拆分应以清晰为目标；只有几行且只在一处使用的私有辅助代码不必机械拆成独立文件

## 39.2 推荐目录结构

以 Kotlin、Jetpack Compose 和 MVVM 为例：

```text
app/src/main/java/com/example/app/
├── MainActivity.kt
├── navigation/
│   ├── AppNavHost.kt
│   └── Routes.kt
├── ui/
│   ├── screen/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   └── HomeUiState.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       ├── SettingsViewModel.kt
│   │       └── SettingsUiState.kt
│   ├── component/
│   │   ├── AppButton.kt
│   │   └── LoadingView.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── domain/
│   ├── model/
│   └── usecase/
├── data/
│   ├── repository/
│   ├── remote/
│   └── local/
└── util/
```

目录可根据项目规模精简，但职责边界应保留。例如，小型应用可以暂时不建立 `usecase` 层，但不能把网络请求直接写入 UI 文件。

## 39.3 各类文件职责

| 类型 | 主要职责 | 不应包含 |
|---|---|---|
| `Activity` | 应用入口、挂载 Compose、系统级交互 | 具体业务流程和数据访问 |
| `Screen` | 页面布局、展示状态、转发用户操作 | 网络请求、数据库读写 |
| `Component` | 可复用的小型 UI 部件 | 页面专属的复杂业务逻辑 |
| `ViewModel` | 管理页面状态、响应事件、调用业务或数据层 | 具体 UI 布局代码 |
| `UiState` | 描述页面当前状态 | 数据获取和状态修改逻辑 |
| `Repository` | 统一协调远程与本地数据来源 | Compose UI 代码 |
| `Remote` / `Local` | 网络接口、数据库或文件读写 | 页面状态和导航逻辑 |
| `Model` | 表达业务数据结构 | 副作用和界面代码 |

## 39.4 开发与审查要求

每次新增功能时，至少检查：

1. 新代码是否放在职责正确的目录和文件中
2. 是否出现过大的类、函数或 Compose 页面
3. UI 是否直接访问网络、数据库或文件系统
4. 重复组件和重复业务逻辑是否可以复用
5. 文件名、类名和函数名是否能直接说明用途
6. 删除某个页面时，其专属代码能否被清晰识别并安全删除

如某个文件持续膨胀，应优先按页面区块、状态、事件处理或数据职责拆分，而不是继续向同一文件追加代码。

---

# 40. 当前官方额度速查（2026-08-31）

## GitHub Free

### GitHub Actions

```text
Private：
2,000 分钟/月

Artifact：
500 MB

Cache：
默认每个 Repository 10 GB
```

### Public Repository

```text
标准 GitHub-hosted Runner：
免费使用
```

但 Larger Runner 即使在公开仓库中也可能收费，因此普通项目建议：

```yaml
runs-on: ubuntu-latest
```

即可。

### 标准 Runner 单个 Job

```text
最长执行时间：6 小时
```

### Free 标准 Runner 并发

```text
20 个 Job
```

---

# 41. 官方资料

以下链接可用于核对最新规则。GitHub 可能以后调整额度，因此长期使用时应以官方页面为准。

- GitHub Actions：
  https://github.com/features/actions

- GitHub Actions 文档：
  https://docs.github.com/en/actions

- GitHub Actions 计费：
  https://docs.github.com/en/billing/concepts/product-billing/github-actions

- GitHub Actions Limits：
  https://docs.github.com/en/actions/reference/limits

- GitHub-hosted Runners：
  https://docs.github.com/en/actions/reference/runners/github-hosted-runners

- Workflow Artifact：
  https://docs.github.com/en/actions/tutorials/store-and-share-data

- Gradle 官方 GitHub Actions：
  https://github.com/gradle/actions

---

# 42. 最简记忆版

只记住下面这条链路即可：

```text
本地写 Kotlin
    ↓
git push
    ↓
GitHub Actions
    ↓
./gradlew assembleDebug
    ↓
app-debug.apk
    ↓
Upload Artifact
    ↓
GitHub 网页下载
    ↓
安卓真机安装
```

核心 Workflow：

```yaml
name: Build Android APK

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v6

      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 17

      - uses: gradle/actions/setup-gradle@v6

      - run: chmod +x gradlew

      - run: ./gradlew assembleDebug

      - uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7
```

如果项目能够在正常 Android 开发环境执行：

```bash
./gradlew assembleDebug
```

那么这份 Workflow 就是一个非常适合作为起点的 GitHub Actions 云端构建方案。
