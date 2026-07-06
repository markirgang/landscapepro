package com.example.landscapepro.data

data class ZoneInfo(
    val zone: String,
    val climate: String,
    val desc: String
)

object ZipCodeDatabase {
    val zipCodes = mapOf(
        "90210" to ZoneInfo("10a", "Warm Mediterranean", "Zone 10a: Min Temp 30°F to 35°F. Ideal for warm Mediterranean and subtropical species. Sandy loam soils are common here."),
        "10001" to ZoneInfo("7b", "Humid Temperate", "Zone 7b: Min Temp 5°F to 10°F. Great for temperate shrubs, ornamental maples, hostas, and heirloom roses."),
        "60601" to ZoneInfo("6a", "Cold Temperate / Continental", "Zone 6a: Min Temp -10°F to -5°F. Requires cold-hardy perennials, evergreens, and resilient summer annuals."),
        "33101" to ZoneInfo("11a", "Subtropical / Tropical", "Zone 11a: Min Temp 40°F to 45°F. Prefers high humidity, organic peaty soils, palms, hibiscus, and heliconias."),
        "98101" to ZoneInfo("8b", "Marine West Coast", "Zone 8b: Min Temp 15°F to 20°F. Cool wet winters, mild dry summers. Ideal for ferns, mosses, rhododendrons, and conifers."),
        "80201" to ZoneInfo("5b", "Semi-arid / Cool Alpine", "Zone 5b: Min Temp -15°F to -10°F. High elevation, dry soils. Needs drought-resistant, cold-hardy plants like yuccas and conifers."),
        "75201" to ZoneInfo("8a", "Hot Humid Subtropical", "Zone 8a: Min Temp 10°F to 15°F. Hot summers. Ideal for crape myrtles, salvias, and heat-tolerant perennials."),
        "04401" to ZoneInfo("5a", "Cold Continental", "Zone 5a: Min Temp -20°F to -15°F. Long freezing winters. Requires northern forest plants like birches, spruces, and native wildflowers."),
        "99501" to ZoneInfo("4b", "Subarctic", "Zone 4b: Min Temp -25°F to -20°F. Short growing season. Emphasize native subarctic shrubs, mosses, and high-altitude wild flora."),
        "96801" to ZoneInfo("12b", "Tropical Wet", "Zone 12b: Min Temp 55°F to 60°F. Year-round tropical. Flourishes with plumerias, palms, gingers, and exotic groundcover."),
        "79901" to ZoneInfo("8b", "Arid Desert", "Zone 8b (El Paso): Min Temp 15°F to 20°F. Dry and sunny. Native Chihuahuan desert plants, agaves, and drought-tolerant shrubs are ideal."),
        "85001" to ZoneInfo("9b", "Hot Arid Desert", "Zone 9b (Phoenix): Min Temp 25°F to 30°F. Extremely hot and dry. Perfect for majestic saguaros, barrel cacti, and desert yuccas."),
        "59001" to ZoneInfo("4b", "High Plains / Cold Semiarid", "Zone 4b (Billings): Min Temp -25°F to -20°F. Hardiest pines, junipers, and resilient native plains grasses are recommended."),
        "32801" to ZoneInfo("9b", "Humid Subtropical", "Zone 9b (Orlando): Min Temp 25°F to 30°F. Wet and humid. Thrives with palmettos, hibiscus, broad ferns, and tropical groundcovers."),
        "95814" to ZoneInfo("9b", "Interior Mediterranean", "Zone 9b (Sacramento): Min Temp 25°F to 30°F. Dry summers, wet winters. Excellent for olives, lavender, citrus, and salvias."),
        "20001" to ZoneInfo("7b", "Mid-Atlantic Temperate", "Zone 7b (Washington DC): Min Temp 5°F to 10°F. Humid summers. Great for boxwood, dogwoods, azaleas, and hydrangeas."),
        "02108" to ZoneInfo("6b", "Coastal New England", "Zone 6b (Boston): Min Temp -5°F to 0°F. Coastal winters. Suitable for hostas, rhododendrons, hydrangeas, and maples."),
        "97201" to ZoneInfo("8b", "Pacific Northwest Wet", "Zone 8b (Portland): Min Temp 15°F to 20°F. Mild and very wet. Ideal for lush ferns, mosses, hostas, and maples."),
        "55401" to ZoneInfo("4b", "Severe Continental", "Zone 4b (Minneapolis): Min Temp -25°F to -20°F. Extreme seasonal range. Hardiest local birches, pines, and native wildflowers."),
        "84101" to ZoneInfo("7a", "Intermountain / Cold Semiarid", "Zone 7a (Salt Lake City): Min Temp 0°F to 5°F. Alpine valleys. Prefers yuccas, sagebrush, dwarf conifers, and rock plants.")
    )

