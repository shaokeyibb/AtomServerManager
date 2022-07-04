package io.hikarilan.atomservermanager.windows.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import io.hikarilan.atomservermanager.Settings
import io.hikarilan.atomservermanager.data.ServerInstances
import io.hikarilan.atomservermanager.i18n.asState
import io.hikarilan.atomservermanager.i18n.getLang
import io.hikarilan.atomservermanager.isMainWindowDisplayable
import io.hikarilan.atomservermanager.requestExitApplication
import io.hikarilan.atomservermanager.theme.MainTheme
import io.hikarilan.atomservermanager.utils.FilesUtils.isImage
import io.hikarilan.atomservermanager.windows.main.view.AddNewServerView.AddNewServerView
import io.hikarilan.atomservermanager.windows.main.view.MainView.MainView
import io.hikarilan.atomservermanager.windows.main.view.ServerInstanceView.ServerInstanceView
import io.hikarilan.atomservermanager.windows.main.view.SettingsView.SettingsView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object MainWindow {

    var currentView by ViewHandler.MAIN.asState()
        private set

    private val viewParameters = mutableStateMapOf<ViewHandler, Map<String, Any>>()

    @Composable
    fun ApplicationScope.MainWindow(
        isVisible: MutableState<Boolean>, isShowTray: MutableState<Boolean>, trayState: TrayState
    ) {
        val scope = rememberCoroutineScope()

        if (isMainWindowDisplayable.value) {
            Window(
                icon = BitmapPainter(useResource("icons/logo.png") { loadImageBitmap(it) }),
                onCloseRequest = ::requestExitApplication,
                state = rememberWindowState(
                    placement = WindowPlacement.Maximized, size = DpSize.Unspecified
                ),
                title = getLang("application.main_window.main_view.title").asString,
                undecorated = true,
                transparent = true,
                resizable = false,
                visible = isVisible.value,
            ) {
                MainTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (Settings.backgroundImageType == Settings.BackgroundImageType.IMAGE
                            && Settings.backgroundImage?.exists() == true
                            && Settings.backgroundImage!!.toPath().isImage()
                        ) {
                            if (Settings.backgroundImage?.extension == "svg") {
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = loadSvgPainter(
                                        Settings.backgroundImage!!.inputStream(),
                                        LocalDensity.current
                                    ),
                                    contentDescription = "Background image",
                                    contentScale = ContentScale.FillBounds
                                )
                            } else {
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    bitmap = loadImageBitmap(Settings.backgroundImage!!.inputStream()),
                                    contentDescription = "Background image",
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                        Crossfade(
                            targetState = currentView,
                            animationSpec = tween(
                                durationMillis = 500,
                                delayMillis = 0,
                                easing = FastOutLinearInEasing
                            )
                        ) { screen ->
                            when (screen) {
                                ViewHandler.MAIN -> MainView(isShowTray, scope, trayState)
                                ViewHandler.SETTINGS -> SettingsView(isShowTray, scope, trayState)
                                ViewHandler.ADD_NEW_SERVER -> AddNewServerView(isShowTray, scope, trayState)
                                ViewHandler.SERVER_INSTANCE -> ServerInstanceView(viewParameters[ViewHandler.SERVER_INSTANCE]!!["instance"] as ServerInstances.ServerInstance)
                            }
                        }
                    }
                }
            }
        }

    }

    @Composable
    fun MinimizeApplicationIconButton(
        isShowTray: MutableState<Boolean>, scope: CoroutineScope, trayState: TrayState
    ) {
        IconButton(onClick = {
            isShowTray.value = true
            scope.launch {
                val lang = getLang("application.tray.tooltip.notification.minimize_to_tray").asJsonObject
                delay(500)
                trayState.sendNotification(
                    Notification(
                        lang["title"].asString, lang["message"].asString, Notification.Type.Info
                    )
                )
            }
        }) {
            useResource("icons/minimize.svg") {
                Icon(
                    painter = loadSvgPainter(
                        inputStream = it, density = Density(0.15f)
                    ),
                    contentDescription = "Minimize Application",
                )
            }
        }
    }

    @Composable
    fun ApplicationScope.ExitApplicationIconButton() {
        IconButton(onClick = {
            requestExitApplication()
        }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Application",
            )
        }
    }

    enum class ViewHandler {
        MAIN, SETTINGS, ADD_NEW_SERVER, SERVER_INSTANCE;

        fun switchTo(view: ViewHandler, parameters: Map<String, Any> = emptyMap()) {
            if (view == this) return

            currentView = view

            viewParameters[view] = parameters
        }
    }

}