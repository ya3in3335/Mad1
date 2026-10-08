package com.madak.spices.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.madak.spices.R
import com.madak.spices.data.model.AppLanguage
import com.madak.spices.data.repository.CartRepository
import com.madak.spices.designsystem.component.AnimatedCountBadge
import com.madak.spices.feature.cart.CartScreen
import com.madak.spices.feature.cart.CheckoutScreen
import com.madak.spices.feature.catalog.FavoritesScreen
import com.madak.spices.feature.catalog.ProductDetailScreen
import com.madak.spices.feature.catalog.ProductsScreen
import com.madak.spices.feature.catalog.SearchScreen
import com.madak.spices.feature.cook.CookTodayScreen
import com.madak.spices.feature.home.HomeScreen
import com.madak.spices.feature.orders.OrderConfirmationScreen
import com.madak.spices.feature.orders.OrderTrackingScreen
import com.madak.spices.feature.orders.OrdersScreen
import com.madak.spices.feature.profile.AboutScreen
import com.madak.spices.feature.profile.ContactScreen
import com.madak.spices.feature.profile.NotificationsScreen
import com.madak.spices.feature.profile.ProfileScreen
import com.madak.spices.feature.profile.StoreLocationScreen
import com.madak.spices.feature.splash.OnboardingScreen
import com.madak.spices.feature.splash.SplashScreen
import com.madak.spices.ui.LocalSnackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PRODUCTS = "products?category={category}"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    const val SEARCH = "search"
    const val PRODUCT = "product/{id}"
    const val FAVORITES = "favorites"
    const val COOK = "cook?dish={dish}"
    const val CHECKOUT = "checkout"
    const val CONFIRMATION = "confirmation/{orderId}"
    const val TRACKING = "tracking/{orderId}"
    const val NOTIFICATIONS = "notifications"
    const val ABOUT = "about"
    const val STORE = "store"
    const val CONTACT = "contact"

    fun products(category: String? = null) = "products?category=${category.orEmpty()}"
    fun product(id: String) = "product/$id"
    fun cook(dish: String) = "cook?dish=$dish"
    fun confirmation(orderId: Long) = "confirmation/$orderId"
    fun tracking(orderId: Long) = "tracking/$orderId"
}

private enum class Tab(val route: String, val navRoute: String, @StringRes val label: Int, val icon: ImageVector) {
    HOME(Routes.HOME, Routes.HOME, R.string.tab_home, Icons.Rounded.Home),
    PRODUCTS(Routes.PRODUCTS, Routes.products(), R.string.tab_products, Icons.Rounded.GridView),
    CART(Routes.CART, Routes.CART, R.string.tab_cart, Icons.Rounded.ShoppingBag),
    ORDERS(Routes.ORDERS, Routes.ORDERS, R.string.tab_orders, Icons.AutoMirrored.Rounded.ReceiptLong),
    PROFILE(Routes.PROFILE, Routes.PROFILE, R.string.tab_profile, Icons.Rounded.Person),
}

