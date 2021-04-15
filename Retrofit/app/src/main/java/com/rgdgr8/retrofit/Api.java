package com.rgdgr8.retrofit;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface Api {

    @GET("jokes/{count}")
    Call<List<Jokes>> getComments(@Path("count") int postId);

    //@GET("/jokes/type/{type}/{count}")
   // Call<List<Jokes>> getComments(@Path("type") String type, @Path("count") int id);

    @POST("jokes/create")
    Call<Jokes> createPost(@Body Jokes comments);

    @FormUrlEncoded
    @POST("jokes")
    Call<Comments> createPost(@FieldMap Map<String,String> map);

    @PUT("jokes/{:id}")
    Call<Comments> putPost(@Path(":id") int ID, @Body Jokes comments);

    @PATCH("jokes/{:id}")
    Call<Comments> patchPost(@Path(":id") int ID, @Body Jokes comments);

    /*@GET
    Call<List<Comments>> getComments(@Url String url);

    @GET("posts")
    Call<List<Comments>> getComments(
            @Query("userId") Integer postId,
            @Query("_sort") String sort,
            @Query("_order") String order
    );

    @POST("posts")
    Call<Comments> createPost(@Body Comments comments);

    @FormUrlEncoded
    @POST("posts")
    Call<Comments> createPost(@FieldMap Map<String,String> map);

    @PUT("posts/{id}")
    Call<Comments> putPost(@Path("id") int ID, @Body Comments comments);

    @PATCH("posts/{id}")
    Call<Comments> patchPost(@Path("id") int ID, @Body Comments comments);

    @DELETE("posts/{id}")
    Call<Void> deletePost(@Path("id") int ID);

     */
}
