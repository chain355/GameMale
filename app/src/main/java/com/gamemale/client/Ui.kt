package com.gamemale.client

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import coil.compose.SubcomposeAsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import com.itos.coui.icons.CouiIcons
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.itos.coui.theme.CouiTheme

private const val ANIME_URL =
    "https://www.gamemale.com/forum.php?mod=forumdisplay&fid=150&filter=lastpost&orderby=lastpost&typeid=831"
private const val GAME_URL =
    "https://www.gamemale.com/forum.php?mod=forumdisplay&fid=152&filter=lastpost&orderby=lastpost&typeid=737"

/** 列表的“回复气泡”图标（COUI 图标集没有评论类图标，这里自绘一个） */
private val ReplyVector: ImageVector by lazy {
    ImageVector.Builder("ReplyBubble", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 3f)
            lineTo(20f, 3f)
            lineTo(22f, 6f)
            lineTo(22f, 14f)
            lineTo(19f, 17f)
            lineTo(11f, 17f)
            lineTo(6f, 21f)
            lineTo(6f, 17f)
            lineTo(4f, 14f)
            close()
        }
    }.build()
}

@Composable
private fun glassModifier(shape: Shape, alpha: Float = 0.86f): Modifier {
    val colors = CouiTheme.colorScheme
    return Modifier
        .clip(shape)
        .background(colors.container.copy(alpha = alpha))
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.03f))
            ),
            shape = shape
        )
}

/**
 * COUI 风格的按压反馈：
 * - 光纹跟随手指移动（不是只亮在按下的那一点）
 * - 手指拖动时按钮朝拖动方向轻微拉伸变形
 * - 松手回弹，点按加亮而不是压暗
 */
@Composable
private fun Modifier.lightPress(onClick: () -> Unit): Modifier {
    var press by remember { mutableStateOf<Offset?>(null) }
    var drag by remember { mutableStateOf(Offset.Zero) }
    var pressing by remember { mutableStateOf(false) }
    val pressAmt by animateFloatAsState(if (pressing) 1f else 0f, tween(160))
    val glow = remember { Animatable(0f) }
    LaunchedEffect(press != null) {
        glow.animateTo(if (press != null) 1f else 0f, tween(160))
        if (press == null) drag = Offset.Zero
    }

    return this
        .graphicsLayer(
            scaleX = 1f - 0.09f * pressAmt + (drag.x / 420f).coerceIn(-0.14f, 0.14f) * pressAmt,
            scaleY = 1f - 0.09f * pressAmt + (drag.y / 900f).coerceIn(-0.07f, 0.07f) * pressAmt
        )
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                press = down.position
                pressing = true
                var moved = 0f
                var last = down.position
                while (true) {
                    val event = awaitPointerEvent()
                    val ch = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!ch.pressed) {
                        press = null
                        pressing = false
                        if (moved < 24f) onClick()
                        break
                    }
                    val delta = ch.position - last
                    last = ch.position
                    moved += delta.getDistance()
                    drag += delta
                    press = ch.position
                    ch.consume()
                }
            }
        }
        .drawWithContent {
            drawContent()
            val p = press
            if (p != null && glow.value > 0f) {
                val r = size.maxDimension * 0.95f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.34f * glow.value), Color.Transparent),
                        center = p,
                        radius = r
                    ),
                    radius = r,
                    center = p
                )
            }
        }
}

/** 通用“分享”图标：三个节点 + 两条连线 */
private val ShareVector: ImageVector by lazy {
    fun ImageVector.Builder.dot(cx: Float, cy: Float, r: Float) = path(fill = SolidColor(Color.White)) {
        moveTo(cx, cy - r * 1.08f)
        lineTo(cx + r * 0.78f, cy - r * 0.38f)
        lineTo(cx + r, cy)
        lineTo(cx + r * 0.78f, cy + r * 0.38f)
        lineTo(cx, cy + r * 1.08f)
        lineTo(cx - r * 0.78f, cy + r * 0.38f)
        lineTo(cx - r, cy)
        lineTo(cx - r * 0.78f, cy - r * 0.38f)
        close()
    }

    ImageVector.Builder("Share", 24.dp, 24.dp, 24f, 24f)
        // 连线（上）
        .path(fill = SolidColor(Color.White)) {
            moveTo(6.4f, 10.9f)
            lineTo(17.0f, 6.3f)
            lineTo(17.0f, 7.7f)
            lineTo(6.4f, 12.3f)
            close()
        }
        // 连线（下）
        .path(fill = SolidColor(Color.White)) {
            moveTo(6.4f, 11.7f)
            lineTo(17.0f, 16.3f)
            lineTo(17.0f, 17.7f)
            lineTo(6.4f, 13.1f)
            close()
        }
        .dot(5f, 12f, 2.7f)
        .dot(18.4f, 6.2f, 2.7f)
        .dot(18.4f, 17.8f, 2.7f)
        .build()
}

