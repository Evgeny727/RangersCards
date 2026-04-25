package com.rangerscards.objects

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rangerscards.R

object CampaignMaps {

    val campaignCyclesMap by lazy {
        mapOf(
            "core" to R.string.core_cycle,
            "loa" to R.string.loa_expansion
        )
    }

    val campaignExpansionsMap by lazy {
        mapOf(
            "core" to listOf(
                CampaignExpansion(
                    id = "sib",
                    name = R.string.spire_in_bloom
                ),
                CampaignExpansion(
                    id = "sos",
                    name = R.string.shadow_of_the_storm
                )
            ),
        )
    }

    val startingLocations by lazy {
        mapOf(
            "core" to "lone_tree_station",
            "loa" to "lone_tree_station"
        )
    }

    fun generalSetsMap(cycleId: String = ""): Map<String, MapLocation> {
        val generalSetsMap by lazy {
            mapOf(
                "general" to MapLocation(
                    id = "general",
                    nameResId = R.string.general_set_general,
                    iconResId = R.drawable.general,
                    cycles = listOf("core", "loa")
                ),
                "the_valley" to MapLocation(
                    id = "the_valley",
                    nameResId = R.string.general_set_the_valley,
                    iconResId = R.drawable.the_valley,
                    cycles = listOf("core", "loa")
                ),
                "the_arcology" to MapLocation(
                    id = "the_arcology",
                    nameResId = R.string.general_set_the_arcology,
                    iconResId = R.drawable.the_arcology,
                    cycles = listOf("loa")
                )
            )
        }
        return if (cycleId.isEmpty()) generalSetsMap else generalSetsMap.filterValues {
            mapLocation -> mapLocation.cycles.contains(cycleId)
        }
    }

