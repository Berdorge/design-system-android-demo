package com.vkui.demo

import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vkui.demo.component.ComponentScreenKey
import com.vkui.view.resolveColor
import com.vkui.view.resolveDimensionPixelSize
import com.vkui.view.resolveTextAppearance
import com.vkui.view.components.R as ComponentsR

class ComponentScreensListAdapter(
    private val items: List<ComponentScreenKey>,
    private val onClick: (ComponentScreenKey) -> Unit,
) : RecyclerView.Adapter<ComponentScreensListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val textView = TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            minimumHeight = 100

            gravity = Gravity.CENTER
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER

            setBackgroundResource(R.drawable.background_button_component)
            setTextAppearance(context.resolveTextAppearance(ComponentsR.attr.vk_ui_font_caption1))
            setTextColor(context.resolveColor(ComponentsR.attr.vk_ui_color_button_text))
        }
        return ViewHolder(textView, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        private val textView: TextView,
        private val onClick: (ComponentScreenKey) -> Unit
    ) : RecyclerView.ViewHolder(textView) {
        fun onBind(item: ComponentScreenKey) {
            textView.text = getComponentName(item)
            textView.setOnClickListener {
                onClick(item)
            }
        }
    }

    companion object {
        private fun getComponentName(screenKey: ComponentScreenKey): String = when (screenKey) {
//            ComponentScreenKey.ALERT -> "Alert"
            ComponentScreenKey.BUTTON -> "Button"
//            ComponentScreenKey.INPUT -> "Input"
//            ComponentScreenKey.SWITCH -> "Switch"
//            ComponentScreenKey.TEXT -> "Text"
        }
    }
}
