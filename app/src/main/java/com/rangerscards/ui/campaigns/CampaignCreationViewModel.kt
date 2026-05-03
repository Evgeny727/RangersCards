package com.rangerscards.ui.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.rangerscards.UiErrorState
import com.rangerscards.domain.model.CampaignListItem
import com.rangerscards.domain.repository.CampaignsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

sealed interface CampaignCreationUiState {
    object Idle : CampaignCreationUiState
    object Loading : CampaignCreationUiState
    data class Success(val campaignId: String) : CampaignCreationUiState
    object Error : CampaignCreationUiState
}

@HiltViewModel
class CampaignCreationViewModel @Inject constructor(
    private val campaignsRepository: CampaignsRepository,
) : ViewModel() {
    private val _campaignCreationUiState = MutableStateFlow<CampaignCreationUiState>(CampaignCreationUiState.Idle)
    val campaignCreationUiState: StateFlow<CampaignCreationUiState> = _campaignCreationUiState.asStateFlow()

    private val _events = MutableSharedFlow<UiErrorState>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiErrorState> = _events

    private fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    fun getTransferCampaigns(cycleId: String, userId: String?): Flow<PagingData<CampaignListItem>> =
        campaignsRepository.getAllPaginatedCampaignsForTransferFlow(cycleId, userId ?: "")

    @OptIn(ExperimentalUuidApi::class)
    fun createCampaign(
        name: String,
        cycleId: String,
        currentLocation: String,
        isUploading: Boolean,
        transferCampaignId: String,
        expansions: List<String>
    ) {
        viewModelScope.launch {
            _campaignCreationUiState.value = CampaignCreationUiState.Loading
            campaignsRepository.createCampaign(
                uploaded = isUploading,
                name = name,
                cycleId = cycleId,
                currentLocation = currentLocation,
                expansions = expansions,
                transferCampaignId = transferCampaignId.ifEmpty { null }
            ).onFailure {
                emitError(it)
                _campaignCreationUiState.value = CampaignCreationUiState.Error
            }.onSuccess {
                _campaignCreationUiState.value = CampaignCreationUiState.Success(it)
            }
        }
    }
}