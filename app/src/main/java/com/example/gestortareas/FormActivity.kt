package com.example.gestortareas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// FormActivity representa la pantalla del formulario.
// En esta pantalla el usuario escribe el nombre y la descripción de una tarea.
class FormActivity : AppCompatActivity() {
    // Campos de texto y botones definidos en activity_form.xml.
    private lateinit var etTaskName: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var btnSaveTask: Button
    private lateinit var btnBack: Button
    // Clase responsable de almacenar la tarea en SharedPreferences.
    private lateinit var taskStorage: TaskStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        // Inicializa el almacenamiento local.
        taskStorage = TaskStorage(this)

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
        // Obtiene los valores escritos por el usuario y elimina espacios innecesarios.
        val name = etTaskName.text.toString().trim()
        val description = etTaskDescription.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre de la tarea", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Ingresa la descripción de la tarea", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(name, description)
        taskStorage.saveTask(task)

        Toast.makeText(this, "Tarea guardada correctamente", Toast.LENGTH_SHORT).show()
        finish()
    }
}