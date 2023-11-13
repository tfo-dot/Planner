package views

import AppData
import FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun ClientView(appData: AppData) {
  if (appData.clients.isEmpty()) {
    Box(Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          "Brak klientów",
          modifier = Modifier.padding(5.dp),
          fontSize = 14.sp
        )
      }
    }
  } else {
    FlowRow(crossAxisSpacing = 5.dp, mainAxisSpacing = 5.dp, modifier = Modifier.padding(10.dp)) {
      appData.clients.forEach {
        Card {
          Column(Modifier.padding(5.dp)) {
            Row(Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                it.name,
                modifier = Modifier.padding(5.dp),
                fontSize = 14.sp
              )
              
              OutlinedButton({ appData.removeClient(it) }, Modifier.scale(0.75f)) {
                Icon(
                  Icons.Default.Close,
                  "Delete client",
                  tint = Color.Red
                )
              }
            }
            
            val reservations = appData.reservations.filter { reservation -> reservation.cid == it.uuid }
            Text(
              "Rezerwacje: ${reservations.size} (${reservations.count { reservation -> reservation.from.year == LocalDate.now().year }} w tym roku)",
              modifier = Modifier.padding(5.dp),
              fontSize = 14.sp,
              color = Color.Gray
            )
          }
        }
      }
    }
  }
}