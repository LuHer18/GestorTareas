package com.example.gestortareas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// MainActivity representa la pantalla principal de la aplicación.
// Desde aquí se muestra la lista de tareas guardadas, se permite navegar al formulario
// y se administra el cierre de sesión del usuario autenticado.
class MainActivity : AppCompatActivity() {

    // FirebaseAuth permite consultar el usuario actual y cerrar sesión.
    private lateinit var auth: FirebaseAuth

    // Clase auxiliar encargada de guardar y recuperar tareas.
    // Por ahora sigue usando SharedPreferences; luego la cambiaremos por Firebase Realtime Database.
    private lateinit var taskStorage: TaskStorage

    // Componentes visuales definidos en activity_main.xml.
    private lateinit var listTasks: ListView
    private lateinit var btnAddTask: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Firebase Authentication.
        auth = FirebaseAuth.getInstance()

        // Si no existe un usuario autenticado, se redirige al login.
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        // Inicializa la clase de almacenamiento local.
        taskStorage = TaskStorage(this)

        // Relaciona las variables Kotlin con los componentes XML.
        listTasks = findViewById(R.id.listTasks)
        btnAddTask = findViewById(R.id.btnAddTask)
        btnLogout = findViewById(R.id.btnLogout)

        configureAddTaskButton()
        configureLogoutButton()
    }

    override fun onResume() {
        super.onResume()

        // Cada vez que la pantalla principal vuelve a estar visible,
        // se actualiza la lista de tareas.
        if (::taskStorage.isInitialized) {
            loadTasks()
        }
    }

    private fun configureAddTaskButton() {
        btnAddTask.setOnClickListener {
            // Intent permite navegar desde la pantalla principal hacia FormActivity.
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configureLogoutButton() {
        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadTasks() {
        // Obtiene las tareas almacenadas.
        val tasks = taskStorage.getTasks()

        val taskTexts = if (tasks.isEmpty()) {
            listOf("No hay tareas guardadas")
        } else {
            tasks.map { task ->
                "${task.name}\n${task.description}"
            }
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            taskTexts
        )

        listTasks.adapter = adapter
    }

    private fun logoutUser() {
        // Cierra la sesión actual de Firebase.
        auth.signOut()

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

        // Después de cerrar sesión, vuelve al login y limpia el historial de pantallas.
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}