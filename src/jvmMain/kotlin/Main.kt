import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import views.*
import java.io.File
import java.time.LocalDate

val viewsWithHiddenFAB = listOf(CurrentView.Calendar)

class AppData {
  val reservations = mutableStateListOf<Reservation>()
  val clients = mutableStateListOf<Client>()
  
  private val defaultDir = File(".plannerConfig")
  private val clientFile = File(defaultDir, "client.json")
  private val reservationsFile = File(defaultDir, "reservations.json")
  private val json = Json
  
  init {
    if (!defaultDir.exists()) defaultDir.mkdir()
    if (!clientFile.exists()) clientFile.createNewFile()
    if (!reservationsFile.exists()) reservationsFile.createNewFile()
    
    clientFile.readText().let {
      if (it.isNotEmpty()) clients.addAll(
        json.decodeFromString<List<Client>>(clientFile.readText())
      )
    }
    
    reservationsFile.readText().let {
      if (it.isNotEmpty()) reservations.addAll(
        json.decodeFromString<List<Reservation>>(reservationsFile.readText())
      )
    }
  }
  
  val takenDays: List<LocalDate>
    get() = reservations.flatMap {
      val localDates = mutableListOf<LocalDate>()
      
      for (day in it.from.plusDays(-1)..it.to) localDates.add(day)
      
      localDates
    }
  
  fun getClient(by: Reservation): Client = clients.find { it.uuid == by.cid }!!
  
  fun addClient(client: Client) {
    clients.add(client)
    
    updateClients()
  }
  
  fun removeClient(client: Client) {
    clients.removeIf { client.uuid == it.uuid }
    reservations.removeIf { it.cid == client.uuid }
    
    updateReservations()
    updateClients()
  }
  
  fun addReservation(reservation: Reservation) {
    reservations.add(reservation)
    
    updateReservations()
  }
  
  fun removeReservation(reservation: Reservation) {
    reservations.removeIf { reservation.uuid == it.uuid }
    
    updateReservations()
  }
  
  private fun updateClients() = clientFile.writeText(json.encodeToString(clients.toList()))
  private fun updateReservations() = reservationsFile.writeText(json.encodeToString(reservations.toList()))
}

@Composable
@Preview
fun App() {
  val appData = AppData()
  
  var currentView by remember { mutableStateOf(CurrentView.Reservations) }
  
  Row {
    NavigationRail(modifier = Modifier.width(50.dp)) {
      NavigationRailItem(
        selected = currentView == CurrentView.Reservations,
        onClick = { currentView = CurrentView.Reservations },
        icon = { Icon(Icons.Default.List, "Reservations", modifier = Modifier.size(25.dp)) },
        alwaysShowLabel = false
      )
      NavigationRailItem(
        selected = currentView == CurrentView.Clients,
        onClick = { currentView = CurrentView.Clients },
        icon = { Icon(Icons.Default.Person, "Clients", modifier = Modifier.size(25.dp)) },
        alwaysShowLabel = false
      )
      NavigationRailItem(
        selected = currentView == CurrentView.Calendar,
        onClick = { currentView = CurrentView.Calendar },
        icon = { Icon(Icons.Default.DateRange, "Reservations", modifier = Modifier.size(25.dp)) },
        alwaysShowLabel = false
      )
    }
    
    Scaffold(floatingActionButton = {
      if (!viewsWithHiddenFAB.contains(currentView)) {
        val icon = when (currentView) {
          CurrentView.Clients, CurrentView.Reservations -> Icons.Default.Add
          else -> Icons.Default.ArrowBack
        }
        
        FloatingActionButton(onClick = {
          currentView = when (currentView) {
            CurrentView.Clients -> CurrentView.AddClient
            CurrentView.AddClient -> CurrentView.Clients
            CurrentView.Reservations -> CurrentView.AddReservation
            CurrentView.AddReservation -> CurrentView.Reservations
            else -> currentView
          }
        }) {
          Icon(icon, "Add", modifier = Modifier.size(25.dp))
        }
      }
    }) {
      when (currentView) {
        CurrentView.Reservations -> ReservationView(appData)
        CurrentView.AddReservation -> AddReservationView(appData) { currentView = CurrentView.Reservations }
        CurrentView.Clients -> ClientView(appData)
        CurrentView.AddClient -> AddClientView(appData) { currentView = it ?: CurrentView.Clients }
        CurrentView.Calendar -> CalendarView(appData)
      }
    }
  }
}

fun main() = application {
  Window(onCloseRequest = ::exitApplication, resizable = false, title = "Planner") {
    App()
  }
}

enum class CurrentView {
  Reservations, AddReservation,
  Clients, AddClient,
  Calendar,
}