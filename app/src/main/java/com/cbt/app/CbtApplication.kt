package com.cbt.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Точка входа в процесс приложения с поддержкой Dagger Hilt
 * и безопасным логированием в режиме отладки.
 */
@HiltAndroidApp
class CbtApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Включаем логирование только в Debug-сборке для сохранения приватности мыслей пользователя
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}