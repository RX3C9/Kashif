package app.kashif.feature.ai

/** Seed heuristics for Egyptian scam texts/transcripts; replaced by an on-device TFLite model in M3. */
object ScamPatterns {
    private val signals = listOf("انستاباي", "instapay", "كود التفعيل", "رمز التحقق", "OTP", "كسبت", "جايزة",
        "حسابك اتوقف", "تحديث بيانات", "خدمة عملاء البنك", "فودافون كاش", "الرقم السري")
    fun score(text: String): Float {
        val t = text.lowercase()
        return (signals.count { t.contains(it.lowercase()) } / 3f).coerceAtMost(1f)
    }
}
