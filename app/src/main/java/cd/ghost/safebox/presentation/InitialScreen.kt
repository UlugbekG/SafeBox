package cd.ghost.safebox.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cd.ghost.safebox.R
import cd.ghost.safebox.presentation.components.CircularTextButton
import cd.ghost.safebox.presentation.components.CustomAlertDialog
import cd.ghost.safebox.presentation.components.TextWithBorder
import cd.ghost.safebox.presentation.components.buttons
import androidx.lifecycle.viewmodel.compose.viewModel
import cd.ghost.safebox.presentation.viewmodels.MainViewModel

@Composable
fun InitialScreen(
    viewModel: MainViewModel = viewModel()
) {
    var passwordState by remember { mutableStateOf("") }
    var dialogState by remember { mutableStateOf(true) }
    if (dialogState) {
        CustomAlertDialog(
            onConfirmation = {
                dialogState = false
            }, dialogTitle = "ENTER PASSWORD",
            dialogText = "Enter your password that contains four digits to access to your secured data.",
            icon = painterResource(R.drawable.ic_lock)
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
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
                    TextWithBorder(
                        text = c.toString(),
                        borderColor = Color.Blue.copy(alpha = 0.8f)
                    )
                }
                repeat(4 - passwordState.length) {
                    TextWithBorder(
                        text = "  "
                    )
                }
            }
        }
        LazyVerticalGrid(
            modifier = Modifier.padding(horizontal = 70.dp),
            columns = GridCells.Fixed(3)
        ) {
            items(buttons) { button ->
                CircularTextButton(
                    modifier = Modifier.fillMaxSize(),
                    onLongClick = {
                        if (button.label == "<" && passwordState.isNotEmpty()) {
                            passwordState = ""
                        }
                    },
                    onClick = {
                        if (button.label == "<" && passwordState.isNotEmpty()) {
                            passwordState = passwordState.substring(0, passwordState.length - 1)
                        }
                        if (button.label == "ok") {
                            if (passwordState.length == 4) {
                                viewModel.savePassword(passwordState)

                            }
                        }
                        if (button.label.any { it.isDigit() } && passwordState.length < 4) {
                            passwordState += button.label
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

