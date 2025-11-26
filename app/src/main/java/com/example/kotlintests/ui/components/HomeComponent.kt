import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.kotlintests.stores.HomeStore
import com.example.kotlintests.stores.MessageType

@Composable
fun HomeComponent(store: HomeStore = viewModel(), modifier: Modifier = Modifier) {
    val fingerprintCount by store.fingerprintCount.collectAsState()
    val messageList by store.messageList.collectAsState()
    val fingerprintPath by store.fingerprintPath.collectAsState()

    val percentageMessage = messageList.find { it.second == MessageType.PERCENTAGE }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Kotlin - Chainway Fingerprint",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Digitais Armazenadas: $fingerprintCount",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                color = Color.Gray
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        fingerprintPath?.let { path ->
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Digital Capturada",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    "Falha ao carregar a imagem.",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } ?: Spacer(modifier = Modifier.size(0.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { store.openFingerprintFolder(context) },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Abrir pasta das digitais")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(onClick = { store.grab() }, modifier = Modifier.weight(1f)) {
                Text("Capturar Digital")
            }
            Button(onClick = { store.capture() }, modifier = Modifier.weight(1f)) {
                Text("Armazenar Digital")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(onClick = { store.identify() }, modifier = Modifier.weight(1f)) {
                Text("Validar Digital")
            }
            Button(onClick = { store.deleteFingers() }, modifier = Modifier.weight(1f)) {
                Text("Apagar digitais")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Logs:",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        percentageMessage?.let { (percentageText, _) ->
            val progress = percentageText.toFloat() / 100f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            )
            Text(
                text = "$percentageText%",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 4.dp),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = Color(0xFF0288D1)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(messageList) { message ->
                val (messageText, messageType) = message
                if (messageType != MessageType.PERCENTAGE) {
                    Text(
                        text = messageText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .padding(horizontal = 16.dp),
                        color = when (messageType) {
                            MessageType.INFO -> Color.Gray
                            MessageType.SUCCESS -> Color(0xFF2E7D32)
                            MessageType.ERROR -> Color(0xFFB00020)
                            else -> Color.Black
                        },
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
