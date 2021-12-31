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
import org.awaiteddev.common.data.AppDataManager.ssh

import java.sql.Timestamp
import java.text.SimpleDateFormat

@Composable
fun ClientPage() {
    var showSpinner by remember { mutableStateOf(false) }
    var cmdInput by remember { mutableStateOf("") }
    var responseValue by remember { mutableStateOf("") }

    val scroll = rememberScrollState(100)
    val scope = rememberCoroutineScope()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.TopCenter) {
            TextField(
                value = responseValue,
                onValueChange = { responseValue = it },
                enabled = false,
                label = { Text("Response") },
                modifier = Modifier.fillMaxWidth().fillMaxHeight(.35f).verticalScroll(scroll, reverseScrolling = true)
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

                ssh!!.execute(List(1) { cmdInput }) {
                    showSpinner = false
                    responseValue += "\n${SimpleDateFormat("HH:mm:ss").format(Timestamp(System.currentTimeMillis()))}: $it"
                    scope.launch {
                        scroll.scrollTo(0)
                    }
                }
            }) { Text("Exec") }
        }
    }
}