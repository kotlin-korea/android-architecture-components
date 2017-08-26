/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.persistence.db

import com.example.android.persistence.db.entity.CommentEntity
import com.example.android.persistence.db.entity.ProductEntity
import com.example.android.persistence.model.Product

import java.util.ArrayList
import java.util.Date
import java.util.Random
import java.util.concurrent.TimeUnit

/** Generates dummy data and inserts them into the database  */
internal object DatabaseInitUtil {

    private val FIRST = arrayOf("Special edition", "New", "Cheap", "Quality", "Used")
    private val SECOND = arrayOf("Three-headed Monkey", "Rubber Chicken", "Pint of Grog", "Monocle")
    private val DESCRIPTION = arrayOf("is finally here", "is recommended by Stan S. Stanman", "is the best sold product on Mêlée Island", "is \uD83D\uDCAF", "is ❤️", "is fine")
    private val COMMENTS = arrayOf("Comment 1", "Comment 2", "Comment 3", "Comment 4", "Comment 5", "Comment 6")

    fun initializeDb(db: AppDatabase) {
        val products = ArrayList<ProductEntity>(FIRST.size * SECOND.size)
        val comments = ArrayList<CommentEntity>()

        generateData(products, comments)

        insertData(db, products, comments)
    }

    private fun generateData(products: MutableList<ProductEntity>, comments: MutableList<CommentEntity>) {
        val rnd = Random()
        for (i in FIRST.indices) {
            for (j in SECOND.indices) {

                val id = FIRST.size * i + j + 1
                val name = FIRST[i] + " " + SECOND[j]
                val description = name + FIRST.size * i + j + 1
                val price = FIRST.size * i + j + 1
                val product = ProductEntity(id, name, description, price)
                products.add(product)
            }
        }

        for ((id, name) in products) {
            val commentsNumber = rnd.nextInt(5) + 1
            (0 until commentsNumber).mapTo(comments) {
                CommentEntity(0, id, COMMENTS[it] + " for " + name,
                        Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis((commentsNumber - it).toLong()) + TimeUnit.HOURS.toMillis(it.toLong())))
            }
        }
    }

    private fun insertData(db: AppDatabase, products: List<ProductEntity>, comments: List<CommentEntity>) {
        db.beginTransaction()
        try {
            db.productDao().insertAll(products)
            db.commentDao().insertAll(comments)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
