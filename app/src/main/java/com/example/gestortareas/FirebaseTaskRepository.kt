package com.example.gestortareas

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Clase encargada de centralizar las operaciones con Firebase Realtime Database.
// Separar esta lógica en un repositorio evita que las Activities conozcan detalles
// internos de Firebase y deja la interfaz de usuario más enfocada en mostrar datos.
class FirebaseTaskRepository {

    private val auth = FirebaseAuth.getInstance()

    // URL de la Realtime Database creada en Firebase Console.
    // Si tu URL cambia, debes actualizarla aquí.
    private val database = FirebaseDatabase
        .getInstance("https://gestortareas-67c82-default-rtdb.firebaseio.com/")
        .reference

    // Obtiene el UID del usuario autenticado.
    // El UID es clave en la estructura de datos porque permite guardar las tareas
    // separadas por usuario dentro del nodo tasks/{uid}.
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Guarda una nueva tarea dentro del nodo tasks/{uid}.
    // Recibe callbacks para avisar a la Activity si la operación terminó bien o falló,
    // ya que las operaciones con Firebase se ejecutan de forma asíncrona.
    fun saveTask(
        task: Task,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUserId()

        if (uid == null) {
            onError("No hay un usuario autenticado")
            return
        }

        // push() crea una referencia nueva con un ID único generado por Firebase.
        // Ese ID se guarda también dentro del objeto para poder editarlo o eliminarlo después.
        val taskReference = database
            .child("tasks")
            .child(uid)
            .push()

        val taskId = taskReference.key

        if (taskId == null) {
            onError("No se pudo generar el ID de la tarea")
            return
        }

        task.id = taskId

        // setValue escribe el objeto completo en la base de datos.
        // Los listeners permiten reaccionar al resultado sin bloquear la pantalla.
        taskReference.setValue(task)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al guardar la tarea")
            }
    }
    // Actualiza una tarea existente en Firebase usando su ID.
    // A diferencia de saveTask, aquí no se usa push() porque la tarea ya tiene
    // una ubicación fija dentro de Firebase.
    fun updateTask(
        task: Task,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUserId()

        if (uid == null) {
            onError("No hay un usuario autenticado")
            return
        }

        if (task.id.isEmpty()) {
            onError("No se encontró el ID de la tarea")
            return
        }

        database
            .child("tasks")
            .child(uid)
            .child(task.id)
            .setValue(task)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al actualizar la tarea")
            }
    }

    // Elimina una tarea de Firebase usando el ID de la tarea.
    // Primero se comprueba el usuario y el ID para evitar borrar una ruta incorrecta.
    fun deleteTask(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUserId()

        if (uid == null) {
            onError("No hay un usuario autenticado")
            return
        }

        if (taskId.isEmpty()) {
            onError("No se encontró el ID de la tarea")
            return
        }

        database
            .child("tasks")
            .child(uid)
            .child(taskId)
            .removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al eliminar la tarea")
            }
    }
    // Escucha en tiempo real las tareas del usuario autenticado.
    // Cada vez que se agrega, cambia o elimina una tarea, Firebase ejecuta onDataChange
    // y entrega una nueva fotografía completa del nodo tasks/{uid}.
    fun listenTasks(
        onTasksLoaded: (List<Task>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener? {
        val uid = getCurrentUserId()

        if (uid == null) {
            onError("No hay un usuario autenticado")
            return null
        }

        val tasksReference = database
            .child("tasks")
            .child(uid)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = mutableListOf<Task>()

                // Cada hijo del snapshot representa una tarea guardada bajo el UID actual.
                // getValue convierte los datos de Firebase nuevamente en un objeto Task.
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)

                    if (task != null) {
                        // El ID no siempre viene dentro del objeto, por eso se recupera
                        // desde la clave del nodo para conservar la referencia real en Firebase.
                        task.id = taskSnapshot.key ?: ""
                        tasks.add(task)
                    }
                }

                onTasksLoaded(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        tasksReference.addValueEventListener(listener)
        return listener
    }

    // Remueve el listener para evitar fugas de memoria cuando se destruye la pantalla.
    // Si no se elimina, Firebase podría seguir enviando cambios a una Activity que ya no existe.
    fun removeTasksListener(listener: ValueEventListener?) {
        val uid = getCurrentUserId()

        if (uid != null && listener != null) {
            database
                .child("tasks")
                .child(uid)
                .removeEventListener(listener)
        }
    }
}
