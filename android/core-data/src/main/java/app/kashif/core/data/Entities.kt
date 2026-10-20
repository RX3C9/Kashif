package app.kashif.core.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A user-defined block rule. [type] is a [RuleType] name, [action] a [RuleAction] name
 * (stored as strings so the schema stays stable when enums grow).
 */
@Entity
data class BlockRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val pattern: String = "",
    val action: String = RuleAction.REJECT.name,
    val label: String = "",
    val enabled: Boolean = true,
    val createdAt: Long = 0,
)

val BlockRule.ruleType: RuleType get() = RuleType.parse(type)
val BlockRule.ruleAction: RuleAction get() = RuleAction.parse(action)

/** Offline spam DB row. Keyed by [NumberHash.of] so the raw number is never stored. */
@Entity(indices = [Index("hashPrefix")])
data class SpamEntry(
    @PrimaryKey val hash: String,
    val hashPrefix: String,
    val score: Float,
    val category: String,
    val reports: Int,
    val updatedAt: Long,
    val userReported: Boolean = false,
)

/** A spam report the user made with one tap. Kept locally; uploaded only if the user opts in (M2). */
@Entity
data class LocalReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numberHash: String,
    val category: String,
    val comment: String = "",
    val createdAt: Long,
    val synced: Boolean = false,
)

@Entity data class CallNote(@PrimaryKey(autoGenerate = true) val id: Long = 0, val e164: String, val text: String, val remindAt: Long?)
