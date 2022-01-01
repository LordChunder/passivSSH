package org.awaiteddev.common.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.awaiteddev.common.Page
import org.awaiteddev.common.ssh.SSHClient

@ExperimentalUnitApi
@Composable
fun HomePage(onPageChange: (Page) -> Unit) {
    val showSpinner by remember { mutableStateOf(false) }
    if (showSpinner) {
        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }
    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.Center) { //TITLE
        Text(
            "PassivSSH",
            modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 16.dp),
            fontSize = TextUnit(32f, TextUnitType.Sp),
            fontWeight = FontWeight.Bold
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            onPageChange.invoke(if (!SSHClient.isConnected) Page.Connect else Page.Client)
        }) {
            Text("SSH Client")
        }
        Button(onClick = { onPageChange.invoke(Page.KeyManagement) }) { Text("Manage Keys") }
    }
}