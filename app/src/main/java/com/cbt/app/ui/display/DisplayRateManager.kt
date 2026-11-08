package com.cbt.app.ui.display

import android.view.Window

/**
 * Утилита активации повышенной частоты обновления экрана (до 120 Гц).
 * Обеспечивает максимально плавную прокрутку длинных КПТ-текстов,
 * снижая зрительную утомляемость при чтении и саморефлексии.
 */
object DisplayRateManager {

    /**
     * Задает предпочтительную частоту обновления дисплея для окна приложения.
     *
     * @param window Окно текущей Activity.
     * @param enable true — запрашивать 120 Гц, false — вернуть системное значение по умолчанию.
     */
    fun setHighRefreshRate(window: Window, enable: Boolean) {
        runCatching {
            val layoutParams = window.attributes
            layoutParams.preferredRefreshRate = if (enable) 120.0f else 0.0f
            layoutParams.preferredDisplayModeId = 0
            window.attributes = layoutParams
        }
    }
}