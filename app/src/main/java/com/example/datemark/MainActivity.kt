package com.example.datemark

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.datemark.navigation.DateMarkNavHost
import com.example.datemark.ui.DateMarkViewModel
import com.example.datemark.ui.theme.DateMarkTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DateMarkViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DateMarkViewModel(application) as T
            }
        }
    }

    private val midnightReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Local date or timezone changed (e.g. across midnight)
            viewModel.onFilterChange(viewModel.uiState.value.filter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
        }
        registerReceiver(midnightReceiver, filter)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            DateMarkTheme(
                appTheme = uiState.settings.theme,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DateMarkNavHost(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh calculations in case date changed while app was in background
        viewModel.onFilterChange(viewModel.uiState.value.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(midnightReceiver)
        } catch (_: Exception) {
            // Already unregistered
        }
    }
}
