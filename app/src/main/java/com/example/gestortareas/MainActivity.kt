package com.example.gestortareas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import androidx.appcompat.app.AlertDialog

// MainActivity representa la pantalla principal de la aplicación.
// Desde aquí se muestra la lista de tareas guardadas en Firebase,
// se permite navegar al formulario y se administra el cierre de sesión.
class MainActivity : AppCompatActivity() {

    // FirebaseAuth permite consultar el usuario actual y cerrar sesión.
    private lateinit var auth: FirebaseAuth

    // Repositorio encargado de leer las tareas desde Firebase Realtime Database.
    private lateinit var taskRepository: FirebaseTaskRepository

    // Listener que escucha cambios en la base de datos en tiempo real.
    private var taskListener: ValueEventListener? = null

    // Componentes visuales definidos en activity_main.xml.
    private lateinit var listTasks: ListView
    private lateinit var btnAddTask: Button
    private lateinit var btnLogout: Button
    private lateinit var btnOpenMap: Button
    private val currentTasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Si no existe un usuario autenticado, se redirige al login.
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        taskRepository = FirebaseTaskRepository()

        listTasks = findViewById(R.id.listTasks)
        btnAddTask = findViewById(R.id.btnAddTask)
        btnLogout = findViewById(R.id.btnLogout)
        btnOpenMap = findViewById(R.id.btnOpenMap)

        configureAddTaskButton()
        configureLogoutButton()
        configureTaskListClick()
        configureMapButton()
        loadTasksFromFirebase()
    }

    private fun configureAddTaskButton() {
        btnAddTask.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configureLogoutButton() {
        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadTasksFromFirebase() {
        taskListener = taskRepository.listenTasks(
            onTasksLoaded = { tasks ->
                showTasks(tasks)
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showTasks(tasks: List<Task>) {
        currentTasks.clear()
        currentTasks.addAll(tasks)

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
        auth.signOut()
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Se elimina el listener para evitar que Firebase siga escuchando cambios
        // cuando la pantalla ya fue destruida.
        if (::taskRepository.isInitialized) {
            taskRepository.removeTasksListener(taskListener)
        }
    }
    private fun configureTaskListClick() {
        listTasks.setOnItemClickListener { _, _, position, _ ->
            if (currentTasks.isEmpty()) {
                return@setOnItemClickListener
            }

            val selectedTask = currentTasks[position]
            showTaskOptionsDialog(selectedTask)
        }
    }

    private fun showTaskOptionsDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle(task.name)
            .setMessage("Selecciona una acción para esta tarea.")
            .setPositiveButton("Editar") { _, _ ->
                goToEditTask(task)
            }
            .setNegativeButton("Eliminar") { _, _ ->
                confirmDeleteTask(task)
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun goToEditTask(task: Task) {
        val intent = Intent(this, FormActivity::class.java)
        intent.putExtra("task_id", task.id)
        intent.putExtra("task_name", task.name)
        intent.putExtra("task_description", task.description)
        startActivity(intent)
    }

    private fun confirmDeleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar tarea")
            .setMessage("¿Deseas eliminar la tarea \"${task.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteTask(task)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        taskRepository.deleteTask(
            taskId = task.id,
            onSuccess = {
                Toast.makeText(this, "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show()
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }
    private fun configureMapButton() {
        btnOpenMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}