@Composable
private fun IconImg(image: ImageVector, size: Dp, tint: Color) {
    Image(
        painter = rememberVectorPainter(image),
        contentDescription = null,
        modifier = Modifier.size(size),
        colorFilter = ColorFilter.tint(tint)
    )
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) colors.accent.copy(alpha = 0.16f) else Color.Transparent)
            .lightPress(onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconImg(icon, 20.dp, if (active) colors.accent else colors.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Txt(
            label,
            size = 11.sp,
            color = if (active) colors.accent else colors.onSurfaceVariant,
            weight = if (active) FontWeight.SemiBold else null
        )
    }
}

@Composable
private fun SearchDialog(onCancel: () -> Unit, onSearch: (String) -> Unit) {
    val colors = CouiTheme.colorScheme
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onCancel) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.background)
                .padding(18.dp)
        ) {
            Txt("搜索帖子", size = 17.sp, weight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.container)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (text.isEmpty()) Txt("输入关键词", size = 14.sp, color = colors.onSurfaceVariant)
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Pill("取消", onCancel, primary = false)
                Spacer(Modifier.width(10.dp))
                Pill("搜索", {
                    if (text.isNotBlank()) onSearch(text.trim())
                }, primary = true)
            }
        }
    }
}

@Composable
fun Txt(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    size: androidx.compose.ui.unit.TextUnit = 15.sp,
    weight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
    lineHeight: Float = 1.45f,
    overflow: TextOverflow = TextOverflow.Clip
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight,
            textAlign = align ?: TextAlign.Start,
            lineHeight = (size.value * lineHeight).sp
        ),
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun AppUi(
    nav: Nav,
    forums: List<ForumItem>,
    forumPage: ForumPage?,
    threadPage: ThreadPage?,
    loading: Boolean,
    error: String?,
    dark: Boolean,
    host: android.view.View,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenForum: (ForumItem) -> Unit,
    onOpenThread: (ThreadItem) -> Unit,
    onLoad: (String) -> Unit,
    onToggleDark: () -> Unit,
    onLogin: () -> Unit,
    onExternal: (String) -> Unit,
    onDump: () -> Unit,
    onQuickForum: (String, String) -> Unit,
    onSearch: (String) -> Unit,
    showBottomBar: Boolean,
    loginName: String?,
    loggedIn: Boolean,
    avatarUrl: String?,
    onShare: () -> Unit,
    onReply: () -> Unit,
    onRate: (String) -> Unit,
    onAttach: (AttachItem) -> Unit,
    onImage: (String) -> Unit,
    replyOpen: Boolean,
    onCancelReply: () -> Unit,
    onSendReply: (String) -> Unit,
    onCancelRate: () -> Unit,
    rateOpen: Boolean,
    rateFields: List<Triple<String, String, String>>,
    onSendRate: (String) -> Unit,
    imageUrl: String?,
    onCloseImage: () -> Unit,
    busy: Boolean,
    currentUrl: String
) {
    val colors = CouiTheme.colorScheme
    val forumListState = rememberLazyListState()
    var searchOpen by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                loginName = loginName,
                canShare = nav is Nav.Thread,
                avatarUrl = avatarUrl,
                onShare = onShare,
                onDump = onDump,
                title = when (nav) {
                    is Nav.Home -> "GameMale"
                    is Nav.Forum -> nav.title.ifEmpty { "版块" }
                    is Nav.Thread -> nav.title.ifEmpty { "帖子" }
                },
                canBack = nav != Nav.Home,
                dark = dark,
                onBack = onBack,
                onToggleDark = onToggleDark,
                onLogin = onLogin
            )
            Box(Modifier.weight(1f)) {
                    when {
                        error != null -> ErrorView(error, onRetry, onDump, onLogin)
                        nav == Nav.Home -> HomeList(forums, loading, onOpenForum)
                        nav is Nav.Forum -> ForumView(forumPage, loading, onOpenThread, onLoad, currentUrl, onImage, forumListState)
                        nav is Nav.Thread -> ThreadView(
                            threadPage, loading, onLoad, onExternal,
                            onReply, onRate, onAttach, onImage
                        )
                    }
                    if (loading) LoadingLayer("正在加载…")
                }
            }

        // 隐藏的取数 WebView（1dp，不渲染任何可见内容）
        AndroidView(factory = { host }, modifier = Modifier.size(1.dp))

        if (busy) LoadingLayer("处理中…")

        if (nav is Nav.Thread && !replyOpen) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(end = 18.dp, bottom = if (showBottomBar) 92.dp else 22.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.92f))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))
                        ),
                        CircleShape
                    )
                    .lightPress { onReply() },
                contentAlignment = Alignment.Center
            ) {
                IconImg(ReplyVector, 24.dp, colors.onAccent)
            }
        }
        // 底部导航（动漫 / 游戏）——只在根级列表页出现，且宽度收紧
        if (showBottomBar) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(start = 14.dp, bottom = 10.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.container)
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.03f))
                        ),
                        RoundedCornerShape(26.dp)
                    )
            ) {
                Row(
                    Modifier.fillMaxHeight().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                val cur = currentUrl
                NavButton(
                    icon = CouiIcons.Outlined.Album,
                    label = "动漫",
                    active = cur.contains("fid=150"),
                    modifier = Modifier.width(66.dp)
                ) { onQuickForum(ANIME_URL, "动漫") }
                NavButton(
                    icon = CouiIcons.Filled.Gamepad,
                    label = "游戏",
                    active = cur.contains("fid=152"),
                    modifier = Modifier.width(66.dp)
                ) { onQuickForum(GAME_URL, "游戏") }
                }
            }

            // 搜索 FAB 独立漂浮在导航栏右侧，不放在胶囊里
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(end = 16.dp, bottom = 10.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.92f))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))
                        ),
                        CircleShape
                    )
                    .lightPress { searchOpen = true },
                contentAlignment = Alignment.Center
            ) {
                IconImg(CouiIcons.Outlined.Search, 24.dp, colors.onAccent)
            }
        }

        if (searchOpen) {
            SearchDialog(
                onCancel = { searchOpen = false },
                onSearch = {
                    searchOpen = false
                    onSearch(it)
                }
            )
        }
        if (replyOpen) ReplyDialog(onCancelReply, onSendReply)
        if (rateOpen) RateDialog(rateFields, onCancelRate, onSendRate)
        if (imageUrl != null) ImageDialog(imageUrl, onCloseImage)
    }
}

