package com.rangerscards.ui.campaign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rangerscards.R
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.settings.components.FriendListItem
import com.rangerscards.ui.settings.components.RangersBaseCard
import com.rangerscards.ui.theme.CustomTheme
import kotlinx.coroutines.launch

@Composable
fun AddPlayersToCampaign(
    navigateBack: () -> Unit,
    campaignViewModel: CampaignViewModel,
    userState: UserUIState,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val campaign = campaignViewModel.campaign.collectAsState()
    val friendsInCampaign = campaign.value!!.access.filter { it.key != userState.currentUser?.uid || it.key != campaign.value!!.userId }
    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
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
    LazyColumn(
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val userProfile = userState.userInfo?.profile?.userProfile
        items(userProfile?.friends ?: emptyList(), { friend -> friend.user?.id.toString() }) { friend ->
            val isInCampaign = friendsInCampaign.containsKey(friend.user?.id)
            FriendListItem(
                handle = friend.user?.userInfo?.handle ?: "",
                isToAdd = !isInCampaign,
                onClick = { if (!isInCampaign) coroutine.launch { showLoadingDialog = true
                    campaignViewModel.addFriendToCampaign(userState.currentUser, friend.user?.id.toString())
                }.invokeOnCompletion { showLoadingDialog = false
                    navigateBack.invoke()
                } else coroutine.launch { showLoadingDialog = true
                    campaignViewModel.removeFriendFromCampaign(userState.currentUser, friend.user?.id.toString())
                }.invokeOnCompletion { showLoadingDialog = false
                    navigateBack.invoke()
                } }
            )
            HorizontalDivider(color = CustomTheme.colors.l10)
        }
    }
}