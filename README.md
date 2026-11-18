# GameMale Native

一个**非官方**的 GameMale 论坛 Android 客户端，用原生 Compose（COUI / ColorOS 17 风格）把 Discuz 论坛重做成移动端阅读体验。

> 本项目与 GameMale 官方无关，仅作个人学习与自用。所有帖子内容、图片、附件的版权归论坛及原帖作者所有，使用时请遵守论坛规则，不要用于商业用途或批量抓取。

**代码全程由 AI 编写**：需求提出、方案取舍、验收判断与真机测试由项目发起人完成，实现由 AI 完成。开源出来是给同样想改造 Discuz 阅读体验的人做参考，不保证生产可用。

当前版本：`3.0-M27`（versionCode 46），实测机 OnePlus PLK110 / Android 17 (API 37)，`targetSdk 37`，单 APK 支持 Android 12+。

## 它是什么

不套壳、不注入 CSS、不渲染网页。整个 App 是原生 Compose 界面，数据来自一个 **1dp 的隐藏 WebView**：它负责过 Cloudflare 校验、带上登录 Cookie 取回 HTML，然后交给 Jsoup 解析成原生数据模型。

## 功能

- **首页两个板块**：底部导航「和谐动漫」（`fid=150`、`typeid=831`，兽人漫）与「和谐游戏」（`fid=152`、`typeid=737`，兽人向游戏），启动默认进和谐动漫；两个一级页之间切换不重载、不压栈。
- **帖子列表**：标题 / 分类标签 / 作者 / 时间 / 回复数 / 查看数，正文首图回填预览（1 张整宽、2–3 张横排、≥4 张九宫格）；下拉刷新只占顶部一条提示，不刷掉旧列表。
- **帖子详情**：正文分段渲染（文本 / 图片 / 内联超链接 / 折叠块 `[spoiler]`），评论区楼层、页码、评分统计；正文内嵌附图与付费附件（大小、下载次数、售价、阅读权限）单独成块。
- **交互**：回复（自动唤起键盘 + 提交动画）、评分（血液 / 追随 / 堕落）、购买附件、下载附件、跳到浏览器打开外链。
- **搜索**：独立页面，进页自动聚焦，带搜索历史；命中站点频率限制时给出明确提示。
- **登录**：账号密码登录，登录态靠 Cookie 维持；顶栏只显示头像。
- **图片**：点图片直接交给系统相册查看（先把图片落到 App 私有目录，再用 `FileProvider` 授权）。
- **深链与分享**：`https://www.gamemale.com/thread-*` 可直接打开详情；也接收系统分享面板来的帖子链接。
- **缓存**：帖子整页 HTML、正文图片、列表预览图三类都落盘，重进不重复联网；默认上限 300 MB，可在设置页改成 100 / 300 / 500 / 1000 MB 或手动清除。
- **设置页**：字体缩放、深色模式（跟随系统 / 常亮 / 常暗）、缓存上限与清理、关于页。

## 技术结构

```
app/src/main/java/com/gamemale/client/
├── MainActivity.kt   // 状态机、导航、登录、评分/回复/搜索、图片与缓存调度
├── Ui.kt             // 全部 Compose 界面（顶栏、列表、详情、弹窗、设置页）
├── Site.kt           // 数据模型 + Jsoup 解析（板块、帖子、表单、附件、折叠块）
├── Fetcher.kt        // 隐藏 WebView 取数、POST、页面内表单提交（请求串行队列）
├── ThreadCache.kt    // 整页缓存 ThreadCache、图片 ImageStore、预览图 ThumbStore
├── Theme.kt          // COUI 主题、Monet 动态取色、深色模式
└── App.kt
```

几个绕不开的关键点（改代码前建议先看）：

1. **模板钉桌面版**：站点按 UA 下发手机模板 `template/mwt2`，桌面节点根本不存在。所以所有请求统一附加 `mobile=no` 并写同名 Cookie，**但不改 UA**——伪装桌面 UA 会直接触发 Cloudflare 600010。
2. **单 WebView 必须串行**：`Fetcher` 只有一个 WebView，两个请求并发会互相覆盖回调，出现"A 帖的预览图贴到 B 帖""点开 N 显示 M"这类错乱。所有取数请求都进 `Fetcher` 的队列依次执行，并且拿回 HTML 后要校验 `thread-<tid>` 是否对得上。
3. **解析不进主线程**：600 KB 级页面用 Jsoup 解析很吃内存，集中在主线程会 `OutOfMemoryError`。预览图解析走独立后台线程，整页缓存在内存里按 3 MB 总量封顶，同时开了 `android:largeHeap`。
4. **图片必须带 Referer**：图片域会 403，Coil 的 OkHttpClient 要注入 `Referer` 与 Cookie。
5. **评分表单是文本框**：`input[name=scoreN]`（血液 `score3`、追随 `score4`、堕落 `score8`），分值上限在旁边的 `#scoreoptionN` 列表里；提交必须带 `ratesubmit=yes`。
6. **成功/失败不要只看返回值**：站点成功时会在返回 XML 里混入"回复奖励"文案，容易误判。回复改为提交后重新抓帖子页、用刚发内容做探针核对；只要没有 `errorhandle_` / `alert_error` 就算成功。

## 构建

要求 JDK 17、Android SDK（`compileSdk`/`targetSdk` 37、build-tools 37.0.0）、Gradle 9.6、AGP 9.4.1、Kotlin 2.4.20。

```bash
# 1. 指向自己的 SDK
echo "sdk.dir=/path/to/android-sdk" > local.properties

# 2. 出 release 包
./gradlew :app:assembleRelease
```

产物是 `app/build/outputs/apk/release/app-release-unsigned.apk`，需要自行对齐与签名：

```bash
zipalign -f -p 4 app-release-unsigned.apk aligned.apk
apksigner sign --ks your.keystore --out Gamemale.apk aligned.apk
```

> `gradle.properties` 里 `-Xmx1536m` 不要调小，否则 Kotlin 编译阶段容易 OOM。

## 已知限制

- 解析依赖站点现行模板，站点改版会失效。App 内置诊断：出错时可把当前 HTML 导出到 `下载/gamemale-debug.html`，方便比对修复。
- 列表页站点本身不提供缩略图，预览图只能逐帖抓正文首图，所以会延迟出现；已缓存的帖子直接从本地读，不再联网。
- 评分默认提交各维度最大合法分值，没有做逐项微调界面。
- 内置的浏览器取数依赖 Android WebView，非 Android 平台无法直接复用。

## License

MIT，见 [LICENSE](LICENSE)。使用本项目造成的任何后果由使用者自行承担。
