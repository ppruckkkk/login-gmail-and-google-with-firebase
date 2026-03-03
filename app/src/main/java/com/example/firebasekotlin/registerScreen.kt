package com.example.firebasekotlin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()  // ✅ เพิ่ม ViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()  // ✅ observe state

    // ✅ เมื่อ register สำเร็จ ให้ navigate ออกไป
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            authViewModel.resetState()
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("สมัครสมาชิก", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ แสดง error message
        if (localError != null) {
            Spacer(Modifier.height(8.dp))
            Text(localError!!, color = Color.Red, fontSize = 14.sp)
        }
        if (authState is AuthViewModel.AuthState.Error) {
            Spacer(Modifier.height(8.dp))
            Text((authState as AuthViewModel.AuthState.Error).message, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (password != confirmPassword) {
                    localError = "Password และ Confirm Password ไม่ตรงกัน"
                    return@Button
                }
                localError = null
                authViewModel.register(email, password)  // ✅ เรียก register จริงๆ
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(6.dp),
            enabled = email.isNotBlank() && password.isNotBlank()
                    && authState !is AuthViewModel.AuthState.Loading,  // ✅ disable ตอน loading
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6D9E51),
                contentColor = Color.White
            )
        ) {
            // ✅ แสดง loading indicator
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.height(20.dp))
            } else {
                Text("สมัครสมาชิก", color = Color.White)
            }
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("เป็นสมาชิกแล้ว? เข้าสู่ระบบ")
        }
    }
}