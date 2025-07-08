/**
 * Проект : Yakki
 * Файл   : YakkiDatabase.kt
 * Версия : 1.1 (first usable Room DB)
 * Дата   : 30 июня 2025 г.
 * Статус : ✅ Готово к интеграции
 *
 * Что нового (v1.1)
 * ────────────────────────────────────────────────────────────
 * • Полноценный Room-Database c LanguageDao.
 * • Синглтон-инстанс через `Room.databaseBuilder`.
 * • Автоматическая первичная загрузка ISO-языков (корутины).
 * • Простейшая auto-migration (v1→v2) показана в комментариях.
 */
package com.yakki.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Список энтити, которые хранит база.  */
@Database(
    entities = [Language::class],        // ← добавляйте новые по мере роста
    version = 1,                         // <-- при изменениях ↑ цифру и добавьте миграцию
    exportSchema = true
)
abstract class YakkiDatabase : RoomDatabase() {

    /** Доступ к DAO. */
    abstract fun languageDao(): LanguageDao

    companion object {
        @Volatile
        private var INSTANCE: YakkiDatabase? = null

        /**
         * Получаем синглтон-экземпляр базы.
         *
         * @param context  ApplicationContext
         * @param scope    любой CoroutineScope (обычно = applicationScope)
         */
        fun getInstance(
            context: Context,
            scope: CoroutineScope
        ): YakkiDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, scope).also { INSTANCE = it }
            }

        // -------------------------------- private --------------------------------

        private fun buildDatabase(
            context: Context,
            scope: CoroutineScope
        ): YakkiDatabase =
            Room.databaseBuilder(
                context,
                YakkiDatabase::class.java,
                "yakki.db"
            )
                // пример будущей миграции 1 → 2
                // .addMigrations(
                //     object : Migration(1, 2) { override fun migrate(db: SupportSQLiteDatabase) { … } }
                // )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        /* Первичное заполнение таблицы языков. */
                        scope.launch(Dispatchers.IO) {
                            getInstance(context, scope)
                                .languageDao()
                                .upsert(*Language.prepopulate())
                        }
                    }
                })
                .fallbackToDestructiveMigration()     // можно убрать, когда появятся миграции
                .build()
    }
}
