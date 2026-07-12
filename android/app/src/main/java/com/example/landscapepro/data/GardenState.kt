package com.example.landscapepro.data

data class GardenImage(
    val id: String,
    val name: String,
    val assetPath: String? = null,
    val localUri: String? = null
)

data class GardenState(
    val images: List<GardenImage> = listOf(
        GardenImage("demo", "Default Bare Yard", assetPath = "template_bare.png"),
        GardenImage("mud", "Muddy Backyard", assetPath = "google_photo_mud.png"),
        GardenImage("shade", "Shady Space Layout", assetPath = "google_photo_shade.png")
    ),
    val activeImageId: String = "demo",
    val activeConcept: String = "concept-1",
    val activeTheme: String = "cottage",
    val activeSeason: String = "summer",
    
    val currentAddress: String = "",
    val currentZip: String = "",
    val currentZone: String = "7b", // Default zone
    val currentClimate: String = "Humid Temperate",
    val currentZoneDesc: String = "Select a zip code to calculate hardiness details.",
    
    val soilType: String = "loam",
    val soilAcidity: String = "neutral",
    val sunExposure: String = "sun_full",
    val soilMoisture: String = "water_moist",
    val perennialRatio: Int = 60,
    
    val areaLength: Float = 20f,
    val areaWidth: Float = 15f,
    
    // A simple incrementing token to trigger regeneration in UI when clicking "Generate"
    val generationToken: Int = 0,
    val settingsDirty: Boolean = false,
    
    val geminiApiKey: String = "",
    val generatedConcepts: Map<String, String> = emptyMap(),
    val isGenerating: Boolean = false
) {
    val activeImage: GardenImage?
        get() = images.find { it.id == activeImageId }
        
    val sqft: Float
        get() = areaLength * areaWidth
}
