package com.madak.spices

import android.graphics.Bitmap
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.madak.spices.data.local.entity.ProductEntity
import dagger.hilt.android.EntryPointAccessors
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders marketing screenshots for the promo video with an illustrative catalogue.
 * Not part of the normal test run: ./gradlew :app:testDebugUnitTest -PmadakAd=true --tests "*AdScreenshotsTest"
 * The sample products exist only inside this test; the shipped app still starts empty.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AdScreenshotsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val dir = File(System.getProperty("madak.screenshots.dir") ?: "build/outputs/screenshots", "ad")

    private fun settle(ms: Long) {
        var left = ms
        while (left > 0) { compose.mainClock.advanceTimeBy(50); Thread.sleep(8); left -= 50 }
    }

    private fun shot(name: String) {
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

    private fun back() {
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        settle(700)
    }

    @Test
    fun renderAdScreens() {
        assumeTrue(System.getProperty("madak.ad") == "true")
        compose.mainClock.autoAdvance = false
        settle(5_500)
        waitForText("تخطي")
        compose.onNodeWithText("تخطي").performClick()
        waitForText("المتجر قيد التجهيز")

        val admin = EntryPointAccessors.fromApplication(compose.activity.application, CatalogEntryPoint::class.java).adminRepository()
        data class P(val id: String, val cat: String, val ar: String, val fr: String, val color: Long, val price: Int, val best: Boolean, val featured: Boolean, val desc: String)
        listOf(
            P("ras_el_hanout", "blends", "رأس الحانوت", "Ras el hanout", 0xFFA4452AL, 420, true, true, "خلطة تقليدية غنية للشوربة والطواجن."),
            P("cumin", "spices", "كمون", "Cumin", 0xFF9C6B30L, 250, true, false, "كمون مطحون طازج برائحة دافئة."),
            P("chicken_mix", "blends", "خلطة الدجاج", "Mélange poulet", 0xFFD98A2BL, 350, true, true, "تجعل الدجاج ذهبياً ومتبلاً."),
            P("turmeric", "spices", "كركم", "Curcuma", 0xFFE3A21AL, 220, false, true, "كركم ذهبي نقي."),
            P("paprika", "spices", "بابريكا", "Paprika", 0xFFC8371FL, 260, false, true, "بابريكا حمراء حلوة."),
            P("herbs", "herbs", "أعشاب طبيعية", "Herbes", 0xFF5E8C31L, 280, false, true, "أعشاب مجففة طبيعياً."),
        ).forEach { p ->
            runBlocking {
                admin.saveProduct(
                    ProductEntity(
                        id = p.id, categoryId = p.cat, nameAr = p.ar, nameFr = p.fr, descriptionAr = p.desc, descriptionFr = "",
                        colorArgb = p.color, isBestSeller = p.best, isFeatured = p.featured, tags = "${p.ar},${p.fr}",
                    ),
                    pricePer100g = p.price, initialStock = 50,
                )
            }
        }
        waitForText("رأس الحانوت")
        settle(1_200); shot("ad_home")

        compose.onAllNodesWithText("دجاج").onFirst().performClick()
        waitForText("نصيحة الشيف"); waitForText("خلطة الدجاج")
        settle(1_000); shot("ad_cook")
        compose.onNodeWithText("أضف الكل إلى السلة").performClick()
        settle(800); back()

        compose.onAllNodesWithText("المنتجات").onFirst().performClick()
        waitForText("منتجاتنا")
        settle(1_000); shot("ad_products")
        compose.onAllNodesWithText("رأس الحانوت").onFirst().performClick()
        waitForText("اختر الوزن")
        settle(1_000); shot("ad_detail")
        compose.onNodeWithText("أضف إلى السلة").performClick()
        settle(800); back()

        compose.onAllNodesWithText("السلة").onFirst().performClick()
        waitForText("إتمام الطلب")
        settle(1_000); shot("ad_cart")
        compose.onNodeWithText("إتمام الطلب").performClick()
        waitForText("الدفع عند الاستلام")
        compose.onNode(hasText("الولاية")).performClick()
        waitForText("01 - أدرار")
        compose.onAllNodes(hasSetTextAction()).onLast().performTextReplacement("الجزائر")
        waitForText("16 - الجزائر")
        compose.onNodeWithText("16 - الجزائر").performClick()
        settle(600)
        compose.onNode(hasSetTextAction() and hasText("الاسم الكامل")).performTextReplacement("أمينة ب.")
        compose.onNode(hasSetTextAction() and hasText("رقم الهاتف")).performTextReplacement("0550000000")
        compose.onNode(hasSetTextAction() and hasText("البلدية")).performTextReplacement("الدار البيضاء")
        compose.onNode(hasSetTextAction() and hasText("العنوان")).performTextReplacement("حي النخيل، عمارة 3")
        settle(600); shot("ad_checkout")
        compose.onAllNodesWithText("تأكيد الطلب", substring = true).onFirst().performClick()
        waitForText("تم استلام طلبك!")
        settle(1_500); shot("ad_confirm")
        compose.onNodeWithText("تتبع الطلب").performClick()
        waitForText("التوصيل إلى")
        settle(1_000); shot("ad_tracking")
    }
}
