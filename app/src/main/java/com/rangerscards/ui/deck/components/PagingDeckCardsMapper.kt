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
    data class CategoryHeader(val category: CardsHeaderType?, val value: String?) : DeckCardListUiModel
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

                val headerOptions = getHeaderOptions(
                    sortOrder = sortOrder,
                    previousItem = beforeItem,
                    nextItem = afterItem
                )

                if (headerOptions.first)
                    DeckCardListUiModel.CategoryHeader(
                        category = headerOptions.second,
                        value = when(headerOptions.second) {
                            CardsHeaderType.EQUIP -> beforeItem?.equip?.toString()
                            CardsHeaderType.SET_ID -> beforeItem?.setName
                            CardsHeaderType.TYPE_NAME -> beforeItem?.typeName
                            CardsHeaderType.COST -> beforeItem?.cost?.toString()
                            CardsHeaderType.ASPECT_ID -> beforeItem?.aspect?.shortName
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
): Pair<Boolean, com.rangerscards.ui.cards.components.CardsHeaderType?> {
    return when(sortOrder.first()) {
        "equip" -> {
            (previousItem?.equip != nextItem?.equip) to com.rangerscards.ui.cards.components.CardsHeaderType.EQUIP
        }
        "set_id" -> {
            (previousItem?.setName != nextItem?.setName) to com.rangerscards.ui.cards.components.CardsHeaderType.SET_ID
        }
        "set_type_id" -> {
            (if (sortOrder.indexOf("set_id") == 1) {
                previousItem?.setName != nextItem?.setName
            } else false) to com.rangerscards.ui.cards.components.CardsHeaderType.SET_ID
        }
        "type_name" -> {
            (previousItem?.typeName != nextItem?.typeName) to com.rangerscards.ui.cards.components.CardsHeaderType.TYPE_NAME
        }
        "cost" -> {
            (previousItem?.cost != nextItem?.cost) to com.rangerscards.ui.cards.components.CardsHeaderType.COST
        }
        "aspect_id" -> {
            (previousItem?.aspect?.id != nextItem?.aspect?.id) to com.rangerscards.ui.cards.components.CardsHeaderType.ASPECT_ID
        }
        else -> false to null
    }
}