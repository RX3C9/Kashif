package app.kashif.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BlockRule::class, SpamEntry::class, LocalReport::class, CallNote::class],
    version = 1,
    exportSchema = false,
)
abstract class KashifDatabase : RoomDatabase() {
    abstract fun blockRules(): BlockRuleDao
    abstract fun spam(): SpamDao
    abstract fun reports(): ReportDao
    abstract fun notes(): CallNoteDao

    companion object {
        @Volatile private var instance: KashifDatabase? = null

        fun get(context: Context): KashifDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, KashifDatabase::class.java, "kashif.db")
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}
