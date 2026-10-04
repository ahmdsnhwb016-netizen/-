package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun StudentAssistantApp(viewModel: StudentViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeSubject by viewModel.activeSubjectDetail.collectAsStateWithLifecycle()
    val showStudyTimer by viewModel.showStudyTimer.collectAsStateWithLifecycle()
    val activeQuiz by viewModel.activeQuiz.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // Force RTL layout for genuine Arabic application experience
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MyApplicationTheme {
            // Check onboarding
            val hasOnboarded = profile?.hasCompletedOnboarding == true

            if (!hasOnboarded) {
                OnboardingScreen(viewModel = viewModel)
            } else if (showStudyTimer) {
                BackHandler { viewModel.closeStudyTimer() }
                StudyTimerScreen(viewModel = viewModel)
            } else {
                // Handle system back navigation cleanly
                BackHandler(enabled = activeSubject != null || activeQuiz != null || currentScreen != AppScreen.HOME) {
                    when {
                        activeQuiz != null -> viewModel.closeQuiz()
                        activeSubject != null -> viewModel.closeSubjectDetail()
                        currentScreen != AppScreen.HOME -> viewModel.navigateTo(AppScreen.HOME)
                    }
                }

                val navItems = listOf(
                    NavItem(AppScreen.HOME, "الرئيسية", Icons.Default.Home, Icons.Outlined.Home),
                    NavItem(AppScreen.SUBJECTS, "الدراسة", Icons.Default.MenuBook, Icons.Outlined.MenuBook),
                    NavItem(AppScreen.TASKS, "المهام", Icons.Default.Checklist, Icons.Outlined.Checklist),
                    NavItem(AppScreen.ASSISTANT, "المساعد", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
                    NavItem(AppScreen.MORE, "المزيد", Icons.Default.MoreHoriz, Icons.Outlined.MoreHoriz)
                )

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("main_bottom_nav"),
                            tonalElevation = 6.dp
                        ) {
                            navItems.forEach { item ->
                                val selected = currentScreen == item.screen
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.navigateTo(item.screen) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) },
                                    modifier = Modifier.testTag("nav_item_${item.screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                            AppScreen.SUBJECTS -> SubjectsScreen(viewModel = viewModel)
                            AppScreen.TASKS -> TasksScreen(viewModel = viewModel)
                            AppScreen.ASSISTANT -> AssistantScreen(viewModel = viewModel)
                            AppScreen.MORE -> MoreScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
