package com.rangerscards.ui.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rangerscards.AppViewModel
import com.rangerscards.R
import com.rangerscards.ui.components.RangersSearchOutlinedField
import com.rangerscards.ui.components.RowTypeDivider
import com.rangerscards.ui.settings.components.FriendListItem
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
fun SettingsFriendsScreen(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    friendsViewModel: FriendsViewModel = hiltViewModel(),
) {
    val user by appViewModel.userUiState.collectAsState()
    val searchQuery by friendsViewModel.searchQuery.collectAsState()
    val searchResults by friendsViewModel.searchResults.collectAsState()
    val friendsUiState by friendsViewModel.friendsUiState.collectAsState()
    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery}
            .debounce(400)
            .distinctUntilChanged()
            .collectLatest { query ->
                if (query.trim().length >= 2) friendsViewModel.getUsersByHandle(query.trim())
                else if (query.trim().isEmpty()) friendsViewModel.getUsersByHandle("")
            }

        snapshotFlow { user }
            .distinctUntilChanged()
            .collectLatest { friendsViewModel.filterUsers(it) }

        friendsViewModel.events.collect { error ->
            appViewModel.emitError(error.exception)
        }
    }

    Column(
        modifier = modifier
            .background(CustomTheme.colors.l20)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
    ) {
        RangersSearchOutlinedField(
            query = searchQuery,
            placeholder = R.string.search_friends,
            onQueryChanged = friendsViewModel::onSearchQueryChanged,
            onClearClicked = friendsViewModel::clearSearchQuery
        )
        if (friendsUiState is FriendsUiState.Loading) Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = CustomTheme.colors.m)
        }
        LazyColumn(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stickyHeader("friends_header") {
                RowTypeDivider(text = stringResource(R.string.friends_amount_header))
            }
            if (user.friends.isNotEmpty()) {
                items(
                    items = user.friends,
                    key = { it.id }
                ) { friend ->
                    FriendListItem(
                        handle = friend.handle ?: "",
                        isToAdd = false,
                        onClick = {
                            friendsViewModel.rejectFriendRequest(friend.id)
                        }
                    )
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            stickyHeader("sent_requests_header") {
                RowTypeDivider(text = stringResource(R.string.sent_requests))
            }
            if (user.sentRequests.isNotEmpty()) {
                items(
                    items = user.sentRequests,
                    key = { it.id }
                ) { friend ->
                    FriendListItem(
                        handle = friend.handle ?: "",
                        isToAdd = false,
                        onClick = {
                            friendsViewModel.rejectFriendRequest(friend.id)
                        }
                    )
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            stickyHeader("received_requests_header") {
                RowTypeDivider(text = stringResource(R.string.received_requests))
            }
            if (user.receivedRequests.isNotEmpty()) {
                items(
                    items = user.receivedRequests,
                    key = { it.id }
                ) { friend ->
                    FriendListItem(
                        handle = friend.handle ?: "",
                        isToAdd = false,
                        onClick = {
                            friendsViewModel.acceptFriendRequest(friend.id)
                        }
                    ) {
                        friendsViewModel.rejectFriendRequest(friend.id)
                    }
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            }
            stickyHeader("search_results_header") {
                RowTypeDivider(text = stringResource(R.string.search_results))
            }
            if (searchResults.isNotEmpty()) {
                items(
                    items = searchResults,
                    key = { it.id }
                ) { result ->
                    FriendListItem(
                        handle = result.handle ?: "",
                        isToAdd = true,
                        onClick = {
                            friendsViewModel.sendFriendRequest(result.id)
                        }
                    )
                    HorizontalDivider(color = CustomTheme.colors.l10)
                }
            } else if (searchQuery.trim().isNotEmpty() && searchQuery.trim().length >= 2) {
                item("no_results_item") {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.no_matching_results, searchQuery),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 0.2.sp,
                        )
                    }
                }
            }
        }
    }
}