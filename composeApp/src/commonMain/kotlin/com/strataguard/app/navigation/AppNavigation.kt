package com.strataguard.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.strataguard.app.ui.auth.ForgotPasswordScreen
import com.strataguard.app.ui.auth.LoginScreen
import com.strataguard.app.ui.auth.RegisterScreen
import com.strataguard.app.ui.home.HomeScreen
import com.strataguard.app.ui.dispute.DisputeListScreen
import com.strataguard.app.ui.evidence.EvidenceListScreen
import com.strataguard.app.ui.rights.KnowYourRightsScreen
import com.strataguard.app.ui.strata.SearchStrataScreen
import com.strataguard.app.ui.strata.StrataPlanDetailScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object SearchStrata : Screen("search_strata")
    object DocumentEvidence : Screen("document_evidence")
    object DisputeRiskCheck : Screen("dispute_risk_check")
    object KnowYourRights : Screen("know_your_rights")
    data class StrataPlanDetail(val spNumber: String) : Screen("strata_plan/$spNumber") {
        companion object { const val ROUTE = "strata_plan/{spNumber}" }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.navigateUp() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onSearchStrata = { navController.navigate(Screen.SearchStrata.route) },
                onDocumentEvidence = { navController.navigate(Screen.DocumentEvidence.route) },
                onDisputeRiskCheck = { navController.navigate(Screen.DisputeRiskCheck.route) },
                onKnowYourRights = { navController.navigate(Screen.KnowYourRights.route) },
            )
        }
        composable(Screen.DisputeRiskCheck.route) {
            DisputeListScreen(onNavigateBack = { navController.navigateUp() })
        }
        composable(Screen.KnowYourRights.route) {
            KnowYourRightsScreen(onNavigateBack = { navController.navigateUp() })
        }
        composable(Screen.DocumentEvidence.route) {
            EvidenceListScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable(Screen.SearchStrata.route) {
            SearchStrataScreen(
                onNavigateBack = { navController.navigateUp() },
                onPlanSelected = { spNumber ->
                    navController.navigate(Screen.StrataPlanDetail(spNumber).route)
                },
            )
        }
        composable(Screen.StrataPlanDetail.ROUTE) { backStack ->
            val spNumber = backStack.arguments?.getString("spNumber") ?: return@composable
            StrataPlanDetailScreen(
                spNumber = spNumber,
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }
}