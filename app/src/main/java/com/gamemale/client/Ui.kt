package com.gamemale.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import coil.compose.SubcomposeAsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.drawCouiBackdrop
import com.itos.coui.icons.CouiIcons
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.itos.coui.theme.CouiTheme

private const val ANIME_URL =
    "https://www.gamemale.com/forum.php?mod=forumdisplay&fid=150&filter=dateline&orderby=dateline&typeid=831"
private const val GAME_URL =
    "https://www.gamemale.com/forum.php?mod=forumdisplay&fid=152&filter=dateline&orderby=dateline&typeid=737"

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
                        if (moved < 24f) {
                            onClick()
                        }
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

/** 整体按压形变：整块等比缩小，拖动时随手指同向平移，光斑跟随 */
@Composable
private fun Modifier.pressMorph(onClick: (() -> Unit)? = null): Modifier {
    var press by remember { mutableStateOf<Offset?>(null) }
    var drag by remember { mutableStateOf(Offset.Zero) }
    var pressing by remember { mutableStateOf(false) }
    val amt by animateFloatAsState(if (pressing) 1f else 0f, tween(150))
    val glow = remember { Animatable(0f) }
    LaunchedEffect(press != null) {
        glow.animateTo(if (press != null) 1f else 0f, tween(150))
        if (press == null) drag = Offset.Zero
    }
    return this
        .graphicsLayer {
            scaleX = 1f - 0.04f * amt
            scaleY = 1f - 0.04f * amt
            translationX = drag.x * 0.22f * amt
            translationY = drag.y * 0.22f * amt
        }
        .pointerInput(onClick) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                press = down.position
                pressing = true
                var moved = 0f
                var last = down.position
                while (true) {
                    val e = awaitPointerEvent()
                    val ch = e.changes.firstOrNull { it.id == down.id } ?: break
                    if (!ch.pressed) {
                        press = null
                        pressing = false
                        if (moved < 24f && onClick != null) onClick()
                        break
                    }
                    val d = ch.position - last
                    last = ch.position
                    drag += d
                    moved += d.getDistance()
                    press = ch.position
                }
            }
        }
        .drawWithContent {
            drawContent()
            val p = press
            if (p != null && glow.value > 0f) {
                val r = size.maxDimension * 0.9f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.22f * glow.value), Color.Transparent),
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

internal class PressDragState {
    val bounds = androidx.compose.runtime.mutableStateMapOf<Int, androidx.compose.ui.geometry.Rect>()
    var hover by mutableStateOf(-1)
    var onCommit: ((Int) -> Unit)? = null
}

/** 按住后手指可以滑到同组其它项：高亮跟着走，松手执行所在项 */
private fun Modifier.pressDragTarget(state: PressDragState, index: Int): Modifier = composed {
    var origin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    this
        .onGloballyPositioned {
            origin = it.positionInRoot()
            state.bounds[index] = androidx.compose.ui.geometry.Rect(
                origin, androidx.compose.ui.geometry.Size(it.size.width.toFloat(), it.size.height.toFloat())
            )
        }
        .pointerInput(index, state) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                state.hover = index
                var last = index
                while (true) {
                    val e = awaitPointerEvent()
                    val ch = e.changes.firstOrNull() ?: break
                    val pt = androidx.compose.ui.geometry.Offset(origin.x + ch.position.x, origin.y + ch.position.y)
                    state.bounds.entries.firstOrNull { it.value.contains(pt) }
                        ?.let { last = it.key; state.hover = it.key }
                    if (!ch.pressed) break
                }
                state.onCommit?.invoke(last)
                state.hover = -1
            }
        }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    dragState: PressDragState? = null,
    dragIndex: Int = -1,
    onClick: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    val hot = active
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (hot) colors.accent.copy(alpha = 0.16f) else Color.Transparent)
            .then(
                if (dragState != null) Modifier.pressDragTarget(dragState, dragIndex)
                else Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onClick() }
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconImg(icon, 20.dp, if (hot) colors.accent else colors.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Txt(
            label,
            size = 11.sp,
            color = if (hot) colors.accent else colors.onSurfaceVariant,
            weight = if (hot) FontWeight.SemiBold else null
        )
    }
}

@Composable
private fun NavButtonSlot(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    dragState: PressDragState? = null,
    dragIndex: Int = -1,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    val hot = active
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (hot) colors.accent.copy(alpha = 0.16f) else Color.Transparent)
            .then(
                if (dragState != null) Modifier.pressDragTarget(dragState, dragIndex)
                else Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onClick() }
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(Modifier.height(2.dp))
        Txt(
            label,
            size = 11.sp,
            color = if (hot) colors.accent else colors.onSurfaceVariant,
            weight = if (hot) FontWeight.SemiBold else null
        )
    }
}

