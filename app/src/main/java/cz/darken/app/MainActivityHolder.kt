package cz.darken.app

import java.lang.ref.WeakReference

object MainActivityHolder {

    private var activityRef: WeakReference<MainActivity>? = null

    fun register(activity: MainActivity) {
        activityRef = WeakReference(activity)
    }

    fun unregister(activity: MainActivity) {
        if (activityRef?.get() === activity) {
            activityRef = null
        }
    }

    fun requestExitApp() {
        activityRef?.get()?.runOnUiThread {
            activityRef?.get()?.exitAppFully()
        }
    }

    fun notifyOverlayStopped() {
        activityRef?.get()?.runOnUiThread {
            activityRef?.get()?.onOverlayStoppedExternally()
        }
    }
}
