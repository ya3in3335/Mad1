package com.madak.spices.admin.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.madak.spices.admin.auth.AdminAuthRepository
import com.madak.spices.admin.auth.AdminPermission
import com.madak.spices.admin.auth.AdminUser
import com.madak.spices.admin.feature.AdminSettingsScreen
import com.madak.spices.admin.feature.AuthScreen
import com.madak.spices.admin.feature.CategoriesAdminScreen
import com.madak.spices.admin.feature.CustomersScreen
import com.madak.spices.admin.feature.DashboardScreen
import com.madak.spices.admin.feature.InventoryScreen
import com.madak.spices.admin.feature.NotificationsAdminScreen
import com.madak.spices.admin.feature.OffersAdminScreen
import com.madak.spices.admin.feature.OrdersAdminScreen
import com.madak.spices.admin.feature.ProductsAdminScreen
import com.madak.spices.designsystem.component.MadakWordmark
import com.madak.spices.designsystem.splash.MadakMotionSplash
import com.madak.spices.designsystem.theme.MadakColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AdminRootViewModel @Inject constructor(auth: AdminAuthRepository) : ViewModel() {
    val session = auth.session
}

private enum class Section(val route: String, val title: String, val icon: ImageVector, val permission: AdminPermission) {
    DASHBOARD("dashboard", "لوحة القيادة", Icons.Rounded.Dashboard, AdminPermission.STATISTICS),
    ORDERS("orders", "الطلبات", Icons.Rounded.ShoppingCart, AdminPermission.ORDERS),
    PRODUCTS("products", "المنتجات", Icons.Rounded.Storefront, AdminPermission.PRODUCTS),
    CATEGORIES("categories", "الأقسام", Icons.Rounded.Category, AdminPermission.CATEGORIES),
    INVENTORY("inventory", "المخزون", Icons.Rounded.Inventory2, AdminPermission.INVENTORY),
    CUSTOMERS("customers", "الزبائن", Icons.Rounded.People, AdminPermission.CUSTOMERS),
    OFFERS("offers", "العروض", Icons.Rounded.LocalOffer, AdminPermission.OFFERS),
    NOTIFICATIONS("notifications", "الإشعارات", Icons.Rounded.Campaign, AdminPermission.NOTIFICATIONS),
    SETTINGS("settings", "الإعدادات والأمان", Icons.Rounded.Settings, AdminPermission.SETTINGS),
}

@Composable
fun AdminApp(viewModel: AdminRootViewModel = hiltViewModel()) {
    var splashDone by rememberSaveable { mutableStateOf(false) }
    val session by viewModel.session.collectAsStateWithLifecycle()
    AnimatedContent(
        targetState = when { !splashDone -> 0; session == null -> 1; else -> 2 },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "adminRoot",
    ) { stage ->
        when (stage) {
            0 -> MadakMotionSplash(onFinished = { splashDone = true }, subtitle = "لوحة التحكم")
            1 -> AuthScreen()
            else -> session?.let { AdminShell(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminShell(user: AdminUser) {
    val nav = rememberNavController()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStack by nav.currentBackStackEntryAsState()
    val current = Section.entries.firstOrNull { it.route == backStack?.destination?.route } ?: Section.DASHBOARD
    val sections = Section.entries.filter { user.can(it.permission) }
    var openOrder by rememberSaveable { mutableStateOf<Long?>(null) }

    fun go(section: Section) {
        nav.navigate(section.route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        scope.launch { drawer.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MadakColors.Ink) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    MadakWordmark(Modifier.width(150.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("لوحة التحكم • ${user.name}", color = MadakColors.Gold, style = MaterialTheme.typography.labelLarge)
                }
                sections.forEach { s ->
                    NavigationDrawerItem(
                        label = { Text(s.title) },
                        icon = { Icon(s.icon, null) },
                        selected = s == current,
                        onClick = { go(s) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = MadakColors.Magenta,
                            unselectedTextColor = MadakColors.Beige,
                            unselectedIconColor = MadakColors.Beige,
                            selectedTextColor = Color.White,
                            selectedIconColor = Color.White,
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(current.title) },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawer.open() } }) { Icon(Icons.Rounded.Menu, "القائمة") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MadakColors.Ink, titleContentColor = Color.White, navigationIconContentColor = Color.White),
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
                NavHost(nav, startDestination = sections.first().route) {
                    composable(Section.DASHBOARD.route) {
                        DashboardScreen(user.name, onOrder = { openOrder = it; go(Section.ORDERS) }, onInventory = { go(Section.INVENTORY) })
                    }
                    composable(Section.ORDERS.route) { OrdersAdminScreen(openOrder, onOrderOpened = { openOrder = it }) }
                    composable(Section.PRODUCTS.route) { ProductsAdminScreen() }
                    composable(Section.CATEGORIES.route) { CategoriesAdminScreen() }
                    composable(Section.INVENTORY.route) { InventoryScreen() }
                    composable(Section.CUSTOMERS.route) { CustomersScreen() }
                    composable(Section.OFFERS.route) { OffersAdminScreen() }
                    composable(Section.NOTIFICATIONS.route) { NotificationsAdminScreen() }
                    composable(Section.SETTINGS.route) { AdminSettingsScreen(user) }
                }
            }
        }
    }
}
