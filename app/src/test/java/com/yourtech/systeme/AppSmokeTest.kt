package com.yourtech.systeme

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Customer app: splash → home → solutions → empty catalogue → installation request → tracking → contact. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AppSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val h by lazy { ComposeHarness(compose) }

    private fun click(text: String) = compose.onAllNodesWithText(text).onFirst().performSemanticsAction(SemanticsActions.OnClick)
    private fun type(label: String, value: String) = compose.onNode(hasSetTextAction() and hasText(label)).apply { performScrollTo() }.performTextReplacement(value)

    @Test
    fun browseAndSubmitInstallationRequest() {
        compose.mainClock.autoAdvance = false
        h.settle(2_600); h.shot("c01_splash")
        h.settle(2_000)
        h.waitForText("احمِ ما يهمّك بتقنية احترافية")
        h.settle(1_500); h.shot("c02_home")

        click("الحلول")
        h.waitForText("كاميرات المراقبة")
        h.settle(1_200); h.shot("c03_solutions")
        click("كاميرات المراقبة")
        h.waitForText("المزايا")
        h.settle(1_200); h.shot("c04_solution_cctv")
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        h.settle(800)
        h.waitForText("المعدات")
        click("المعدات")
        h.waitForText("الكتالوج قيد التحضير")
        h.settle(1_000); h.shot("c05_products_empty")

        click("الرئيسية")
        h.waitForText("طلب تركيب")
        click("طلب تركيب")
        h.waitForText("نوع العقار")
        h.settle(600)
        click("كاميرات المراقبة")
        click("منزل / فيلا")
        type("عدد الكاميرات / الأجهزة", "6")
        type("الاسم الكامل", "ياسين بن علي")
        type("رقم الهاتف", "0661234567")
        type("البلدية", "بئر الجير")
        type("عنوان التركيب", "حي 500 مسكن، عمارة 3")
        compose.onNode(hasClickAction() and hasContentDescription("الولاية")).apply { performScrollTo() }.performSemanticsAction(SemanticsActions.OnClick)
        h.settle(600)
        compose.onNode(hasSetTextAction() and hasText("ابحث بالاسم أو الرقم")).performTextReplacement("31")
        h.waitForText("31 - وهران")
        h.settle(600); h.shot("c06_wilaya_sheet")
        compose.onNodeWithText("31 - وهران").performSemanticsAction(SemanticsActions.OnClick)
        h.settle(800); h.shot("c07_request_form")
        click("إرسال الطلب")
        h.waitForText("تم تسجيل طلبك")
        h.settle(1_200); h.shot("c08_request_sent")
        click("تتبع الطلب")
        h.waitForText("YT-INS-", substring = true)
        h.settle(1_200); h.shot("c09_request_tracking")

        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        h.settle(600)
        click("المزيد")
        h.waitForText("التواصل والموقع")
        h.settle(800); h.shot("c10_more")
        click("التواصل والموقع")
        h.waitForText("+213 561 03 41 49", substring = true)
        h.settle(1_200); h.shot("c11_contact")
    }
}
