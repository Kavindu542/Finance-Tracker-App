package com.example.financeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.financeapp.R

class OnboardingScreenFragment : Fragment() {

    companion object {
        private const val ARG_LAYOUT_ID = "layout_id"
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"

        fun newInstance(layoutId: Int, title: String, description: String): OnboardingScreenFragment {
            val fragment = OnboardingScreenFragment()
            val args = Bundle().apply {
                putInt(ARG_LAYOUT_ID, layoutId)
                putString(ARG_TITLE, title)
                putString(ARG_DESCRIPTION, description)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = arguments?.getInt(ARG_LAYOUT_ID) ?: R.layout.onboarding_screen_1
        val view = inflater.inflate(layoutId, container, false)

        // Update title and description
        val title = arguments?.getString(ARG_TITLE) ?: ""
        val description = arguments?.getString(ARG_DESCRIPTION) ?: ""

        view.findViewById<TextView>(R.id.onboarding_title)?.text = title
        view.findViewById<TextView>(R.id.onboarding_description)?.text = description

        return view
    }

    // Method to retrieve budget input
    fun getBudgetInput(): Float? {
        val budgetEdit = view?.findViewById<EditText>(R.id.budget_edit)
        return budgetEdit?.text?.toString()?.toFloatOrNull()
    }
}