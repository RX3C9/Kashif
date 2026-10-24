package app.kashif.feature.callerid.overlay

import app.kashif.core.data.CallVerdict

/** Pure description of the caller-ID popup; colours follow docs/DESIGN.md (blue/red/green). */
data class OverlayModel(val title: String, val subtitle: String, val colorArgb: Int) {
    companion object {
        const val BLUE = 0xFF0A84FF.toInt()
        const val RED = 0xFFE5322D.toInt()
        const val GREEN = 0xFF1FA85A.toInt()

        fun of(verdict: CallVerdict, label: String?, reports: Int, number: String?, arabic: Boolean): OverlayModel {
            val shown = number ?: if (arabic) "رقم مجهول" else "Unknown number"
            return when (verdict) {
                CallVerdict.SPAM -> OverlayModel(
                    label ?: shown,
                    if (arabic) "⚠ سبام محتمل · $reports بلاغ" else "⚠ Likely spam · $reports reports",
                    RED)
                CallVerdict.VERIFIED_BUSINESS -> OverlayModel(
                    label ?: shown,
                    if (arabic) "✔ شركة موثقة" else "✔ Verified business",
                    GREEN)
                CallVerdict.NORMAL -> OverlayModel(
                    label ?: shown, if (label != null) shown else if (arabic) "لا توجد بلاغات" else "No reports", BLUE)
            }
        }
    }
}
