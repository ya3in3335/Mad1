package com.yourtech.systeme.admin.auth

import androidx.room.withTransaction
import com.yourtech.systeme.data.local.YourTechDatabase
import com.yourtech.systeme.data.local.entity.AdminAccountEntity
import com.yourtech.systeme.data.local.entity.AuditLogEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

enum class AdminPermission {
    DASHBOARD, REQUESTS, APPOINTMENTS, PRODUCTS, CATEGORIES, SERVICES, INVENTORY,
    PORTFOLIO, PROMOTIONS, TESTIMONIALS, BUSINESS_INFO, ACCOUNTS, AUDIT_LOG,
}

/**
 * Role-based access control. Checked in the UI *and* again in [com.yourtech.systeme.admin.data.AdminRepository]
 * before every write, so a hidden button is never the only protection. Ready to map to server-side claims.
 */
enum class AdminRole(val labelAr: String, val permissions: Set<AdminPermission>) {
    OWNER("المالك", AdminPermission.entries.toSet()),
    MANAGER("مدير", AdminPermission.entries.toSet() - AdminPermission.ACCOUNTS),
    STAFF(
        "موظف مبيعات",
        setOf(AdminPermission.DASHBOARD, AdminPermission.REQUESTS, AdminPermission.APPOINTMENTS, AdminPermission.PRODUCTS, AdminPermission.INVENTORY),
    ),
    TECHNICIAN("تقني", setOf(AdminPermission.DASHBOARD, AdminPermission.REQUESTS, AdminPermission.APPOINTMENTS));

    companion object {
        fun of(name: String) = entries.firstOrNull { it.name == name } ?: TECHNICIAN
    }
}

data class AdminUser(val id: Long, val username: String, val displayName: String, val role: AdminRole, val mustChangePassword: Boolean = false) {
    fun can(permission: AdminPermission) = permission in role.permissions
}

sealed interface LoginResult {
    data class Success(val user: AdminUser) : LoginResult
    /** Same answer for unknown user, wrong password and disabled account (no user enumeration). */
    data object WrongCredentials : LoginResult
    data class LockedOut(val secondsLeft: Long) : LoginResult
}

class AuthException(message: String) : Exception(message)

/**
 * Local back-office accounts. No default or hardcoded credentials: the first launch forces the owner
 * to create an account; the owner then creates the other accounts with temporary passwords that must
 * be changed at first sign-in. Repeated failures lock the account with an exponential delay.
 */
@Singleton
class AdminAuthRepository @Inject constructor(private val db: YourTechDatabase) {
    private val dao get() = db.adminDao()
    private val _session = MutableStateFlow<AdminUser?>(null)
    val session: StateFlow<AdminUser?> = _session

    val accountCount: Flow<Int> = dao.accountCount()
    val accounts: Flow<List<AdminAccountEntity>> = dao.accounts()

    suspend fun createOwner(username: String, displayName: String, password: String): AdminUser {
        validateNew(username, displayName, password)
        val hash = hash(password)
        val user = db.withTransaction {
            if (dao.accountCountNow() > 0) throw AuthException("تم إعداد حساب المالك مسبقًا")
            val id = dao.insertAccount(account(username, displayName, AdminRole.OWNER, hash, mustChange = false))
            dao.log(AuditLogEntity(actor = username.trim(), role = AdminRole.OWNER.name, action = "OWNER_CREATED", entityType = "account", entityId = id.toString()))
            AdminUser(id, username.trim(), displayName.trim(), AdminRole.OWNER)
        }
        _session.value = user
        return user
    }

    suspend fun login(username: String, password: String, now: Long = System.currentTimeMillis()): LoginResult {
        val account = dao.byUsername(username.trim())
        if (account == null) {
            // Spend the same time as a real check so response timing does not reveal valid usernames.
            hash(password)
            return LoginResult.WrongCredentials
        }
        if (account.lockedUntil > now) return LoginResult.LockedOut((account.lockedUntil - now + 999) / 1000)
        val ok = withContext(Dispatchers.Default) {
            PasswordHasher.verify(password.toCharArray(), PasswordHasher.Hash(account.hashAlgorithm, account.iterations, account.saltHex, account.hashHex))
        }
        if (ok && account.isActive) {
            dao.updateAccount(account.copy(failedAttempts = 0, lockedUntil = 0))
            val user = AdminUser(account.id, account.username, account.displayName, AdminRole.of(account.role), account.mustChangePassword)
            dao.log(AuditLogEntity(actor = account.username, role = account.role, action = "LOGIN", entityType = "account", entityId = account.id.toString(), timestamp = now))
            _session.value = user
            return LoginResult.Success(user)
        }
        val failures = account.failedAttempts + 1
        val lock = lockoutSeconds(failures)
        dao.updateAccount(account.copy(failedAttempts = failures, lockedUntil = if (lock > 0) now + lock * 1000 else 0))
        dao.log(AuditLogEntity(actor = account.username, role = account.role, action = "LOGIN_FAILED", entityType = "account", entityId = account.id.toString(), details = "attempt $failures", timestamp = now))
        return if (lock > 0) LoginResult.LockedOut(lock) else LoginResult.WrongCredentials
    }

