package cd.ghost.safebox.presentation.navigation

sealed class Destinations(val label: String) {
    object OnBoarding : Destinations("onboarding_screen")
    object SplashScreen : Destinations("splash_screen")
    object InitialScreen : Destinations("initial_screen")
    object PrivacyPolicyScreen : Destinations("privacy_policy_screen")
    object LockScreen : Destinations("lock_screen")
    object MainScreen : Destinations("main_screen")
    object SettingsScreen : Destinations("settings_screen")
    object ChangePasswordScreen : Destinations("change_password_screen")
}
