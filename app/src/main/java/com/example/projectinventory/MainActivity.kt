package com.example.projectinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectinventory.data.InventoryViewModel
import com.example.projectinventory.ui.screens.InventoryScreen
import com.example.projectinventory.ui.theme.ProJectInventoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProJectInventoryTheme {
                val viewModel: InventoryViewModel = viewModel()
                InventoryScreen(viewModel = viewModel)
            }
        }
    }
}
