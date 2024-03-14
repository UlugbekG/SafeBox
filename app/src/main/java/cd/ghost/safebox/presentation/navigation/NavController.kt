package cd.ghost.safebox.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cd.ghost.safebox.presentation.ChangePasswordScreen
import cd.ghost.safebox.presentation.InitialScreen
import cd.ghost.safebox.presentation.LockScreen
import cd.ghost.safebox.presentation.MainScreen
import cd.ghost.safebox.presentation.viewmodels.MainViewModel
import cd.ghost.safebox.presentation.OnboardingScreen
import cd.ghost.safebox.presentation.PrivacyPolicyScreen
import cd.ghost.safebox.presentation.SettingsScreen
import cd.ghost.safebox.presentation.SplashScreen

private const val TAG = "NavController"

@Composable
fun SetupNavHost(
    navController: NavHostController,
    startDestination: String,
    viewModel: MainViewModel
) {
    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.SplashScreen.label) {
            SplashScreen()
        }
        composable(Destinations.OnBoarding.label) {
            OnboardingScreen {
                navController.navigate(Destinations.PrivacyPolicyScreen.label)
            }
        }
        composable(Destinations.PrivacyPolicyScreen.label) {
            PrivacyPolicyScreen {
                navController.navigate(Destinations.InitialScreen.label)
            }
        }
        composable(Destinations.InitialScreen.label) {
            InitialScreen(viewModel)
        }
        composable(Destinations.LockScreen.label) {
            LockScreen(viewModel) {
                navController.navigate(Destinations.MainScreen.label) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
            }
        }
        composable(Destinations.MainScreen.label) {
            // Main screen that shows all data
            MainScreen(viewModel = viewModel, onDrawerItemClick = {
                navController.navigate(it)
            })
        }
        composable(Destinations.SettingsScreen.label) {
            SettingsScreen(
                viewModel = viewModel,
                navigateToChangePassword = {
                    navController.navigate(Destinations.ChangePasswordScreen.label)
                },
                popUp = {
                    navController.popBackStack()
                }
            )
        }
        composable(Destinations.ChangePasswordScreen.label) {
            ChangePasswordScreen(
                popUp = { navController.popBackStack() }
            )
        }
    }
}