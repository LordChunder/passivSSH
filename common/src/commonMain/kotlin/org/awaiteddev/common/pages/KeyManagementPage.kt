package org.awaiteddev.common.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.awaiteddev.common.Page
import org.awaiteddev.common.data.AppDataManager
import org.awaiteddev.common.data.KeyData
import org.awaiteddev.common.ssh.KeyFileHandler.Companion.saveKeyDataListToFile

@ExperimentalUnitApi
@Composable
fun KeyManagement(onPageChange: (Page) -> Unit) {
    var showSpinner by remember { mutableStateOf(false) }
    var keyValue by remember { mutableStateOf("") }
    if (showSpinner) {
        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }

    fun deleteKey(keyData: KeyData) {
        if (AppDataManager.keyList.remove(keyData)) {
            showSpinner = true
            Thread {
                saveKeyDataListToFile()
                showSpinner = false
            }.start()
        }
    }
    @Composable
    fun messageList(keys: List<KeyData>) {
        Column {
            keys.forEach { key ->
                Card(
                    modifier = Modifier.width(196.dp).padding(0.dp, 16.dp),
                    backgroundColor = Color.LightGray,
                    elevation = 4.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().clickable {
                        keyValue = "Name: ${key.name}   UserName: ${key.userName}\n\n" +
                                "Public Key: ${key.publicKey}"

                    }) {
                        Column(modifier = Modifier.padding(8.dp).wrapContentWidth()) {
                            Text("Key: ${key.name}", fontSize = TextUnit(24f, TextUnitType.Sp))
                            Text(
                                "User: ${key.userName}",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontSize = TextUnit(16f, TextUnitType.Sp))
                        }
                        Icon(
                            Icons.Rounded.Delete,
                            "Delete Key Button",
                            modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp).clickable {
                                deleteKey(key)
                            })
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.width(256.dp).fillMaxHeight(.65f)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Saved Keys", modifier = Modifier.align(Alignment.CenterStart).padding(8.dp))
                Button(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp),
                    onClick = { onPageChange(Page.CreateKey) }) {
                    Text("New Key")
                }
            }

            messageList(AppDataManager.keyList)
        }

        Box(Modifier.fillMaxWidth().height(256.dp),contentAlignment = Alignment.BottomCenter) {
            SelectionContainer {
                TextField(
                    value = keyValue,
                    onValueChange = {},

                    label = { Text("Key Information") },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}



