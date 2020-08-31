package com.example.recyclerviewwithpagination.api;


import com.example.recyclerviewwithpagination.model.EventModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiRequestData {
    @GET("users")
    Call<EventModel> getPageResult(
            @Query("page") int pageIndex
    );
}
