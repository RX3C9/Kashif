package app.kashif.feature.dialer.incall

import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow

enum class Verdict { NORMAL, SPAM, VERIFIED_BUSINESS }
object CallState { val current = MutableStateFlow<Call?>(null) }
