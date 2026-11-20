package com.gamemale.client

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.layout.positionInParent
import kotlinx.coroutines.delay
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.SideEffect
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.Job
import kotlinx.coroutines.yield
import kotlin.math.roundToInt

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
    /** 手指是否真的从按下项滑到了另一项；直接点按不算 */
    var dragging by mutableStateOf(false)
    /** 手指是否发生过超过 touchSlop 的位移；用于区分点按与按压拖动 */
    var moved by mutableStateOf(false)
    /** 菜单整块在根坐标系里的位置，用来判断手指是否已经拖出菜单 */
    var panel by mutableStateOf(androidx.compose.ui.geometry.Rect.Zero)
    /** 手指的 Y（根坐标系），拖出菜单后高亮也要继续跟到这里 */
    var pointerY by mutableStateOf(-1f)
    var pointerX by mutableStateOf(-1f)
    var inside by mutableStateOf(true)
    var onCommit: ((Int) -> Unit)? = null

    fun reset() {
        hover = -1
        pointerY = -1f
        pointerX = -1f
        inside = true
        dragging = false
        moved = false
    }
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
                val down = awaitFirstDown(requireUnconsumed = false)
                val start = down.position
                val absDown = androidx.compose.ui.geometry.Offset(origin.x + down.position.x, origin.y + down.position.y)
                state.hover = index
                state.pointerY = absDown.y
                state.pointerX = absDown.x
                state.inside = state.panel == androidx.compose.ui.geometry.Rect.Zero ||
                    state.panel.contains(absDown)
                state.dragging = false
                var last = index
                while (true) {
                    val e = awaitPointerEvent()
                    val ch = e.changes.firstOrNull() ?: break
                    val pt = androidx.compose.ui.geometry.Offset(origin.x + ch.position.x, origin.y + ch.position.y)
                    state.pointerY = pt.y
                    state.pointerX = pt.x
                    state.inside = state.panel == androidx.compose.ui.geometry.Rect.Zero ||
                        state.panel.contains(pt)
                    // 手指要真的滑出一段距离才算拖动；直接点按时轻微抖动不算
                    if ((ch.position - start).getDistance() > viewConfiguration.touchSlop) {
                        state.moved = true
                        val hit = state.bounds.entries
                            .sortedBy { it.value.top }
                            .firstOrNull { it.value.contains(pt) }
                        if (hit != null) {
                            if (hit.key != last) state.dragging = true
                            last = hit.key
                            state.hover = hit.key
                        } else {
                            state.dragging = true
                        }
                    }
                    if (!ch.pressed) break
                }
                // 松手时手指在菜单外：不执行任何功能，高亮直接复位消失
                val onRow = state.bounds[state.hover]
                    ?.contains(androidx.compose.ui.geometry.Offset(state.pointerX, state.pointerY)) == true
                if (state.inside && state.hover >= 0 && onRow) state.onCommit?.invoke(last)
                state.reset()
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
    showActiveBg: Boolean = true,
    onClick: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    val hot = active
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (hot && showActiveBg) colors.accent.copy(alpha = 0.16f) else Color.Transparent)
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
    showActiveBg: Boolean = true,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    val hot = active
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (hot && showActiveBg) colors.accent.copy(alpha = 0.16f) else Color.Transparent)
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

