package com.juani.afterlight

import android.app.Application
import com.juani.afterlight.data.AfterlightDatabase
import com.juani.afterlight.data.ArticleRepository

class AfterlightApp : Application() {
    val database by lazy { AfterlightDatabase.create(this) }
    val repository by lazy { ArticleRepository(this, database.articleDao()) }
}
