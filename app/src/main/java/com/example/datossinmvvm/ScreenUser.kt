package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val db = remember { crearDatabase(context) }
    val dao = db.userDao()

    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dataUser by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                actions = {
                    // 👉 Botón agregar
                    IconButton(onClick = {
                        val user = User(firstName = firstName, lastName = lastName)
                        coroutineScope.launch {
                            AgregarUsuario(user = user, dao = dao)
                        }
                        firstName = ""
                        lastName = ""
                    }) {
                        Text("Agregar")
                    }

                    // 👉 Botón listar
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val data = getUsers(dao = dao)
                            dataUser = data
                        }
                    }) {
                        Text("Listar")
                    }

                    // 👉 Botón eliminar último
                    IconButton(onClick = {
                        coroutineScope.launch {
                            EliminarUltimo(dao = dao)
                            val data = getUsers(dao = dao)
                            dataUser = data
                        }
                    }) {
                        Text("Eliminar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(50.dp))
            TextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID (solo lectura)") },
                readOnly = true,
                singleLine = true
            )
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name:") },
                singleLine = true
            )
            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name:") },
                singleLine = true
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = dataUser,
                fontSize = 20.sp
            )
        }
    }
}

fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao: UserDao): String {
    val users = dao.getAll()
    var rpta = ""
    users.forEach { user ->
        rpta += "${user.uid} | ${user.firstName} - ${user.lastName}\n"
    }
    return rpta
}

suspend fun AgregarUsuario(user: User, dao: UserDao) {
    try {
        dao.insert(user)
    } catch (e: Exception) {
        Log.e("User", "Error insert: ${e.message}")
    }
}

suspend fun EliminarUltimo(dao: UserDao) {
    try {
        val ultimo = dao.getLastUser()
        if (ultimo != null) {
            dao.delete(ultimo)
        }
    } catch (e: Exception) {
        Log.e("User", "Error delete: ${e.message}")
    }
}
