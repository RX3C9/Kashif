package app.kashif.feature.callerid.screening

import android.telecom.Call
import android.telecom.CallScreeningService

/** Runs before ring: local rules → offline spam DB → (opt-in) hash-prefix lookup. Never uploads contacts. */
class KashifScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val number = details.handle?.schemeSpecificPart.orEmpty()
        val block = RuleEngine.shouldBlock(number)
        respondToCall(details, CallResponse.Builder()
            .setDisallowCall(block).setRejectCall(block)
            .setSkipCallLog(false).setSkipNotification(block).build())
    }
}
