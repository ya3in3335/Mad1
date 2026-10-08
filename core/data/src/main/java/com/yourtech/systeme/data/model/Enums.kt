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

    /** The shop only sells: a quote request is reviewed, priced, then closed. */
    fun next(): RequestStatus? = when (this) {
        SUBMITTED -> UNDER_REVIEW
        UNDER_REVIEW -> QUOTATION_PREPARED
        QUOTATION_PREPARED -> COMPLETED
        else -> null
    }

    /** Forward moves along [timeline] only; a request can be cancelled until it is completed. */
    fun canTransitionTo(target: RequestStatus): Boolean = when {
        isTerminal -> false
        target == CANCELLED -> true
        else -> target in timeline && target.ordinal > ordinal
    }

    companion object {
        val timeline = listOf(SUBMITTED, UNDER_REVIEW, QUOTATION_PREPARED, COMPLETED)
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
