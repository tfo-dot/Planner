package views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import capitalise
import getPolishName
import java.time.LocalDate
import java.time.Month
import rangeTo

@Composable
fun CalendarTile(content: String, tileType: TileType = TileType.Normal, modifier: Modifier = Modifier) {
  val color = when (tileType) {
    TileType.Header -> Color.Gray.copy(alpha = 0.3f)
    TileType.Start -> Color.Green.copy(alpha = 0.3f)
    TileType.End -> Color.Red.copy(alpha = 0.3f)
    TileType.Middle -> Color.Blue.copy(alpha = 0.1f)
    TileType.Normal -> Color.Transparent
    TileType.Taken -> Color.Red.copy(alpha = 0.8f)
  }
  
  Box(Modifier.width(30.dp).height(25.dp).then(modifier)) {
    Text(
      content,
      modifier = Modifier.padding(5.dp).align(Alignment.Center).zIndex(2f),
      fontSize = 14.sp,
      textAlign = TextAlign.Center
    )
    Box(Modifier.background(color).height(5.dp).width(30.dp).align(Alignment.Center).offset(y = 20.dp))
  }
}

enum class TileType {
  Header, Start, End, Middle, Normal, Taken
}

@Composable
fun CalendarMonth(
  year: Int,
  month: Month,
  dayMap: (LocalDate) -> TileType = { TileType.Normal },
  onTileClick: (LocalDate) -> Unit = { },
  clickEnabled: (LocalDate) -> Boolean = { true }
) {
  val startOfTheMonth = LocalDate.of(year, month, 1)
  val endOfTHeMonth =
    LocalDate.of(
      if (month.value == 12) year + 1 else year,
      if (month.value == 12) 1 else month.value + 1,
      1
    ).plusDays(-1)
  
  val getStartOfTheFirstWeek = startOfTheMonth.plusDays(-(startOfTheMonth.dayOfWeek.value.toLong() - 1))
  val getEndOfTheLastWeek = endOfTHeMonth.plusDays(7L - endOfTHeMonth.dayOfWeek.value + 1)
  
  Text(
    month.getPolishName().capitalise(),
    modifier = Modifier.padding(5.dp),
    fontSize = 14.sp,
    textAlign = TextAlign.Center
  )
  
  Row {
    for (weekDay in listOf("Pn", "Wt", "Śr", "Czw", "Pią", "So", "Ndz")) {
      CalendarTile(weekDay, TileType.Header)
    }
  }
  
  val daysList = mutableListOf<MutableList<Pair<String, TileType>>>()
  
  for ((index, day) in (getStartOfTheFirstWeek.plusDays(-1)..getEndOfTheLastWeek).withIndex()) {
    
    val dayValue = if (month == day.month) day.dayOfMonth.toString() else " "
    
    if (daysList.getOrNull(index / 7) == null) {
      daysList.add(index / 7, mutableListOf())
    }
    
    daysList[index / 7].add(Pair(dayValue, dayMap(day)))
  }
  
  for (week in daysList) {
    Row {
      for (day in week) {
        
        var modifier: Modifier = Modifier
        val actualDate = LocalDate.of(year, month, day.first.toIntOrNull() ?: 1)
        
        if (day.first.isNotBlank() && clickEnabled(actualDate)) {
          modifier = modifier.clickable { onTileClick(actualDate) }
        }
        
        CalendarTile(
          content = day.first,
          tileType = day.second,
          modifier = modifier
        )
      }
    }
  }
}