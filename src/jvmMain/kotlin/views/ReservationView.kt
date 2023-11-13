package views

import AppData
import Client
import LocalizedPrice
import Reservation
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capitalise
import format
import getPolishName
import toTwoDecimal
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReservationView(appData: AppData) {
  if (appData.reservations.isEmpty()) {
    Box(Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          "Brak rezerwacji",
          modifier = Modifier.padding(5.dp),
          fontSize = 14.sp
        )
      }
    }
  } else {
    var availableYearsExpanded by remember { mutableStateOf(false) }
    var availableClientsExpanded by remember { mutableStateOf(false) }
    var availableMonthsExpanded by remember { mutableStateOf(false) }
    
    val searchState = remember { SearchState() }
    
    val availableYears = remember(appData.reservations) {
      appData.reservations.flatMap { listOf(it.from.year, it.to.year) }.toSet()
    }
    
    val availableClients = remember(appData.clients) {
      appData.reservations.map { appData.getClient(it) }.toSet()
    }
    
    val availableMonths = remember(appData.reservations) {
      
      appData.reservations.flatMap {
        var currentDate = it.from.withDayOfMonth(1)
        val months = mutableListOf<Month>(currentDate.month)
        
        while (currentDate.month != it.to.month) {
          currentDate = currentDate.plusMonths(1)
          
          months.add(currentDate.month)
        }
        
        months.toSet()
      }.toSet()
    }
    
    Column(Modifier.fillMaxSize(1f)) {
      Row(
        Modifier
          .fillMaxWidth(1f)
          .height(50.dp)
          .padding(horizontal = 5.dp, vertical = 5.dp)
          .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row {
          OutlinedButton(onClick = { availableYearsExpanded = true }) {
            Icon(Icons.Default.Menu, "Filter")
            Box(Modifier.size(5.dp))
            Text("Rok")
          }
          DropdownMenu(expanded = availableYearsExpanded, onDismissRequest = { availableYearsExpanded = false }) {
            availableYears.forEach { year ->
              DropdownMenuItem(
                onClick = { searchState.addYear(year); availableYearsExpanded = false }
              ) {
                Text(year.toString())
              }
            }
          }
          
          Box(Modifier.size(5.dp))
          
          Row {
            OutlinedButton(onClick = { availableMonthsExpanded = true }) {
              Icon(Icons.Default.Menu, "Filter")
              Box(Modifier.size(5.dp))
              Text("Miesiąc")
            }
            DropdownMenu(expanded = availableMonthsExpanded, onDismissRequest = { availableMonthsExpanded = false }) {
              availableMonths.forEach { month ->
                DropdownMenuItem(
                  onClick = { searchState.addMonth(month); availableMonthsExpanded = false }
                ) {
                  Text(month.getPolishName().capitalise())
                }
              }
            }
          }
          
          Box(Modifier.size(5.dp))
          
          Row {
            OutlinedButton(onClick = { availableClientsExpanded = true }) {
              Icon(Icons.Default.Menu, "Filter")
              Box(Modifier.size(5.dp))
              Text("Klient")
            }
            DropdownMenu(expanded = availableClientsExpanded, onDismissRequest = { availableClientsExpanded = false }) {
              availableClients.forEach { client ->
                DropdownMenuItem(
                  onClick = { searchState.addClient(client); availableClientsExpanded = false }
                ) {
                  Text(client.name)
                }
              }
            }
          }
        }
        
        if (searchState.years.isNotEmpty()) {
          Box(Modifier.size(5.dp))
        }
        
        searchState.years.forEach {
          Chip(onClick = { searchState.removeYear(it) }) {
            Text("Rok: $it", Modifier.padding(5.dp))
            Box(Modifier.size(5.dp))
            Icon(Icons.Default.Close, "Delete", Modifier.scale(0.75f))
          }
        }
        
        if (searchState.months.isNotEmpty()) {
          Box(Modifier.size(5.dp))
        }
        
        searchState.months.forEach {
          Chip(onClick = { searchState.removeMonth(it) }) {
            Text("W: ${it.getPolishName().capitalise()}", Modifier.padding(5.dp))
            Box(Modifier.size(5.dp))
            Icon(Icons.Default.Close, "Delete", Modifier.scale(0.75f))
          }
        }
        
        if (searchState.clients.isNotEmpty()) {
          Box(Modifier.size(5.dp))
        }
        
        searchState.clients.forEach {
          Chip(onClick = { searchState.removeClient(it) }) {
            Text("Przez: ${it.name}", Modifier.padding(5.dp))
            Box(Modifier.size(5.dp))
            Icon(Icons.Default.Close, "Delete", Modifier.scale(0.75f))
          }
        }
      }
      
      Column(Modifier.verticalScroll(rememberScrollState()).fillMaxSize(1f)) {
        appData.reservations
          .filter {
            (it.from.year in searchState.years || it.to.year in searchState.years) || searchState.years.isEmpty()
          }
          .filter { reservation ->
            var currentDate = reservation.from.withDayOfMonth(1)
            val months = mutableListOf<Month>(currentDate.month)
            
            while (currentDate.month != reservation.to.month) {
              currentDate = currentDate.plusMonths(1)
              
              months.add(currentDate.month)
            }
            
            months.toSet().any { it in searchState.months } || searchState.months.isEmpty()
          }
          .filter { reservation ->
            searchState.clients.map { it.uuid }.contains(reservation.cid) || searchState.clients.isEmpty()
          }.forEach {
            ReservationCard(reservation = it, client = appData.getClient(it), appData)
          }
      }
    }
  }
}

