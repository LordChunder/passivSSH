@file:OptIn(ExperimentalMaterialApi::class)

package org.awaiteddev.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.awaiteddev.common.pages.*
import org.awaiteddev.common.ssh.KeyFileHandler
import org.awaiteddev.common.ssh.SSHClient

sealed class Page(val name: String) {
    object Connect : Page("Connect")
    object Client : Page("Client")
    object Home : Page("Home")
    object KeyManagement : Page("Key Management")
    object CreateKey : Page("Create Key")
}


var init = false

@ExperimentalUnitApi
@Composable
fun App() {
    if (!init) KeyFileHandler.readKeyDataFromFile()

    var pageState by remember { mutableStateOf<Page>(Page.Home) }
    var isConnectedSSH by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val showDrawer = pageState != Page.CreateKey

    if(pageState!=Page.Client)
        SSHClient.closeShell()

    Scaffold(
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar {
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {

                    Row(modifier = Modifier.align(Alignment.CenterStart).wrapContentWidth()) {
                        if (showDrawer) Button(onClick = {
                            scope.launch {
                                scaffoldState.drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) { Icon(Icons.Rounded.Menu, "Menu Button") } // Menu Button
                        Text(
                            pageState.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = TextUnit(18f, TextUnitType.Sp),
                            modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 16.dp)
                        )
                    }
                    if (pageState != Page.Home)
                        Button(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            onClick = {
                                scope.launch {
                                    pageState = if (pageState == Page.CreateKey)
                                        Page.KeyManagement
                                    else
                                        Page.Home
                                }
                            }) {
                            if (pageState == Page.CreateKey) {
                                Icon(Icons.Rounded.ArrowBack, "Back Button")
                            } else
                                Icon(Icons.Rounded.Home, "Home Button")
                        } // Menu Button
                }
            }
        },
        drawerContent = {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                Column {
                    Row {
                        Text("Menu", modifier = Modifier.padding(16.dp).weight(1f).align(Alignment.CenterVertically))
                        Button(
                            modifier = Modifier.width(96.dp).padding(16.dp),
                            onClick = {
                                scope.launch {
                                    scaffoldState.drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }) { Icon(Icons.Rounded.Close, "Close Menu") }
                    } //Menu Row
                    Divider()
                    DrawerRow("Home") {
                        if (pageState != Page.Home) pageState = Page.Home
                        scope.launch { scaffoldState.drawerState.close() }
                    }
                    DrawerRow("SSH Client") {
                        if (!isConnectedSSH)
                            if (pageState != Page.Connect) pageState = Page.Connect
                        else if (pageState != Page.Client) pageState = Page.Client
                        scope.launch { scaffoldState.drawerState.close() }
                    }
                    DrawerRow("Manage Keys") {
                        if (pageState != Page.KeyManagement) pageState = Page.KeyManagement
                        scope.launch { scaffoldState.drawerState.close() }
                    }
                    DrawerRow("Disconnect", isConnectedSSH) {
                        if (pageState != Page.Home) pageState = Page.Home
                        SSHClient.disconnect()
                        showSnackbar("Disconnected From Session", scaffoldState, scope)
                        scope.launch { scaffoldState.drawerState.close() }
                    }
                }
                Column(modifier = Modifier.align(Alignment.BottomCenter).wrapContentHeight()) {
                    Divider()
                    ConnectionStatusRow(isConnectedSSH) {
                        if (pageState != Page.Client) pageState = Page.Client
                        scope.launch { scaffoldState.drawerState.close() }
                    }
                }
            }
        }
    ) {
        when (pageState) {
            Page.Home -> HomePage(onPageChange = { pageState = it })
            Page.Connect -> ConnectPage(onPageChange = { pageState = it })
            Page.Client -> ClientPage()
            Page.KeyManagement -> KeyManagement(onPageChange = { pageState = it })
            Page.CreateKey -> CreateKeyPage(onPageChange = { pageState = it })
        }
        isConnectedSSH = SSHClient.isConnected
    }
}

fun showSnackbar(message: String, scaffoldState: ScaffoldState, scope: CoroutineScope) {
    scope.launch {
        scaffoldState.snackbarHostState.showSnackbar(
            message = message,
        )
    }
}

@Composable
private fun ConnectionStatusRow(active: Boolean = true, onClick: () -> Unit) {
    val background = if (!active) Color.LightGray.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (!active) Color.Blue.copy(alpha = 0.1f) else Color.Blue
    if (active)
        ListItem(modifier = Modifier.clickable(onClick = onClick).background(background).fillMaxWidth()) {
            Box(Modifier.fillMaxWidth()) {
                Text(modifier = Modifier.align(Alignment.CenterStart), color = textColor, text = "PassivBot Status:")
                Text(modifier = Modifier.align(Alignment.CenterEnd), color = textColor, text = "Connected!")
            }
        }
    else
        ListItem(modifier = Modifier.background(background).fillMaxWidth()) {
            Box(Modifier.fillMaxWidth()) {
                Text(modifier = Modifier.align(Alignment.CenterStart), color = textColor, text = "PassivBot Status:")
                Text(modifier = Modifier.align(Alignment.CenterEnd), color = textColor, text = "Connect With SSH")
            }
        }
}

@Composable
private fun DrawerRow(title: String, active: Boolean = true, onClick: () -> Unit) {
    val background = if (!active) Color.LightGray.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (!active) Color.Blue.copy(alpha = 0.1f) else Color.Blue
    if (active)
        ListItem(modifier = Modifier.clickable(onClick = onClick).background(background)) {
            Text(color = textColor, text = title)
        }
    else
        ListItem(modifier = Modifier.background(background)) {
            Text(color = textColor, text = title)
        }
}

