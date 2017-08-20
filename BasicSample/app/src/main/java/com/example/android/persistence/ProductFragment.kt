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

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.persistence.databinding.ProductFragmentBinding
import com.example.android.persistence.ui.CommentAdapter
import com.example.android.persistence.ui.CommentClickCallback
import com.example.android.persistence.viewmodel.ProductViewModel

class ProductFragment : LifecycleFragment() {

    private lateinit var mBinding: ProductFragmentBinding

    private val mCommentAdapter by lazy { CommentAdapter(mCommentClickCallback) }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater!!, R.layout.product_fragment, container, false)

        // Create and set the adapter for the RecyclerView.
        mBinding.commentList.adapter = mCommentAdapter
        return mBinding.root
    }

    private val mCommentClickCallback = CommentClickCallback {
        // no-op
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = ProductViewModel.Factory(
                activity.application, arguments.getInt(KEY_PRODUCT_ID))

        ViewModelProviders.of(this, factory).get(ProductViewModel::class.java).let {
            mBinding.productViewModel = it

            subscribeToModel(it)
        }
    }

    private fun subscribeToModel(model: ProductViewModel) {

        // Observe product data
        model.observableProduct.observe(this, Observer { productEntity -> model.setProduct(productEntity) })

        // Observe comments
        model.comments.observe(this, Observer { commentEntities ->
            commentEntities?.let {
                mBinding.isLoading = false
                mCommentAdapter.setCommentList(it)
            } ?: let { mBinding.isLoading = true }
        })
    }

    companion object {

        private val KEY_PRODUCT_ID = "product_id"

        /** Creates product fragment for specific product ID  */
        fun forProduct(productId: Int): ProductFragment =
                ProductFragment().apply {
                    arguments = Bundle().apply {
                        putInt(KEY_PRODUCT_ID, productId)
                    }
                }
    }
}
