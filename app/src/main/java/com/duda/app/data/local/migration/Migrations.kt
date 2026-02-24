package com.duda.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Todas as migrations do banco devem ser declaradas aqui.
 *
 * COMO ADICIONAR UMA MIGRATION:
 * 1. Incremente a versão em AppDatabase (ex: version = 2)
 * 2. Adicione o objeto de migration aqui
 * 3. Registre em AppDatabase.ALL_MIGRATIONS
 * 4. Registre no DatabaseModule (addMigrations)
 *
 * EXEMPLO (migration de versão 1 para 2):
 * val migration1To2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE books ADD COLUMN language TEXT NOT NULL DEFAULT 'unknown'")
 *     }
 * }
 */
object Migrations {
    // Adicione migrations aqui conforme o app evolui
    // val migration1To2 = object : Migration(1, 2) { ... }
}
