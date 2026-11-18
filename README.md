# GameMale Native

一个**非官方**的 GameMale 论坛 Android 客户端，用原生 Compose（COUI / ColorOS 风格）重做移动端阅读体验。

> 本项目不是 GameMale 官方应用，仅作个人学习与自用。所有内容版权归论坛及原帖作者所有，使用时请遵守论坛规则。

**代码全程由 AI 编写**（需求提出、验收判断与真机测试由项目发起人完成）。开源出来主要是给同样想改造 Discuz 论坛阅读体验的人做参考，不保证生产可用。

## 特性

- **原生界面**：Jetpack Compose + [COUI-Kit](https://github.com/itoseo/coui)（仿 ColorOS 17），强调色跟随系统 Monet。
- **隐藏 WebView 取数**：用 1dp 不可见 WebView 完成 Cloudflare 校验、携带登录 Cookie 抓取 HTML，再用 Jsoup 解析成原生数据模型——不渲染网页，也不套壳。
- 版块列表 / 分类 tab / 帖子详情 / 附件列表 / 评分 / 回复 / 搜索 / 登录。
- 底部导航栏两个按钮分别直达 **和谐动漫**（`forumdisplay&fid=150&typeid=831`）与 **和谐游戏**（`forumdisplay&fid=152&typeid=737`），启动默认进「和谐动漫」。
- 列表附图预览（正文首图回填，1 张整宽、2–3 张横排、≥4 张九宫格）。
- 图片查看器支持双指缩放、双击放大、单指拖动。
- 深色模式跟随系统并可持久化；状态栏图标自动反色。
- 帖子链接可作为深链打开：`https://www.gamemale.com/thread-*` 直接进详情；也支持从系统分享面板接收链接。
- 播放/提交类操作失败会落盘诊断文件（`下载/gamemale-submit.html` 等）。

## 技术结构

```
app/src/main/java/com/gamemale/client/
├── MainActivity.kt   // 状态机、导航、登录、评分/回复提交、深链
├── Ui.kt             // 全部 Compose 界面（顶栏/列表/详情/弹窗/底部导航）
├── Site.kt           // 数据模型 + Jsoup 解析（版块、帖子、表单、图片）
├── Fetcher.kt        // 隐藏 WebView 取数、POST、页面内表单提交
├── Theme.kt          // COUI 主题与配色
└── App.kt
```

关键设计：

- 站点会按 UA 下发手机模板，桌面 DOM 节点不存在。因此所有请求统一附加 `mobile=no` 并写同名 Cookie，**不改 UA**（改 UA 会触发 Cloudflare 600010）。
- 图片加载走 Coil，并自定义 OkHttpClient 注入 `Referer` / Cookie / UA，否则图片域会 403。
- 评分表单是 `input[name=scoreN]` 文本框（默认值 0），分值上限来自旁边的 `#scoreoptionN` 列表；提交必须带 `ratesubmit=yes`，否则站点返回“请输入正确的分值”。

## 构建

要求：JDK 17、Android SDK（compileSdk / targetSdk 37、build-tools 37.0.0）。

```bash
# local.properties 里写自己的 SDK 路径
echo "sdk.dir=/path/to/android-sdk" > local.properties

./gradlew :app:assembleRelease
```

产物：`app/build/outputs/apk/release/app-release-unsigned.apk`，需要自行 zipalign + apksigner 签名后安装。

> 低内存机器建议在 `gradle.properties` 保持 `-Xmx1536m`，否则 Kotlin 编译可能 OOM。

## 版本

当前 `versionCode 35` / `3.0-M16`。历史迭代（M1 → M16）主要在修：移动模板识别、分类 tab 状态合并、附件下载、图片 Referer、列表附图回填、评分/回复表单字段、深链与分享接收、图片兜底与加载遮罩、评分成功判定与兜底表单。

## 已知限制

- 取数依赖站点现行模板，站点改版会导致解析失效（错误页可直接导出当前 HTML 用于排查）。
- 站点未在列表页提供缩略图，附图预览需要逐帖抓正文首图，因此会延迟出现（只补前几条）。
- 评分默认提交各维度的最大合法分值，未做逐项微调界面。

## License

MIT，见 [LICENSE](LICENSE)。
