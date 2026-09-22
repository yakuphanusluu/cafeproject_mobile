package com.brewandbean.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.brewandbean.app.ui.customer.CartScreen
import com.brewandbean.app.ui.customer.CustomerViewModel
import com.brewandbean.app.ui.customer.MenuScreen
import com.brewandbean.app.ui.theme.BrewAndBeanTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.brewandbean.app.util.LanguageManager.init(applicationContext)
        
        createNotificationChannel()
        askNotificationPermission()

        setContent {
            BrewAndBeanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val customerViewModel = hiltViewModel<CustomerViewModel>()
                    val authViewModel = hiltViewModel<com.brewandbean.app.ui.auth.AuthViewModel>()
                    
                    val startDest = "menu"

                    NavHost(navController = navController, startDestination = startDest) {
                        composable("login") {
                            com.brewandbean.app.ui.auth.LoginScreen(
                                viewModel = authViewModel,
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToVerify = { email -> navController.navigate("verify/$email") },
                                onNavigateToForgot = { navController.navigate("forgot_password") },
                                onLoginSuccess = {
                                    navController.navigate("menu") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("register") {
                            com.brewandbean.app.ui.auth.RegisterScreen(
                                viewModel = authViewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToVerify = { email ->
                                    navController.navigate("verify/$email") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("verify/{email}") { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            com.brewandbean.app.ui.auth.VerifyScreen(
                                email = email,
                                viewModel = authViewModel,
                                onBack = { navController.navigate("login") { popUpTo(0) } },
                                onVerifySuccess = {
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }
                        composable("forgot_password") {
                            com.brewandbean.app.ui.auth.ForgotPasswordScreen(
                                viewModel = authViewModel,
                                onBack = { navController.popBackStack() },
                                onSuccess = {
                                    navController.navigate("login") { popUpTo(0) }
                                }
                            )
                        }
                        composable("menu") {
                            MenuScreen(
                                viewModel = customerViewModel,
                                onCartClick = { navController.navigate("cart") },
                                onProfileClick = {
                                    if (authViewModel.currentUser.value != null) navController.navigate("profile") else navController.navigate("login")
                                }
                            )
                        }
                        composable("profile") {
                            com.brewandbean.app.ui.customer.ProfileScreen(
                                viewModel = authViewModel,
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    navController.navigate("login") { popUpTo(0) }
                                }
                            )
                        }
                        composable("cart") {
                            CartScreen(
                                viewModel = customerViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Siparis Bildirimleri"
            val descriptionText = "Siparisiniz hazir oldugunda bildirim alirsiniz"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("ORDER_STATUS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
