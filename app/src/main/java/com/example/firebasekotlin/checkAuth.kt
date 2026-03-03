package com.example.firebasekotlin

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        object ResetPasswordSent : AuthState()
        data class Error(val message: String) : AuthState()
    }

    //------------------ ลงทะเบียนใช้งาน ------------------
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    //------------------ รีเซตรหัสผ่าน ------------------
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.ResetPasswordSent
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "ไม่พบบัญชีนี้ในระบบ"
                    "ERROR_INVALID_EMAIL"  -> "รูปแบบ Email ไม่ถูกต้อง"
                    else -> "เกิดข้อผิดพลาด กรุณาลองใหม่"
                }
                _authState.value = AuthState.Error(message)
            }
        }
    }

    //------------------ ล็อกอินด้วยอีเมล์ ------------------
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    // ✅ ------------------ ล็อกอินด้วย Google ------------------
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. สร้าง Google Sign-In Option พร้อม Web Client ID
                val googleIdOption = GetSignInWithGoogleOption
                    .Builder(WEB_CLIENT_ID)
                    .build()

                // 2. สร้าง Credential Request
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 3. ดึง Credential จาก CredentialManager
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                // 4. ตรวจสอบว่าเป็น GoogleIdTokenCredential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken, null
                    )

                    // 5. Sign in เข้า Firebase
                    auth.signInWithCredential(firebaseCredential).await()
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("ไม่สามารถรับข้อมูล Google ได้")
                }

            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("Google Sign-In ถูกยกเลิก")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    //------------------ ออกจากระบบ ------------------
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        // ✅ ใส่ Web Client ID จาก Firebase Console
        private const val WEB_CLIENT_ID = "90614285274-ut6qg019pl6lp1fhdegkp2t9mmbrnkog.apps.googleusercontent.com"
    }
}