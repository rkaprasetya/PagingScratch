package com.example.paginationscratchapp.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paginationscratchapp.R
import com.example.paginationscratchapp.ui.HomeViewModel.State
import com.example.paginationscratchapp.data.api.CommunityService
import com.example.paginationscratchapp.util.CustomOnScrollListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import android.util.Log

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mAdapter: PaginationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private var isLoading = false
    private var isLastPage = false
    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var communityService: CommunityService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initObservable()
    }
    private fun initView() {
        mAdapter = PaginationAdapter()
        recyclerView = findViewById(R.id.rv_list)
        progressBar = findViewById(R.id.pb_main)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = mAdapter
            addOnScrollListener(object : CustomOnScrollListener(linearLayoutManager) {
                override fun isLoading(): Boolean {
                    return isLoading
                }

                override fun isLastPage(): Boolean {
                    return isLastPage
                }

                override fun loadMoreItems() {
                    viewModel.loadNextPage()
                }
            })
        }
    }

    private fun initObservable() {
        viewModel.communityObservable.observe(this) {
            when (it) {
                is State.SUCCESS -> mAdapter.setData(it.data)

                is State.ERROR -> {
                    Timber.e("Error Message = ${it.error.message}")
                    isLoading = false
                }
                is State.LOADING -> {
                    isLoading = it.isLoading
                    if (it.showLoadingMain){
                        progressBar.visibility = View.VISIBLE
                    }else{
                        progressBar.visibility = View.GONE
                    }
                    it.showLoadingFooter?.let {
                        if (it) {
                            mAdapter.addLoadingFooter()
                        } else {
                            mAdapter.removeLoadingFooter()
                        }
                    }
                }
                is State.LASTPAGE -> {
                    isLastPage = it.isLastPage
                    progressBar.visibility = View.GONE
                    mAdapter.notifyDataSet()
                }
            }
        }
        viewModel.isLastPage.observe(this) {
            isLastPage = it
        }
        viewModel.loadFirstPage()
    }
}