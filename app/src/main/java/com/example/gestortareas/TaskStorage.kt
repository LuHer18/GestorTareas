package com.example.gestortareas

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
// TaskStorage centraliza la lógica de almacenamiento local de tareas.
// Utiliza SharedPreferences para guardar la información dentro del dispositivo.
class TaskStorage(private val context: Context) {
    // Archivo interno de preferencias donde se almacenan las tareas.
    private val sharedPreferences = context.getSharedPreferences("Tasks", Context.MODE_PRIVATE)

    fun saveTask(task: Task) {
        val tasks = getTasks().toMutableList()
        tasks.add(task)

        val jsonArray = JSONArray()

        for (item in tasks) {
            val jsonObject = JSONObject()
            jsonObject.put("name", item.name)
            jsonObject.put("description", item.description)
            jsonArray.put(jsonObject)
        }
        // Guarda el JSON en SharedPreferences.
        sharedPreferences.edit()
            .putString("task_list", jsonArray.toString())
            .apply()
    }

    fun getTasks(): List<Task> {
        // Obtiene el JSON guardado. Si no existe, usa una lista vacía.
        val json = sharedPreferences.getString("task_list", "[]")
        val jsonArray = JSONArray(json)
        val tasks = mutableListOf<Task>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val task = Task(
                name = jsonObject.getString("name"),
                description = jsonObject.getString("description")
            )

            tasks.add(task)
        }

        return tasks
    }
}