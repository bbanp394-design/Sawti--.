package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.correction.SmartCorrector
import com.example.data.db.AppDatabase
import com.example.data.repository.PhraseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sawti", appName)
  }

  @Test
  fun `smart corrector fixes common handwriting mistakes`() {
    // Arabic tests
    val arabicCorrection = SmartCorrector.checkCorrection("مء", "ar")
    assertNotNull(arabicCorrection)
    assertEquals("ماء", arabicCorrection?.suggestedText)

    val greetingCorrection = SmartCorrector.checkCorrection("مرحبه", "ar")
    assertNotNull(greetingCorrection)
    assertEquals("مرحبا", greetingCorrection?.suggestedText)

    // English tests
    val englishCorrection = SmartCorrector.checkCorrection("watr", "en")
    assertNotNull(englishCorrection)
    assertEquals("water", englishCorrection?.suggestedText)

    val needCorrection = SmartCorrector.checkCorrection("i ned watr", "en")
    assertNotNull(needCorrection)
    assertEquals("I need water", needCorrection?.suggestedText)

    // Chinese tests
    val chineseCorrection = SmartCorrector.checkCorrection("需水", "zh")
    assertNotNull(chineseCorrection)
    assertEquals("我需要水", chineseCorrection?.suggestedText)
  }

  @Test
  fun `phrase repository seeds default accessibility phrases`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val repo = PhraseRepository(db.phraseDao())

    repo.checkAndSeedDefaults("ar")
    val arPhrases = repo.getPhrases("ar").first()
    assertTrue(arPhrases.isNotEmpty())
    assertTrue(arPhrases.any { it.text == "نعم" })
    assertTrue(arPhrases.any { it.text == "أريد ماء" })
    assertTrue(arPhrases.any { it.text == "أشعر بالألم" })
  }
}
