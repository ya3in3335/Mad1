package com.yourtech.systeme.data.model

import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.local.entity.ProductEntity
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity

data class Product(val entity: ProductEntity, val isFavorite: Boolean = false) {
    val id get() = entity.id

    /** Image to display: uploaded file, then URL; [photoKey] selects the built-in illustration fallback. */
    val image: String? get() = entity.localImagePath ?: entity.imageUrl
    val photoKey: String? get() = entity.photoKey ?: SecurityVisuals.keyFor(entity.name, entity.model, entity.description, entity.categoryId)
    val hasPrice get() = entity.priceDzd != null && entity.priceDzd > 0

    /** Parsed "Key: value" lines. */
    val specs: List<Pair<String, String>>
        get() = entity.specs.lines().mapNotNull { line ->
            val i = line.indexOf(':')
            if (i <= 0) line.trim().takeIf { it.isNotEmpty() }?.let { it to "" }
            else line.substring(0, i).trim() to line.substring(i + 1).trim()
        }
}

fun ServiceCategoryEntity.name(l: AppLanguage) = l.pick(nameAr, nameFr, nameEn)
fun ServiceCategoryEntity.summary(l: AppLanguage) = l.pick(summaryAr, summaryFr, summaryEn)
fun ServiceCategoryEntity.benefits(l: AppLanguage) = l.pick(benefitsAr, benefitsFr, benefitsEn).lines().filter { it.isNotBlank() }
fun ServiceCategoryEntity.useCases(l: AppLanguage) = l.pick(useCasesAr, useCasesFr, useCasesEn).lines().filter { it.isNotBlank() }
fun ServiceCategoryEntity.specs(l: AppLanguage) = l.pick(specsAr, specsFr, specsEn).lines().filter { it.isNotBlank() }
fun ProductCategoryEntity.name(l: AppLanguage) = l.pick(nameAr, nameFr, nameEn)

object Money {
    fun format(dzd: Int, language: AppLanguage): String {
        // U+202F keeps digit groups in one bidi run inside RTL text.
        val g = "%,d".format(java.util.Locale.US, dzd).replace(',', ' ')
        return if (language == AppLanguage.ARABIC) "$g د.ج" else "$g DA"
    }
}

data class DashboardStats(
    val newToday: Int = 0,
    val open: Int = 0,
    val submitted: Int = 0,
    val scheduledToday: Int = 0,
    val completed: Int = 0,
)
