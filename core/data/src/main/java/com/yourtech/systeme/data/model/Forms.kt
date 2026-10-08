package com.yourtech.systeme.data.model

object Validation {
    private val phoneRegex = Regex("^(?:\\+213|00213|0)([567]\\d{8})$")

    fun normalizePhone(raw: String) = raw.filter { it.isDigit() || it == '+' }
    fun isValidAlgerianPhone(raw: String) = phoneRegex.matches(normalizePhone(raw))

    /** "0561034149" / "+213561034149" → "213561034149" */
    fun toInternational(raw: String): String? = phoneRegex.find(normalizePhone(raw))?.groupValues?.get(1)?.let { "213$it" }

    fun isHttpsUrl(raw: String) = raw.isBlank() || Regex("^https://[\\w.-]+(:\\d+)?(/\\S*)?$").matches(raw.trim())

    /** Removes control characters and trims user text; keeps newlines. */
    fun clean(text: String, max: Int = 2000): String =
        text.filter { it == '\n' || !it.isISOControl() }.trim().take(max)
}

/** Installation / maintenance / consultation / quote form. */
data class RequestForm(
    val type: RequestType,
    val customerName: String = "",
    val phone: String = "",
    val wilaya: Wilaya? = null,
    val commune: String = "",
    val address: String = "",
    val propertyType: PropertyType? = null,
    val systemType: String? = null,
    val deviceCount: String = "",
    val problemDescription: String = "",
    val productId: String? = null,
    val preferredDate: Long? = null,
    val notes: String = "",
    val photoPaths: List<String> = emptyList(),
) {
    enum class Field { NAME, PHONE, WILAYA, COMMUNE, ADDRESS, PROPERTY, SYSTEM, DEVICES, PROBLEM, DATE }

    fun validate(now: Long = System.currentTimeMillis()): Set<Field> = buildSet {
        if (customerName.trim().length < 3) add(Field.NAME)
        if (!Validation.isValidAlgerianPhone(phone)) add(Field.PHONE)
        if (wilaya == null) add(Field.WILAYA)
        if (commune.trim().length < 2) add(Field.COMMUNE)
        val count = deviceCount.trim()
        if (count.isNotEmpty() && (count.toIntOrNull() == null || count.toInt() !in 1..500)) add(Field.DEVICES)
        if (preferredDate != null && preferredDate < now - 24 * 3600_000L) add(Field.DATE)
        when (type) {
            RequestType.INSTALLATION -> {
                if (address.trim().length < 5) add(Field.ADDRESS)
                if (propertyType == null) add(Field.PROPERTY)
                if (systemType == null) add(Field.SYSTEM)
            }
            RequestType.MAINTENANCE -> {
                if (systemType == null) add(Field.SYSTEM)
                if (problemDescription.trim().length < 10) add(Field.PROBLEM)
            }
            RequestType.CONSULTATION, RequestType.QUOTE -> Unit
        }
    }

    companion object {
        const val MAX_PHOTOS = 6
    }
}
