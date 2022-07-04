package io.hikarilan.atomservermanager.windows.main.view

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.TrayState
import io.hikarilan.atomservermanager.Settings
import io.hikarilan.atomservermanager.data.Progress
import io.hikarilan.atomservermanager.data.ServerInstances
import io.hikarilan.atomservermanager.i18n.getLang
import io.hikarilan.atomservermanager.platform.OperatingSystems
import io.hikarilan.atomservermanager.servers.*
import io.hikarilan.atomservermanager.utils.AikarsFlagsUtils
import io.hikarilan.atomservermanager.utils.CommandLineUtils
import io.hikarilan.atomservermanager.utils.jvmMemoryFlagToBytes
import io.hikarilan.atomservermanager.windows.main.MainWindow
import io.hikarilan.atomservermanager.windows.main.MainWindow.ExitApplicationIconButton
import kotlinx.coroutines.*
import org.jackhuang.hmcl.util.platform.Bits
import org.jackhuang.hmcl.util.platform.JavaVersion
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.pathString

object AddNewServerView {

    private val selectedLoaders = mutableStateListOf<Loaders.Loader>()

    private val selectedTypes = mutableStateListOf<Cores.Core.Type>()

    private var selectedCore: Cores.Core? by mutableStateOf(null)

    private var selectedDownloadProvider by mutableStateOf<DownloadProviders.DownloadProvider?>(null)

    private var selectedVersion by mutableStateOf<String?>(null)

    private var selectedBuild by mutableStateOf<String?>(null)

    private var selectedResourcePatcher by mutableStateOf<ResourcePatchers.ResourcePatcher?>(null)

    private val selectedAddons = mutableStateListOf<Addons.Addon>()

    private var instanceName by mutableStateOf<String>(
        getLang("application.main_window.add_new_server_view.instance_name.default").asString.replace(
            "{0}",
            ServerInstances.instances.size.toString()
        )
    )

    private var usedJavaCommandLine by mutableStateOf(
        CommandLineUtils.JavaCommandLine(
            javaPath = JavaVersion.CURRENT_JAVA.binary.pathString
        )
    )

    private val progress = mutableStateOf<List<Progress<*>>>(emptyList())

