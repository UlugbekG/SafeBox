package cd.ghost.safebox.presentation

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cd.ghost.safebox.R
import cd.ghost.safebox.data.BiometricStatus
import cd.ghost.safebox.data.BiometricUtils
import cd.ghost.safebox.presentation.components.CircularTextButton
import cd.ghost.safebox.presentation.components.buttons
import cd.ghost.safebox.presentation.viewmodels.MainViewModel
import cd.ghost.safebox.ui.theme.md_theme_light_primary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

val buttonsOfLockScreen = buttons.apply { removeAt(buttons.size - 1) }

@Composable
fun LockScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigate: () -> Unit
) {
    val context = LocalContext.current
    var passwordState by remember { mutableStateOf("") }
    var passwordBorderColor by remember { mutableStateOf(Color.Gray) }
    var blockButtons by remember { mutableStateOf(false) }
    val password by viewModel.password.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    // set up biometrics
    val biometricState by viewModel.biometricIsAllowed.observeAsState(false)
    LaunchedEffect(Unit){
        if (biometricState) {
            BiometricUtils.launchBiometric { status ->
                when (status) {
                    is BiometricStatus.Error -> {
                        Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                    }

                    is BiometricStatus.Fail -> {
                        Toast.makeText(context, "Authentication failed!", Toast.LENGTH_SHORT).show()
                    }

                    is BiometricStatus.Success -> onNavigate()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(200.dp),
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "ENTER PASSCODE",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Text(
                text = "Please enter your passcode",
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                passwordState.toCharArray().forEachIndexed { index, c ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(passwordBorderColor)
                    )
                }
                repeat(4 - passwordState.length) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                }
            }
        }
        LazyVerticalGrid(
            modifier = Modifier.padding(horizontal = 70.dp),
            columns = GridCells.Fixed(3)
        ) {
            items(buttonsOfLockScreen) { button ->
                CircularTextButton(
                    modifier = Modifier.fillMaxSize(),
                    onLongClick = {
                        if (button.label == "<" && passwordState.isNotEmpty() && !blockButtons) {
                            passwordState = ""
                        }
                    },
                    onClick = {
                        if (button.label == "<" && passwordState.isNotEmpty() && !blockButtons) {
                            passwordState = passwordState.substring(0, passwordState.length - 1)
                        }
                        if (button.label.any { it.isDigit() } && passwordState.length < 4 && !blockButtons) {
                            passwordState += button.label
                        }
                        if (passwordState.length == 4 && !blockButtons) {

                            val passwordCheck = password == passwordState
                            // If password is wrong change dost color to red
                            passwordBorderColor =
                                if (passwordCheck) md_theme_light_primary else Color.Red

                            // if state is true navigate to the main screen
                            if (passwordCheck) {
                                onNavigate()
                            }

                            coroutineScope.launch {
                                if (!passwordCheck) {
                                    blockButtons = true
                                    delay(300L)
                                    passwordState = ""
                                    blockButtons = false
                                }
                            }
                        } else {
                            passwordBorderColor = md_theme_light_primary
                        }
                    },
                    content = {
                        if (button.icon == null) {
                            Text(
                                text = button.label,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding()
                            )
                        } else {
                            Image(
                                painter = painterResource(button.icon),
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}




