package com.juani.afterlight.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AfterlightDatabase_Impl : AfterlightDatabase() {
  private val _articleDao: Lazy<ArticleDao> = lazy {
    ArticleDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "f4977be7b6b751cb32d0c9032e3fab04", "dcbd8f587189abd1747e44d8d07f51dc") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `articles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `source` TEXT, `originalUrl` TEXT, `resolvedUrl` TEXT, `markdownPath` TEXT NOT NULL, `wordCount` INTEGER NOT NULL, `estimatedReadingMinutes` INTEGER NOT NULL, `status` TEXT NOT NULL, `capturedAt` INTEGER NOT NULL, `lastReadPosition` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f4977be7b6b751cb32d0c9032e3fab04')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `articles`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsArticles: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsArticles.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("source", TableInfo.Column("source", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("originalUrl", TableInfo.Column("originalUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("resolvedUrl", TableInfo.Column("resolvedUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("markdownPath", TableInfo.Column("markdownPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("wordCount", TableInfo.Column("wordCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("estimatedReadingMinutes", TableInfo.Column("estimatedReadingMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("capturedAt", TableInfo.Column("capturedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArticles.put("lastReadPosition", TableInfo.Column("lastReadPosition", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysArticles: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesArticles: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoArticles: TableInfo = TableInfo("articles", _columnsArticles, _foreignKeysArticles, _indicesArticles)
        val _existingArticles: TableInfo = read(connection, "articles")
        if (!_infoArticles.equals(_existingArticles)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |articles(com.juani.afterlight.data.ArticleEntity).
              | Expected:
              |""".trimMargin() + _infoArticles + """
              |
              | Found:
              |""".trimMargin() + _existingArticles)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "articles")
  }

  public override fun clearAllTables() {
    super.performClear(false, "articles")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ArticleDao::class, ArticleDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun articleDao(): ArticleDao = _articleDao.value
}
