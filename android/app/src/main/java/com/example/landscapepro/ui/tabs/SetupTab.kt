package com.example.landscapepro.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.landscapepro.data.GardenState
import com.example.landscapepro.data.ZipCodeDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupTab(
    state: GardenState,
    onStateChanged: (GardenState) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Location & Climate",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Address & Zip Input
        OutlinedTextField(
            value = state.currentAddress,
            onValueChange = { addr ->
                val zip = ZipCodeDatabase.getZipForAddress(addr, null)
                val zoneInfo = ZipCodeDatabase.getZoneForZipCode(zip)
                onStateChanged(
                    state.copy(
                        currentAddress = addr,
                        currentZip = zip,
                        currentZone = zoneInfo.zone,
                        currentClimate = zoneInfo.climate,
                        currentZoneDesc = zoneInfo.desc,
                        settingsDirty = true
                    )
                )
            },
            label = { Text("Property Address or Zip Code") },
            placeholder = { Text("e.g. 90210 or 123 Maple St") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Address") },
            modifier = Modifier.fillMaxWidth()
        )

        // GPS simulation button
        Button(
            onClick = {
                // Simulate GPS address lookup
                val simulatedAddr = "Beverly Hills, CA 90210"
                val zip = "90210"
                val zoneInfo = ZipCodeDatabase.getZoneForZipCode(zip)
                onStateChanged(
                    state.copy(
                        currentAddress = simulatedAddr,
                        currentZip = zip,
                        currentZone = zoneInfo.zone,
                        currentClimate = zoneInfo.climate,
                        currentZoneDesc = zoneInfo.desc,
                        settingsDirty = true
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simulate GPS Location Lookup")
        }

        // Zone Info Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Climate Zone")
                    Text(
                        text = "USDA Hardiness Zone: ${state.currentZone}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Climate: ${state.currentClimate}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.currentZoneDesc,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Divider()

        Text(
            text = "Soil & Environment",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Soil Type Selector
        val soilTypes = listOf(
            "loam" to "Loam (Fertile, Balanced)",
            "clay" to "Clay (Heavy, Clayey)",
            "sandy" to "Sandy (Light, Sandy)",
            "silty" to "Silty (Smooth, Fertile)",
            "peaty" to "Peaty (Acidic, Organic)",
            "chalky" to "Chalky (Alkaline, Stony)"
        )
        var soilExpanded by remember { mutableStateOf(false) }
        val selectedSoilLabel = soilTypes.find { it.first == state.soilType }?.second ?: state.soilType

        ExposedDropdownMenuBox(
            expanded = soilExpanded,
            onExpandedChange = { soilExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedSoilLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Soil Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = soilExpanded,
                onDismissRequest = { soilExpanded = false }
            ) {
                soilTypes.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onStateChanged(state.copy(soilType = value, settingsDirty = true))
                            soilExpanded = false
                        }
                    )
                }
            }
        }

        // Soil Acidity Selector
        val acidities = listOf(
            "neutral" to "Neutral (pH 6.0 - 7.0) - Optimal",
            "acidic" to "Acidic (pH < 6.0) - Azaleas/Hydrangeas",
            "alkaline" to "Alkaline (pH > 7.0) - Lilacs/Clematis"
        )
        var acidExpanded by remember { mutableStateOf(false) }
        val selectedAcidLabel = acidities.find { it.first == state.soilAcidity }?.second ?: state.soilAcidity

        ExposedDropdownMenuBox(
            expanded = acidExpanded,
            onExpandedChange = { acidExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedAcidLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Soil Acidity (pH)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = acidExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = acidExpanded,
                onDismissRequest = { acidExpanded = false }
            ) {
                acidities.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onStateChanged(state.copy(soilAcidity = value, settingsDirty = true))
                            acidExpanded = false
                        }
                    )
                }
            }
        }

        // Sun Exposure Selector
        val sunExposures = listOf(
            "sun_full" to "Full Sun (> 6 hours)",
            "sun_partial" to "Partial Shade (3 - 6 hours)",
            "sun_shade" to "Full Shade (< 3 hours)"
        )
        var sunExpanded by remember { mutableStateOf(false) }
        val selectedSunLabel = sunExposures.find { it.first == state.sunExposure }?.second ?: state.sunExposure

        ExposedDropdownMenuBox(
            expanded = sunExpanded,
            onExpandedChange = { sunExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedSunLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sun Exposure") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sunExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = sunExpanded,
                onDismissRequest = { sunExpanded = false }
            ) {
                sunExposures.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onStateChanged(state.copy(sunExposure = value, settingsDirty = true))
                            sunExpanded = false
                        }
                    )
                }
            }
        }

        // Moisture Selector
        val moistures = listOf(
            "water_dry" to "Dry (Low Water Requirement)",
            "water_moist" to "Moist (Regular Watering)",
            "water_wet" to "Wet (High Water/Bog)",
            "water_well" to "Well-drained (Optimal Drainage)"
        )
        var moistExpanded by remember { mutableStateOf(false) }
        val selectedMoistLabel = moistures.find { it.first == state.soilMoisture }?.second ?: state.soilMoisture

        ExposedDropdownMenuBox(
            expanded = moistExpanded,
            onExpandedChange = { moistExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedMoistLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Soil Moisture") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = moistExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = moistExpanded,
                onDismissRequest = { moistExpanded = false }
            ) {
                moistures.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onStateChanged(state.copy(soilMoisture = value, settingsDirty = true))
                            moistExpanded = false
                        }
                    )
                }
            }
        }

        Divider()

        Text(
            text = "Plant Bed Dimensions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = if (state.areaLength > 0f) state.areaLength.toString() else "",
                onValueChange = { lenStr ->
                    val len = lenStr.toFloatOrNull() ?: 0f
                    onStateChanged(state.copy(areaLength = len, settingsDirty = true))
                },
                label = { Text("Length (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = if (state.areaWidth > 0f) state.areaWidth.toString() else "",
                onValueChange = { widStr ->
                    val wid = widStr.toFloatOrNull() ?: 0f
                    onStateChanged(state.copy(areaWidth = wid, settingsDirty = true))
                },
                label = { Text("Width (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Total Area Size: ${state.sqft.toInt()} sq ft",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
