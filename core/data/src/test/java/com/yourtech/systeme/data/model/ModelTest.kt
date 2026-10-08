package com.yourtech.systeme.data.model

import com.yourtech.systeme.data.repository.RequestRepository
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestStatusTest {
    @Test fun forwardOnly() {
        assertTrue(RequestStatus.SUBMITTED.canTransitionTo(RequestStatus.UNDER_REVIEW))
        assertTrue(RequestStatus.UNDER_REVIEW.canTransitionTo(RequestStatus.SCHEDULED)) // maintenance skips the quote
        assertFalse(RequestStatus.SCHEDULED.canTransitionTo(RequestStatus.UNDER_REVIEW))
        assertFalse(RequestStatus.COMPLETED.canTransitionTo(RequestStatus.IN_PROGRESS))
    }

    @Test fun cancelOnlyBeforeWorkStarts() {
        assertTrue(RequestStatus.SCHEDULED.canTransitionTo(RequestStatus.CANCELLED))
        assertFalse(RequestStatus.IN_PROGRESS.canTransitionTo(RequestStatus.CANCELLED))
        assertFalse(RequestStatus.CANCELLED.canTransitionTo(RequestStatus.CANCELLED))
    }

    @Test fun nextFollowsTimeline() {
        var s: RequestStatus? = RequestStatus.SUBMITTED
        val seen = mutableListOf<RequestStatus>()
        while (s != null) { seen += s; s = s.next() }
        assertEquals(RequestStatus.timeline, seen)
    }

    @Test fun referenceFormat() {
        val ref = RequestRepository.reference(RequestType.MAINTENANCE, 1_760_000_000_123L)
        assertTrue(ref, Regex("^YT-SAV-\\d{6}-\\d{4}$").matches(ref))
    }
}

class ValidationTest {
    @Test fun algerianPhones() {
        assertTrue(Validation.isValidAlgerianPhone("0561 03 41 49"))
        assertTrue(Validation.isValidAlgerianPhone("+213 661-23-45-67"))
        assertTrue(Validation.isValidAlgerianPhone("00213771234567"))
        assertFalse(Validation.isValidAlgerianPhone("0412345678"))
        assertFalse(Validation.isValidAlgerianPhone("05610341"))
        assertEquals("213561034149", Validation.toInternational("0561034149"))
    }

    @Test fun httpsOnly() {
        assertTrue(Validation.isHttpsUrl(""))
        assertTrue(Validation.isHttpsUrl("https://facebook.com/yourtech"))
        assertFalse(Validation.isHttpsUrl("http://example.com"))
        assertFalse(Validation.isHttpsUrl("javascript:alert(1)"))
    }

    @Test fun cleanRemovesControlCharacters() {
        assertEquals("a\nb", Validation.clean("  a\u0000\n\u0007b  "))
    }

    private val wilaya = Wilayas.byCode(31)

    @Test fun installationRequiresAddressPropertyAndSystem() {
        val f = RequestForm(RequestType.INSTALLATION, "Yacine", "0561034149", wilaya, "Bir El Djir")
        assertEquals(setOf(RequestForm.Field.ADDRESS, RequestForm.Field.PROPERTY, RequestForm.Field.SYSTEM), f.validate())
        val ok = f.copy(address = "Cité 500 logements", propertyType = PropertyType.HOUSE, systemType = "cctv", deviceCount = "4")
        assertTrue(ok.validate().isEmpty())
    }

    @Test fun maintenanceRequiresProblem() {
        val f = RequestForm(RequestType.MAINTENANCE, "Yacine", "0561034149", wilaya, "Oran", systemType = "recorders", problemDescription = "short")
        assertEquals(setOf(RequestForm.Field.PROBLEM), f.validate())
    }

    @Test fun rejectsBadDeviceCountAndPastDate() {
        val now = 1_760_000_000_000L
        val f = RequestForm(RequestType.QUOTE, "Yacine", "0561034149", wilaya, "Oran", deviceCount = "0", preferredDate = now - 3 * 86_400_000L)
        assertEquals(setOf(RequestForm.Field.DEVICES, RequestForm.Field.DATE), f.validate(now))
    }
}

class BusinessTest {
    private val hours = BusinessDefaults.info.openingHours

    private fun at(day: Int, h: Int, m: Int): Long = Calendar.getInstance(BusinessHours.ALGIERS).apply {
        clear(); set(2026, Calendar.OCTOBER, 1, h, m)
        while (get(Calendar.DAY_OF_WEEK) != day) add(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis

    @Test fun fridayClosedSaturdayToThursdayOpen() {
        val parsed = BusinessHours.parse(hours)
        assertEquals(6, parsed.size)
        assertNull(parsed[Calendar.FRIDAY])
        assertEquals(8 * 60 to 17 * 60 + 30, parsed[Calendar.SATURDAY])
        assertEquals(hours, BusinessHours.format(parsed))
    }

    @Test fun openStatus() {
        assertTrue(BusinessHours.status(hours, at(Calendar.SUNDAY, 10, 0)).isOpen)
        assertFalse(BusinessHours.status(hours, at(Calendar.SUNDAY, 17, 30)).isOpen)
        val friday = BusinessHours.status(hours, at(Calendar.FRIDAY, 11, 0))
        assertFalse(friday.isOpen)
        assertEquals(Calendar.SATURDAY, friday.nextOpenDay)
        val early = BusinessHours.status(hours, at(Calendar.MONDAY, 7, 0))
        assertEquals(Calendar.MONDAY, early.nextOpenDay)
    }

    @Test fun phoneDisplayAndLinks() {
        assertEquals("+213 561 03 41 49", displayPhone("213561034149"))
        assertEquals("https://wa.me/213561034149?text=Salam%20YT", BusinessDefaults.info.whatsappUrl("Salam YT"))
        assertTrue(BusinessDefaults.info.directionsUrl().contains("35.7190252,-0.5664739"))
    }

    @Test fun moneyUsesNarrowSpaceGroups() {
        assertEquals("12 500 DA", Money.format(12_500, AppLanguage.FRENCH))
        assertEquals("12 500 د.ج", Money.format(12_500, AppLanguage.ARABIC))
    }

    @Test fun visualsMatchKeywords() {
        assertEquals("fingerprint", SecurityVisuals.keyFor("Pointeuse biométrique empreinte"))
        assertEquals("dome", SecurityVisuals.keyFor("Caméra dôme 4MP"))
        assertEquals("camera", SecurityVisuals.keyFor("Caméra IP 4MP"))
        assertEquals("tools", SecurityVisuals.keyFor("Équipement installation"))
        assertNull(SecurityVisuals.keyFor("xyz"))
    }
}
