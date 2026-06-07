package com.example.gestortareas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

// MainActivity representa la pantalla principal de la aplicación.
// Desde aquí se muestra la lista de tareas guardadas y se permite navegar al formulario.
class MainActivity : AppCompatActivity() {
    // Clase auxiliar encargada de guardar y recuperar tareas desde SharedPreferences.
    private lateinit var taskStorage: TaskStorage
    // Componentes visuales definidos en activity_main.xml.
    private lateinit var listTasks: ListView
    private lateinit var btnAddTask: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa la clase de almacenamiento local.
        taskStorage = TaskStorage(this)
        // Relaciona las variables Kotlin con los componentes XML de la pantalla principal.
        listTasks = findViewById(R.id.listTasks)
        btnAddTask = findViewById(R.id.btnAddTask)

        configureAddTaskButton()
    }

    override fun onResume() {
        super.onResume()
        // Se ejecuta cada vez que la pantalla principal vuelve a estar visible.
        // Esto permite actualizar la lista después de guardar una tarea en FormActivity.
        loadTasks()
    }

    private fun configureAddTaskButton() {
        btnAddTask.setOnClickListener {
            // Intent permite navegar desde la pantalla principal hacia FormActivity.
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadTasks() {
        // Obtiene las tareas almacenadas localmente.
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
}