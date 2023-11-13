import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.Month
import java.util.*
import kotlin.math.floor

fun String.capitalise(): String =
  this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun Month.getPolishName(): String = when (this.value) {
  1 -> "styczeń"
  2 -> "luty"
  3 -> "marzec"
  4 -> "kwiecień"
  5 -> "maj"
  6 -> "czerwiec"
  7 -> "lipiec"
  8 -> "sierpień"
  9 -> "wrzesień"
  10 -> "październik"
  11 -> "listopad"
  12 -> "grudzień"
  else -> "ERR"
}

fun LocalDate.format(): String = "${dayOfMonth}.${if (monthValue <= 9) "0$monthValue" else monthValue}"

fun Double.stripDecimal() = if (this == floor(this)) this.toInt() else this
fun Double.toTwoDecimal() = String.format("%.2f", this)

@Composable
fun FlowRow(
  modifier: Modifier = Modifier,
  alignment: Alignment.Horizontal = Alignment.Start,
  crossAxisSpacing: Dp = 0.dp,
  mainAxisSpacing: Dp = 0.dp,
  content: @Composable () -> Unit
) = Layout(content, modifier) { measurables, constraints ->
  val hGapPx = mainAxisSpacing.roundToPx()
  val vGapPx = crossAxisSpacing.roundToPx()
  
  val rows = mutableListOf<MeasuredRow>()
  val itemConstraints = constraints.copy(minWidth = 0)
  
  for (measurable in measurables) {
    val lastRow = rows.lastOrNull()
    val placeable = measurable.measure(itemConstraints)
    
    if (lastRow != null && lastRow.width + hGapPx + placeable.width <= constraints.maxWidth) {
      lastRow.items.add(placeable)
      lastRow.width += hGapPx + placeable.width
      lastRow.height = maxOf(lastRow.height, placeable.height)
    } else {
      val nextRow = MeasuredRow(
        items = mutableListOf(placeable), width = placeable.width, height = placeable.height
      )
      
      rows.add(nextRow)
    }
  }
  
  val width = rows.maxOfOrNull { row -> row.width } ?: 0
  val height = rows.sumOf { row -> row.height } + maxOf(vGapPx.times(rows.size - 1), 0)
  
  val coercedWidth = width.coerceIn(constraints.minWidth, constraints.maxWidth)
  val coercedHeight = height.coerceIn(constraints.minHeight, constraints.maxHeight)
  
  layout(coercedWidth, coercedHeight) {
    var y = 0
    
    for (row in rows) {
      var x = when (alignment) {
        Alignment.Start -> 0
        Alignment.CenterHorizontally -> (coercedWidth - row.width) / 2
        Alignment.End -> coercedWidth - row.width
        
        else -> throw Exception("unsupported alignment")
      }
      
      for (item in row.items) {
        item.place(x, y)
        x += item.width + hGapPx
      }
      
      y += row.height + vGapPx
    }
  }
}

private data class MeasuredRow(
  val items: MutableList<Placeable>, var width: Int, var height: Int
)

operator fun LocalDate.rangeTo(other: LocalDate) = LocalDateRange(this, other)

class LocalDateRange(private val start: LocalDate, private val end: LocalDate) : Iterator<LocalDate> {
  private var current = start.plusDays(0)
  
  override fun hasNext(): Boolean =
    (current.isAfter(start) && current.isBefore(end)) || (current.isEqual(start) && !current.isEqual(end))
  
  override fun next(): LocalDate = current.plusDays(1).also { current = it }
}