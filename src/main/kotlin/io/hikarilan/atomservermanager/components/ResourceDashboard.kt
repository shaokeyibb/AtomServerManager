package io.hikarilan.atomservermanager.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.hikarilan.atomservermanager.i18n.getLang
import io.hikarilan.atomservermanager.platform.OperatingSystems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.nio.file.FileStore
import java.nio.file.FileSystems

@Composable
fun ResourceDashboard(
    color: Color = MaterialTheme.colors.surface,
    modifier: Modifier = Modifier,
    baseProgressIndicatorSize: Dp,
    minWidth: Dp = Dp.Unspecified,
    minHeight: Dp = Dp.Unspecified,
    maxWidth: Dp = Dp.Unspecified,
    maxHeight: Dp = Dp.Unspecified,
    softwareCheckDelay: Long = 1000,
    hardwareCheckDelay: Long = 10000
) {
    // For CPU usage, OperatingSystemMXBean.getSystemLoadAverage() / OperatingSystemMXBean.getAvailableProcessors() (load average per cpu)
    // For memory, OperatingSystemMXBean.getTotalPhysicalMemorySize() and OperatingSystemMXBean.getFreePhysicalMemorySize()
    // For disk space, File.getTotalSpace() and File.getUsableSpace()
    val cpuUsage = remember { mutableStateOf(0.0f) }
    val freeMemorySize = remember { mutableStateOf(0L) }
    val totalMemorySize = remember { mutableStateOf(0L) }
    val fileStores = remember { mutableSetOf<FileStore>() }
    LaunchedEffect("Usage software calc") {
        while (true) {
            cpuUsage.value = withContext(Dispatchers.IO) {
                OperatingSystems.operatingSystemMXBean.processCpuLoad.toFloat()
            }
            freeMemorySize.value = withContext(Dispatchers.IO) {
                OperatingSystems.operatingSystemMXBean.freeMemorySize
            }
            totalMemorySize.value = withContext(Dispatchers.IO) {
                OperatingSystems.operatingSystemMXBean.totalMemorySize
            }
            delay(softwareCheckDelay)
        }
    }
    LaunchedEffect("Usage hardware calc") {
        while (true) {
            withContext(Dispatchers.IO) {
                fileStores.addAll(FileSystems.getDefault().fileStores)
            }
            delay(hardwareCheckDelay)
        }
    }
    Surface(
        color = color,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.padding(10.dp).sizeIn(minWidth, minHeight, maxWidth, maxHeight)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                UsageCircularProgressIndicator(
                    color = MaterialTheme.colors.primaryVariant,
                    progress = cpuUsage.value,
                    progressIndicatorSize = baseProgressIndicatorSize,
                ) {
                    Text(
                        text = """
                            ${getLang("application.main_window.main_view.cpu_usage").asString}
                            ${String.format("%.2f", cpuUsage.value * 100)}%""".trimIndent(),
                        textAlign = TextAlign.Center
                    )
                }
                UsageCircularProgressIndicator(
                    color = MaterialTheme.colors.primaryVariant,
                    progress = 1 - freeMemorySize.value.toFloat() / totalMemorySize.value,
                    progressIndicatorSize = baseProgressIndicatorSize,
                ) {
                    Text(
                        text = """
                                ${getLang("application.main_window.main_view.memory_usage").asString}
                                ${freeMemorySize.value / 1024 / 1024}MB free of ${totalMemorySize.value / 1024 / 1024}MB
                            """.trimIndent(), textAlign = TextAlign.Center
                    )
                }
            }
            LazyRow(
                modifier = Modifier.sizeIn(minWidth = minWidth, maxWidth = maxWidth),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                items(fileStores.toList()) {
                    UsageCircularProgressIndicator(
                        color = MaterialTheme.colors.secondary,
                        progress = 1 - it.usableSpace.toFloat() / it.totalSpace,
                        progressIndicatorSize = baseProgressIndicatorSize * 0.8f,
                    ) {
                        Text(
                            text = """
                                        ${it.name()}
                                        ${getLang("application.main_window.main_view.disk_usage").asString}
                                        ${it.usableSpace / 1024 / 1024 / 1024}GB free of ${it.totalSpace / 1024 / 1024 / 1024}GB
                                    """.trimIndent(), textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}