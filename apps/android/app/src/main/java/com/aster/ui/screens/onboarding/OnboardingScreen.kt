package com.aster.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aster.R
import com.aster.ui.components.AnimatedEntrance
import com.aster.ui.components.AsterButton
import com.aster.ui.components.AsterButtonVariant
import com.aster.ui.components.AsterCard
import com.aster.ui.components.StatusBadge
import com.aster.ui.theme.AsterTheme
import com.aster.util.PermissionUtils

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val colors = AsterTheme.colors
    val currentStep by viewModel.currentStep.collectAsState()
    val context = LocalContext.current

    var allPermissionsGranted by remember { mutableStateOf(false) }
    var grantedCount by remember { mutableStateOf(0) }
    var totalCount by remember { mutableStateOf(0) }

    fun refreshPermissions() {
        val result = PermissionUtils.checkAllPermissions(context)
        allPermissionsGranted = result.allGranted
        grantedCount = result.grantedCount
        totalCount = result.totalCount
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pagerState = rememberPagerState(
        initialPage = currentStep,
        pageCount = { OnboardingViewModel.TOTAL_STEPS }
    )

    LaunchedEffect(currentStep) {
        if (pagerState.currentPage != currentStep) {
            pagerState.animateScrollToPage(currentStep)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != currentStep) {
                when {
                    page > currentStep -> repeat(page - currentStep) { viewModel.nextStep() }
                    page < currentStep -> repeat(currentStep - page) { viewModel.prevStep() }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                OnboardingViewModel.STEP_ABOUT -> AboutStep()
                OnboardingViewModel.STEP_PERMISSIONS -> PermissionsStep(
                    onOpenPermissions = onNavigateToPermissions,
                    allGranted = allPermissionsGranted,
                    grantedCount = grantedCount,
                    totalCount = totalCount
                )
            }
        }

        BottomNavigation(
            currentStep = pagerState.currentPage,
            totalSteps = OnboardingViewModel.TOTAL_STEPS,
            allPermissionsGranted = allPermissionsGranted,
            onNext = {
                if (currentStep < OnboardingViewModel.TOTAL_STEPS - 1) {
                    viewModel.nextStep()
                } else if (allPermissionsGranted) {
                    viewModel.completeOnboarding()
                    onComplete()
                }
            },
            onBack = {
                if (currentStep > 0) {
                    viewModel.prevStep()
                }
            }
        )
    }
}

@Composable
private fun AboutStep() {
    val colors = AsterTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedEntrance(delayMillis = 100) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_adaptive_fore),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedEntrance(delayMillis = 300) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                color = colors.text,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedEntrance(delayMillis = 500) {
            Text(
                text = stringResource(R.string.android_device_controller),
                style = MaterialTheme.typography.titleLarge,
                color = colors.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedEntrance(delayMillis = 650) {
            Text(
                text = stringResource(R.string.onboarding_description),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSubtle,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedEntrance(delayMillis = 800) {
            AsterCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureItem(
                        title = stringResource(R.string.ipc_mode),
                        description = stringResource(R.string.ipc_mode_description)
                    )
                    FeatureItem(
                        title = stringResource(R.string.local_mcp_server),
                        description = stringResource(R.string.local_mcp_server_description)
                    )
                    FeatureItem(
                        title = stringResource(R.string.remote_websocket),
                        description = stringResource(R.string.remote_websocket_description)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    title: String,
    description: String
) {
    val colors = AsterTheme.colors

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.text,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted
        )
    }
}

@Composable
private fun PermissionsStep(
    onOpenPermissions: () -> Unit,
    allGranted: Boolean,
    grantedCount: Int,
    totalCount: Int
) {
    val colors = AsterTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedEntrance(delayMillis = 100) {
            Text(
                text = stringResource(R.string.required_permissions),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.text,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedEntrance(delayMillis = 250) {
            Text(
                text = stringResource(R.string.permissions_description),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSubtle,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedEntrance(delayMillis = 400) {
            AsterCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.permissions_count, grantedCount, totalCount),
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (allGranted) colors.success else colors.warning,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (allGranted) stringResource(R.string.all_permissions_granted) else stringResource(R.string.permissions_granted),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSubtle
                            )
                        }

                        StatusBadge(
                            color = if (allGranted) colors.success else colors.warning,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    if (!allGranted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.border)
                        )

                        Text(
                            text = stringResource(R.string.permissions_open_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedEntrance(delayMillis = 550) {
            if (allGranted) {
                AsterCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.you_are_all_set),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.success,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.permissions_complete_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                AsterButton(
                    onClick = onOpenPermissions,
                    text = stringResource(R.string.grant_permission),
                    variant = AsterButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BottomNavigation(
    currentStep: Int,
    totalSteps: Int,
    allPermissionsGranted: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colors = AsterTheme.colors
    val isLastStep = currentStep == totalSteps - 1
    val canProceed = if (isLastStep) allPermissionsGranted else true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(bottom = 32.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val dotColor by animateColorAsState(
                    targetValue = if (index == currentStep) colors.primary else colors.border,
                    animationSpec = tween(300),
                    label = "dot_color_$index"
                )
                Box(
                    modifier = Modifier
                        .size(if (index == currentStep) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                AsterButton(
                    onClick = onBack,
                    text = stringResource(R.string.back),
                    variant = AsterButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
            }

            AsterButton(
                onClick = onNext,
                text = if (isLastStep) stringResource(R.string.get_started) else stringResource(R.string.next),
                variant = AsterButtonVariant.PRIMARY,
                enabled = canProceed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
