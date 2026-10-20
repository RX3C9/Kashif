package app.kashif.feature.callerid.screening

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import android.util.Log
import app.kashif.core.data.CachedVerdict
import app.kashif.core.data.IncomingNumber
import app.kashif.core.data.KashifDatabase
import app.kashif.core.data.NumberHash
import app.kashif.core.data.RuleAction
import app.kashif.core.data.VerdictCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Runs before the phone rings: contacts (on-device only) → user rules → offline spam DB.
 * Never uploads contacts or the caller's number. Fails open (lets the call ring) on any error.
 */
class KashifScreeningService : CallScreeningService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(details: Call.Details) {
        if (details.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(details, CallResponse.Builder().build()); return
        }
        scope.launch {
            val decision = withTimeoutOrNull(3_000) { runCatching { decide(details) }.getOrNull() }
            val response = CallResponse.Builder()
            if (decision != null) {
                val number = rawNumber(details)
                IncomingNumber.of(number).e164?.let {
                    VerdictCache.put(it, CachedVerdict(decision.verdict, decision.label, decision.reports, System.currentTimeMillis()))
                }
                when (decision.action) {
                    RuleAction.REJECT -> response.setDisallowCall(true).setRejectCall(true).setSkipNotification(true)
                    RuleAction.SILENCE -> response.setSilenceCall(true)
                    null -> Unit
                }
            }
            runCatching { respondToCall(details, response.build()) }.onFailure { Log.w(TAG, "respond failed", it) }
        }
    }

    private fun rawNumber(details: Call.Details): String? =
        if (details.handlePresentation != TelecomManager.PRESENTATION_ALLOWED) null
        else details.handle?.schemeSpecificPart

    private suspend fun decide(details: Call.Details): Decision {
        val raw = rawNumber(details)
        val probe = IncomingNumber.of(raw)
        val n = probe.copy(inContacts = raw != null && isContact(raw))
        val db = KashifDatabase.get(this)
        val spam = n.e164?.let { db.spam().byHash(NumberHash.of(it)) }
        return ScreeningDecider.decide(n, db.blockRules().enabled(), spam)
    }

    /** Local-only phonebook check (PhoneLookup); nothing is read beyond a yes/no. */
    private fun isContact(number: String): Boolean {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return false
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        return runCatching {
            contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
                ?.use { it.count > 0 } ?: false
        }.getOrDefault(false)
    }

    private companion object { const val TAG = "KashifScreening" }
}
