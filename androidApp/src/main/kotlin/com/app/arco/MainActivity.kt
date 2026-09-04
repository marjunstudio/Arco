package com.app.arco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.arco.app.ArcoApp
import com.app.arco.app.ArcoAppGraph

// Activity の再生成をまたいで生き残る必要があるため、Activity の外に置く。
// DI を入れたらここが差し替わる。
private val appGraph = ArcoAppGraph()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ArcoApp(graph = appGraph)
        }
    }
}
