package com.brinkmc.plop.shared.base.gui

import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.util.PaginationContext
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.Element
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ContainerInterfaceBuilder
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.inventory.ItemStack

internal interface PaginatedGui<T> : Gui {

    private suspend fun getNextPageButton(): ItemStack {
        return messages.getItem(
            ItemKey.NEXT_PAGE,
            MessageKey.MENU_NEXT_PAGE_NAME,
            MessageKey.MENU_NEXT_PAGE_DESC,
        )
    }

    private suspend fun getPrevPageButton(): ItemStack {
        return messages.getItem(
            ItemKey.PREV_PAGE,
            MessageKey.MENU_PREV_PAGE_NAME,
            MessageKey.MENU_PREV_PAGE_DESC,
        )
    }


    suspend fun ContainerInterfaceBuilder.Simple.setupPagedGrid(
        context: PaginationContext<T>,
        slotMapper: suspend (T, InterfaceView) -> Element
    ) {
        withTransform { pane, view ->
            val chunks = context.items.chunked(context.pageSize)
            val currentVisible = chunks.getOrNull(context.currentPage) ?: emptyList()

            currentVisible.forEachIndexed { index, data ->
                // Calculate the row relative to the starting row in the context
                val row = context.rowRange.first + (index / 9)
                val col = index % 9
                pane[row, col] = slotMapper(data, view)
            }
        }
        setupNextPageButton(context, context.rowRange.last, 8)
        setupPrevPageButton(context, context.rowRange.last, 0)
    }

    suspend fun ContainerInterfaceBuilder.Simple.setupNextPageButton(
        context: PaginationContext<T>,
        row: Int, col: Int
    ) {
        withTransform { pane, view ->
            val totalPages = (context.items.size + context.pageSize - 1) / context.pageSize

            if (context.currentPage < totalPages - 1) {
                pane[row, col] = StaticElement(drawable(getNextPageButton())) {
                    context.nextPage()
                    view.redrawComplete()
                }
            }
        }
    }

    suspend fun ContainerInterfaceBuilder.Simple.setupPrevPageButton(
        context: PaginationContext<T>,
        row: Int, col: Int
    ) {
        withTransform { pane, view ->
            if (context.currentPage > 0) {
                pane[row, col] = StaticElement(drawable(getPrevPageButton())) {
                    context.prevPage()
                    view.redrawComplete()
                }
            }
        }
    }
}