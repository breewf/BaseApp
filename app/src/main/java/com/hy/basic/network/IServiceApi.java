package com.hy.basic.network;

import com.hy.myapp.bean.ReviewProductData;
import com.hy.myapp.bean.TestResultData;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author hy
 * @date 2021/7/12
 * desc: IServiceApi
 **/
public interface IServiceApi {

    String BASE_URL_REVIEW = NetConstants.BASE_URL_REVIEW;

    /**
     * sid=28654780
     */
    @GET("https://api.apiopen.top/getSingleJoke")
    Observable<HttpResponse<TestResultData>> getApiDataTest(
            @Query("sid") String sid);

    @FormUrlEncoded
    @POST("https://api.uomg.com/api/rand.qinghua?format=json")
    Observable<HttpResponse<String>> getApiDataTest2(
            @Field("id") String id);

    @FormUrlEncoded
    @POST(BASE_URL_REVIEW + "review/product")
    Observable<HttpResponse<ReviewProductData>> getReviewProductDetail(
            @Field("review_product_id") String reviewProductId);


    @POST("review/product")
    Observable<HttpResponse<ReviewProductData>> getReviewProductDetail2(@Body JSONObject parmas);

    /**
     * Json格式的Post请求（application/json）
     */
    @POST("account/update_user_info")
    Observable<HttpResponse<String>> updateUserInfo(@Body RequestBody body);

    /**
     * Form格式的Post请求（application/x-www-form-urlencoded）
     */
    @FormUrlEncoded
    @POST("account/update_user_info")
    Observable<HttpResponse<String>> updateUserInfo(@FieldMap Map<String, String> params);
}
