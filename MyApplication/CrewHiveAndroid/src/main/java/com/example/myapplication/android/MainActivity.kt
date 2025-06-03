// FILE: MainActivity.kt
package com.example.myapplication.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myapplication.android.state.*
import com.example.myapplication.android.ui.components.BottomNavigationBarComponent.BottomNavigationBar
import com.example.myapplication.android.ui.components.DrawerContent
import com.example.myapplication.android.ui.components.ShiftTemplate
import com.example.myapplication.android.ui.screens.*
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                var currentScreen by remember { mutableStateOf("Home") }
                var showingSendScreen by remember { mutableStateOf(false) }
                var selectedNotification by remember { mutableStateOf<NotificationData?>(null) }
                var screenSource by remember { mutableStateOf(NavigationSource.Drawer) }

                // Stato condiviso calendario
                val calendarState = remember {
                    CalendarState(
                        selectedDate = mutableStateOf(Calendar.getInstance()),
                        userEvents = mutableStateListOf()
                    )
                }

                // Stato condiviso template
                val templateState = remember { mutableStateListOf<ShiftTemplate>() }

                // Stato simulato dell'utente loggato
                val currentUser = remember { mutableStateOf("Giulia Verdi") }

                CompositionLocalProvider(
                    LocalCalendarState provides calendarState,
                    LocalTemplateState provides templateState,
                    LocalCurrentUser provides currentUser
                ) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(
                                onClose = { coroutineScope.launch { drawerState.close() } },
                                onDestinationSelected = {
                                    currentScreen = it
                                    screenSource = NavigationSource.Drawer
                                    showingSendScreen = false
                                    selectedNotification = null
                                }
                            )
                        },
                        content = {
                            Scaffold(
                                bottomBar = {
                                    BottomNavigationBar(
                                        currentScreen = currentScreen,
                                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                        onTabSelected = {
                                            currentScreen = it
                                            screenSource = NavigationSource.BottomBar
                                            showingSendScreen = false
                                            selectedNotification = null
                                        }
                                    )
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                        .background(Color.White)
                                ) {
                                    when (currentScreen) {
                                        "Home" -> HomeScreen()
                                        "Calendar" -> CalendarScreen(screenSource = screenSource)
                                        "Profile" -> ProfileScreen()
                                        "Notifications" -> {
                                            if (selectedNotification != null) {
                                                NotificationDetailScreen(
                                                    notification = selectedNotification!!,
                                                    onBackClick = { selectedNotification = null }
                                                )
                                            } else {
                                                NotificationScreen(
                                                    onEditClick = { showingSendScreen = true },
                                                    onNotificationClick = { notification ->
                                                        selectedNotification = notification
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (showingSendScreen) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.White)
                                        ) {
                                            NotificationSendScreen(
                                                onBackClick = { showingSendScreen = false },
                                                onSendClick = { showingSendScreen = false }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class NavigationSource {
    Drawer, BottomBar
}