/** 浏览记录图标：带指针的时钟（COUI 图标集里没有时钟/历史） */
@Composable
private fun IconHistory(c: Color) {
    Canvas(Modifier.size(20.dp)) {
        val sw = 1.7.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 0.40f
        drawCircle(c, radius = r, center = androidx.compose.ui.geometry.Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(sw))
        // 时针 / 分针
        drawLine(c, androidx.compose.ui.geometry.Offset(cx, cy), androidx.compose.ui.geometry.Offset(cx, cy - r * 0.72f), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(cx, cy), androidx.compose.ui.geometry.Offset(cx + r * 0.62f, cy), sw)
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
    onCopyLink: () -> Unit = {},
    searchUpdating: Boolean = false,
    dailyFailed: Boolean = false,
    onOpenSign: () -> Unit = {},
    onOpenAward: () -> Unit = {},
    historySelectionCount: Int = 0,
    historySelecting: Boolean = false,
    onHistorySelecting: (Boolean) -> Unit = {},
    onHistorySelectionCount: (Int) -> Unit = {},
    onDeleteHistory: (List<Long>) -> Unit = {},
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
    currentUrl: String,
    threadKey: String? = null,
    onSettings: () -> Unit = {},
    fontScale: Float = 1f,
    blurBg: Boolean = true,
    onSetBlurBg: (Boolean) -> Unit = {},
    themeMode: Int = 0,
    cacheMb: Int = 300,
    cacheBytes: Long = 0L,
    cacheCount: Int = 0,
    onSetThemeMode: (Int) -> Unit = {},
    onSetFontScale: (Float) -> Unit = {},
    onSetCacheMb: (Int) -> Unit = {},
    onClearCache: () -> Unit = {},
    onExportLogs: () -> Unit = {},
    onRefreshThread: () -> Unit = {},
    onOpenBrowser: () -> Unit = {},
    canGoBack: Boolean = false,
    showThemeToggle: Boolean = true,
    refreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    searchHistory: List<String> = emptyList(),
    backProgress: Float = 0f,
    onThumbNeeded: (ThreadItem) -> Unit = {},
    needUpgrade: Boolean = false,
    onOpenSite: () -> Unit = {},
    onAvatar: () -> Unit = {},
    needLogin: Boolean = false,
    loginPage: Boolean = false,
    loginWeb: android.webkit.WebView? = null,
    onLoginDone: () -> Unit = {},
    onLoginClose: () -> Unit = {},
    verifyPage: Boolean = false,
    verifyWeb: android.webkit.WebView? = null,
    onVerifyClose: () -> Unit = {},
    accountsOpen: Boolean = false,
    accounts: List<Accounts.Acc> = emptyList(),
    currentAccountId: String? = null,
    onOpenAccounts: () -> Unit = {},
    onSwitchAccount: (Accounts.Acc) -> Unit = {},
    onDeleteAccount: (Accounts.Acc) -> Unit = {},
    onAddAccount: () -> Unit = {},
    onCloseAccounts: () -> Unit = {},
    history: List<HistoryStore.Entry> = emptyList(),
    onOpenHistory: (String, String) -> Unit = { _, _ -> },
    onClearHistory: () -> Unit = {},
    onOpenHistoryTab: () -> Unit = {}
) {
    val colors = CouiTheme.colorScheme
    val baseDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(baseDensity.density, fontScale)
    ) {
    val forumListState = rememberLazyListState()
    LaunchedEffect(nav) {
        if (nav is Nav.Search) runCatching { forumListState.scrollToItem(0) }
    }
    // 浏览记录页独立的滚动位置
    val historyListState = rememberLazyListState()
    // 每个板块各自记住上次停留的位置；翻到新的一页则回到顶部
    val forumPositions = remember { androidx.compose.runtime.mutableStateMapOf<String, Int>() }
    var lastForumUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentUrl) {
        val prev = lastForumUrl
        if (prev != null && prev != currentUrl) {
            if (!prev.contains("&page=")) {
                forumPositions[prev.substringBefore("&page=")] = forumListState.firstVisibleItemIndex
            }
            val target = if (currentUrl.contains("&page=")) 0
            else forumPositions[currentUrl.substringBefore("&page=")] ?: 0
            forumListState.scrollToItem(target)
        }
        lastForumUrl = currentUrl
    }
    var searchOpen by remember { mutableStateOf(false) }
    var topMenuOpen by remember { mutableStateOf(false) }
    // 顶栏返回 / 系统返回 / 侧滑：通知浏览记录页退出选择模式
    var historyExitSignal by remember { mutableStateOf(0) }
    val backdrop = rememberCouiBackdrop()
    // 卡片→全屏：几何用矩形插值，内容用截图交叉淡化
    val zoomProg = remember { Animatable(1f) }
    var zoomRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var zoomBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var zoomLayer by remember { mutableStateOf<androidx.compose.ui.graphics.layer.GraphicsLayer?>(null) }
    var zoomSrc by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val screenWpx = with(LocalDensity.current) {
        androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val screenHpx = with(LocalDensity.current) {
        androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    var zoomMode by remember { mutableStateOf(1) }
    var threadOrigin by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var prevNavZoom by remember { mutableStateOf<Nav>(nav) }
    val pageGl = rememberGraphicsLayer()
    var sharedTid by remember { mutableStateOf<String?>(null) }
    var prevNavShared by remember { mutableStateOf<Nav>(nav) }
    LaunchedEffect(nav, threadKey) {
        val from = prevNavShared
        prevNavShared = nav
        when {
            nav is Nav.Thread -> sharedTid = threadKey
            from is Nav.Thread && nav is Nav.Forum -> {
                // 退场动画期间保持 key，结束后立即关闭，避免列表互切时串卡
                kotlinx.coroutines.delay(620)
                sharedTid = null
            }
            else -> sharedTid = null
        }
    }
    LaunchedEffect(nav) {
        val from = prevNavZoom
        prevNavZoom = nav
        if (nav is Nav.Thread && (from is Nav.Forum || from is Nav.History || from == Nav.Home)) {
            val r = CardZoom.origin
            if (r != null && r.width > 1f && r.height > 1f) {
                threadOrigin = r
                zoomRect = r
                zoomBitmap = CardZoom.bitmap
                zoomLayer = CardZoom.layer
                zoomSrc = androidx.compose.ui.geometry.Size(r.width, r.height)
                zoomMode = 1
                zoomProg.snapTo(0f)
                zoomProg.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
                zoomRect = null
                zoomBitmap = null
            }
        } else if (from is Nav.Thread && (nav is Nav.Forum || nav is Nav.History || nav == Nav.Home)) {
            val r = threadOrigin
            if (r != null) {
                zoomRect = r
                zoomBitmap = runCatching { pageGl.toImageBitmap() }.getOrNull()
                zoomLayer = pageGl
                zoomSrc = androidx.compose.ui.geometry.Size(screenWpx, screenHpx)
                zoomMode = -1
                zoomProg.snapTo(1f)
                zoomProg.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
                zoomRect = null
                zoomBitmap = null
            }
        }
    }
    val inHistorySelection = nav is Nav.History && historySelecting
    if (inHistorySelection) {
        androidx.activity.compose.BackHandler(enabled = true) { historyExitSignal++ }
    }
    // 菜单项：未登录先给「登录」；分享/浏览器/刷新只在帖子页；设置始终有
    val topMenuItems = buildList {
        if (avatarUrl.isNullOrBlank()) add(Triple("登录", 4) { onLogin() })
        if (nav is Nav.Thread) add(Triple("分享", 0) { onShare() })
        if (nav is Nav.Thread) add(Triple("复制链接", 5) { onCopyLink() })
        if (nav is Nav.Thread) add(Triple("用浏览器打开", 1) { onOpenBrowser() })
        if (nav is Nav.Thread) add(Triple("刷新", 2) { onRefreshThread() })
        add(Triple("设置", 3) { onSettings() })
        // 今日自动签到/抽卡有失败时，给出手动入口
        if (dailyFailed) {
            add(Triple("手动签到", 6) { onOpenSign() })
            add(Triple("手动抽卡", 7) { onOpenAward() })
        }
    }
    // 切换页面/弹窗时自动收起菜单
    LaunchedEffect(nav, loginPage, verifyPage, accountsOpen) { topMenuOpen = false }
    Box(Modifier.fillMaxSize().background(colors.background)) {
        val barH = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 56.dp
        // 列表页让内容从顶栏下方穿过，顶栏才能采到模糊源
        val overlayBar = true
        CompositionLocalProvider(LocalBarInset provides barH) {
        Box(Modifier.fillMaxSize()) {
            TopBar(
                selectionMode = inHistorySelection,
                onExitSelection = { historyExitSignal++ },
                backdrop = backdrop,
                blur = overlayBar && blurBg,
                menuOpen = topMenuOpen,
                onToggleMenu = {
                    // 只有一项时没必要展开菜单，直接执行
                    if (topMenuItems.size == 1 && topMenuOpen.not()) {
                        topMenuItems.first().third()
                    } else {
                        topMenuOpen = !topMenuOpen
                    }
                },
                loginName = loginName,
                canShare = nav is Nav.Thread,
                canRefresh = nav is Nav.Thread,
                onRefresh = onRefreshThread,
                onOpenBrowser = onOpenBrowser,
                avatarUrl = avatarUrl,
                onShare = onShare,
                onDump = onDump,
                title = when (nav) {
                    is Nav.Home -> "GameMale"
                    Nav.Settings -> "设置"
                    Nav.Search -> "搜索"
                    Nav.History -> if (historySelecting) "已选择 " + historySelectionCount + " 项" else "浏览记录"
                    is Nav.Forum -> nav.title.ifEmpty { "版块" }
                    is Nav.Thread -> nav.title.ifEmpty { "帖子" }
                },
                canBack = canGoBack,
                dark = dark,
                onSettings = onSettings,
                showSettings = nav is Nav.Forum || nav is Nav.Thread,
                onAvatar = onAvatar,
                onBack = onBack,
                onToggleDark = onToggleDark,
                onLogin = onLogin
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = if (overlayBar) 0.dp else barH)
                    .graphicsLayer {
                        translationX = backProgress * size.width * 0.35f
                        if (zoomRect != null) {
                            alpha = if (zoomMode > 0) {
                                // 进入：B 后段淡入
                                ((zoomProg.value - 0.35f) / 0.5f).coerceIn(0f, 1f)
                            } else {
                                // 退出：列表从 0% 起淡入，45% 处完全接替
                                ((1f - zoomProg.value) / 0.45f).coerceIn(0f, 1f)
                            }
                        }
                    }
                    .drawWithContent {
                        if (nav is Nav.Thread) {
                            pageGl.record { this@drawWithContent.drawContent() }
                            drawLayer(pageGl)
                        } else {
                            drawContent()
                        }
                    }
                    .then(if (blurBg) Modifier.couiBackdrop(backdrop) else Modifier)
            ) {
                androidx.compose.animation.SharedTransitionLayout {
                val sharedScope = this
                AnimatedContent(
                    targetState = nav,
                    transitionSpec = {
                        fadeIn(tween(200)).togetherWith(fadeOut(tween(160)))
                    },
                    label = "nav"
                ) { cur ->
                    val animScope = this
                    CompositionLocalProvider(
                        LocalZoomScopes provides ZoomScopes(sharedScope, animScope),
                        LocalZoomTid provides sharedTid
                    ) {
                    when {
                        error != null && (nav is Nav.Forum || nav is Nav.Thread) ->
                            ErrorView(error, onRetry, onDump, onLogin)
                        cur == Nav.Home -> HomeList(forums, loading, onOpenForum)
                        cur is Nav.History -> HistoryView(
                            history, historyListState, onOpenHistory, onClearHistory,
                            exitSignal = historyExitSignal,
                            onSelectionCount = onHistorySelectionCount,
                            onSelectionMode = onHistorySelecting,
                            onDeleteSelected = onDeleteHistory,
                            backdrop = backdrop,
                            blur = blurBg
                        )
                        cur is Nav.Search -> SearchScreen(
                            history = searchHistory,
                            onCancel = onBack,
                            onSubmit = onSearch
                        )
                        cur is Nav.Forum && needUpgrade && (forumPage?.threads?.isEmpty() != false) ->
                            UpgradeView(loading, onOpenSite)
                        cur is Nav.Forum && needLogin && (forumPage?.threads?.isEmpty() != false) ->
                            LoginPromptView(onLogin)
                        cur is Nav.Forum -> ForumView(
                            forumPage, loading, onOpenThread, onLoad, currentUrl, onImage, forumListState,
                            refreshing, onRefresh, onThumbNeeded, searchUpdating,
                            onSwipeBoard = { next ->
                                val u = currentUrl
                                if (!u.contains("search.php") && u.contains("forumdisplay")) {
                                    if (u.contains("fid=150") && next) onQuickForum(GAME_URL, "和谐游戏")
                                    else if (u.contains("fid=152") && !next) onQuickForum(ANIME_URL, "和谐动漫")
                                }
                            }
                        )
                        cur is Nav.Thread -> ThreadView(
                            threadPage, loading, onLoad, onExternal,
                            onReply, onRate, onAttach, onImage
                        )
                        cur is Nav.Settings -> SettingsView(
                            onAccounts = onOpenAccounts,
                            fontScale = fontScale,
                            themeMode = themeMode,
                            cacheMb = cacheMb,
                            cacheBytes = cacheBytes,
                            cacheCount = cacheCount,
                            onSetThemeMode = onSetThemeMode,
                            onSetFontScale = onSetFontScale,
                            blurBg = blurBg,
                            onSetBlurBg = onSetBlurBg,
                            onSetCacheMb = onSetCacheMb,
                            onClearCache = onClearCache,
                            onExportLogs = onExportLogs,
                            onExternal = onExternal
                        )
                    }
                    }
                }
                // 设置页/搜索页不该被列表的加载遮罩挡住
                if (loading && (nav is Nav.Forum || nav is Nav.Thread)) LoadingLayer("正在加载…")
                }
            }
            }

        if (verifyPage && verifyWeb != null) {
            Box(Modifier.fillMaxSize().background(colors.background)) {
                // 登录页自己处理键盘避让；主界面用 adjustNothing，不会被别的应用的悬浮输入框顶下去
                Column(Modifier.fillMaxSize().imePadding()) {
                    Row(
                        Modifier.fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.systemBars)
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleBtn(CouiIcons.Filled.Back, onVerifyClose)
                        Txt(
                            "站点验证",
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            size = 17.sp,
                            weight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                    Txt(
                        "站点要求先确认你是真人，正在自动通过验证。一般几秒就好，通过后会自动返回列表。",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                        size = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                    AndroidView(
                        factory = {
                            val wrapper = android.widget.FrameLayout(it)
                            (verifyWeb.parent as? android.view.ViewGroup)?.removeView(verifyWeb)
                            wrapper.addView(
                                verifyWeb,
                                android.widget.FrameLayout.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            )
                            wrapper
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }
        }

        if (loginPage && loginWeb != null) {
            // 用独立 Dialog 窗口承载 WebView：独立窗口才有正常的输入法焦点，
            // 嵌在主窗口的 Compose 覆盖层里 WebView 拿不到 IME 连接。
            androidx.compose.ui.window.Dialog(
                onDismissRequest = onLoginClose,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = true
                )
            ) {
            Box(Modifier.fillMaxSize().background(colors.background)) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.systemBars)
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleBtn(CouiIcons.Filled.Back, onLoginClose)
                        Txt(
                            "登录",
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            size = 17.sp,
                            weight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        // 只有 ✓ 图标：表示"我登录完了，回首页"
                        CircleSlotBtn(onLoginDone) { IconCheck() }
                    }
                    AndroidView(
                        factory = {
                            val wrapper = android.widget.FrameLayout(it)
                            (loginWeb.parent as? android.view.ViewGroup)?.removeView(loginWeb)
                            wrapper.addView(
                                loginWeb,
                                android.widget.FrameLayout.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            )
                            wrapper
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }
            }
        }

        // 卡片/页面位图过渡层：几何在矩形与全屏之间插值，A 淡出 B 淡入
        zoomRect?.let { r ->
            val density = LocalDensity.current
            val p = zoomProg.value
            val screenW = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.toPx() }
            val screenH = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx() }
            val left = r.left * (1f - p)
            val top = r.top * (1f - p)
            val w = r.width + (screenW - r.width) * p
            val h = r.height + (screenH - r.height) * p
            val corner = (1f - p) * 18f
            val alpha = if (zoomMode > 0) {
                // 进入：卡片位图前 55% 不透明，之后淡出
                if (p <= 0.55f) 1f else ((1f - p) / 0.45f).coerceIn(0f, 1f)
            } else {
                // 退出：详情位图前 15% 保持不透明，15%~65% 淡出
                when {
                    p >= 0.85f -> 1f
                    p > 0.35f -> ((p - 0.35f) / 0.50f).coerceIn(0f, 1f)
                    else -> 0f
                }
            }
            val shape = RoundedCornerShape(corner.dp)
            val layer = zoomLayer
            if (layer != null && zoomSrc.width > 1f && zoomSrc.height > 1f) {
                // 直接绘制录好的图层，等比铺满目标矩形，不做非等比拉伸
                Box(
                    Modifier
                        .offset { androidx.compose.ui.unit.IntOffset(left.toInt(), top.toInt()) }
                        .size(with(density) { w.toDp() }, with(density) { h.toDp() })
                        .clip(shape)
                        .alpha(alpha)
                ) {
                    androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
                        val k = maxOf(w / zoomSrc.width, h / zoomSrc.height)
                        val dx = (w - zoomSrc.width * k) / 2f
                        val dy = (h - zoomSrc.height * k) / 2f
                        translate(dx, dy) {
                            scale(k, k, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                                drawLayer(layer)
                            }
                        }
                    }
                }
            } else {
                Box(
                    Modifier
                        .offset { androidx.compose.ui.unit.IntOffset(left.toInt(), top.toInt()) }
                        .size(with(density) { w.toDp() }, with(density) { h.toDp() })
                        .clip(shape)
                        .background(colors.container)
                        .alpha(alpha)
                )
            }
        }

        // 隐藏的取数 WebView（1dp，不渲染任何可见内容）
        AndroidView(factory = { host }, modifier = Modifier.size(1.dp))

        if (busy && !rateOpen && !replyOpen) LoadingLayer("处理中…")

        if (nav is Nav.Thread && !replyOpen) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(end = 16.dp, bottom = if (showBottomBar && !inHistorySelection) 92.dp else 10.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .then(
                        if (blurBg) Modifier.drawCouiBackdrop(
                            backdrop,
                            shape = { CircleShape },
                            effects = { blur(28f) },
                            onDrawSurface = { drawRect(colors.accent.copy(alpha = 0.82f)) }
                        ) else Modifier.background(colors.accent.copy(alpha = 0.92f))
                    )
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
        // 底部导航（动漫 / 游戏 / 浏览记录）——只在根级列表页出现，且宽度收紧
        val navDrag = remember { PressDragState() }
        navDrag.onCommit = { idx ->
            when (idx) {
                0 -> onQuickForum(ANIME_URL, "和谐动漫")
                1 -> onQuickForum(GAME_URL, "和谐游戏")
                2 -> onOpenHistoryTab()
            }
        }
        if (showBottomBar && !inHistorySelection) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(start = 14.dp, bottom = 10.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .then(
                        if (blurBg) Modifier.drawCouiBackdrop(
                            backdrop,
                            shape = { RoundedCornerShape(26.dp) },
                            effects = { blur(30f) },
                            onDrawSurface = { drawRect(colors.container.copy(alpha = 0.66f)) }
                        ) else Modifier.background(colors.container)
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.03f))
                        ),
                        RoundedCornerShape(26.dp)
                    )
            ) {
                var navRowOrigin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                val navHover = navDrag.bounds[navDrag.hover]
                val navHL by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = navHover?.let { it.left - navRowOrigin.x } ?: 0f,
                    animationSpec = tween(150, easing = LinearEasing),
                    label = "navHl"
                )
                val navHW by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = navHover?.width ?: 0f,
                    animationSpec = tween(150, easing = LinearEasing),
                    label = "navHw"
                )
                Row(
                    Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp)
                        .onGloballyPositioned { navRowOrigin = it.positionInRoot() }
                        .drawBehind {
                            if (navDrag.hover >= 0 && navHW > 1f) {
                                drawRoundRect(
                                    color = colors.accent.copy(alpha = 0.16f),
                                    topLeft = androidx.compose.ui.geometry.Offset(navHL, 0f),
                                    size = androidx.compose.ui.geometry.Size(navHW, size.height),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                                )
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                val cur = currentUrl
                NavButton(
                    icon = CouiIcons.Outlined.Album,
                    label = "和谐动漫",
                    active = cur.contains("fid=150"),
                    modifier = Modifier.width(66.dp),
                    dragState = navDrag,
                    dragIndex = 0
                ) { onQuickForum(ANIME_URL, "和谐动漫") }
                NavButton(
                    icon = CouiIcons.Filled.Gamepad,
                    label = "和谐游戏",
                    active = cur.contains("fid=152"),
                    modifier = Modifier.width(66.dp),
                    dragState = navDrag,
                    dragIndex = 1
                ) { onQuickForum(GAME_URL, "和谐游戏") }
                NavButtonSlot(
                    label = "浏览记录",
                    active = nav is Nav.History,
                    modifier = Modifier.width(72.dp),
                    dragState = navDrag,
                    dragIndex = 2,
                    icon = { IconHistory(if (nav is Nav.History) CouiTheme.colorScheme.accent else CouiTheme.colorScheme.onSurfaceVariant) },
                    onClick = onOpenHistoryTab
                )
                }
            }

            // 搜索 FAB 独立漂浮在导航栏右侧，不放在胶囊里
            if (!inHistorySelection) Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(end = 16.dp, bottom = 10.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .then(
                        if (blurBg) Modifier.drawCouiBackdrop(
                            backdrop,
                            shape = { CircleShape },
                            effects = { blur(28f) },
                            onDrawSurface = { drawRect(colors.accent.copy(alpha = 0.82f)) }
                        ) else Modifier.background(colors.accent.copy(alpha = 0.92f))
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))
                        ),
                        CircleShape
                    )
                    .lightPress { onOpenSearch() },
                contentAlignment = Alignment.Center
            ) {
                IconImg(CouiIcons.Outlined.Search, 24.dp, colors.onAccent)
            }
        }

        if (searchOpen && !inHistorySelection) {
            SearchDialog(
                onCancel = { searchOpen = false },
                onSearch = {
                    searchOpen = false
                    onSearch(it)
                }
            )
        }
        AvatarMenu(
            visible = topMenuOpen,
            items = topMenuItems,
            backdrop = backdrop,
            blur = blurBg,
            onDismiss = { topMenuOpen = false }
        )
        if (accountsOpen) AccountDialog(
            accounts, currentAccountId, onSwitchAccount, onDeleteAccount, onAddAccount, onCloseAccounts
        )
        if (replyOpen) ReplyDialog(busy, onCancelReply, onSendReply)
        if (rateOpen) RateDialog(rateFields, busy, onCancelRate, onSendRate)
        if (imageUrl != null) {
            val allImages = remember(threadPage) {
                threadPage?.posts?.flatMap { p -> collectImageUrls(p.blocks) }?.distinct().orEmpty()
            }
            ImageDialog(allImages, imageUrl, onCloseImage)
        }
    }
    }
    }
}

