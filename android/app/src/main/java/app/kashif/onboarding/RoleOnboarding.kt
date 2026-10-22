package app.kashif.onboarding

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent

/** Default-dialer / call-screening role helpers (RoleManager, API 29+). */
object RoleOnboarding {
    fun isHeld(context: Context, role: String): Boolean {
        val rm = context.getSystemService(RoleManager::class.java) ?: return false
        return rm.isRoleAvailable(role) && rm.isRoleHeld(role)
    }

    fun requestIntent(context: Context, role: String): Intent? {
        val rm = context.getSystemService(RoleManager::class.java) ?: return null
        if (!rm.isRoleAvailable(role) || rm.isRoleHeld(role)) return null
        return rm.createRequestRoleIntent(role)
    }

    /** Runtime permissions the onboarding asks for. */
    val permissions = arrayOf(
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.POST_NOTIFICATIONS,
    )
}
