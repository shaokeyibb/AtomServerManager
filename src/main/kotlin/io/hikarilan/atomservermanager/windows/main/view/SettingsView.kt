package io.hikarilan.atomservermanager.windows.main.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.TrayState
import com.google.gson.JsonObject
import io.hikarilan.atomservermanager.Settings
import io.hikarilan.atomservermanager.i18n.LocaleUtils
import io.hikarilan.atomservermanager.i18n.getLang
import io.hikarilan.atomservermanager.i18n.language
import io.hikarilan.atomservermanager.i18n.setLocale
import io.hikarilan.atomservermanager.utils.FilesUtils.isImage
import io.hikarilan.atomservermanager.windows.main.MainWindow
import io.hikarilan.atomservermanager.windows.main.MainWindow.ExitApplicationIconButton
import io.hikarilan.atomservermanager.windows.main.MainWindow.MinimizeApplicationIconButton
import kotlinx.coroutines.CoroutineScope
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.Locale


object SettingsView {

    @Composable
    fun ApplicationScope.SettingsView(isShowTray: MutableState<Boolean>, scope: CoroutineScope, trayState: TrayState) {
        Scaffold(
            topBar = {
                TopAppBar(isShowTray, scope, trayState)
            },
            backgroundColor = Color.Transparent
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxSize().padding(10.dp),
            ) {
                Column {
                    val generalLang = getLang("application.main_window.settings_view.menus.general").asJsonObject
                    SettingsContent(buildMap {
                        put(generalLang["title"].asString, buildList {
                            add {
                                Proxy(generalLang["proxy"].asJsonObject)
                            }
                        })
                    })
                    val appearanceLang = getLang("application.main_window.settings_view.menus.appearance").asJsonObject
                    SettingsContent(buildMap {
                        put(appearanceLang["title"].asString, buildList {
                            add {
                                BackgroundImage(appearanceLang["background_image"].asJsonObject)
                            }
                            add {
                                Language(appearanceLang["language"].asJsonObject)
                            }
                            add {
                                Opacity(appearanceLang["opacity"].asJsonObject)
                            }
                        })
                    })
                    val downloadLang = getLang("application.main_window.settings_view.menus.download").asJsonObject
                    SettingsContent(buildMap {
                        put(downloadLang["title"].asString, buildList {
                            add {
                                DisableVerify(downloadLang["disable_verify"].asJsonObject)
                            }
                        })
                    })
                }
            }
        }
    }

    @Composable
    private fun SettingsEntry(
        title: @Composable () -> Unit,
        caption: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        isNextTimeApply: Boolean = false,
        content: @Composable RowScope.() -> Unit
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                title()
                caption()
                if (isNextTimeApply) {
                    NextTimeApplyText()
                }
            }
            content()
        }
    }

    @Composable
    private fun NextTimeApplyText() {
        Text(
            text = getLang("application.main_window.settings_view.next_time_apply").asString,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.error,
        )
    }

    @Composable
    private fun DisableVerify(langContext: JsonObject) {
        val title = langContext.getAsJsonArray("title")
        SettingsEntry(
            title = {
                Text(
                    text = title[0].asString,
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface
                )
            },
            caption = {
                Text(
                    text = title[1].asString,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                )
            }
        ) {
            Switch(
                checked = Settings.disableVerify,
                onCheckedChange = {
                    Settings.disableVerify = it
                }
            )
        }
    }

    private fun checkAndApplyProxy(type: Proxy.Type, host: String, port: String) {
        if (type == Proxy.Type.DIRECT) {
            Settings.proxy = null
            return
        }
        if (host.isEmpty() || port.toIntOrNull() !in 1..65535) return
        Settings.proxy = Proxy(type, InetSocketAddress(host, port.toInt()))
    }

    @Composable
    private fun Proxy(langContext: JsonObject) {
        val title = langContext.getAsJsonArray("title")
        SettingsEntry(
            title = {
                Text(
                    text = title[0].asString,
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface
                )
            },
            caption = {
                Text(
                    text = title[1].asString,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                )
            },
            isNextTimeApply = true
        ) {

            val proxyType = remember { mutableStateOf(Settings.proxy?.type() ?: Proxy.Type.DIRECT) }
            val host = remember { mutableStateOf((Settings.proxy?.address() as InetSocketAddress?)?.hostString ?: "") }
            val port =
                remember { mutableStateOf((Settings.proxy?.address() as InetSocketAddress?)?.port?.toString() ?: "") }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(0.25f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Proxy.Type.values().forEach {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = proxyType.value == it,
                                onClick = {
                                    proxyType.value = it
                                    checkAndApplyProxy(proxyType.value, host.value, port.value)
                                }
                            )
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                    }
                }
                if (proxyType.value != Proxy.Type.DIRECT) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLang("host.text").asString,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier.requiredWidth(50.dp)
                        )
                        OutlinedTextField(
                            value = host.value,
                            onValueChange = {
                                host.value = it
                                checkAndApplyProxy(proxyType.value, host.value, port.value)
                            },
                            enabled = proxyType.value != Proxy.Type.DIRECT,
                            isError = host.value.isEmpty(),
                            placeholder = {
                                Text(text = "127.0.0.1")
                            }
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLang("port.text").asString,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier.requiredWidth(50.dp)
                        )
                        OutlinedTextField(
                            value = port.value,
                            onValueChange = {
                                port.value = it
                                checkAndApplyProxy(proxyType.value, host.value, port.value)
                            },
                            enabled = proxyType.value != Proxy.Type.DIRECT,
                            isError = port.value.toIntOrNull() !in 1..65535,
                            placeholder = {
                                Text(text = "1080")
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Opacity(langContext: JsonObject) {
        val title = langContext.getAsJsonArray("title")
        SettingsEntry(
            title = {
                Text(
                    text = title[0].asString,
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface
                )
            },
            caption = {
                Text(
                    text = title[1].asString,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                )
            }
        ) {
            val tempValue = remember { mutableStateOf(Settings.opacity) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(0.2f),
            ) {
                Slider(
                    value = tempValue.value,
                    onValueChange = { tempValue.value = it },
                    onValueChangeFinished = {
                        Settings.opacity = tempValue.value
                    },
                    valueRange = 0.0f..1.0f,
                    steps = 0,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                Text(
                    text = String.format("%.2f", tempValue.value),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.requiredWidth(40.dp)
                )
            }
        }
    }

    @Composable
    private fun Language(langContext: JsonObject) {
        val title = langContext.getAsJsonArray("title")
        val textField = langContext.getAsJsonObject("text_field")
        SettingsEntry(
            title = {
                Text(
                    text = title[0].asString,
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface
                )
            },
            caption = {
                Text(
                    text = title[1].asString,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                )
            }
        ) {
            val languages = LocaleUtils.availableLocales.keys.associateWith {
                "${it.getDisplayLanguage(language)} (${it.getDisplayCountry(language)})"
            }.let { remember { mutableStateMapOf<Locale, String>().apply { putAll(it) } } }
            val selectedLanguage = remember { mutableStateOf<Locale?>(language) }
            val isDropdownMenuExpanded = remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(0.2f),
                    value = languages[selectedLanguage.value] ?: "",
                    onValueChange = {},
                    isError = selectedLanguage.value == null,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { isDropdownMenuExpanded.value = true },
                            modifier = Modifier.padding(horizontal = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown menu",
                                tint = if (isDropdownMenuExpanded.value) {
                                    Color.Black
                                } else {
                                    Color.Gray
                                },
                            )
                        }
                    },
                    placeholder = {
                        Text(
                            text = textField.getAsJsonArray("placeholder").asString,
                        )
                    }
                )
                DropdownMenu(
                    modifier = Modifier.fillMaxWidth(0.18f),
                    expanded = isDropdownMenuExpanded.value,
                    onDismissRequest = { isDropdownMenuExpanded.value = false },
                ) {
                    languages.forEach {
                        DropdownMenuItem(
                            onClick = {
                                selectedLanguage.value = it.key
                                setLocale(it.key)

                                languages.clear()
                                languages.putAll(LocaleUtils.availableLocales.keys.associateWith {
                                    "${it.getDisplayLanguage(language)} (${it.getDisplayCountry(language)})"
                                })
                                isDropdownMenuExpanded.value = false
                            },
                        ) {
                            Text(
                                text = it.value,
                                style = MaterialTheme.typography.body2,
                                color = if (it.key == selectedLanguage.value) Color.Black else Color.Gray,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BackgroundImage(langContext: JsonObject) {
        val title = langContext.getAsJsonArray("title")
        val buttons = langContext.getAsJsonObject("buttons")
        val errors = langContext.getAsJsonObject("errors")
        val imageType = remember { mutableStateOf(Settings.backgroundImageType) }
        SettingsEntry(
            title = {
                Text(
                    text = title[0].asString,
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface
                )
            },
            caption = {
                Text(
                    text = title[1].asString,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                )
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.7f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val image: MutableState<File?> = remember { mutableStateOf(Settings.backgroundImage) }
                    val error: MutableState<Boolean> = remember { mutableStateOf(false) }
                    RadioButton(
                        selected = imageType.value == Settings.BackgroundImageType.IMAGE,
                        onClick = {
                            var selected: File? = image.value

                            if (imageType.value == Settings.BackgroundImageType.IMAGE || selected == null) {

                                FileDialog(null as Frame?, "Select Image", FileDialog.LOAD).apply {
                                    this.setFilenameFilter { dir, name ->
                                        dir.toPath().resolve(name).isImage()
                                    }
                                    isVisible = true
                                }.let { if (it.file != null) selected = File(it.directory, it.file) }

                            }

                            if (selected == null) return@RadioButton

                            if (selected?.toPath()?.isImage() != true) {
                                error.value = true
                                return@RadioButton
                            }

                            error.value = false
                            image.value = selected!!
                            imageType.value = Settings.BackgroundImageType.IMAGE
                            Settings.backgroundImageType = Settings.BackgroundImageType.IMAGE
                            Settings.backgroundImage = image.value
                        },
                    )
                    Column {
                        Text(
                            text = buttons["image"].asString,
                            style = MaterialTheme.typography.subtitle1
                        )
                        if (error.value) {
                            Text(
                                text = errors["not_a_image"].asString,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.error,
                            )
                        } else if (image.value != null) {
                            Text(
                                text = image.value!!.absolutePath,
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = imageType.value == Settings.BackgroundImageType.TRANSPARENT,
                        onClick = {
                            imageType.value = Settings.BackgroundImageType.TRANSPARENT
                            Settings.backgroundImageType = Settings.BackgroundImageType.TRANSPARENT
                        },
                    )
                    Text(
                        text = buttons["transparent"].asString,
                        style = MaterialTheme.typography.subtitle1,
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingsContent(
        items: Map<String, List<@Composable ColumnScope.() -> Unit>> = mapOf()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
        ) {
            items(items.keys.toList()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.onSurface
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    items[it]!!.forEach { it() }
                }
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
                Text(getLang("application.main_window.settings_view.title").asString)
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        MainWindow.currentView.switchTo(MainWindow.ViewHandler.MAIN)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to main"
                    )
                }
            },
            actions = {
                MinimizeApplicationIconButton(isShowTray, scope, trayState)
                ExitApplicationIconButton()
            }
        )
    }
}