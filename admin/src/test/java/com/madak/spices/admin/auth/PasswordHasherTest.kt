package com.madak.spices.admin.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun `correct password verifies and wrong one does not`() {
        val hash = PasswordHasher.hash("Madak2026!".toCharArray(), iterations = 2_000)
        assertTrue(PasswordHasher.verify("Madak2026!".toCharArray(), hash))
        assertFalse(PasswordHasher.verify("madak2026!".toCharArray(), hash))
    }

    @Test
    fun `salts make identical passwords hash differently`() {
        val a = PasswordHasher.hash("SamePass123".toCharArray(), iterations = 2_000)
        val b = PasswordHasher.hash("SamePass123".toCharArray(), iterations = 2_000)
        assertNotEquals(a.saltHex, b.saltHex)
        assertNotEquals(a.hashHex, b.hashHex)
        assertFalse(a.hashHex.contains("SamePass123"))
    }

    @Test
    fun `password policy`() {
        assertTrue(PasswordHasher.isStrongEnough("spices2026"))
        assertFalse(PasswordHasher.isStrongEnough("short1"))
        assertFalse(PasswordHasher.isStrongEnough("onlyletters"))
        assertFalse(PasswordHasher.isStrongEnough("1234567890"))
    }

    @Test
    fun `lockout grows after five failures and is capped`() {
        assertEquals(0, AdminAuthRepository.lockoutSeconds(4))
        assertEquals(30, AdminAuthRepository.lockoutSeconds(5))
        assertEquals(60, AdminAuthRepository.lockoutSeconds(6))
        assertEquals(900, AdminAuthRepository.lockoutSeconds(50))
    }
}
