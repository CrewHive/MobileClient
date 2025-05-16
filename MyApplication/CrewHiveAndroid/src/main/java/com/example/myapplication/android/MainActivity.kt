package com.example.myapplication.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.android.ui.screens.HomeScreen
import com.example.myapplication.android.ui.screens.NotificationScreen
import com.example.myapplication.android.ui.screens.NotificationSendScreen
import com.example.myapplication.android.ui.screens.NotificationDetailScreen
import com.example.myapplication.android.ui.screens.NotificationData
import com.example.myapplication.android.MyApplicationTheme
import com.example.myapplication.android.ui.components.BottomNavigationBarComponent.BottomNavigationBar
import com.example.myapplication.android.ui.components.DrawerContentComponent.DrawerContent
import com.example.myapplication.android.ui.screens.CalendarScreen
import kotlinx.coroutines.launch

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

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent { coroutineScope.launch { drawerState.close() } }
                    },
                    content = {
                        Scaffold(
                            bottomBar = {
                                BottomNavigationBar(
                                    currentScreen = currentScreen,
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                    onTabSelected = {
                                        currentScreen = it
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
                                    .background(Color.White) // Sfondo uniforme per evitare trasparenze
                            ) {
                                when (currentScreen) {
                                    "Home" -> HomeScreen()
                                    "Calendar" -> CalendarScreen()
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
                                            .background(Color.White) // Sfondo pieno per schermata invio
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

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        HomeScreen()
    }
}