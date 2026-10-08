package com.yourtech.systeme

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import java.io.File

/** Manual clock stepping + decor-view screenshots (Robolectric native graphics). */
class ComposeHarness<A : ComponentActivity>(private val compose: AndroidComposeTestRule<ActivityScenarioRule<A>, A>, property: String = "yt.screenshots.dir") {
    private val dir = System.getProperty(property)?.let(::File)

    fun settle(ms: Long) {
        var left = ms
        while (left > 0) { compose.mainClock.advanceTimeBy(50); Thread.sleep(6); left -= 50 }
    }

    fun shot(name: String) {
        val d = dir ?: return
        d.mkdirs()
        compose.activityRule.scenario.onActivity { activity ->
            val view = activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(d, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    fun waitForText(text: String, timeoutMs: Long = 15_000, substring: Boolean = false) {
        var waited = 0L
        while (compose.onAllNodesWithText(text, substring = substring, useUnmergedTree = true).fetchSemanticsNodes().isEmpty()) {
            check(waited < timeoutMs) { "Text not found: $text" }
            settle(100); waited += 100
        }
    }
}
