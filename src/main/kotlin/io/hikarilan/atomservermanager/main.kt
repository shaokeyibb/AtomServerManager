package io.hikarilan.atomservermanager

import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.*
import io.hikarilan.atomservermanager.data.ServerInstances
import io.hikarilan.atomservermanager.i18n.getLang
import io.hikarilan.atomservermanager.windows.main.MainWindow.MainWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jackhuang.hmcl.util.platform.JavaVersion

val isMainWindowDisplayable = mutableStateOf(true)

fun main() = application {

    LaunchedEffect("Initial") {
        withContext(Dispatchers.IO) {
            init()
        }
    }

    val isMainWindowVisible = remember { mutableStateOf(true) }

    val isShowTray = remember { mutableStateOf(false) }

    val trayState = rememberTrayState()

    MainTray(isMainWindowVisible, isShowTray, trayState)

    MainWindow(isMainWindowVisible, isShowTray, trayState)
}

@Composable
fun ApplicationScope.MainTray(
    isMainWindowVisible: MutableState<Boolean>,
    isShowTray: MutableState<Boolean>,
    trayState: TrayState
) {
    if (isShowTray.value) {
        Tray(
            icon = BitmapPainter(useResource("icons/logo.png") { loadImageBitmap(it) }),
            state = trayState,
            tooltip = getLang("application.tray.tooltip.title").asString,
            onAction = {
                isShowTray.value = false
                isMainWindowVisible.value = true
            }
        )
        isMainWindowVisible.value = false
    }
}

private fun init() {
    JavaVersion.initialize()
}

fun ApplicationScope.requestExitApplication() {
    Settings.save()
    ServerInstances.save()
    exitApplication()
}
