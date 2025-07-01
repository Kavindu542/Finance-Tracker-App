package com.example.financeapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.financeapp.R
import com.example.financeapp.viewmodel.SettingsViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextButton: Button
    private lateinit var skipButton: Button
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)

        viewPager = view.findViewById(R.id.view_pager)
        tabLayout = view.findViewById(R.id.tab_layout)
        nextButton = view.findViewById(R.id.next_button)
        skipButton = view.findViewById(R.id.skip_button)

        // Setup ViewPager2 with adapter
        val adapter = OnboardingPagerAdapter(this)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2 for dots indicator
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // Handle Next button
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                viewPager.currentItem = currentItem + 1
            } else {
                completeOnboarding()
            }
        }

        // Handle Skip button
        skipButton.setOnClickListener {
            completeOnboarding()
        }

        // Update button text based on ViewPager position
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                nextButton.text = if (position == adapter.itemCount - 1) "Get Started" else "Next"
            }
        })

        return view
    }

    private fun completeOnboarding() {
        // Retrieve budget from the second screen
        val secondFragment = childFragmentManager.fragments.getOrNull(1) as? OnboardingScreenFragment
        val budget = secondFragment?.getBudgetInput()

        // Validate budget
        if (budget != null && budget < 0) {
            Toast.makeText(requireContext(), "Budget cannot be negative", Toast.LENGTH_SHORT).show()
            return
        }

        // Save budget to SettingsViewModel (if provided)
        if (budget != null) {
            settingsViewModel.budget = budget
        }

        // Mark onboarding as completed
        val sharedPref = requireContext().getSharedPreferences("finance", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("has_seen_onboarding", true).apply()

        // Navigate to RegisterFragment
        findNavController().navigate(R.id.mainActivity)
    }
}