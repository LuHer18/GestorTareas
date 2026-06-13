package com.example.gestortareas

// Modelo de datos que representa una tarea dentro de la aplicación.
// Se usan propiedades var y valores por defecto para que Firebase pueda convertir
// los datos de la Realtime Database en objetos Task.
data class Task(
    var id: String = "",
    var name: String = "",
    var description: String = ""
)