@Composable
private fun TopBar(
    title: String,
    loginName: String?,
    canBack: Boolean,
    dark: Boolean,
    canShare: Boolean,
    avatarUrl: String?,
    onBack: () -> Unit,
    onToggleDark: () -> Unit,
    onLogin: () -> Unit,
    onShare: () -> Unit,
    onDump: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    // 不透明顶栏（已放弃模糊），并让下面的内容从它下方开始
    Box(
        Modifier
            .fillMaxWidth()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
    Row(
        Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (canBack) {
            CircleBtn(CouiIcons.Filled.Back, onBack)
            Spacer(Modifier.width(6.dp))
        }
        Txt(
            title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .clickable { onDump() },
            size = 17.sp,
            weight = FontWeight.Bold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (canShare) {
            CircleBtn(ShareVector, onShare)
            Spacer(Modifier.width(8.dp))
        }
        CircleBtn(if (dark) CouiIcons.Filled.Sun else CouiIcons.Filled.DarkLight, onToggleDark)
        Spacer(Modifier.width(8.dp))
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .clickable { onLogin() }
            )
        } else {
            CircleBtn(CouiIcons.Filled.UserCircle, onLogin)
        }
    }
    }
}

@Composable
private fun CircleBtn(icon: ImageVector, onClick: () -> Unit) {
    val colors = CouiTheme.colorScheme
    Box(
        Modifier
            .size(40.dp)
            .then(glassModifier(CircleShape))
            .lightPress(onClick),
        contentAlignment = Alignment.Center
    ) {
        IconImg(icon, 20.dp, colors.onSurfaceVariant)
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit, primary: Boolean) {
    val colors = CouiTheme.colorScheme
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (primary) colors.accent else colors.container)
            .lightPress(onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Txt(
            text,
            size = 13.sp,
            color = if (primary) colors.onAccent else colors.onSurfaceVariant,
            weight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onDump: () -> Unit,
    onLogin: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Txt("加载不出来", size = 18.sp, weight = FontWeight.Bold, color = colors.onSurface)
        Spacer(Modifier.height(8.dp))
        Txt(message, size = 13.sp, color = colors.onSurfaceVariant, align = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill("重试", onRetry, primary = true)
            Pill("登录", onLogin, primary = false)
        }
        Spacer(Modifier.height(10.dp))
        Pill("导出页面(排查用)", onDump, primary = false)
        Spacer(Modifier.height(14.dp))
        Txt(
            "如果页面提示未登录，点上面的「登录」重新登一次，再点重试。",
            size = 12.sp,
            color = colors.onSurfaceVariant,
            align = TextAlign.Center
        )
    }
}

@Composable
private fun CardBox(content: @Composable () -> Unit) {
    val colors = CouiTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.container)
            .padding(14.dp)
    ) { content() }
}

