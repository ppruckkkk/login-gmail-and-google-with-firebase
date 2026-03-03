package com.example.firebasekotlin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = viewModel()  // ✅ เพิ่ม ViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()  // ✅ observe state

    // ✅ เมื่อ login สำเร็จ ให้ navigate ออกไป
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            authViewModel.resetState()
            onLoginSuccess()
        }
    }

    // ------------------- Dialog ลืมรหัสผ่าน -------------------
    if (showForgotDialog) {
        val resetState by authViewModel.authState.collectAsState()
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
                resetEmail = ""
                authViewModel.resetState()
            },
            title = { Text("ลืมรหัสผ่าน") },
            text = {
                Column {
                    Text("กรอก Email ที่ใช้สมัครสมาชิก\nระบบจะส่งลิงก์รีเซ็ตรหัสผ่านให้")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    // ✅ แสดงผลหลังส่ง reset email
                    when (resetState) {
                        is AuthViewModel.AuthState.ResetPasswordSent -> {
                            Spacer(Modifier.height(8.dp))
                            Text("ส่ง Email สำเร็จแล้ว กรุณาตรวจสอบ inbox", color = Color(0xFF6D9E51))
                        }
                        is AuthViewModel.AuthState.Error -> {
                            Spacer(Modifier.height(8.dp))
                            Text((resetState as AuthViewModel.AuthState.Error).message, color = Color.Red)
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { authViewModel.resetPassword(resetEmail) },  // ✅ เรียก resetPassword
                    enabled = resetEmail.isNotBlank() && resetState !is AuthViewModel.AuthState.Loading
                ) { Text("ส่ง Email", color = Color(0xFF6D9E51)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    resetEmail = ""
                    authViewModel.resetState()
                }) { Text("ยกเลิก", color = Color.Gray) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFE3DBBB)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sign in", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))

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
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(
                        imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Visibility"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ แสดง error message
        if (authState is AuthViewModel.AuthState.Error) {
            Spacer(Modifier.height(8.dp))
            Text(
                (authState as AuthViewModel.AuthState.Error).message,
                color = Color.Red,
                fontSize = 14.sp
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                resetEmail = email
                showForgotDialog = true
            }) {
                Text("ลืมรหัสผ่าน?")
            }
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { authViewModel.loginWithEmail(email, password) },  // ✅ เรียก login จริงๆ
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
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("เข้าสู่ระบบ", color = Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("ยังไม่เป็นสมาชิก? สมัครสมาชิก")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { authViewModel.signInWithGoogle(context) },  // ✅ เรียก function
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Text("Sign in with Google", color = Color.Black)
        }
    }
}