package com.example.gestortareas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// FormActivity representa la pantalla donde el usuario crea una nueva tarea.
// Los datos ingresados se guardan en Firebase Realtime Database.
class FormActivity : AppCompatActivity() {

    private lateinit var etTaskName: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var btnSaveTask: Button
    private lateinit var btnBack: Button

    // Repositorio encargado de guardar las tareas en Firebase.
    private lateinit var taskRepository: FirebaseTaskRepository

    private var taskId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        taskRepository = FirebaseTaskRepository()

        etTaskName = findViewById(R.id.etTaskName)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        btnSaveTask = findViewById(R.id.btnSaveTask)
        btnBack = findViewById(R.id.btnBack)

        loadTaskDataIfEditing()
        configureSaveButton()
        configureBackButton()

    }

    private fun configureSaveButton() {
        btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun configureBackButton() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun saveTask() {
        val name = etTaskName.text.toString().trim()
        val description = etTaskDescription.text.toString().trim()

        if (name.isEmpty()) {
            etTaskName.error = "Ingresa el nombre de la tarea"
            return
        }

        if (description.isEmpty()) {
            etTaskDescription.error = "Ingresa la descripción de la tarea"
            return
        }

        btnSaveTask.isEnabled = false

        if (isEditMode) {
            updateExistingTask(name, description)
        } else {
            createNewTask(name, description)
        }
    }
    private fun loadTaskDataIfEditing() {
        taskId = intent.getStringExtra("task_id")

        val taskName = intent.getStringExtra("task_name")
        val taskDescription = intent.getStringExtra("task_description")

        if (!taskId.isNullOrEmpty()) {
            isEditMode = true
            etTaskName.setText(taskName)
            etTaskDescription.setText(taskDescription)
            btnSaveTask.text = "Actualizar Tarea"
        }
    }
    private fun createNewTask(name: String, description: String) {
        val task = Task(
            name = name,
            description = description
        )

        taskRepository.saveTask(
            task = task,
            onSuccess = {
                Toast.makeText(this, "Tarea guardada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                btnSaveTask.isEnabled = true
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateExistingTask(name: String, description: String) {
        val task = Task(
            id = taskId ?: "",
            name = name,
            description = description
        )

        taskRepository.updateTask(
            task = task,
            onSuccess = {
                Toast.makeText(this, "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                btnSaveTask.isEnabled = true
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }
}