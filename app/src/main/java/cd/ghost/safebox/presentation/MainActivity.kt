package cd.ghost.safebox.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import cd.ghost.safebox.core.ViewModelFactory
import cd.ghost.safebox.data.BiometricUtils
import cd.ghost.safebox.data.DoActivityResultsImpl
import cd.ghost.safebox.domain.DoActivityRequests
import cd.ghost.safebox.presentation.navigation.Destinations
import cd.ghost.safebox.presentation.navigation.SetupNavHost
import cd.ghost.safebox.presentation.viewmodels.MainViewModel
import com.example.compose.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var factory: MainViewModel.Factory
    private lateinit var viewModel: MainViewModel
    private lateinit var activityRequests: DoActivityRequests


    @SuppressLint("PermissionLaunchedDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRequests = DoActivityResultsImpl(this)
        BiometricUtils.isBiometricAllowed(this)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { factory.create(activityRequests) })[MainViewModel::class.java]

        setContent {
            AppTheme {

                var arePermissionsGranted by remember {
                    mutableStateOf(checkPermissionsState())
                }

                var shouldDirectUserToApplicationSettings by remember {
                    mutableStateOf(false)
                }

                var shouldShowPermissionRationale by remember {
                    mutableStateOf(shouldPermissionRationale())
                }

                val permissionLauncher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { permissions ->
                            arePermissionsGranted =
                                permissions.values.reduce { acc, isPermissionGranted ->
                                    acc && isPermissionGranted
                                }

                            if (!arePermissionsGranted) {
                                shouldShowPermissionRationale = shouldPermissionRationale()
                            }

                            shouldDirectUserToApplicationSettings =
                                !arePermissionsGranted && shouldShowPermissionRationale
                        })


                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(key1 = lifecycleOwner, effect = {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_START && !arePermissionsGranted && !shouldShowPermissionRationale) {
                            permissionLauncher.launch(permissions)
                        }

                    }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                })

                val navController = rememberNavController()

                val isAppActive by viewModel.isAppActive.observeAsState()

                // Initialize start destination
                val startDestination = when (isAppActive) {
                    true -> {
                        Destinations.LockScreen.label
                    }

                    false -> {
                        Destinations.OnBoarding.label
                    }

                    null -> {
                        Destinations.SplashScreen.label
                    }
                }
                SetupNavHost(navController, startDestination, viewModel)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.onBackPressed()) {
                    finish()
                }
            }
        })
    }

    private fun checkBiometricSupport(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }

    }

    private fun launchBiometric() {
        checkBiometricSupport()
    }

    private fun checkPermissionsState(): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openApplicationSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(PACKAGE, packageName, null)
        ).also {
            startActivity(it)
        }
    }

    private val autCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence
        ) {
            super.onAuthenticationError(errorCode, errString)
            Toast.makeText(
                applicationContext,
                "Authentication error: $errString", Toast.LENGTH_SHORT
            )
                .show()
        }

        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            super.onAuthenticationSucceeded(result)
            Toast.makeText(
                applicationContext,
                "Authentication succeeded!", Toast.LENGTH_SHORT
            )
                .show()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Toast.makeText(
                applicationContext, "Authentication failed",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun shouldPermissionRationale(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shouldShowRequestPermissionRationale(
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            shouldShowRequestPermissionRationale(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.MANAGE_DOCUMENTS,
            android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            android.Manifest.permission.USE_BIOMETRIC
        )
    } else {
        arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.MANAGE_DOCUMENTS,
            android.Manifest.permission.USE_BIOMETRIC
        )
    }

    companion object {
        const val PACKAGE = "package"
    }

}