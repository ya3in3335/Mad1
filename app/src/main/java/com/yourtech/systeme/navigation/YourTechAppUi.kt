package com.yourtech.systeme.navigation

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourtech.systeme.R
import com.yourtech.systeme.data.model.AppLanguage
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.designsystem.splash.YourTechSplash
import com.yourtech.systeme.designsystem.theme.YT
import com.yourtech.systeme.feature.home.HomeScreen
import com.yourtech.systeme.feature.more.AboutScreen
import com.yourtech.systeme.feature.more.ContactScreen
import com.yourtech.systeme.feature.more.MoreScreen
import com.yourtech.systeme.feature.more.NotificationsScreen
import com.yourtech.systeme.feature.more.PrivacyScreen
import com.yourtech.systeme.feature.products.FavoritesScreen
import com.yourtech.systeme.feature.products.ProductDetailScreen
import com.yourtech.systeme.feature.products.ProductsScreen
import com.yourtech.systeme.feature.requests.RequestDetailScreen
import com.yourtech.systeme.feature.requests.RequestFormScreen
import com.yourtech.systeme.feature.requests.RequestSentScreen
import com.yourtech.systeme.feature.requests.RequestsScreen
import com.yourtech.systeme.feature.solutions.SolutionDetailScreen
import com.yourtech.systeme.feature.solutions.SolutionsScreen
import com.yourtech.systeme.ui.LocalSnackbar

object Routes {
    const val HOME = "home"
    const val SOLUTIONS = "solutions"
    const val SOLUTION = "solution/{id}"
    const val PRODUCTS = "products"
    const val PRODUCT = "product/{id}"
    const val FAVORITES = "favorites"
    const val REQUESTS = "requests"
    const val REQUEST_NEW = "request/new?type={type}&system={system}&product={product}"
    const val REQUEST = "request/{id}"
    const val REQUEST_SENT = "request/sent/{id}"
    const val CONTACT = "contact"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val NOTIFICATIONS = "notifications"
    const val MORE = "more"

    fun newRequest(type: RequestType, system: String? = null, product: String? = null): String =
        "request/new?type=${type.name}&system=${Uri.encode(system.orEmpty())}&product=${Uri.encode(product.orEmpty())}"
}

private data class Tab(val route: String, val label: Int, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, R.string.tab_home, Icons.Rounded.Home),
    Tab(Routes.SOLUTIONS, R.string.tab_solutions, Icons.Rounded.Videocam),
    Tab(Routes.PRODUCTS, R.string.tab_products, Icons.Rounded.GridView),
    Tab(Routes.REQUESTS, R.string.tab_requests, Icons.Rounded.Assignment),
    Tab(Routes.MORE, R.string.tab_more, Icons.Rounded.Menu),
)

