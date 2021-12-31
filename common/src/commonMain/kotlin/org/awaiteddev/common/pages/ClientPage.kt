package org.awaiteddev.common.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.awaiteddev.common.data.AppDataManager
import org.awaiteddev.common.data.AppDataManager.cmdQueue
import org.awaiteddev.common.data.AppDataManager.ssh
import org.awaiteddev.common.ssh.SSHClient

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue


@Composable
fun ClientPage() {
    var showSpinner by remember { mutableStateOf(false) }
    var cmdInput by remember { mutableStateOf("") }
    var responseValue by remember { mutableStateOf("") }

    val scroll = rememberScrollState(100)
    val scope = rememberCoroutineScope()

    if (ssh != null && !ssh!!.shellOpen) {
        ssh!!.openShell {
            responseValue += "\n${SimpleDateFormat("HH:mm:ss").format(Timestamp(System.currentTimeMillis()))}: $it"
            showSpinner = false
            scope.launch {
                scroll.scrollTo(0)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.TopCenter) {
            TextField(
                value = responseValue,
                onValueChange = { responseValue = it },
                enabled = false,
                label = { Text("Response") },
                modifier = Modifier.fillMaxWidth().fillMaxHeight(.65f).verticalScroll(scroll, reverseScrolling = true)
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            if (showSpinner) {
                CircularProgressIndicator()
            }
            TextField(value = cmdInput, onValueChange = { cmdInput = it }, label = { Text("Enter Command") })
            Button(onClick = {
                showSpinner = true
                responseValue += "\n>> $cmdInput"
                cmdQueue.add(cmdInput)
            }) { Text("Exec") }
        }
    }
}