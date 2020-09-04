package com.example.recyclerviewwithpagination.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import com.example.recyclerviewwithpagination.R;
import com.example.recyclerviewwithpagination.adapter.PaginationAdapter;
import com.example.recyclerviewwithpagination.api.ApiRequestData;
import com.example.recyclerviewwithpagination.api.RetroServer;
import com.example.recyclerviewwithpagination.custom_views.CustomProgressDialog;
import com.example.recyclerviewwithpagination.db.AppDatabase;
import com.example.recyclerviewwithpagination.model.DataModel;
import com.example.recyclerviewwithpagination.model.EventModel;
import com.example.recyclerviewwithpagination.util.CleanableEditText;
import com.example.recyclerviewwithpagination.util.NetworkStatus;
import com.example.recyclerviewwithpagination.util.PaginationScrollListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private Activity activity = null;
    private static final String TAG = "MainActivity";
    private Toolbar toolbar;
    private CustomProgressDialog progress = null;
    private CleanableEditText search;
    RecyclerView recyclerView;
    PaginationAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES = 1;
    private int currentPage = PAGE_START;

    private ApiRequestData avtarService;
    private List<DataModel> results;

    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = MainActivity.this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        results = new ArrayList<>();
        database = AppDatabase.getDatabaseInstance(this);

        AppCompatTextView txtHeaderNname = (AppCompatTextView) toolbar.findViewById(R.id.actv_header_name);
        txtHeaderNname.setText(getString(R.string.app_name));
        search = (CleanableEditText) findViewById(R.id.search);
        recyclerView = findViewById(R.id.recycler_view);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        setRecyclerViewData();

        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {

                if (NetworkStatus.getConnectivityStatusString(activity)) {
                    isLoading = true;
                    currentPage += 1;

                    // mocking network delay for API call
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadNextPage();
                        }
                    }, 1000);
                }
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        //init service and load data
        avtarService = RetroServer.getRetrofitInstance().create(ApiRequestData.class);
        if (NetworkStatus.getConnectivityStatusString(activity)) {

            loadFirstPage();

        } else {
            setRecyclerViewData();
            Toast.makeText(activity, "OffLine", Toast.LENGTH_LONG).show();
        }

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence query, int start, int before, int count) {
                if (null != results) {
                    String text = search.getText().toString().toLowerCase(Locale.getDefault());
                    adapter.filters(text);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    //@Override
//    protected void onResume() {
//        super.onResume();
//        if (NetworkStatus.getConnectivityStatusString(activity)) {
//            loadFirstPage();
//        } else {
//            setRecyclerViewData();
//            Toast.makeText(activity, "OffLine", Toast.LENGTH_LONG).show();
//        }
//    }

//

    private void loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ");

        callTopRatedMoviesApi().enqueue(new Callback<EventModel>() {
            @Override
            public void onResponse(Call<EventModel> call, Response<EventModel> response) {
                // Got data. Send it to adapter
                TOTAL_PAGES = response.body().getTotalPages();
                results = fetchResults(response);
                adapter.addAll(results);
                database.dataDao().deleteAll();
                for (DataModel result : results) {
                    database.dataDao().insert(result);
                }
                setRecyclerViewData();

                if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<EventModel> call, Throwable t) {
                t.printStackTrace();


            }
        });

    }
    private List<DataModel> fetchResults(Response<EventModel> response) {
        EventModel eventModel = response.body();
        return eventModel.getData();
    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callTopRatedMoviesApi().enqueue(new Callback<EventModel>() {
            @Override
            public void onResponse(Call<EventModel> call, Response<EventModel> response) {
                adapter.removeLoadingFooter();
                isLoading = false;
                Log.i("RESPONSE", response.toString());
                results = fetchResults(response);
                adapter.addAll(results);
                for (DataModel result : results) {
                    database.dataDao().insert(result);
                }
                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<EventModel> call, Throwable t) {
                t.printStackTrace();


                Toast.makeText(MainActivity.this, "Sync Fail!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Performs a Retrofit call to the top rated User API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<EventModel> callTopRatedMoviesApi() {
        return avtarService.getPageResult(currentPage);
    }

    private void setRecyclerViewData() {
        results = database.dataDao().getAll();
        adapter = new PaginationAdapter(activity, results);
        recyclerView.setAdapter(adapter);
    }
}