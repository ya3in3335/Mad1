package com.yourtech.systeme.admin.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourtech.systeme.admin.auth.AdminPermission
import com.yourtech.systeme.admin.auth.AdminUser
import com.yourtech.systeme.admin.feature.AccountsScreen
import com.yourtech.systeme.admin.feature.AppointmentsScreen
import com.yourtech.systeme.admin.feature.AuditLogScreen
import com.yourtech.systeme.admin.feature.BusinessEditorScreen
import com.yourtech.systeme.admin.feature.CatalogHubScreen
import com.yourtech.systeme.admin.feature.CategoriesScreen
import com.yourtech.systeme.admin.feature.CategoryEditorScreen
import com.yourtech.systeme.admin.feature.ChangePasswordScreen
import com.yourtech.systeme.admin.feature.ContentHubScreen
import com.yourtech.systeme.admin.feature.DashboardScreen
import com.yourtech.systeme.admin.feature.ForcedPasswordChangeScreen
import com.yourtech.systeme.admin.feature.InventoryScreen
import com.yourtech.systeme.admin.feature.LoginScreen
import com.yourtech.systeme.admin.feature.NewAccountScreen
import com.yourtech.systeme.admin.feature.ProductEditorScreen
import com.yourtech.systeme.admin.feature.ProductsAdminScreen
import com.yourtech.systeme.admin.feature.ProjectEditorScreen
import com.yourtech.systeme.admin.feature.ProjectsAdminScreen
import com.yourtech.systeme.admin.feature.PromotionEditorScreen
import com.yourtech.systeme.admin.feature.PromotionsAdminScreen
import com.yourtech.systeme.admin.feature.RequestAdminScreen
import com.yourtech.systeme.admin.feature.RequestsAdminScreen
import com.yourtech.systeme.admin.feature.ServiceEditorScreen
import com.yourtech.systeme.admin.feature.ServicesScreen
import com.yourtech.systeme.admin.feature.SettingsHubScreen
import com.yourtech.systeme.admin.feature.SetupOwnerScreen
import com.yourtech.systeme.admin.feature.Shortcut
import com.yourtech.systeme.admin.feature.TestimonialEditorScreen
import com.yourtech.systeme.admin.feature.TestimonialsAdminScreen
import com.yourtech.systeme.designsystem.splash.YourTechSplash
import com.yourtech.systeme.designsystem.theme.YT

private data class Tab(val route: String, val label: String, val icon: ImageVector, val visible: (AdminUser) -> Boolean)

private val tabs = listOf(
    Tab("dashboard", "الرئيسية", Icons.Rounded.Dashboard) { it.can(AdminPermission.DASHBOARD) },
    Tab("requests", "الطلبات", Icons.Rounded.Assignment) { it.can(AdminPermission.REQUESTS) },
    Tab("catalog", "الكتالوج", Icons.Rounded.Widgets) { u -> listOf(AdminPermission.PRODUCTS, AdminPermission.INVENTORY, AdminPermission.CATEGORIES, AdminPermission.SERVICES).any(u::can) },
    Tab("content", "المحتوى", Icons.Rounded.PhotoLibrary) { u -> listOf(AdminPermission.PORTFOLIO, AdminPermission.PROMOTIONS, AdminPermission.TESTIMONIALS, AdminPermission.BUSINESS_INFO).any(u::can) },
    Tab("settings", "الإعدادات", Icons.Rounded.Settings) { true },
)

private val shortcuts = listOf(
    Shortcut("المواعيد", Icons.Rounded.CalendarMonth, "appointments", AdminPermission.APPOINTMENTS),
    Shortcut("منتج جديد", Icons.Rounded.Inventory2, "product/edit", AdminPermission.PRODUCTS),
    Shortcut("الخدمات", Icons.Rounded.Security, "services", AdminPermission.SERVICES),
    Shortcut("مشروع جديد", Icons.Rounded.PhotoLibrary, "project/edit", AdminPermission.PORTFOLIO),
    Shortcut("معلومات الشركة", Icons.Rounded.Business, "business", AdminPermission.BUSINESS_INFO),
)

@Composable
fun AdminApp(showSplash: Boolean = true, vm: AdminViewModel = hiltViewModel()) {
    var splash by rememberSaveable { mutableStateOf(showSplash) }
    val snackbar = remember { SnackbarHostState() }
    val user by vm.session.collectAsStateWithLifecycle()
    val count by vm.accountCount.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    Box(Modifier.fillMaxSize().background(YT.Navy)) {
        Scaffold(containerColor = YT.Navy, snackbarHost = { SnackbarHost(snackbar) }) { pad ->
            Box(Modifier.padding(bottom = pad.calculateBottomPadding())) {
                val u = user
                when {
                    count == null -> Unit
                    count == 0 -> SetupOwnerScreen(vm)
                    u == null -> LoginScreen(vm)
                    u.mustChangePassword -> ForcedPasswordChangeScreen(vm)
                    // Keyed by account so nothing from a previous session's back stack survives a re-login.
                    else -> androidx.compose.runtime.key(u.id) { AdminMain(vm, u) }
                }
            }
        }
        if (splash) YourTechSplash(onFinished = { splash = false }, badge = "لوحة التحكم")
    }
}

