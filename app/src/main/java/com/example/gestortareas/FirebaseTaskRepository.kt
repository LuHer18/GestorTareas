package com.example.gestortareas

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Clase encargada de centralizar las operaciones con Firebase Realtime Database.
// Aquí se guardan y consultan las tareas del usuario autenticado.
class FirebaseTaskRepository {

    private val auth = FirebaseAuth.getInstance()

    // URL de la Realtime Database creada en Firebase Console.
    // Si tu URL cambia, debes actualizarla aquí.
    private val database = FirebaseDatabase
        .getInstance("https://gestortareas-67c82-default-rtdb.firebaseio.com/")
        .reference

    // Obtiene el UID del usuario autenticado.
    // Este UID permite guardar las tareas separadas por usuario.
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Guarda una nueva tarea dentro del nodo tasks/{uid}.
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

        taskReference.setValue(task)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al guardar la tarea")
            }
    }
    // Actualiza una tarea existente en Firebase usando su ID.
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
    // Cada vez que se agrega, cambia o elimina una tarea, Firebase actualiza la lista.
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

                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)

                    if (task != null) {
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