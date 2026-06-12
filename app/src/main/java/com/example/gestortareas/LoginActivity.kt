package com.example.gestortareas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Si el usuario ya inició sesión anteriormente, entra directamente a la app.
        if (auth.currentUser != null) {
            goToMainActivity()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (!validateFields(email, password)) return

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Error al iniciar sesión: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (!validateFields(email, password)) return

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Error al registrar usuario: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun validateFields(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Ingresa el correo electrónico"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingresa la contraseña"
            return false
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener mínimo 6 caracteres"
            return false
        }

        return true
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}