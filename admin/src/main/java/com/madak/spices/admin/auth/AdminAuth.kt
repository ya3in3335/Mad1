package com.madak.spices.admin.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

enum class AdminPermission { PRODUCTS, CATEGORIES, ORDERS, CUSTOMERS, INVENTORY, OFFERS, NOTIFICATIONS, STATISTICS, SETTINGS }

/** Role-based access control, ready to be backed by server-side claims (Supabase RLS / Firebase custom claims). */
enum class AdminRole(val labelAr: String, val permissions: Set<AdminPermission>) {
    OWNER("المالك", AdminPermission.entries.toSet()),
    MANAGER("مدير", AdminPermission.entries.toSet() - AdminPermission.SETTINGS),
    STAFF("موظف", setOf(AdminPermission.ORDERS, AdminPermission.INVENTORY, AdminPermission.CUSTOMERS)),
}

data class AdminUser(val name: String, val role: AdminRole) {
    fun can(permission: AdminPermission) = permission in role.permissions
}

sealed interface LoginResult {
    data class Success(val user: AdminUser) : LoginResult
    data object WrongPassword : LoginResult
    data class LockedOut(val secondsLeft: Long) : LoginResult
}

private val Context.adminAuthStore: DataStore<Preferences> by preferencesDataStore(name = "madak_admin_auth")

/**
 * Local owner account for the back office. The first launch forces the owner to create a strong
 * passphrase; nothing is hardcoded. Repeated failures trigger an exponential lock-out.
 */
@Singleton
class AdminAuthRepository @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.adminAuthStore
    private val nameKey = stringPreferencesKey("name")
    private val roleKey = stringPreferencesKey("role")
    private val algoKey = stringPreferencesKey("algo")
    private val iterKey = intPreferencesKey("iterations")
    private val saltKey = stringPreferencesKey("salt")
    private val hashKey = stringPreferencesKey("hash")
    private val failuresKey = intPreferencesKey("failures")
    private val lockedUntilKey = longPreferencesKey("locked_until")

    private val _session = MutableStateFlow<AdminUser?>(null)
    val session: StateFlow<AdminUser?> = _session

    val isConfigured = store.data.map { it[hashKey] != null }

    suspend fun createOwner(name: String, password: String): AdminUser {
        require(name.isNotBlank())
        require(PasswordHasher.isStrongEnough(password))
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password.toCharArray()) }
        store.edit {
            check(it[hashKey] == null) { "Owner already configured" }
            it[nameKey] = name.trim()
            it[roleKey] = AdminRole.OWNER.name
            it[algoKey] = hash.algorithm
            it[iterKey] = hash.iterations
            it[saltKey] = hash.saltHex
            it[hashKey] = hash.hashHex
            it[failuresKey] = 0
        }
        return AdminUser(name.trim(), AdminRole.OWNER).also { _session.value = it }
    }

    suspend fun login(password: String, now: Long = System.currentTimeMillis()): LoginResult {
        val prefs = store.data.first()
        val lockedUntil = prefs[lockedUntilKey] ?: 0L
        if (lockedUntil > now) return LoginResult.LockedOut((lockedUntil - now + 999) / 1000)
        val stored = PasswordHasher.Hash(
            prefs[algoKey] ?: return LoginResult.WrongPassword,
            prefs[iterKey] ?: return LoginResult.WrongPassword,
            prefs[saltKey] ?: return LoginResult.WrongPassword,
            prefs[hashKey] ?: return LoginResult.WrongPassword,
        )
        val ok = withContext(Dispatchers.Default) { PasswordHasher.verify(password.toCharArray(), stored) }
        if (ok) {
            store.edit { it[failuresKey] = 0; it[lockedUntilKey] = 0L }
            val user = AdminUser(prefs[nameKey].orEmpty(), AdminRole.entries.firstOrNull { it.name == prefs[roleKey] } ?: AdminRole.STAFF)
            _session.value = user
            return LoginResult.Success(user)
        }
        val failures = (prefs[failuresKey] ?: 0) + 1
        val lockSeconds = lockoutSeconds(failures)
        store.edit {
            it[failuresKey] = failures
            if (lockSeconds > 0) it[lockedUntilKey] = now + lockSeconds * 1000
        }
        return if (lockSeconds > 0) LoginResult.LockedOut(lockSeconds) else LoginResult.WrongPassword
    }

    suspend fun changePassword(current: String, new: String): Boolean {
        if (!PasswordHasher.isStrongEnough(new)) return false
        val prefs = store.data.first()
        val stored = PasswordHasher.Hash(prefs[algoKey] ?: return false, prefs[iterKey] ?: return false, prefs[saltKey] ?: return false, prefs[hashKey] ?: return false)
        val ok = withContext(Dispatchers.Default) { PasswordHasher.verify(current.toCharArray(), stored) }
        if (!ok) return false
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(new.toCharArray()) }
        store.edit {
            it[algoKey] = hash.algorithm; it[iterKey] = hash.iterations; it[saltKey] = hash.saltHex; it[hashKey] = hash.hashHex
        }
        return true
    }

    fun logout() { _session.value = null }

    companion object {
        /** 5 free attempts, then 30 s, 60 s, 120 s … capped at 15 minutes. */
        fun lockoutSeconds(failures: Int): Long =
            if (failures < 5) 0 else (30L shl (failures - 5).coerceAtMost(5)).coerceAtMost(900)
    }
}
