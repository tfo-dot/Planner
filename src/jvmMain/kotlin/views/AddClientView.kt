package views

import AppData
import Client
import CurrentView
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddClientView(appData: AppData, onFinish: (CurrentView?) -> Unit) {
  var clientName by remember { mutableStateOf("") }
  
  Box(Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
    Column {
      Text(
        "Dodaj clienta",
        modifier = Modifier.padding(5.dp),
        fontSize = 14.sp
      )
      
      val isError = appData.clients.find { it.name == clientName } != null
      var showClientModal by remember { mutableStateOf(false) }
      
      if (showClientModal) {
        AlertDialog(
          modifier = Modifier.width(320.dp),
          buttons = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth(1f).padding(10.dp)) {
              OutlinedButton({ onFinish(null) }) {
                Text("Nie, dzięki!")
              }
              Box(Modifier.size(8.dp))
              Button({ onFinish(CurrentView.AddReservation) }) {
                Text("Dodaj")
              }
            }
          },
          text = {
            Text("Od razu dodać mu rezerwację?")
          },
          onDismissRequest = { showClientModal = false })
      }
      
      OutlinedTextField(clientName, { clientName = it }, placeholder = { Text("Nazwa") }, isError = isError)
      OutlinedButton(
        {
          appData.addClient(Client(clientName, UUID.randomUUID())).also { showClientModal = true }
        },
        enabled = !isError && clientName.isNotEmpty(),
        modifier = Modifier.align(Alignment.End)
      ) {
        Text("Dodaj")
      }
    }
  }
}