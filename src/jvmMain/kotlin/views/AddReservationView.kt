package views

import AppData
import LocalizedPrice
import Reservation
import ReservationMeta
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import capitalise
import getPolishName
import toTwoDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@Composable
fun AddReservationView(appData: AppData, onFinish: () -> Unit) {
  var clientName by remember { mutableStateOf("") }
  
  var meta by remember {
    mutableStateOf(
      ReservationMeta(
        4.7,
        addCleaningCost = false,
        addCleaningTime = false,
        keysIncluded = false,
        ownership = false
      )
    )
  }
  
  var notes by remember { mutableStateOf("") }
  
  var fullPrice by remember { mutableStateOf(LocalizedPrice()) }
  var pricePerDay by remember { mutableStateOf(LocalizedPrice()) }
  
  var arriveDate by remember { mutableStateOf(LocalDate.now()) }
  var leaveDate by remember { mutableStateOf(LocalDate.now()) }
  
  var reservationStage by remember { mutableStateOf(AddReservationStage.Client) }
  
  val canMove = when (reservationStage) {
    AddReservationStage.Client -> appData.clients.find { it.name == clientName } != null && clientName.isNotEmpty()
    AddReservationStage.Meta -> true
    AddReservationStage.Timeline -> true
    AddReservationStage.Pricing -> true
    AddReservationStage.Notes -> true
    AddReservationStage.Finalize -> false
  }
  
  Scaffold(modifier = Modifier.fillMaxSize(1f), bottomBar = {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(1f),
      horizontalArrangement = Arrangement.Center
    ) {
      Button(
        colors = ButtonDefaults.outlinedButtonColors(),
        modifier = Modifier.padding(5.dp).scale(0.75f),
        onClick = {
          reservationStage = AddReservationStage.values().first { it.ordinal == reservationStage.ordinal - 1 }
        },
        enabled = reservationStage != AddReservationStage.Client
      ) {
        Icon(Icons.Default.KeyboardArrowLeft, "Go back")
      }
      
      for (index in 0 until AddReservationStage.values().size) {
        Box(
          Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(if (index == reservationStage.ordinal) Color.Green else Color.LightGray)
        )
      }
      
      Button(
        colors = ButtonDefaults.outlinedButtonColors(),
        modifier = Modifier.padding(5.dp).scale(0.75f),
        onClick = {
          if (canMove) reservationStage =
            AddReservationStage.values().first { it.ordinal == reservationStage.ordinal + 1 }
        },
        enabled = canMove
      ) {
        Icon(Icons.Default.KeyboardArrowRight, "Go next")
      }
    }
  }) {
    Box(modifier = Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
      when (reservationStage) {
        AddReservationStage.Client -> ChoseClientStage(clientName, { clientName = it }, appData)
        AddReservationStage.Meta -> ChoseMeta(meta) { meta = it }
        AddReservationStage.Timeline -> ChoseTimeline(
          meta,
          arriveDate,
          { arriveDate = it },
          leaveDate
        ) { leaveDate = it }
        
        AddReservationStage.Pricing -> ChosePricing(
          meta,
          Pair(arriveDate, leaveDate),
          fullPrice,
          { fullPrice = it },
          pricePerDay
        ) {
          pricePerDay = it
        }
        
        AddReservationStage.Notes -> ChoseNote(notes) { notes = it }
        AddReservationStage.Finalize -> {
          Box(modifier = Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp),
              onClick = {
                appData.addReservation(
                  Reservation(
                    appData.clients.find { it.name == clientName }!!.uuid,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    arriveDate,
                    leaveDate,
                    meta,
                    LocalizedPrice(),
                    pricePerDay,
                    fullPrice,
                    notes
                  )
                ).also { onFinish() }
              }
            ) {
              Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, "Add")
                Text("Dodaj rezerwację")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ChoseNote(notes: String, onNotesChange: (String) -> Unit) {
  var note by remember { mutableStateOf(notes) }
  
  Column {
    OutlinedTextField(
      note,
      label = { Text("Uwagi") },
      onValueChange = {
        note = it; onNotesChange(it)
      },
      singleLine = false,
      modifier = Modifier.fillMaxSize(0.75f)
    )
  }
}

@Composable
fun ChoseTimeline(
  meta: ReservationMeta,
  initialArriveDate: LocalDate,
  onArriveDayChange: (LocalDate) -> Unit,
  initialLeaveDate: LocalDate,
  onLeaveDayChange: (LocalDate) -> Unit
) {
  var arriveDate by remember { mutableStateOf(initialArriveDate) }
  var leaveDate by remember { mutableStateOf(initialLeaveDate) }
  
  Column {
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth(1f)) {
      Column {
        Row {
          Column {
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp).scale(0.75f),
              onClick = {
                arriveDate = arriveDate.withYear(arriveDate.year - 1)
                onArriveDayChange(arriveDate)
              }
            ) {
              Icon(Icons.Default.KeyboardArrowUp, "Go back")
            }
            Text(
              arriveDate.year.toString(),
              modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
              fontSize = 14.sp,
              textAlign = TextAlign.Center
            )
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp).scale(0.75f),
              onClick = {
                arriveDate = arriveDate.withYear(arriveDate.year + 1)
                onArriveDayChange(arriveDate)
              }
            ) {
              Icon(Icons.Default.KeyboardArrowDown, "Go back")
            }
          }
          Column {
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp).scale(0.75f),
              onClick = {
                arriveDate = if (arriveDate.monthValue - 1 == 0) arriveDate.withMonth(12)
                  .withYear(arriveDate.year - 1) else arriveDate.withMonth(arriveDate.monthValue - 1)
                onArriveDayChange(arriveDate)
              }
            ) {
              Icon(Icons.Default.KeyboardArrowUp, "Go back")
            }
            Text(
              arriveDate.month.getPolishName().capitalise(),
              modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
              fontSize = 14.sp,
              textAlign = TextAlign.Center
            )
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp).scale(0.75f),
              onClick = {
                arriveDate = if (arriveDate.monthValue + 1 == 13) arriveDate.withMonth(1)
                  .withYear(arriveDate.year + 1) else arriveDate.withMonth(arriveDate.monthValue + 1)
                onArriveDayChange(arriveDate)
              }
            ) {
              Icon(Icons.Default.KeyboardArrowDown, "Go back")
            }
          }
        }
        
        CalendarMonth(
          arriveDate.year,
          arriveDate.month,
          dayMap = { date -> if (date == arriveDate) TileType.Start else TileType.Normal },
          onTileClick = { date -> arriveDate = date; onArriveDayChange(date) },
          clickEnabled = { date -> date.isBefore(leaveDate) || date == leaveDate }
        )
        
        Text("Data przyjazdu: ${arriveDate.year} ${arriveDate.month.getPolishName()} ${arriveDate.dayOfMonth}")
      }
      
      Column {
        Row {
          Column {
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp).scale(0.75f),
              onClick = {
                leaveDate = leaveDate.withYear(leaveDate.year - 1)
                onLeaveDayChange(leaveDate)
              }
            ) {
              Icon(Icons.Default.KeyboardArrowUp, "Go back")
            }
            Text(
              leaveDate.year.toString(),
              modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
              fontSize = 14.sp,
              textAlign = TextAlign.Center
            )
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp).scale(0.75f),
              onClick = {
                leaveDate = leaveDate.withYear(leaveDate.year + 1)
                onLeaveDayChange(leaveDate)
              }
            ) {
              Icon(Icons.Default.KeyboardArrowDown, "Go back")
            }
          }
          Column {
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp).scale(0.75f),
              onClick = {
                leaveDate = if (leaveDate.monthValue - 1 == 0) leaveDate.withMonth(12)
                  .withYear(leaveDate.year - 1) else leaveDate.withMonth(leaveDate.monthValue - 1)
                onLeaveDayChange(leaveDate)
              }
            ) {
              Icon(Icons.Default.KeyboardArrowUp, "Go back")
            }
            Text(
              leaveDate.month.getPolishName().capitalise(),
              modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
              fontSize = 14.sp,
              textAlign = TextAlign.Center
            )
            Button(
              colors = ButtonDefaults.outlinedButtonColors(),
              modifier = Modifier.padding(5.dp).scale(0.75f),
              onClick = {
                leaveDate = if (leaveDate.monthValue + 1 == 13) leaveDate.withMonth(1)
                  .withYear(leaveDate.year + 1) else leaveDate.withMonth(leaveDate.monthValue + 1)
                onLeaveDayChange(leaveDate)
              }
            ) {
              Icon(Icons.Default.KeyboardArrowDown, "Go back")
            }
          }
        }
        
        CalendarMonth(
          leaveDate.year,
          leaveDate.month,
          dayMap = { date -> if (date == leaveDate) TileType.End else TileType.Normal },
          onTileClick = { date -> leaveDate = date; onLeaveDayChange(date) },
          clickEnabled = { date -> date.isAfter(arriveDate) || date == arriveDate }
        )
        
        Text("Data odjazdu: ${leaveDate.year} ${leaveDate.month.getPolishName()} ${leaveDate.dayOfMonth}")
      }
    }
    Box(Modifier.fillMaxWidth(1f)) {
      Text(
        "${
          ChronoUnit.DAYS.between(arriveDate, leaveDate)
        } nocy ${
          if (meta.addCleaningTime) "(${
            (ChronoUnit.DAYS.between(arriveDate, leaveDate) - 1).coerceAtLeast(0)
          }: odliczając dzień na sprzątanie)" else ""
        }",
        modifier = Modifier.padding(5.dp).align(Alignment.Center),
        fontSize = 14.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun ChosePricing(
  meta: ReservationMeta,
  timeline: Pair<LocalDate, LocalDate>,
  defaultFullPrice: LocalizedPrice,
  onFullPriceChange: (LocalizedPrice) -> Unit,
  defaultPricePerDay: LocalizedPrice,
  onPricePerDayChange: (LocalizedPrice) -> Unit
) {
  
  var fullPrice by remember { mutableStateOf(defaultFullPrice) }
  var pricePerDay by remember { mutableStateOf(defaultPricePerDay) }
  
  Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
    Box(Modifier.fillMaxWidth(1f)) {
      Text(
        "Koszta",
        modifier = Modifier.padding(5.dp).align(Alignment.Center),
        fontSize = 14.sp,
        textAlign = TextAlign.Center
      )
    }
    
    Row(Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
      Column {
        Text(
          "Cena za dzień",
          modifier = Modifier.padding(5.dp),
          fontSize = 14.sp
        )
        OutlinedTextField(
          pricePerDay.pln.toString(),
          label = { Text("PLN") },
          onValueChange = {
            pricePerDay = pricePerDay.copy(pln = it.toDoubleOrNull() ?: 0.0); onPricePerDayChange(
            pricePerDay
          )
          },
          singleLine = true
        )
        OutlinedTextField(
          pricePerDay.euro.toString(),
          label = { Text("Euro") },
          onValueChange = {
            pricePerDay = pricePerDay.copy(euro = it.toDoubleOrNull() ?: 0.0); onPricePerDayChange(
            pricePerDay
          )
          },
          singleLine = true
        )
      }
      Column {
        Text(
          "Pozostała cena",
          modifier = Modifier.padding(5.dp),
          fontSize = 14.sp
        )
        OutlinedTextField(
          fullPrice.pln.toString(),
          label = { Text("PLN") },
          onValueChange = {
            fullPrice = fullPrice.copy(pln = it.toDoubleOrNull() ?: 0.0); onFullPriceChange(fullPrice)
          },
          singleLine = true
        )
        OutlinedTextField(
          fullPrice.euro.toString(),
          label = { Text("Euro") },
          onValueChange = {
            fullPrice = fullPrice.copy(euro = it.toDoubleOrNull() ?: 0.0); onFullPriceChange(fullPrice)
          },
          singleLine = true
        )
      }
    }
    
    val days = ChronoUnit.DAYS.between(timeline.first, timeline.second) - if (meta.addCleaningTime) 1 else 0
    
    
    val fees = LocalizedPrice(
      (if (meta.addCleaningCost) 30 else 0) + (if (meta.keysIncluded) 10 else 0).toDouble()
    )
    
    val pricePerDayPLN = pricePerDay.getPLN(meta.euroPrice)
    val fullPricePLN = fullPrice.getPLN(meta.euroPrice)
    val feesPLN = fees.getPLN(meta.euroPrice)
    val sumPricePln = ((days * pricePerDayPLN) + fullPricePLN - feesPLN).toTwoDecimal()
    
    val pricePerDayEuro = pricePerDay.getEuro(meta.euroPrice)
    val fullPriceEuro = fullPrice.getEuro(meta.euroPrice)
    val feesEuro = fees.getEuro(meta.euroPrice)
    val sumPriceEuro = ((days * pricePerDayEuro) + fullPriceEuro - feesEuro).toTwoDecimal()
    
    Box(Modifier.fillMaxWidth(1f)) {
      Text(
        "PLN: $days x ${pricePerDayPLN.toTwoDecimal()} + ${fullPricePLN.toTwoDecimal()} - ${feesPLN.toTwoDecimal()} = $sumPricePln",
        modifier = Modifier.padding(5.dp).align(Alignment.Center),
        fontSize = 14.sp,
        textAlign = TextAlign.Center
      )
    }
    
    Box(Modifier.fillMaxWidth(1f)) {
      Text(
        "Euro: $days x ${pricePerDayEuro.toTwoDecimal()} + ${fullPriceEuro.toTwoDecimal()} - ${feesEuro.toTwoDecimal()} = $sumPriceEuro",
        modifier = Modifier.padding(5.dp).align(Alignment.Center),
        fontSize = 14.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun ChoseMeta(meta: ReservationMeta, onChange: (ReservationMeta) -> Unit) {
  
  var euroPrice by remember { mutableStateOf(meta.euroPrice) }
  var addCleaningCost by remember { mutableStateOf(meta.addCleaningCost) }
  var keysIncluded by remember { mutableStateOf(meta.keysIncluded) }
  var addCleaningTime by remember { mutableStateOf(meta.addCleaningTime) }
  
  Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
    Text(
      "Metadane",
      modifier = Modifier.padding(5.dp),
      fontSize = 12.sp,
      color = Color.Gray
    )
    OutlinedTextField(
      value = euroPrice.toString(),
      onValueChange = {
        euroPrice = it.toDoubleOrNull() ?: 0.0
        onChange(ReservationMeta(euroPrice, addCleaningCost, addCleaningTime, keysIncluded, false))
      },
      isError = euroPrice == 0.0,
      label = { Text("Cena euro") },
      keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
      singleLine = true
    )
    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Text(
        "Opłata za sprzątanie?",
        modifier = Modifier.padding(5.dp),
        fontSize = 14.sp
      )
      Checkbox(
        addCleaningCost,
        onCheckedChange = {
          addCleaningCost = it
          onChange(ReservationMeta(euroPrice, addCleaningCost, addCleaningTime, keysIncluded, false))
        }
      )
    }
    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Text(
        "Opłata za wydanie kluczy?",
        modifier = Modifier.padding(5.dp),
        fontSize = 14.sp
      )
      Checkbox(keysIncluded,
        onCheckedChange = {
          keysIncluded = it
          onChange(ReservationMeta(euroPrice, addCleaningCost, addCleaningTime, keysIncluded, false))
        }
      )
    }
    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Text(
        "Dodać dzień na sprzątanie?",
        modifier = Modifier.padding(5.dp),
        fontSize = 14.sp
      )
      Checkbox(
        addCleaningTime,
        onCheckedChange = {
          addCleaningTime = it
          onChange(ReservationMeta(euroPrice, addCleaningCost, addCleaningTime, keysIncluded, false))
        }
      )
    }
  }
}

