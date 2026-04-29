package com.rangerscards.ui.campaign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.User
import com.rangerscards.ui.components.RangersLoadingDialog
import com.rangerscards.ui.settings.components.FriendListItem
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.utils.applyScaffoldPaddings

@Composable
fun AddPlayersToCampaign(
    campaign: Campaign,
    campaignUiState: CampaignUiState,
    addFriend: (String) -> Unit,
    removeFriend: (String) -> Unit,
    user: User,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val friendsInCampaign = remember(campaign.access) {
        campaign.access.filter { it.id != user.userInfo?.id || it.id != campaign.userId }
    }
    if (campaignUiState is CampaignUiState.Loading) RangersLoadingDialog(isDarkTheme = isDarkTheme)
    LazyColumn(
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(user.friends, { friend -> friend.id }) { friend ->
            val isInCampaign = remember(friendsInCampaign) {
                friendsInCampaign.firstOrNull { it.id == friend.id } != null
            }
            FriendListItem(
                handle = friend.handle ?: "",
                isToAdd = !isInCampaign,
                onClick = { if (!isInCampaign) addFriend(friend.id)
                    else removeFriend(friend.id) },
            )
            HorizontalDivider(color = CustomTheme.colors.l10)
        }
    }
}