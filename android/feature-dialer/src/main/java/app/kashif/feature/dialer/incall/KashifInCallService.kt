package app.kashif.feature.dialer.incall

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService

/** Default-dialer in-call service. Holds ROLE_DIALER and drives InCallActivity. */
class KashifInCallService : InCallService() {
    override fun onCreate() { super.onCreate(); instance = this }
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }
    override fun onCallAudioStateChanged(state: CallAudioState) {
        CallState.muted.value = state.isMuted
        CallState.speaker.value = state.route == CallAudioState.ROUTE_SPEAKER
    }
    override fun onCallAdded(call: Call) {
        CallState.current.value = call
        startActivity(Intent(this, InCallActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    override fun onCallRemoved(call: Call) { if (CallState.current.value == call) CallState.current.value = null }

    companion object {
        @Volatile var instance: KashifInCallService? = null
        fun toggleMute() { instance?.let { it.setMuted(!CallState.muted.value) } }
        fun toggleSpeaker() {
            instance?.setAudioRoute(if (CallState.speaker.value) CallAudioState.ROUTE_EARPIECE else CallAudioState.ROUTE_SPEAKER)
        }
    }
}
