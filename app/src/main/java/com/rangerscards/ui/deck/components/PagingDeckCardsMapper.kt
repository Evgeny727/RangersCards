package com.rangerscards.ui.deck.components

import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.ui.cards.components.CardsHeaderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface DeckCardListUiModel {
    data class CardItem(val card: CardDeckListItem) : DeckCardListUiModel
    data class CategoryHeader(val key: String, val category: CardsHeaderType?, val value: String?) : DeckCardListUiModel
}

internal fun Flow<PagingData<CardDeckListItem>>.withCategoryHeaders(sortOrder: List<String>): Flow<PagingData<DeckCardListUiModel>> =
    map { pagingData ->
        pagingData
            .map { item ->
                DeckCardListUiModel.CardItem(item)
            }
            .insertSeparators { before, after ->
                val beforeItem = before?.card
                val afterItem = after?.card

                if (afterItem == null) return@insertSeparators null

                val headerOptions = getHeaderOptions(
                    sortOrder = sortOrder,
                    previousItem = beforeItem,
                    nextItem = afterItem
                )

                if (headerOptions.first)
                    DeckCardListUiModel.CategoryHeader(
                        key = "header_before_${afterItem.id}",
                        category = headerOptions.second,
                        value = when(headerOptions.second) {
                            CardsHeaderType.EQUIP -> afterItem.equip?.toString()
                            CardsHeaderType.SET_ID -> afterItem.setName
                            CardsHeaderType.TYPE_NAME -> afterItem.typeName
                            CardsHeaderType.COST -> afterItem.cost?.toString()
                            CardsHeaderType.ASPECT_ID -> afterItem.aspect?.shortName
                            else -> null
                        }
                    )
                else null
            }
    }

private fun getHeaderOptions(
    sortOrder: List<String>,
    previousItem: CardDeckListItem?,
    nextItem: CardDeckListItem?
): Pair<Boolean, CardsHeaderType?> {
    return when(sortOrder.first()) {
        "equip" -> {
            (previousItem?.equip != nextItem?.equip) to CardsHeaderType.EQUIP
        }
        "set_id" -> {
            (previousItem?.setName != nextItem?.setName) to CardsHeaderType.SET_ID
        }
        "set_type_id" -> {
            (if (sortOrder.indexOf("set_id") == 1) {
                previousItem?.setName != nextItem?.setName
            } else false) to CardsHeaderType.SET_ID
        }
        "type_name" -> {
            (previousItem?.typeName != nextItem?.typeName) to CardsHeaderType.TYPE_NAME
        }
        "cost" -> {
            (previousItem?.cost != nextItem?.cost) to CardsHeaderType.COST
        }
        "aspect_id" -> {
            (previousItem?.aspect?.id != nextItem?.aspect?.id) to CardsHeaderType.ASPECT_ID
        }
        else -> false to null
    }
}