@Composable
fun YourTechAppUi(onLanguageChange: (AppLanguage) -> Unit, showSplash: Boolean = true) {
    var splash by rememberSaveable { mutableStateOf(showSplash) }
    val snackbar = remember { SnackbarHostState() }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(splash) {
        if (!splash && Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    CompositionLocalProvider(LocalSnackbar provides snackbar) {
        Box(Modifier.fillMaxSize().background(YT.NavyDeep)) {
            Scaffold(
                containerColor = YT.NavyDeep,
                snackbarHost = { SnackbarHost(snackbar) },
                bottomBar = {
                    if (tabs.any { it.route == current }) {
                        NavigationBar(containerColor = YT.Surface, contentColor = YT.White) {
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = current == tab.route,
                                    onClick = { nav.switchTab(tab.route) },
                                    icon = { Icon(tab.icon, null) },
                                    label = { Text(stringResource(tab.label), maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = YT.White, selectedTextColor = YT.White, indicatorColor = YT.Blue.copy(alpha = 0.85f),
                                        unselectedIconColor = YT.TextMuted, unselectedTextColor = YT.TextMuted,
                                    ),
                                )
                            }
                        }
                    }
                },
            ) { padding ->
                NavHost(
                    nav, startDestination = Routes.HOME, modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                    enterTransition = { fadeIn() }, exitTransition = { fadeOut() },
                ) {
                    graph(nav, onLanguageChange)
                }
            }
            if (splash) YourTechSplash(onFinished = { splash = false })
        }
    }
}

private fun NavHostController.switchTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

private fun androidx.navigation.NavGraphBuilder.graph(nav: NavHostController, onLanguageChange: (AppLanguage) -> Unit) {
    val back: () -> Unit = { nav.popBackStack() }
    val idArg = listOf(navArgument("id") { type = NavType.StringType })

    composable(Routes.HOME) {
        HomeScreen(
            onQuote = { nav.navigate(Routes.newRequest(RequestType.QUOTE)) },
            onSolution = { nav.navigate("solution/$it") },
            onSolutions = { nav.switchTab(Routes.SOLUTIONS) },
            onProduct = { nav.navigate("product/$it") },
            onProducts = { nav.switchTab(Routes.PRODUCTS) },
            onContact = { nav.navigate(Routes.CONTACT) },
            onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
        )
    }
    composable(Routes.SOLUTIONS) { SolutionsScreen(onSolution = { nav.navigate("solution/$it") }) }
    composable(Routes.SOLUTION, idArg) {
        SolutionDetailScreen(
            onBack = back,
            onQuote = { nav.navigate(Routes.newRequest(RequestType.QUOTE, system = it)) },
            onProducts = { nav.switchTab(Routes.PRODUCTS) },
        )
    }
    composable(Routes.PRODUCTS) {
        ProductsScreen(
            onProduct = { nav.navigate("product/$it") },
            onQuote = { nav.navigate(Routes.newRequest(RequestType.QUOTE)) },
            onFavorites = { nav.navigate(Routes.FAVORITES) },
        )
    }
    composable(Routes.PRODUCT, idArg) {
        ProductDetailScreen(onBack = back, onQuote = { nav.navigate(Routes.newRequest(RequestType.QUOTE, product = it)) })
    }
    composable(Routes.FAVORITES) { FavoritesScreen(onBack = back, onProduct = { nav.navigate("product/$it") }) }
    composable(Routes.REQUESTS) {
        RequestsScreen(onRequest = { nav.navigate("request/$it") }, onNew = { nav.navigate(Routes.newRequest(it)) })
    }
    composable(
        Routes.REQUEST_NEW,
        listOf("type", "system", "product").map { name -> navArgument(name) { type = NavType.StringType; defaultValue = "" } },
    ) {
        RequestFormScreen(onBack = back, onSent = { id ->
            nav.navigate("request/sent/$id") { popUpTo(Routes.REQUEST_NEW) { inclusive = true } }
        })
    }
    composable(Routes.REQUEST_SENT, idArg) {
        RequestSentScreen(
            onTrack = { id -> nav.navigate("request/$id") { popUpTo(Routes.REQUEST_SENT) { inclusive = true } } },
            onHome = { nav.switchTab(Routes.HOME) },
        )
    }
    composable(Routes.REQUEST, idArg) { RequestDetailScreen(onBack = back) }
    composable(Routes.CONTACT) { ContactScreen(onBack = back) }
    composable(Routes.ABOUT) { AboutScreen(onBack = back) }
    composable(Routes.PRIVACY) { PrivacyScreen(onBack = back) }
    composable(Routes.NOTIFICATIONS) { NotificationsScreen(onBack = back, onRequest = { nav.navigate("request/$it") }) }
    composable(Routes.MORE) {
        MoreScreen(
            onContact = { nav.navigate(Routes.CONTACT) },
            onFavorites = { nav.navigate(Routes.FAVORITES) },
            onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
            onAbout = { nav.navigate(Routes.ABOUT) },
            onPrivacy = { nav.navigate(Routes.PRIVACY) },
            onLanguage = onLanguageChange,
        )
    }
}
