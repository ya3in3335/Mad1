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
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * End-to-end smoke test on the JVM (Robolectric): boots the real app with its Room database,
 * plays the motion splash, goes through onboarding and places a cash-on-delivery order.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AppSmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val shotsDir = System.getProperty("madak.screenshots.dir")?.let(::File)

    private fun settle(ms: Long) {
        var left = ms
        while (left > 0) {
            compose.mainClock.advanceTimeBy(50)
            Thread.sleep(8)
            left -= 50
        }
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

    private fun waitForText(text: String, timeoutMs: Long = 8_000) {
        var waited = 0L
        while (compose.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()) {
            check(waited < timeoutMs) { "Text not found: $text" }
            settle(100); waited += 100
        }
    }

    @Test
    fun splashOnboardingAndCheckout() {
        compose.mainClock.autoAdvance = false

        // Motion splash keyframes
        settle(700); shot("01_splash_chef")
        settle(900); shot("02_splash_wordmark")
        settle(800); shot("03_splash_leaf_shimmer")
        settle(500); shot("04_splash_tagline")
        settle(1_500)

        // Onboarding
        waitForText("توابل طازجة بنكهة أصيلة")
        settle(400); shot("05_onboarding")
        compose.onNodeWithText("تخطي").performClick()

        // Home: the store starts EMPTY (no fake products)
        waitForText("المتجر قيد التجهيز")
        settle(600); shot("06_home_empty")

        // The owner adds a real product (same repository the Admin app uses)
        val app = compose.activity.application
        kotlinx.coroutines.runBlocking {
            dagger.hilt.android.EntryPointAccessors.fromApplication(app, CatalogEntryPoint::class.java).adminRepository().saveProduct(
                com.madak.spices.data.local.entity.ProductEntity(
                    id = "cumin", categoryId = "spices", nameAr = "كمون", nameFr = "Cumin",
                    descriptionAr = "كمون مطحون طازج.", descriptionFr = "Cumin moulu.", colorArgb = 0xFF9C6B30L,
                    isBestSeller = true, tags = "كمون,cumin",
                ),
                pricePer100g = 250, initialStock = 40,
            )
        }
        waitForText("كمون")
        settle(800); shot("07_home_with_product")

        // What to cook today → meat suggests cumin
        compose.onAllNodesWithText("لحم").onFirst().performClick()
        waitForText("نصيحة الشيف")
        waitForText("كمون")
        settle(800); shot("08_cook_today")
        compose.onNodeWithText("أضف الكل إلى السلة").performClick()
        settle(800)
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        settle(600)

        // Products tab → product detail
        compose.onAllNodesWithText("المنتجات").onFirst().performClick()
        waitForText("منتجاتنا")
        settle(800); shot("09_products")
        compose.onAllNodesWithText("كمون").onFirst().performClick()
        waitForText("اختر الوزن")
        settle(800); shot("10_product_detail")
        compose.onNodeWithText("أضف إلى السلة").performClick()
        settle(800)
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        settle(600)

        // Cart
        compose.onAllNodesWithText("السلة").onFirst().performClick()
        waitForText("سلة التسوق")
        waitForText("إتمام الطلب")
        settle(800); shot("11_cart")
        compose.onNodeWithText("إتمام الطلب").performClick()

        // Checkout
        waitForText("الدفع عند الاستلام")
        compose.onNode(hasText("الولاية")).performClick()
        waitForText("01 - أدرار")
        settle(500); shot("12_wilaya_sheet")
        compose.onAllNodes(hasSetTextAction()).onLast().performTextReplacement("الجزائر")
        waitForText("16 - الجزائر")
        compose.onNodeWithText("16 - الجزائر").performClick()
        settle(600)
        compose.onNode(hasSetTextAction() and hasText("الاسم الكامل")).performTextReplacement("أمينة بن علي")
        compose.onNode(hasSetTextAction() and hasText("رقم الهاتف")).performTextReplacement("0551234567")
        compose.onNode(hasSetTextAction() and hasText("البلدية")).performTextReplacement("باب الزوار")
        compose.onNode(hasSetTextAction() and hasText("العنوان")).performTextReplacement("حي 5 جويلية، عمارة 3")
        settle(300); shot("13_checkout")
        compose.onAllNodesWithText("تأكيد الطلب", substring = true).onFirst().performClick()

        // Confirmation → tracking
        waitForText("تم استلام طلبك!")
        settle(1_500); shot("14_confirmation")
        compose.onNodeWithText("تتبع الطلب").performClick()
        waitForText("التوصيل إلى")
        settle(800); shot("15_tracking")

        // Profile
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        settle(600)
        compose.onAllNodesWithText("حسابي").onFirst().performClick()
        waitForText("أمينة بن علي")
        settle(800); shot("16_profile")
    }
}
