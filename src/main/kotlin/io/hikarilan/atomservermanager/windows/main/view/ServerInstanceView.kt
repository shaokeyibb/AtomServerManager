package io.hikarilan.atomservermanager.windows.main.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.NavigationRail
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import io.hikarilan.atomservermanager.data.ServerInstances

object ServerInstanceView {

    @Composable
    fun ApplicationScope.ServerInstanceView(instance: ServerInstances.ServerInstance) {
        NavigationRail(
            modifier = Modifier.fillMaxSize()
        ) {

        }
    }

}