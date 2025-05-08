package com.example.kotlintests

import HomeComponent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.kotlintests.stores.HomeStore
import com.example.kotlintests.ui.theme.KotlinTestsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinTestsTheme {
                val store: HomeStore = androidx.lifecycle.viewmodel.compose.viewModel()

                Scaffold(
                    floatingActionButton  = {
                        FloatingActionButton(
                            onClick = {store.clearMessageList()},
                            containerColor = MaterialTheme.colorScheme.primary,
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "",
                                    tint = Color.White,
                                )
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize().systemBarsPadding()) { innerPadding ->
                    HomeComponent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
