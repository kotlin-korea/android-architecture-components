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

package com.example.android.persistence.viewmodel

import android.app.Application
import android.arch.core.util.Function
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations

import com.example.android.persistence.db.DatabaseCreator
import com.example.android.persistence.db.entity.ProductEntity

class ProductListViewModel(application: Application) : AndroidViewModel(application) {

    private val ABSENT = MutableLiveData<List<ProductEntity>>()

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    val products: LiveData<List<ProductEntity>>

    init {

        ABSENT.value = null

        val databaseCreator = DatabaseCreator.getInstance(this.getApplication())

        val databaseCreated = databaseCreator.isDatabaseCreated
        products = Transformations.switchMap(databaseCreated
        ) { isDbCreated ->
            if (java.lang.Boolean.TRUE != isDbCreated) { // Not needed here, but watch out for null

                ABSENT
            } else {

                databaseCreator.database!!.productDao().loadAllProducts()
            }
        }

        databaseCreator.createDb(this.getApplication())
    }
}
