package app.kashif.feature.dialer.incall

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService

/** Default-dialer in-call service. Holds ROLE_DIALER and drives InCallActivity. */
class KashifInCallService : InCallService() {
    override fun onCallAdded(call: Call) {
        CallState.current.value = call
        startActivity(Intent(this, InCallActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    override fun onCallRemoved(call: Call) { if (CallState.current.value == call) CallState.current.value = null }
}
