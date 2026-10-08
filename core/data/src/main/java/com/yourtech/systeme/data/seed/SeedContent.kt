package com.yourtech.systeme.data.seed

import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity

/**
 * Proposed starting content. Service categories reflect the company's stated specialization and
 * are fully editable (and can be hidden) in the Admin app. Two categories start hidden until the
 * company confirms it offers them. No products, prices, brands, warranties, projects or
 * testimonials are invented.
 */
object SeedContent {
    val services = listOf(
        ServiceCategoryEntity(
            id = "cctv", sortOrder = 0, iconKey = "camera", photoKey = "camera", isFeatured = true,
            nameAr = "كاميرات المراقبة", nameFr = "Vidéosurveillance", nameEn = "CCTV surveillance",
            summaryAr = "أنظمة كاميرات مراقبة تناظرية وIP مع تسجيل DVR/NVR ومشاهدة عن بُعد من الهاتف.",
            summaryFr = "Systèmes de caméras analogiques et IP avec enregistrement DVR/NVR et visualisation à distance sur smartphone.",
            summaryEn = "Analog and IP camera systems with DVR/NVR recording and remote viewing from your phone.",
            benefitsAr = "كاميرات تسجّل على مدار الساعة\nمشاهدة مباشرة عن بُعد\nتسجيل وأرشفة الأحداث\nردع السرقة والتخريب",
            benefitsFr = "Caméras qui enregistrent 24h/24\nVisualisation à distance en direct\nEnregistrement et archivage des événements\nDissuasion du vol et du vandalisme",
            benefitsEn = "Cameras recording 24/7\nLive remote viewing\nEvent recording and archiving\nDeters theft and vandalism",
            useCasesAr = "المنازل والفيلات\nالمحلات التجارية\nالمكاتب والشركات\nالمستودعات والمصانع",
            useCasesFr = "Maisons et villas\nCommerces\nBureaux et entreprises\nEntrepôts et usines",
            useCasesEn = "Homes and villas\nShops\nOffices and businesses\nWarehouses and factories",
            specsAr = "الأنواع: كاميرات قبة، أسطوانية، PTZ\nالتقنية: تناظرية HD أو شبكية IP/PoE\nالتسجيل: DVR / NVR مع أقراص صلبة\nالرؤية الليلية: أشعة تحت الحمراء حسب الطراز",
            specsFr = "Types : dôme, bullet, PTZ\nTechnologie : analogique HD ou réseau IP/PoE\nEnregistrement : DVR / NVR avec disques durs\nVision nocturne : infrarouge selon le modèle",
            specsEn = "Types: dome, bullet, PTZ\nTechnology: HD analog or IP/PoE network\nRecording: DVR / NVR with hard drives\nNight vision: infrared depending on model",
        ),
        ServiceCategoryEntity(
            id = "alarm", sortOrder = 1, iconKey = "alarm", photoKey = "alarm", isFeatured = true,
            nameAr = "أنظمة الإنذار", nameFr = "Systèmes d'alarme", nameEn = "Alarm systems",
            summaryAr = "أنظمة إنذار ضد التسلل مع حساسات حركة وفتح أبواب وصفارات وتنبيهات على الهاتف.",
            summaryFr = "Alarmes anti-intrusion avec détecteurs de mouvement et d'ouverture, sirènes et alertes sur téléphone.",
            summaryEn = "Intrusion alarms with motion and door sensors, sirens and phone alerts.",
            benefitsAr = "تنبيه فوري عند التسلل\nتشغيل وإيقاف من الهاتف\nحماية المداخل والنوافذ",
            benefitsFr = "Alerte immédiate en cas d'intrusion\nArmement/désarmement depuis le téléphone\nProtection des accès et fenêtres",
            benefitsEn = "Instant intrusion alerts\nArm/disarm from your phone\nProtects doors and windows",
            useCasesAr = "المنازل\nالمحلات\nالمكاتب",
            useCasesFr = "Maisons\nCommerces\nBureaux",
            useCasesEn = "Homes\nShops\nOffices",
            specsAr = "المكونات: لوحة تحكم، لوحة مفاتيح، حساسات\nالاتصال: سلكي أو لاسلكي\nالتنبيه: صفارة، مكالمة، إشعار",
            specsFr = "Composants : centrale, clavier, détecteurs\nLiaison : filaire ou sans fil\nAlerte : sirène, appel, notification",
            specsEn = "Components: control panel, keypad, sensors\nConnection: wired or wireless\nAlerts: siren, call, notification",
        ),
        ServiceCategoryEntity(
            id = "access_control", sortOrder = 2, iconKey = "fingerprint", photoKey = "fingerprint", isFeatured = true,
            nameAr = "التحكم في الدخول", nameFr = "Contrôle d'accès", nameEn = "Access control",
            summaryAr = "التحكم في الدخول بالبطاقات RFID/NFC أو البصمة أو الرمز مع سجل للدخول والخروج.",
            summaryFr = "Contrôle d'accès par badge RFID/NFC, empreinte ou code, avec historique des entrées et sorties.",
            summaryEn = "Access control with RFID/NFC cards, fingerprint or PIN code, with entry/exit history.",
            benefitsAr = "دخول الأشخاص المصرح لهم فقط\nسجل دقيق للحضور\nإدارة الصلاحيات بسهولة",
            benefitsFr = "Accès réservé aux personnes autorisées\nHistorique précis des présences\nGestion simple des droits",
            benefitsEn = "Only authorized people get in\nAccurate attendance history\nEasy permission management",
            useCasesAr = "الشركات والمكاتب\nالعمارات السكنية\nالمستودعات",
            useCasesFr = "Entreprises et bureaux\nImmeubles résidentiels\nEntrepôts",
            useCasesEn = "Companies and offices\nResidential buildings\nWarehouses",
            specsAr = "التعريف: بطاقة RFID/NFC، بصمة، رمز\nالأقفال: كهرومغناطيسية أو إلكترونية\nالإدارة: برنامج أو تطبيق حسب النظام",
            specsFr = "Identification : badge RFID/NFC, empreinte, code\nVerrouillage : ventouse électromagnétique ou gâche\nGestion : logiciel ou application selon le système",
            specsEn = "Identification: RFID/NFC card, fingerprint, PIN\nLocks: electromagnetic or electric strike\nManagement: software or app depending on system",
        ),
        ServiceCategoryEntity(
            id = "smart_security", sortOrder = 3, iconKey = "home", photoKey = "home", isFeatured = true,
            nameAr = "الأمن الذكي", nameFr = "Sécurité intelligente", nameEn = "Smart security",
            summaryAr = "حلول المنزل الذكي: أقفال ذكية، حساسات، وكاميرات متصلة تُدار من تطبيق واحد.",
            summaryFr = "Maison connectée : serrures intelligentes, capteurs et caméras connectées pilotés depuis une seule application.",
            summaryEn = "Smart home: smart locks, sensors and connected cameras managed from one app.",
            benefitsAr = "تحكم كامل من الهاتف\nسيناريوهات وتنبيهات تلقائية\nأجهزة لاسلكية سهلة الربط",
            benefitsFr = "Contrôle total depuis le téléphone\nScénarios et alertes automatiques\nAppareils sans fil faciles à associer",
            benefitsEn = "Full control from your phone\nAutomatic scenes and alerts\nWireless devices that pair easily",
            useCasesAr = "المنازل والشقق\nالفيلات",
            useCasesFr = "Maisons et appartements\nVillas",
            useCasesEn = "Houses and apartments\nVillas",
        ),
        ServiceCategoryEntity(
            id = "intercom", sortOrder = 4, iconKey = "doorbell", photoKey = "intercom", isFeatured = true,
            nameAr = "الإنتركم والفيديو فون", nameFr = "Interphones et visiophones", nameEn = "Video intercom",
            summaryAr = "أنظمة إنتركم وفيديو فون للمنازل والعمارات مع فتح الباب عن بُعد.",
            summaryFr = "Interphones et visiophones pour maisons et immeubles avec ouverture de porte à distance.",
            summaryEn = "Intercom and video door entry for homes and buildings with remote door opening.",
            benefitsAr = "رؤية الزائر قبل فتح الباب\nفتح الباب عن بُعد\nتواصل صوتي واضح",
            benefitsFr = "Voir le visiteur avant d'ouvrir\nOuverture à distance\nCommunication audio claire",
            benefitsEn = "See visitors before opening\nRemote door release\nClear two-way audio",
            useCasesAr = "المنازل\nالعمارات\nالمكاتب",
            useCasesFr = "Maisons\nImmeubles\nBureaux",
            useCasesEn = "Houses\nApartment buildings\nOffices",
        ),
        // Hidden until the company confirms it offers these services.
        ServiceCategoryEntity(
            id = "gates", sortOrder = 5, iconKey = "gate", photoKey = "barrier", isActive = false,
            nameAr = "البوابات والحواجز الآلية", nameFr = "Portails et barrières automatiques", nameEn = "Automatic gates & barriers",
            summaryAr = "محركات البوابات والحواجز الآلية للمداخل ومواقف السيارات.",
            summaryFr = "Motorisation de portails et barrières pour accès et parkings.",
            summaryEn = "Gate motors and automatic barriers for entrances and car parks.",
        ),
    )

    val productCategories = listOf(
        ProductCategoryEntity("cameras", "كاميرات", "Caméras", "Cameras", "camera", 0),
        ProductCategoryEntity("recorders", "أجهزة تسجيل DVR/NVR", "Enregistreurs DVR/NVR", "DVR/NVR recorders", "recorder", 1),
        ProductCategoryEntity("alarms", "إنذار", "Alarme", "Alarm", "alarm", 2),
        ProductCategoryEntity("access", "التحكم في الدخول", "Contrôle d'accès", "Access control", "fingerprint", 3),
        ProductCategoryEntity("intercom", "إنتركم", "Interphonie", "Intercom", "doorbell", 4),
        ProductCategoryEntity("smart_home", "منزل ذكي", "Maison connectée", "Smart home", "home", 5),
        ProductCategoryEntity("accessories", "ملحقات", "Accessoires", "Accessories", "cable", 6),
    )
}
