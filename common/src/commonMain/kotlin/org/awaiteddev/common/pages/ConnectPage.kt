package org.awaiteddev.common.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.awaiteddev.common.Page
import org.awaiteddev.common.data.AppDataManager
import org.awaiteddev.common.data.AppDataManager.ssh
import org.awaiteddev.common.data.KeyData
import org.awaiteddev.common.ssh.SSHClient

@ExperimentalMaterialApi
@Composable
fun ConnectPage(onPageChange: (Page) -> Unit) {
    var hostIPInput by remember { mutableStateOf("35.221.109.178") }
    var usernameInput by remember { mutableStateOf("test") }
    var portInput by remember { mutableStateOf("22") }
    var showSpinner by remember { mutableStateOf(false) }
    var connectionMessage by remember { mutableStateOf("") }
    var selectedKey: KeyData? by remember { mutableStateOf(null) }
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    if (showSpinner) {
        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            Column {
                AppDataManager.keyList.forEach {
                    ListItem(
                        text = { Text("Key: ${it.name} UserName: ${it.userName}") },
                        icon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Localized description"
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedKey = it
                            scope.launch { sheetState.hide() }
                        }
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Enter SSH Connection Info", modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 16.dp))

            TextField(value = hostIPInput, onValueChange = { hostIPInput = it }, label = { Text("Host IP") })
            TextField(value = portInput, onValueChange = { portInput = it }, label = { Text("Port") })
            TextField(value = usernameInput, onValueChange = { usernameInput = it }, label = { Text("User Name") })
            TextField(value = selectedKey?.name ?: "",
                onValueChange = {},
                label = { Text("Selected Key") },
                enabled = false,
                modifier = Modifier.clickable {
                    scope.launch { sheetState.show() }
                })
            Text(connectionMessage, modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 16.dp))

            Button(
                enabled = selectedKey != null,
                onClick = {
                    connectionMessage = ""
                    showSpinner = true
                    ssh = SSHClient(
                        hostIPInput,
                        usernameInput,
                        portInput.toInt(),
                        selectedKey!!,
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
}
