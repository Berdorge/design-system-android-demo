package com.vkui.demo.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import com.vkui.demo.R
import com.vkui.demo.databinding.FragmentButtonDemoBinding

class ButtonDemoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentButtonDemoBinding.inflate(inflater, container, false)
        binding.button.setOnClickListener {
            Toast.makeText(context, "Нажатие на кнопку", Toast.LENGTH_SHORT).show()
        }
        binding.button.updateLayoutParams<MarginLayoutParams> {
            updateMargins(
                bottom = with(requireContext().resources) {
                    getDimensionPixelSize(R.dimen.example_dimen) +
                        getDimensionPixelSize(R.dimen.example_dimen)
                }
            )
        }
        return binding.root
    }
}