// ---------------- 首页：版块 ----------------
@Composable
private fun HomeList(forums: List<ForumItem>, loading: Boolean, onOpen: (ForumItem) -> Unit) {
    val colors = CouiTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Txt("版块", size = 13.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(6.dp))
        }
        items(forums) { f ->
            CardBox {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onOpen(f) }) {
                    if (!f.icon.isNullOrEmpty()) {
                        AsyncImage(
                            model = f.icon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Txt(f.name, size = 16.sp, weight = FontWeight.SemiBold, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (f.desc.isNotEmpty()) {
                            Spacer(Modifier.height(3.dp))
                            Txt(f.desc, size = 12.sp, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Txt("›", size = 18.sp, color = colors.onSurfaceVariant)
                }
            }
        }
        if (forums.isEmpty() && !loading) {
            item { Txt("没有数据，点右上角登录后再试", size = 13.sp, color = colors.onSurfaceVariant) }
        }
    }
}

// ---------------- 版块页：分类 + 主题列表 ----------------
@Composable
private fun ForumView(
    page: ForumPage?,
    loading: Boolean,
    onThread: (ThreadItem) -> Unit,
    onLoad: (String) -> Unit,
    currentUrl: String,
    onImage: (String) -> Unit,
    listState: LazyListState
) {
    val colors = CouiTheme.colorScheme
    Column(Modifier.fillMaxSize()) {
        if (page != null && page.cats.isNotEmpty()) {
            LazyRow(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(page.cats) { c ->
                    val curType = Regex("typeid=(\\d+)").find(currentUrl)?.groupValues?.get(1)
                    val chipType = Regex("typeid=(\\d+)").find(c.url)?.groupValues?.get(1)
                    val active = if (chipType == null) curType == null else chipType == curType
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) colors.accent else colors.container)
                            .clickable { onLoad(c.url) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Txt(c.label, size = 13.sp, color = if (active) colors.onAccent else colors.onSurfaceVariant)
                    }
                }
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = page?.threads ?: emptyList()
            items(list) { t ->
                CardBox {
                    Column(Modifier.clickable { onThread(t) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!t.typeName.isNullOrEmpty()) {
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.accent.copy(alpha = 0.14f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Txt(t.typeName, size = 11.sp, color = colors.accent)
                                }
                                Spacer(Modifier.width(6.dp))
                            }
                            Txt(
                                t.title,
                                modifier = Modifier.weight(1f),
                                size = 15.sp,
                                weight = FontWeight.SemiBold,
                                color = colors.onSurface,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Txt(
                            listOfNotNull(
                                t.author.takeIf { it.isNotEmpty() },
                                t.date.takeIf { it.isNotEmpty() },
                                if (t.replies.isNotEmpty()) "回复 ${t.replies}" else null,
                                if (t.views.isNotEmpty()) "查看 ${t.views}" else null
                            ).joinToString("  ·  "),
                            size = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                        if (t.thumbs.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            ThumbsGrid(t.thumbs, onImage)
                        }
                    }
                }
            }
            if (page != null && page.pageCount > 1) {
                item {
                    PagerRow(page.pageNo, page.pageCount, page.pages, onLoad)
                }
            }
        }
    }
}

@Composable
private fun ThumbsGrid(thumbs: List<String>, onImage: (String) -> Unit) {
    val items9 = thumbs.take(9)
    val shape = RoundedCornerShape(10.dp)
    when {
        items9.size == 1 -> NetImage(
            url = items9[0],
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(shape),
            onClick = { onImage(items9[0]) },
            crop = true
        )
        items9.size <= 3 -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items9.forEach { u ->
                NetImage(
                    url = u,
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                        .clip(shape),
                    onClick = { onImage(u) },
                    crop = true
                )
            }
        }
        else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items9.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { u ->
                        NetImage(
                            url = u,
                            modifier = Modifier
                                .weight(1f)
                                .height(96.dp)
                                .clip(shape),
                            onClick = { onImage(u) },
                            crop = true
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PagerRow(pageNo: Int, pageCount: Int, pages: List<Pair<Int, String>>, onLoad: (String) -> Unit) {
    val colors = CouiTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val shown = pages.filter { it.first <= pageCount }.take(8)
        shown.forEach { (n, url) ->
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(width = 34.dp, height = 32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (n == pageNo) colors.accent else colors.container)
                    .clickable { onLoad(url) },
                contentAlignment = Alignment.Center
            ) {
                Txt(n.toString(), size = 13.sp, color = if (n == pageNo) colors.onAccent else colors.onSurfaceVariant)
            }
        }
    }
}

// ---------------- 帖子页 ----------------
@Composable
private fun ThreadView(
    page: ThreadPage?,
    loading: Boolean,
    onLoad: (String) -> Unit,
    onExternal: (String) -> Unit,
    onReply: () -> Unit,
    onRate: (String) -> Unit,
    onAttach: (AttachItem) -> Unit,
    onImage: (String) -> Unit
) {
    val colors = CouiTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val p = page ?: return@LazyColumn
        item {
            CardBox {
                Column {
                    if (!p.typeName.isNullOrEmpty()) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.accent.copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) { Txt(p.typeName, size = 11.sp, color = colors.accent) }
                        Spacer(Modifier.height(6.dp))
                    }
                    Txt(p.subject, size = 18.sp, weight = FontWeight.Bold, color = colors.onSurface)
                }
            }
        }
        itemsIndexed(p.posts, key = { i, _ -> i }) { idx, post ->
            CardBox {
                Column {
                    // 作者行
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!post.avatar.isNullOrEmpty()) {
                            AsyncImage(
                                model = post.avatar,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Txt(post.author, size = 14.sp, weight = FontWeight.SemiBold, color = colors.onSurface)
                            Txt(post.time, size = 11.sp, color = colors.onSurfaceVariant)
                        }
                        if (post.pid.isNotEmpty()) {
                            Box(
                                Modifier
                                    .padding(end = 6.dp)
                                    .clip(RoundedCornerShape(0.dp))
                                    .clickable { onRate(post.pid) }
                            ) { Txt("评分", size = 12.sp, color = colors.accent) }
                        }
                        if (post.floor.isNotEmpty()) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(colors.accent.copy(alpha = 0.12f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) { Txt(post.floor, size = 11.sp, color = colors.accent) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // 正文
                    val paragraphs = buildParagraphs(post.blocks)
                    paragraphs.forEachIndexed { index, item ->
                        if (index > 0) Spacer(Modifier.height(12.dp))
                        when (item) {
                            is Block.Text -> Txt(
                                item.text,
                                size = 15.sp,
                                color = colors.onSurface,
                                lineHeight = 1.55f
                            )
                            is Block.Image -> {
                                NetImage(
                                    url = item.url,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    onClick = { onImage(item.url) }
                                )
                            }
                            else -> {}
                        }
                    }
                    // 附件
                    if (post.attachments.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Txt("附件", size = 12.sp, weight = FontWeight.SemiBold, color = colors.onSurfaceVariant)
                        post.attachments.forEach { at ->
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.card)
                                    .clickable { onAttach(at) }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Txt(at.name, size = 14.sp, weight = FontWeight.Medium, color = colors.accent)
                                    val meta = listOfNotNull(at.meta.takeIf { it.isNotEmpty() }, at.price)
                                        .joinToString("  ")
                                    if (meta.isNotEmpty()) Txt(meta, size = 12.sp, color = colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    // 评分
                    post.rating?.let { r ->
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Txt("评分 ${r.people} 人", size = 12.sp, color = colors.onSurfaceVariant)
                            Spacer(Modifier.width(10.dp))
                            Txt(r.parts.joinToString("  "), size = 12.sp, color = colors.accent)
                        }
                    }
                }
            }
        }
        if (p.pageCount > 1) {
            item { PagerRow(p.pageNo, p.pageCount, p.pages, onLoad) }
        }
    }
}

@Composable
private fun ReplyDialog(onCancel: () -> Unit, onSend: (String) -> Unit) {
    val colors = CouiTheme.colorScheme
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onCancel) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.background)
                .padding(18.dp)
        ) {
            Txt("回复", size = 17.sp, weight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.container)
                    .padding(12.dp)
            ) {
                if (text.isEmpty()) {
                    Txt("说点什么…", size = 14.sp, color = colors.onSurfaceVariant)
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Pill("取消", onCancel, primary = false)
                Spacer(Modifier.width(10.dp))
                Pill("发送", {
                    if (text.isNotBlank()) onSend(text.trim()) else {}
                }, primary = true)
            }
        }
    }
}

