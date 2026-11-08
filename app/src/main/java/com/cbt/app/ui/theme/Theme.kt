package com.cbt.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================================
// Бело-зеленая палитра (CBT Calm Sage & Pure Canvas)
// Разработана для снижения сенсорной перегрузки при работе с тревожными мыслями.
// =========================================================================

val CbtPureWhite = Color(0xFFFFFFFF)
val CbtBackgroundCanvas = Color(0xFFFAFBF9)      // Едва уловимый теплый светлый фон без бликов
val CbtSurface = Color(0xFFFFFFFF)               // Чистый белый для блоков и листов
val CbtSurfaceVariant = Color(0xFFF1F5F2)        // Мягкий мятный оттенок для плашек и разделителей

val CbtSagePrimary = Color(0xFF1E6B47)           // Глубокий шалфейный (акцент стабильности)
val CbtSageSecondary = Color(0xFF385A48)         // Спокойный приглушенный зеленый
val CbtSageTertiary = Color(0xFF4C7860)          // Мягкий акцент

val CbtTextPrimary = Color(0xFF1B2620)           // Мягкий темно-графитовый (легче для глаз, чем черный)
val CbtTextSecondary = Color(0xFF5A6E63)         // Цвет пояснений и второстепенного текста
val CbtTextPlaceholder = Color(0xFF8FA397)       // Ненавязчивый цвет подсказок

val CbtBorderLight = Color(0xFFE2EBE5)           // Тончайшая граница карточек
val CbtBorderFocused = Color(0xFF82B89A)         // Мягкая подсветка фокуса

val CbtDangerSoft = Color(0xFFC04B45)            // Сдержанный цвет удаления

private val CbtColorScheme = lightColorScheme(
    primary = CbtSagePrimary,
    onPrimary = CbtPureWhite,
    primaryContainer = Color(0xFFE7F3EC),
    onPrimaryContainer = Color(0xFF0F3B25),
    secondary = CbtSageSecondary,
    onSecondary = CbtPureWhite,
    secondaryContainer = CbtSurfaceVariant,
    onSecondaryContainer = CbtTextPrimary,
    background = CbtBackgroundCanvas,
    onBackground = CbtTextPrimary,
    surface = CbtSurface,
    onSurface = CbtTextPrimary,
    surfaceVariant = CbtSurfaceVariant,
    onSurfaceVariant = CbtTextSecondary,
    outline = CbtBorderLight,
    outlineVariant = Color(0xFFD3E0D8)
)

// =========================================================================
// Эргономичная типографика для КПТ:
// Больший межстрочный интервал (lineHeight) предотвращает зрительную усталость
// и помогает структурировать когнитивные формулировки.
// =========================================================================

val CbtTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        color = CbtTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = CbtTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = CbtTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = CbtTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 27.sp,                      // Комфортный шаг строки для глубокого чтения мыслей
        letterSpacing = 0.25.sp,
        color = CbtTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp,
        color = CbtTextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp,
        color = CbtSagePrimary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = CbtTextPlaceholder
    )
)

val CbtShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun CbtTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CbtColorScheme,
        typography = CbtTypography,
        shapes = CbtShapes,
        content = content
    )
}