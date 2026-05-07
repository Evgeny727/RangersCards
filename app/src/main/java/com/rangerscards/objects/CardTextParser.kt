package com.rangerscards.objects

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.ui.theme.CustomTheme

object CardTextParser {
    /**
     * Recursively parses a string with custom markup.
     *
     * @param text The full text to parse.
     * @param start The starting index.
     * @param endTag If non-null, indicates the closing tag (for example, "</b>")
     * that ends this segment.
     *
     * @return A Pair of the parsed AnnotatedString and the new index after processing.
     */
    @Composable
    private fun parseSegment(
        text: String?,
        start: Int,
        endTag: String? = null,
        aspectId: String?
    ): Pair<AnnotatedString, Int> {
        val builder = AnnotatedString.Builder()
        var index = start

        if (text == null) return Pair(builder.toAnnotatedString(), index)

        while (index < text.length) {
            // If an expected closing tag is found at the current index, finish this segment.
            if (endTag != null && text.startsWith(endTag, index)) {
                return Pair(builder.toAnnotatedString(), index + endTag.length)
            }

            when (val currentChar = text[index]) {
                '<' -> {
                    // Check if this is a closing tag.
                    if (index + 1 < text.length && text[index + 1] == '/') {
                        // Find the full closing tag.
                        val tagEnd = text.indexOf('>', index)
                        if (tagEnd == -1) {
                            // Malformed tag; append the rest as literal.
                            builder.append(text.substring(index))
                            index = text.length
                        } else {
                            val closingTag = text.substring(index, tagEnd + 1)
                            // If this closing tag matches our expected endTag, return.
                            if (endTag != null && closingTag == endTag) {
                                return Pair(builder.toAnnotatedString(), tagEnd + 1)
                            } else {
                                // Otherwise, treat it as literal text.
                                builder.append(closingTag)
                                index = tagEnd + 1
                            }
                        }
                    } else {
                        // This is an opening tag. Look for the closing '>'.
                        val tagEnd = text.indexOf('>', index)
                        if (tagEnd == -1) {
                            // Malformed tag; append the rest as literal.
                            builder.append(text.substring(index))
                            index = text.length
                        } else {
                            // Extract the tag name (for example, "b" in "<b>").
                            val tagName = text.substring(index + 1, tagEnd)
                            // Define the corresponding closing marker.
                            val closingMarker = "</$tagName>"
                            // Advance index past the opening tag.
                            index = tagEnd + 1
                            // Recursively parse the content inside this tag.
                            val (innerAnnotated, newIndex) = parseSegment(
                                text, index, closingMarker, aspectId
                            )
                            index = newIndex
                            // Choose a style based on the tag name.
                            val spanStyle = when (tagName) {
                                "b" -> SpanStyle(fontWeight = FontWeight.Bold)
                                "i" -> SpanStyle(fontStyle = FontStyle.Italic)
                                "f" -> SpanStyle(fontStyle = FontStyle.Italic, color = CustomTheme.colors.d10)
                                else -> null
                            }
                            if (spanStyle != null) {
                                builder.withStyle(spanStyle) {
                                    append(innerAnnotated)
                                }
                            } else {
                                // If the tag is not recognized, just append its content unstyled.
                                builder.append(innerAnnotated)
                            }
                        }
                    }
                }
                '[' -> {
                    // Check if this is a double square bracket case.
                    if (index + 1 < text.length && text[index + 1] == '[') {
                        // Find the matching closing "]]"
                        val endDoubleBracket = text.indexOf("]]", index)
                        if (endDoubleBracket != -1) {
                            val content = text.substring(index + 2, endDoubleBracket)
                            // Apply a text shadow style.
                            builder.withStyle(
                                SpanStyle(
                                    shadow = Shadow(
                                        color = when(aspectId) {
                                            "AWA" -> CustomTheme.colors.green
                                            "FIT" -> CustomTheme.colors.red
                                            "FOC" -> CustomTheme.colors.blue
                                            "SPI" -> CustomTheme.colors.orange
                                            else -> CustomTheme.colors.m
                                        },
                                        blurRadius = 2f
                                    )
                                )
                            ) {
                                append(content)
                            }
                            index = endDoubleBracket + 2
                            continue
                        } else {
                            // No matching closing brackets; treat as literal.
                            builder.append(currentChar)
                            index++
                        }
                    } else {
                        // Process content within square brackets.
                        val endBracket = text.indexOf(']', index)
                        if (endBracket != -1) {
                            when (val key = text.substring(index + 1, endBracket)) {
                                "AWA" -> builder.withStyle(
                                    SpanStyle(
                                        color = CustomTheme.colors.green,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.awa_styled_card_text))
                                }
                                "FIT" -> builder.withStyle(
                                    SpanStyle(
                                        color = CustomTheme.colors.red,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.fit_styled_card_text))
                                }
                                "FOC" -> builder.withStyle(
                                    SpanStyle(
                                        color = CustomTheme.colors.blue,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.foc_styled_card_text))
                                }
                                "SPI" -> builder.withStyle(
                                    SpanStyle(
                                        color = CustomTheme.colors.orange,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.spi_styled_card_text))
                                }
                                else -> builder.appendInlineContent(key, "[$key]")
                            }
                            index = endBracket + 1
                            continue
                        } else {
                            // No closing bracket found; treat as literal.
                            builder.append(currentChar)
                            index++
                        }
                    }
                }
                else -> {
                    // Append regular text until the next special character.
                    val nextSpecial = listOf(
                        text.indexOf('<', index).takeIf { it != -1 } ?: text.length,
                        text.indexOf('[', index).takeIf { it != -1 } ?: text.length
                    ).minOrNull() ?: text.length
                    builder.append(text.substring(index, nextSpecial))
                    index = nextSpecial
                }
            }
        }
        return Pair(builder.toAnnotatedString(), index)
    }

    /**
     * Parses the entire string and returns an AnnotatedString.
     *
     * This is the function you would call from your composable.
     */
    @Composable
    fun parseCustomText(rawText: String?, aspectId: String?): AnnotatedString {
        return parseSegment(rawText, 0, endTag = null, aspectId).first
    }

    fun inlineIconsMap(color: Color): Map<String, InlineTextContent> {
        return mapOf(
            "conflict" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.conflict),
                    contentDescription = "Conflict Icon",
                    tint = color
                )
            },
            "reason" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.reason),
                    contentDescription = "Reason Icon",
                    tint = color
                )
            },
            "exploration" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.exploration),
                    contentDescription = "Exploration Icon",
                    tint = color
                )
            },
            "connection" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.connection),
                    contentDescription = "Connection Icon",
                    tint = color
                )
            },
            "harm" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.harm),
                    contentDescription = "Harm Icon",
                    tint = color
                )
            },
            "progress" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.progress),
                    contentDescription = "Progress Icon",
                    tint = color
                )
            },
            "ranger" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ranger),
                    contentDescription = "Ranger Icon",
                    tint = color
                )
            },
            "sun" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.sun),
                    contentDescription = "Sun Icon",
                    tint = color
                )
            },
            "mountain" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.mountain),
                    contentDescription = "Mountain Icon",
                    tint = color
                )
            },
            "crest" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.crest),
                    contentDescription = "Crest Icon",
                    tint = color
                )
            },
            "aspiration" to InlineTextContent(
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.aspiration),
                    contentDescription = "Aspiration Icon",
                    tint = color
                )
            },
            "guide" to InlineTextContent(
                Placeholder(
                    width = 22.sp,
                    height = 18.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.guide),
                    contentDescription = "Guide Icon",
                    tint = color,
                    modifier = Modifier.fillMaxSize()
                )
            },
        )
    }
}