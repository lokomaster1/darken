package cz.darken.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import cz.darken.app.data.PreferencesRepository
import cz.darken.app.locale.LocaleHelper
import cz.darken.app.overlay.OverlayService
import cz.darken.app.overlay.OverlayTint
import cz.darken.app.ui.CustomColorDialog
import cz.darken.app.ui.DarkenTheme
import cz.darken.app.ui.MainScreen
import cz.darken.app.ui.PrivacyPolicyDialog
import cz.darken.app.ui.SettingsDialog
import cz.darken.app.ui.SystemPermissionsDialog
import cz.darken.app.util.PermissionIntents
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var preferences: PreferencesRepository

    private val dimLevelState = mutableIntStateOf(PreferencesRepository.FALLBACK_DEFAULT)
    private var overlayActive = mutableStateOf(false)
    private var showPermissionsDialog = mutableStateOf(false)
    private var showSettingsDialog = mutableStateOf(false)
    private var showCustomColorDialog = mutableStateOf(false)
    private var showPrivacyPolicy = mutableStateOf(false)
    private var colorFilterExpanded = mutableStateOf(false)
    private var permissionRefreshKey = mutableIntStateOf(0)
    private var overlayDisabledByUser = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        permissionRefreshKey.intValue++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferences = PreferencesRepository(applicationContext)

        overlayDisabledByUser = savedInstanceState?.getBoolean(KEY_OVERLAY_DISABLED_BY_USER, false) ?: false
        colorFilterExpanded.value = savedInstanceState?.getBoolean(KEY_COLOR_FILTER_EXPANDED, false) ?: false

        setContent {
            val defaultDim by preferences.defaultDimLevel.collectAsStateWithLifecycle(
                initialValue = PreferencesRepository.FALLBACK_DEFAULT,
            )
            val hasSavedDefault by preferences.hasSavedDefaultDim.collectAsStateWithLifecycle(
                initialValue = false,
            )
            val appLanguage by preferences.appLanguage.collectAsStateWithLifecycle(
                initialValue = PreferencesRepository.LANG_SYSTEM,
            )
            val autoStartOnLaunch by preferences.autoStartOnLaunch.collectAsStateWithLifecycle(
                initialValue = false,
            )
            val tintPreset by preferences.overlayTintPreset.collectAsStateWithLifecycle(
                initialValue = OverlayTint.PRESET_GRAY,
            )
            val resolvedTintArgb by preferences.resolvedOverlayTintArgb.collectAsStateWithLifecycle(
                initialValue = OverlayTint.GrayArgb,
            )
            val customTintArgb by preferences.customOverlayTintArgb.collectAsStateWithLifecycle(
                initialValue = OverlayTint.AmberArgb,
            )
            val privacyAcknowledged by preferences.privacyAcknowledged.collectAsStateWithLifecycle(
                initialValue = false,
            )
            var dimLevel by dimLevelState
            var snackbarMessage by mutableStateOf<String?>(null)
            val overlayOn by overlayActive
            val permissionsOpen by showPermissionsDialog
            val settingsOpen by showSettingsDialog
            val refreshKey by permissionRefreshKey
            val colorExpanded by colorFilterExpanded
            var dialogRestorePreset by remember { mutableStateOf(OverlayTint.PRESET_GRAY) }
            var dialogRestoreArgb by remember { mutableStateOf(OverlayTint.GrayArgb) }

            if (permissionsOpen) {
                @Suppress("UNUSED_VARIABLE")
                val triggerRefresh = refreshKey
                SystemPermissionsDialog(
                    overlayGranted = canDrawOverlays(),
                    notificationsGranted = notificationsGranted(),
                    batteryUnrestricted = batteryUnrestricted(),
                    onOverlayClick = {
                        startActivity(
                            PermissionIntents.overlaySettings(this)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                    onNotificationsClick = { requestNotificationsIfNeeded() },
                    onBatteryClick = {
                        startActivity(
                            PermissionIntents.batteryOptimizationSettings(this)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                    onAppDetailsClick = {
                        startActivity(
                            PermissionIntents.appDetailsSettings(this)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                    onDismiss = { showPermissionsDialog.value = false },
                )
            }

            if (showCustomColorDialog.value) {
                CustomColorDialog(
                    currentArgb = if (tintPreset == OverlayTint.PRESET_CUSTOM) {
                        resolvedTintArgb
                    } else {
                        customTintArgb
                    },
                    onColorApply = { argb ->
                        lifecycleScope.launch {
                            preferences.setCustomOverlayTintArgb(argb)
                            if (overlayOn) {
                                OverlayService.refreshAppearance(this@MainActivity)
                            }
                        }
                    },
                    onConfirm = { showCustomColorDialog.value = false },
                    onDismiss = {
                        lifecycleScope.launch {
                            if (dialogRestorePreset == OverlayTint.PRESET_CUSTOM) {
                                preferences.setCustomOverlayTintArgb(dialogRestoreArgb)
                            } else {
                                preferences.setOverlayTintPreset(dialogRestorePreset)
                            }
                            if (overlayOn) {
                                OverlayService.refreshAppearance(this@MainActivity)
                            }
                        }
                        showCustomColorDialog.value = false
                    },
                )
            }

            val showPrivacy = showPrivacyPolicy.value || !privacyAcknowledged

            if (showPrivacy) {
                PrivacyPolicyDialog(
                    firstRun = !privacyAcknowledged,
                    onAccept = {
                        if (!privacyAcknowledged) {
                            lifecycleScope.launch {
                                preferences.setPrivacyAcknowledged(true)
                            }
                        }
                        showPrivacyPolicy.value = false
                    },
                    onDismiss = { showPrivacyPolicy.value = false },
                )
            }

            if (settingsOpen) {
                SettingsDialog(
                    savedLanguage = appLanguage,
                    savedAutoStartOnLaunch = autoStartOnLaunch,
                    onConfirm = { language, autoStart ->
                        applySettings(language, autoStart)
                        showSettingsDialog.value = false
                    },
                    onDismiss = { showSettingsDialog.value = false },
                    onOpenPrivacy = { showPrivacyPolicy.value = true },
                    onOpenGithub = { openGithub() },
                    onOpenContact = { openContactEmail() },
                )
            }

            DarkenTheme {
                MainScreen(
                    dimLevel = dimLevel,
                    defaultDimLevel = defaultDim,
                    hasSavedDefault = hasSavedDefault,
                    overlayActive = overlayOn,
                    canDrawOverlays = canDrawOverlays(),
                    notificationsGranted = notificationsGranted(),
                    batteryUnrestricted = batteryUnrestricted(),
                    onDimLevelChange = { newLevel ->
                        dimLevelState.intValue = newLevel
                        if (overlayOn) {
                            OverlayService.update(this, newLevel)
                        }
                    },
                    onToggleOverlay = { enable ->
                        if (!enable) {
                            overlayDisabledByUser = true
                            OverlayService.stop(this)
                            overlayActive.value = false
                        } else {
                            overlayDisabledByUser = false
                            startOverlay(dimLevelState.intValue) { snackbarMessage = it }
                        }
                    },
                    onSaveDefault = {
                        lifecycleScope.launch {
                            preferences.setDefaultDimLevel(dimLevel)
                            snackbarMessage = getString(R.string.default_saved, dimLevel)
                        }
                    },
                    onOpenPermissions = { showPermissionsDialog.value = true },
                    onOpenSettings = { showSettingsDialog.value = true },
                    onExitApp = { exitAppFully() },
                    colorFilterExpanded = colorExpanded,
                    onColorFilterExpandedChange = { colorFilterExpanded.value = it },
                    tintPreset = tintPreset,
                    resolvedTintArgb = resolvedTintArgb,
                    onTintPresetSelected = { preset ->
                        lifecycleScope.launch {
                            preferences.setOverlayTintPreset(preset)
                            if (overlayOn) {
                                OverlayService.refreshAppearance(this@MainActivity)
                            }
                        }
                    },
                    onCustomTintClick = {
                        dialogRestorePreset = tintPreset
                        dialogRestoreArgb = if (tintPreset == OverlayTint.PRESET_CUSTOM) {
                            resolvedTintArgb
                        } else {
                            customTintArgb
                        }
                        showCustomColorDialog.value = true
                    },
                    snackbarMessage = snackbarMessage,
                    onSnackbarShown = { snackbarMessage = null },
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_OVERLAY_DISABLED_BY_USER, overlayDisabledByUser)
        outState.putBoolean(KEY_COLOR_FILTER_EXPANDED, colorFilterExpanded.value)
    }

    override fun onStart() {
        super.onStart()
        MainActivityHolder.register(this)
    }

    override fun onStop() {
        MainActivityHolder.unregister(this)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        overlayActive.value = isOverlayServiceRunning()
        permissionRefreshKey.intValue++
        lifecycleScope.launch {
            syncDimLevelFromState()
            maybeAutoStartOverlay()
        }
    }

    fun exitAppFully() {
        OverlayService.stop(this)
        overlayActive.value = false
        finishAndRemoveTask()
    }

    private suspend fun syncDimLevelFromState() {
        if (overlayActive.value) {
            OverlayService.lastKnownDimLevel?.let { dimLevelState.intValue = it }
        }
    }

    private fun applySettings(language: String, autoStartOnLaunch: Boolean) {
        lifecycleScope.launch {
            val previousLanguage = preferences.appLanguage.first()
            preferences.setAppLanguage(language)
            preferences.setAutoStartOnLaunch(autoStartOnLaunch)
            preferences.setNotificationMode(PreferencesRepository.NOTIF_MINIMAL)

            if (previousLanguage != language) {
                LocaleHelper.apply(language)
                recreate()
            }
        }
    }

    private fun maybeAutoStartOverlay() {
        if (overlayDisabledByUser) return
        lifecycleScope.launch {
            if (!preferences.autoStartOnLaunchEnabled()) return@launch
            if (!canDrawOverlays() || !notificationsGranted()) return@launch
            if (isOverlayServiceRunning()) {
                overlayActive.value = true
                return@launch
            }
            val level = preferences.resolveDefaultDimLevel()
            dimLevelState.intValue = level
            startOverlay(level) { /* silent auto-start */ }
        }
    }

    private fun startOverlay(level: Int, onMessage: (String) -> Unit) {
        val clamped = level.coerceIn(PreferencesRepository.MIN_DIM, PreferencesRepository.MAX_DIM)
        dimLevelState.intValue = clamped

        if (!canDrawOverlays()) {
            showPermissionsDialog.value = true
            return
        }
        if (!notificationsGranted()) {
            requestNotificationsIfNeeded()
            return
        }
        if (!batteryUnrestricted()) {
            onMessage(getString(R.string.permission_battery_body))
        }
        OverlayService.start(this, clamped)
        overlayActive.value = true
    }

    private fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationsGranted()
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openGithub() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url)))
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun openContactEmail() {
        val intent = Intent(
            Intent.ACTION_SENDTO,
            Uri.parse("mailto:${getString(R.string.contact_email)}"),
        )
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun canDrawOverlays(): Boolean = PermissionIntents.canDrawOverlays(this)

    private fun notificationsGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun batteryUnrestricted(): Boolean =
        PermissionIntents.isIgnoringBatteryOptimizations(this)

    private fun isOverlayServiceRunning(): Boolean {
        val manager = getSystemService(android.app.ActivityManager::class.java) ?: return false
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Int.MAX_VALUE).any { info ->
            info.service.className == OverlayService::class.java.name
        }
    }

    companion object {
        private const val KEY_OVERLAY_DISABLED_BY_USER = "overlay_disabled_by_user"
        private const val KEY_COLOR_FILTER_EXPANDED = "color_filter_expanded"
    }
}
