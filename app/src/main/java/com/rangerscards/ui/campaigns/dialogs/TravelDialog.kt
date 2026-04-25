package com.rangerscards.ui.campaigns.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.objects.ConnectionRestriction
import com.rangerscards.objects.Path
import com.rangerscards.ui.campaigns.CampaignViewModel
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersRadioButton
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

enum class TravelDataDialog {
    Location,
    PathTerrain
}

@Composable
fun TravelDialog(
    campaignViewModel: CampaignViewModel,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    user: FirebaseUser?
) {
    val campaign by campaignViewModel.campaign.collectAsState()
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    var showDialogPicker by rememberSaveable { mutableStateOf<TravelDataDialog?>(null) }
    var showAllLocations by rememberSaveable { mutableStateOf(false) }
    var isCamping by rememberSaveable { mutableStateOf(false) }
    var selectedLocation by rememberSaveable { mutableStateOf("") }
    var selectedPathTerrain by rememberSaveable { mutableStateOf("") }
    var selectedConnectionRestriction by rememberSaveable { mutableStateOf("") }
    val coroutine = rememberCoroutineScope()
    val locationsMap by remember(campaign) {
        mutableStateOf(CampaignMaps.getMapLocations(
            true,
            campaign!!.cycleId,
            campaign!!.expansions
        ))
    }
    val currentLocation by remember { derivedStateOf { locationsMap[campaign!!.currentLocation] } }
    val isLegitTravel by remember { derivedStateOf {
        selectedLocation.isNotEmpty() && selectedPathTerrain.isNotEmpty()
    } }
    LaunchedEffect(currentLocation) {
        if (currentLocation == null) showAllLocations = true
    }
    RangersDialogWithContent(
        header = stringResource(
            id = R.string.travel_header,
            stringResource(currentLocation?.nameResId ?: R.string.text_none)
        ),
        isDarkTheme = isDarkTheme,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp).clickable {
                    showAllLocations = !showAllLocations
                },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.show_all_locations_radio_button),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                RangersRadioButton(
                    selected = showAllLocations,
                    onClick = { showAllLocations = !showAllLocations },
                    modifier = Modifier.size(32.dp),
                    enabled = currentLocation != null
                )
            }
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text(
                    text = stringResource(R.string.location_data_type),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedLocation.isNotEmpty()) {
                        val location = locationsMap[selectedLocation]!!
                        Icon(
                            painterResource(location.iconResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = stringResource(location.nameResId),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    } else Text(
                        text = stringResource(R.string.travel_location_placeholder),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showDialogPicker = TravelDataDialog.Location },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.edit_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text(
                    text = stringResource(R.string.path_terrain_data_type),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedPathTerrain.isNotEmpty()) {
                        val currentPathTerrain = Path
                            .fromValue(selectedPathTerrain)!!
                        if (currentPathTerrain.iconResId != null) Icon(
                            painterResource(currentPathTerrain.iconResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = stringResource(currentPathTerrain.nameResId),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    } else if (showAllLocations) Text(
                        text = stringResource(R.string.travel_path_terrain_placeholder),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    ) else Text(
                        text = stringResource(R.string.text_none),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    )
                    if (showAllLocations) IconButton(
                        onClick = { showDialogPicker = TravelDataDialog.PathTerrain },
                        colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.edit_32dp),
                            contentDescription = null,
                            tint = CustomTheme.colors.m,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            if (selectedConnectionRestriction.isNotEmpty())
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Text(
                        text = stringResource(R.string.connection_restriction_data_type),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentConnectionRestriction = ConnectionRestriction
                            .fromValue(selectedConnectionRestriction)!!
                        Icon(
                            painterResource(currentConnectionRestriction.iconResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = stringResource(currentConnectionRestriction.nameResId),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
            }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp).clickable {
                    isCamping = !isCamping
                },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.camp_radio_button),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                RangersRadioButton(
                    selected = isCamping,
                    onClick = { isCamping = !isCamping },
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = stringResource(if (isCamping) R.string.travel_with_camp
                else R.string.travel_without_camp_text, campaign!!.currentDay),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        SquareButton(
            stringId = R.string.travel_button,
            leadingIcon = R.drawable.travel_32dp,
            buttonColor = ButtonDefaults.buttonColors().copy(
                containerColor = CustomTheme.colors.d10,
                disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.3f)
            ),
            onClick = { coroutine.launch { showLoadingDialog = true
                campaignViewModel.campaignTravel(selectedLocation, selectedPathTerrain, isCamping, user)
            }.invokeOnCompletion { showLoadingDialog = false
                onBack.invoke()
            } },
            isEnabled = isLegitTravel,
            modifier = Modifier.padding(8.dp)
        )
    }
    if (showLoadingDialog) Dialog(
        onDismissRequest = { showLoadingDialog = false },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        RangersBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = R.string.saving_changes_header
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
            }
        }
    }
    if (showDialogPicker != null) Dialog(
        onDismissRequest = { showDialogPicker = null },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        RangersBaseCard(
            isDarkTheme = isDarkTheme,
            labelIdRes = when(showDialogPicker) {
                TravelDataDialog.Location -> R.string.location_data_type
                else -> R.string.path_terrain_data_type
            },
            modifier = Modifier.sizeIn(maxHeight = 400.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when(showDialogPicker) {
                    TravelDataDialog.Location ->
                        if (showAllLocations || currentLocation == null) locationsMap.forEach { (key, value) ->
                            if (key != currentLocation?.id) item(key) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedLocation = key
                                        selectedConnectionRestriction = currentLocation?.connections
                                            ?.firstOrNull { it.id == key }?.restriction?.value ?: ""
                                        showDialogPicker = null
                                    },
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(value.iconResId),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = stringResource(value.nameResId),
                                        color = CustomTheme.colors.d30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = CustomTheme.colors.l10)
                            }
                        } else currentLocation?.connections?.forEach { location ->
                            item(location.id) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedLocation = location.id
                                        selectedPathTerrain = location.path.value
                                        selectedConnectionRestriction = location.restriction?.value ?: ""
                                        showDialogPicker = null
                                    },
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val displayedLocation = locationsMap[location.id]!!
                                    Icon(
                                        painterResource(displayedLocation.iconResId),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = stringResource(displayedLocation.nameResId),
                                        color = CustomTheme.colors.d30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = CustomTheme.colors.l10)
                            }
                        }
                        else -> Path.entries.filter { it.cycles.contains(campaign!!.cycleId) }.forEach { path ->
                            item(path.value) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedPathTerrain = path.value
                                        showDialogPicker = null
                                    },
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (path.iconResId != null) Icon(
                                        painterResource(path.iconResId),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = stringResource(path.nameResId),
                                        color = CustomTheme.colors.d30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = CustomTheme.colors.l10)
                            }
                        }
                }
            }
        }
    }
}