    private fun connections(
        cycleId: String,
        expansions: List<String> = emptyList(),
        ignoreExpansions: Boolean = false
    ): List<Connection> {
        return when(cycleId) {
            "core" -> listOf(
                Connection("atrox_mountain", "northern_outpost", Path.WOODS),
                Connection("atrox_mountain", "golden_shore", Path.LAKESHORE),
                Connection("atrox_mountain", "lone_tree_station", Path.MOUNTAIN_PASS),
                Connection("golden_shore", "northern_outpost", Path.WOODS),
                Connection("golden_shore", "mount_nim", Path.LAKESHORE),
                Connection("northern_outpost", "mount_nim", Path.MOUNTAIN_PASS),
                Connection("white_sky", "mount_nim", Path.MOUNTAIN_PASS),
                Connection("white_sky", "lone_tree_station", Path.LAKESHORE),
                Connection("white_sky", "boulder_field", Path.WOODS),
                Connection("lone_tree_station", "boulder_field", Path.WOODS),
                Connection("lone_tree_station", "ancestors_grove", Path.OLD_GROWTH),
                Connection("ancestors_grove", "boulder_field", Path.WOODS),
                Connection("kobos_market", "boulder_field", Path.GRASSLAND),
                Connection("kobos_market", "ancestors_grove", Path.MOUNTAIN_PASS),
                Connection("the_plummet", "ancestors_grove", Path.MOUNTAIN_PASS),
                Connection("the_plummet", "kobos_market", Path.LAKESHORE),
                Connection("the_plummet", "the_concordant_ziggurats", Path.RAVINE),
                Connection("headwaters_station", "the_concordant_ziggurats", Path.RIVER),
                Connection("meadow", "the_concordant_ziggurats", Path.GRASSLAND),
                Connection("meadow", "greenbriar_knoll", Path.GRASSLAND),
                Connection("meadow", "stoneweaver_bridge", Path.GRASSLAND),
                Connection("meadow", "rings_of_the_moon", Path.RAVINE),
                Connection("the_concordant_ziggurats", "rings_of_the_moon", Path.RIVER),
                Connection("the_concordant_ziggurats", "archeological_outpost", Path.GRASSLAND),
                Connection("rings_of_the_moon", "archeological_outpost", Path.RAVINE),
                Connection("rings_of_the_moon", "the_alluvial_ruins", Path.RIVER),
                Connection("archeological_outpost", "the_alluvial_ruins", Path.MOUNTAIN_PASS),
                Connection("archeological_outpost", "watchers_rock", Path.RAVINE),
                Connection("the_tumbledown", "watchers_rock", Path.MOUNTAIN_PASS),
                Connection("kobos_market", "spire", Path.LAKESHORE,  expansionConditions = mapOf("sib" to ExpansionMapCondition.REMOVE)),
                Connection("white_sky", "spire", Path.RIVER, expansionConditions = mapOf("sib" to ExpansionMapCondition.REMOVE)),
                Connection("the_philosophers_garden", "spire", Path.OLD_GROWTH, expansionConditions = mapOf("sib" to ExpansionMapCondition.REMOVE)),
                Connection("the_philosophers_garden", "the_fractured_wall", Path.WOODS),
                Connection("mount_nim", "the_fractured_wall", Path.MOUNTAIN_PASS),
                Connection("the_high_basin", "the_fractured_wall", Path.LAKESHORE),
                Connection("the_furrow", "the_fractured_wall", Path.MOUNTAIN_PASS),
                Connection("the_high_basin", "the_furrow", Path.LAKESHORE),
                Connection("the_high_basin", "branch", Path.OLD_GROWTH),
                Connection("the_philosophers_garden", "branch", Path.OLD_GROWTH),
                Connection("crossroads_station", "branch", Path.OLD_GROWTH),
                Connection("crossroads_station", "spire", Path.GRASSLAND, expansionConditions = mapOf("sib" to ExpansionMapCondition.REMOVE)),
                Connection("terravore", "the_furrow", Path.RAVINE),
                Connection("crossroads_station", "biologists_outpost", Path.OLD_GROWTH),
                Connection("the_high_basin", "biologists_outpost", Path.RIVER),
                Connection("stoneweaver_bridge", "biologists_outpost", Path.RIVER),
                Connection("stoneweaver_bridge", "spire", Path.RIVER, expansionConditions = mapOf("sib" to ExpansionMapCondition.REMOVE)),
                Connection("stoneweaver_bridge", "greenbriar_knoll", Path.RAVINE),
                Connection("spire", "greenbriar_knoll", Path.WOODS, expansionConditions = mapOf("sib" to ExpansionMapCondition.REMOVE)),
                Connection("biologists_outpost", "mound_of_the_navigator", Path.WOODS),
                Connection("terravore", "mound_of_the_navigator", Path.OLD_GROWTH),
                Connection("stoneweaver_bridge", "mound_of_the_navigator", Path.RAVINE),
                Connection("the_greenbridge", "mound_of_the_navigator", Path.SWAMP),
                Connection("the_greenbridge", "sunken_outpost", Path.SWAMP),
                Connection("michaels_bog", "sunken_outpost", Path.SWAMP),
                Connection("michaels_bog", "the_cypress_citadel", Path.SWAMP),
                Connection("michaels_bog", "the_frowning_gate", Path.SWAMP),
                Connection("sunken_outpost", "the_frowning_gate", Path.SWAMP),
                Connection("the_alluvial_ruins", "the_frowning_gate", Path.RAVINE),
                Connection("bowl_of_the_sun", "the_frowning_gate", Path.MOUNTAIN_PASS),
                Connection("the_alluvial_ruins", "stoneweaver_bridge", Path.RIVER),
                Connection("the_alluvial_ruins", "bowl_of_the_sun", Path.RAVINE),
                Connection("the_tumbledown", "bowl_of_the_sun", Path.MOUNTAIN_PASS),
                Connection("the_tumbledown", "the_alluvial_ruins", Path.RIVER),
                Connection("kobos_market", "spire_crossing", Path.LAKESHORE, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("white_sky", "spire_crossing", Path.RIVER, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("the_philosophers_garden", "brookside", Path.OLD_GROWTH, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("crossroads_station", "brookside", Path.GRASSLAND, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("greenbriar_knoll", "silverfin_docks", Path.WOODS, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("stoneweaver_bridge", "silverfin_docks", Path.RIVER, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("spire_crossing", "spire", Path.THOROUGHFARE, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("spire_crossing", "silverfin_docks", Path.RIVER, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("spire_crossing", "brookside", Path.THOROUGHFARE, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("spire", "brookside", Path.THOROUGHFARE, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("silverfin_docks", "brookside", Path.THOROUGHFARE, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("spire", "silverfin_docks", Path.THOROUGHFARE, expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)),
                Connection("northern_outpost", "terra_lodge", Path.OLD_GROWTH, expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)),
                Connection("the_otherwoods", "terra_lodge", Path.OLD_GROWTH, expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)),
                Connection("crystal_basin", "the_otherwoods", Path.WOODS, expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)),
                Connection("crystal_basin", "atrox_mountains", Path.RAVINE, expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)),
                Connection("crystal_basin", "sentinal_peak", Path.MOUNTAIN_PASS, expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)),
                Connection("terra_lodge", "sentinal_peak", Path.MOUNTAIN_PASS, expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)),
                Connection("nimbus_facility", "arbor_mists", Path.NIMBUS, expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)),
                Connection("arbor_mists", "the_undergrove", Path.NIMBUS, expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)),
            )
            "loa" -> listOf(
                Connection("greenbriar_knoll", "the_concordant_ziggurats", Path.GRASSLAND),
                Connection("spire", "the_chimney", Path.NONE),
                Connection("the_chimney", "oasis_of_sunlight", Path.FLOODED_RUINS),
                Connection("oasis_of_sunlight", "scuttler_network", Path.CAVE_SYSTEM),
                Connection("scuttler_network", "orlins_vault", Path.FUNGAL_FOREST),
                Connection("the_chimney", "orlins_vault", Path.ANCIENT_RUINS),
                Connection("the_chimney", "desert_of_endless_night", Path.FLOODED_RUINS),
                Connection("orlins_vault", "desert_of_endless_night", Path.CAVE_SYSTEM),
                Connection("desert_of_endless_night", "drenching_chamber", Path.CAVE_SYSTEM),
                Connection("the_chimney", "drenching_chamber", Path.FUNGAL_FOREST),
                Connection("scuttler_network", "terminal_artery", Path.FLOODED_RUINS, ConnectionRestriction.FLOODED_PASSAGE),
                Connection("orlins_vault", "branching_artery", Path.ANCIENT_RUINS, ConnectionRestriction.LOCKED_PASSAGE),
                Connection("drenching_chamber", "severed_artery", Path.FLOODED_RUINS, ConnectionRestriction.FLOODED_PASSAGE),
                Connection("branching_artery", "terminal_artery", Path.ANCIENT_RUINS),
                Connection("branching_artery", "severed_artery", Path.ANCIENT_RUINS),
                Connection("terminal_artery", "mycelial_conclave", Path.FUNGAL_FOREST),
                Connection("branching_artery", "mycelial_conclave", Path.FUNGAL_FOREST),
                Connection("branching_artery", "silent_dormitories", Path.DEEP_ROOTS),
                Connection("severed_artery", "silent_dormitories", Path.ANCIENT_RUINS),
                Connection("severed_artery", "cerulean_curtain", Path.CAVE_SYSTEM),
                Connection("terminal_artery", "carbonforged_maze", Path.FLOODED_RUINS, ConnectionRestriction.LOCKED_PASSAGE),
                Connection("cerulean_curtain", "the_cistern", Path.FLOODED_RUINS, ConnectionRestriction.OVERGROWN_PASSAGE),
                Connection("carbonforged_maze", "arboretum_of_memory", Path.CAVE_SYSTEM),
                Connection("carbonforged_maze", "talpids_squeeze", Path.FUNGAL_FOREST),
                Connection("arboretum_of_memory", "talpids_squeeze", Path.FLOODED_RUINS),
                Connection("the_cistern", "the_verdant_sphere", Path.DEEP_ROOTS),
                Connection("the_verdant_sphere", "inverted_forest", Path.DEEP_ROOTS),
                Connection("the_cistern", "inverted_forest", Path.CAVE_SYSTEM),
                Connection("inverted_forest", "the_rootway", Path.FUNGAL_FOREST),
                Connection("the_verdant_sphere", "the_rootway", Path.DEEP_ROOTS),
                Connection("talpids_squeeze", "the_cage", Path.ANCIENT_RUINS, ConnectionRestriction.OVERGROWN_PASSAGE),
                Connection("the_rootway", "the_cage", Path.ANCIENT_RUINS, ConnectionRestriction.LOCKED_PASSAGE),
                Connection("lone_tree_station", "boulder_field", Path.WOODS),
                Connection("lone_tree_station", "ancestors_grove", Path.OLD_GROWTH),
                Connection("ancestors_grove", "boulder_field", Path.WOODS),
                Connection("kobos_market", "boulder_field", Path.GRASSLAND),
                Connection("kobos_market", "ancestors_grove", Path.MOUNTAIN_PASS),
                Connection("the_plummet", "ancestors_grove", Path.MOUNTAIN_PASS),
                Connection("the_plummet", "kobos_market", Path.LAKESHORE),
                Connection("the_plummet", "the_concordant_ziggurats", Path.RAVINE),
                Connection("headwaters_station", "the_concordant_ziggurats", Path.RIVER),
                Connection("kobos_market", "spire", Path.LAKESHORE),
                Connection("spire", "greenbriar_knoll", Path.WOODS),
            )
            else -> emptyList()
        }.let { list ->
            if (ignoreExpansions) list
            else list.filter { it.shouldIncludeForExpansions(expansions) }
        }
    }

    private fun Connection.shouldIncludeForExpansions(activeExpansions: List<String>): Boolean {
        // If there are no expansion conditions defined for this connection — include it.
        if (expansionConditions.isEmpty()) return true

        // Only consider conditions whose key is present in activeExpansions
        val matched = expansionConditions.filterKeys { it in activeExpansions }.values

        // If one or more expansion conditions matched active expansions, apply precedence:
        // REMOVE wins (exclude), then ADD (include).
        if (matched.isNotEmpty()) {
            if (matched.any { it == ExpansionMapCondition.REMOVE }) return false
            if (matched.any { it == ExpansionMapCondition.ADD }) return true
        }

        // If none matched active expansions, but this connection's expansionConditions
        // contains *only* ADDs, then do NOT include it (it's only for expansions that are not active).
        if (expansionConditions.values.all { it == ExpansionMapCondition.ADD }) return false

        // Fallback: include the connection (not tied to active expansions or mixed conditions).
        return true
    }

    private fun paths(
        cycleId: String,
        expansions: List<String> = emptyList(),
        ignoreExpansions: Boolean = false
    ): List<MapLocation> {
        val locationsList by lazy {
            listOf(
                MapLocation("atrox_mountain",           R.string.atrox_mountain,            R.drawable.atrox_mountain),
                MapLocation("northern_outpost",         R.string.northern_outpost,          R.drawable.northern_outpost),
                MapLocation("white_sky",                R.string.white_sky,                 R.drawable.white_sky),
                MapLocation("golden_shore",             R.string.golden_shore,              R.drawable.golden_shore),
                MapLocation("mount_nim",                R.string.mount_nim,                 R.drawable.mount_nim),
                MapLocation("the_fractured_wall",       R.string.the_fractured_wall,        R.drawable.the_fractured_wall),
                MapLocation("the_philosophers_garden",  R.string.the_philosophers_garden,  R.drawable.the_philosophers_garden),
                MapLocation("the_high_basin",           R.string.the_high_basin,            R.drawable.the_high_basin),
                MapLocation("branch",                   R.string.branch,                    R.drawable.branch),
                MapLocation("crossroads_station",       R.string.crossroads_station,        R.drawable.crossroads_station),
                MapLocation("the_furrow",               R.string.the_furrow,                R.drawable.the_furrow),
                MapLocation("biologists_outpost",       R.string.biologists_outpost,        R.drawable.biologists_outpost),
                MapLocation("terravore",                R.string.terravore,                 R.drawable.terravore),
                MapLocation("mound_of_the_navigator",   R.string.mound_of_the_navigator,    R.drawable.mound_of_the_navigator),
                MapLocation("the_greenbridge",          R.string.the_greenbridge,           R.drawable.the_greenbridge),
                MapLocation("michaels_bog",             R.string.michaels_bog,             R.drawable.michaels_bog),
                MapLocation("the_cypress_citadel",      R.string.the_cypress_citadel,       R.drawable.the_cypress_citadel),
                MapLocation("marsh_of_rebirth",         R.string.marsh_of_rebirth,          R.drawable.marsh_of_rebirth),
                MapLocation("sunken_outpost",           R.string.sunken_outpost,            R.drawable.sunken_outpost),
                MapLocation("the_frowning_gate",        R.string.the_frowning_gate,         R.drawable.the_frowning_gate),
                MapLocation("bowl_of_the_sun",          R.string.bowl_of_the_sun,           R.drawable.bowl_of_the_sun),
                MapLocation("the_alluvial_ruins",       R.string.the_alluvial_ruins,        R.drawable.the_alluvial_ruins),
                MapLocation("the_tumbledown",           R.string.the_tumbledown,            R.drawable.the_tumbledown),
                MapLocation("watchers_rock",            R.string.watchers_rock,            R.drawable.watchers_rock),
                MapLocation("archeological_outpost",    R.string.archeological_outpost,     R.drawable.archeological_outpost),
                MapLocation("rings_of_the_moon",        R.string.rings_of_the_moon,         R.drawable.rings_of_the_moon),
                MapLocation("meadow",                   R.string.meadow,                    R.drawable.meadow),
                MapLocation("stoneweaver_bridge",       R.string.stoneweaver_bridge,        R.drawable.stoneweaver_bridge),
                MapLocation("lone_tree_station",        R.string.lone_tree_station,         R.drawable.lone_tree_station,         cycles = listOf("core","loa")),
                MapLocation("ancestors_grove",          R.string.ancestors_grove,          R.drawable.ancestors_grove,          cycles = listOf("core","loa")),
                MapLocation("kobos_market",             R.string.kobos_market,             R.drawable.kobos_market,             cycles = listOf("core","loa")),
                MapLocation("boulder_field",            R.string.boulder_field,             R.drawable.boulder_field,             cycles = listOf("core","loa")),
                MapLocation("spire",                    R.string.spire,                     R.drawable.spire,                     cycles = listOf("core","loa")),
                MapLocation("the_concordant_ziggurats", R.string.the_concordant_ziggurats,  R.drawable.the_concordant_ziggurats,  cycles = listOf("core","loa")),
                MapLocation("greenbriar_knoll",         R.string.greenbriar_knoll,          R.drawable.greenbriar_knoll,          cycles = listOf("core","loa")),
                MapLocation("the_plummet",              R.string.the_plummet,               R.drawable.the_plummet,               cycles = listOf("core","loa")),
                MapLocation("headwaters_station",       R.string.headwaters_station,        R.drawable.headwaters_station,        cycles = listOf("core","loa")),
                MapLocation("the_chimney",              R.string.the_chimney,               R.drawable.the_chimney,               cycles = listOf("loa")),
                MapLocation("oasis_of_sunlight",        R.string.oasis_of_sunlight,         R.drawable.oasis_of_sunlight,         cycles = listOf("loa")),
                MapLocation("scuttler_network",         R.string.scuttler_network,          R.drawable.scuttler_network,          cycles = listOf("loa")),
                MapLocation("drenching_chamber",        R.string.drenching_chamber,         R.drawable.drenching_chamber,         cycles = listOf("loa")),
                MapLocation("desert_of_endless_night",  R.string.desert_of_endless_night,   R.drawable.desert_of_endless_night,   cycles = listOf("loa")),
                MapLocation("orlins_vault",             R.string.orlins_vault,             R.drawable.orlins_vault,             cycles = listOf("loa")),
                MapLocation("severed_artery",           R.string.severed_artery,            R.drawable.artery,                    cycles = listOf("loa")),
                MapLocation("branching_artery",         R.string.branching_artery,          R.drawable.artery,                    cycles = listOf("loa")),
                MapLocation("terminal_artery",          R.string.terminal_artery,           R.drawable.artery,                    cycles = listOf("loa")),
                MapLocation("silent_dormitories",       R.string.silent_dormitories,        R.drawable.silent_dormitories,        cycles = listOf("loa")),
                MapLocation("cerulean_curtain",         R.string.cerulean_curtain,          R.drawable.cerulean_curtain,          cycles = listOf("loa")),
                MapLocation("mycelial_conclave",        R.string.mycelial_conclave,         R.drawable.mycelial_conclave,         cycles = listOf("loa")),
                MapLocation("carbonforged_maze",        R.string.carbonforged_maze,         R.drawable.carbonforged_maze,         cycles = listOf("loa")),
                MapLocation("the_cistern",              R.string.the_cistern,               R.drawable.the_cistern,               cycles = listOf("loa")),
                MapLocation("inverted_forest",          R.string.inverted_forest,           R.drawable.inverted_forest,           cycles = listOf("loa")),
                MapLocation("the_verdant_sphere",       R.string.the_verdant_sphere,        R.drawable.the_verdant_sphere,        cycles = listOf("loa")),
                MapLocation("the_rootway",              R.string.the_rootway,               R.drawable.the_rootway,               cycles = listOf("loa")),
                MapLocation("arboretum_of_memory",      R.string.arboretum_of_memory,       R.drawable.arboretum_of_memory,       cycles = listOf("loa")),
                MapLocation("talpids_squeeze",          R.string.talpids_squeeze,           R.drawable.talpids_squeeze,          cycles = listOf("loa")),
                MapLocation("the_cage",                 R.string.the_cage,                  R.drawable.the_cage,                  cycles = listOf("loa")),
                MapLocation("spire_crossing",          R.string.spires_crossing,            R.drawable.spire_crossing,
                    expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)
                ),
                MapLocation("brookside",R.string.brookside,R.drawable.brookside,
                    expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)
                ),
                MapLocation("silverfin_docks",R.string.silverfin_docks,R.drawable.silverfin_docks,
                    expansionConditions = mapOf("sib" to ExpansionMapCondition.ADD)
                ),
                MapLocation("terra_lodge",R.string.terra_lodge,R.drawable.terra_lodge,
                    expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)
                ),
                MapLocation("the_otherwoods",R.string.the_otherwoods,R.drawable.the_otherwoods,
                    expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)
                ),
                MapLocation("crystal_basin",          R.string.crystal_basin,            R.drawable.crystal_basin,
                    expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)
                ),
                MapLocation("sentinel_peak",R.string.sentinel_peak,R.drawable.sentinel_peak,
                    expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)
                ),
                MapLocation("nimbus_facility",R.string.nimbus_facility,R.drawable.nimbus_facility,
                    expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)
                ),
                MapLocation("arbor_mists",          R.string.arbor_mists,            R.drawable.arbor_mists,
                    expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)
                ),
                MapLocation("the_undergrove",          R.string.the_undergrove,            R.drawable.the_undergrove,
                    expansionConditions = mapOf("sos" to ExpansionMapCondition.ADD)
                ),
            )
        }
        return when {
            cycleId.isEmpty() -> locationsList
            ignoreExpansions -> locationsList.filter { it.cycles.contains(cycleId) }
            else -> locationsList.filter {
                it.cycles.contains(cycleId) && it.shouldIncludeForExpansions(expansions)
            }
        }
    }

    private fun MapLocation.shouldIncludeForExpansions(activeExpansions: List<String>): Boolean {

        // Only consider conditions whose key is present in activeExpansions
        val matched = expansionConditions.filterKeys { it in activeExpansions }.values

        if (expansionConditions.isEmpty()) return true
        if (matched.any { it == ExpansionMapCondition.REMOVE }) return false
        if (matched.any { it == ExpansionMapCondition.ADD }) return true

        return false // fallback
    }

    fun getMapLocations(
        needConnections: Boolean,
        cycleId: String = "",
        expansions: List<String> = emptyList(),
        ignoreExpansions: Boolean = false
    ): Map<String, MapLocation> {
        val results = paths(cycleId, expansions, ignoreExpansions).associateBy { it.id }
        if (needConnections) connections(cycleId, expansions, ignoreExpansions).forEach { connection ->
            val locA = results[connection.locA]
            val locB = results[connection.locB]
            if (locA != null && locB != null) {
                val connectionB = MapConnection(connection.locB, connection.path, connection.restriction)
                val connectionA = MapConnection(connection.locA, connection.path, null)
                if (!locA.connections.contains(connectionB)) locA.connections.add(connectionB)
                if (!locB.connections.contains(connectionA)) locB.connections.add(connectionA)
            }
        }
        return results
    }

    val fixedGuideEntries by lazy {
        mapOf(
            "core" to mapOf(
                1 to listOf("1"),
                3 to listOf("94.1"),
                4 to listOf("1.04"),
            ),
            "loa" to mapOf(
                1 to listOf("1"),
                4 to listOf("199.2"),
            )
        )
    }

    fun weather(cycleId: String): List<Weather> {
        return when(cycleId) {
            "core" -> listOf(
                Weather(1, 3, R.string.weather_perfect_day),
                Weather(4, 7, R.string.weather_downpour),
                Weather(8, 9, R.string.weather_perfect_day),
                Weather(10, 12, R.string.weather_downpour),
                Weather(13, 14, R.string.weather_howling_winds),
                Weather(15, 17, R.string.weather_downpour),
                Weather(18, 20, R.string.weather_howling_winds),
                Weather(21, 22, R.string.weather_perfect_day),
                Weather(23, 25, R.string.weather_downpour),
                Weather(26, 28, R.string.weather_howling_winds),
                Weather(29, 30, R.string.weather_perfect_day),
            )
            "loa" -> listOf(
                Weather(1, 3, R.string.weather_downpour, R.string.weather_enveloping_silence),
                Weather(4, 6, R.string.weather_perfect_day, R.string.weather_glitterain),
                Weather(7, 8, R.string.weather_howling_winds, R.string.weather_shimmering_runoff),
                Weather(9, 12, R.string.weather_downpour, R.string.weather_enveloping_silence),
                Weather(13, 15, R.string.weather_perfect_day, R.string.weather_glitterain),
                Weather(16, 18, R.string.weather_downpour, R.string.weather_enveloping_silence),
                Weather(19, 21, R.string.weather_perfect_day, R.string.weather_glitterain),
                Weather(22, 23, R.string.weather_howling_winds, R.string.weather_shimmering_runoff),
                Weather(24, 27, R.string.weather_downpour, R.string.weather_enveloping_silence),
                Weather(28, 30, R.string.weather_perfect_day, R.string.weather_glitterain),
            )
            else -> emptyList()
        }
    }

    fun expansionsWeather(expansionId: String): List<Weather> {
        return when(expansionId) {
            "sib" -> listOf(
                Weather(31, 33, R.string.weather_downpour),
                Weather(34, 35, R.string.weather_perfect_day),
                Weather(36, 39, R.string.weather_howling_winds),
                Weather(40, 42, R.string.weather_downpour),
                Weather(43, 45, R.string.weather_perfect_day)
            )
            else -> emptyList()
        }
    }

    fun moonIconsMap(): Map<Int, Int> {
        return mapOf(
            1 to R.drawable.day_1,
            2 to R.drawable.day_2,
            3 to R.drawable.day_3,
            4 to R.drawable.day_4,
            5 to R.drawable.day_5,
            6 to R.drawable.day_6,
            7 to R.drawable.day_7,
            8 to R.drawable.day_8,
            9 to R.drawable.day_9,
            10 to R.drawable.day_10,
            11 to R.drawable.day_11,
            12 to R.drawable.day_12,
            13 to R.drawable.day_13,
            14 to R.drawable.day_13,
            15 to R.drawable.day_15,
            16 to R.drawable.day_16,
            17 to R.drawable.day_17,
            18 to R.drawable.day_18,
            19 to R.drawable.day_19,
            20 to R.drawable.day_20,
            21 to R.drawable.day_21,
            22 to R.drawable.day_22,
            23 to R.drawable.day_23,
            24 to R.drawable.day_24,
            25 to R.drawable.day_25,
            26 to R.drawable.day_26,
            27 to R.drawable.day_27,
            28 to R.drawable.day_28,
            29 to R.drawable.day_28,
            30 to R.drawable.day_30,
        )
    }
}

