package io.hikarilan.atomservermanager.utils

import io.hikarilan.atomservermanager.i18n.language
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor

fun TemporalAccessor.format(): String {
    return DateTimeFormatter.ofLocalizedDateTime(
        FormatStyle.LONG,
        FormatStyle.FULL
    ).withLocale(language)
        .withZone(ZoneId.systemDefault())
        .format(this)
}