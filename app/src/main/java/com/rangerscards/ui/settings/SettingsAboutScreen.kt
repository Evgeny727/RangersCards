package com.rangerscards.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import com.rangerscards.utils.applyScaffoldPaddings
import com.rangerscards.utils.openEmail

const val email = "rangerscardsapp@gmail.com"
const val telegramIcon = "Telegram App"
const val discordIcon = "Discord New"
const val icons8 = "Icons8"

@Composable
fun SettingsAboutScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val emailText = stringResource(R.string.about_email_text, email)
    val annotatedText = buildAnnotatedString {
        // Find where the email appears in the full text.
        val startIndex = emailText.indexOf(email)
        // Append text before the email.
        append(emailText.substring(0, startIndex))
        // Mark the email part as clickable by pushing an annotation.
        pushStringAnnotation(tag = "URL", annotation = email)
        // Optionally, style the email to look like a link.
        withStyle(
            style = SpanStyle(
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(email)
        }
        pop() // Remove the annotation.
        // Append text after the email.
        append(emailText.substring(startIndex + email.length))
    }

    // Get the current context for launching intents.
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .applyScaffoldPaddings(contentPadding),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.about_text),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp,
            )
        }
        item {
            var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            BasicText(
                text = annotatedText,
                // Capture the layout result.
                onTextLayout = { layoutResult ->
                    textLayoutResult = layoutResult
                },
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures { tapOffset: Offset ->
                        // When a tap occurs, use the layout result to get the character offset.
                        textLayoutResult?.let { layoutResult ->
                            // Convert tap coordinates into an offset within the text.
                            val tappedOffset = layoutResult.getOffsetForPosition(tapOffset)
                            // Check if that offset is within our annotation range.
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = tappedOffset,
                                end = tappedOffset
                            ).firstOrNull()?.let { annotation ->
                                // When the email is tapped, open the email client.
                                context.openEmail(annotation.item)
                            }
                        }
                    }
                },
                style = TextStyle(
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.2.sp,
                )
            )
        }
        item {
            Text(
                text = stringResource(R.string.about_rangersdb_text),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp,
            )
        }
        item {
            var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.icon_attribution),
                    color = CustomTheme.colors.d30,
                    fontFamily = Jost,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.2.sp,
                )
                Row {
                    // The bullet/dot on the left.
                    Text(
                        text = "\u2022",  // Unicode for bullet
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    val telegramIconText = buildAnnotatedString {
                        pushStringAnnotation(tag = "URL1", annotation = "https://icons8.com/icon/85466/telegram-app")
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(telegramIcon)
                        }
                        pop() // Remove the annotation.

                        // Append text after the email.
                        append(" icon by ")
                        pushStringAnnotation(tag = "URL2", annotation = "https://icons8.com/")
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(icons8)
                        }
                        pop()
                    }
                    BasicText(
                        text = telegramIconText,
                        // Capture the layout result.
                        onTextLayout = { layoutResult ->
                            textLayoutResult = layoutResult
                        },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures { tapOffset: Offset ->
                                // When a tap occurs, use the layout result to get the character offset.
                                textLayoutResult?.let { layoutResult ->
                                    // Convert tap coordinates into an offset within the text.
                                    val tappedOffset = layoutResult.getOffsetForPosition(tapOffset)
                                    // Check if that offset is within our annotation range.
                                    telegramIconText.getStringAnnotations(
                                        tag = "URL1",
                                        start = tappedOffset,
                                        end = tappedOffset
                                    ).firstOrNull()?.let { annotation ->
                                        // When the email is tapped, open the email client.
                                        val uri = annotation.item.toUri()
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    }
                                    telegramIconText.getStringAnnotations(
                                        tag = "URL2",
                                        start = tappedOffset,
                                        end = tappedOffset
                                    ).firstOrNull()?.let { annotation ->
                                        // When the email is tapped, open the email client.
                                        val uri = annotation.item.toUri()
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        },
                        style = TextStyle(
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            letterSpacing = 0.2.sp,
                        )
                    )
                }
                Row {
                    // The bullet/dot on the left.
                    Text(
                        text = "\u2022",  // Unicode for bullet
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    val discordIconText = buildAnnotatedString {
                        pushStringAnnotation(tag = "URL1", annotation = "https://icons8.com/icon/pF4jOjHaKg59/discord-new")
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(discordIcon)
                        }
                        pop() // Remove the annotation.

                        // Append text after the email.
                        append(" icon by ")
                        pushStringAnnotation(tag = "URL2", annotation = "https://icons8.com/")
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(icons8)
                        }
                        pop()
                    }
                    BasicText(
                        text = discordIconText,
                        // Capture the layout result.
                        onTextLayout = { layoutResult ->
                            textLayoutResult = layoutResult
                        },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures { tapOffset: Offset ->
                                // When a tap occurs, use the layout result to get the character offset.
                                textLayoutResult?.let { layoutResult ->
                                    // Convert tap coordinates into an offset within the text.
                                    val tappedOffset = layoutResult.getOffsetForPosition(tapOffset)
                                    // Check if that offset is within our annotation range.
                                    discordIconText.getStringAnnotations(
                                        tag = "URL1",
                                        start = tappedOffset,
                                        end = tappedOffset
                                    ).firstOrNull()?.let { annotation ->
                                        // When the email is tapped, open the email client.
                                        val uri = annotation.item.toUri()
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    }
                                    discordIconText.getStringAnnotations(
                                        tag = "URL2",
                                        start = tappedOffset,
                                        end = tappedOffset
                                    ).firstOrNull()?.let { annotation ->
                                        // When the email is tapped, open the email client.
                                        val uri = annotation.item.toUri()
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        },
                        style = TextStyle(
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            letterSpacing = 0.2.sp,
                        )
                    )
                }
            }
        }
    }
}