enum class Path(
    val value: String,
    @StringRes val nameResId: Int,
    @DrawableRes val iconResId: Int?,
    val cycles: List<String>,
    val expansions: List<String> = emptyList()
) {
    NONE("none", R.string.text_none, null, listOf("loa")),
    WOODS("woods", R.string.woods, R.drawable.woods, listOf("core", "loa")),
    MOUNTAIN_PASS("mountain_pass", R.string.mountain_pass, R.drawable.mountain_pass, listOf("core", "loa")),
    OLD_GROWTH("old_growth", R.string.old_growth, R.drawable.old_growth, listOf("core", "loa")),
    LAKESHORE("lakeshore", R.string.lakeshore, R.drawable.lakeshore, listOf("core", "loa")),
    GRASSLAND("grassland", R.string.grassland, R.drawable.grassland, listOf("core", "loa")),
    RAVINE("ravine", R.string.ravine, R.drawable.ravine, listOf("core", "loa")),
    SWAMP("swamp", R.string.swamp, R.drawable.swamp, listOf("core")),
    RIVER("river", R.string.river, R.drawable.river, listOf("core", "loa")),
    THOROUGHFARE("thoroughfare", R.string.thoroughfare, R.drawable.thoroughfare, listOf("core"), listOf("sib")),
    NIMBUS("nimbus", R.string.nimbus, R.drawable.nimbus, listOf("core"), listOf("sos")),
    ANCIENT_RUINS("ancient_ruins", R.string.ancient_ruins, R.drawable.ancient_ruins, listOf("loa")),
    FLOODED_RUINS("flooded_ruins", R.string.flooded_ruins, R.drawable.flooded_ruins, listOf("loa")),
    DEEP_ROOTS("deep_roots", R.string.deep_roots, R.drawable.deep_roots, listOf("loa")),
    FUNGAL_FOREST("fungal_forest", R.string.fungal_forest, R.drawable.fungal_forest, listOf("loa")),
    CAVE_SYSTEM("cave_system", R.string.cave_system, R.drawable.cave_system, listOf("loa"));

    companion object {
        fun fromValue(value: String): Path? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class ConnectionRestriction(
    val value: String,
    @StringRes val nameResId: Int,
    @DrawableRes val iconResId: Int,
    val cycles: List<String>,
) {
    FLOODED_PASSAGE("flooded_passage", R.string.flooded_passage, R.drawable.flooded_passage, listOf("loa")),
    LOCKED_PASSAGE("locked_passage", R.string.locked_passage, R.drawable.locked_passage, listOf("loa")),
    OVERGROWN_PASSAGE("overgrown_passage", R.string.overgrown_passage, R.drawable.overgrown_passage, listOf("loa"));

    companion object {
        fun fromValue(value: String): ConnectionRestriction? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

data class Connection(
    val locA: String,
    val locB: String,
    val path: Path,
    val restriction: ConnectionRestriction? = null,
    val expansionConditions: Map<String, ExpansionMapCondition> = emptyMap()
)

data class MapConnection(
    val id: String,
    val path: Path,
    val restriction: ConnectionRestriction?
)

data class MapLocation(
    val id: String,
    @StringRes val nameResId: Int,
    @DrawableRes val iconResId: Int,
    val connections: MutableList<MapConnection> = mutableListOf(),
    val cycles: List<String> = listOf("core"),
    val expansionConditions: Map<String, ExpansionMapCondition> = emptyMap()
)

data class Weather(
    val start: Int,
    val end: Int,
    @StringRes val nameResId: Int,
    @StringRes val secondNameResId: Int? = null,
)

enum class ExpansionMapCondition {
    ADD,  REMOVE
}

data class CampaignExpansion (
    val id: String,
    @StringRes val name: Int,
)