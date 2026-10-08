package com.madak.spices.admin

import android.graphics.Bitmap
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Boots the Admin app: motion splash, first-run owner setup (no default credentials), dashboard, products. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AdminSmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<AdminActivity>()

    private val shotsDir = System.getProperty("madak.screenshots.dir")?.let(::File)

    private fun settle(ms: Long) {
        var left = ms
        while (left > 0) { compose.mainClock.advanceTimeBy(50); Thread.sleep(8); left -= 50 }
    }

    private fun shot(name: String) {
        val dir = shotsDir ?: return
        dir.mkdirs()
        compose.activityRule.scenario.onActivity { activity ->
            val view = activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        var waited = 0L
        while (compose.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()) {
            check(waited < timeoutMs) { "Text not found: $text" }
            settle(100); waited += 100
        }
    }

    @Test
    fun setupOwnerAndManageCatalogue() {
        compose.mainClock.autoAdvance = false
        settle(2_400); shot("a01_admin_splash")
        settle(2_000)

        // First run: owner must create credentials (weak password rejected)
        waitForText("إنشاء الحساب")
        compose.onNode(hasSetTextAction() and hasText("اسم المسؤول")).performTextReplacement("ياسين")
        compose.onNode(hasSetTextAction() and hasText("كلمة المرور")).performTextReplacement("short")
        compose.onNode(hasSetTextAction() and hasText("تأكيد كلمة المرور")).performTextReplacement("short")
        compose.onNodeWithText("إنشاء الحساب").performSemanticsAction(SemanticsActions.OnClick)
        waitForText("كلمة المرور يجب أن تحتوي على 8 أحرف على الأقل مع حروف وأرقام")
        settle(300); shot("a02_admin_setup")

        compose.onNode(hasSetTextAction() and hasText("كلمة المرور")).performTextReplacement("Madak2026")
        compose.onNode(hasSetTextAction() and hasText("تأكيد كلمة المرور")).performTextReplacement("Madak2026")
        compose.onNodeWithText("إنشاء الحساب").performSemanticsAction(SemanticsActions.OnClick)

        // Dashboard on an empty store
        waitForText("طلبات اليوم")
        waitForText("لا توجد مبيعات بعد")
        settle(1_200); shot("a03_admin_dashboard")

        // Products section → add a real product
        compose.onNode(androidx.compose.ui.test.hasContentDescription("القائمة")).performClick()
        settle(800); shot("a04_admin_drawer")
        compose.onAllNodesWithText("المنتجات").onFirst().performSemanticsAction(SemanticsActions.OnClick)
        settle(800)
        waitForText("لا توجد منتجات بعد")
        settle(800); shot("a05_admin_products_empty")
    }
}
