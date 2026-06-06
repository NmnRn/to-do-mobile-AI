package com.odak.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Per-app language override. The user picks "system", "tr" or "en" in Settings;
 * we wrap every entry-point context (Activity, Application, Service) so all
 * resources — including notifications — follow the chosen language.
 */
object LocaleManager {
    private const val PREFS = "odak_prefs"
    private const val KEY = "app_lang"
    const val SYSTEM = "system"

    /** Selectable languages; "system" follows the device locale. */
    val OPTIONS = listOf(SYSTEM, "tr", "en")

    fun language(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, SYSTEM) ?: SYSTEM

    fun setLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, lang).apply()
    }

    /** Returns a context configured with the chosen locale (or [base] for system). */
    fun wrap(base: Context): Context {
        val lang = language(base)
        if (lang == SYSTEM) return base
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
