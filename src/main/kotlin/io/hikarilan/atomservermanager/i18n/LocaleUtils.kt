package io.hikarilan.atomservermanager.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.useResource
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.hikarilan.atomservermanager.Settings
import io.hikarilan.atomservermanager.io.openResourceOrNull
import java.util.*

val language: Locale
    get() = LocaleUtils.locale.first

fun getLang(element: String): JsonElement =
    LocaleUtils.locale.second[element]
        ?: JsonPrimitive("Missing translation $element for ${LocaleUtils.locale.first}")

fun setLocale(locale: Locale) {
    Settings.locale = locale.toLanguageTag().replace('-', '_')
    LocaleUtils.locale = locale to LocaleUtils.availableLocales[locale]!!
    Locale.setDefault(locale)
}

inline fun <reified T> T.asState() = mutableStateOf(this)

object LocaleUtils {

    val availableLocales = Locale.getAvailableLocales().associateWith {
        openResourceOrNull("locales/${it.toLanguageTag().replace('-', '_')}.json")
    }.filter { it.value != null }
        .mapValues { it.value!!.use { input -> Gson().fromJson(input.reader(), JsonObject::class.java) } }

    private val defaultLocale = Locale.SIMPLIFIED_CHINESE to availableLocales[Locale.SIMPLIFIED_CHINESE]!!

    internal var locale by mutableStateOf(Locale.forLanguageTag(
        Settings.locale.replace(
            '_', '-'
        )
    ).takeIf { it in availableLocales.keys }?.let { it to availableLocales[it]!! } ?: defaultLocale)
}