package com.rangerscards.ui.deck.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.objects.DeckErrorsMap
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.collections.immutable.ImmutableList

@Composable
fun FullDeckProblemsItem(
    problems: ImmutableList<String>
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val errorsMap = DeckErrorsMap.deckErrorsMap()
    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().animateContentSize().clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val iconId = "problem"
            BasicText(
                text = buildAnnotatedString {
                    appendInlineContent(
                        iconId,
                        "[$iconId]"
                    )
                    append(" ${
                        stringResource(
                            errorsMap[problems[0]]!!
                        )
                    } ")
                    if (problems.size > 1) {
                        if (isExpanded) {
                            problems.forEachIndexed { index, problem ->
                                if (index != 0) {
                                    append(" ${
                                        stringResource(
                                            errorsMap[problem]!!
                                        )
                                    } ")
                                }
                            }
                        } else append(
                            pluralStringResource(
                                R.plurals.more_problems,
                                problems.size - 1,
                                problems.size - 1
                            )
                        )
                    }
                },
                inlineContent = mapOf(
                    "problem" to InlineTextContent(
                        Placeholder(
                            width = 16.sp,
                            height = 16.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.error_32dp),
                            contentDescription = "Error Icon",
                            tint = CustomTheme.colors.warn
                        )
                    },
                ),
                style = TextStyle(
                    color = CustomTheme.colors.warn,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                ),
                modifier = Modifier.weight(1f)
            )
            if (problems.size > 1) Icon(
                painterResource(if (isExpanded) R.drawable.arrow_drop_up_32dp
                else R.drawable.arrow_drop_down_32dp),
                contentDescription = null,
                tint = CustomTheme.colors.m,
                modifier = Modifier.size(32.dp)
            )
        }
        HorizontalDivider(color = CustomTheme.colors.l10)
    }
}