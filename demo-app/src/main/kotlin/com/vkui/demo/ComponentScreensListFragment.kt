package com.vkui.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vkui.demo.component.ButtonDemoFragment
import com.vkui.demo.component.ComponentScreenKey

class ComponentScreensListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = RecyclerView(requireContext()).apply {
        fitsSystemWindows = true
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        layoutManager = GridLayoutManager(context, SPAN_COUNT)
        adapter = ComponentScreensListAdapter(
            items = ComponentScreenKey.entries.toList(),
            onClick = ::onComponentButtonClicked
        )
        addItemDecoration(ComponentScreensListGapDecoration(SPAN_COUNT))
    }

    private fun onComponentButtonClicked(screenKey: ComponentScreenKey) {
        val fragment = when (screenKey) {
            ComponentScreenKey.BUTTON -> ButtonDemoFragment()
        }
        parentFragmentManager.commit {
            add(R.id.root_fragment_container, fragment)
            addToBackStack(null)
        }
    }

    companion object {
        private const val SPAN_COUNT = 2
    }
}
