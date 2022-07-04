package io.hikarilan.atomservermanager.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import io.hikarilan.atomservermanager.Settings

@Composable
fun MainTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = Themes.DefaultColors(),
        typography = Themes.MiSansTypography()
    ) {
        content()
    }
}

object Themes {

    @Composable
    fun DefaultColors() = lightColors(
        primary = Color(0xFFffa000),
        primaryVariant = Color(0xFFffd149),
        secondary = Color(0xFF64b5f6),
        secondaryVariant = Color(0xFF9be7ff),
        surface = Color(240, 255, 255, (255 * Settings.opacity).toInt()),
        error = Color(255, 33, 33),
    )

    @Composable
    fun MiSansTypography() =
        Typography(
            defaultFontFamily = FontFamily(
                Font(
                    resource = "/fonts/MiSans-Bold.ttf",
                    weight = FontWeight.Bold,
                    style = FontStyle.Normal
                ),
                Font(
                    resource = "/fonts/MiSans-Semibold.ttf",
                    weight = FontWeight.SemiBold,
                    style = FontStyle.Normal
                ),
                Font(
                    resource = "/fonts/MiSans-ExtraLight.ttf",
                    weight = FontWeight.ExtraLight,
                    style = FontStyle.Normal
                ),
                Font(
                    resource = "/fonts/MiSans-Light.ttf",
                    weight = FontWeight.Light,
                    style = FontStyle.Normal
                ),
                Font(
                    resource = "/fonts/MiSans-Medium.ttf",
                    weight = FontWeight.Medium,
                    style = FontStyle.Normal
                ),
                Font(
                    resource = "/fonts/MiSans-Normal.ttf",
                    weight = FontWeight.Normal,
                    style = FontStyle.Normal
                ),
                Font(
                    resource = "/fonts/MiSans-Thin.ttf",
                    weight = FontWeight.Thin,
                    style = FontStyle.Normal
                ),
            )
        )
}