@Composable
private fun TopBar(
    title: String,
    loginName: String?,
    canBack: Boolean,
    dark: Boolean,
    canShare: Boolean,
    canRefresh: Boolean = false,
    onRefresh: () -> Unit = {},
    onOpenBrowser: () -> Unit = {},
    avatarUrl: String?,
    showSettings: Boolean = false,
    onAvatar: () -> Unit = {},
    onBack: () -> Unit,
    onToggleDark: () -> Unit,
    onSettings: () -> Unit,
    onLogin: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit = {},
    onDump: () -> Unit,
    menuOpen: Boolean = false,
    onToggleMenu: () -> Unit = {},
    selectionMode: Boolean = false,
    onExitSelection: () -> Unit = {},
    backdrop: com.itos.coui.backdrop.CouiBackdrop? = null,
    blur: Boolean = false
) {
    val colors = CouiTheme.colorScheme
    val statusTop = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val barH = statusTop + 56.dp
    val fadeH = 26.dp
    val fadeStop = (barH.value * 0.55f) / (barH.value + fadeH.value)
    Box(
        Modifier
            .fillMaxWidth()
            .height(barH + fadeH)
            .zIndex(2f)
    ) {
        if (backdrop != null && blur) {
            // 顶栏本体 + 下方一段渐隐区，让模糊从 100% 平滑过渡到 0
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawCouiBackdrop(
                            backdrop,
                            shape = { RectangleShape },
                            effects = { blur(64f, TileMode.Clamp) },
                            onDrawSurface = {
                                drawRect(colors.background.copy(alpha = 0.55f))
                            }
                        )
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to Color.Black,
                                    fadeStop to Color.Black,
                                    1f to Color.Transparent
                                ),
                                blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                            )
                        }
                )
            }
        } else {
            Box(Modifier.fillMaxSize().background(colors.background))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(barH)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                CircleBtn(CouiIcons.Filled.Back, onExitSelection)
                Spacer(Modifier.width(6.dp))
            } else if (canBack) {
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
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .clickable { onToggleMenu() }
                )
            } else {
                // 未登录时点头像也展开菜单，菜单里第一项是"登录"
                Box(
                    Modifier
                        .size(40.dp)
                        .then(glassModifier(CircleShape))
                        .lightPress { onToggleMenu() },
                    contentAlignment = Alignment.Center
                ) { IconImg(CouiIcons.Filled.UserCircle, 22.dp, colors.onSurface) }
            }
        }
    }
    }
}

