package org.awaiteddev.common.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.awaiteddev.common.Page
import org.awaiteddev.common.data.AppDataManager
import org.awaiteddev.common.ssh.KeyFileHandler.Companion.saveKeyDataListToFile
import org.awaiteddev.common.ssh.KeyGeneration.Companion.generateKeyPair

@Composable
fun CreateKeyPage(onPageChange: (Page) -> Unit) {
    var rsaChecked by remember { mutableStateOf(true) }
    var usernameInput by remember { mutableStateOf("") }
    var keyNameInput by remember { mutableStateOf("") }
    var showSpinner by remember { mutableStateOf(false) }

    if (showSpinner) {
        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }
    fun generateKey() {
        if (keyNameInput == "") keyNameInput = "default"
        val keyPair = generateKeyPair(usernameInput, keyNameInput)
        showSpinner = false
        if (keyPair == null) return

        AppDataManager.keyList.add(keyPair)
        saveKeyDataListToFile()
        onPageChange.invoke(Page.KeyManagement)

    }
    //COMPOSE
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Key Info", modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 16.dp))

        TextField(value = keyNameInput, onValueChange = { keyNameInput = it }, label = { Text("Key Name") })
        Text("Key Algorithm", modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 0.dp))

        Row { //TODO add more algorithms
            Text("RSA", Modifier.align(Alignment.CenterVertically))
            Checkbox(rsaChecked, { rsaChecked = it }, enabled = false)
        }

        TextField(value = usernameInput, onValueChange = { usernameInput = it }, label = { Text("User Name") })

        Button({
            showSpinner = true
            Thread {
                generateKey()
            }.start()
        }, Modifier.padding(16.dp), enabled = !showSpinner) {
            Text("Generate")
        }
    }
}