@HiltViewModel
class AppViewModel @Inject constructor(cart: CartRepository) : ViewModel() {
    val cartCount: StateFlow<Int> = cart.count.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

@Composable
fun MadakApp(onLanguageChange: (AppLanguage) -> Unit, viewModel: AppViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBar = Tab.entries.any { it.route == currentRoute }
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var askedNotifications by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(currentRoute == Routes.HOME) {
        if (currentRoute == Routes.HOME && !askedNotifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askedNotifications = true
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    CompositionLocalProvider(LocalSnackbar provides snackbar) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                AnimatedVisibility(showBar, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                        Tab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    nav.navigateTab(tab.navRoute)
                                },
                                icon = {
                                    if (tab == Tab.CART) AnimatedCountBadge(cartCount) { Icon(tab.icon, null) }
                                    else Icon(tab.icon, null)
                                },
                                label = { Text(stringResource(tab.label)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                MadakNavHost(nav, onLanguageChange)
            }
        }
    }
}

private fun NavHostController.navigateTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
private fun MadakNavHost(nav: NavHostController, onLanguageChange: (AppLanguage) -> Unit) {
    val back: () -> Unit = { nav.popBackStack() }
    val openProduct: (String) -> Unit = { nav.navigate(Routes.product(it)) }
    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(260)) + scaleIn(initialScale = 0.97f, animationSpec = tween(260)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(260)) },
        popExitTransition = { fadeOut(tween(200)) + scaleOut(targetScale = 0.97f, animationSpec = tween(200)) },
    ) {
        composable(Routes.SPLASH, enterTransition = { fadeIn() }, exitTransition = { fadeOut(tween(450)) }) {
            SplashScreen(onFinished = { onboarded ->
                nav.navigate(if (onboarded) Routes.HOME else Routes.ONBOARDING) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = { nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } } })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onSearch = { nav.navigate(Routes.SEARCH) },
                onProduct = openProduct,
                onCategory = { nav.navigateTab(Routes.products(it)) },
                onSeeAll = { nav.navigateTab(Routes.products()) },
                onDish = { nav.navigate(Routes.cook(it.name)) },
                onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
            )
        }
        composable(Routes.PRODUCTS, arguments = listOf(navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null })) {
            ProductsScreen(onProduct = openProduct, onSearch = { nav.navigate(Routes.SEARCH) })
        }
        composable(Routes.CART) {
            CartScreen(onCheckout = { nav.navigate(Routes.CHECKOUT) }, onBrowse = { nav.navigateTab(Routes.products()) }, onProduct = openProduct)
        }
        composable(Routes.ORDERS) {
            OrdersScreen(onOrder = { nav.navigate(Routes.tracking(it)) }, onBrowse = { nav.navigateTab(Routes.products()) })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onOrders = { nav.navigateTab(Routes.ORDERS) },
                onFavorites = { nav.navigate(Routes.FAVORITES) },
                onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
                onAbout = { nav.navigate(Routes.ABOUT) },
                onStore = { nav.navigate(Routes.STORE) },
                onContact = { nav.navigate(Routes.CONTACT) },
                onLanguageChange = onLanguageChange,
            )
        }
        composable(Routes.SEARCH) { SearchScreen(onBack = back, onProduct = openProduct) }
        composable(Routes.PRODUCT, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            ProductDetailScreen(onBack = back, onDish = { nav.navigate(Routes.cook(it.name)) })
        }
        composable(Routes.FAVORITES) { FavoritesScreen(onBack = back, onProduct = openProduct, onBrowse = { nav.navigateTab(Routes.products()) }) }
        composable(Routes.COOK, arguments = listOf(navArgument("dish") { type = NavType.StringType; nullable = true; defaultValue = null })) {
            CookTodayScreen(onBack = back, onProduct = openProduct)
        }
        composable(Routes.CHECKOUT) {
            CheckoutScreen(onBack = back, onPlaced = { id -> nav.navigate(Routes.confirmation(id)) { popUpTo(Routes.CART) } })
        }
        composable(Routes.CONFIRMATION, arguments = listOf(navArgument("orderId") { type = NavType.StringType })) {
            OrderConfirmationScreen(
                onTrack = { id -> nav.navigate(Routes.tracking(id)) { popUpTo(Routes.CART) } },
                onContinue = { nav.navigateTab(Routes.HOME) },
            )
        }
        composable(Routes.TRACKING, arguments = listOf(navArgument("orderId") { type = NavType.StringType })) { OrderTrackingScreen(onBack = back) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(onBack = back, onOrder = { nav.navigate(Routes.tracking(it)) }) }
        composable(Routes.ABOUT) { AboutScreen(onBack = back) }
        composable(Routes.STORE) { StoreLocationScreen(onBack = back) }
        composable(Routes.CONTACT) { ContactScreen(onBack = back) }
    }
}
