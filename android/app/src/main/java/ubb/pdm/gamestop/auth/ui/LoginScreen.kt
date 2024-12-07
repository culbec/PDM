package ubb.pdm.gamestop.auth.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ubb.pdm.gamestop.R

const val TAG = "LoginScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onClose: () -> Unit) {
    val authViewModel = viewModel<AuthViewModel>(factory = AuthViewModel.Factory)
    val loginState = authViewModel.loginState

    var hasErrors by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf("") }

    Log.d("LoginScreen", "isAuthenticating: ${loginState.isAuthenticating}")

    LaunchedEffect(key1 = loginState.authenticationCompleted) {
        Log.d(TAG, "Authentication completed")
        if (loginState.authenticationCompleted) {
            onClose()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.login)) })
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Log.d(TAG, "recompose")

            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            TextField(
                label = { Text(text = "Username") },
                value = username,
                onValueChange = { username = it.toString() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                shape = AbsoluteRoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Color.DarkGray,
                    unfocusedLabelColor = Color.DarkGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                leadingIcon = {
                    Icon(Icons.Rounded.AccountCircle, contentDescription = null)
                }
            )
            TextField(
                label = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                shape = AbsoluteRoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Color.DarkGray,
                    unfocusedLabelColor = Color.DarkGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                leadingIcon = {
                    Icon(Icons.Rounded.Lock, contentDescription = null)
                }

            )
            Button(
                onClick = {
                    Log.d(TAG, "login...")

                    errors = ""
                    if (username.isEmpty()) {
                        errors += "Username is required\n"
                    }
                    if (password.isEmpty()) {
                        errors += "Password is required\n"
                    }

                    hasErrors = errors.isNotEmpty()
                    if (hasErrors) {
                        return@Button
                    }

                    authViewModel.login(username, password)
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.dark_navy)),
                border = BorderStroke(2.dp, colorResource(R.color.dark_orange))
            )
            {
                Text(text = "Login")
            }

            if (hasErrors) {
                BasicAlertDialog(
                    onDismissRequest = {
                        hasErrors = false
                    },
                    modifier = Modifier
                        .background(Color.White)
                        .padding(16.dp),
                ) {
                    Column {
                        Text(
                            text = "Error",
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                        )
                        Text(
                            text = errors,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                            color = Color.Red
                        )
                        Button(
                            onClick = {
                                hasErrors = false
                            },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp),
                        ) {
                            Text(text = "OK")
                        }
                    }
                }
            }

            if (loginState.isAuthenticating) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp),
                )
            }

            if (loginState.authenticationError != null) {
                // Update errors
                errors = loginState.authenticationError.message ?: "Unknown error"
                hasErrors = true
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen {}
}