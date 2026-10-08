package com.yourtech.systeme.admin

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Admin app: splash → first-run owner setup (no default credentials) → dashboard → services → products → business info → audit log. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AdminSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<AdminActivity>()
    private val h by lazy { ComposeHarness(compose) }

    private fun click(text: String) = compose.onAllNodesWithText(text).onFirst().performSemanticsAction(SemanticsActions.OnClick)
    private fun type(label: String, value: String) = compose.onNode(hasSetTextAction() and hasText(label)).apply { performScrollTo() }.performTextReplacement(value)
    private fun back() { compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }; h.settle(800) }

    @Test
    fun ownerSetupAndBackOffice() {
        compose.mainClock.autoAdvance = false
        h.settle(2_400); h.shot("a01_splash")
        h.settle(2_400)

        h.waitForText("إنشاء حساب المالك")
        type("اسم المستخدم (لاتيني)", "owner")
        type("الاسم الظاهر", "ياسين")
        type("كلمة المرور", "short")
        type("تأكيد كلمة المرور", "short")
        click("إنشاء الحساب والدخول")
        h.waitForText("كلمة المرور: 8 أحرف على الأقل مع حرف ورقم")
        h.settle(400); h.shot("a02_owner_setup")
        type("كلمة المرور", "Yourtech2026")
        type("تأكيد كلمة المرور", "Yourtech2026")
        click("إنشاء الحساب والدخول")

        h.waitForText("مرحبًا ياسين", 30_000)
        h.settle(1_200); h.shot("a03_dashboard")

        click("الكتالوج")
        h.waitForText("الخدمات والحلول")
        click("الخدمات والحلول")
        h.waitForText("بانتظار التأكيد")
        h.settle(1_000); h.shot("a04_services")
        back()

        click("المنتجات")
        h.waitForText("الكتالوج فارغ")
        click("منتج جديد")
        h.waitForText("اسم المنتج *")
        type("اسم المنتج *", "كاميرا IP قبة 4MP")
        click("كاميرات")
        type("السعر (د.ج)", "12500")
        h.settle(600); h.shot("a05_product_editor")
        click("حفظ")
        h.waitForText("كاميرا IP قبة 4MP")
        h.settle(1_000); h.shot("a06_products")
        back()

        click("المحتوى")
        h.waitForText("معلومات الشركة")
        click("معلومات الشركة")
        h.waitForText("ساعات العمل")
        h.settle(1_000); h.shot("a07_business_info")
        back()

        click("الإعدادات")
        h.waitForText("سجل العمليات")
        click("سجل العمليات")
        h.waitForText("PRODUCT_CREATED", substring = true)
        h.settle(800); h.shot("a08_audit_log")
    }
}