/** 五角星：收藏用（COUI 图标集里没有星形） */
@Composable
private fun IconStar(size: Dp = 20.dp, color: Color = CouiTheme.colorScheme.onSurface) {
    Canvas(Modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val rOut = this.size.minDimension * 0.46f
        val rIn = rOut * 0.42f
        val p = androidx.compose.ui.graphics.Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) rOut else rIn
            // 从正上方开始，每 36° 一个顶点
            val ang = (-90.0 + i * 36.0) * Math.PI / 180.0
            val x = cx + (r * kotlin.math.cos(ang)).toFloat()
            val y = cy + (r * kotlin.math.sin(ang)).toFloat()
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        drawPath(p, color = color)
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
            DialogHead("搜索帖子", onCancel)
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
    overflow: TextOverflow = TextOverflow.Clip,
    decoration: TextDecoration? = null
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight,
            textAlign = align ?: TextAlign.Start,
            lineHeight = (size.value * lineHeight).sp,
            textDecoration = decoration
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
    onOpenHistoryTab: () -> Unit = {},
    animePage: ForumPage? = null,
    gamePage: ForumPage? = null,
    animeUrl: String = ANIME_URL,
    gameUrl: String = GAME_URL,
    onRootTab: (Int) -> Unit = {},
    blockedWords: List<String> = emptyList(),
    watchWords: List<String> = emptyList(),
    readTids: Set<Long> = emptySet(),
    contentPrefOpen: Boolean = false,
    contentPrefKind: Int = 0,
    contentPrefItems: List<String> = emptyList(),
    onOpenContentPref: (Int) -> Unit = {},
    onCloseContentPref: () -> Unit = {},
    onAddContentPref: (List<String>) -> Unit = {},
    onDeleteContentPref: (String) -> Unit = {},
    changelogOpen: Boolean = false,
    onOpenChangelog: () -> Unit = {},
    onCloseChangelog: () -> Unit = {},
    upgradeReleases: List<Changelog.Release> = emptyList(),
    onDismissUpgradeLog: () -> Unit = {},
    firstRunNotice: Boolean = false,
    onDismissFirstRunNotice: () -> Unit = {},
    appVersion: String = "",
    watchLater: List<WatchStore.Entry> = emptyList(),
    watchSelecting: Boolean = false,
    watchSelectionCount: Int = 0,
    onWatchSelectionCount: (Int) -> Unit = {},
    onWatchSelecting: (Boolean) -> Unit = {},
    onOpenWatch: (String, String) -> Unit = { _, _ -> },
    onClearWatch: () -> Unit = {},
    onDeleteWatch: (List<Long>) -> Unit = {},
    watchTidSaved: Boolean = false,
    onToggleWatch: () -> Unit = {},
    attachPayOpen: Boolean = false,
    attachPayName: String = "",
    attachPayPrice: String = "",
    attachPayBalance: String = "",
    attachPayBusy: Boolean = false,
    onConfirmAttachPay: () -> Unit = {},
    onCloseAttachPay: () -> Unit = {},
    attachConfirmOpen: Boolean = false,
    attachConfirmName: String = "",
    attachConfirmMeta: String = "",
    onConfirmAttachDownload: () -> Unit = {},
    onCloseAttachConfirm: () -> Unit = {},
    threadTid: Long? = null,
    threadListState: LazyListState = rememberLazyListState(),
    attachments: List<AttachmentStore.Entry> = emptyList(),
    attachLoads: Map<String, AttachLoad> = emptyMap(),
    attachTexts: Map<String, String> = emptyMap(),
    attachFocus: String? = null,
    onFocusConsumed: () -> Unit = {},
    onNeedAttachText: (String) -> Unit = {},
    onOpenAttachment: (AttachmentStore.Entry) -> Unit = {},
    onCopyText: (String) -> Unit = {},
    onFreeCopyText: (String, String) -> Unit = { _, _ -> },
    onOpenAttachManager: () -> Unit = {},
    attachmentCount: Int = 0,
    attachSelecting: Boolean = false,
    attachSelectionCount: Int = 0,
    onAttachSelecting: (Boolean) -> Unit = {},
    onAttachSelectionCount: (Int) -> Unit = {},
    onDeleteAttachments: (List<Long>) -> Unit = {},
    fileTextOpen: Boolean = false,
    fileTextTitle: String = "",
    fileTextBody: String = "",
    onCloseFileText: () -> Unit = {}
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
    // 首页三个 tab 各自记住滚动位置，盖上详情/搜索时也不会被拆掉
    val historyListState = rememberLazyListState()
    val watchListState = rememberLazyListState()
    val overlayHistoryState = rememberLazyListState()
    val attachListState = rememberLazyListState()
    val animeListState = rememberLazyListState()
    val gameListState = rememberLazyListState()
    val rootPager = rememberPagerState(initialPage = 0) { 4 }
    val scope = rememberCoroutineScope()
    var seenAnimeUrl by remember { mutableStateOf<String?>(null) }
    var seenGameUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(animeUrl) {
        val prev = seenAnimeUrl
        seenAnimeUrl = animeUrl
        if (prev != null && prev != animeUrl) runCatching { animeListState.scrollToItem(0) }
    }
    LaunchedEffect(gameUrl) {
        val prev = seenGameUrl
        seenGameUrl = gameUrl
        if (prev != null && prev != gameUrl) runCatching { gameListState.scrollToItem(0) }
    }
    // 滑到某一页并停稳后，才去对上那一页的数据；滑的过程中首页三页都还在
    LaunchedEffect(rootPager.settledPage, showBottomBar) {
        if (showBottomBar) onRootTab(rootPager.settledPage)
    }
    // 搜索结果等盖层列表：只在盖层里记住位置，不要跟着首页 tab 一起滚
    val forumPositions = remember { androidx.compose.runtime.mutableStateMapOf<String, Int>() }
    var lastForumUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentUrl, showBottomBar) {
        if (showBottomBar) {
            lastForumUrl = currentUrl
            return@LaunchedEffect
        }
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
    // 右上角「全选」按钮给当前选择页发信号；页面自己把可见条目全部勾上
    var selectAllSignal by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var clearSelectionSignal by remember { androidx.compose.runtime.mutableIntStateOf(0) }
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
    val inHistorySelection = (nav is Nav.History && historySelecting) ||
        (nav is Nav.Watch && watchSelecting) ||
        (nav is Nav.Attachments && attachSelecting)
    // 全选按钮的「已全选」态：按当前页可见条目（屏蔽词过滤后）算
    val selectionAllSelected = when {
        nav is Nav.Watch && watchSelecting ->
            watchSelectionCount > 0 && watchSelectionCount >= watchLater.count {
                !ContentPrefs.hit(it.title, blockedWords)
            }
        nav is Nav.History && historySelecting ->
            historySelectionCount > 0 && historySelectionCount >= history.count {
                !ContentPrefs.hit(it.title, blockedWords)
            }
        nav is Nav.Attachments && attachSelecting ->
            attachSelectionCount > 0 && attachSelectionCount >= attachments.size
        else -> false
    }
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
        if (nav is Nav.Thread) add(
            Triple(if (watchTidSaved) "取消收藏" else "收藏", 8) { onToggleWatch() }
        )
        // 已经在设置页/附件管理页时不再给「设置」，否则点头像会把自己再打开一次
        if (nav !is Nav.Settings && nav !is Nav.Attachments) add(Triple("设置", 3) { onSettings() })
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
                onSelectAll = {
                    // 统一发一个「切换」信号，由当前选择页按自己的全选状态决定全选还是取消
                    selectAllSignal++
                },
                selectionAllSelected = selectionAllSelected,
                backdrop = backdrop,
                blur = overlayBar && blurBg,
                menuOpen = topMenuOpen,
                onToggleMenu = {
                    // 设置页/附件管理页：头像纯展示，点了不展开菜单也不打开设置
                    if (nav is Nav.Settings || nav is Nav.Attachments) return@TopBar
                    // 只有一项时没必要展开菜单，直接执行；一项都没有就什么都不做
                    if (topMenuItems.isEmpty()) {
                        // 设置页/附件管理页已经没别的动作，点头像不再打开设置
                    } else if (topMenuItems.size == 1 && topMenuOpen.not()) {
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
                avatarInert = nav is Nav.Settings || nav is Nav.Attachments,
                onShare = onShare,
                onDump = onDump,
                title = if (showBottomBar) {
                    when (rootPager.currentPage) {
                        0 -> "和谐动漫"
                        1 -> "和谐游戏"
                        2 -> if (watchSelecting) "已选择 " + watchSelectionCount + " 项" else "收藏"
                        else -> if (historySelecting) "已选择 " + historySelectionCount + " 项" else "浏览记录"
                    }
                } else when (nav) {
                    is Nav.Home -> "GameMale"
                    Nav.Settings -> "设置"
                    Nav.Search -> "搜索"
                    Nav.Watch -> if (watchSelecting) "已选择 " + watchSelectionCount + " 项" else "收藏"
                    Nav.History -> if (historySelecting) "已选择 " + historySelectionCount + " 项" else "浏览记录"
                    Nav.Attachments -> if (attachSelecting) "已选择 " + attachSelectionCount + " 项" else "附件管理"
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
                    .then(if (blurBg) Modifier.couiBackdrop(backdrop) else Modifier)
            ) {
                // 首页一直铺在最底下：三个 tab 左右滑，详情/搜索只是盖上来的一层
                HorizontalPager(
                    state = rootPager,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    userScrollEnabled = showBottomBar && !inHistorySelection
                ) { page ->
                    val settled = rootPager.settledPage == page && showBottomBar
                    when (page) {
                        0 -> HomeBoardPage(
                            page = animePage,
                            url = animeUrl,
                            listState = animeListState,
                            loading = loading && settled,
                            refreshing = refreshing && settled,
                            error = if (settled) error else null,
                            needLogin = needLogin && settled,
                            needUpgrade = needUpgrade && settled,
                            onOpenThread = onOpenThread,
                            onLoad = onLoad,
                            onImage = onImage,
                            onRefresh = onRefresh,
                            onThumbNeeded = onThumbNeeded,
                            onRetry = onRetry,
                            onDump = onDump,
                            onLogin = onLogin,
                            onOpenSite = onOpenSite,
                            blocked = blockedWords,
                            watch = watchWords,
                            readTids = readTids
                        )
                        1 -> HomeBoardPage(
                            page = gamePage,
                            url = gameUrl,
                            listState = gameListState,
                            loading = loading && settled,
                            refreshing = refreshing && settled,
                            error = if (settled) error else null,
                            needLogin = needLogin && settled,
                            needUpgrade = needUpgrade && settled,
                            onOpenThread = onOpenThread,
                            onLoad = onLoad,
                            onImage = onImage,
                            onRefresh = onRefresh,
                            onThumbNeeded = onThumbNeeded,
                            onRetry = onRetry,
                            onDump = onDump,
                            onLogin = onLogin,
                            onOpenSite = onOpenSite,
                            blocked = blockedWords,
                            watch = watchWords,
                            readTids = readTids
                        )
                        2 -> HistoryView(
                            watchLater.map { HistoryStore.Entry(it.tid, it.title, it.url, it.ts) },
                            watchListState, onOpenWatch, onClearWatch,
                            blocked = blockedWords,
                            watch = watchWords,
                            exitSignal = historyExitSignal,
                            selectAllSignal = selectAllSignal,
                            clearSelectionSignal = clearSelectionSignal,
                            onSelectionCount = onWatchSelectionCount,
                            onSelectionMode = onWatchSelecting,
                            onDeleteSelected = onDeleteWatch,
                            backdrop = backdrop,
                            blur = blurBg,
                            emptyText = "还没有收藏",
                            deleteText = "移除"
                        )
                        else -> HistoryView(
                            history, historyListState, onOpenHistory, onClearHistory,
                            blocked = blockedWords,
                            watch = watchWords,
                            exitSignal = historyExitSignal,
                            selectAllSignal = selectAllSignal,
                            clearSelectionSignal = clearSelectionSignal,
                            onSelectionCount = onHistorySelectionCount,
                            onSelectionMode = onHistorySelecting,
                            onDeleteSelected = onDeleteHistory,
                            backdrop = backdrop,
                            blur = blurBg
                        )
                    }
                }

                val overlayShown = !showBottomBar
                var heldOverlay by remember { mutableStateOf<Nav?>(null) }
                SideEffect { if (overlayShown) heldOverlay = nav }
                val overlayNav = if (overlayShown) nav else heldOverlay
                /*
                 * 页面级淡入：AnimatedVisibility 只在「底栏显隐」时触发，附件管理页和详情页之间
                 * （两者都是 overlay）来回切时不会播动画，看起来就像硬切。这里按 nav 变化补一层淡入：
                 * 进入附件管理、以及从附件管理点「查看原帖」进详情，都走 0→1 的透明度过渡。
                 */
                val pageFade = remember { androidx.compose.animation.core.Animatable(1f) }
                var lastOverlayNav by remember { mutableStateOf<Nav?>(null) }
                LaunchedEffect(overlayNav) {
                    val n = overlayNav
                    val fade = n is Nav.Attachments || (lastOverlayNav is Nav.Attachments && n is Nav.Thread)
                    if (fade && n != lastOverlayNav) {
                        pageFade.snapTo(0f)
                        pageFade.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
                    } else {
                        pageFade.snapTo(1f)
                    }
                    lastOverlayNav = n
                }
                AnimatedVisibility(
                    visible = overlayShown,
                    modifier = Modifier.fillMaxSize(),
                    // 设置页统一走淡入淡出，别再按「从右侧滑入」硬切；以后新页面也复用这套
                    enter = if (nav is Nav.Thread || nav is Nav.Attachments || nav is Nav.Settings)
                        fadeIn(tween(220, easing = FastOutSlowInEasing))
                    else slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(160)),
                    exit = if (heldOverlay is Nav.Thread || heldOverlay is Nav.Attachments ||
                        heldOverlay is Nav.Settings
                    ) fadeOut(tween(180, easing = FastOutSlowInEasing))
                    else slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(140))
                ) {
                    /*
                     * 用 AnimatedContent 管 overlay 内部的页面切换：附件管理页 <-> 设置页/详情页
                     * 都是 overlay，外层 AnimatedVisibility 的 visible 不变，必须在这里补淡入淡出。
                     * 其它页面切换保持硬切，避免和卡片放大、外层的滑入动画叠在一起。
                     */
                    AnimatedContent(
                        targetState = overlayNav,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            if (initialState is Nav.Attachments || targetState is Nav.Attachments ||
                                initialState is Nav.Settings || targetState is Nav.Settings
                            ) {
                                fadeIn(tween(220, easing = FastOutSlowInEasing)) togetherWith
                                    fadeOut(tween(190, easing = FastOutSlowInEasing))
                            } else {
                                EnterTransition.None togetherWith ExitTransition.None
            }
                        },
                        label = "overlayPage"
                    ) { target ->
                    val cur = target ?: return@AnimatedContent
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = backProgress * size.width * 0.35f
                                var a = pageFade.value
                                if (zoomRect != null && cur is Nav.Thread) {
                                    a *= if (zoomMode > 0) {
                                        ((zoomProg.value - 0.35f) / 0.5f).coerceIn(0f, 1f)
                                    } else {
                                        ((1f - zoomProg.value) / 0.45f).coerceIn(0f, 1f)
                                    }
                                }
                                alpha = a
                            }
                            .drawWithContent {
                                if (nav is Nav.Thread && cur is Nav.Thread) {
                                    pageGl.record { this@drawWithContent.drawContent() }
                                    drawLayer(pageGl)
                                } else {
                                    drawContent()
                                }
                            }
                    ) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(colors.background)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {}
                        )
                        when {
                            error != null && (cur is Nav.Forum || cur is Nav.Thread) ->
                                ErrorView(error, onRetry, onDump, onLogin)
                            cur == Nav.Home -> HomeList(forums, loading, onOpenForum)
                            cur is Nav.Watch -> HistoryView(
                                watchLater.map { HistoryStore.Entry(it.tid, it.title, it.url, it.ts) },
                                overlayHistoryState, onOpenWatch, onClearWatch,
                                blocked = blockedWords,
                                watch = watchWords,
                                exitSignal = historyExitSignal,
                            selectAllSignal = selectAllSignal,
                            clearSelectionSignal = clearSelectionSignal,
                                onSelectionCount = onWatchSelectionCount,
                                onSelectionMode = onWatchSelecting,
                                onDeleteSelected = onDeleteWatch,
                                backdrop = backdrop,
                                blur = blurBg,
                                emptyText = "还没有收藏",
                                deleteText = "移除"
                            )
                            cur is Nav.History -> HistoryView(
                                history, overlayHistoryState, onOpenHistory, onClearHistory,
                                blocked = blockedWords,
                                watch = watchWords,
                                exitSignal = historyExitSignal,
                            selectAllSignal = selectAllSignal,
                            clearSelectionSignal = clearSelectionSignal,
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
                                blocked = blockedWords, watch = watchWords, readTids = readTids
                            )
                            cur is Nav.Thread -> ThreadView(
                                page = threadPage, loading = loading, onLoad = onLoad, onExternal = onExternal,
                                onReply = onReply, onRate = onRate, onAttach = onAttach, onImage = onImage,
                                listState = threadListState, threadTid = threadTid,
                                attachments = attachments, attachLoads = attachLoads, attachTexts = attachTexts,
                                attachFocus = attachFocus, onFocusConsumed = onFocusConsumed,
                                onNeedAttachText = onNeedAttachText,
                                onOpenAttachment = onOpenAttachment,
                                onCopyText = onCopyText, onFreeCopyText = onFreeCopyText
                            )
                            cur is Nav.Attachments -> AttachManagerPage(
                                entries = attachments,
                                listState = attachListState,
                                onOpenThread = { e -> onOpenHistory(e.url, e.title) },
                                onView = onOpenAttachment,
                                onSelectionCount = onAttachSelectionCount,
                                onSelectionMode = onAttachSelecting,
                                onDeleteSelected = onDeleteAttachments,
                                exitSignal = historyExitSignal,
                            selectAllSignal = selectAllSignal,
                            clearSelectionSignal = clearSelectionSignal,
                                backdrop = backdrop,
                                blur = blurBg
                            )
                            cur is Nav.Settings -> SettingsView(
                                onAccounts = onOpenAccounts,
                                blockedWords = blockedWords,
                                watchWords = watchWords,
                                onOpenContentPref = onOpenContentPref,
                                onOpenChangelog = onOpenChangelog,
                                appVersion = appVersion,
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
                                onExternal = onExternal,
                                onOpenAttachManager = onOpenAttachManager,
                                attachmentCount = attachmentCount
                            )
                        }
                        if (loading && overlayShown && (cur is Nav.Forum || cur is Nav.Thread)) {
                            LoadingLayer("正在加载…")
                        }
                    }
                    }
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
        // 底部三个 tab，跟首页 pager 同一页；按住可以滑到旁边的项
        val navDrag = remember { PressDragState() }
        // 「和谐动漫 / 和谐游戏」：单击在顶部时刷新，否则回顶；300ms 内双击刷新当前列表。
        // 按压拖动（超过 touchSlop）只执行原来的切页，不触发回顶或刷新。
        var navTapIndex by remember { mutableIntStateOf(-1) }
        var navTapAt by remember { mutableLongStateOf(0L) }
        val navListState: (Int) -> androidx.compose.foundation.lazy.LazyListState = { idx ->
            if (idx == 0) animeListState else gameListState
        }
        val switchTab: (Int) -> Unit = { idx ->
            scope.launch { rootPager.animateScrollToPage(idx) }
        }
        navDrag.onCommit = { idx ->
            val wasDrag = navDrag.moved
            if (wasDrag || rootPager.settledPage != idx) {
                navTapIndex = -1
                switchTab(idx)
            } else if (idx <= 1) {
                val now = android.os.SystemClock.uptimeMillis()
                val st = navListState(idx)
                val atTop = st.firstVisibleItemIndex == 0 && st.firstVisibleItemScrollOffset == 0
                if (navTapIndex == idx && now - navTapAt <= 300L) {
                    // 第二次点击落在 300ms 内：按双击处理，刷新当前列表
                    navTapIndex = -1
                    onRefresh()
                } else {
                    navTapIndex = idx
                    navTapAt = now
                    if (atTop) onRefresh() else scope.launch { st.animateScrollToItem(0) }
                }
            } else {
                // 「收藏 / 浏览记录」：单击只回顶，列表已在顶部就什么都不做（不刷新）
                navTapIndex = -1
                val st = if (idx == 2) watchListState else historyListState
                val atTop = st.firstVisibleItemIndex == 0 && st.firstVisibleItemScrollOffset == 0
                if (!atTop) scope.launch { st.animateScrollToItem(0) }
            }
        }
        if (showBottomBar && !inHistorySelection) {
            // 导航栏 + 搜索 FAB 作为一整组，底部居中；两者之间留 8dp
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(bottom = 10.dp)
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Box(
                Modifier
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
                val tabPosition = (rootPager.currentPage + rootPager.currentPageOffsetFraction).coerceIn(0f, 3f)
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
                            val widths = floatArrayOf(
                                62.dp.toPx(), 62.dp.toPx(), 68.dp.toPx(), 68.dp.toPx()
                            )
                            val pos = tabPosition.coerceIn(0f, 3f)
                            val i = pos.toInt().coerceAtMost(3)
                            val t = pos - i
                            var left = 0f
                            for (k in 0 until i) left += widths[k]
                            left += if (i == 3) 0f else widths[i] * t
                            val width = if (i == 3) widths[3]
                            else widths[i] * (1f - t) + widths[(i + 1).coerceAtMost(3)] * t
                            val inset = 4.dp.toPx()
                            drawRoundRect(
                                color = colors.accent.copy(alpha = 0.16f),
                                topLeft = androidx.compose.ui.geometry.Offset(left, inset),
                                size = androidx.compose.ui.geometry.Size(width, size.height - inset * 2),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                            )
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
                val tab = rootPager.currentPage
                NavButton(
                    icon = CouiIcons.Outlined.Album,
                    label = "和谐动漫",
                    active = tab == 0,
                    modifier = Modifier.width(62.dp),
                    dragState = navDrag,
                    dragIndex = 0,
                    showActiveBg = false
                ) { scope.launch { rootPager.animateScrollToPage(0) } }
                NavButton(
                    icon = CouiIcons.Filled.Gamepad,
                    label = "和谐游戏",
                    active = tab == 1,
                    modifier = Modifier.width(62.dp),
                    dragState = navDrag,
                    dragIndex = 1,
                    showActiveBg = false
                ) { scope.launch { rootPager.animateScrollToPage(1) } }
                NavButtonSlot(
                    label = "收藏",
                    active = tab == 2,
                    modifier = Modifier.width(68.dp),
                    dragState = navDrag,
                    dragIndex = 2,
                    showActiveBg = false,
                    icon = { IconStar(20.dp, if (tab == 2) CouiTheme.colorScheme.accent else CouiTheme.colorScheme.onSurfaceVariant) },
                    onClick = { scope.launch { rootPager.animateScrollToPage(2) } }
                )
                NavButtonSlot(
                    label = "浏览记录",
                    active = tab == 3,
                    modifier = Modifier.width(68.dp),
                    dragState = navDrag,
                    dragIndex = 3,
                    showActiveBg = false,
                    icon = { IconHistory(if (tab == 3) CouiTheme.colorScheme.accent else CouiTheme.colorScheme.onSurfaceVariant) },
                    onClick = { scope.launch { rootPager.animateScrollToPage(3) } }
                )
                }
            }

            Spacer(Modifier.width(8.dp))
            // 搜索 FAB 与导航栏并排，同属底部那一组
            Box(
                Modifier
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
        if (contentPrefOpen) ContentPrefDialog(
            kind = contentPrefKind,
            items = contentPrefItems,
            onAdd = onAddContentPref,
            onDelete = onDeleteContentPref,
            onClose = onCloseContentPref
        )
        if (fileTextOpen) FileTextDialog(
            title = fileTextTitle,
            text = fileTextBody,
            onOpen = onExternal,
            onClose = onCloseFileText
        )
        if (attachConfirmOpen) AttachInfoDialog(
            name = attachConfirmName,
            meta = attachConfirmMeta,
            onConfirm = onConfirmAttachDownload,
            onCancel = onCloseAttachConfirm
        )
        if (attachPayOpen) AttachBuyDialog(
            name = attachPayName,
            price = attachPayPrice,
            balance = attachPayBalance,
            busy = attachPayBusy,
            onConfirm = onConfirmAttachPay,
            onCancel = onCloseAttachPay
        )
        if (changelogOpen) ChangelogDialog(Changelog.all, onCloseChangelog)
        // 使用声明优先级最高：确认之后才展示更新日志
        if (upgradeReleases.isNotEmpty() && !firstRunNotice) UpgradeLogDialog(upgradeReleases, onDismissUpgradeLog)
        if (firstRunNotice) FirstRunNoticeDialog(onDismissFirstRunNotice)
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
    avatarInert: Boolean = false,
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
    onSelectAll: () -> Unit = {},
    selectionAllSelected: Boolean = false,
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
            if (selectionMode) {
                // 选择模式下右上角统一是「全选」：和左上角返回同一尺寸、同一玻璃圆底
                SelectAllBtn(active = selectionAllSelected, onClick = onSelectAll)
            } else if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .clickable(enabled = !avatarInert) { onToggleMenu() }
                )
            } else {
                // 未登录时点头像也展开菜单，菜单里第一项是"登录"
                Box(
                    Modifier
                        .size(40.dp)
                        .then(glassModifier(CircleShape))
                        .lightPress { if (!avatarInert) onToggleMenu() },
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
                    .onGloballyPositioned {
                        val pos = it.positionInRoot()
                        dragState.panel = androidx.compose.ui.geometry.Rect(
                            pos, androidx.compose.ui.geometry.Size(it.size.width.toFloat(), it.size.height.toFloat())
                        )
                    }
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
                        if (dragState.inside && dragState.hover >= 0 && dragState.pointerY > 0f) {
                            val padX = 6.dp.toPx()
                            // 直接点按时高亮直接落在手指所在行，不做从上一项平移过来的动画
                            val top = (dragState.pointerY - dragState.panel.top - 23.dp.toPx())
                                .coerceIn(6.dp.toPx(), size.height - 52.dp.toPx())
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.12f),
                                topLeft = androidx.compose.ui.geometry.Offset(padX, top),
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
                            8 -> MenuIcon(AppIcon.Star)
                            else -> MenuIcon(AppIcon.Setting)
                        }
                        Spacer(Modifier.width(12.dp))
                        Txt(label, size = 14.sp, color = colors.onSurface)
                    }
                }
            }
        }
        // 手指拖出菜单后：高亮不再受菜单圆角裁剪，继续跟随手指 Y 移动
        if (!dragState.inside && dragState.hover >= 0 && dragState.pointerY > 0f &&
            dragState.panel != androidx.compose.ui.geometry.Rect.Zero
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val padX = 6.dp.toPx()
                        // 横向拖出菜单后高亮继续跟随手指，但纵向要夹在菜单内部，不能溢出到菜单卡片外
                        val localTop = (dragState.pointerY - dragState.panel.top - 23.dp.toPx())
                            .coerceIn(6.dp.toPx(), dragState.panel.height - 52.dp.toPx())
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.14f),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                dragState.panel.left + padX, dragState.panel.top + localTop
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                dragState.panel.width - padX * 2f, 46.dp.toPx()
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                        )
                    }
            )
        }
    }
}

