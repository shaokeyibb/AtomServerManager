package io.hikarilan.atomservermanager.windows.main.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.TrayState
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.hikarilan.atomservermanager.components.ResourceDashboard
import io.hikarilan.atomservermanager.data.ServerInstances
import io.hikarilan.atomservermanager.i18n.LocaleUtils
import io.hikarilan.atomservermanager.i18n.getLang
import io.hikarilan.atomservermanager.i18n.language
import io.hikarilan.atomservermanager.io.Client
import io.hikarilan.atomservermanager.windows.main.MainWindow
import io.hikarilan.atomservermanager.windows.main.MainWindow.ExitApplicationIconButton
import io.hikarilan.atomservermanager.windows.main.MainWindow.MinimizeApplicationIconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request


object MainView {

    @Composable
    fun ApplicationScope.MainView(isShowTray: MutableState<Boolean>, scope: CoroutineScope, trayState: TrayState) {
        Scaffold(
            topBar = {
                TopAppBar(isShowTray, scope, trayState)
            }, backgroundColor = Color.Transparent
        ) {
            Row {
                Column(
                    modifier = Modifier.fillMaxWidth(0.4f),
                    verticalArrangement = Arrangement.Top
                ) {
                    ResourceDashboard(
                        minWidth = 700.dp,
                        minHeight = 500.dp,
                        modifier = Modifier.fillMaxHeight(0.5f),
                        baseProgressIndicatorSize = 250.dp,
                    )
                    NewsCard()
                }
                ServerInstancePanel()
            }
        }
    }

    @Composable
    private fun ServerInstancePanel() {
        Surface(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxSize().padding(10.dp),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
            ) {
                items(ServerInstances.instances) {
                    InstanceCore(it)
                }
                item("Create new server") {
                    AddNewServerCard()
                }
                item("Import server") {
                    ImportServerCard()
                }
            }
        }
    }

    @Composable
    private fun InstanceCore(instance: ServerInstances.ServerInstance) {
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().clickable {
                MainWindow.currentView.switchTo(MainWindow.ViewHandler.SERVER_INSTANCE, mapOf("instance" to instance))
            },
        ) {
            Row(
                modifier = Modifier.sizeIn(minHeight = 200.dp)
            ) {
                instance.core.logo(Modifier.size(180.dp))
                Column {
                    Text(
                        text = instance.name,
                        style = MaterialTheme.typography.h4
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    private fun AddNewServerCard() {
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().clickable {
                MainWindow.currentView.switchTo(MainWindow.ViewHandler.ADD_NEW_SERVER)
            },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.sizeIn(minHeight = 200.dp)
            ) {
                Icon(
                    modifier = Modifier.size(180.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new server",
                )
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = getLang("application.main_window.main_view.create_new_server").asString,
                    textAlign = TextAlign.Center,
                    fontSize = TextUnit(2.0f, TextUnitType.Em)
                )
            }
        }
    }

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    private fun ImportServerCard() {
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().clickable {
                // TODO
            },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.sizeIn(minHeight = 200.dp)
            ) {
                Icon(
                    modifier = Modifier.size(180.dp),
                    imageVector = Icons.Default.Search,
                    contentDescription = "Import server",
                )
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = getLang("application.main_window.main_view.import_server").asString,
                    textAlign = TextAlign.Center,
                    fontSize = TextUnit(2.0f, TextUnitType.Em)
                )
            }
        }
    }

    @Composable
    private fun ApplicationScope.TopAppBar(
        isShowTray: MutableState<Boolean>,
        scope: CoroutineScope,
        trayState: TrayState
    ) {
        TopAppBar(
            title = {
                Text(getLang("application.main_window.main_view.title").asString)
            },
            actions = {
                IconButton(onClick = {
                    MainWindow.currentView.switchTo(MainWindow.ViewHandler.SETTINGS)
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                    )
                }
                MinimizeApplicationIconButton(isShowTray, scope, trayState)
                ExitApplicationIconButton()
            }
        )
    }

    @Composable
    private fun NewsCard(
        color: Color = MaterialTheme.colors.surface,
        modifier: Modifier = Modifier
    ) {
        Surface(
            color = color,
            modifier = modifier.padding(10.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            val rawNewsContent = remember { mutableStateOf(JsonObject()) }
            val isFinishLoading = remember { mutableStateOf(false) }
            LaunchedEffect("Loading news") {
                try {
                    rawNewsContent.value = Gson().fromJson(withContext(Dispatchers.IO) {
                        Client.okHttpClient
                            .newCall(
                                Request.Builder()
                                    .url("https://app.minecraft.kim/AtomServerManager/News.json")
                                    .get()
                                    .build()
                            )
                            .execute().body?.string()
                    }, JsonObject::class.java)
                } catch (e: Throwable) {
                    rawNewsContent.value = JsonObject().apply {
                        LocaleUtils.availableLocales.forEach {
                            addProperty(
                                it.key.toLanguageTag().replace('-', '_'),
                                it.value["application.main_window.main_view.news.error"].asString.replace(
                                    "{0}",
                                    e.stackTraceToString()
                                )
                            )
                        }
                    }
                } finally {
                    isFinishLoading.value = true
                }
            }
            Column(
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(Modifier.padding(2.dp))
                Text(
                    text = getLang("application.main_window.main_view.news.title").asString,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.padding(2.dp))
                Divider(Modifier.padding(horizontal = 5.dp))
                Spacer(Modifier.padding(2.dp))
                Text(
                    text = if (!isFinishLoading.value) {
                        remember { mutableStateOf(getLang("application.main_window.main_view.news.loading").asString) }.value
                    } else {
                        if (rawNewsContent.value[language.toLanguageTag().replace('-', '_')] != null) {
                            rawNewsContent.value[language.toLanguageTag().replace('-', '_')].asString
                        } else {
                            rawNewsContent.value[rawNewsContent.value["default"].asString].asString
                        }
                    },
                    modifier = Modifier.fillMaxHeight().padding(5.dp).verticalScroll(rememberScrollState(0))
                )
            }
        }
    }

}