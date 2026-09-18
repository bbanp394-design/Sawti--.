package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.SpeechControls
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun sawti_speech_controls_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        SpeechControls(
          isSpeaking = false,
          hasText = true,
          speechSpeed = 0.85f,
          autoSpeakEnabled = false,
          language = "ar",
          onSpeak = {},
          onStop = {},
          onRepeat = {},
          onSpeedChange = {},
          onToggleAutoSpeak = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/sawti_controls.png")
  }
}
