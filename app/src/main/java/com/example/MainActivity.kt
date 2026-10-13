package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.PhysicsRepository
import com.example.notification.NotificationHelper
import com.example.ui.MainScreen
import com.example.ui.theme.PhysicsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize local notification channel and reschedule alarms
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.rescheduleAllAlarms(this)

        val database = AppDatabase.getDatabase(this)
        val repository = PhysicsRepository(
            problemDao = database.problemDao(),
            quizDao = database.quizDao(),
            achievementDao = database.achievementDao(),
            progressDao = database.progressDao(),
            topicDao = database.topicDao()
        )

        lifecycleScope.launch {
            repository.ensureDefaultAchievements()
            repository.ensureDefaultTopics()
        }

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }

            PhysicsTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        repository = repository,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}
