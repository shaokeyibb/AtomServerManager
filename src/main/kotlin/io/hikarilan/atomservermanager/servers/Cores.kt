package io.hikarilan.atomservermanager.servers

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.hikarilan.atomservermanager.components.NetworkImage

object Cores {

    val allCores =
        listOf(Vanilla, CraftBukkit, Spigot, Paper, SpongeVanilla, SpongeForge, BungeeCord, Waterfall, Velocity)


    sealed interface Core {

        val name: String

        val types: List<Type>

        val supportLoaders: List<Loaders.Loader>

        val downloadProviders: List<DownloadProviders.DownloadProvider>

        val resourcePatchers: List<ResourcePatchers.ResourcePatcher>

        val addons: List<Addons.Addon>

        // only direct upstream needed
        val upstream: List<Core>

        val technicalAlias: Map<TechnicalAliasVisitor, String>
            get() = mapOf()

        private fun getTechnicalAlias(visitor: TechnicalAliasVisitor): String? = technicalAlias[visitor]

        fun getTechnicalName(visitor: TechnicalAliasVisitor): String =
            technicalAlias[visitor] ?: name

        @Composable
        fun logo(
            modifier: Modifier
        ) {
            Image(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Default Core Logo",
                modifier = modifier,
            )
        }

        enum class Type(val friendlyName: () -> String) {
            SERVER({ io.hikarilan.atomservermanager.i18n.getLang("core.type").asJsonObject["server"].asString }),
            PROXY({ io.hikarilan.atomservermanager.i18n.getLang("core.type").asJsonObject["proxy"].asString }),
        }

    }

    private object Unsupported {

        object BukkitAPI : Core {

            override val name: String = "(BukkitAPI)"

            override val types: List<Core.Type> = listOf(Core.Type.SERVER)

            override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.Bukkit)

            override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf()

            override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = listOf()

            override val addons: List<Addons.Addon> = listOf()

            override val upstream: List<Core> = listOf(Vanilla)

        }

        object SpongeAPI : Core {

            override val name: String = "(SpongeAPI)"

            override val types: List<Core.Type> = listOf(Core.Type.SERVER)

            override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.Sponge)

            override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf()

            override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = listOf()

            override val addons: List<Addons.Addon> = listOf()