/** 头像下拉菜单：同一个窗口里的浮层，盖在内容之上；点击空白处收起 */
@Composable
private fun AvatarMenu(
    visible: Boolean,
    items: List<Triple<String, Int, () -> Unit>>,
    onDismiss: () -> Unit,
    backdrop: com.itos.coui.backdrop.CouiBackdrop? = null,
    blur: Boolean = false
) {
    val colors = CouiTheme.colorScheme
    val dragState = remember { PressDragState() }
    val hlY = androidx.compose.animation.core.animateDpAsState(
        targetValue = 6.dp + (dragState.hover.coerceAtLeast(0) * 46).dp,
        animationSpec = tween(140, easing = LinearEasing),
        label = "menuHighlight"
    )
    dragState.onCommit = { idx ->
        onDismiss()
        items.getOrNull(idx)?.third?.invoke()
    }
    Box(Modifier.fillMaxSize()) {
        // 透明遮罩：点空白收起
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(140))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.001f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)) + scaleIn(tween(220), initialScale = 0.90f, transformOrigin = TransformOrigin(1f, 0f)),
            exit = fadeOut(tween(130)) + scaleOut(tween(130), targetScale = 0.94f, transformOrigin = TransformOrigin(1f, 0f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(top = 52.dp, end = 12.dp)
        ) {
            Column(
                Modifier
                    .width(216.dp)
                    .shadow(24.dp, RoundedCornerShape(30.dp), clip = false)
                    .clip(RoundedCornerShape(30.dp))
                    .then(
                        if (blur && backdrop != null) Modifier.drawCouiBackdrop(
                            backdrop,
                            shape = { RoundedCornerShape(30.dp) },
                            effects = { blur(26f, TileMode.Clamp) },
                            onDrawSurface = { drawRect(colors.container.copy(alpha = 0.72f)) }
                        ) else Modifier.background(colors.container)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp))
                    .drawBehind {
                        if (dragState.hover >= 0) {
                            val padX = 6.dp.toPx()
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.12f),
                                topLeft = androidx.compose.ui.geometry.Offset(padX, hlY.value.toPx()),
                                size = androidx.compose.ui.geometry.Size(size.width - padX * 2f, 46.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                            )
                        }
                    }
                    .padding(vertical = 6.dp)
            ) {
                items.forEachIndexed { pos, item ->
                    val label = item.first
                    val idx = item.second
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .pressDragTarget(dragState, pos)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (idx) {
                            0 -> MenuIcon(AppIcon.Share)
                            1 -> MenuIcon(AppIcon.Browser)
                            2 -> MenuIcon(AppIcon.Refresh)
                            4 -> MenuIcon(AppIcon.Login)
                            5 -> MenuIcon(AppIcon.Copy)
                            6, 7 -> MenuIcon(AppIcon.Browser)
                            else -> MenuIcon(AppIcon.Setting)
                        }
                        Spacer(Modifier.width(12.dp))
                        Txt(label, size = 14.sp, color = colors.onSurface)
                    }
                }
            }
        }
    }
}

private enum class AppIcon { Share, Browser, Refresh, Setting, Login, Copy }

@Composable
private fun MenuIcon(kind: AppIcon) {
    when (kind) {
        AppIcon.Setting -> IconImg(CouiIcons.Filled.Setting, 20.dp, CouiTheme.colorScheme.onSurface)
        AppIcon.Share -> IconImg(ShareVector, 20.dp, CouiTheme.colorScheme.onSurface)
        AppIcon.Browser -> IconBrowser()
        AppIcon.Refresh -> IconRefresh()
        AppIcon.Login -> IconImg(CouiIcons.Filled.UserCircle, 20.dp, CouiTheme.colorScheme.onSurface)
        AppIcon.Copy -> IconCopy()
    }
}

/** 纯文本里的网址（含不带 http 前缀的裸域名）渲染成可点链接 */
private val BARE_URL = Regex(
    "(?i)(?:https?://|www\\.)[^\\s<>\"'（）()【】，。、；]+|" +
    "(?:[a-z0-9][a-z0-9_-]*\\.)+(?:com|net|org|io|me|cc|tv|cn|jp|us|uk|de|fr|ru|xyz|top|link|app|dev|gg|info|biz|kr|tw|hk|co)" +
    "(?:/[^\\s<>\"'（）()【】，。、；]*)?"
)

@Composable
private fun LinkifiedText(
    text: String,
    size: androidx.compose.ui.unit.TextUnit,
    color: androidx.compose.ui.graphics.Color,
    lineHeight: Float = 1.5f,
    onOpen: (String) -> Unit
) {
    val accent = CouiTheme.colorScheme.accent
    val links = remember(text) {
        val out = ArrayList<Pair<IntRange, String>>()
        for (m in BARE_URL.findAll(text)) {
            var raw = m.value.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '\'', '"')
            if (raw.length <= 4) continue
            val href = when {
                raw.startsWith("http") -> raw
                raw.startsWith("www.") -> "https://$raw"
                else -> "https://$raw"
            }
            val range = m.range.first until (m.range.first + raw.length)
            if (out.any { it.first.first < range.last && range.first < it.first.last }) continue
            out.add(range to href)
        }
        out
    }
    val annotated = remember(text, accent, links) {
        if (links.isEmpty()) AnnotatedString(text)
        else buildAnnotatedString {
            var last = 0
            for ((range, _) in links) {
                append(text.substring(last, range.first))
                withStyle(SpanStyle(color = accent, textDecoration = TextDecoration.Underline)) {
                    append(text.substring(range.first, range.last + 1))
                }
                last = range.last + 1
            }
            if (last < text.length) append(text.substring(last))
        }
    }
    var layout by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    BasicText(
        text = annotated,
        style = TextStyle(color = color, fontSize = size, lineHeight = (size.value * lineHeight).sp),
        onTextLayout = { layout = it },
        modifier = Modifier.pointerInput(text, links) {
            if (links.isEmpty()) return@pointerInput
            detectTapGestures { pos: androidx.compose.ui.geometry.Offset ->
                val l = layout ?: return@detectTapGestures
                val offset = l.getOffsetForPosition(pos)
                links.firstOrNull { offset in it.first }?.let { onOpen(it.second) }
            }
        }
    )
}

/** 只有图标的圆形按钮（图标用 Canvas 画，COUI 图标集里没有刷新/浏览器） */
@Composable
private fun CircleSlotBtn(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .then(glassModifier(CircleShape))
            .lightPress(onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun IconRefresh() {
    val c = CouiTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(20.dp)) {
        val sw = 2.0.dp.toPx()
        drawArc(
            color = c,
            startAngle = -50f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(sw, sw),
            size = androidx.compose.ui.geometry.Size(size.width - sw * 2, size.height - sw * 2),
            style = Stroke(width = sw)
        )
        // 箭头
        val tip = androidx.compose.ui.geometry.Offset(size.width * 0.80f, size.height * 0.10f)
        val p1 = androidx.compose.ui.geometry.Offset(size.width * 0.52f, size.height * 0.02f)
        val p2 = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.06f)
        val p3 = androidx.compose.ui.geometry.Offset(size.width * 0.86f, size.height * 0.46f)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(p1.x, p1.y); lineTo(p2.x, p2.y); lineTo(p3.x, p3.y); close()
        }
        drawPath(path, c)
        // 让箭头与弧线衔接
        drawLine(c, tip, androidx.compose.ui.geometry.Offset(size.width * 0.62f, size.height * 0.16f), strokeWidth = sw)
    }
}