private enum class AppIcon { Share, Browser, Refresh, Setting, Login, Copy, Star }

@Composable
private fun MenuIcon(kind: AppIcon) {
    when (kind) {
        AppIcon.Setting -> IconImg(CouiIcons.Filled.Setting, 20.dp, CouiTheme.colorScheme.onSurface)
        AppIcon.Share -> IconImg(ShareVector, 20.dp, CouiTheme.colorScheme.onSurface)
        AppIcon.Browser -> IconBrowser()
        AppIcon.Refresh -> IconRefresh()
        AppIcon.Login -> IconImg(CouiIcons.Filled.UserCircle, 20.dp, CouiTheme.colorScheme.onSurface)
        AppIcon.Copy -> IconCopy()
        AppIcon.Star -> IconStar()
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

/** 选择模式右上角的「双对勾」全选按钮，尺寸/圆底与左上返回完全一致 */
@Composable
private fun SelectAllBtn(active: Boolean, onClick: () -> Unit) {
    val colors = CouiTheme.colorScheme
    Box(
        Modifier
            .size(40.dp)
            .then(glassModifier(CircleShape))
            .lightPress(onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(20.dp)) {
            val tint = if (active) colors.accent else colors.onSurfaceVariant
            val stroke = 1.7.dp.toPx()
            fun tick(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float) {
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x0 * size.width, y0 * size.height)
                    lineTo(x1 * size.width, y1 * size.height)
                    lineTo(x2 * size.width, y2 * size.height)
                }
                drawPath(p, color = tint, style = Stroke(width = stroke))
            }
            tick(0.04f, 0.56f, 0.24f, 0.76f, 0.52f, 0.34f)
            tick(0.36f, 0.56f, 0.56f, 0.76f, 0.96f, 0.22f)
        }
    }
}