@Composable
private fun RateDialog(
    fields: List<Triple<String, String, String>>,
    onCancel: () -> Unit,
    onSend: (String) -> Unit
) {
    val colors = CouiTheme.colorScheme
    var reason by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onCancel) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.background)
                .padding(18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Txt("评分", size = 17.sp, weight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            Txt("默认给最高档，需要细分请在网页端调整", size = 12.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            // 每个维度展示：默认最高档
            fields.groupBy { it.first }.forEach { (name, opts) ->
                val chosen = opts.lastOrNull()
                if (chosen != null) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Txt(name, size = 13.sp, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Txt(chosen.second, size = 13.sp, weight = FontWeight.Medium, color = colors.accent)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.container)
                    .padding(12.dp)
            ) {
                if (reason.isEmpty()) Txt("评分理由（可留空）", size = 14.sp, color = colors.onSurfaceVariant)
                BasicTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Pill("取消", onCancel, primary = false)
                Spacer(Modifier.width(10.dp))
                Pill("提交评分", { onSend(reason.trim()) }, primary = true)
            }
        }
    }
}

@Composable
private fun ImageDialog(url: String, onClose: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset = if (scale <= 1.02f) Offset.Zero else offset + panChange
    }
    Dialog(onDismissRequest = onClose) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black)
        ) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, delta ->
                            if (scale > 1.02f) {
                                change.consume()
                                offset += delta
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.4f
                                }
                            },
                            onTap = { if (scale <= 1.05f) onClose() }
                        )
                    }
                    .transformable(state, lockRotationOnZoomPan = true)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit
            )
            // 关闭键
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) { IconImg(CouiIcons.Filled.Close, 18.dp, Color.White) }
        }
    }
}

