package io.hikarilan.atomservermanager.servers

object Loaders {

    val allLoaders =
        listOf(Plugin.Bukkit, Plugin.Sponge, Plugin.BungeeCord, Plugin.Other, Mod.Forge, Mod.Fabric, Mod.Other)

    sealed interface Loader {

        val name: String

    }

    object Plugin {

        sealed interface PluginLoader : Loader

        object Bukkit : PluginLoader {

            override val name: String = "CraftBukkit"

        }

        object Sponge : PluginLoader {

            override val name: String = "Sponge"

        }

        object BungeeCord : PluginLoader {

            override val name: String = "BungeeCord"

        }

        object Other : PluginLoader {

            override val name: String = "Other"

        }
    }

    object Mod {

        sealed interface ModLoader : Loader

        object Forge : ModLoader {

            override val name: String = "Forge"

        }

        object Fabric : ModLoader {

            override val name: String = "Fabric"

        }

        object Other : ModLoader {

            override val name: String = "Other"

        }
    }

}