import org.awaiteddev.common.App
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.awaiteddev.common.ssh.KeyFileHandler

@ExperimentalUnitApi
fun main() = application {

    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme {
            App ()
        }
    }
}

