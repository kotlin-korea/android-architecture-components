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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.Room
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.example.android.persistence.db.AppDatabase.Companion.DATABASE_NAME

import java.util.concurrent.atomic.AtomicBoolean

import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Creates the [AppDatabase] asynchronously, exposing a LiveData object to notify of creation.
 */
class DatabaseCreator {

    private val mIsDatabaseCreated = MutableLiveData<Boolean>()

    var database: AppDatabase? = null
        private set

    private val mInitializing = AtomicBoolean(true)

    /** Used to observe when the database initialization is done  */
    val isDatabaseCreated: LiveData<Boolean>
        get() = mIsDatabaseCreated

    /**
     * Creates or returns a previously-created database.
     *
     *
     * Although this uses an AsyncTask which currently uses a serial executor, it's thread-safe.
     */
    fun createDb(context: Context) {
        Log.d("DatabaseCreator", "Creating DB from " + Thread.currentThread().name)

        if (!mInitializing.compareAndSet(true, false)) {
            return  // Already initializing
        }

        mIsDatabaseCreated.value = false// Trigger an update to show a loading screen.

        Flowable.just(context.applicationContext)
                .subscribeOn(Schedulers.io())
                .doOnNext { c -> c.deleteDatabase(DATABASE_NAME) }
                .map { Room.databaseBuilder(it.applicationContext, AppDatabase::class.java, DATABASE_NAME).build() }
                .doOnNext { addDelay() }
                .doOnNext { DatabaseInitUtil.initializeDb(it) }
                .doOnNext { database = it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { mIsDatabaseCreated.value = true }
    }

    private fun addDelay() {
        try {
            Thread.sleep(4000)
        } catch (ignored: InterruptedException) {
        }

    }
    companion object {

        private var sInstance: DatabaseCreator? = null

        // For Singleton instantiation
        private val LOCK = Any()

        @Synchronized
        fun getInstance(context: Context): DatabaseCreator {
            if (sInstance == null) {
                synchronized(LOCK) {
                    if (sInstance == null) {
                        sInstance = DatabaseCreator()
                    }
                }
            }
            return sInstance!!
        }
    }
}
