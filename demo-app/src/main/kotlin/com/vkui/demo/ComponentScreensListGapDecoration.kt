package com.vkui.demo

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.vkui.view.resolveDimensionPixelOffset
import com.vkui.view.components.R as ComponentsR

class ComponentScreensListGapDecoration(
    private val spanCount: Int,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val gap = view.context.resolveDimensionPixelOffset(ComponentsR.attr.vk_ui_size_button_group_gap_small)
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = gap - column * gap / spanCount
        outRect.right = (column + 1) * gap / spanCount
        outRect.top = if (position < spanCount) gap else 0
        outRect.bottom = gap
    }
}
