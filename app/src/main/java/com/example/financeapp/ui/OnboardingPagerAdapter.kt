package com.example.financeapp.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.financeapp.R

class OnboardingPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingScreenFragment.newInstance(
                R.layout.onboarding_screen_1,
                "Welcome to FinanceApp",
                "Track your income and expenses effortlessly."
            )
            1 -> OnboardingScreenFragment.newInstance(
                R.layout.onboarding_screen_2,
                "Set Your Budget",
                "Plan your finances with a customizable budget."
            )
            2 -> OnboardingScreenFragment.newInstance(
                R.layout.onboarding_screen_3,
                "Analyze Your Spending",
                "Get insights with detailed charts and reports."
            )
            else -> throw IllegalStateException("Invalid position")
        }
    }
}