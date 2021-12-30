@file:OptIn(ExperimentalMaterialApi::class)

package org.awaiteddev.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.awaiteddev.common.pages.ClientPage
import org.awaiteddev.common.pages.ConnectPage
import org.awaiteddev.common.ssh.SSHClient

sealed class Page {
    object Connect : Page()
    object Client : Page()
}

var ssh: SSHClient? = null

@Composable
fun App() {
    var pageState by remember { mutableStateOf<Page>(Page.Connect) }
    var isConnectedSSH by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar {
                Button(onClick = {
                    scope.launch {
                        scaffoldState.drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }) { Icon(Icons.Rounded.Menu, "Menu Button") } // Menu Button
            }
        },
        drawerContent = {
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
            DrawerRow("Connect To SSH", !isConnectedSSH) {
                if (pageState != Page.Connect) pageState = Page.Connect
                scope.launch { scaffoldState.drawerState.close() }
            }
            DrawerRow("Disconnect", isConnectedSSH) {
                if (pageState != Page.Connect) pageState = Page.Connect
                ssh!!.disconnect()
                ssh = null
                showSnackbar("Disconnected From Session", scaffoldState, scope)
                scope.launch { scaffoldState.drawerState.close() }
            }
        }
    ) {
        when (pageState) {
            Page.Connect -> ConnectPage(onPageChange = { pageState = it })
            Page.Client -> ClientPage()
        }
        isConnectedSSH = ssh != null
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
private fun DrawerRow(title: String, active: Boolean, onClick: () -> Unit) {
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

