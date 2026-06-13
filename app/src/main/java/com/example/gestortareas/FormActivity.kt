package com.example.gestortareas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

// FormActivity representa la pantalla donde el usuario crea o edita una tarea.
// La misma Activity se reutiliza para ambos casos para evitar duplicar pantallas
// con formularios casi idénticos.
class FormActivity : AppCompatActivity() {

    private lateinit var etTaskName: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var btnSaveTask: Button
    private lateinit var btnBack: Button
    private lateinit var tvFormTitle: TextView

    // Repositorio encargado de guardar las tareas en Firebase.
    private lateinit var taskRepository: FirebaseTaskRepository

    // Si taskId tiene valor, el formulario trabaja en modo edición.
    // Si es null o está vacío, se interpreta como creación de una tarea nueva.
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
        tvFormTitle = findViewById(R.id.tvFormTitle)

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

        // Se validan los campos antes de crear el objeto Task para evitar guardar
        // información incompleta en Firebase.
        if (name.isEmpty()) {
            etTaskName.error = getString(R.string.error_task_name_required)
            return
        }

        if (description.isEmpty()) {
            etTaskDescription.error = getString(R.string.error_task_description_required)
            return
        }

        btnSaveTask.isEnabled = false

        // El mismo botón guarda una tarea nueva o actualiza una existente según
        // el modo detectado al abrir la pantalla.
        if (isEditMode) {
            updateExistingTask(name, description)
        } else {
            createNewTask(name, description)
        }
    }
    private fun loadTaskDataIfEditing() {
        // MainActivity envía estos extras cuando el usuario selecciona una tarea para editar.
        // Si no llegan extras, el formulario queda en modo creación.
        taskId = intent.getStringExtra("task_id")

        val taskName = intent.getStringExtra("task_name")
        val taskDescription = intent.getStringExtra("task_description")

        if (!taskId.isNullOrEmpty()) {
            isEditMode = true

            // En modo edición se precargan los datos actuales para que el usuario
            // modifique solo lo necesario.
            etTaskName.setText(taskName)
            etTaskDescription.setText(taskDescription)
            btnSaveTask.text = getString(R.string.action_update_task)
            tvFormTitle.text = getString(R.string.edit_task_title)
        }
    }
    private fun createNewTask(name: String, description: String) {
        // Para una tarea nueva no se asigna ID manualmente; Firebase lo generará
        // dentro del repositorio usando push().
        val task = Task(
            name = name,
            description = description
        )

        taskRepository.saveTask(
            task = task,
            onSuccess = {
                Toast.makeText(this, getString(R.string.task_saved_success), Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                btnSaveTask.isEnabled = true
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateExistingTask(name: String, description: String) {
        // En edición sí se conserva el ID, porque Firebase necesita saber
        // exactamente qué nodo debe sobrescribir.
        val task = Task(
            id = taskId ?: "",
            name = name,
            description = description
        )

        taskRepository.updateTask(
            task = task,
            onSuccess = {
                Toast.makeText(this, getString(R.string.task_updated_success), Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                btnSaveTask.isEnabled = true
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }
}
