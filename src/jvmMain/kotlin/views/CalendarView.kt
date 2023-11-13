package views

import AppData
import FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rangeTo
import java.time.LocalDate
import java.time.Month

@Composable
fun CalendarView(appData: AppData) {
  
  var currentYear by remember { mutableStateOf(LocalDate.now().year) }
  
  val reservations =
    appData.reservations.filter { it.from.year == currentYear || it.to.year == currentYear }
  
  Column(Modifier.verticalScroll(rememberScrollState())) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth(1f)) {
      Button(
        colors = ButtonDefaults.outlinedButtonColors(),
        modifier = Modifier.padding(5.dp).scale(0.75f),
        onClick = { currentYear-- }
      ) {
        Icon(Icons.Default.KeyboardArrowLeft, "Last year")
      }
      Text(
        currentYear.toString(),
        modifier = Modifier.padding(5.dp).align(Alignment.CenterVertically),
        fontSize = 14.sp
      )
      Button(
        colors = ButtonDefaults.outlinedButtonColors(),
        modifier = Modifier.padding(5.dp).scale(0.75f),
        onClick = { currentYear++ }
      ) {
        Icon(Icons.Default.KeyboardArrowRight, "Last year")
      }
    }
    
    FlowRow(modifier = Modifier.padding(20.dp), crossAxisSpacing = 20.dp, mainAxisSpacing = 20.dp) {
      for (monthIndex in 1..12) {
        val currentMonth = Month.of(monthIndex)
        
        val takenDaysInMonth = appData.takenDays.filter { it.month == currentMonth && it.year == currentYear }
        
        Column {
          CalendarMonth(
            currentYear,
            currentMonth,
            { if (it in takenDaysInMonth) TileType.Taken else TileType.Normal },
            clickEnabled = { false }
          )
        }
        
      }
    }
  }
}