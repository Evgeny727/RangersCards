package com.rangerscards.ui.campaigns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.ui.components.DataPicker
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.RangersLoadingDialog
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.components.RangersRadioButtonRow
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings

@Composable
fun CampaignCreationScreen(
    onCancel: () -> Unit,
    onCreate: (String) -> Unit,
    emitError: (Throwable) -> Unit,
    userInfo: UserInfo?,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    campaignCreationViewModel: CampaignCreationViewModel = hiltViewModel()
) {
    val campaignCreationUiState by campaignCreationViewModel.campaignCreationUiState.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var isUploading by rememberSaveable { mutableStateOf(false) }
    var cycle by remember { mutableStateOf("") }
    var transferCampaignInfo by remember { mutableStateOf("" to "") }
    var showDialogPicker by rememberSaveable { mutableStateOf(false) }
    var showTransferDialogPicker by rememberSaveable { mutableStateOf(false) }
    val isLegit by remember {
        derivedStateOf {
            name.isNotEmpty() && cycle.isNotEmpty()
        }
    }
    val expansions = rememberSaveable(saver = listSaver(
        save = { stateList -> stateList.toList() },
        restore = { restored -> restored.toMutableStateList() }
    )) { mutableStateListOf<String>() }
    val availableExpansions = remember(cycle) { CampaignMaps.campaignExpansionsMap[cycle] }

    LaunchedEffect(campaignCreationUiState) {
        when (val state = campaignCreationUiState) {
            is CampaignCreationUiState.Success -> onCreate(state.campaignId)
            is CampaignCreationUiState.Error -> onCancel()
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        campaignCreationViewModel.events.collect {
            emitError(it.exception)
        }
    }

    if (campaignCreationUiState is CampaignCreationUiState.Loading) RangersLoadingDialog(isDarkTheme)

    if (showDialogPicker) RangersDialogWithContent(
        headerId = R.string.campaign,
        isDarkTheme = isDarkTheme,
        onBack = { showDialogPicker = false },
    ) {
        LazyColumn(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
            CampaignMaps.campaignCyclesMap.forEach { (key, value) ->
                item(key) {
                    Text(
                        text = stringResource(value),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                cycle = key
                                showDialogPicker = false
                            }
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                    )
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
        }
    }
    if (showTransferDialogPicker) RangersDialogWithContent(
        headerId = R.string.transfer_campaign,
        isDarkTheme = isDarkTheme,
        onBack = { showTransferDialogPicker = false },
    ) {
        val campaignsForTransfer = campaignCreationViewModel.getTransferCampaigns(cycle, userInfo?.id).collectAsLazyPagingItems()
        LazyColumn(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
            if (campaignsForTransfer.itemCount == 0 && campaignsForTransfer.loadState.isIdle) item("no_results") {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.transfer_campaign_no_results),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.2.sp,
                    )
                }
            }
            items(
                count = campaignsForTransfer.itemCount,
                key = campaignsForTransfer.itemKey { it.id },
                contentType = campaignsForTransfer.itemContentType { it::class }
            ) { index ->
                val campaignForTransfer = campaignsForTransfer[index] ?: return@items
                Text(
                    text = campaignForTransfer.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            transferCampaignInfo = campaignForTransfer.id to campaignForTransfer.name
                            name = campaignForTransfer.name
                            showTransferDialogPicker = false
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                )
                HorizontalDivider(color = CustomTheme.colors.l10)
            }
        }
    }

    Column(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth().padding(horizontal = 8.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(text = buildAnnotatedString {
                        append(stringResource(R.string.name_label))
                        withStyle(style = SpanStyle(color = CustomTheme.colors.warn)) {
                            append("*")
                        }
                    })
                },
                placeholder = {
                    Text(text = stringResource(R.string.campaign_creation_name_placeholder))
                },
                textStyle = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                ),
                singleLine = true,
                shape = CustomTheme.shapes.small,
                colors = TextFieldDefaults.colors().copy(
                    focusedIndicatorColor = CustomTheme.colors.m,
                    unfocusedIndicatorColor = CustomTheme.colors.m,
                    unfocusedLabelColor = CustomTheme.colors.d30,
                    focusedLabelColor = CustomTheme.colors.d30,
                    disabledLabelColor = CustomTheme.colors.d30,
                    unfocusedPlaceholderColor = CustomTheme.colors.d30,
                    focusedPlaceholderColor = CustomTheme.colors.d30,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = CustomTheme.colors.m
                ),
                enabled = transferCampaignInfo.second.isEmpty()
            )
            DataPicker(
                onClick = { showDialogPicker = true },
                type = R.string.campaign
            ) {
                Text(
                    text = stringResource(if (cycle.isEmpty())
                        R.string.campaign_placeholder
                    else CampaignMaps.campaignCyclesMap[cycle]!!),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (cycle.isNotEmpty() && cycle != "core") DataPicker(
                onClick = { showTransferDialogPicker = true },
                type = R.string.transfer_campaign,
                isRequired = false
            ) {
                Text(
                    text = if (transferCampaignInfo.first.isEmpty()) stringResource(R.string.campaign_placeholder)
                        else transferCampaignInfo.second,
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!availableExpansions.isNullOrEmpty()) Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.campaign_expansions),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                )
                availableExpansions.forEach { expansion ->
                    val isAdded = expansions.contains(expansion.id)
                    RangersRadioButtonRow(
                        text = stringResource(expansion.name),
                        isSelected = isAdded,
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                        ),
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) { value ->
                        if (value) expansions.add(expansion.id)
                        else expansions.remove(expansion.id)
                    }
                }
            }
            if (userInfo?.id != null && transferCampaignInfo.first.isEmpty()) RangersRadioButtonRow(
                text = stringResource(R.string.upload_to_rangersdb),
                isSelected = isUploading,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) { value -> isUploading = value }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SquareButton(
                stringId = R.string.cancel_button,
                leadingIcon = R.drawable.close_32dp,
                onClick = onCancel,
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.warn),
                iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                modifier = Modifier.weight(1f)
            )
            SquareButton(
                stringId = R.string.create_campaign_button,
                leadingIcon = R.drawable.add_32dp,
                onClick = {
                    campaignCreationViewModel.createCampaign(
                        name = name,
                        cycleId = cycle,
                        isUploading = isUploading,
                        currentLocation = CampaignMaps.startingLocations[cycle]!!,
                        transferCampaignId = transferCampaignInfo.first,
                        expansions = expansions
                    )
                },
                buttonColor = ButtonDefaults.buttonColors().copy(
                    containerColor = CustomTheme.colors.d10,
                    disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
                ),
                iconColor = CustomTheme.colors.m,
                textColor = CustomTheme.colors.l30,
                modifier = Modifier.weight(1.1f),
                isEnabled = isLegit
            )
        }
    }
}