private fun buildParagraphs(blocks: List<Block>): List<Block> {
    val out = ArrayList<Block>()
    val sb = StringBuilder()
    fun flush() {
        val t = sb.toString().trim()
        if (t.isNotEmpty()) out.add(Block.Text(t))
        sb.clear()
    }
    for (b in blocks) {
        when (b) {
            is Block.Text -> {
                if (sb.isNotEmpty() && sb.endsWith("。").not() && b.text.length > 40) flush()
                sb.append(b.text).append(' ')
            }
            is Block.Image -> { flush(); out.add(b) }
            Block.Break -> flush()
        }
    }
    flush()
    return out
}

/** 带明显兜底的网络图片：加载中显示占位，成功后自动替换成原图，失败显示可点提示 */
@Composable
private fun NetImage(
    url: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    crop: Boolean = false
) {
    val colors = CouiTheme.colorScheme
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        contentScale = if (crop) ContentScale.Crop else ContentScale.FillWidth,
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        loading = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.card),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconImg(CouiIcons.Outlined.Photo, 28.dp, colors.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Txt("图片加载中…", size = 12.sp, color = colors.onSurfaceVariant)
                }
            }
        },
        error = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.card),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconImg(CouiIcons.Outlined.Close, 24.dp, colors.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Txt("图片加载失败，点一下可查看大图", size = 12.sp, color = colors.onSurfaceVariant)
                }
            }
        }
    )
}

/** 变暗 + 居中转圈的加载遮罩 */
@Composable
private fun LoadingLayer(text: String) {
    val colors = CouiTheme.colorScheme
    val transition = rememberInfiniteTransition()
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing))
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(40.dp)) {
                drawArc(
                    color = colors.accent,
                    startAngle = angle,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Txt(text, size = 13.sp, color = Color.White)
        }
    }
}
