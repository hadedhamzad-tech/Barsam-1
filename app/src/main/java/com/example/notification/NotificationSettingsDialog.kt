package com.example.notification

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.util.PersianUtils

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(NotificationHelper.getPreferences(context)) }
    var isEnabled by remember { mutableStateOf(prefs.isEnabled) }
    var reminderTimes by remember { mutableStateOf(prefs.times) }

    var showTimePicker by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var canScheduleExact by remember {
        mutableStateOf(NotificationHelper.canScheduleExactAlarms(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            NotificationHelper.rescheduleAllAlarms(context)
            Toast.makeText(context, "مجوز اعلان با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "برای دریافت یادآور، نیاز به تأیید مجوز اعلان است", Toast.LENGTH_LONG).show()
        }
    }

    fun saveAndSchedule(newTimes: List<ReminderTime>, enabled: Boolean = isEnabled) {
        val updated = ReminderPreferences(isEnabled = enabled, times = newTimes)
        prefs = updated
        reminderTimes = newTimes
        isEnabled = enabled
        NotificationHelper.savePreferences(context, updated)
        if (enabled) {
            NotificationHelper.rescheduleAllAlarms(context)
        } else {
            NotificationHelper.cancelAllAlarms(context, updatePrefs = false)
        }
    }

    val presetOptions = listOf(
        ReminderTime(9, 0),
        ReminderTime(14, 0),
        ReminderTime(20, 0),
        ReminderTime(22, 0)
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PhysicsCyan.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsCyan.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = PhysicsCyan,
                    modifier = Modifier.padding(10.dp).size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "تنظیمات یادآور هوشمند فیزیک",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ارسال خودکار و روزانه فرمول‌های کلیدی و چالش‌های مفهومی دبیرستان برای تثبیت مطالب در حافظه بلندمدت.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Permission Warning & Settings Navigation (Android 13+ / Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PhysicsRed.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = PhysicsRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "مجوز اعلان‌ها غیرفعال است",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsRed
                                )
                            }
                            Text(
                                "برای نمایش یادآور در پس‌زمینه و بالای صفحه، مجوز اعلان لازم است.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PhysicsRed),
                                modifier = Modifier.fillMaxWidth().height(34.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("درخواست و اعطای مجوز اعلان", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExact) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PhysicsAmber.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = PhysicsAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "مجوز آلارم دقیق (Exact Alarm)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PhysicsAmber
                                )
                            }
                            Text(
                                "برای ارسال سر ساعت در حالت ذخیره باتری، اجازه آلارم دقیق را فعال کنید.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val appIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(appIntent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(34.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("رفتن به تنظیمات آلارم دقیق", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PhysicsAmber)
                            }
                        }
                    }
                }

                // Enable / Disable Switch Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "فعال‌سازی یادآور روزانه",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEnabled) "یادآور فعال است (${PersianUtils.formatInt(reminderTimes.size)} زمان تنظیم‌شده)" else "یادآور خاموش است",
                                fontSize = 11.sp,
                                color = if (isEnabled) PhysicsGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                saveAndSchedule(reminderTimes, enabled = checked)
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBackground,
                                checkedTrackColor = PhysicsCyan
                            )
                        )
                    }
                }

                // Configured Active Reminder Times (up to 3)
                if (isEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "زمان‌های فعال (حداکثر ۳ یادآور):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${PersianUtils.formatInt(reminderTimes.size)} از ۳",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PhysicsCyan
                            )
                        }

                        // FlowRow or Wrap so buttons never truncate or clip
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            reminderTimes.forEach { time ->
                                InputChip(
                                    selected = true,
                                    onClick = { },
                                    label = {
                                        Text(
                                            time.toPersianFormatted(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PhysicsCyan
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = PhysicsCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    trailingIcon = if (reminderTimes.size > 1) {
                                        {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "حذف این ساعت",
                                                tint = PhysicsRed,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        val newList = reminderTimes.filter { it != time }
                                                        saveAndSchedule(newList)
                                                    }
                                            )
                                        }
                                    } else null,
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = PhysicsCyan.copy(alpha = 0.18f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Quick Presets and Custom Time
                        Text(
                            text = "افزودن یا تغییر زمان:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // FlowRow for preset hours + Custom Time Button
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presetOptions.forEach { preset ->
                                val isAlreadyAdded = reminderTimes.any { it.hour == preset.hour && it.minute == preset.minute }
                                FilterChip(
                                    selected = isAlreadyAdded,
                                    onClick = {
                                        if (isAlreadyAdded) {
                                            if (reminderTimes.size > 1) {
                                                saveAndSchedule(reminderTimes.filter { !(it.hour == preset.hour && it.minute == preset.minute) })
                                            }
                                        } else {
                                            if (reminderTimes.size < NotificationHelper.MAX_REMINDERS) {
                                                saveAndSchedule((reminderTimes + preset).distinct())
                                            } else {
                                                // Replace or alert
                                                saveAndSchedule((reminderTimes.drop(1) + preset).distinct())
                                            }
                                        }
                                    },
                                    label = {
                                        Text(
                                            preset.toPersianFormatted(),
                                            fontSize = 11.sp,
                                            fontWeight = if (isAlreadyAdded) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PhysicsCyan.copy(alpha = 0.25f),
                                        selectedLabelColor = PhysicsCyan
                                    )
                                )
                            }

                            // Custom Time Picker Button
                            AssistChip(
                                onClick = {
                                    showTimePicker = true
                                },
                                label = {
                                    Text(
                                        "⏰ ساعت دلخواه...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PhysicsAmber
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.AddAlarm,
                                        contentDescription = null,
                                        tint = PhysicsAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsAmber.copy(alpha = 0.6f))
                            )
                        }
                    }
                }

                // Instant Test Notification Button
                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val reminder = PhysicsReminderData.getRandomReminder()
                            NotificationHelper.showDailyNotification(context, reminder)
                            Toast.makeText(context, "نوتیفیکیشن آزمایشی ارسال شد!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PhysicsAmber
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicsAmber.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ارسال فوری اعلان آزمایشی (تست)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("تأیید و ذخیره", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )

    // Material 3 TimePicker Dialog for custom time
    if (showTimePicker) {
        val initialHour = reminderTimes.firstOrNull()?.hour ?: 18
        val initialMinute = reminderTimes.firstOrNull()?.minute ?: 30
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    "انتخاب ساعت دلخواه یادآوری",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhysicsCyan
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newTime = ReminderTime(timePickerState.hour, timePickerState.minute)
                        val newList = if (reminderTimes.size >= NotificationHelper.MAX_REMINDERS) {
                            (reminderTimes.drop(1) + newTime).distinct()
                        } else {
                            (reminderTimes + newTime).distinct()
                        }
                        saveAndSchedule(newList)
                        showTimePicker = false
                        Toast.makeText(
                            context,
                            "یادآور ساعت ${newTime.toPersianFormatted()} افزوده شد",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhysicsCyan)
                ) {
                    Text("ثبت این ساعت", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
