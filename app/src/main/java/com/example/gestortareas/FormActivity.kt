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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        taskRepository = FirebaseTaskRepository()

        etTaskName = findViewById(R.id.etTaskName)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        btnSaveTask = findViewById(R.id.btnSaveTask)
        btnBack = findViewById(R.id.btnBack)

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

        val task = Task(
            name = name,
            description = description
        )

        btnSaveTask.isEnabled = false

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
}