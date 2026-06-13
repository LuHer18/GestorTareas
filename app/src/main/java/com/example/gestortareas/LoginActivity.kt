package com.example.gestortareas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// LoginActivity concentra el flujo de autenticación de la aplicación.
// Desde esta pantalla el usuario puede iniciar sesión o crear una cuenta
// usando Firebase Authentication con correo y contraseña.
class LoginActivity : AppCompatActivity() {

    // FirebaseAuth administra la sesión actual y las operaciones de login/registro.
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

        // Firebase mantiene la sesión iniciada entre aperturas de la app.
        // Por eso, si ya existe un usuario autenticado, no es necesario pedir login otra vez.
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

        // Antes de llamar a Firebase se validan los campos para evitar solicitudes inválidas.
        if (!validateFields(email, password)) return

        // signInWithEmailAndPassword es una operación asíncrona: el resultado llega
        // en el listener cuando Firebase termina de comprobar las credenciales.
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.login_error, task.exception?.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Se reutiliza la misma validación porque login y registro necesitan datos válidos.
        if (!validateFields(email, password)) return

        // Al crear el usuario correctamente, Firebase también deja la sesión iniciada.
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.register_error, task.exception?.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun validateFields(email: String, password: String): Boolean {
        // Las validaciones se separan en una función para mantener más claros
        // los flujos de inicio de sesión y registro.
        if (email.isEmpty()) {
            etEmail.error = getString(R.string.error_email_required)
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = getString(R.string.error_password_required)
            return false
        }

        if (password.length < 6) {
            etPassword.error = getString(R.string.error_password_min_length)
            return false
        }

        return true
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)

        // Estas banderas limpian el historial de pantallas para que el usuario
        // no pueda volver al login presionando el botón Atrás después de autenticarse.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
