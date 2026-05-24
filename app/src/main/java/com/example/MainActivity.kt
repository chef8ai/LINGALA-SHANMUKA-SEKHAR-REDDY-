package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.FridgeChefMainScreen
import com.example.ui.screens.GmailAuthScreen
import com.example.viewmodel.FridgeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FridgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                if (isLoggedIn) {
                    FridgeChefMainScreen(viewModel = viewModel)
                } else {
                    GmailAuthScreen(viewModel = viewModel)
                }
            }
        }
    }
}