@Composable
fun ChoseClientStage(name: String, onNameChange: (String) -> Unit, appData: AppData) {
  
  var textFieldSize by remember { mutableStateOf(Size.Zero) }
  var expanded by remember { mutableStateOf(false) }
  
  val isWrongName = appData.clients.find { it.name == name } == null
  
  Column(modifier = Modifier.padding(20.dp)) {
    Text(
      "Wybierz clienta któremu przypisać rezerwacje",
      modifier = Modifier.padding(10.dp).align(Alignment.CenterHorizontally),
      fontSize = 14.sp,
      color = Color.Gray,
      textAlign = TextAlign.Center
    )
    Text(
      "P.S. Najpierw musisz go dodać",
      modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
      fontSize = 12.sp,
      color = Color.Gray,
      textAlign = TextAlign.Center
    )
    OutlinedTextField(
      value = name,
      onValueChange = onNameChange,
      isError = isWrongName && name.isNotEmpty(),
      modifier = Modifier
        .onGloballyPositioned { coordinates ->
          textFieldSize = coordinates.size.toSize()
        },
      label = { Text("Nazwa") },
      trailingIcon = {
        Icon(
          Icons.Filled.ArrowDropDown, "contentDescription",
          Modifier.clickable { expanded = !expanded }.rotate(if (expanded) 180f else 0f)
        )
      },
      singleLine = true
    )
    DropdownMenu(
      expanded = expanded && appData.clients.any { it.name.startsWith(name) },
      onDismissRequest = { expanded = false },
      modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
    ) {
      appData.clients.filter { it.name.startsWith(name) }.forEach { label ->
        DropdownMenuItem(onClick = {
          onNameChange(label.name)
        }) {
          Text(text = label.name)
        }
      }
    }
  }
}

enum class AddReservationStage {
  Client, Meta, Timeline, Pricing, Notes, Finalize
}