@Composable
private fun AdminMain(vm: AdminViewModel, user: AdminUser) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    val visibleTabs = tabs.filter { it.visible(user) }
    Scaffold(
        containerColor = YT.Navy,
        bottomBar = {
            if (visibleTabs.any { it.route == current }) {
                NavigationBar(containerColor = YT.Surface) {
                    visibleTabs.forEach { t ->
                        NavigationBarItem(
                            selected = current == t.route, onClick = { nav.switchTab(t.route) },
                            icon = { Icon(t.icon, null) }, label = { Text(t.label, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = YT.White, selectedTextColor = YT.White, indicatorColor = YT.Blue.copy(alpha = 0.85f),
                                unselectedIconColor = YT.TextMuted, unselectedTextColor = YT.TextMuted,
                            ),
                        )
                    }
                }
            }
        },
    ) { pad ->
        NavHost(
            nav, startDestination = visibleTabs.first().route, modifier = Modifier.padding(bottom = pad.calculateBottomPadding()),
            enterTransition = { fadeIn() }, exitTransition = { fadeOut() },
        ) {
            val back: () -> Unit = { nav.popBackStack() }
            val open: (String) -> Unit = { nav.navigate(it) }
            val optionalId = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })

            composable("dashboard") { DashboardScreen(vm, shortcuts, open) { nav.navigate("request/$it") } }
            composable("requests") { Guard(user, AdminPermission.REQUESTS) { RequestsAdminScreen(vm, { nav.navigate("request/$it") }, { open("appointments") }) } }
            composable("request/{id}", listOf(navArgument("id") { type = NavType.LongType })) {
                Guard(user, AdminPermission.REQUESTS) { RequestAdminScreen(vm, it.arguments!!.getLong("id"), back) }
            }
            composable("appointments") { Guard(user, AdminPermission.APPOINTMENTS) { AppointmentsScreen(vm, back) { nav.navigate("request/$it") } } }

            composable("catalog") { CatalogHubScreen(vm, open) }
            composable("products") { Guard(user, AdminPermission.PRODUCTS) { ProductsAdminScreen(vm, back) { id -> open(if (id == null) "product/edit" else "product/edit?id=$id") } } }
            composable("product/edit?id={id}", optionalId) {
                Guard(user, AdminPermission.PRODUCTS) { ProductEditorScreen(vm, it.arguments?.getString("id"), back) }
            }
            composable("inventory") { Guard(user, AdminPermission.INVENTORY) { InventoryScreen(vm, back) } }
            composable("categories") { Guard(user, AdminPermission.CATEGORIES) { CategoriesScreen(vm, back) { id -> open(if (id == null) "category/edit" else "category/edit?id=$id") } } }
            composable("category/edit?id={id}", optionalId) {
                Guard(user, AdminPermission.CATEGORIES) { CategoryEditorScreen(vm, it.arguments?.getString("id"), back) }
            }
            composable("services") { Guard(user, AdminPermission.SERVICES) { ServicesScreen(vm, back) { id -> open(if (id == null) "service/edit" else "service/edit?id=$id") } } }
            composable("service/edit?id={id}", optionalId) {
                Guard(user, AdminPermission.SERVICES) { ServiceEditorScreen(vm, it.arguments?.getString("id"), back) }
            }

            composable("content") { ContentHubScreen(vm, open) }
            composable("projects") { Guard(user, AdminPermission.PORTFOLIO) { ProjectsAdminScreen(vm, back) { id -> open(if (id == null) "project/edit" else "project/edit?id=$id") } } }
            composable("project/edit?id={id}", optionalId) {
                Guard(user, AdminPermission.PORTFOLIO) { ProjectEditorScreen(vm, it.arguments?.getString("id")?.toLongOrNull(), back) }
            }
            composable("promotions") { Guard(user, AdminPermission.PROMOTIONS) { PromotionsAdminScreen(vm, back) { id -> open(if (id == null) "promotion/edit" else "promotion/edit?id=$id") } } }
            composable("promotion/edit?id={id}", optionalId) {
                Guard(user, AdminPermission.PROMOTIONS) { PromotionEditorScreen(vm, it.arguments?.getString("id")?.toLongOrNull(), back) }
            }
            composable("testimonials") { Guard(user, AdminPermission.TESTIMONIALS) { TestimonialsAdminScreen(vm, back) { id -> open(if (id == null) "testimonial/edit" else "testimonial/edit?id=$id") } } }
            composable("testimonial/edit?id={id}", optionalId) {
                Guard(user, AdminPermission.TESTIMONIALS) { TestimonialEditorScreen(vm, it.arguments?.getString("id")?.toLongOrNull(), back) }
            }
            composable("business") { Guard(user, AdminPermission.BUSINESS_INFO) { BusinessEditorScreen(vm, back) } }

            composable("settings") { SettingsHubScreen(vm, open) }
            composable("password") { ChangePasswordScreen(vm, back) }
            composable("accounts") { Guard(user, AdminPermission.ACCOUNTS) { AccountsScreen(vm, back) { open("account/new") } } }
            composable("account/new") { Guard(user, AdminPermission.ACCOUNTS) { NewAccountScreen(vm, back) } }
            composable("audit") { Guard(user, AdminPermission.AUDIT_LOG) { AuditLogScreen(vm, back) } }
        }
    }
}

/** Route-level authorization check (the repository checks again before any write). */
@Composable
private fun Guard(user: AdminUser, permission: AdminPermission, content: @Composable () -> Unit) {
    if (user.can(permission)) content()
    else com.yourtech.systeme.designsystem.component.EmptyState(Icons.Rounded.Security, "غير مصرّح", "ليست لديك صلاحية للوصول إلى هذا القسم.")
}

private fun NavHostController.switchTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