@Composable
private fun IconBrowser() {
    val c = CouiTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(20.dp)) {
        val sw = 1.8.dp.toPx()
        // 方框（左下）
        val left = size.width * 0.08f
        val top = size.height * 0.34f
        val right = size.width * 0.62f
        val bottom = size.height * 0.94f
        drawLine(c, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left, bottom), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(right, bottom), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(right, bottom), androidx.compose.ui.geometry.Offset(right, size.height * 0.66f), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(size.width * 0.34f, top), sw)
        // 外跳箭头
        drawLine(c, androidx.compose.ui.geometry.Offset(size.width * 0.38f, size.height * 0.62f), androidx.compose.ui.geometry.Offset(size.width * 0.94f, size.height * 0.06f), sw * 1.1f)
        drawLine(c, androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.06f), androidx.compose.ui.geometry.Offset(size.width * 0.94f, size.height * 0.06f), sw * 1.1f)
        drawLine(c, androidx.compose.ui.geometry.Offset(size.width * 0.94f, size.height * 0.06f), androidx.compose.ui.geometry.Offset(size.width * 0.94f, size.height * 0.34f), sw * 1.1f)
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

/** 卡片在窗口中的位置，用于帖子详情页的放大/缩小过渡 */
object CardZoom {
    var origin: androidx.compose.ui.geometry.Rect? = null
    var bitmap: androidx.compose.ui.graphics.ImageBitmap? = null
    var layer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
}

private data class ZoomScopes(
    val shared: androidx.compose.animation.SharedTransitionScope,
    val animated: androidx.compose.animation.AnimatedVisibilityScope
)

private val LocalZoomScopes = staticCompositionLocalOf<ZoomScopes?> { null }
private val LocalZoomTid = staticCompositionLocalOf<String?> { null }
internal val LocalBarInset = compositionLocalOf { 0.dp }

internal object BarOverlay {
    var inset: androidx.compose.ui.unit.Dp by mutableStateOf(0.dp)
}

/** 卡片与帖子页共享同一 key，交给 Compose 的 sharedBounds 做卡片↔全屏容器变换 */
@Composable
private fun Modifier.threadZoom(key: String?): Modifier {
    val z = LocalZoomScopes.current ?: return this
    if (key.isNullOrBlank()) return this
    // 只有当前正在做卡片<->详情过渡的那条帖子才挂共享元素
    if (key != LocalZoomTid.current) return this
    return with(z.shared) {
        this@threadZoom.sharedBounds(
            rememberSharedContentState("thread-$key"),
            z.animated,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(160)),
            boundsTransform = { _, _ -> tween(500, easing = FastOutSlowInEasing) },
            resizeMode = androidx.compose.animation.SharedTransitionScope.ResizeMode.scaleToBounds(
                ContentScale.Crop,
                Alignment.Center
            )
        )
    }
}

@Composable
private fun CardBox(
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    zoomKey: String? = null,
    content: @Composable () -> Unit
) {
    val colors = CouiTheme.colorScheme
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // 整卡等比缩放：背景和内容一起缩，不做横竖分别拉伸（那会把文字和图片拉变形）
    val scale by animateFloatAsState(if (pressed) 0.975f else 1f, tween(160))
    var touch by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Unspecified) }
    var bounds by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    var rectRoot by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    val gl = rememberGraphicsLayer()
    val cardScope = rememberCoroutineScope()
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onGloballyPositioned {
                bounds = it.boundsInWindow()
                rectRoot = it.boundsInRoot()
            }
            .drawWithContent {
                if (pressed) {
                    gl.record { this@drawWithContent.drawContent() }
                    drawLayer(gl)
                } else {
                    drawContent()
                }
            }

            .clip(shape)
            .background(if (pressed) colors.card else colors.container)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    touch = down.position
                    while (true) {
                        val e = awaitPointerEvent()
                        val ch = e.changes.firstOrNull { it.id == down.id } ?: break
                        touch = ch.position
                        if (!ch.pressed) break
                    }
                }
            }
            .then(
                if (onClick != null || onLongClick != null) Modifier.combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = {
                        val act = onClick
                        if (act != null) {
                            CardZoom.origin = rectRoot
                            CardZoom.layer = gl
                            cardScope.launch {
                                CardZoom.bitmap = runCatching { gl.toImageBitmap() }.getOrNull()
                                act()
                            }
                        }
                    },
                    onLongClick = onLongClick
                ) else Modifier
            )
    ) {
        // 跟随手指的柔光：只在卡片内部画，不参与布局
        if (pressed && touch != androidx.compose.ui.geometry.Offset.Unspecified) {
            Box(
                Modifier
                    .matchParentSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(colors.accent.copy(alpha = 0.16f), Color.Transparent),
                                center = touch,
                                radius = size.maxDimension * 0.85f
                            ),
                            radius = size.maxDimension * 0.85f,
                            center = touch
                        )
                    }
            )
        }
        Box(Modifier.padding(14.dp)) { content() }
    }
}

// ---------------- 首页：版块 ----------------
@Composable
private fun HomeList(forums: List<ForumItem>, loading: Boolean, onOpen: (ForumItem) -> Unit) {
    val colors = CouiTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp + LocalBarInset.current, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Txt("版块", size = 13.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(6.dp))
        }
        items(forums) { f ->
            CardBox(onClick = { onOpen(f) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
private fun HistoryView(
    history: List<HistoryStore.Entry>,
    listState: LazyListState,
    onOpen: (String, String) -> Unit,
    onClear: () -> Unit,
    onSelectionCount: (Int) -> Unit = {},
    onDeleteSelected: (List<Long>) -> Unit = {},
    onSelectionMode: (Boolean) -> Unit = {},
    exitSignal: Int = 0,
    backdrop: com.itos.coui.backdrop.CouiBackdrop? = null,
    blur: Boolean = false
) {
    val colors = CouiTheme.colorScheme
    // 选择模式：长按进入，点按切换选中；顶栏显示已选数量
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    LaunchedEffect(selecting, selected.size) {
        onSelectionCount(if (selecting) selected.size else 0)
        onSelectionMode(selecting)
    }
    LaunchedEffect(exitSignal) {
        if (exitSignal > 0) {
            selecting = false
            selected = emptySet()
        }
    }
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Txt("还没有浏览记录", size = 14.sp, color = colors.onSurfaceVariant)
        }
        return
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp + LocalBarInset.current, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Txt("共 ${history.size} 条", size = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                    if (!selecting) {
                        Txt(
                            "清空",
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onClear() }.padding(horizontal = 8.dp, vertical = 4.dp),
                            size = 12.sp,
                            color = colors.accent
                        )
                    }
                }
            }
            items(history, key = { it.tid }) { e ->
                // 预览图优先用本地字节（ImageStore），没有本地文件才退回地址，由 Coil 读它自己的磁盘缓存
                val thumbs = remember(e.tid) {
                    ThumbStore.get(e.tid.toString()).take(3).map { url -> ImageStore.local(url) ?: url }
                }
                val picked = e.tid in selected
                Box(
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (picked) Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.5.dp, colors.accent, RoundedCornerShape(18.dp))
                            else Modifier
                        )
                ) {
                    CardBox(
                        onClick = {
                            if (selecting) {
                                selected = if (picked) selected - e.tid else selected + e.tid
                            } else onOpen(e.url, e.title)
                        },
                        zoomKey = if (selecting) null else e.tid.toString(),
                        onLongClick = {
                            selecting = true
                            selected = selected + e.tid
                        }
                    ) {
                        Column {
                            Txt(
                                e.title.ifBlank { "帖子 " + e.tid },
                                size = 15.sp,
                                weight = FontWeight.SemiBold,
                                color = colors.onSurface,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Txt(historyTime(e.ts), size = 12.sp, color = colors.onSurfaceVariant)
                            if (thumbs.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                ThumbsGrid(thumbs)
                            }
                        }
                    }
                }
            }
        }

        // 选择模式底部居中的胶囊删除按钮：上垃圾桶图标，下「删除」
        if (selecting) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
                    .width(150.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .then(
                        if (selected.isNotEmpty() && blur && backdrop != null) Modifier.drawCouiBackdrop(
                            backdrop,
                            shape = { RoundedCornerShape(26.dp) },
                            effects = { blur(28f) },
                            onDrawSurface = { drawRect(Color(0xFF8E3B3B).copy(alpha = 0.88f)) }
                        ) else Modifier.background(
                            if (selected.isEmpty()) colors.onSurfaceVariant.copy(alpha = 0.35f)
                            else Color(0xFF8E3B3B)
                        )
                    )
                    .clickable(enabled = selected.isNotEmpty()) {
                        val ids = selected.toList()
                        selected = emptySet()
                        selecting = false
                        onDeleteSelected(ids)
                    }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconTrash(tint = colors.onAccent)
                Spacer(Modifier.height(2.dp))
                Txt("删除", size = 12.sp, color = colors.onAccent)
            }
        }
    }
}

/** 浏览记录时间：今天显示「今天 HH:mm」，更早显示「MM-dd HH:mm」，跨年加年份 */
private fun historyTime(ts: Long): String {
    val now = java.util.Calendar.getInstance()
    val t = java.util.Calendar.getInstance().apply { timeInMillis = ts }
    val hm = String.format("%02d:%02d", t.get(java.util.Calendar.HOUR_OF_DAY), t.get(java.util.Calendar.MINUTE))
    val sameYear = now.get(java.util.Calendar.YEAR) == t.get(java.util.Calendar.YEAR)
    return when {
        sameYear && now.get(java.util.Calendar.DAY_OF_YEAR) == t.get(java.util.Calendar.DAY_OF_YEAR) -> "今天 $hm"
        sameYear -> String.format("%02d-%02d %s", t.get(java.util.Calendar.MONTH) + 1, t.get(java.util.Calendar.DAY_OF_MONTH), hm)
        else -> String.format("%d-%02d-%02d %s", t.get(java.util.Calendar.YEAR), t.get(java.util.Calendar.MONTH) + 1, t.get(java.util.Calendar.DAY_OF_MONTH), hm)
    }
}

