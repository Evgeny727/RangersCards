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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.R
import com.rangerscards.ui.cards.components.FullCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch

@Composable
fun CampaignRewardFullScreen(
    campaignViewModel: CampaignViewModel,
    cardIndex: Int,
    query: String,
    user: FirebaseUser?,
    isDarkTheme: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val campaignState by campaignViewModel.campaign.collectAsState()
    val rewards = campaignViewModel.getRewardsCards(query).collectAsState(emptyList())
    val pagerState = rememberPagerState(initialPage = cardIndex) { rewards.value.size }
    val coroutine = rememberCoroutineScope()
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) { page ->
        val cardCode = rewards.value[page].code
        var isAdded by remember { mutableStateOf(campaignState?.rewards?.contains(cardCode) ?: false) }
        var currentIsAdded by remember { mutableIntStateOf(if (isAdded) 2 else 0) }
        val fullCard by campaignViewModel.getRewardById(cardCode).collectAsState(null)
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
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    item {
                        FullCard(
                            tabooId = fullCard!!.tabooId,
                            aspectId = fullCard!!.aspectId,
                            aspectShortName = fullCard!!.aspectShortName,
                            cost = fullCard!!.cost,
                            imageSrc = fullCard!!.imageSrc,
                            realImageSrc = fullCard!!.realImageSrc,
                            name = fullCard!!.name,
                            presence = fullCard!!.presence,
                            approachConflict = fullCard!!.approachConflict,
                            approachReason = fullCard!!.approachReason,
                            approachExploration = fullCard!!.approachExploration,
                            approachConnection = fullCard!!.approachConnection,
                            typeName = fullCard!!.typeName,
                            typeId = fullCard!!.typeId,
                            traits = fullCard!!.traits,
                            equip = fullCard!!.equip,
                            harm = fullCard!!.harm,
                            progress = fullCard!!.progress,
                            tokenPlurals = fullCard!!.tokenPlurals,
                            tokenCount = fullCard!!.tokenCount,
                            text = fullCard!!.text,
                            flavor = fullCard!!.flavor,
                            level = fullCard!!.level,
                            setName = fullCard!!.setName,
                            setSize = fullCard!!.setSize,
                            setPosition = fullCard!!.setPosition,
                            subsetSize = fullCard!!.subsetSize,
                            subsetPosition = fullCard!!.subsetPosition,
                            packShortName = fullCard!!.packShortName,
                            sunChallenge = fullCard!!.sunChallenge,
                            mountainChallenge = fullCard!!.mountainChallenge,
                            crestChallenge = fullCard!!.crestChallenge,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
                // Overlay custom FABs in the bottom-end corner
                Row(
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
                            onClick = { coroutine.launch {
                                campaignViewModel.removeCampaignReward(cardCode, user)
                            }.invokeOnCompletion { currentIsAdded = 0
                                isAdded = !isAdded
                            } },
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
                            onClick = { coroutine.launch { currentIsAdded = 2
                                campaignViewModel.addCampaignReward(cardCode, user)
                            }.invokeOnCompletion { currentIsAdded = 2
                                isAdded = !isAdded
                            } },
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