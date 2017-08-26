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

package com.example.android.persistence


import android.arch.lifecycle.ViewModelProviders
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingResource
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.android.persistence.viewmodel.ProductListViewModel
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class MainActivityTest {

    @Rule
    var mActivityRule = ActivityTestRule(
            MainActivity::class.java)

    private val idlingRes = SimpleIdlingResource()

    @Before
    fun idlingResourceSetup() {

        Espresso.registerIdlingResources(idlingRes)
        // There's always
        idlingRes.isIdleNow = false

        val productListViewModel = productListViewModel

        // Subscribe to ProductListViewModel's products list observable to figure out when the
        // app is idle.
        productListViewModel.products.observeForever { productEntities ->
            if (productEntities != null) {
                idlingRes.isIdleNow = true
            }
        }
    }

    @Test
    fun clickOnFirstItem_opensComments() {
        // When clicking on the first product
        onView(withContentDescription(R.string.cd_products_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Then the second screen with the comments should appear.
        onView(withContentDescription(R.string.cd_comments_list))
                .check(matches(isDisplayed()))

        // Then the second screen with the comments should appear.
        onView(withContentDescription(R.string.cd_product_name))
                .check(matches(not<View>(withText(""))))

    }

    /** Gets the ViewModel for the current fragment  */
    private val productListViewModel: ProductListViewModel
        get() {
            val activity = mActivityRule.activity

            val productListFragment = activity.supportFragmentManager
                    .findFragmentByTag(ProductListFragment.TAG)

            return ViewModelProviders.of(productListFragment)
                    .get(ProductListViewModel::class.java)
        }

    private class SimpleIdlingResource : IdlingResource {

        // written from main thread, read from any thread.
        @Volatile private var mResourceCallback: IdlingResource.ResourceCallback? = null

        private val mIsIdleNow = AtomicBoolean(true)

        fun setIdleNow(idleNow: Boolean) {
            mIsIdleNow.set(idleNow)
            if (idleNow) {
                mResourceCallback!!.onTransitionToIdle()
            }
        }

        override fun getName(): String {
            return "Simple idling resource"
        }

        override fun isIdleNow(): Boolean {
            return mIsIdleNow.get()
        }

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
            mResourceCallback = callback
        }
    }
}