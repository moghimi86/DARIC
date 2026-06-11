package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.viewmodel.WealthViewModel

@Composable
fun AppNavigation(viewModel: WealthViewModel) {
    val navController = rememberNavController()
    val isPersian by viewModel.isPersian.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // 1. Splash Screen
        composable("splash") {
            SplashScreen(navController = navController)
        }

        // 2. Onboarding Screen
        composable("onboarding") {
            OnboardingScreen(
                navController = navController,
                isPersianFlow = isPersian,
                onLangToggle = { toggled -> viewModel.isPersian.value = toggled }
            )
        }

        // 3. Home Dashboard
        composable("dashboard") {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // 4. Asset List Screen
        composable("assets_list") {
            AssetsListScreen(navController = navController, viewModel = viewModel)
        }

        // 5. Add Asset Screen
        composable("add_asset") {
            AddAssetScreen(navController = navController, viewModel = viewModel)
        }

        // 6 & 8. Debts & Receivables Tabbed Screen
        composable("debts_list") {
            DebtsListScreen(navController = navController, viewModel = viewModel)
        }

        // 7. Add Debt/Receivable Screen
        composable("add_debt") {
            AddDebtScreen(navController = navController, viewModel = viewModel)
        }

        // 9. Transaction historical logger
        composable("transactions_list") {
            TransactionsHistoryScreen(navController = navController, viewModel = viewModel)
        }

        // 10. Reports & Ratios Distribution
        composable("reports_analytics") {
            ReportsAnalyticsScreen(navController = navController, viewModel = viewModel)
        }

        // 11. AI Advisory Insights Screen
        composable("ai_insights") {
            AiInsightsScreen(navController = navController, viewModel = viewModel)
        }

        // 12. Settings Control Screen
        composable("settings") {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
    }
}