@Composable
private fun ForumView(
    page: ForumPage?,
    loading: Boolean,
    onThread: (ThreadItem) -> Unit,
    onLoad: (String) -> Unit,
    currentUrl: String,
    onImage: (String) -> Unit,
    listState: LazyListState,
    refreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onThumbNeeded: (ThreadItem) -> Unit = {},
    searchUpdating: Boolean = false,
    onSwipeBoard: ((Boolean) -> Unit)? = null
) {
    val colors = CouiTheme.colorScheme
    var pull by remember { mutableStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val refreshingNow = rememberUpdatedState(refreshing)
    val updatingNow = rememberUpdatedState(searchUpdating)
    val scrollConn = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (available.y < 0 && pull > 0f) {
                    val d = available.y.coerceAtLeast(-pull)
                    pull += d
                    return androidx.compose.ui.geometry.Offset(0f, d)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (available.y > 0 && !listState.canScrollBackward && !refreshingNow.value && dragging) {
                    pull += available.y * 0.6f
                    if (pull > 900f) pull = 900f
                    return androidx.compose.ui.geometry.Offset(0f, available.y)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: androidx.compose.ui.unit.Velocity,
                available: androidx.compose.ui.unit.Velocity
            ): androidx.compose.ui.unit.Velocity {
                // 惯性滑动（尤其从列表底部快速甩回顶部）不算下拉，顺手复位残留位移
                if (!dragging && !refreshingNow.value) pull = 0f
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }
    val scope = rememberCoroutineScope()
    var spin by remember { mutableStateOf(0f) }
    var spinSpeed by remember { mutableStateOf(0f) }
    var lastPull by remember { mutableStateOf(0f) }
    val autoOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val autoPx = with(LocalDensity.current) { (LocalBarInset.current + 87.dp).toPx() }
    LaunchedEffect(refreshing, searchUpdating) {
        if (refreshing || searchUpdating) autoOffset.animateTo(autoPx, tween(300))
        else autoOffset.animateTo(0f, tween(380, easing = FastOutSlowInEasing))
    }
    fun settleTo(target: Float) = scope.launch {
        androidx.compose.animation.core.animate(
            initialValue = pull, targetValue = target,
            animationSpec = tween(380, easing = FastOutSlowInEasing)
        ) { v, _ -> pull = v }
    }
    // 刷新完成后，列表再慢慢回顶把动画盖住
    LaunchedEffect(refreshing) { if (!refreshing && pull > 0f) settleTo(0f) }
    // 转圈速度跟随手指位移：拉动时转，停手后慢慢停，刷新中匀速
    LaunchedEffect(Unit) {
        while (true) {
            androidx.compose.runtime.withFrameNanos { _ ->
                val d = pull - lastPull
                lastPull = pull
                spinSpeed = if (refreshingNow.value || updatingNow.value) 5.5f
                else (spinSpeed * 0.86f + d * 0.8f).coerceIn(-45f, 45f)
                spin = (spin + spinSpeed) % 360f
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        // 刷新动画固定藏在列表下面，列表下拉时才露出来
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = LocalBarInset.current + 28.dp)
                .zIndex(-1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(Modifier.size(18.dp)) {
                drawArc(
                    color = colors.onSurfaceVariant,
                    startAngle = spin,
                    sweepAngle = 250f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }
            Spacer(Modifier.width(8.dp))
            Txt(
                if (refreshing || searchUpdating) "刷新中…" else if (pull > 90f) "松开刷新" else "下拉刷新",
                size = 12.sp,
                color = colors.onSurfaceVariant
            )
        }
        LazyColumn(
            Modifier
                .fillMaxSize()
                .offset {
                    androidx.compose.ui.unit.IntOffset(0, maxOf(pull, autoOffset.value).toInt())
                }
                .background(colors.background)

                .nestedScroll(scrollConn)
                .pointerInput(refreshing) {
                    awaitPointerEventScope {
                        var accX = 0f
                        var accY = 0f
                        while (true) {
                            val e = awaitPointerEvent()
                            var moved = false
                            for (c in e.changes) {
                                val d = c.position - c.previousPosition
                                if (d != androidx.compose.ui.geometry.Offset.Zero) {
                                    accX += d.x; accY += d.y; moved = true
                                }
                            }
                            if (e.changes.any { it.pressed }) dragging = true
                            if (e.changes.isNotEmpty() && e.changes.all { !it.pressed }) {
                                dragging = false
                                val th = 110.dp.toPx()
                                if (onSwipeBoard != null && kotlin.math.abs(accX) > th &&
                                    kotlin.math.abs(accX) > kotlin.math.abs(accY) * 2f && !refreshing
                                ) {
                                    onSwipeBoard(accX > 0f)
                                    accX = 0f; accY = 0f
                                    continue
                                }
                                accX = 0f; accY = 0f
                                if (pull > 90f && !refreshing && !listState.canScrollBackward) {
                                    // 保持当前偏移，等刷新结束再回顶
                                    onRefresh()
                                } else if (!refreshing) {
                                    settleTo(0f)
                                }
                            }
                        }
                    }
                },
            state = listState,
            contentPadding = PaddingValues(start = 12.dp, top = 12.dp + LocalBarInset.current, end = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = page?.threads ?: emptyList()
            items(list, key = { it.tid }) { t ->
                LaunchedEffect(t.tid, t.thumbs.size) {
                    if (t.thumbs.isEmpty()) onThumbNeeded(t)
                }
                CardBox(onClick = { onThread(t) }, zoomKey = t.tid) {
                    Column {
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
private fun ThumbsGrid(thumbs: List<Any>, onImage: ((String) -> Unit)? = null) {
    val items9 = thumbs.take(9)
    val shape = RoundedCornerShape(10.dp)
    when {
        items9.size == 1 -> NetImage(
            url = items9[0],
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(shape),
            onClick = (items9[0] as? String)?.let { u -> { onImage?.invoke(u) } },
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
                    onClick = (u as? String)?.let { s0 -> { onImage?.invoke(s0) } },
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
                            onClick = (u as? String)?.let { s0 -> { onImage?.invoke(s0) } },
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
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp + LocalBarInset.current, bottom = 96.dp),
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
                            is Block.Text -> LinkifiedText(
                                item.text,
                                size = 15.sp,
                                color = colors.onSurface,
                                lineHeight = 1.55f,
                                onOpen = onExternal
                            )
                            is Block.Link -> Txt(
                                item.text,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onExternal(item.url) }
                                    .padding(horizontal = 2.dp, vertical = 1.dp),
                                size = 15.sp,
                                color = colors.accent,
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
                            is Block.Spoiler -> SpoilerBox(item, onImage, onExternal)
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
private fun ReplyDialog(submitting: Boolean, onCancel: () -> Unit, onSend: (String) -> Unit) {
    val colors = CouiTheme.colorScheme
    var text by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        runCatching { focus.requestFocus() }
        keyboard?.show()
    }
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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp).focusRequester(focus)
                )
            }
            if (submitting) {
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniSpinner()
                    Spacer(Modifier.width(10.dp))
                    Txt("回复提交中…", size = 13.sp, color = colors.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Pill("取消", onCancel, primary = false)
                Spacer(Modifier.width(10.dp))
                Pill(if (submitting) "提交中…" else "发送", {
                    if (!submitting && text.isNotBlank()) onSend(text.trim())
                }, primary = true)
            }
        }
    }
}

@Composable
private fun RateDialog(
    fields: List<Triple<String, String, String>>,
    submitting: Boolean,
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
            if (submitting) {
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniSpinner()
                    Spacer(Modifier.width(10.dp))
                    Txt("评分提交中…", size = 13.sp, color = colors.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Pill("取消", onCancel, primary = false)
                Spacer(Modifier.width(10.dp))
                Pill(if (submitting) "提交中…" else "提交评分", { if (!submitting) onSend(reason.trim()) }, primary = true)
            }
        }
    }
}


@Composable
private fun SpoilerBox(spoiler: Block.Spoiler, onImage: (String) -> Unit, onExternal: (String) -> Unit) {
    val colors = CouiTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .padding(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .clickable { open = !open }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Txt(if (open) "收起" else spoiler.title, size = 14.sp, weight = FontWeight.Medium, color = colors.accent)
            Spacer(Modifier.width(6.dp))
            Txt(if (open) "▴" else "▾", size = 12.sp, color = colors.accent)
        }
        if (open) {
            Spacer(Modifier.height(8.dp))
            buildParagraphs(spoiler.blocks).forEachIndexed { i, b ->
                if (i > 0) Spacer(Modifier.height(10.dp))
                when (b) {
                    is Block.Text -> LinkifiedText(b.text, size = 15.sp, color = colors.onSurface, lineHeight = 1.55f, onOpen = onExternal)
                    is Block.Link -> Txt(
                        b.text,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .clickable { onExternal(b.url) }
                            .padding(horizontal = 2.dp, vertical = 1.dp),
                        size = 15.sp,
                        color = colors.accent
                    )
                    is Block.Image -> NetImage(
                        url = b.url,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                        onClick = { onImage(b.url) }
                    )
                    else -> {}
                }
            }
        }
    }
}

private fun collectImageUrls(blocks: List<Block>): List<String> = blocks.flatMap { b ->
    when (b) {
        is Block.Image -> listOf(b.url)
        is Block.Spoiler -> collectImageUrls(b.blocks)
        else -> emptyList()
    }
}

@Composable
private fun ImageDialog(images: List<String>, startUrl: String, onClose: () -> Unit) {
    val list = images.ifEmpty { listOf(startUrl) }
    val startIndex = list.indexOf(startUrl).let { if (it >= 0) it else 0 }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = startIndex) { list.size }
    val scope = rememberCoroutineScope()
    // 每张图各自记住缩放/位移，来回切图能“从哪来回哪去”
    val zstates = remember(list) { androidx.compose.runtime.mutableStateMapOf<Int, ZState>() }
    Dialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black).clipToBounds()) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                ZoomableImage(
                    url = list[page],
                    st = remember(page) { zstates.getOrPut(page) { ZState() } },
                    onClose = onClose,
                    onSwipeOut = { dir ->
                        val target = page + dir
                        if (target in list.indices) {
                            scope.launch { pagerState.animateScrollToPage(target) }
                        }
                    }
                )
            }

            // 底部页标：圆点，当前页更大
            if (list.size > 1) {
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 22.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    for (i in list.indices) {
                        val active = i == pagerState.currentPage
                        Box(
                            Modifier
                                .size(if (active) 9.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.42f))
                        )
                    }
                }
            }

            // 返回键（左上角）
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) { IconImg(CouiIcons.Filled.Back, 18.dp, Color.White) }
        }
    }
}

