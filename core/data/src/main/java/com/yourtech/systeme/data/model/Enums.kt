package com.yourtech.systeme.data.model

/** Lifecycle of installation / maintenance / consultation requests. */
enum class RequestStatus {
    SUBMITTED,
    UNDER_REVIEW,
    QUOTATION_PREPARED,
    APPROVED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    val isTerminal get() = this == COMPLETED || this == CANCELLED
    val isOpen get() = !isTerminal

    fun next(): RequestStatus? = when (this) {
        SUBMITTED -> UNDER_REVIEW
        UNDER_REVIEW -> QUOTATION_PREPARED
        QUOTATION_PREPARED -> APPROVED
        APPROVED -> SCHEDULED
        SCHEDULED -> IN_PROGRESS
        IN_PROGRESS -> COMPLETED
        COMPLETED, CANCELLED -> null
    }

    /**
     * Forward moves only. Maintenance tickets may skip the quotation steps
     * (UNDER_REVIEW → SCHEDULED); a request can be cancelled until work starts.
     */
    fun canTransitionTo(target: RequestStatus): Boolean = when {
        target == CANCELLED -> this.ordinal <= SCHEDULED.ordinal
        isTerminal -> false
        else -> target.ordinal > ordinal && target != CANCELLED
    }

    companion object {
        val timeline = listOf(SUBMITTED, UNDER_REVIEW, QUOTATION_PREPARED, APPROVED, SCHEDULED, IN_PROGRESS, COMPLETED)
        val open = entries.filter { it.isOpen }
    }
}

enum class RequestType { INSTALLATION, MAINTENANCE, CONSULTATION, QUOTE }

enum class PropertyType { HOUSE, APARTMENT, SHOP, OFFICE, WAREHOUSE, FACTORY, OTHER }

enum class StockStatus { IN_STOCK, LOW_STOCK, OUT_OF_STOCK, ON_ORDER, UNKNOWN }

enum class AppLanguage(val tag: String, val localeTag: String) {
    ARABIC("ar", "ar-DZ-u-nu-latn"),
    FRENCH("fr", "fr-DZ"),
    ENGLISH("en", "en");

    val isRtl get() = this == ARABIC

    fun pick(ar: String, fr: String, en: String): String = when (this) {
        ARABIC -> ar.ifBlank { fr.ifBlank { en } }
        FRENCH -> fr.ifBlank { en.ifBlank { ar } }
        ENGLISH -> en.ifBlank { fr.ifBlank { ar } }
    }

    companion object {
        fun fromTag(tag: String?) = entries.firstOrNull { it.tag == tag } ?: ARABIC
    }
}