/**
 * 窗口统一标题栏：左上角返回箭头 + 标题。
 * 规范：任何窗口的「返回 / 关闭」都放左上角，不再用右上角叉或底部「关闭」。
 */
@Composable
private fun DialogHead(
    title: String,
    onBack: () -> Unit,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = CouiTheme.colorScheme
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CircleBtn(CouiIcons.Filled.Back, onBack)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Txt(title, size = 17.sp, weight = FontWeight.Bold, color = colors.onSurface)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Txt(subtitle, size = 12.sp, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** 等宽分段按钮：一排选项按屏幕宽度均分，窄屏也不会把最后一项挤出屏幕 */
@Composable
private fun SegBtn(text: String, on: Boolean, onClick: () -> Unit) {
    val colors = CouiTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (on) colors.accent else colors.container)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Txt(
            text,
            size = 12.sp,
            weight = FontWeight.Medium,
            color = if (on) colors.onAccent else colors.onSurfaceVariant,
            maxLines = 1
        )
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
    read: Boolean = false,
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
                } else if (read) {
                    // 已读态明显压暗：整卡（图片和文字）统一降亮度，但不转黑白
                    drawContent()
                    drawRect(Color.Black.copy(alpha = 0.45f))
                } else {
                    drawContent()
                }
            }

            .clip(shape)
            .background(if (pressed) colors.card else colors.container)
            .pointerInput(Unit) {
                // 这里只读位置给柔光用，不消费事件；外面套一层保护，
                // 免得长按/选择模式切换时指针协程被取消抛出异常把 App 带走
                runCatching {
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
    selectAllSignal: Int = 0,
    clearSelectionSignal: Int = 0,
    backdrop: com.itos.coui.backdrop.CouiBackdrop? = null,
    blur: Boolean = false,
    blocked: List<String> = emptyList(),
    watch: List<String> = emptyList(),
    emptyText: String = "还没有浏览记录",
    deleteText: String = "删除"
) {
    val colors = CouiTheme.colorScheme
    val shown = history.filter { e -> !ContentPrefs.hit(e.title, blocked) }
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
    var seenSelectAll by remember { androidx.compose.runtime.mutableIntStateOf(selectAllSignal) }
    LaunchedEffect(selectAllSignal) {
        if (selectAllSignal != seenSelectAll) {
            seenSelectAll = selectAllSignal
            if (selecting) {
                val all = shown.map { it.tid }.toSet()
                // 全选按钮是切换：已经全选就取消，否则补成全选
                selected = if (all.isNotEmpty() && selected.size >= all.size) emptySet() else all
            }
        }
    }
    var seenClearSelection by remember { androidx.compose.runtime.mutableIntStateOf(clearSelectionSignal) }
    LaunchedEffect(clearSelectionSignal) {
        if (clearSelectionSignal != seenClearSelection) {
            seenClearSelection = clearSelectionSignal
            if (selecting) selected = emptySet()
        }
    }
    if (shown.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Txt(
                if (history.isEmpty()) emptyText else "屏蔽词把这里的帖子全隐藏了",
                size = 14.sp, color = colors.onSurfaceVariant
            )
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
                    Txt("共 ${shown.size} 条", size = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
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
            items(shown, key = { it.tid }) { e ->
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
                                (if (ContentPrefs.hit(e.title, watch)) "✨ " else "") +
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
                    .background(
                        if (selected.isEmpty()) colors.onSurfaceVariant.copy(alpha = 0.35f)
                        else Color(0xFF8E3B3B).copy(alpha = 0.92f)
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.03f))
                        ),
                        RoundedCornerShape(26.dp)
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
                Txt(deleteText, size = 12.sp, color = colors.onAccent)
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

/** 首页里的一块版块：动漫或游戏。没有数据时只在自己这一页显示加载，不盖住旁边的页 */
@Composable
private fun HomeBoardPage(
    page: ForumPage?,
    url: String,
    listState: LazyListState,
    loading: Boolean,
    refreshing: Boolean,
    error: String?,
    needLogin: Boolean,
    needUpgrade: Boolean,
    onOpenThread: (ThreadItem) -> Unit,
    onLoad: (String) -> Unit,
    onImage: (String) -> Unit,
    onRefresh: () -> Unit,
    onThumbNeeded: (ThreadItem) -> Unit,
    onRetry: () -> Unit,
    onDump: () -> Unit,
    onLogin: () -> Unit,
    onOpenSite: () -> Unit,
    blocked: List<String> = emptyList(),
    watch: List<String> = emptyList(),
    readTids: Set<Long> = emptySet()
) {
    when {
        error != null -> ErrorView(error, onRetry, onDump, onLogin)
        needUpgrade && page?.threads.isNullOrEmpty() -> UpgradeView(loading, onOpenSite)
        needLogin && page?.threads.isNullOrEmpty() -> LoginPromptView(onLogin)
        page == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Txt("正在加载…", size = 14.sp, color = CouiTheme.colorScheme.onSurfaceVariant)
        }
        else -> ForumView(
            page, loading, onOpenThread, onLoad, url, onImage, listState,
            refreshing, onRefresh, onThumbNeeded,
            blocked = blocked, watch = watch, readTids = readTids
        )
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
    blocked: List<String> = emptyList(),
    watch: List<String> = emptyList(),
    readTids: Set<Long> = emptySet()
) {
    val colors = CouiTheme.colorScheme
    val density = LocalDensity.current
    val thresholdPx = with(density) { 64.dp.toPx() }
    val restPx = with(density) { 48.dp.toPx() }
    val maxPullPx = with(density) { 128.dp.toPx() }
    val indicatorPx = with(density) { 32.dp.toPx() }
    val pullState = remember { mutableFloatStateOf(0f) }
    val fingerDown = remember { mutableStateOf(false) }
    val pullGen = remember { mutableIntStateOf(0) }
    val settleJob = remember { mutableStateOf<Job?>(null) }
    val thresholdNow = rememberUpdatedState(thresholdPx)
    val restNow = rememberUpdatedState(restPx)
    val maxNow = rememberUpdatedState(maxPullPx)
    val refreshingNow = rememberUpdatedState(refreshing)
    val updatingNow = rememberUpdatedState(searchUpdating)
    val onRefreshNow = rememberUpdatedState(onRefresh)
    val scope = rememberCoroutineScope()
    fun settleTo(target: Float) {
        val gen = pullGen.intValue + 1
        pullGen.intValue = gen
        settleJob.value?.cancel()
        val from = pullState.floatValue
        if (kotlin.math.abs(from - target) < 0.5f) {
            pullState.floatValue = target
            return
        }
        settleJob.value = scope.launch {
            animate(
                initialValue = from,
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) { v, _ ->
                if (pullGen.intValue == gen) pullState.floatValue = v
            }
        }
    }
    val connection = remember(listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (refreshingNow.value || updatingNow.value) return Offset.Zero
                if (available.y < 0f && pullState.floatValue > 0f) {
                    pullGen.intValue += 1
                    settleJob.value?.cancel()
                    fingerDown.value = true
                    val next = (pullState.floatValue + available.y).coerceAtLeast(0f)
                    val consumed = next - pullState.floatValue
                    pullState.floatValue = next
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (refreshingNow.value || updatingNow.value) return Offset.Zero
                if (available.y > 0f && !listState.canScrollBackward) {
                    pullGen.intValue += 1
                    settleJob.value?.cancel()
                    fingerDown.value = true
                    pullState.floatValue = rubberPull(pullState.floatValue, available.y, maxNow.value)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val pulling = fingerDown.value
                fingerDown.value = false
                if (!pulling) return Velocity.Zero
                val pull = pullState.floatValue
                val busy = refreshingNow.value || updatingNow.value
                when {
                    busy -> if (pull > 0f) settleTo(restNow.value)
                    pull >= thresholdNow.value -> {
                        onRefreshNow.value()
                        scope.launch {
                            yield()
                            if (!refreshingNow.value && !updatingNow.value && !fingerDown.value) {
                                settleTo(0f)
                            }
                        }
                    }
                    pull > 0f -> settleTo(0f)
                }
                return if (pull > 0f) Velocity(0f, available.y) else Velocity.Zero
            }
        }
    }
    LaunchedEffect(refreshing, searchUpdating) {
        if (fingerDown.value) return@LaunchedEffect
        settleTo(if (refreshing || searchUpdating) restPx else 0f)
    }
    val pull = pullState.floatValue
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .offset { androidx.compose.ui.unit.IntOffset(0, pull.roundToInt()) }
                .background(colors.background)
                .nestedScroll(connection),
            state = listState,
            contentPadding = PaddingValues(start = 12.dp, top = 12.dp + LocalBarInset.current, end = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = (page?.threads ?: emptyList())
                .filter { t -> !ContentPrefs.hit(t.title, blocked) }
                .map { t ->
                    if (ContentPrefs.hit(t.title, watch)) t.copy(title = "✨ " + t.title) else t
                }
            items(list, key = { it.tid }) { t ->
                LaunchedEffect(t.tid, t.thumbs.size) {
                    if (t.thumbs.isEmpty()) onThumbNeeded(t)
                }
                val isRead = t.tid.toLongOrNull()?.let { it in readTids } == true
                CardBox(
                    onClick = { onThread(t) },
                    zoomKey = t.tid,
                    read = isRead
                ) {
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
                                color = if (isRead) colors.onSurfaceVariant.copy(alpha = 0.72f)
                                else colors.onSurface,
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
                            color = if (isRead) colors.onSurfaceVariant.copy(alpha = 0.55f)
                            else colors.onSurfaceVariant
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
        ListPullIndicator(
            pullPx = pull,
            thresholdPx = thresholdPx,
            indicatorPx = indicatorPx,
            refreshing = refreshing || searchUpdating,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/** 下拉距离来自列表位移，圆圈只是跟着这段位移走。 */
private fun rubberPull(current: Float, delta: Float, max: Float): Float {
    val t = (current / max).coerceIn(0f, 1f)
    val resistance = (0.65f * (1f - t * t)).coerceAtLeast(0.16f)
    return (current + delta * resistance).coerceIn(0f, max)
}

@Composable
private fun ListPullIndicator(
    pullPx: Float,
    thresholdPx: Float,
    indicatorPx: Float,
    refreshing: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = CouiTheme.colorScheme
    val fraction = (pullPx / thresholdPx).coerceIn(0f, 1f)
    val spinTransition = rememberInfiniteTransition()
    val spin by spinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing))
    )
    val alpha = if (refreshing) 1f else (pullPx / (thresholdPx * 0.35f)).coerceIn(0f, 1f)
    if (alpha <= 0f) return
    Box(
        modifier
            .padding(top = LocalBarInset.current)
            .offset { androidx.compose.ui.unit.IntOffset(0, (pullPx - indicatorPx).roundToInt()) }
            .graphicsLayer { this.alpha = alpha }
            .size(32.dp)
            .background(colors.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(16.dp)) {
            drawArc(
                color = if (refreshing || fraction >= 1f) colors.accent else colors.onSurfaceVariant,
                startAngle = if (refreshing) spin else -90f,
                sweepAngle = if (refreshing) 270f else 32f + 250f * fraction,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun ThumbsGrid(thumbs: List<Any>, onImage: ((String) -> Unit)? = null, dim: Boolean = false) {
    val items9 = thumbs.take(9)
    val shape = RoundedCornerShape(10.dp)
    val dimMod = Modifier.graphicsLayer { alpha = if (dim) 0.62f else 1f }
    when {
        items9.size == 1 -> NetImage(
            url = items9[0],
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .then(dimMod)
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
                        .then(dimMod)
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
                                .then(dimMod)
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
    onImage: (String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    threadTid: Long?,
    attachments: List<AttachmentStore.Entry>,
    attachLoads: Map<String, AttachLoad>,
    attachTexts: Map<String, String>,
    attachFocus: String?,
    onFocusConsumed: () -> Unit,
    onNeedAttachText: (String) -> Unit,
    onOpenAttachment: (AttachmentStore.Entry) -> Unit,
    onCopyText: (String) -> Unit,
    onFreeCopyText: (String, String) -> Unit
) {
    val colors = CouiTheme.colorScheme
    val attachOffsets = remember { androidx.compose.runtime.mutableStateMapOf<String, Float>() }
    val focusTopPx = with(androidx.compose.ui.platform.LocalDensity.current) { 84.dp.toPx() }
    LaunchedEffect(attachFocus) {
        val key = attachFocus ?: return@LaunchedEffect
        val idx = page?.posts?.indexOfFirst { p ->
            p.attachments.any { attachKey(threadTid, it.name) == key }
        } ?: -1
        if (idx >= 0) {
            // 购买成功/下载完成后不要再从顶部「动画滚过去」：那会先闪回整帖开头。
            // 用已知的附件块偏移直接定位，保证一次到位、不经过顶部。
            val off = attachOffsets[key]
            if (off != null) {
                listState.scrollToItem(idx + 1, (off - focusTopPx).toInt().coerceAtLeast(0))
            } else {
                // 首帧还没量到偏移（比如刚从附件管理跳进来）：先定位帖子，下一帧再补精确偏移
                listState.scrollToItem(idx + 1)
                delay(90)
                val measured = attachOffsets[key]
                if (measured != null) {
                    listState.scrollToItem(
                        idx + 1,
                        (measured - focusTopPx).toInt().coerceAtLeast(0)
                    )
                }
            }
        }
        onFocusConsumed()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
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
                            val key = attachKey(threadTid, at.name)
                            val load = attachLoads[key]
                            val entry = attachments.firstOrNull {
                                it.tid == (threadTid ?: 0L) && it.name == at.name.trim()
                            }
                            val txt = attachTexts[key]
                            val downloaded = entry != null || load?.done == true
                            Spacer(Modifier.height(6.dp))
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { attachOffsets[key] = it.positionInParent().y }
                            ) {
                                if (downloadTxt(entry, txt, key, downloaded, onNeedAttachText)) {
                                    // txt：正文直接替换附件区内容
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.card)
                                            .padding(10.dp)
                                    ) {
                                        Txt(at.name, size = 13.sp, weight = FontWeight.Medium, color = colors.accent)
                                        Spacer(Modifier.height(6.dp))
                                        LinkifiedText(
                                            txt.orEmpty(),
                                            size = 14.sp,
                                            color = colors.onSurface,
                                            lineHeight = 1.55f,
                                            onOpen = onExternal
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    AttachActionRow(
                                        downloaded = downloaded,
                                        txt = txt,
                                        onOpen = {
                                            entry?.let(onOpenAttachment) ?: onAttach(at)
                                        },
                                        onCopy = { onCopyText(txt.orEmpty()) },
                                        onFree = { onFreeCopyText(at.name, txt.orEmpty()) }
                                    )
                                } else {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.card)
                                            .clickable { if (entry != null) onOpenAttachment(entry) else onAttach(at) }
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                FileTypeIcon(at.name, 22.dp)
                                                Spacer(Modifier.width(8.dp))
                                                Column(Modifier.weight(1f)) {
                                                    Txt(at.name, size = 14.sp, weight = FontWeight.Medium, color = colors.accent)
                                                    val meta = listOfNotNull(
                                                        at.meta.takeIf { it.isNotEmpty() }, at.price
                                                    ).joinToString("  ")
                                                    if (meta.isNotEmpty()) Txt(meta, size = 12.sp, color = colors.onSurfaceVariant)
                                                }
                                            }
                                            if (load != null && !load.done && !load.failed) {
                                                Spacer(Modifier.height(8.dp))
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(colors.onSurfaceVariant.copy(alpha = 0.18f))
                                                ) {
                                                    Box(
                                                        Modifier
                                                            .fillMaxWidth(load.progress.coerceIn(0f, 1f))
                                                            .height(4.dp)
                                                            .clip(RoundedCornerShape(2.dp))
                                                            .background(colors.accent)
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Txt(
                                                    if (load.failed) "下载失败，点一下重试"
                                                    else "正在下载 ${(load.progress * 100).toInt()}%",
                                                    size = 11.sp,
                                                    color = if (load.failed) Color(0xFFB0392F) else colors.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    if (downloaded) {
                                        Spacer(Modifier.height(6.dp))
                                        AttachActionRow(
                                            downloaded = true,
                                            txt = null,
                                            onOpen = { entry?.let(onOpenAttachment) ?: onAttach(at) },
                                            onCopy = null,
                                            onFree = null
                                        )
                                    }
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
            DialogHead("回复", onCancel)
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(168.dp)
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
                    modifier = Modifier.fillMaxSize().focusRequester(focus)
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
            DialogHead("评分", onCancel, subtitle = "默认给最高档，需要细分请在网页端调整")
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
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.container)
                    .padding(12.dp)
            ) {
                if (reason.isEmpty()) Txt("评分理由（可留空）", size = 14.sp, color = colors.onSurfaceVariant)
                BasicTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                    modifier = Modifier.fillMaxSize()
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
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var attempt by remember(url) { androidx.compose.runtime.mutableIntStateOf(0) }
    var failed by remember(url) { mutableStateOf(false) }
    var retrying by remember(url) { mutableStateOf(false) }
    // 第 0 次正常走缓存；点击失败图后换一个禁用缓存的请求，保证真的重新联网取图
    val model = remember(url, attempt) {
        if (attempt == 0) url else coil.request.ImageRequest.Builder(ctx)
            .data(url)
            .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
            .diskCachePolicy(coil.request.CachePolicy.DISABLED)
            .build()
    }
    SubcomposeAsyncImage(
        model = model,
        onSuccess = { st ->
            failed = false
            retrying = false
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
        onError = {
            failed = true
            retrying = false
        },
        contentDescription = null,
        contentScale = if (crop) ContentScale.Crop else ContentScale.FillWidth,
        modifier = modifier.then(
            Modifier.clickable(enabled = onClick != null || failed) {
                if (failed) {
                    // 失败图点一下是重新拉取，不再打开大图
                    failed = false
                    retrying = true
                    attempt++
                } else {
                    onClick?.invoke()
                }
            }
        ),
        loading = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.card),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (retrying) {
                        MiniSpinner(24.dp)
                    } else {
                        IconImg(CouiIcons.Outlined.Photo, 28.dp, colors.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(6.dp))
                    Txt(
                        if (retrying) "正在重新加载…" else "图片加载中…",
                        size = 12.sp, color = colors.onSurfaceVariant
                    )
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
                    IconRefresh()
                    Spacer(Modifier.height(6.dp))
                    Txt("图片加载失败，点一下重试", size = 12.sp, color = colors.onSurfaceVariant)
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
    onExternal: (String) -> Unit,
    blockedWords: List<String> = emptyList(),
    watchWords: List<String> = emptyList(),
    onOpenContentPref: (Int) -> Unit = {},
    onOpenChangelog: () -> Unit = {},
    onOpenAttachManager: () -> Unit = {},
    attachmentCount: Int = 0,
    appVersion: String = ""
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

    var clearAsk by remember { mutableStateOf(false) }
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
            section("内容偏好") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenContentPref(0) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconNoEntry(20.dp)
                    Spacer(Modifier.width(12.dp))
                    Txt("屏蔽词", size = 14.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
                    Txt(
                        if (blockedWords.isEmpty()) "未设置" else "${blockedWords.size} 个",
                        size = 12.sp, color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    IconImg(CouiIcons.Filled.Next, 18.dp, colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenContentPref(1) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconImg(CouiIcons.Outlined.Album, 20.dp, colors.onSurface)
                    Spacer(Modifier.width(12.dp))
                    Txt("特别关注", size = 14.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
                    Txt(
                        if (watchWords.isEmpty()) "未设置" else "${watchWords.size} 个",
                        size = 12.sp, color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    IconImg(CouiIcons.Filled.Next, 18.dp, colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                Txt(
                    "屏蔽词命中的帖子会从首页列表里消失；特别关注命中的帖子标题前会加 ✨。两者只作用于首页的和谐动漫、和谐游戏和浏览记录，搜索结果与外部分享进来的帖子不受影响。",
                    size = 11.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 1.5f
                )
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
            section("附件管理") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenAttachManager() }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FileTypeIcon("file", 20.dp)
                    Spacer(Modifier.width(12.dp))
                    Txt("已下载附件", size = 14.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
                    Txt(
                        if (attachmentCount == 0) "暂无" else "$attachmentCount 个",
                        size = 12.sp, color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    IconImg(CouiIcons.Filled.Next, 18.dp, colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Txt(
                    "附件保存在 App 私有目录，删除 App 数据会一起清掉。",
                    size = 11.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
        item {
            section("帖子缓存") {
                val mb = (cacheBytes / 1024.0 / 1024.0)
                Txt(
                    "已用 %.1f MB（含浏览记录 / 附件）· %d 篇帖子".format(mb, cacheCount),
                    size = 13.sp,
                    color = colors.onSurface
                )
                if (attachmentCount > 0) {
                    Spacer(Modifier.height(2.dp))
                    Txt(
                        "其中已下载附件 %d 个，附件也算在存储上限内".format(attachmentCount),
                        size = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                Txt("存储上限", size = 12.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                val limits = listOf(100, 300, 500, 1000)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    limits.forEach { mb ->
                        Box(Modifier.weight(1f)) {
                            SegBtn("$mb MB", cacheMb == mb) { onSetCacheMb(mb) }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Pill("清除全部缓存", { clearAsk = true }, primary = false)
                Spacer(Modifier.height(4.dp))
                Txt(
                    "帖子缓存、正文图片、预览图、列表快照、浏览记录都算在已用容量里；附件同样计入。清理时会连已下载附件一起删除，点按钮后会再确认一次。浏览记录本身请在浏览记录页「清空」。",
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
                Txt(
                    if (appVersion.isBlank()) "GameMale 客户端" else "GameMale 客户端 $appVersion",
                    size = 14.sp, weight = FontWeight.Medium, color = colors.onSurface
                )
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
                Txt("开发人员", size = 13.sp, weight = FontWeight.SemiBold, color = colors.onSurface)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt("项目发起人", modifier = Modifier.weight(1f), size = 12.sp, color = colors.onSurfaceVariant)
                    Txt("chain355", size = 12.sp, color = colors.onSurface)
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt("功能代码编写", modifier = Modifier.weight(1f), size = 12.sp, color = colors.onSurfaceVariant)
                    Txt("豆包", size = 12.sp, color = colors.onSurface)
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt("UI 界面细节优化", modifier = Modifier.weight(1f), size = 12.sp, color = colors.onSurfaceVariant)
                    Txt(
                        "orz12",
                        size = 12.sp,
                        color = colors.accent,
                        decoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onExternal("https://github.com/orz12") }
                    )
                }
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
                Spacer(Modifier.height(12.dp))
                Pill("更新日志", onOpenChangelog, primary = false)
            }
        }
    }

    if (clearAsk) {
        Dialog(onDismissRequest = { clearAsk = false }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .padding(18.dp)
            ) {
                DialogHead("清除全部缓存", { clearAsk = false })
                Spacer(Modifier.height(10.dp))
                Txt(
                    "这次会同时删除已下载的 %d 个附件（含 txt 正文），删除后需要重新下载，且金币已扣不会退回。".format(attachmentCount),
                    size = 13.sp,
                    color = colors.onSurface,
                    lineHeight = 1.55f
                )
                if (attachmentCount > 0) {
                    Spacer(Modifier.height(6.dp))
                    Txt(
                        "缓存超限时系统会先清理帖子缓存，附件留到最后才会被清理。",
                        size = 12.sp,
                        color = colors.onSurfaceVariant,
                        lineHeight = 1.5f
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Pill("仍然清除", { clearAsk = false; onClearCache() }, primary = true)
                }
            }
        }
    }
}


/** txt 附件是否可以内联展示：下载完且正文读出来了才替换附件区 */
@Composable
private fun downloadTxt(
    entry: AttachmentStore.Entry?,
    txt: String?,
    key: String,
    downloaded: Boolean,
    onNeed: (String) -> Unit
): Boolean {
    val name = entry?.name ?: key.substringAfter("|")
    val isTxt = AttachmentStore.kindOf(name) == "txt"
    if (!isTxt) return false
    if (!downloaded) return false
    if (txt == null) {
        LaunchedEffect(key) { onNeed(key) }
        return false
    }
    return true
}

/** 附件卡片下方的操作按钮：打开文件 / 复制全文 / 自由复制 */
@Composable
private fun AttachActionRow(
    downloaded: Boolean,
    txt: String?,
    onOpen: () -> Unit,
    onCopy: (() -> Unit)?,
    onFree: (() -> Unit)?
) {
    val colors = CouiTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // txt 已内联成正文：不再给「打开文件」，只留复制类按钮
        if (txt == null) SmallAction("打开文件", enabled = downloaded, onClick = onOpen)
        if (txt != null && onCopy != null) SmallAction("复制全文", onClick = onCopy)
        if (txt != null && onFree != null) SmallAction("自由复制", onClick = onFree)
    }
}

@Composable
private fun SmallAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val colors = CouiTheme.colorScheme
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.accent.copy(alpha = if (enabled) 0.14f else 0.06f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Txt(
            text,
            size = 12.sp,
            color = if (enabled) colors.accent else colors.onSurfaceVariant.copy(alpha = 0.6f),
            weight = FontWeight.Medium
        )
    }
}

/** 按文件类型区分的图标：不同底色 + 类型缩写 */
@Composable
private fun FileTypeIcon(name: String, size: Dp) {
    val kind = AttachmentStore.kindOf(name)
    val tint = when (kind) {
        "txt" -> Color(0xFF7E9CBF)
        "pdf" -> Color(0xFFD47C79)
        "word" -> Color(0xFF7E9FD6)
        "pages" -> Color(0xFFDDB173)
        "archive" -> Color(0xFFA79AC0)
        "apk" -> Color(0xFF7FB894)
        "image" -> Color(0xFF83B7B4)
        else -> Color(0xFF9AA0A8)
    }
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.25.dp.toPx()
        // 纸张轮廓 + 折角，去掉原来的纯色底
        val doc = androidx.compose.ui.graphics.Path().apply {
            moveTo(0.16f * w, 0.08f * h)
            lineTo(0.62f * w, 0.08f * h)
            lineTo(0.84f * w, 0.30f * h)
            lineTo(0.84f * w, 0.92f * h)
            lineTo(0.16f * w, 0.92f * h)
            close()
        }
        drawPath(doc, color = tint, style = Stroke(width = stroke))
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(0.62f * w, 0.08f * h),
            end = androidx.compose.ui.geometry.Offset(0.62f * w, 0.30f * h),
            strokeWidth = stroke
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(0.62f * w, 0.30f * h),
            end = androidx.compose.ui.geometry.Offset(0.84f * w, 0.30f * h),
            strokeWidth = stroke
        )
        when (kind) {
            "image" -> {
                drawRoundRect(
                    color = tint,
                    topLeft = androidx.compose.ui.geometry.Offset(0.26f * w, 0.44f * h),
                    size = androidx.compose.ui.geometry.Size(0.48f * w, 0.30f * h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                    style = Stroke(width = stroke)
                )
                val mountain = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0.28f * w, 0.72f * h)
                    lineTo(0.42f * w, 0.56f * h)
                    lineTo(0.52f * w, 0.66f * h)
                    lineTo(0.60f * w, 0.54f * h)
                    lineTo(0.72f * w, 0.72f * h)
                }
                drawPath(mountain, color = tint, style = Stroke(width = stroke))
                drawCircle(tint, radius = 1.6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(0.64f * w, 0.50f * h))
            }
            "archive" -> {
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(0.50f * w, 0.38f * h),
                    end = androidx.compose.ui.geometry.Offset(0.50f * w, 0.84f * h),
                    strokeWidth = stroke
                )
                for (i in 0..3) {
                    val y = (0.42f + i * 0.10f) * h
                    drawLine(
                        color = tint,
                        start = androidx.compose.ui.geometry.Offset(0.44f * w, y),
                        end = androidx.compose.ui.geometry.Offset(0.56f * w, y),
                        strokeWidth = stroke
                    )
                }
            }
            "apk" -> {
                drawRoundRect(
                    color = tint,
                    topLeft = androidx.compose.ui.geometry.Offset(0.28f * w, 0.44f * h),
                    size = androidx.compose.ui.geometry.Size(0.44f * w, 0.30f * h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                    style = Stroke(width = stroke)
                )
                drawCircle(tint, radius = 1.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(0.40f * w, 0.54f * h))
                drawCircle(tint, radius = 1.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(0.60f * w, 0.54f * h))
            }
            else -> {
                val lines = if (kind == "pdf") 3 else 3
                for (i in 0 until lines) {
                    val y = (0.46f + i * 0.11f) * h
                    val endX = if (i == lines - 1) 0.58f else 0.72f
                    drawLine(
                        color = tint.copy(alpha = if (i == 0) 0.85f else 0.6f),
                        start = androidx.compose.ui.geometry.Offset(0.26f * w, y),
                        end = androidx.compose.ui.geometry.Offset(endX * w, y),
                        strokeWidth = stroke
                    )
                }
            }
        }
    }
}

/** 找出文本里的裸链接，供可选中文本标注用 */
private fun findBareLinks(text: String): List<Pair<IntRange, String>> {
    val out = ArrayList<Pair<IntRange, String>>()
    for (m in BARE_URL.findAll(text)) {
        var raw = m.value.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '\'', '"')
        if (raw.length <= 4) continue
        val href = if (raw.startsWith("http")) raw else "https://$raw"
        val range = m.range.first until (m.range.first + raw.length)
        if (out.any { it.first.first < range.last && range.first < it.first.last }) continue
        out.add(range to href)
    }
    return out
}

/** 可长按选中复制的文本，链接仍可点开 */
@Composable
private fun SelectableLinkText(text: String, size: androidx.compose.ui.unit.TextUnit, color: Color, onOpen: (String) -> Unit) {
    val accent = CouiTheme.colorScheme.accent
    val open = rememberUpdatedState(onOpen)
    val annotated = remember(text, accent) {
        val links = findBareLinks(text)
        if (links.isEmpty()) AnnotatedString(text)
        else buildAnnotatedString {
            var last = 0
            for ((range, href) in links) {
                append(text.substring(last, range.first))
                withLink(
                    LinkAnnotation.Url(
                        href,
                        TextLinkStyles(SpanStyle(color = accent, textDecoration = TextDecoration.Underline)),
                        LinkInteractionListener { open.value(href) }
                    )
                ) { append(text.substring(range.first, range.last + 1)) }
                last = range.last + 1
            }
            if (last < text.length) append(text.substring(last))
        }
    }
    BasicText(
        text = annotated,
        style = TextStyle(color = color, fontSize = size, lineHeight = (size.value * 1.55f).sp)
    )
}

/** 文本展示窗口：支持链接跳转、长按自由选择复制 */
@Composable
private fun FileTextDialog(title: String, text: String, onOpen: (String) -> Unit, onClose: () -> Unit) {
    val colors = CouiTheme.colorScheme
    Dialog(onDismissRequest = onClose) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
        ) {
            // 统一规范：窗口左上角返回箭头（不再用右上角叉）
            DialogHead(title.ifBlank { "附件正文" }, onClose)
            Spacer(Modifier.height(8.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(end = 4.dp)
            ) {
                SelectionContainer {
                    SelectableLinkText(text, 14.sp, colors.onSurface, onOpen)
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(6.dp))
            Txt(
                "长按可自由选择并复制文本",
                size = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/** 附件管理：已下载列表，复用浏览记录的长按多选与删除胶囊 */
@Composable
private fun AttachManagerPage(
    entries: List<AttachmentStore.Entry>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpenThread: (AttachmentStore.Entry) -> Unit,
    onView: (AttachmentStore.Entry) -> Unit,
    onSelectionCount: (Int) -> Unit,
    onDeleteSelected: (List<Long>) -> Unit,
    onSelectionMode: (Boolean) -> Unit,
    exitSignal: Int = 0,
    selectAllSignal: Int = 0,
    clearSelectionSignal: Int = 0,
    backdrop: com.itos.coui.backdrop.CouiBackdrop? = null,
    blur: Boolean = false
) {
    val colors = CouiTheme.colorScheme
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    LaunchedEffect(selecting, selected.size) {
        onSelectionCount(if (selecting) selected.size else 0)
        onSelectionMode(selecting)
    }
    LaunchedEffect(exitSignal) {
        if (exitSignal > 0) { selecting = false; selected = emptySet() }
    }
    var seenSelectAll by remember { androidx.compose.runtime.mutableIntStateOf(selectAllSignal) }
    LaunchedEffect(selectAllSignal) {
        if (selectAllSignal != seenSelectAll) {
            seenSelectAll = selectAllSignal
            if (selecting) {
                val all = entries.map { it.id }.toSet()
                selected = if (all.isNotEmpty() && selected.size >= all.size) emptySet() else all
            }
        }
    }
    var seenClearSelection by remember { androidx.compose.runtime.mutableIntStateOf(clearSelectionSignal) }
    LaunchedEffect(clearSelectionSignal) {
        if (clearSelectionSignal != seenClearSelection) {
            seenClearSelection = clearSelectionSignal
            if (selecting) selected = emptySet()
        }
    }
    LaunchedEffect(entries.size) {
        if (entries.isEmpty()) { selecting = false; selected = emptySet() }
    }
    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Txt("还没有下载过附件", size = 14.sp, color = colors.onSurfaceVariant)
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
                Txt(
                    "共 ${entries.size} 个附件 · 长按可多选",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    size = 12.sp,
                    color = colors.onSurfaceVariant
                )
            }
            items(entries, key = { it.id }) { e ->
                val picked = e.id in selected
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
                            if (selecting) selected = if (picked) selected - e.id else selected + e.id
                            else onView(e)
                        },
                        onLongClick = {
                            selecting = true
                            selected = selected + e.id
                        }
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            FileTypeIcon(e.name, 38.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Txt(
                                    e.name,
                                    size = 14.sp,
                                    weight = FontWeight.Medium,
                                    color = colors.onSurface
                                    // 文件名不限行数，右侧按钮固定宽度，不会压到一起
                                )
                                Spacer(Modifier.height(4.dp))
                                Txt(
                                    listOf(AttachmentStore.sizeText(e.size), historyTime(e.ts))
                                        .joinToString(" · "),
                                    size = 11.sp,
                                    color = colors.onSurfaceVariant
                                )
                                if (e.title.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Txt(
                                        "来自：" + e.title,
                                        size = 11.sp,
                                        color = colors.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                TinyAction("查看原帖") { onOpenThread(e) }
                                Spacer(Modifier.height(6.dp))
                                TinyAction("查看文件") { onView(e) }
                            }
                        }
                    }
                }
            }
        }
        if (selecting) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
                    .width(150.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        if (selected.isEmpty()) colors.onSurfaceVariant.copy(alpha = 0.35f)
                        else Color(0xFF8E3B3B).copy(alpha = 0.92f)
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.03f))
                        ),
                        RoundedCornerShape(26.dp)
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

/** 列表右侧的小按钮，固定宽度避免被长文件名挤压 */
@Composable
private fun TinyAction(text: String, onClick: () -> Unit) {
    val colors = CouiTheme.colorScheme
    Box(
        Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(colors.accent.copy(alpha = 0.13f))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Txt(text, size = 11.sp, color = colors.accent, weight = FontWeight.Medium)
    }
}

/** 权限/金币附件：下载前先说明要求，用户确认才下载 */
@Composable
private fun AttachInfoDialog(
    name: String,
    meta: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    Dialog(onDismissRequest = onCancel) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .padding(18.dp)
        ) {
            DialogHead("下载附件", onCancel)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FileTypeIcon(name, 30.dp)
                Spacer(Modifier.width(10.dp))
                Txt(
                    name.ifBlank { "附件" },
                    size = 14.sp,
                    weight = FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (meta.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Txt(meta, size = 12.sp, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Txt(
                "这个附件有阅读权限或金币要求，确认后才会开始下载。已经购买过的话，本次不会再扣金币。",
                size = 12.sp,
                color = colors.onSurfaceVariant,
                lineHeight = 1.5f
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Pill("确认下载", onConfirm, primary = true)
            }
        }
    }
}

/** 购买附件：展示售价与购买后余额，确认后走 attachpay POST */
@Composable
private fun AttachBuyDialog(
    name: String,
    price: String,
    balance: String,
    busy: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    androidx.compose.ui.window.Dialog(onDismissRequest = { if (!busy) onCancel() }) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .padding(18.dp)
        ) {
            DialogHead("购买附件", { if (!busy) onCancel() })
            Spacer(Modifier.height(10.dp))
            Txt(
                name.ifBlank { "附件" },
                size = 14.sp,
                color = colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            if (price.isNotBlank()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Txt("售价", size = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Txt(price, size = 13.sp, weight = FontWeight.Medium, color = colors.accent)
                }
                Spacer(Modifier.height(4.dp))
            }
            if (balance.isNotBlank()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Txt("购买后余额", size = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Txt(balance, size = 13.sp, color = colors.onSurface)
                }
                Spacer(Modifier.height(4.dp))
            }
            Txt(
                "金币由站点结算，购买后再次下载不再扣费；如需确认明细可到原站「购买记录」查看。",
                size = 11.sp,
                color = colors.onSurfaceVariant,
                lineHeight = 1.5f
            )
            if (busy) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniSpinner()
                    Spacer(Modifier.width(10.dp))
                    Txt("正在购买…", size = 12.sp, color = colors.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Pill(if (busy) "购买中…" else "购买并下载", { if (!busy) onConfirm() }, primary = true)
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
            DialogHead(
                "账号管理",
                onClose,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
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
        }
    }
}

/** 内容偏好：屏蔽词 / 特别关注。列表样式沿用账号管理（首行新增、右侧垃圾桶） */
@Composable
private fun ContentPrefDialog(
    kind: Int,
    items: List<String>,
    onAdd: (List<String>) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit
) {
    val colors = CouiTheme.colorScheme
    val title = if (kind == 0) "屏蔽词" else "特别关注"
    val tip = if (kind == 0) {
        "命中的帖子会从首页列表里直接消失（和谐动漫 / 和谐游戏 / 浏览记录）"
    } else {
        "命中的帖子标题前会加 ✨（和谐动漫 / 和谐游戏 / 浏览记录）"
    }
    var adding by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .padding(vertical = 14.dp)
        ) {
            DialogHead(
                title,
                onClose,
                subtitle = tip,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(10.dp))

            if (adding) {
                Box(
                    Modifier
                        .padding(horizontal = 18.dp)
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.container)
                        .padding(12.dp)
                ) {
                    if (text.isEmpty()) {
                        Txt(
                            "支持换行批量输入，一行一个关键词",
                            size = 13.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.height(4.dp))
                Txt(
                    "换行、逗号、分号都能分隔；重复的会自动去掉。",
                    modifier = Modifier.padding(horizontal = 18.dp),
                    size = 11.sp,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Pill("取消", {
                        adding = false
                        text = ""
                    }, primary = false)
                    Spacer(Modifier.width(10.dp))
                    Pill("添加", {
                        val parsed = ContentPrefs.parseBatch(text)
                        if (parsed.isNotEmpty()) {
                            onAdd(parsed)
                            text = ""
                            adding = false
                        }
                    }, primary = true)
                }
            } else {
                if (items.isEmpty()) {
                    Txt(
                        "还没有关键词",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                        size = 13.sp,
                        color = colors.onSurfaceVariant
                    )
                } else {
                    Column(
                        Modifier
                            .heightIn(max = 340.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        items.forEach { w ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Txt(w, size = 14.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { onDelete(w) },
                                    contentAlignment = Alignment.Center
                                ) { IconTrash() }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            text = ""
                            adding = true
                        }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconImg(CouiIcons.Filled.Add, 20.dp, colors.accent)
                    Spacer(Modifier.width(12.dp))
                    Txt("新增关键词", size = 14.sp, color = colors.accent)
                }
            }

        }
    }
}

/** 更新日志：新增功能排最前并带「- ✨」，调整优化与修复用灰色「- 」条目 */
@Composable
private fun ChangelogDialog(releases: List<Changelog.Release>, onClose: () -> Unit) {
    val colors = CouiTheme.colorScheme
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .padding(vertical = 14.dp)
        ) {
            DialogHead(
                "更新日志",
                onClose,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
            )
            Column(
                Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ReleaseList(releases)
            }

        }
    }
}

/** 首次安装的告知弹窗：个人学习研究作品，不得用于商业用途 */
@Composable
private fun FirstRunNoticeDialog(onDismiss: () -> Unit) {
    val colors = CouiTheme.colorScheme
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .padding(vertical = 14.dp)
        ) {
            DialogHead(
                "欢迎使用 GameMale 客户端",
                onDismiss,
                subtitle = "开始前，请先了解这几件事",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(horizontal = 18.dp)) {
                NoticeLine("本应用为个人学习研究作品，非 GameMale 官方客户端，不得用于任何商业用途，也不提供任何付费服务。")
                NoticeLine("应用内展示的帖子、图片与附件均来自原站点，版权归站点及原作者所有；请遵守站点规则和当地法律法规。")
                NoticeLine("登录状态、浏览记录、收藏与已下载附件只保存在本机，不会上传到第三方服务器；卸载应用会一并清除。")
                NoticeLine("继续使用即表示你已阅读并理解上述说明。")
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Pill("我已了解", onDismiss, primary = true)
            }
        }
    }
}

@Composable
private fun NoticeLine(text: String) {
    val colors = CouiTheme.colorScheme
    Row(Modifier.padding(vertical = 4.dp)) {
        Txt("·", size = 12.sp, color = colors.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Txt(text, size = 12.sp, color = colors.onSurface, lineHeight = 1.65f)
    }
}

/** 升级后首次启动的弹窗：只列本次跨越的版本区间 */
@Composable
private fun UpgradeLogDialog(releases: List<Changelog.Release>, onDismiss: () -> Unit) {
    val colors = CouiTheme.colorScheme
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .padding(vertical = 14.dp)
        ) {
            DialogHead(
                "已更新到 " + releases.first().version,
                onDismiss,
                subtitle = "以下是这次更新带来的变化",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(6.dp))
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ReleaseList(releases, compact = true)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Pill("知道了", onDismiss, primary = true)
            }
        }
    }
}

@Composable
private fun ReleaseList(releases: List<Changelog.Release>, compact: Boolean = false) {
    val colors = CouiTheme.colorScheme
    releases.forEachIndexed { idx, r ->
        if (idx > 0 || compact) {
            Txt(
                if (compact) "v" + r.version else r.version,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = if (compact) 8.dp else 6.dp),
                size = if (compact) 13.sp else 14.sp,
                weight = FontWeight.SemiBold,
                color = colors.accent
            )
        } else {
            Txt(
                r.version,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                size = 14.sp,
                weight = FontWeight.SemiBold,
                color = colors.accent
            )
        }
        r.features.forEach { f ->
            Row(Modifier.padding(horizontal = 18.dp, vertical = 3.dp)) {
                Txt("- ✨", size = 12.sp, color = colors.accent)
                Spacer(Modifier.width(6.dp))
                Txt(f, size = 12.sp, color = colors.onSurface, lineHeight = 1.55f)
            }
        }
        r.changes.forEach { f ->
            Row(Modifier.padding(horizontal = 18.dp, vertical = 3.dp)) {
                Txt("-", size = 12.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Txt(f, size = 12.sp, color = colors.onSurfaceVariant, lineHeight = 1.55f)
            }
        }
        r.fixes.forEach { f ->
            Row(Modifier.padding(horizontal = 18.dp, vertical = 3.dp)) {
                Txt("-", size = 12.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Txt(f, size = 12.sp, color = colors.onSurfaceVariant, lineHeight = 1.55f)
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

/**
 * 屏蔽词图标：圆圈加一道斜杠，跟系统的禁止符号一致。
 * COUI 图标集里没有这个形状，直接用 Canvas 画。
 */
@Composable
private fun IconNoEntry(size: Dp = 20.dp) {
    val c = CouiTheme.colorScheme.onSurface
    Canvas(Modifier.size(size)) {
        val sw = 1.8.dp.toPx()
        val r = this.size.minDimension * 0.42f
        drawCircle(color = c, radius = r, style = Stroke(width = sw))
        // 斜杠压在圆内，两端各留一点，避免出圈
        val k = r * 0.7071f
        drawLine(
            color = c,
            start = androidx.compose.ui.geometry.Offset(center.x - k * 0.86f, center.y - k * 0.86f),
            end = androidx.compose.ui.geometry.Offset(center.x + k * 0.86f, center.y + k * 0.86f),
            strokeWidth = sw
        )
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