private class ZState {
    val scale = mutableStateOf(1f)
    val ox = mutableStateOf(0f)
    val oy = mutableStateOf(0f)
    val overX = mutableStateOf(0f)
}

/** 单页图片：双击缩放（锚定点击处）、双指缩放（锚定手指中点）、放大后拖动 */
@Composable
private fun ZoomableImage(
    url: String,
    st: ZState,
    onClose: () -> Unit,
    onSwipeOut: (Int) -> Unit = {}
) {
    var scale by st.scale
    var box by remember(url) { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var ratio by remember(url) { mutableStateOf(1f) }
    var oxv by st.ox
    var oyv by st.oy
    var overX by st.overX
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    fun displaySize(sc: Float): FloatArray {
        val cw = box.width.toFloat(); val ch = box.height.toFloat()
        if (cw <= 0f || ch <= 0f || ratio <= 0f) return floatArrayOf(0f, 0f)
        return if (cw / ch > ratio) floatArrayOf(ch * ratio, ch) else floatArrayOf(cw, cw / ratio)
    }
    fun boundsFor(sc: Float): Pair<Float, Float> {
        val d = displaySize(sc)
        val cw = box.width.toFloat(); val ch = box.height.toFloat()
        return Pair(((d[0] * sc - cw) / 2f).coerceAtLeast(0f), ((d[1] * sc - ch) / 2f).coerceAtLeast(0f))
    }
    val cur = boundsFor(scale)

    Box(
        Modifier.fillMaxSize().clipToBounds().onSizeChanged { box = it },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageStore.local(url) ?: url,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            onSuccess = { st2 ->
                val d = st2.result.drawable
                if (d.intrinsicWidth > 0 && d.intrinsicHeight > 0) {
                    ratio = d.intrinsicWidth.toFloat() / d.intrinsicHeight.toFloat()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(url) {
                    awaitEachGesture {
                        var hadGesture = false
                        do {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.size >= 2) {
                                hadGesture = true
                                val p0 = pressed[0]; val p1 = pressed[1]
                                val curDist = (p0.position - p1.position).getDistance()
                                val prevDist = (p0.previousPosition - p1.previousPosition).getDistance()
                                val zoom = if (prevDist > 1f) curDist / prevDist else 1f
                                val oldScale = scale
                                val newScale = (oldScale * zoom).coerceIn(1f, 6f)
                                val cx = box.width / 2f; val cy = box.height / 2f
                                val focal = androidx.compose.ui.geometry.Offset(
                                    (p0.position.x + p1.position.x) / 2f - cx,
                                    (p0.position.y + p1.position.y) / 2f - cy
                                )
                                // 以手指中点为锚点缩放
                                val k = newScale / oldScale
                                val (bmaxX, bmaxY) = boundsFor(newScale)
                                scale = newScale
                                if (newScale <= 1.02f) {
                                    oxv = 0f; oyv = 0f
                                } else {
                                    oxv = (focal.x - k * (focal.x - oxv)).coerceIn(-bmaxX, bmaxX)
                                    oyv = (focal.y - k * (focal.y - oyv)).coerceIn(-bmaxY, bmaxY)
                                }
                                pressed.forEach { it.consume() }
                            } else if (pressed.size == 1 && scale > 1.02f) {
                                hadGesture = true
                                val delta = pressed[0].position - pressed[0].previousPosition
                                val mx = boundsFor(scale).first
                                val my = boundsFor(scale).second
                                if (delta != androidx.compose.ui.geometry.Offset.Zero) {
                                    val nx = oxv + delta.x
                                    val cxOver = when {
                                        nx > mx -> { overX += nx - oxv; mx + (nx - mx) / 3f }
                                        nx < -mx -> { overX += nx - oxv; -mx + (nx + mx) / 3f }
                                        else -> { overX = 0f; nx }
                                    }
                                    val ny = oyv + delta.y
                                    val cyOver = when {
                                        ny > my -> my + (ny - my) / 3f
                                        ny < -my -> -my + (ny + my) / 3f
                                        else -> ny
                                    }
                                    oxv = cxOver
                                    oyv = cyOver
                                }
                                pressed[0].consume()
                            }
                        } while (pressed.any { it.pressed })

                        if (hadGesture) {
                            val over = overX
                            overX = 0f
                            val threshold = with(density) { 72.dp.toPx() }
                            if (kotlin.math.abs(over) > threshold) {
                                onSwipeOut(if (over > 0f) -1 else 1)
                                // 不重置缩放与位移：从哪张回来就回到哪
                            }
                            val (mx, my) = boundsFor(scale)
                            val tx = oxv.coerceIn(-mx, mx)
                            val ty = oyv.coerceIn(-my, my)
                            scope.launch { androidx.compose.animation.core.animate(oxv, tx) { v, _ -> oxv = v } }
                            scope.launch { androidx.compose.animation.core.animate(oyv, ty) { v, _ -> oyv = v } }
                        }
                    }
                }
                .pointerInput(url) {
                    detectTapGestures(
                        onDoubleTap = { pos ->
                            val old = scale
                            val target = if (old > 1.05f) 1f else 2.6f
                            val cx = box.width / 2f; val cy = box.height / 2f
                            val anchor = androidx.compose.ui.geometry.Offset(pos.x - cx, pos.y - cy)
                            val k = target / old
                            val (mx, my) = boundsFor(target)
                            val tx = if (target <= 1.02f) 0f else (anchor.x - k * (anchor.x - oxv)).coerceIn(-mx, mx)
                            val ty = if (target <= 1.02f) 0f else (anchor.y - k * (anchor.y - oyv)).coerceIn(-my, my)
                            scope.launch {
                                androidx.compose.animation.core.animate(scale, target, animationSpec = tween(280)) { v, _ -> scale = v }
                            }
                            scope.launch { androidx.compose.animation.core.animate(oxv, tx, animationSpec = tween(280)) { v, _ -> oxv = v } }
                            scope.launch { androidx.compose.animation.core.animate(oyv, ty, animationSpec = tween(280)) { v, _ -> oyv = v } }
                        },
                        onTap = { if (scale <= 1.05f) onClose() }
                    )
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = oxv
                    translationY = oyv
                }
        )
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
            is Block.Link -> { flush(); out.add(b) }
            is Block.Spoiler -> { flush(); out.add(b) }
            Block.Break -> flush()
        }
    }
    flush()
    return out
}

/** 带明显兜底的网络图片：加载中显示占位，成功后自动替换成原图，失败显示可点提示 */
@Composable
private fun NetImage(
    url: Any,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    crop: Boolean = false
) {
    val colors = CouiTheme.colorScheme
    SubcomposeAsyncImage(
        model = url,
        onSuccess = { st ->
            // 远程图加载成功后把字节写进 ImageStore，浏览记录等页面以后就能纯本地读
            if (url is String) {
                val bmp = (st.result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bmp != null) {
                    Thread {
                        runCatching {
                            val bos = java.io.ByteArrayOutputStream()
                            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, bos)
                            ImageStore.save(url, bos.toByteArray())
                        }
                    }.start()
                }
            }
        },
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

/** 小号转圈（弹窗内用） */
@Composable
private fun MiniSpinner(size: Dp = 20.dp, color: Color = CouiTheme.colorScheme.accent) {
    val transition = rememberInfiniteTransition()
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing))
    )
    Canvas(Modifier.size(size)) {
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 260f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )
    }
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


