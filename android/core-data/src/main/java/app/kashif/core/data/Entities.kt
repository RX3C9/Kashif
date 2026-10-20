package app.kashif.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity data class BlockRule(@PrimaryKey(autoGenerate = true) val id: Long = 0, val type: String, val pattern: String, val action: String = "REJECT")
@Entity data class SpamEntry(@PrimaryKey val hash: String, val hashPrefix: String, val score: Float, val category: String, val reports: Int, val updatedAt: Long)
@Entity data class CallNote(@PrimaryKey(autoGenerate = true) val id: Long = 0, val e164: String, val text: String, val remindAt: Long?)