    @OptIn(ExperimentalFoundationApi::class)
    private val stages =
        mutableStateListOf<Pair<StageInfo, @Composable BoxScope.() -> Unit>>(StageInfo(shouldNext = { selectedCore != null }) to {

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
            ) {
                Text(
                    text = getLang("application.main_window.add_new_server_view.step_1.title").asString,
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.onSurface
                )
                Divider()
                LoaderFilter<Loaders.Mod.ModLoader>(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = getLang("application.main_window.add_new_server_view.step_1.filters.mod_loader").asString,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.fillMaxWidth(0.2f)
                    )
                }
                LoaderFilter<Loaders.Plugin.PluginLoader>(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = getLang("application.main_window.add_new_server_view.step_1.filters.plugin_loader").asString,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.fillMaxWidth(0.2f)
                    )
                }
                TypeFilter(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = getLang("application.main_window.add_new_server_view.step_1.filters.filter_type").asString,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.fillMaxWidth(0.2f)
                    )
                }
                Divider()

                val showingCores = Cores.allCores
                    .filter { it.supportLoaders.containsAll(selectedLoaders) && it.types.containsAll(selectedTypes) }

                if (showingCores.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getLang("application.main_window.add_new_server_view.step_1.filters.not_found").asString,
                            style = MaterialTheme.typography.h3
                        )
                    }
                }

                LazyVerticalGrid(
                    cells = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    items(showingCores) {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.requiredHeight(200.dp).clickable {
                                selectedCore = if (selectedCore != it) {
                                    it
                                } else {
                                    null
                                }
                                selectedDownloadProvider = null
                                selectedResourcePatcher = null
                                selectedAddons.clear()
                            },
                            backgroundColor = if (selectedCore == it) {
                                MaterialTheme.colors.secondaryVariant
                            } else {
                                MaterialTheme.colors.surface
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start)
                            ) {
                                Spacer(Modifier.requiredWidth(5.dp))
                                Box(
                                    modifier = Modifier.size(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    it.logo(
                                        modifier = Modifier.requiredSize(150.dp),
                                    )
                                }
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
                                ) {
                                    Row {
                                        Text(
                                            text = it.name,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.h4
                                        )
                                        if (it.upstream.isNotEmpty()) {
                                            TooltipArea(tooltip = {
                                                Surface {
                                                    Column {
                                                        Text(
                                                            text = getLang("application.main_window.add_new_server_view.step_1.known_upstreams").asString,
                                                            style = MaterialTheme.typography.h6
                                                        )
                                                        buildUpstreamsChain(it).forEach { inner ->
                                                            Text(
                                                                text = inner.joinToString(
                                                                    separator = " -> "
                                                                ) { str -> str.name },
                                                                style = MaterialTheme.typography.body1
                                                            )
                                                        }
                                                    }
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "More Information"
                                                )
                                            }
                                        }
                                    }
                                    if (it.supportLoaders.filterIsInstance<Loaders.Plugin.PluginLoader>()
                                            .isNotEmpty()
                                    ) {
                                        Text(text = "- " + getLang("application.main_window.add_new_server_view.step_1.supported_plugin_loader").asString + " ${
                                            it.supportLoaders.filterIsInstance<Loaders.Plugin.PluginLoader>()
                                                .joinToString(", ") { loader -> loader.name }
                                        }", color = MaterialTheme.colors.secondary)
                                    } else {
                                        Text(
                                            text = "- " + getLang("application.main_window.add_new_server_view.step_1.no_supported_plugin_loader").asString,
                                            color = MaterialTheme.colors.error
                                        )
                                    }
                                    if (it.supportLoaders.filterIsInstance<Loaders.Mod.ModLoader>().isNotEmpty()) {
                                        Text(text = "- " + getLang("application.main_window.add_new_server_view.step_1.supported_mod_loader").asString + " ${
                                            it.supportLoaders.filterIsInstance<Loaders.Mod.ModLoader>()
                                                .joinToString(", ") { loader -> loader.name }
                                        }", color = MaterialTheme.colors.secondary)
                                    } else {
                                        Text(
                                            text = "- " + getLang("application.main_window.add_new_server_view.step_1.no_supported_mod_loader").asString,
                                            color = MaterialTheme.colors.error
                                        )
                                    }
                                    Text(
                                        text = "- " + getLang("application.main_window.add_new_server_view.step_1.supported_provider").asString.replace(
                                            "{0}", it.downloadProviders.size.toString()
                                        ), color = MaterialTheme.colors.primary
                                    )
                                    if (it.resourcePatchers.isNotEmpty()) {
                                        Text(
                                            text = "- " + getLang("application.main_window.add_new_server_view.step_1.support_patcher").asString,
                                            color = MaterialTheme.colors.secondary
                                        )
                                    } else {
                                        Text(
                                            text = "- " + getLang("application.main_window.add_new_server_view.step_1.not_support_patcher").asString,
                                            color = MaterialTheme.colors.error
                                        )
                                    }
                                    if (it.addons.isNotEmpty()) {
                                        Text(text = "- " + getLang("application.main_window.add_new_server_view.step_1.supported_addons").asString + " ${
                                            it.addons.joinToString(", ") { addon -> addon.name }
                                        }", color = Color.Magenta)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, StageInfo(
            shouldNext = { selectedDownloadProvider != null }
        ) to {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            ) {
                Text(
                    text = getLang("application.main_window.add_new_server_view.step_2.title").asString.replace(
                        "{0}",
                        selectedCore?.name ?: ""
                    ),
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.onSurface
                )
                Divider()
                LazyVerticalGrid(
                    cells = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    items(selectedCore?.downloadProviders ?: listOf()) {

                        val isAvailable = remember { mutableStateOf<Boolean?>(null) }

                        LaunchedEffect(it) {
                            if (selectedCore == null) return@LaunchedEffect
                            withContext(Dispatchers.IO) {
                                isAvailable.value = it.available(selectedCore!!)
                            }
                        }

                        Card(
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.clickable {
                                if (isAvailable.value != true) return@clickable
                                selectedDownloadProvider = if (selectedDownloadProvider != it) {
                                    it
                                } else {
                                    null
                                }

                                selectedVersion = null
                            },
                            backgroundColor = if (selectedDownloadProvider == it) {
                                MaterialTheme.colors.secondaryVariant
                            } else {
                                MaterialTheme.colors.surface
                            }
                        ) {
                            it.banner(modifier = Modifier.requiredHeight(300.dp))
                            when (isAvailable.value) {
                                null -> {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                true -> {
                                    LinearProgressIndicator(
                                        progress = 1.0f,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.Green
                                    )
                                }
                                false -> {
                                    LinearProgressIndicator(
                                        progress = 1.0f,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }, StageInfo(
            shouldNext = {
                (selectedDownloadProvider?.getBuilds(
                    selectedCore ?: return@StageInfo false,
                    selectedVersion ?: return@StageInfo false
                ) ?: return@StageInfo false).let {
                    (it.isSuccess && it.getOrNull() == null)
                            || selectedBuild != null
                }
            },
        ) to {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            ) {

                val versions: MutableState<Result<List<String>?>?> = remember { mutableStateOf(null) }

                val builds: MutableState<Result<List<String>?>?> = remember { mutableStateOf(null) }

                val details: MutableState<Result<Map<String, String>?>?> = remember { mutableStateOf(null) }

                LaunchedEffect(selectedDownloadProvider) {
                    if (selectedDownloadProvider == null || selectedCore == null) return@LaunchedEffect
                    withContext(Dispatchers.IO) {
                        versions.value = selectedDownloadProvider!!.getVersions(selectedCore!!)
                        selectedVersion = versions.value?.getOrNull()?.firstOrNull()
                    }
                }

                LaunchedEffect(selectedDownloadProvider, selectedVersion) {
                    if (selectedDownloadProvider == null || selectedCore == null || selectedVersion == null) return@LaunchedEffect
                    selectedBuild = null
                    builds.value = null
                    withContext(Dispatchers.IO) {
                        builds.value = selectedDownloadProvider!!.getBuilds(selectedCore!!, selectedVersion!!)
                        selectedBuild = builds.value?.getOrNull()?.firstOrNull()
                    }
                }

                LaunchedEffect(selectedDownloadProvider, selectedVersion, selectedBuild) {
                    details.value = null
                    if (selectedDownloadProvider == null || selectedCore == null) return@LaunchedEffect
                    withContext(Dispatchers.IO) {
                        details.value =
                            selectedDownloadProvider!!.getDetails(selectedCore!!, selectedVersion, selectedBuild)
                    }
                }

                Text(
                    text = getLang("application.main_window.add_new_server_view.step_3.title").asString.replace(
                        "{0}",
                        selectedCore?.name ?: ""
                    ),
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.onSurface
                )

                Divider()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
                ) {
                    TextedSelector(
                        title = getLang("application.main_window.add_new_server_view.step_3.select_version").asString,
                        selected = selectedVersion,
                        onSelected = { selectedVersion = it },
                        elements = versions,
                    )

                    TextedSelector(
                        title = getLang("application.main_window.add_new_server_view.step_3.select_build").asString,
                        selected = selectedBuild,
                        onSelected = { selectedBuild = it },
                        elements = builds,
                    )

                    Surface(
                        color = MaterialTheme.colors.primaryVariant,
                        modifier = Modifier.fillMaxWidth().padding(10.dp)
                    ) {
                        DetailsPanel(
                            details = details,
                        )
                    }
                }
            }
        }, StageInfo(
            autoSkip = { selectedCore?.resourcePatchers?.isEmpty() != false && selectedCore?.addons?.isEmpty() != false },
            shouldNext = { ServerInstances.instances.none { it.name == instanceName } }
        ) to {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            ) {

                Text(
                    text = getLang("application.main_window.add_new_server_view.step_4.title").asString.replace(
                        "{0}",
                        selectedCore?.name ?: ""
                    ),
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.onSurface
                )

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        text = getLang("application.main_window.add_new_server_view.step_4.specify_instance_name").asString,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        value = instanceName,
                        onValueChange = {
                            instanceName = it
                        },
                        isError = ServerInstances.instances.any { it.name == instanceName }
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        text = getLang("application.main_window.add_new_server_view.step_4.select_resource_patcher").asString,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )

                    Box(
                        Modifier.fillMaxWidth(0.25f)
                    ) {

                        val expanded = remember { mutableStateOf(false) }

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = selectedResourcePatcher?.name ?: getLang("none.text").asString,
                            onValueChange = {},
                            readOnly = true,
                            enabled = selectedCore?.resourcePatchers?.isNotEmpty() == true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { expanded.value = true },
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Dropdown menu",
                                        tint = if (expanded.value) {
                                            Color.Black
                                        } else {
                                            Color.Gray
                                        },
                                    )
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expanded.value,
                            onDismissRequest = {
                                expanded.value = false
                            },
                            modifier = Modifier.sizeIn(maxHeight = 300.dp)
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    selectedResourcePatcher = null
                                    expanded.value = false
                                }
                            ) {
                                Text(
                                    text = getLang("none.text").asString,
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                            selectedCore?.resourcePatchers?.forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        selectedResourcePatcher = it
                                        expanded.value = false
                                    }
                                ) {
                                    Text(
                                        text = it.name,
                                        style = MaterialTheme.typography.body1,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        text = getLang("application.main_window.add_new_server_view.step_4.select_addons").asString,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )

                    Box(
                        Modifier.fillMaxWidth(0.25f)
                    ) {

                        val expanded = remember { mutableStateOf(false) }

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = selectedAddons.joinToString().takeIf { it.isNotEmpty() }
                                ?: getLang("none.text").asString,
                            onValueChange = {},
                            readOnly = true,
                            enabled = selectedCore?.addons?.isNotEmpty() == true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { expanded.value = true },
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Dropdown menu",
                                        tint = if (expanded.value) {
                                            Color.Black
                                        } else {
                                            Color.Gray
                                        },
                                    )
                                }
                            },
                        )
                        DropdownMenu(
                            expanded = expanded.value,
                            onDismissRequest = {
                                expanded.value = false
                            },
                            modifier = Modifier.sizeIn(maxHeight = 300.dp)
                        ) {
                            selectedCore?.addons?.forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        if (it in selectedAddons) {
                                            selectedAddons.remove(it)
                                        } else {
                                            selectedAddons.add(it)
                                        }
                                    }
                                ) {
                                    Row {
                                        Checkbox(
                                            checked = it in selectedAddons,
                                            onCheckedChange = null,
                                        )
                                        Text(
                                            text = it.name,
                                            style = MaterialTheme.typography.body1,
                                            color = MaterialTheme.colors.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }, StageInfo() to {
            Column(
                modifier = Modifier.fillMaxSize().padding(5.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Done",
                    modifier = Modifier.size(300.dp),
                    tint = Color.Green
                )
                getLang("application.main_window.add_new_server_view.step_5.finish").asJsonArray.forEach {
                    Text(
                        text = it.asString.replace("{0}", getLang("next.text").asString),
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        }, StageInfo(
            shouldPrevious = { false },
            shouldNext = { progress.value.isEmpty() },
        ) to {

            data class ProgressMeta(
                val progress: Float?,
                val isFinished: Boolean,
                val isChecked: Result<Boolean?>,
            )

            val meta = remember {
                mutableStateOf(progress.value.map { ProgressMeta(it.progress, it.isFinished(), it.checked) })
            }

            LaunchedEffect(selectedCore, selectedDownloadProvider, selectedResourcePatcher, selectedAddons) {

                if (selectedCore == null || selectedDownloadProvider == null) return@LaunchedEffect

                progress.value = listOfNotNull(
                    selectedDownloadProvider?.download(
                        Paths.get(instanceName),
                        selectedCore!!,
                        selectedVersion,
                        selectedBuild
                    ) {
                        usedJavaCommandLine = usedJavaCommandLine.copy(jarPath = it.result.getOrNull().toString())
                    },
                    selectedResourcePatcher?.execute(Paths.get(instanceName), selectedVersion, selectedBuild)
                ).onEach { withContext(Dispatchers.IO) { it.start() } }

                withContext(Dispatchers.IO) {
                    while (true) {
                        meta.value = progress.value.map { ProgressMeta(it.progress, it.isFinished(), it.checked) }
                        progress.value =
                            progress.value.filter {
                                !it.isFinished()
                                        || it.checked.isFailure
                                        || (!Settings.disableVerify && (it.checked.isSuccess && it.checked.getOrThrow() == false))
                            }
                    }
                }
            }

            if (progress.value.all { it.isFinished() }) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getLang("application.main_window.add_new_server_view.step_6.finish").asString.replace(
                            "{0}",
                            getLang("next.text").asString
                        ),
                        style = MaterialTheme.typography.h3
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Text(
                            text = getLang("application.main_window.add_new_server_view.step_6.download_progress").asString,
                            style = MaterialTheme.typography.h3
                        )
                        Spacer(Modifier.requiredHeightIn(200.dp))
                    }
                    items(progress.value) {
                        val m = meta.value[progress.value.indexOf(it)]
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 100.dp),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.h5,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (m.progress == null) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(0.7f),
                                        color = if (m.isChecked.isSuccess && m.isChecked.getOrNull() == false) {
                                            Color.Red
                                        } else if (m.isFinished) {
                                            Color.Green
                                        } else {
                                            MaterialTheme.colors.primary
                                        }
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(0.7f),
                                        progress = animateFloatAsState(
                                            targetValue = m.progress,
                                            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                                        ).value,
                                        color = if (m.isChecked.isSuccess && m.isChecked.getOrNull() == false) {
                                            Color.Red
                                        } else if (m.isFinished) {
                                            Color.Green
                                        } else {
                                            MaterialTheme.colors.primary
                                        }
                                    )
                                }
                                Text(
                                    text = if (m.isChecked.isSuccess && m.isChecked.getOrNull() == false) {
                                        getLang("application.main_window.add_new_server_view.step_6.verify_failed").asString
                                    } else if (m.isFinished) {
                                        getLang("finish.text").asString
                                    } else {
                                        "%.2f".format(m.progress?.times(100) ?: 0.0f) + "%"
                                    },
                                    color = if (m.isChecked.isSuccess && m.isChecked.getOrNull() == false) {
                                        Color.Red
                                    } else if (m.isFinished) {
                                        Color.Green
                                    } else {
                                        MaterialTheme.colors.onSurface
                                    },
                                    style = MaterialTheme.typography.h6,
                                )
                            }
                        }
                    }
                }
            }
        }, StageInfo(
            shouldPrevious = { false },
        ) to {

            val isUseAikarsFlag = remember { mutableStateOf(false) }

            val aikarsFlag = remember { mutableStateOf<String?>(null) }

            LaunchedEffect(null) {

                withContext(Dispatchers.IO) {
                    (OperatingSystems.operatingSystemMXBean.freeMemorySize / 1024 / 1024 * 0.85).toLong()
                }.let {
                    usedJavaCommandLine.minMemory = it.toString() + "M"
                    usedJavaCommandLine.maxMemory = it.toString() + "M"
                }

                aikarsFlag.value = withContext(Dispatchers.Default) {
                    AikarsFlagsUtils.findAikarsFlagFromAikarsWebsite()
                }

            }

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            ) {

                Text(
                    text = getLang("application.main_window.add_new_server_view.step_7.title").asString.replace(
                        "{0}",
                        instanceName
                    ),
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.onSurface
                )

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {
                        Text(
                            modifier = Modifier.fillMaxWidth(0.4f),
                            text = getLang("application.main_window.add_new_server_view.step_7.preview_java_command_line").asString,
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = getLang("application.main_window.add_new_server_view.step_7.do_use_aikars_flag").asString,
                                style = MaterialTheme.typography.subtitle2,
                                color = MaterialTheme.colors.onSurface
                            )

                            if (aikarsFlag.value != null) {
                                Switch(
                                    checked = isUseAikarsFlag.value,
                                    onCheckedChange = {
                                        isUseAikarsFlag.value = it
                                        if (it) {
                                            usedJavaCommandLine = CommandLineUtils.JavaCommandLine(aikarsFlag.value!!)
                                                .copy(
                                                    javaPath = usedJavaCommandLine.javaPath,
                                                    jarPath = usedJavaCommandLine.jarPath,
                                                )
                                        }
                                    }
                                )
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    OutlinedTextField(
                        value = usedJavaCommandLine.buildCommandLine(),
                        onValueChange = {},
                        enabled = false,
                        readOnly = true
                    )

                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        text = getLang("application.main_window.add_new_server_view.step_7.select_java").asString,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )

                    Box(
                        Modifier.fillMaxWidth(0.4f)
                    ) {

                        val expanded = remember { mutableStateOf(false) }

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = usedJavaCommandLine.javaPath.takeIf { it.isNotEmpty() }
                                ?: getLang("none.text").asString,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { expanded.value = true },
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Dropdown menu",
                                        tint = if (expanded.value) {
                                            Color.Black
                                        } else {
                                            Color.Gray
                                        },
                                    )
                                }
                            },
                            isError = usedJavaCommandLine.javaPath.isEmpty()
                        )
                        DropdownMenu(
                            expanded = expanded.value,
                            onDismissRequest = {
                                expanded.value = false
                            },
                            modifier = Modifier.sizeIn(maxHeight = 300.dp)
                        ) {
                            JavaVersion.getJavas().forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        usedJavaCommandLine = usedJavaCommandLine.copy(javaPath = it.binary.pathString)
                                        expanded.value = false
                                    }
                                ) {
                                    Row {
                                        Text(
                                            text = "${it.version}(${it.bits.bit} ${getLang("bit.text").asString})",
                                            style = MaterialTheme.typography.body1,
                                            color = MaterialTheme.colors.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        text = getLang("application.main_window.add_new_server_view.step_7.set_min_memory").asString,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )

                    OutlinedTextField(
                        value = usedJavaCommandLine.minMemory,
                        onValueChange = {
                            usedJavaCommandLine = usedJavaCommandLine.copy(minMemory = it)
                        },
                        isError = !usedJavaCommandLine.minMemory.matches("\\d+?[gGmM]".toRegex()) || usedJavaCommandLine.minMemory.jvmMemoryFlagToBytes() > usedJavaCommandLine.maxMemory.jvmMemoryFlagToBytes(),
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        text = getLang("application.main_window.add_new_server_view.step_7.set_max_memory").asString,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )

                    OutlinedTextField(
                        value = usedJavaCommandLine.maxMemory,
                        onValueChange = {
                            if (it.jvmMemoryFlagToBytes() >= 12 * 1024 * 1024 * 1024L && isUseAikarsFlag.value) {
                                usedJavaCommandLine = usedJavaCommandLine
                                    .copy(
                                        jvmOptions = AikarsFlagsUtils.modifyJVMFlagWhenMemoryGreaterThan12G(
                                            usedJavaCommandLine.jvmOptions
                                        )
                                    )
                            } else if (it.jvmMemoryFlagToBytes() < 12 * 1024 * 1024 * 1024L && isUseAikarsFlag.value) {
                                usedJavaCommandLine = usedJavaCommandLine
                                    .copy(
                                        jvmOptions = CommandLineUtils.JavaCommandLine(aikarsFlag.value!!).jvmOptions
                                    )
                            }
                            usedJavaCommandLine = usedJavaCommandLine.copy(maxMemory = it)
                        },
                        isError = !usedJavaCommandLine.maxMemory.matches("\\d+?[gGmM]".toRegex())
                                || usedJavaCommandLine.minMemory.jvmMemoryFlagToBytes() > usedJavaCommandLine.maxMemory.jvmMemoryFlagToBytes()
                                || JavaVersion.fromExecutable(Path(usedJavaCommandLine.javaPath))
                            .let { it.bits == Bits.BIT_32 && usedJavaCommandLine.maxMemory.jvmMemoryFlagToBytes() > 1.5 * 1024 * 1024 }
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        text = getLang("application.main_window.add_new_server_view.step_7.jvm_options").asString,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )

                    OutlinedTextField(
                        value = usedJavaCommandLine.jvmOptions,
                        onValueChange = {
                            usedJavaCommandLine = usedJavaCommandLine.copy(jvmOptions = it)
                        },
                        enabled = !isUseAikarsFlag.value
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        text = getLang("application.main_window.add_new_server_view.step_7.application_options").asString,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )

                    OutlinedTextField(
                        value = usedJavaCommandLine.applicationOptions,
                        onValueChange = {
                            usedJavaCommandLine = usedJavaCommandLine.copy(applicationOptions = it)
                        },
                        enabled = !isUseAikarsFlag.value
                    )

                }

            }

        })

    @Composable
    private fun DetailsPanel(
        details: MutableState<Result<Map<String, String>?>?>,
        modifier: Modifier = Modifier
    ) {
        val detailsLang = getLang("application.main_window.add_new_server_view.step_3.details").asJsonObject
        if (details.value?.isSuccess != true || details.value?.getOrNull() != null) {
            Surface(
                color = MaterialTheme.colors.primaryVariant,
                modifier = modifier,
                shape = RoundedCornerShape(10.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp)
                ) {
                    Text(
                        text = detailsLang["title"].asString,
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.onSurface
                    )
                    if (details.value == null) {
                        Text(
                            text = detailsLang["loading"].asString,
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface
                        )
                    } else if (details.value?.isFailure == true) {
                        Text(
                            text = detailsLang["error"].asString,
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.error
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 7.dp)
                        ) {
                            items(details.value?.getOrNull()?.entries?.toList() ?: emptyList()) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(0.1f).wrapContentHeight(),
                                            text = it.key,
                                            style = MaterialTheme.typography.h6,
                                            color = MaterialTheme.colors.onSurface
                                        )
                                        Text(
                                            text = it.value.trimEnd('\n'),
                                            style = MaterialTheme.typography.body1,
                                            color = MaterialTheme.colors.onSurface
                                        )
                                    }
                                    Divider(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TextedSelector(
        title: String,
        selected: String? = null,
        onSelected: (String) -> Unit = {},
        elements: MutableState<Result<List<String>?>?>,
        modifier: Modifier = Modifier
    ) {

        if (elements.value?.isSuccess != true || elements.value?.getOrNull() != null) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    modifier = modifier.fillMaxWidth(0.4f),
                    text = title,
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface
                )

                Box(
                    modifier.fillMaxWidth(0.25f)
                ) {

                    val expanded = remember { mutableStateOf(false) }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = selected ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            if (elements.value == null) {
                                CircularProgressIndicator()
                            } else {
                                IconButton(
                                    onClick = { expanded.value = true },
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Dropdown menu",
                                        tint = if (expanded.value) {
                                            Color.Black
                                        } else {
                                            Color.Gray
                                        },
                                    )
                                }
                            }
                        },
                        leadingIcon = if (elements.value?.isFailure == true) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Error",
                                    tint = Color.Red,
                                )
                            }
                        } else null,
                        enabled = elements.value?.getOrNull()?.isNotEmpty() == true,
                    )
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = {
                            expanded.value = false
                        },
                        modifier = Modifier.sizeIn(maxHeight = 300.dp)
                    ) {
                        elements.value?.getOrNull()?.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    onSelected(it)
                                    expanded.value = false
                                }
                            ) {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private var currentStep by mutableStateOf(0)

    private fun onFinished() {
        ServerInstances.instances.add(
            ServerInstances.ServerInstance(
                core = selectedCore!!,
                version = selectedVersion!!,
                build = selectedBuild,
                addons = selectedAddons.toList(),
                name = instanceName,
                commandLine = usedJavaCommandLine.buildCommandLine()
            )
        )
    }

    private fun backToMain() {
        MainWindow.currentView.switchTo(MainWindow.ViewHandler.MAIN)
        currentStep = 0
        instanceName = getLang("application.main_window.add_new_server_view.instance_name.default").asString
        selectedAddons.clear()
        selectedResourcePatcher = null
        selectedDownloadProvider = null
        selectedVersion = null
        selectedCore = null
        selectedLoaders.clear()
        selectedTypes.clear()
    }

    private fun nextStep() {
        if (currentStep >= stages.size - 1) return
        if (stages[++currentStep].first.autoSkip()) {
            nextStep()
        }
    }

    private fun previousStep() {
        if (currentStep <= 0) return
        if (stages[--currentStep].first.autoSkip()) {
            previousStep()
        }
    }

    data class StageInfo(
        val canSkip: () -> Boolean = { false },
        val shouldPrevious: () -> Boolean = { true },
        val shouldNext: () -> Boolean = { true },
        val autoSkip: () -> Boolean = { false },
    )

    @Composable
    fun ApplicationScope.AddNewServerView(
        isShowTray: MutableState<Boolean>, scope: CoroutineScope, trayState: TrayState
    ) {

        val currentStage = stages[currentStep]

        Scaffold(
            topBar = {
                TopAppBar(isShowTray, scope, trayState)
            }, backgroundColor = Color.Transparent
        ) {
            val animatedProgress = animateFloatAsState(
                targetValue = (currentStep + 1) / stages.size.toFloat(),
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            ).value
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.secondaryVariant,
                progress = animatedProgress
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxSize().padding(10.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
                            .padding(horizontal = 20.dp, vertical = 15.dp)
                    ) {
                        Crossfade(
                            targetState = currentStage.second,
                            animationSpec = tween(
                                durationMillis = 200,
                                delayMillis = 0,
                                easing = FastOutLinearInEasing
                            )
                        ) {
                            it()
                        }
                    }
                    Divider(
                        modifier = Modifier.fillMaxWidth().padding(10.dp), thickness = 2.dp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    ) {
                        if (currentStage.first.canSkip()) {
                            Button(
                                onClick = {
                                    nextStep()
                                }, modifier = Modifier.requiredWidth(100.dp)
                            ) {
                                Text(
                                    text = getLang("skip.text").asString,
                                )
                            }
                        }
                        Button(
                            onClick = {
                                previousStep()
                            },
                            enabled = currentStep > 0 && currentStage.first.shouldPrevious(),
                            modifier = Modifier.requiredWidth(100.dp)
                        ) {
                            Text(
                                text = getLang("previous.text").asString,
                            )
                        }
                        if (currentStep < stages.size - 1) {
                            Button(
                                onClick = {
                                    nextStep()
                                },
                                modifier = Modifier.requiredWidth(100.dp),
                                enabled = currentStage.first.shouldNext()
                            ) {
                                Text(
                                    text = getLang("next.text").asString,
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    onFinished()
                                    backToMain()
                                },
                                modifier = Modifier.requiredWidth(100.dp),
                                enabled = currentStage.first.shouldNext()
                            ) {
                                Text(
                                    text = getLang("finish.text").asString,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ApplicationScope.TopAppBar(
        isShowTray: MutableState<Boolean>, scope: CoroutineScope, trayState: TrayState
    ) {
        TopAppBar(title = {
            Text(getLang("application.main_window.add_new_server_view.title").asString)
        }, navigationIcon = {
            IconButton(onClick = {
                backToMain()
            }) {
                Icon(
                    imageVector = Icons.Default.Home, contentDescription = "Back to home"
                )
            }
        }, actions = {
            MainWindow.MinimizeApplicationIconButton(isShowTray, scope, trayState)
            ExitApplicationIconButton()
        })
    }

    @Composable
    private inline fun TypeFilter(
        modifier: Modifier = Modifier,
        title: @Composable RowScope.() -> Unit
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            title()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start)
            ) {
                Cores.Core.Type.values().forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.size(height = Dp.Unspecified, width = 200.dp)
                    ) {
                        Checkbox(checked = it in selectedTypes, onCheckedChange = { checked ->
                            if (checked) {
                                selectedTypes.add(it)
                            } else {
                                selectedTypes.remove(it)
                            }
                        })
                        Text(
                            text = it.friendlyName(),
                            style = MaterialTheme.typography.body1,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private inline fun <reified R : Loaders.Loader> LoaderFilter(
        modifier: Modifier = Modifier,
        title: @Composable RowScope.() -> Unit
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            title()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start)
            ) {
                Loaders.allLoaders.filterIsInstance<R>().forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.size(height = Dp.Unspecified, width = 200.dp)
                    ) {
                        Checkbox(checked = it in selectedLoaders, onCheckedChange = { checked ->
                            if (checked) {
                                selectedLoaders.add(it)
                            } else {
                                selectedLoaders.remove(it)
                            }
                        })
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.body1,
                        )
                    }
                }
            }
        }
    }
}