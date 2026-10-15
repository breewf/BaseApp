package com.hy.baseapp.base.net;

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
     * <a href="https://api.apiopen.top/docs">...</a>
     * <p>
     * {
     * "code": 200,
     * "data": {
     * "from": "朱敦儒《鹧鸪天·西都作》",
     * "name": "玉楼金阙慵归去，且插梅花醉洛阳。"
     * },
     * "message": "成功"
     * }
     * <p>
     * 返回：{"code":200,"message":"成功","data":{"name":"万户千门成野草，只缘一曲后庭花。","from":"刘禹锡《台城》"}}
     */
    @GET("https://api.apiopen.top/api/tools/famous-sentence")
    Observable<HttpResponse<TestResultData>> getApiDataTest();

    @FormUrlEncoded
    @POST(BASE_URL_REVIEW + "review/product")
    Observable<HttpResponse<ReviewProductData>> getReviewProductDetail(
            @Field("review_product_id") String reviewProductId);

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
