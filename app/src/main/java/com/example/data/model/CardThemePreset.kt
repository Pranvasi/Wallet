package com.example.data.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class GradientDirection {
    DIAGONAL,
    HORIZONTAL,
    VERTICAL
}

data class CardThemePreset(
    val id: String,
    val name: String,
    val startColor: Long,
    val endColor: Long,
    val middleColors: List<Long> = emptyList(),
    val direction: GradientDirection = GradientDirection.DIAGONAL,
    val textColor: Long = 0xFFFFFFFF,
    val accentColor: Long = 0xFFFFD700,
    val category: String = "Classic"
) {
    fun createBrush(): Brush {
        val colors = if (middleColors.isNotEmpty()) {
            listOf(Color(startColor)) + middleColors.map { Color(it) } + listOf(Color(endColor))
        } else {
            listOf(Color(startColor), Color(endColor))
        }
        return when (direction) {
            GradientDirection.HORIZONTAL -> Brush.horizontalGradient(colors)
            GradientDirection.VERTICAL -> Brush.verticalGradient(colors)
            GradientDirection.DIAGONAL -> Brush.linearGradient(colors)
        }
    }
}

object CardThemes {
    val presets = listOf(
        // -------------------------------------------------------------------------
        // 1. CLASSIC & STEALTH (Original presets preserved for 100% compatibility)
        // -------------------------------------------------------------------------
        CardThemePreset(
            id = "midnight_black",
            name = "Midnight Obsidian",
            startColor = 0xFF1E2022,
            endColor = 0xFF121314,
            textColor = 0xFFECEFF1,
            accentColor = 0xFFFFC107,
            category = "Classic & Dark"
        ),
        CardThemePreset(
            id = "sapphire_blue",
            name = "Royal Sapphire",
            startColor = 0xFF1A365D,
            endColor = 0xFF0D1B2A,
            textColor = 0xFFFFFFFF,
            accentColor = 0xFF64B5F6,
            category = "Classic & Dark"
        ),
        CardThemePreset(
            id = "emerald_vault",
            name = "Emerald Vault",
            startColor = 0xFF0F5132,
            endColor = 0xFF052C1A,
            textColor = 0xFFE8F5E9,
            accentColor = 0xFF81C784,
            category = "Classic & Dark"
        ),
        CardThemePreset(
            id = "sunset_amber",
            name = "Sunset Amber",
            startColor = 0xFFB45309,
            endColor = 0xFF78350F,
            textColor = 0xFFFFFBEB,
            accentColor = 0xFFFCD34D,
            category = "Classic & Dark"
        ),
        CardThemePreset(
            id = "royal_amethyst",
            name = "Imperial Purple",
            startColor = 0xFF581C87,
            endColor = 0xFF3B0764,
            textColor = 0xFFFAF5FF,
            accentColor = 0xFFC084FC,
            category = "Classic & Dark"
        ),
        CardThemePreset(
            id = "rose_gold",
            name = "Rose Gold",
            startColor = 0xFF9F1239,
            endColor = 0xFF4C0519,
            textColor = 0xFFFFF1F2,
            accentColor = 0xFFFECDD3,
            category = "Classic & Dark"
        ),
        CardThemePreset(
            id = "ocean_cyan",
            name = "Ocean Cyan",
            startColor = 0xFF0E7490,
            endColor = 0xFF164E63,
            textColor = 0xFFECFEFF,
            accentColor = 0xFF67E8F9,
            category = "Classic & Dark"
        ),
        CardThemePreset(
            id = "carbon_fiber",
            name = "Space Gray",
            startColor = 0xFF374151,
            endColor = 0xFF1F2937,
            textColor = 0xFFF3F4F6,
            accentColor = 0xFF9CA3AF,
            category = "Classic & Dark"
        ),

        // -------------------------------------------------------------------------
        // 2. INDIAN FLAG TRICOLOR TRIO (Saffron, White, Green in H, V, D directions)
        // -------------------------------------------------------------------------
        CardThemePreset(
            id = "tiranga_horizontal",
            name = "Tiranga (Horizontal)",
            startColor = 0xFFFF7722, // Rich India Saffron (Kesari)
            endColor = 0xFF0B7A38,   // Vibrant India Green
            middleColors = listOf(0xFFFFFFFF), // Pure White
            direction = GradientDirection.HORIZONTAL,
            textColor = 0xFF0A192F, // Deep Ashoka Chakra Navy Blue for crisp contrast
            accentColor = 0xFF002171, // National Emblem Ashoka Blue
            category = "Indian Flag"
        ),
        CardThemePreset(
            id = "tiranga_vertical",
            name = "Tiranga (Vertical)",
            startColor = 0xFFFF7722, // India Saffron (Top)
            endColor = 0xFF0B7A38,   // India Green (Bottom)
            middleColors = listOf(0xFFFFFFFF), // White (Center)
            direction = GradientDirection.VERTICAL,
            textColor = 0xFF0A192F, // Deep Ashoka Chakra Navy Blue
            accentColor = 0xFF002171, // Ashoka Blue
            category = "Indian Flag"
        ),
        CardThemePreset(
            id = "tiranga_diagonal",
            name = "Tiranga (Diagonal)",
            startColor = 0xFFFF7722, // India Saffron (Top-Left)
            endColor = 0xFF0B7A38,   // India Green (Bottom-Right)
            middleColors = listOf(0xFFFFFFFF), // White (Center)
            direction = GradientDirection.DIAGONAL,
            textColor = 0xFF0A192F, // Deep Ashoka Chakra Navy Blue
            accentColor = 0xFF002171, // Ashoka Blue
            category = "Indian Flag"
        ),

        // -------------------------------------------------------------------------
        // 3. PRESTIGE METALS & GOLD (Elite Banking)
        // -------------------------------------------------------------------------
        CardThemePreset(
            id = "champagne_gold",
            name = "Champagne Gold",
            startColor = 0xFFB8860B,
            endColor = 0xFF5A3E09,
            middleColors = listOf(0xFFD4AF37),
            textColor = 0xFFFFFDF5,
            accentColor = 0xFFFFECB3,
            category = "Prestige Metals"
        ),
        CardThemePreset(
            id = "aurum_elite",
            name = "Aurum Elite",
            startColor = 0xFF926F12,
            endColor = 0xFF3D2C04,
            middleColors = listOf(0xFFC9A038),
            textColor = 0xFFFFFDF2,
            accentColor = 0xFFFFD54F,
            category = "Prestige Metals"
        ),
        CardThemePreset(
            id = "titanium_silver",
            name = "Titanium Slate",
            startColor = 0xFF4B5563,
            endColor = 0xFF1E293B,
            middleColors = listOf(0xFF64748B),
            textColor = 0xFFF8FAFC,
            accentColor = 0xFFE2E8F0,
            category = "Prestige Metals"
        ),
        CardThemePreset(
            id = "platinum_sheen",
            name = "Platinum Sheen",
            startColor = 0xFF334155,
            endColor = 0xFF0F172A,
            textColor = 0xFFF1F5F9,
            accentColor = 0xFFCBD5E1,
            category = "Prestige Metals"
        ),
        CardThemePreset(
            id = "matte_black_onyx",
            name = "Black Onyx",
            startColor = 0xFF111113,
            endColor = 0xFF050506,
            textColor = 0xFFE4E4E7,
            accentColor = 0xFF71717A,
            category = "Prestige Metals"
        ),
        CardThemePreset(
            id = "copper_bronze",
            name = "Aegis Copper",
            startColor = 0xFF7C2D12,
            endColor = 0xFF381004,
            textColor = 0xFFFFF7ED,
            accentColor = 0xFFFDBA74,
            category = "Prestige Metals"
        ),
        CardThemePreset(
            id = "dark_espresso",
            name = "Dark Espresso",
            startColor = 0xFF2E1A14,
            endColor = 0xFF140B08,
            textColor = 0xFFFFF7ED,
            accentColor = 0xFFD97706,
            category = "Prestige Metals"
        ),
        CardThemePreset(
            id = "monaco_navy",
            name = "Monaco Gold",
            startColor = 0xFF0A192F,
            endColor = 0xFF1E3E62,
            textColor = 0xFFF8FAFC,
            accentColor = 0xFFD4AF37,
            category = "Prestige Metals"
        ),

        // -------------------------------------------------------------------------
        // 4. ROYAL GEMSTONES
        // -------------------------------------------------------------------------
        CardThemePreset(
            id = "ruby_resplendent",
            name = "Ruby Velvet",
            startColor = 0xFF881337,
            endColor = 0xFF3F0713,
            textColor = 0xFFFFF1F2,
            accentColor = 0xFFFDA4AF,
            category = "Royal Gemstones"
        ),
        CardThemePreset(
            id = "crimson_garnet",
            name = "Crimson Garnet",
            startColor = 0xFF7F1D1D,
            endColor = 0xFF360808,
            textColor = 0xFFFEF2F2,
            accentColor = 0xFFF87171,
            category = "Royal Gemstones"
        ),
        CardThemePreset(
            id = "deep_maritime",
            name = "Deep Maritime",
            startColor = 0xFF1E3A8A,
            endColor = 0xFF0C1428,
            textColor = 0xFFEFF6FF,
            accentColor = 0xFF93C5FD,
            category = "Royal Gemstones"
        ),
        CardThemePreset(
            id = "lapis_lazuli",
            name = "Lapis Lazuli",
            startColor = 0xFF172554,
            endColor = 0xFF09142E,
            textColor = 0xFFEFF6FF,
            accentColor = 0xFF60A5FA,
            category = "Royal Gemstones"
        ),
        CardThemePreset(
            id = "cosmic_amethyst",
            name = "Cosmic Amethyst",
            startColor = 0xFF6B21A8,
            endColor = 0xFF2E0854,
            textColor = 0xFFFAF5FF,
            accentColor = 0xFFD8B4FE,
            category = "Royal Gemstones"
        ),
        CardThemePreset(
            id = "velvet_orchid",
            name = "Velvet Orchid",
            startColor = 0xFF701A75,
            endColor = 0xFF3B073D,
            textColor = 0xFFFDF4FF,
            accentColor = 0xFFF0ABFC,
            category = "Royal Gemstones"
        ),
        CardThemePreset(
            id = "solar_topaz",
            name = "Solar Topaz",
            startColor = 0xFFC2410C,
            endColor = 0xFF651B03,
            textColor = 0xFFFFF7ED,
            accentColor = 0xFFFFB74D,
            category = "Royal Gemstones"
        ),
        CardThemePreset(
            id = "tanzanite_glow",
            name = "Tanzanite Glow",
            startColor = 0xFF3730A3,
            endColor = 0xFF1E1B4B,
            textColor = 0xFFEEF2FF,
            accentColor = 0xFFA5B4FC,
            category = "Royal Gemstones"
        ),

        // -------------------------------------------------------------------------
        // 5. NATURE, FOREST & OCEAN
        // -------------------------------------------------------------------------
        CardThemePreset(
            id = "imperial_jade",
            name = "Imperial Jade",
            startColor = 0xFF065F46,
            endColor = 0xFF022C22,
            textColor = 0xFFECFDF5,
            accentColor = 0xFF6EE7B7,
            category = "Nature & Ocean"
        ),
        CardThemePreset(
            id = "forest_nocturne",
            name = "Forest Nocturne",
            startColor = 0xFF14532D,
            endColor = 0xFF052410,
            textColor = 0xFFF0FDF4,
            accentColor = 0xFF86EFAC,
            category = "Nature & Ocean"
        ),
        CardThemePreset(
            id = "alpine_moss",
            name = "Alpine Moss",
            startColor = 0xFF365314,
            endColor = 0xFF142405,
            textColor = 0xFFF7FEE7,
            accentColor = 0xFFA3E635,
            category = "Nature & Ocean"
        ),
        CardThemePreset(
            id = "sage_eucalyptus",
            name = "Sage Eucalyptus",
            startColor = 0xFF115E59,
            endColor = 0xFF042F2C,
            textColor = 0xFFF0FDFA,
            accentColor = 0xFF5EEAD4,
            category = "Nature & Ocean"
        ),
        CardThemePreset(
            id = "pacific_abyss",
            name = "Pacific Abyss",
            startColor = 0xFF0C4A6E,
            endColor = 0xFF041E2D,
            textColor = 0xFFF0F9FF,
            accentColor = 0xFF38BDF8,
            category = "Nature & Ocean"
        ),
        CardThemePreset(
            id = "caribbean_turquoise",
            name = "Caribbean Sea",
            startColor = 0xFF0891B2,
            endColor = 0xFF155E75,
            textColor = 0xFFECFEFF,
            accentColor = 0xFF67E8F9,
            category = "Nature & Ocean"
        ),
        CardThemePreset(
            id = "arctic_glacier",
            name = "Arctic Glacier",
            startColor = 0xFF0284C7,
            endColor = 0xFF0369A1,
            middleColors = listOf(0xFF38BDF8),
            textColor = 0xFFF0F9FF,
            accentColor = 0xFFBAE6FD,
            category = "Nature & Ocean"
        ),

        // -------------------------------------------------------------------------
        // 6. CYBER & COSMIC GRADIENTS
        // -------------------------------------------------------------------------
        CardThemePreset(
            id = "cyber_sunset",
            name = "Cyber Sunset",
            startColor = 0xFF4338CA,
            endColor = 0xFFBE185D,
            textColor = 0xFFFFFFFF,
            accentColor = 0xFFF472B6,
            category = "Cyber & Cosmic"
        ),
        CardThemePreset(
            id = "solar_flare",
            name = "Solar Flare",
            startColor = 0xFFB91C1C,
            endColor = 0xFFD97706,
            textColor = 0xFFFFFBEB,
            accentColor = 0xFFFDE047,
            category = "Cyber & Cosmic"
        ),
        CardThemePreset(
            id = "borealis_sky",
            name = "Borealis Sky",
            startColor = 0xFF1E3A8A,
            endColor = 0xFF065F46,
            textColor = 0xFFF0FDF4,
            accentColor = 0xFF34D399,
            category = "Cyber & Cosmic"
        ),
        CardThemePreset(
            id = "deep_nebula",
            name = "Deep Nebula",
            startColor = 0xFF311042,
            endColor = 0xFF0A192F,
            middleColors = listOf(0xFF4C1D95),
            textColor = 0xFFFAF5FF,
            accentColor = 0xFFC084FC,
            category = "Cyber & Cosmic"
        ),
        CardThemePreset(
            id = "volcanic_magma",
            name = "Volcanic Magma",
            startColor = 0xFF831843,
            endColor = 0xFF18181B,
            textColor = 0xFFFFF1F2,
            accentColor = 0xFFFB7185,
            category = "Cyber & Cosmic"
        )
    )

    fun getPreset(id: String?): CardThemePreset {
        return presets.find { it.id == id } ?: presets.first()
    }
}