    suspend fun changePassword(current: String, new: String) {
        val user = _session.value ?: throw AuthException("انتهت الجلسة")
        val account = dao.byId(user.id) ?: throw AuthException("الحساب غير موجود")
        val ok = withContext(Dispatchers.Default) {
            PasswordHasher.verify(current.toCharArray(), PasswordHasher.Hash(account.hashAlgorithm, account.iterations, account.saltHex, account.hashHex))
        }
        if (!ok) throw AuthException("كلمة المرور الحالية غير صحيحة")
        if (!PasswordHasher.isStrongEnough(new)) throw AuthException(WEAK)
        if (new == current) throw AuthException("اختر كلمة مرور مختلفة")
        val h = hash(new)
        dao.updateAccount(account.copy(hashAlgorithm = h.algorithm, iterations = h.iterations, saltHex = h.saltHex, hashHex = h.hashHex, mustChangePassword = false))
        dao.log(AuditLogEntity(actor = user.username, role = user.role.name, action = "PASSWORD_CHANGED", entityType = "account", entityId = user.id.toString()))
        _session.value = user.copy(mustChangePassword = false)
    }

    /** Owner only: creates a staff account with a temporary password that must be changed at first login. */
    suspend fun createAccount(username: String, displayName: String, role: AdminRole, tempPassword: String) {
        val actor = requireAccounts()
        if (role == AdminRole.OWNER) throw AuthException("لا يمكن إنشاء مالك ثانٍ")
        validateNew(username, displayName, tempPassword)
        if (dao.byUsername(username.trim()) != null) throw AuthException("اسم المستخدم مستعمل")
        val id = dao.insertAccount(account(username, displayName, role, hash(tempPassword), mustChange = true))
        dao.log(AuditLogEntity(actor = actor.username, role = actor.role.name, action = "ACCOUNT_CREATED", entityType = "account", entityId = id.toString(), details = "${username.trim()} (${role.name})"))
    }

    suspend fun setActive(id: Long, active: Boolean) {
        val actor = requireAccounts()
        val account = dao.byId(id) ?: return
        if (account.id == actor.id || AdminRole.of(account.role) == AdminRole.OWNER) throw AuthException("لا يمكن تعطيل حساب المالك")
        dao.updateAccount(account.copy(isActive = active, failedAttempts = 0, lockedUntil = 0))
        dao.log(AuditLogEntity(actor = actor.username, role = actor.role.name, action = if (active) "ACCOUNT_ENABLED" else "ACCOUNT_DISABLED", entityType = "account", entityId = id.toString(), details = account.username))
    }

    suspend fun resetPassword(id: Long, tempPassword: String) {
        val actor = requireAccounts()
        val account = dao.byId(id) ?: return
        if (account.id == actor.id) throw AuthException("استعمل «تغيير كلمة المرور» لحسابك")
        if (!PasswordHasher.isStrongEnough(tempPassword)) throw AuthException(WEAK)
        val h = hash(tempPassword)
        dao.updateAccount(account.copy(hashAlgorithm = h.algorithm, iterations = h.iterations, saltHex = h.saltHex, hashHex = h.hashHex, mustChangePassword = true, failedAttempts = 0, lockedUntil = 0))
        dao.log(AuditLogEntity(actor = actor.username, role = actor.role.name, action = "PASSWORD_RESET", entityType = "account", entityId = id.toString(), details = account.username))
    }

    fun logout() {
        _session.value = null
    }

    private fun requireAccounts(): AdminUser {
        val user = _session.value ?: throw AuthException("انتهت الجلسة")
        if (!user.can(AdminPermission.ACCOUNTS)) throw AuthException("ليست لديك صلاحية")
        return user
    }

    private fun validateNew(username: String, displayName: String, password: String) {
        if (!USERNAME.matches(username.trim())) throw AuthException("اسم المستخدم: 3–32 حرفًا لاتينيًا أو أرقامًا أو . _ -")
        if (displayName.isBlank() || displayName.length > 60) throw AuthException("أدخل الاسم الظاهر")
        if (!PasswordHasher.isStrongEnough(password)) throw AuthException(WEAK)
    }

    private suspend fun hash(password: String) = withContext(Dispatchers.Default) { PasswordHasher.hash(password.toCharArray()) }

    private fun account(username: String, displayName: String, role: AdminRole, h: PasswordHasher.Hash, mustChange: Boolean) = AdminAccountEntity(
        username = username.trim(), displayName = displayName.trim(), role = role.name,
        hashAlgorithm = h.algorithm, iterations = h.iterations, saltHex = h.saltHex, hashHex = h.hashHex,
        mustChangePassword = mustChange,
    )

    companion object {
        private val USERNAME = Regex("^[A-Za-z0-9._-]{3,32}$")
        const val WEAK = "كلمة المرور: 8 أحرف على الأقل مع حرف ورقم"

        /** 5 free attempts, then 30 s, 60 s, 120 s … capped at 15 minutes. */
        fun lockoutSeconds(failures: Int): Long =
            if (failures < 5) 0 else (30L shl (failures - 5).coerceAtMost(5)).coerceAtMost(900)
    }
}
