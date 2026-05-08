package com.rangerscards.ui.campaign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.cards.components.FullCard
import com.rangerscards.ui.components.RangersLoadingDialog
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings

@Composable
fun CampaignRewardFullScreen(
    campaignViewModel: CampaignViewModel,
    cardIndex: Int,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val campaignUiState by campaignViewModel.campaignUiState.collectAsState()
    val campaignState by campaignViewModel.campaign.collectAsState()
    val rewards by campaignViewModel.rewards.collectAsState()
    val pagerState = rememberPagerState(initialPage = cardIndex) { rewards.size }
    val isViewOnly by campaignViewModel.isViewOnly.collectAsState()
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
        key = { page -> rewards[page].id }
    ) { page ->
        if (campaignUiState is CampaignUiState.Loading) RangersLoadingDialog(isDarkTheme = isDarkTheme)
        val cardCode = rewards[page].code
        val isAdded = remember(campaignState?.rewards) { campaignState?.rewards?.contains(cardCode) ?: false }
        val currentIsAdded = remember(isAdded) { if (isAdded) 2 else 0 }
        val fullCard by campaignViewModel.getRewardByCode(cardCode).collectAsState(null)
        Box(modifier = Modifier.fillMaxSize()) {
            if (fullCard == null) Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = CustomTheme.colors.m
                )
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    FullCard(
                        tabooId = fullCard!!.tabooId,
                        aspect = fullCard!!.aspect,
                        cost = fullCard!!.cost,
                        image = fullCard!!.image,
                        name = fullCard!!.name,
                        presence = fullCard!!.presence,
                        approaches = fullCard!!.approaches,
                        type = fullCard!!.type,
                        traits = fullCard!!.traits,
                        equip = fullCard!!.equip,
                        harm = fullCard!!.harm,
                        progress = fullCard!!.progress,
                        tokens = fullCard!!.tokens,
                        text = fullCard!!.text,
                        flavor = fullCard!!.flavor,
                        level = fullCard!!.level,
                        set = fullCard!!.set,
                        subset = fullCard!!.subset,
                        packShortName = fullCard!!.packShortName,
                        challenges = fullCard!!.challenges,
                        isDarkTheme = isDarkTheme
                    )
                }
                // Overlay custom FABs in the bottom-end corner
                if (!isViewOnly) Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .height(IntrinsicSize.Max)
                ) {
                    Row(
                        modifier = Modifier.height(62.dp)
                            .background(CustomTheme.colors.d30, CustomTheme.shapes.circle)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { campaignViewModel.removeCampaignReward(cardCode) },
                            colors = IconButtonDefaults.iconButtonColors()
                                .copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(32.dp),
                            enabled = isAdded
                        ) {
                            Icon(
                                painterResource(id = R.drawable.remove_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                            color = CustomTheme.colors.l10,
                            shape = CustomTheme.shapes.small,
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 6.dp)
                                    .sizeIn(minWidth = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "×$currentIsAdded",
                                    color = CustomTheme.colors.d10,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                        IconButton(
                            onClick = { campaignViewModel.addCampaignReward(cardCode) },
                            colors = IconButtonDefaults.iconButtonColors()
                                .copy(containerColor = Color.Transparent),
                            modifier = Modifier.size(32.dp),
                            enabled = !isAdded
                        ) {
                            Icon(
                                painterResource(id = R.drawable.add_32dp),
                                contentDescription = null,
                                tint = CustomTheme.colors.m,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}