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
import android.view.View
import android.widget.TextView

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
    private lateinit var tvUserEmail: TextView
    private lateinit var tvEmptyState: TextView

    // Mantiene en memoria las tareas cargadas para poder identificar cuál selecciona
    // el usuario en el ListView y enviarla al flujo de edición o eliminación.
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
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        tvUserEmail.text = "Sesión: ${auth.currentUser?.email ?: "Usuario autenticado"}"

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
        // Se guarda la referencia del listener para poder removerlo en onDestroy.
        // Esto es importante porque Firebase escucha cambios en tiempo real.
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
        // Se sincroniza la lista visual con una lista de objetos Task completos.
        // El ListView solo muestra texto, pero para editar o eliminar se necesita el objeto original.
        currentTasks.clear()
        currentTasks.addAll(tasks)

        if (tasks.isEmpty()) {
            // Si no hay tareas se muestra un estado vacío en lugar de una lista sin contenido.
            tvEmptyState.visibility = View.VISIBLE
            listTasks.visibility = View.GONE
            return
        }

        tvEmptyState.visibility = View.GONE
        listTasks.visibility = View.VISIBLE

        val taskTexts = tasks.map { task ->
            "${task.name}\n${task.description}"
        }

        // ArrayAdapter convierte la lista de textos en filas visibles dentro del ListView.
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

            // La posición tocada en el ListView coincide con la posición del objeto
            // dentro de currentTasks, por eso se puede recuperar la tarea seleccionada.
            val selectedTask = currentTasks[position]
            showTaskOptionsDialog(selectedTask)
        }
    }

    private fun showTaskOptionsDialog(task: Task) {
        // El diálogo evita crear botones extra en cada fila y concentra las acciones
        // disponibles para una tarea: editar, eliminar o cancelar.
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

        // Se envían los datos actuales de la tarea para que FormActivity pueda
        // abrirse en modo edición y precargar el formulario.
        intent.putExtra("task_id", task.id)
        intent.putExtra("task_name", task.name)
        intent.putExtra("task_description", task.description)
        startActivity(intent)
    }

    private fun confirmDeleteTask(task: Task) {
        // La eliminación se confirma antes de ejecutarse porque borra datos persistentes
        // en Firebase y no solo información temporal de la pantalla.
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
        // La lista se actualizará automáticamente cuando Firebase notifique el cambio
        // al listener configurado en loadTasksFromFirebase.
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
