package com.gamemale.client

/**
 * 更新日志：历代版本的新增功能与修复点。
 * 展示时新增功能加 ✨ 前缀，修复点排在后面。
 */
object Changelog {

    data class Release(
        val version: String,
        val features: List<String>,
        val fixes: List<String>
    )

    /** 新版本在上 */
    val all: List<Release> = listOf(
        Release(
            "4.3.1",
            features = listOf(
                "设置页「内容偏好」的屏蔽词换了图标：圆圈加斜杠的禁止符号，不再是放大镜"
            ),
            fixes = listOf(
                "修从旧版本升级后首次启动不弹更新日志：旧版本没留下版本号，改为按设备是否用过判断，且关闭弹窗后才记账",
                "修内容偏好批量输入过长时「添加 / 取消」被挤出屏幕；回复、评分弹窗的输入区改成固定高度、内部滚动",
                "修浏览记录页长按帖子闪退：选择态的删除胶囊不再叠离屏模糊层，拖拽手势协程加保护",
                "修已读只把标签压灰：整卡压灰并加透明度，标题与预览图一起变灰"
            )
        ),
        Release(
            "4.3.0",
            features = listOf(
                "设置页「账号管理」下新增「内容偏好」，含屏蔽词与特别关注；支持换行批量录入，已有关键词可逐条删除",
                "屏蔽词命中的帖子直接从首页列表消失；特别关注关键词命中的帖子标题前加 ✨",
                "新增已读记录：打开过的帖子在列表里整卡压灰，仅改颜色，不影响点击；已读数据独立于浏览记录，删浏览记录不会清掉已读",
                "设置页「关于」新增更新日志入口，可翻看历代版本的新增功能与修复点",
                "升级后首次启动自动比对升级前后的版本区间，弹窗汇总这段时间的更新点，新增功能排在前"
            ),
            fixes = listOf(
                "内容偏好只作用于首页「和谐动漫 / 和谐游戏 / 浏览记录」，搜索结果页与外部分享进来的帖子不受影响"
            )
        ),
        Release(
            "4.2.1",
            features = listOf(
                "每日自动任务执行时给出过程提示（正在签到、正在翻卡、完成汇总）",
                "自动任务失败时，头像菜单出现「手动签到 / 手动抽卡」，点击用系统浏览器打开对应页面"
            ),
            fixes = listOf(
                "修签到 / 抽卡实际成功却提示失败：判定改用动作接口返回文本，不再重载页面二次核验",
                "修首页列表左右滑动无法切换板块"
            )
        ),
        Release(
            "4.2.0",
            features = listOf(
                "每日首次启动自动签到，并自动翻卡抽奖",
                "首页帖子列表左右滑动可直接切换板块",
                "按住拖动的高亮块改为匀速跟随，松手直接执行落点那一项",
                "图片查看器以手指位置为锚点缩放，并按页保存各自的缩放状态"
            ),
            fixes = listOf(
                "修底部导航拖拽命中只判纵向，导致始终命中第一项"
            )
        ),
        Release(
            "4.1.0",
            features = listOf(
                "头像菜单与底部导航支持按住拖动「指哪打哪」",
                "浏览记录选择模式的删除按钮改为警示红，并加模糊背景",
                "首页右下角搜索 FAB 加同款模糊背景"
            ),
            fixes = listOf(
                "修列表惯性滑回顶部时误触发下拉刷新",
                "修帖子详情回复 FAB 与首页搜索 FAB 距屏幕底部不一致"
            )
        ),
        Release(
            "4.0.6",
            features = listOf(
                "搜索先铺缓存结果，再后台取最新并提示刷新中"
            ),
            fixes = listOf(
                "顶栏占位改用 CompositionLocal 下发，修冷启动首帖压到顶栏下",
                "修正刷新浮层与列表的覆盖顺序",
                "统一两个 FAB 与屏幕边缘的间距"
            )
        ),
        Release(
            "4.0.5",
            features = listOf(
                "搜索保留上一次结果，刷新只覆盖顶部提示"
            ),
            fixes = listOf(
                "顶栏 inset 改为状态，修冷启动算出偏小后不再恢复"
            )
        ),
        Release(
            "4.0.4",
            features = listOf(
                "顶栏模糊加强，下拉刷新改为重叠式布局"
            ),
            fixes = listOf(
                "修顶栏模糊在边界处硬切"
            )
        ),
        Release(
            "4.0.3",
            features = listOf(
                "折叠块内的图片进入图片查看器",
                "设置页淡入与图层过渡"
            ),
            fixes = listOf(
                "修顶栏渐隐遮罩过渡"
            )
        ),
        Release(
            "4.0.2",
            features = listOf(
                "顶栏模糊背景与刷新浮层"
            ),
            fixes = listOf(
                "修下拉刷新时列表把刷新动画盖住"
            )
        ),
        Release(
            "4.0.1",
            features = listOf(
                "顶栏渐隐过渡"
            ),
            fixes = listOf(
                "修顶栏模糊到中途突然截断"
            )
        )
    )

    private fun parts(v: String): List<Int> =
        v.trim().removePrefix("v").split('.').map { it.toIntOrNull() ?: 0 }

    private fun cmp(a: String, b: String): Int {
        val x = parts(a)
        val y = parts(b)
        for (i in 0 until maxOf(x.size, y.size)) {
            val l = x.getOrElse(i) { 0 }
            val r = y.getOrElse(i) { 0 }
            if (l != r) return l.compareTo(r)
        }
        return 0
    }

    /** 取 (from, to] 区间内的版本；from 为空表示全新安装，返回空 */
    fun between(from: String?, to: String): List<Release> {
        if (from.isNullOrBlank()) return emptyList()
        if (cmp(from, to) >= 0) return emptyList()
        return all.filter { cmp(it.version, from) > 0 && cmp(it.version, to) <= 0 }
            .sortedByDescending { r -> parts(r.version).fold(0) { acc, n -> acc * 1000 + n } }
    }

    fun find(version: String): Release? = all.firstOrNull { cmp(it.version, version) == 0 }
}