            override val upstream: List<Core> = listOf(Vanilla)

        }
    }

    object Vanilla : Core {

        override val name: String = "Vanilla"

        override val types: List<Core.Type> = listOf(Core.Type.SERVER)

        override val supportLoaders: List<Loaders.Loader> = emptyList()

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf(
            DownloadProviders.ServerJarsDownloadProvider
        )

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = emptyList()

        override val addons: List<Addons.Addon> = emptyList()

        override val upstream: List<Core> = emptyList()

        @Composable
        override fun logo(
            modifier: Modifier
        ) {
            NetworkImage(
                url = "https://avatars.githubusercontent.com/u/1162641",
                contentDescription = "CraftBukkit Logo",
                modifier = modifier
            )
        }

    }

    object CraftBukkit : Core {

        override val name: String = "CraftBukkit"

        override val types: List<Core.Type> = listOf(Core.Type.SERVER)

        override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.Bukkit)

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf(
            DownloadProviders.ServerJarsDownloadProvider
        )

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = emptyList()

        override val addons: List<Addons.Addon> = emptyList()

        override val upstream: List<Core> = listOf(Unsupported.BukkitAPI)

        override val technicalAlias: Map<TechnicalAliasVisitor, String> = mapOf(
            DownloadProviders.ServerJarsDownloadProvider to "bukkit"
        )

        @Composable
        override fun logo(
            modifier: Modifier
        ) {
            NetworkImage(
                url = "https://media.forgecdn.net/avatars/thumbnails/65/443/48/48/636162895990633284.png",
                contentDescription = "CraftBukkit Logo",
                modifier = modifier
            )
        }

    }

    object Spigot : Core {

        override val name: String = "Spigot"

        override val types: List<Core.Type> = listOf(Core.Type.SERVER)

        override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.Bukkit)

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf(
            DownloadProviders.ServerJarsDownloadProvider
        )

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = emptyList()

        override val addons: List<Addons.Addon> = emptyList()

        override val upstream: List<Core> = listOf(CraftBukkit)

        @Composable
        override fun logo(
            modifier: Modifier
        ) {
            NetworkImage(
                url = "https://static.spigotmc.org/img/spigot.png",
                contentDescription = "Spigot Logo",
                modifier = modifier
            )
        }

    }

    object Paper : Core {

        override val name: String = "Paper"

        override val types: List<Core.Type> = listOf(Core.Type.SERVER)

        override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.Bukkit)

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf(
            DownloadProviders.PaperMCDownloadProvider,
            DownloadProviders.ServerJarsDownloadProvider,
        )

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = listOf(
            ResourcePatchers.BMCLAPIOriginPaperResourcePatcher,
            ResourcePatchers.BMCLAPIMCBBSPaperResourcePatcher
        )

        override val addons: List<Addons.Addon> = emptyList()

        override val upstream: List<Core> = listOf(Spigot)

        @Composable
        override fun logo(
            modifier: Modifier
        ) {
            NetworkImage(
                url = "https://forums.papermc.io/data/assets/logo/logo-nwm-250.png",
                contentDescription = "Paper Logo",
                modifier = modifier
            )
        }

    }

    object SpongeVanilla : Core {

        override val name: String = "SpongeVanilla"

        override val types: List<Core.Type> = listOf(Core.Type.SERVER)

        override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.Sponge)

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf(
            DownloadProviders.SpongeDownloadProvider
        )

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = emptyList()

        override val addons: List<Addons.Addon> = emptyList()

        override val upstream: List<Core> = listOf(Unsupported.SpongeAPI)

        override val technicalAlias: Map<TechnicalAliasVisitor, String> = mapOf(
            DownloadProviders.SpongeDownloadProvider to "spongevanilla"
        )

        @Composable
        override fun logo(
            modifier: Modifier
        ) {
            NetworkImage(
                url = "https://www.spongepowered.org/assets/img/icons/spongie-mark.svg",
                contentDescription = "Sponge Logo",
                modifier = modifier
            )
        }

    }

    object SpongeForge : Core {

        override val name: String = "SpongeForge"

        override val types: List<Core.Type> = listOf(Core.Type.SERVER)

        override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.Sponge, Loaders.Mod.Forge)

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = emptyList()

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = emptyList()

        override val addons: List<Addons.Addon> = emptyList()

        override val upstream: List<Core> = listOf(Unsupported.SpongeAPI)

        override val technicalAlias: Map<TechnicalAliasVisitor, String> = mapOf(
            DownloadProviders.SpongeDownloadProvider to "spongeforge"
        )

        @Composable
        override fun logo(
            modifier: Modifier
        ) {
            NetworkImage(
                url = "https://www.spongepowered.org/assets/img/icons/spongie-mark.svg",
                contentDescription = "Sponge Logo",
                modifier = modifier
            )
        }

    }

    object BungeeCord : Core {

        override val name: String = "BungeeCord"

        override val types: List<Core.Type> = listOf(Core.Type.PROXY)

        override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.BungeeCord)

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf(
            DownloadProviders.ServerJarsDownloadProvider,
        )

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = listOf()

        override val addons: List<Addons.Addon> = listOf()

        override val upstream: List<Core> = listOf()

        @Composable
        override fun logo(modifier: Modifier) {
            NetworkImage(
                url = "https://i.imgur.com/MAg2r2J.png",
                isSvgImage = false,
                contentDescription = "BungeeCord Logo",
                modifier = modifier
            )
        }

    }

    object Waterfall : Core {

        override val name: String = "Waterfall"

        override val types: List<Core.Type> = listOf(Core.Type.PROXY)

        override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.BungeeCord)

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf(
            DownloadProviders.PaperMCDownloadProvider,
            DownloadProviders.ServerJarsDownloadProvider,
        )

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = listOf()

        override val addons: List<Addons.Addon> = listOf()

        override val upstream: List<Core> = listOf(BungeeCord)

    }


    object Velocity : Core {

        override val name: String = "Velocity"

        override val types: List<Core.Type> = listOf(Core.Type.PROXY)

        override val supportLoaders: List<Loaders.Loader> = listOf(Loaders.Plugin.Other)

        override val downloadProviders: List<DownloadProviders.DownloadProvider> = listOf(
            DownloadProviders.PaperMCDownloadProvider,
            DownloadProviders.ServerJarsDownloadProvider,
        )

        override val resourcePatchers: List<ResourcePatchers.ResourcePatcher> = listOf()

        override val addons: List<Addons.Addon> = listOf()

        override val upstream: List<Core> = listOf()

        @Composable
        override fun logo(modifier: Modifier) {
            NetworkImage(
                url = "https://forums.velocitypowered.com/uploads/default/original/1X/7c91080e5ad6a68421c1f9c33683fde39163fbe0.png",
                isSvgImage = false,
                contentDescription = "Velocity Logo",
                modifier = modifier
            )
        }

    }

}

fun buildUpstreamsChain(
    root: Cores.Core
): List<List<Cores.Core>> {
    val result: MutableList<MutableList<Cores.Core>> = mutableListOf(mutableListOf())
    contentUpstreamChain(listOf(root), result, 0)
    return result.map { it.reversed() }
}

private fun contentUpstreamChain(
    upstreams: List<Cores.Core>, context: MutableList<MutableList<Cores.Core>>, contextIdx: Int
) {
    if (upstreams.isEmpty()) {
        return
    }
    upstreams.forEachIndexed() { idx, element ->
        if (idx != 0) {
            context.add(contextIdx + idx, mutableListOf<Cores.Core>().apply {
                addAll(context[contextIdx])
                add(element)
            })
        }
    }
    upstreams.forEachIndexed() { idx, element ->
        if (idx == 0) {
            context[contextIdx].add(element)
        }
    }
    upstreams.forEachIndexed { idx, element ->
        contentUpstreamChain(element.upstream, context, contextIdx + idx)
    }
}