class SearchState {
  val years = mutableStateListOf<Int>()
  val clients = mutableStateListOf<Client>()
  val months = mutableStateListOf<Month>()
  
  fun addYear(year: Int) = if (year !in years) years.add(year) else false
  fun removeYear(year: Int) = years.remove(year)
  
  fun addClient(client: Client) = if (client !in clients) clients.add(client) else false
  fun removeClient(client: Client) = clients.remove(client)
  
  fun addMonth(month: Month) = if (month !in months) months.add(month) else false
  fun removeMonth(month: Month) = months.remove(month)
}

@Composable
fun ReservationCard(reservation: Reservation, client: Client, appData: AppData) {
  val fees = LocalizedPrice(
    (if (reservation.meta.addCleaningCost) 30 else 0) + (if (reservation.meta.keysIncluded) 10 else 0).toDouble()
  )
  
  val days = ChronoUnit.DAYS.between(reservation.from, reservation.to)
  
  Card(modifier = Modifier.fillMaxWidth(1f).padding(5.dp, 10.dp), elevation = 4.dp) {
    Column(Modifier.padding(10.dp)) {
      Row(Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
          "Rezerwacja",
          modifier = Modifier.padding(5.dp),
          fontSize = 12.sp,
          color = Color.Gray
        )
        Text(
          client.name,
          modifier = Modifier.padding(5.dp),
          fontSize = 14.sp
        )
        
        OutlinedButton({ appData.removeReservation(reservation) }, Modifier.scale(0.75f)) {
          Icon(
            Icons.Default.Close,
            "Delete reservation",
            tint = Color.Red
          )
        }
      }
      
      Row(modifier = Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.SpaceAround) {
        Column(Modifier.wrapContentWidth()) {
          
          val priceForDaysPLN =
            (days - if (reservation.meta.addCleaningTime) 1 else 0) * reservation.pricePerDay.getPLN(reservation.meta.euroPrice)
          val priceForDaysEuro =
            (days - if (reservation.meta.addCleaningTime) 1 else 0) * reservation.pricePerDay.getEuro(reservation.meta.euroPrice)
          
          val fullPricePLN = reservation.price.getPLN(reservation.meta.euroPrice)
          val fullPriceEuro = reservation.price.getEuro(reservation.meta.euroPrice)
          
          val feesCostPLN = fees.getPLN(reservation.meta.euroPrice)
          val feesCostEuro = fees.getEuro(reservation.meta.euroPrice)
          
          Row {
            if (reservation.pricePerDay.getEuro(reservation.meta.euroPrice) != 0.0) {
              Column {
                Text(
                  "Koszt z dni",
                  modifier = Modifier.padding(5.dp),
                  fontSize = 12.sp,
                  color = Color.Gray
                )
                Text(
                  "${priceForDaysPLN.toTwoDecimal()} PLN",
                  modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                  fontSize = 14.sp,
                  textAlign = TextAlign.Center
                )
                Text(
                  "${priceForDaysEuro.toTwoDecimal()} Euro",
                  modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                  fontSize = 12.sp,
                  color = Color.LightGray,
                  textAlign = TextAlign.Center
                )
              }
              Column {
                Text(
                  " ",
                  modifier = Modifier.padding(5.dp),
                  fontSize = 12.sp,
                  color = Color.Gray,
                  textAlign = TextAlign.Center
                )
                Text(
                  "+",
                  modifier = Modifier.padding(10.dp, 5.dp).align(Alignment.CenterHorizontally),
                  fontSize = 14.sp,
                  textAlign = TextAlign.Center
                )
              }
            }
            Column {
              Text(
                "Dodatek",
                modifier = Modifier.padding(5.dp),
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
              )
              Text(
                "${fullPricePLN.toTwoDecimal()} PLN",
                modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
              )
              Text(
                "${fullPriceEuro.toTwoDecimal()} Euro",
                modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
              )
            }
            if (fees.euro > 0) {
              Column {
                Text(
                  " ",
                  modifier = Modifier.padding(5.dp),
                  fontSize = 12.sp,
                  color = Color.Gray,
                  textAlign = TextAlign.Center
                )
                Text(
                  "-",
                  modifier = Modifier.padding(10.dp, 5.dp).align(Alignment.CenterHorizontally),
                  fontSize = 14.sp,
                  textAlign = TextAlign.Center
                )
              }
              Column {
                Text(
                  "Opłaty",
                  modifier = Modifier.padding(5.dp),
                  fontSize = 12.sp,
                  color = Color.Gray,
                  textAlign = TextAlign.Center
                )
                Text(
                  "${feesCostPLN.toTwoDecimal()} PLN",
                  modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                  fontSize = 14.sp,
                  textAlign = TextAlign.Center
                )
                Text(
                  "${feesCostEuro.toTwoDecimal()} Euro",
                  modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                  fontSize = 12.sp,
                  color = Color.LightGray,
                  textAlign = TextAlign.Center
                )
              }
            }
            Column {
              Text(
                " ",
                modifier = Modifier.padding(5.dp),
                fontSize = 12.sp,
                color = Color.Gray
              )
              Text(
                "=",
                modifier = Modifier.padding(10.dp, 5.dp).align(Alignment.CenterHorizontally),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
              )
            }
            Column {
              Text(
                "Dochód",
                modifier = Modifier.padding(5.dp),
                fontSize = 12.sp,
                color = Color.Gray
              )
              Text(
                "${(priceForDaysPLN + fullPricePLN - feesCostPLN).toTwoDecimal()} PLN",
                modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
              )
              Text(
                "${(priceForDaysEuro + fullPriceEuro - feesCostEuro).toTwoDecimal()} Euro",
                modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
      
      Row(modifier = Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
          Column {
            
            Text(
              "Kiedy?",
              modifier = Modifier.padding(5.dp),
              fontSize = 12.sp,
              color = Color.Gray
            )
            
            if (reservation.meta.addCleaningTime) {
              Text(
                "${reservation.from.format()} - ${reservation.to.format()} (${days - 1} dni + 1 sprzątanie)",
                modifier = Modifier.padding(5.dp),
                fontSize = 14.sp
              )
            } else {
              Text(
                "${reservation.from.format()} - ${reservation.to.format()} ($days dni)",
                modifier = Modifier.padding(5.dp),
                fontSize = 14.sp
              )
            }
            
            Text(
              "Meta",
              modifier = Modifier.padding(5.dp),
              fontSize = 12.sp,
              color = Color.Gray
            )
            
            
            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(reservation.meta.addCleaningTime, {}, enabled = false)
              
              Box(Modifier.size(5.dp))
              
              Text(
                "Czas na sprzątanie",
                modifier = Modifier.padding(5.dp),
                fontSize = 14.sp
              )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(reservation.meta.addCleaningCost, {}, enabled = false)
              
              Box(Modifier.size(5.dp))
              
              Text(
                "Koszt sprzątania",
                modifier = Modifier.padding(5.dp),
                fontSize = 14.sp
              )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(reservation.meta.keysIncluded, {}, enabled = false)
              
              Box(Modifier.size(5.dp))
              
              Text(
                "Opłata za wydanie kluczy",
                modifier = Modifier.padding(5.dp),
                fontSize = 14.sp
              )
            }
            
            Text(
              "Cena euro po: ${reservation.meta.euroPrice}",
              modifier = Modifier.padding(5.dp),
              fontSize = 14.sp
            )
            
            
            if (reservation.notes.isNotEmpty()) {
              Text(
                "Uwagi",
                modifier = Modifier.padding(5.dp),
                fontSize = 12.sp,
                color = Color.Gray
              )
              Text(
                reservation.notes,
                modifier = Modifier.padding(5.dp),
                fontSize = 14.sp
              )
            }
          }
        }
        
        Column(Modifier.wrapContentWidth()) {
          var currentDay = reservation.from.plusDays(0)
          val daysList = mutableListOf<LocalDate>()
          
          while (currentDay != reservation.to) {
            daysList.add(currentDay)
            currentDay = currentDay.plusDays(1)
          }
          
          if (reservation.from.year == reservation.to.year && reservation.from.month == reservation.to.month) {
            CalendarMonth(reservation.from.year, reservation.from.month, clickEnabled = { false }, dayMap = {
              when (it) {
                reservation.from -> TileType.Start
                reservation.to -> TileType.End
                in daysList -> TileType.Middle
                else -> TileType.Normal
              }
            })
          } else {
            //Two months
            if (days > 56) {
              Column(Modifier.padding(10.dp)) {
                Text("Kalendarz obejmuje więcej niż 56 dni...", fontSize = 16.sp)
                Text("Sprawdź rezerwację na widoku kalendarza!", fontSize = 16.sp)
              }
              
            } else {
              CalendarMonth(
                reservation.from.year,
                reservation.from.month,
                clickEnabled = { false },
                dayMap = {
                  when (it) {
                    reservation.from -> TileType.Start
                    reservation.to -> TileType.End
                    in daysList -> TileType.Middle
                    else -> TileType.Normal
                  }
                }
              )
              CalendarMonth(
                reservation.to.year,
                reservation.to.month,
                clickEnabled = { false },
                dayMap = {
                  when (it) {
                    reservation.from -> TileType.Start
                    reservation.to -> TileType.End
                    in daysList -> TileType.Middle
                    else -> TileType.Normal
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}