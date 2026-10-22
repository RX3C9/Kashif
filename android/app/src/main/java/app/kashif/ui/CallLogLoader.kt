package app.kashif.ui

import android.content.Context
import android.provider.CallLog
import app.kashif.core.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/** Reads the system call log (needs READ_CALL_LOG) and colours rows from the local spam DB. Nothing leaves the device. */
object CallLogLoader {
    suspend fun load(ctx: Context, limit: Int = 200): List<CallRow> = withContext(Dispatchers.IO) {
        val raw = ArrayList<RawCall>()
        try {
            ctx.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE,
                    CallLog.Calls.DATE, CallLog.Calls.DURATION),
                null, null, CallLog.Calls.DATE + " DESC"
            )?.use { c ->
                while (c.moveToNext() && raw.size < limit) {
                    raw.add(RawCall(c.getString(0) ?: "", c.getString(1), c.getInt(2), c.getLong(3), c.getLong(4)))
                }
            }
        } catch (_: SecurityException) {
            return@withContext emptyList()
        }
        val repo = SpamRepository(KashifDatabase.get(ctx))
        CallLogFormat.group(raw) { n ->
            val e164 = PhoneNumbers.toE164(n)
            runBlocking { repo.lookup(e164) }
        }
    }

    suspend fun report(ctx: Context, number: String, category: String, block: Boolean) = withContext(Dispatchers.IO) {
        val e164 = PhoneNumbers.toE164(number) ?: return@withContext
        SpamRepository(KashifDatabase.get(ctx)).reportSpam(e164, category, block)
    }
}