// ---------------- 搜索页 ----------------
@Composable
private fun SearchScreen(
    history: List<String>,
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val colors = CouiTheme.colorScheme
    var q by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        runCatching { focus.requestFocus() }
        keyboard?.show()
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp).padding(top = LocalBarInset.current)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.container)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (q.isEmpty()) {
                    Txt("搜索帖子、作者、关键词", size = 14.sp, color = colors.onSurfaceVariant)
                }
                BasicTextField(
                    value = q,
                    onValueChange = { q = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = colors.onSurface, fontSize = 14.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (q.isNotBlank()) {
                            keyboard?.hide()
                            onSubmit(q.trim())
                        }
                    }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus)
                )
            }
            Spacer(Modifier.width(10.dp))
            Pill("搜索", {
                if (q.isNotBlank()) {
                    keyboard?.hide()
                    onSubmit(q.trim())
                }
            }, primary = true)
        }
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Txt("搜索历史", size = 12.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            history.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { h ->
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.card)
                                .clickable {
                                    keyboard?.hide()
                                    onSubmit(h)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Txt(h, size = 13.sp, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Txt("搜索使用站内搜索，结果按关键词匹配；可以搜作者名或标题片段。", size = 11.sp, color = colors.onSurfaceVariant)
    }
}

// ---------------- 设置页 ----------------
@Composable
private fun SettingsView(
    fontScale: Float,
    themeMode: Int,
    blurBg: Boolean = true,
    onSetBlurBg: (Boolean) -> Unit = {},
    cacheMb: Int,
    cacheBytes: Long,
    cacheCount: Int,
    onSetThemeMode: (Int) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onSetCacheMb: (Int) -> Unit,
    onClearCache: () -> Unit,
    onExportLogs: () -> Unit = {},
    onAccounts: () -> Unit = {},
    onExternal: (String) -> Unit
) {
    val colors = CouiTheme.colorScheme
    @Composable
    fun section(title: String, content: @Composable () -> Unit) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.card)
                .padding(14.dp)
        ) {
            Txt(title, size = 13.sp, weight = FontWeight.SemiBold, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            content()
        }
        Spacer(Modifier.height(10.dp))
    }

    @Composable
    fun choices(options: List<Pair<String, Boolean>>, onPick: (Int) -> Unit) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            options.forEachIndexed { i, (label, on) ->
                Pill(label, { onPick(i) }, primary = on)
            }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp + LocalBarInset.current, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            section("账号") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAccounts() }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconImg(CouiIcons.Filled.Users, 20.dp, colors.onSurface)
                    Spacer(Modifier.width(12.dp))
                    Txt("账号管理", size = 14.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
                    IconImg(CouiIcons.Filled.Next, 18.dp, colors.onSurfaceVariant)
                }
            }
        }
        item {
            section("界面风格") {
                choices(
                    listOf("COUI 模糊背景" to blurBg, "经典纯色" to !blurBg)
                ) { i -> onSetBlurBg(i == 0) }
                Spacer(Modifier.height(6.dp))
                Txt(
                    "模糊背景需要页面正文作为采样源，开销略高；卡顿时可切回经典纯色。",
                    size = 11.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
        item {
            section("字体大小") {
                val scales = listOf(1.0f to "标准", 1.12f to "较大", 1.24f to "大", 1.36f to "特大")
                choices(scales.map { it.second to (kotlin.math.abs(fontScale - it.first) < 0.02f) }) { i ->
                    onSetFontScale(scales[i].first)
                }
                Spacer(Modifier.height(6.dp))
                Txt("调大后正文、标题、按钮文字会一起放大。", size = 11.sp, color = colors.onSurfaceVariant)
            }
        }
        item {
            section("深色模式") {
                choices(listOf("跟随系统" to (themeMode == 0), "深色" to (themeMode == 1), "浅色" to (themeMode == 2))) {
                    onSetThemeMode(it)
                }
            }
        }
        item {
            section("帖子缓存") {
                val mb = (cacheBytes / 1024.0 / 1024.0)
                Txt(
                    "已用 %.1f MB（含浏览记录）· %d 篇帖子".format(mb, cacheCount),
                    size = 13.sp,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Txt("存储上限", size = 12.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                val limits = listOf(100, 300, 500, 1000)
                choices(limits.map { "$it MB" to (cacheMb == it) }) { i -> onSetCacheMb(limits[i]) }
                Spacer(Modifier.height(10.dp))
                Pill("清除全部缓存", onClearCache, primary = false)
                Spacer(Modifier.height(4.dp))
                Txt(
                    "清空只删缓存，不会动浏览记录（浏览记录请在浏览记录页点「清空」）；帖子缓存、正文图片、预览图、列表快照、浏览记录都算在已用容量里。",
                    size = 12.sp,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Pill("导出诊断日志", onExportLogs, primary = false)
                Spacer(Modifier.height(4.dp))
                Txt(
                    "遇到加载异常时点这里，把日志发给我排查；日志只记录请求地址、耗时和解析结果，不含账号密码。",
                    size = 12.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 1.5f
                )
            }
        }
        item {
            section("关于") {
                Txt("GameMale 客户端 3.0", size = 14.sp, weight = FontWeight.Medium, color = colors.onSurface)
                Spacer(Modifier.height(6.dp))
                Txt(
                    "非官方 Android 客户端，界面基于开源 COUI-Kit（仿 ColorOS 17）绘制，数据来自 gamemale.com 网页。代码全程由 AI 编写。",
                    size = 12.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 1.55f
                )
                Spacer(Modifier.height(6.dp))
                Txt(
                    "首页默认进入「和谐动漫」的兽人漫分类（fid=150 / typeid=831），「和谐游戏」默认进入兽人向游戏分类（fid=152 / typeid=737）。",
                    size = 12.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 1.55f
                )
                Spacer(Modifier.height(10.dp))
                Pill("访问站点", { onExternal("https://www.gamemale.com/") }, primary = false)
                Spacer(Modifier.height(8.dp))
                Pill("开源地址 · chain355/GameMale",
                    { onExternal("https://github.com/chain355/GameMale") }, primary = true)
                Spacer(Modifier.height(6.dp))
                Txt(
                    "github.com/chain355/GameMale",
                    size = 12.sp,
                    color = colors.accent,
                    modifier = Modifier.clickable { onExternal("https://github.com/chain355/GameMale") }
                )
            }
        }
    }
}


/** 账号管理：列表 + 右侧删除 + 末行新增 */
@Composable
private fun AccountDialog(
    accounts: List<Accounts.Acc>,
    currentId: String?,
    onSelect: (Accounts.Acc) -> Unit,
    onDelete: (Accounts.Acc) -> Unit,
    onAdd: () -> Unit,
    onClose: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .padding(vertical = 14.dp)
        ) {
            Txt(
                "账号管理",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                size = 17.sp,
                weight = FontWeight.Bold,
                color = colors.onSurface
            )
            if (accounts.isEmpty()) {
                Txt(
                    "还没有保存的账号",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    size = 13.sp,
                    color = colors.onSurfaceVariant
                )
            }
            accounts.forEach { acc ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(acc) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (acc.avatar.isNotBlank()) {
                        AsyncImage(
                            model = acc.avatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                    } else {
                        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            IconImg(CouiIcons.Filled.UserCircle, 26.dp, colors.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Txt(acc.name, size = 14.sp, color = colors.onSurface)
                        if (acc.id == currentId) {
                            Txt("当前账号", size = 11.sp, color = colors.accent)
                        }
                    }
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onDelete(acc) },
                        contentAlignment = Alignment.Center
                    ) { IconTrash() }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onAdd() }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconImg(CouiIcons.Filled.Add, 20.dp, colors.accent)
                Spacer(Modifier.width(12.dp))
                Txt("新增账号", size = 14.sp, color = colors.accent)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Pill("关闭", onClose, primary = false)
            }
        }
    }
}

/** 复制图标（两个叠起来的圆角方框） */
@Composable
private fun IconCopy() {
    val c = CouiTheme.colorScheme.onSurface
    Canvas(Modifier.size(20.dp)) {
        val sw = 1.6.dp.toPx()
        val w = size.width
        val h = size.height
        val r = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        // 后层
        drawRoundRect(
            color = c,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.16f, h * 0.10f),
            size = androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.60f),
            cornerRadius = r,
            style = androidx.compose.ui.graphics.drawscope.Stroke(sw)
        )
        // 前层
        drawRoundRect(
            color = c,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.30f),
            size = androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.60f),
            cornerRadius = r,
            style = androidx.compose.ui.graphics.drawscope.Stroke(sw)
        )
    }
}

/** 垃圾桶图标（COUI 图标集里没有删除） */
@Composable
private fun IconTrash(tint: Color? = null) {
    val c = tint ?: CouiTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(18.dp)) {
        val sw = 1.6.dp.toPx()
        val w = size.width
        val h = size.height
        // 桶盖
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.26f), androidx.compose.ui.geometry.Offset(w * 0.86f, h * 0.26f), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.26f), androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.14f), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.26f), androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.14f), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.14f), androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.14f), sw)
        // 桶身
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.26f), androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.90f), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.26f), androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.90f), sw)
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.90f), androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.90f), sw)
        // 两条竖纹
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.42f), androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.78f), sw * 0.8f)
        drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.42f), androidx.compose.ui.geometry.Offset(w * 0.56f, h * 0.78f), sw * 0.8f)
    }
}

/** 未登录时的提示页 */
@Composable
private fun LoginPromptView(onLogin: () -> Unit) {
    val colors = CouiTheme.colorScheme
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Txt("还没有登录", size = 17.sp, weight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(10.dp))
            Txt(
                "未登录时首页帖子列表是加载不出来的。先登录 GameMale 账号，再回来刷新列表。",
                size = 13.sp,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
            Pill("去登录", onLogin, primary = true)
        }
    }
}

/** 顶栏/登录页用的对勾图标（COUI 图标集里没有 Check） */
@Composable
private fun IconCheck() {
    val c = CouiTheme.colorScheme.onSurface
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val p = androidx.compose.ui.graphics.Path()
        p.moveTo(w * 0.12f, h * 0.52f)
        p.lineTo(w * 0.40f, h * 0.80f)
        p.lineTo(w * 0.88f, h * 0.22f)
        drawPath(p, color = c, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f))
    }
}

/** 版块因用户组等级不足打不开时的提示页 */
@Composable
private fun UpgradeView(loading: Boolean, onOpenSite: () -> Unit) {
    val colors = CouiTheme.colorScheme
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Txt("该版块暂时打不开", size = 17.sp, weight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(10.dp))
            Txt(
                "站点提示你的用户组等级不够。可以回到原站多参与互动、完成任务把等级升上去，再回来刷新列表。",
                size = 13.sp,
                color = colors.onSurfaceVariant ?: colors.onSurface
            )
            Spacer(Modifier.height(18.dp))
            Pill("去原站做任务升级", onOpenSite, primary = true)
            if (loading) {
                Spacer(Modifier.height(18.dp))
                MiniSpinner()
            }
        }
    }
}
