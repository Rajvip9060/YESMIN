package com.example

import com.example.data.local.entity.UserSettingsEntity
import com.example.util.AppActionManager
import com.example.util.AssistantAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDefaultAssistantIdentity() {
        val defaultSettings = UserSettingsEntity()
        assertEquals("YESMIN AI", defaultSettings.assistantName)
        assertEquals("Jaan", defaultSettings.userNickname)
        assertEquals("Girlfriend / Companion", defaultSettings.personalityMode)
        assertTrue(defaultSettings.memoryEnabled)
    }

    @Test
    fun testCustomNameUpdate() {
        val defaultSettings = UserSettingsEntity()
        val customSettings = defaultSettings.copy(assistantName = "Muskan")
        assertEquals("Muskan", customSettings.assistantName)
    }

    @Test
    fun testActionParsing() {
        val actionYoutube = AppActionManager.parseActionFromText("Can you please open youtube for me?")
        assertNotNull(actionYoutube)
        assertTrue(actionYoutube is AssistantAction.OpenApp)
        assertEquals("YouTube", (actionYoutube as AssistantAction.OpenApp).appName)

        val actionWhatsapp = AppActionManager.parseActionFromText("Open whatsapp and message my friend")
        assertNotNull(actionWhatsapp)
        assertTrue(actionWhatsapp is AssistantAction.OpenApp)
        assertEquals("WhatsApp", (actionWhatsapp as AssistantAction.OpenApp).appName)

        val actionCalc = AppActionManager.parseActionFromText("Open calculator")
        assertNotNull(actionCalc)
        assertEquals("Calculator", (actionCalc as AssistantAction.OpenApp).appName)

        // 1. Accessibility Settings
        val actionAccessibility = AppActionManager.parseActionFromText("Open accessibility settings please")
        assertEquals(AssistantAction.OpenAccessibilitySettings, actionAccessibility)

        // 2. Notification Listener Access
        val actionNotif = AppActionManager.parseActionFromText("Open notification listener access")
        assertEquals(AssistantAction.OpenNotificationListenerSettings, actionNotif)

        // 3. Draw Over Other Apps Access
        val actionOverlay = AppActionManager.parseActionFromText("Enable draw over other apps permission")
        assertEquals(AssistantAction.OpenOverlayPermissionSettings, actionOverlay)

        // 4. System Settings Write Access
        val actionWrite = AppActionManager.parseActionFromText("Grant system settings write permission")
        assertEquals(AssistantAction.OpenWriteSettingsPermission, actionWrite)
    }
}
