package app.kashif.feature.dialer.incall

import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow

object CallState { val current = MutableStateFlow<Call?>(null) }
