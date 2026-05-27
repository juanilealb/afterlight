package com.juani.afterlight.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ArticleDao_Impl(
  __db: RoomDatabase,
) : ArticleDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfArticleEntity: EntityInsertAdapter<ArticleEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfArticleEntity = object : EntityInsertAdapter<ArticleEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `articles` (`id`,`title`,`source`,`originalUrl`,`resolvedUrl`,`markdownPath`,`wordCount`,`estimatedReadingMinutes`,`status`,`capturedAt`,`lastReadPosition`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ArticleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        val _tmpSource: String? = entity.source
        if (_tmpSource == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpSource)
        }
        val _tmpOriginalUrl: String? = entity.originalUrl
        if (_tmpOriginalUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpOriginalUrl)
        }
        val _tmpResolvedUrl: String? = entity.resolvedUrl
        if (_tmpResolvedUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpResolvedUrl)
        }
        statement.bindText(6, entity.markdownPath)
        statement.bindLong(7, entity.wordCount.toLong())
        statement.bindLong(8, entity.estimatedReadingMinutes.toLong())
        statement.bindText(9, entity.status)
        statement.bindLong(10, entity.capturedAt)
        statement.bindLong(11, entity.lastReadPosition.toLong())
      }
    }
  }

  public override suspend fun insert(article: ArticleEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfArticleEntity.insertAndReturnId(_connection, article)
    _result
  }

  public override fun observeAll(): Flow<List<ArticleEntity>> {
    val _sql: String = "SELECT * FROM articles ORDER BY capturedAt DESC"
    return createFlow(__db, false, arrayOf("articles")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfOriginalUrl: Int = getColumnIndexOrThrow(_stmt, "originalUrl")
        val _columnIndexOfResolvedUrl: Int = getColumnIndexOrThrow(_stmt, "resolvedUrl")
        val _columnIndexOfMarkdownPath: Int = getColumnIndexOrThrow(_stmt, "markdownPath")
        val _columnIndexOfWordCount: Int = getColumnIndexOrThrow(_stmt, "wordCount")
        val _columnIndexOfEstimatedReadingMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedReadingMinutes")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCapturedAt: Int = getColumnIndexOrThrow(_stmt, "capturedAt")
        val _columnIndexOfLastReadPosition: Int = getColumnIndexOrThrow(_stmt, "lastReadPosition")
        val _result: MutableList<ArticleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ArticleEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSource: String?
          if (_stmt.isNull(_columnIndexOfSource)) {
            _tmpSource = null
          } else {
            _tmpSource = _stmt.getText(_columnIndexOfSource)
          }
          val _tmpOriginalUrl: String?
          if (_stmt.isNull(_columnIndexOfOriginalUrl)) {
            _tmpOriginalUrl = null
          } else {
            _tmpOriginalUrl = _stmt.getText(_columnIndexOfOriginalUrl)
          }
          val _tmpResolvedUrl: String?
          if (_stmt.isNull(_columnIndexOfResolvedUrl)) {
            _tmpResolvedUrl = null
          } else {
            _tmpResolvedUrl = _stmt.getText(_columnIndexOfResolvedUrl)
          }
          val _tmpMarkdownPath: String
          _tmpMarkdownPath = _stmt.getText(_columnIndexOfMarkdownPath)
          val _tmpWordCount: Int
          _tmpWordCount = _stmt.getLong(_columnIndexOfWordCount).toInt()
          val _tmpEstimatedReadingMinutes: Int
          _tmpEstimatedReadingMinutes = _stmt.getLong(_columnIndexOfEstimatedReadingMinutes).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCapturedAt: Long
          _tmpCapturedAt = _stmt.getLong(_columnIndexOfCapturedAt)
          val _tmpLastReadPosition: Int
          _tmpLastReadPosition = _stmt.getLong(_columnIndexOfLastReadPosition).toInt()
          _item = ArticleEntity(_tmpId,_tmpTitle,_tmpSource,_tmpOriginalUrl,_tmpResolvedUrl,_tmpMarkdownPath,_tmpWordCount,_tmpEstimatedReadingMinutes,_tmpStatus,_tmpCapturedAt,_tmpLastReadPosition)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun `get`(id: Long): ArticleEntity? {
    val _sql: String = "SELECT * FROM articles WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfOriginalUrl: Int = getColumnIndexOrThrow(_stmt, "originalUrl")
        val _columnIndexOfResolvedUrl: Int = getColumnIndexOrThrow(_stmt, "resolvedUrl")
        val _columnIndexOfMarkdownPath: Int = getColumnIndexOrThrow(_stmt, "markdownPath")
        val _columnIndexOfWordCount: Int = getColumnIndexOrThrow(_stmt, "wordCount")
        val _columnIndexOfEstimatedReadingMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedReadingMinutes")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCapturedAt: Int = getColumnIndexOrThrow(_stmt, "capturedAt")
        val _columnIndexOfLastReadPosition: Int = getColumnIndexOrThrow(_stmt, "lastReadPosition")
        val _result: ArticleEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSource: String?
          if (_stmt.isNull(_columnIndexOfSource)) {
            _tmpSource = null
          } else {
            _tmpSource = _stmt.getText(_columnIndexOfSource)
          }
          val _tmpOriginalUrl: String?
          if (_stmt.isNull(_columnIndexOfOriginalUrl)) {
            _tmpOriginalUrl = null
          } else {
            _tmpOriginalUrl = _stmt.getText(_columnIndexOfOriginalUrl)
          }
          val _tmpResolvedUrl: String?
          if (_stmt.isNull(_columnIndexOfResolvedUrl)) {
            _tmpResolvedUrl = null
          } else {
            _tmpResolvedUrl = _stmt.getText(_columnIndexOfResolvedUrl)
          }
          val _tmpMarkdownPath: String
          _tmpMarkdownPath = _stmt.getText(_columnIndexOfMarkdownPath)
          val _tmpWordCount: Int
          _tmpWordCount = _stmt.getLong(_columnIndexOfWordCount).toInt()
          val _tmpEstimatedReadingMinutes: Int
          _tmpEstimatedReadingMinutes = _stmt.getLong(_columnIndexOfEstimatedReadingMinutes).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCapturedAt: Long
          _tmpCapturedAt = _stmt.getLong(_columnIndexOfCapturedAt)
          val _tmpLastReadPosition: Int
          _tmpLastReadPosition = _stmt.getLong(_columnIndexOfLastReadPosition).toInt()
          _result = ArticleEntity(_tmpId,_tmpTitle,_tmpSource,_tmpOriginalUrl,_tmpResolvedUrl,_tmpMarkdownPath,_tmpWordCount,_tmpEstimatedReadingMinutes,_tmpStatus,_tmpCapturedAt,_tmpLastReadPosition)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePosition(id: Long, position: Int) {
    val _sql: String = "UPDATE articles SET lastReadPosition = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, position.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
