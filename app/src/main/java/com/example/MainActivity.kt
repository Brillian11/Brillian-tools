package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.ui.ViewModelFactory
import com.example.ui.navigation.NavGraph
import com.example.ui.theme.BrillianToolsTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val factory = remember { ViewModelFactory(applicationContext) }
      val userSettings by factory.settingsRepository.settings.collectAsState()
      val isDarkTheme = !userSettings.isLightMode

      DisposableEffect(isDarkTheme) {
        enableEdgeToEdge(
          statusBarStyle = if (isDarkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
          } else {
            SystemBarStyle.light(
              android.graphics.Color.TRANSPARENT,
              android.graphics.Color.TRANSPARENT
            )
          },
          navigationBarStyle = if (isDarkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
          } else {
            SystemBarStyle.light(
              android.graphics.Color.TRANSPARENT,
              android.graphics.Color.TRANSPARENT
            )
          }
        )
        onDispose {}
      }

      BrillianToolsTheme(darkTheme = isDarkTheme) {
        NavGraph(factory = factory)
      }
    }
  }
}
