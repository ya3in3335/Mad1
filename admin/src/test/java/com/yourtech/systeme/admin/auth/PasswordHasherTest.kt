package com.yourtech.systeme.admin.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test fun verifiesOnlyTheRightPassword() {
        val h = PasswordHasher.hash("Secure123".toCharArray(), iterations = 2_000)
        assertTrue(PasswordHasher.verify("Secure123".toCharArray(), h))
        assertFalse(PasswordHasher.verify("secure123".toCharArray(), h))
    }

    @Test fun saltsAreRandom() {
        val a = PasswordHasher.hash("Secure123".toCharArray(), iterations = 1_000)
        val b = PasswordHasher.hash("Secure123".toCharArray(), iterations = 1_000)
        assertNotEquals(a.saltHex, b.saltHex)
        assertNotEquals(a.hashHex, b.hashHex)
    }

    @Test fun passwordPolicy() {
        assertFalse(PasswordHasher.isStrongEnough("short1"))
        assertFalse(PasswordHasher.isStrongEnough("onlyletters"))
        assertTrue(PasswordHasher.isStrongEnough("Yourtech2026"))
    }

    @Test fun lockoutGrowsAndCaps() {
        assertEquals(0, AdminAuthRepository.lockoutSeconds(4))
        assertEquals(30, AdminAuthRepository.lockoutSeconds(5))
        assertEquals(60, AdminAuthRepository.lockoutSeconds(6))
        assertEquals(900, AdminAuthRepository.lockoutSeconds(20))
    }

    @Test fun rolePermissions() {
        assertTrue(AdminRole.OWNER.permissions.contains(AdminPermission.ACCOUNTS))
        assertFalse(AdminRole.MANAGER.permissions.contains(AdminPermission.ACCOUNTS))
        assertFalse(AdminRole.TECHNICIAN.permissions.contains(AdminPermission.PRODUCTS))
        assertFalse(AdminRole.STAFF.permissions.contains(AdminPermission.BUSINESS_INFO))
    }
}
