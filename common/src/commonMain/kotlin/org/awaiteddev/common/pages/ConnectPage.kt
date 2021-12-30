package org.awaiteddev.common.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.awaiteddev.common.Page
import org.awaiteddev.common.ssh
import org.awaiteddev.common.ssh.SSHClient

@Composable
fun ConnectPage(onPageChange: (Page) -> Unit) {
    var hostIPInput by remember { mutableStateOf("35.221.109.178") }
    var usernameInput by remember { mutableStateOf("test") }
    var portInput by remember { mutableStateOf("22") }
    var showSpinner by remember { mutableStateOf(false) }
    var connectionMessage by remember { mutableStateOf("") }

    if (showSpinner) {
        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }

    //COMPOSE
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Enter SSH Connection Info", modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 16.dp))

        TextField(value = hostIPInput, onValueChange = { hostIPInput = it }, label = { Text("Host IP") })
        TextField(value = portInput, onValueChange = { portInput = it }, label = { Text("Port") })
        TextField(value = usernameInput, onValueChange = { usernameInput = it }, label = { Text("User Name") })

        Text(connectionMessage, modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 16.dp))

        Button(onClick = {
            connectionMessage = ""
            showSpinner = true
            ssh = SSHClient(
                hostIPInput,
                usernameInput,
                portInput.toInt(),
                onConnected = {
                    showSpinner = false
                    if (it) onPageChange.invoke(Page.Client)
                    else connectionMessage = "Check Inputs, unable to connect to SSH Session"
                })
        }) {
            Text("Connect")
        }
    }
}




