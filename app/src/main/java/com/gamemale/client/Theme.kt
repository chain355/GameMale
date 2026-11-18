package com.gamemale.client

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.itos.coui.theme.CouiColorScheme
import com.itos.coui.theme.CouiPresets

/**
 * 跟随系统取色（Android 12+ 的 Monet 调色板）。
 *
 * 取 system_accent1_500 作为强调色，并用它给背景、容器做极浅的染色，
 * 保持 ColorOS 的“凝光”质感；系统没有提供调色板时回退到 COUI 的默认蓝。
 */
fun systemAccent(context: Context): Color? = try {
    Color(context.getColor(android.R.color.system_accent1_500))
} catch (e: Throwable) {
    null
}

fun accentOnColor(accent: Color): Color =
    if (accent.luminance() > 0.55f) Color(0xFF101010) else Color.White

fun buildScheme(context: Context, dark: Boolean): CouiColorScheme {
    val accent = systemAccent(context)
        ?: return if (dark) CouiPresets.Dark else CouiPresets.Light
    val neutral = try {
        Color(context.getColor(android.R.color.system_neutral2_500))
    } catch (e: Throwable) {
        Color(0xFF808080)
    }

    return if (dark) {
        CouiColorScheme(
            accent = accent,
            onAccent = accentOnColor(accent),
            surface = Color(0xFF191A1C),
            container = Color(0xFF2A2C30),
            onSurface = Color(0xE6FFFFFF),
            onSurfaceVariant = Color(0x8AFFFFFF),
            outlineVariant = Color(0x2EFFFFFF),
            background = lerp(Color.Black, neutral, 0.12f),
            card = Color(0x14FFFFFF)
        )
    } else {
        CouiColorScheme(
            accent = accent,
            onAccent = accentOnColor(accent),
            surface = Color.White,
            container = Color.White,
            onSurface = Color(0xFF1B1C1E),
            onSurfaceVariant = Color(0x99000000),
            outlineVariant = Color(0x1F000000),
            background = lerp(Color(0xFFF4F4F6), neutral, 0.10f),
            card = Color.White
        )
    }
}

@Composable
fun rememberScheme(dark: Boolean): CouiColorScheme {
    val context = LocalContext.current
    return remember(dark) { buildScheme(context, dark) }
}

/** 注入网页层的十六进制色值，让帖子页的按钮、分页、链接跟 App 强调色一致 */
fun Color.toHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}
