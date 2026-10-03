package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

sealed class AssistantAction(val type: String) {
    data class OpenUrl(val url: String) : AssistantAction("OPEN_URL")
    data class OpenApp(val appName: String, val packageName: String?, val fallbackUrl: String) : AssistantAction("OPEN_APP")
    data class SearchWeb(val query: String) : AssistantAction("SEARCH_WEB")
    data class Calculate(val expression: String) : AssistantAction("CALCULATE")
    data class SendNotification(val title: String, val message: String) : AssistantAction("SEND_NOTIFICATION")
    object OpenAccessibilitySettings : AssistantAction("OPEN_ACCESSIBILITY_SETTINGS")
    object OpenNotificationListenerSettings : AssistantAction("OPEN_NOTIFICATION_LISTENER_SETTINGS")
    object OpenOverlayPermissionSettings : AssistantAction("OPEN_OVERLAY_PERMISSION")
    object OpenWriteSettingsPermission : AssistantAction("OPEN_WRITE_SETTINGS")
}

object AppActionManager {

    val SUPPORTED_APPS = listOf(
        "YouTube" to Triple("com.google.android.youtube", "https://youtube.com", "android.resource://"),
        "Gmail" to Triple("com.google.android.gm", "https://mail.google.com", ""),
        "Maps" to Triple("com.google.android.apps.maps", "https://maps.google.com", ""),
        "WhatsApp" to Triple("com.whatsapp", "https://web.whatsapp.com", ""),
        "Telegram" to Triple("org.telegram.messenger", "https://web.telegram.org", ""),
        "Instagram" to Triple("com.instagram.android", "https://instagram.com", ""),
        "Chrome" to Triple("com.android.chrome", "https://google.com", ""),
        "Calculator" to Triple("com.google.android.calculator", "", ""),
        "Calendar" to Triple("com.google.android.calendar", "https://calendar.google.com", ""),
        "Camera" to Triple("", "", "")
    )

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun isWriteSettingsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    fun isNotificationListenerGranted(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(context.packageName)
    }

    fun isAccessibilityEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return enabledServices.contains(context.packageName)
    }

    fun executeAction(context: Context, action: AssistantAction): Boolean {
        return try {
            when (action) {
                is AssistantAction.OpenUrl -> {
                    var finalUrl = action.url.trim()
                    if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                        finalUrl = "https://$finalUrl"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    true
                }
                is AssistantAction.OpenApp -> {
                    if (action.appName.equals("Camera", ignoreCase = true)) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        if (cameraIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(cameraIntent)
                            return true
                        }
                    }

                    if (!action.packageName.isNullOrBlank()) {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(action.packageName)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                            return true
                        }
                    }

                    // Fallback to URL if package is not installed
                    if (action.fallbackUrl.isNotBlank()) {
                        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(action.fallbackUrl)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(fallbackIntent)
                        return true
                    }
                    Toast.makeText(context, "App ${action.appName} is not installed", Toast.LENGTH_SHORT).show()
                    false
                }
                is AssistantAction.SearchWeb -> {
                    val encoded = Uri.encode(action.query)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    true
                }
                is AssistantAction.Calculate -> {
                    val calcIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                    if (calcIntent != null) {
                        calcIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(calcIntent)
                        true
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(action.expression))).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        true
                    }
                }
                is AssistantAction.SendNotification -> {
                    NotificationHelper.showNotification(context, action.title, action.message)
                    true
                }

                // 1. Accessibility Settings खोलना
                is AssistantAction.OpenAccessibilitySettings -> {
                    val accessibilityIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(accessibilityIntent)
                    true
                }

                // 2. Notification Listener Access खोलना
                is AssistantAction.OpenNotificationListenerSettings -> {
                    val notificationIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(notificationIntent)
                    true
                }

                // 3. Draw Over Other Apps Access खोलना
                is AssistantAction.OpenOverlayPermissionSettings -> {
                    val overlayIntent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(overlayIntent)
                    true
                }

                // 4. System Settings Write Access खोलना
                is AssistantAction.OpenWriteSettingsPermission -> {
                    val writeSettingsIntent = Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(writeSettingsIntent)
                    true
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Action failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Parses intent keywords from an AI answer or user input
     */
    fun parseActionFromText(text: String): AssistantAction? {
        val lower = text.lowercase()
        return when {
            // System & Special Settings
            lower.contains("accessibility") || lower.contains("accessibilty") -> {
                AssistantAction.OpenAccessibilitySettings
            }
            lower.contains("notification listener") || lower.contains("notification access") -> {
                AssistantAction.OpenNotificationListenerSettings
            }
            lower.contains("draw over") || lower.contains("overlay permission") || lower.contains("floating window") -> {
                AssistantAction.OpenOverlayPermissionSettings
            }
            lower.contains("write settings") || lower.contains("modify system settings") || lower.contains("system settings write") -> {
                AssistantAction.OpenWriteSettingsPermission
            }

            // Supported native apps
            lower.contains("open youtube") || lower.contains("launch youtube") -> {
                AssistantAction.OpenApp("YouTube", "com.google.android.youtube", "https://youtube.com")
            }
            lower.contains("open gmail") || lower.contains("open email") -> {
                AssistantAction.OpenApp("Gmail", "com.google.android.gm", "https://mail.google.com")
            }
            lower.contains("open whatsapp") -> {
                AssistantAction.OpenApp("WhatsApp", "com.whatsapp", "https://web.whatsapp.com")
            }
            lower.contains("open maps") || lower.contains("open google maps") -> {
                AssistantAction.OpenApp("Maps", "com.google.android.apps.maps", "https://maps.google.com")
            }
            lower.contains("open camera") -> {
                AssistantAction.OpenApp("Camera", "", "")
            }
            lower.contains("open calculator") -> {
                AssistantAction.OpenApp("Calculator", "com.google.android.calculator", "https://www.google.com/search?q=calculator")
            }
            lower.contains("open calendar") -> {
                AssistantAction.OpenApp("Calendar", "com.google.android.calendar", "https://calendar.google.com")
            }
            lower.contains("open telegram") -> {
                AssistantAction.OpenApp("Telegram", "org.telegram.messenger", "https://web.telegram.org")
            }
            lower.contains("open instagram") -> {
                AssistantAction.OpenApp("Instagram", "com.instagram.android", "https://instagram.com")
            }
            lower.contains("open chrome") || lower.contains("open browser") -> {
                AssistantAction.OpenApp("Chrome", "com.android.chrome", "https://google.com")
            }
            else -> null
        }
    }
}