    fun getZoneForZipCode(zip: String): ZoneInfo {
        val cleanZip = zip.trim()
        val mapped = zipCodes[cleanZip]
        if (mapped != null) return mapped

        val firstDigit = if (cleanZip.isNotEmpty()) cleanZip[0] else ' '
        return when (firstDigit) {
            '0' -> ZoneInfo("6b", "Northeastern Coastal/Temperate", "Zone 6b (Zip $zip): Min Temp -5°F to 0°F. Cool summers, cold winters. Ideal for mixed woodland species, hostas, and hydrangeas.")
            '1' -> ZoneInfo("7a", "Mid-Atlantic Temperate", "Zone 7a (Zip $zip): Min Temp 0°F to 5°F. Moderately cold winters. Good for dogwoods, azaleas, and hardy perennials.")
            '2' -> ZoneInfo("7b", "Upper South Temperate", "Zone 7b (Zip $zip): Min Temp 5°F to 10°F. Humid temperate. Ideal for camellias, boxwoods, and deciduous magnolias.")
            '3' -> ZoneInfo("9a", "Humid Subtropical", "Zone 9a (Zip $zip): Min Temp 20°F to 25°F. Mild winters, hot summers. Excellent for palms, citrus, and southern live oaks.")
            '4' -> ZoneInfo("6a", "East Central Continental", "Zone 6a (Zip $zip): Min Temp -10°F to -5°F. Seasonal extremes. Prefers native oaks, maples, and cold-hardy shrubs.")
            '5' -> ZoneInfo("4b", "Upper Midwest Severe Continental", "Zone 4b (Zip $zip): Min Temp -25°F to -20°F. Very cold winters. Requires extremely hardy evergreens and native prairie flowers.")
            '6' -> ZoneInfo("6a", "Midwest Continental", "Zone 6a (Zip $zip): Min Temp -10°F to -5°F. Hot summers, cold winters. Resilient perennials and deciduous shade trees flourish.")
            '7' -> ZoneInfo("8a", "South Central Subtropical", "Zone 8a (Zip $zip): Min Temp 10°F to 15°F. Hot, humid summers. Ideal for crape myrtles, salvias, and drought-tolerant grasses.")
            '8' -> ZoneInfo("7a", "Intermountain / Semi-Arid", "Zone 7a (Zip $zip): Min Temp 0°F to 5°F. High elevation, dry air. Best for pines, yuccas, and drought-tolerant groundcovers.")
            '9' -> ZoneInfo("9b", "Pacific Coastal / Mediterranean", "Zone 9b (Zip $zip): Min Temp 25°F to 30°F. Mild wet winters, dry summers. Perfect for lavender, rosemary, and olive trees.")
            else -> ZoneInfo("7b", "Temperate Climate", "Zone 7b (Zip $zip): Min Temp 5°F to 10°F. Suitable for a wide range of temperate garden styles.")
        }
    }

    fun getZipForAddress(addressText: String, resolvedZip: String?): String {
        if (resolvedZip != null && resolvedZip.isNotEmpty()) return resolvedZip

        val cleanText = addressText.trim()
        val regex = Regex("\\b\\d{5}\\b")
        val matchResult = regex.find(cleanText)
        if (matchResult != null) return matchResult.value

        val cityZip = findZipByCityName(cleanText)
        if (cityZip != null) return cityZip

        // Deterministic hashing fallback
        var hash = 0
        for (char in cleanText) {
            hash = char.code + ((hash shl 5) - hash)
        }
        val fallbackZips = zipCodes.keys.toList()
        if (fallbackZips.isEmpty()) return "90210"
        val index = kotlin.math.abs(hash) % fallbackZips.size
        return fallbackZips[index]
    }

    private fun findZipByCityName(addressText: String): String? {
        val lower = addressText.lowercase()
        return when {
            lower.contains("beverly hills") || lower.contains("los angeles") -> "90210"
            lower.contains("new york") || lower.contains("manhattan") -> "10001"
            lower.contains("chicago") -> "60601"
            lower.contains("miami") -> "33101"
            lower.contains("seattle") -> "98101"
            lower.contains("denver") -> "80201"
            lower.contains("dallas") -> "75201"
            lower.contains("bangor") -> "04401"
            lower.contains("anchorage") -> "99501"
            lower.contains("honolulu") -> "96801"
            lower.contains("el paso") -> "79901"
            lower.contains("phoenix") -> "85001"
            lower.contains("billings") -> "59001"
            lower.contains("orlando") -> "32801"
            lower.contains("sacramento") -> "95814"
            lower.contains("washington dc") || lower.contains("washington d.c.") -> "20001"
            lower.contains("boston") -> "02108"
            lower.contains("portland") -> "97201"
            lower.contains("minneapolis") -> "55401"
            lower.contains("salt lake city") -> "84101"
            else -> null
        }
    }
}
