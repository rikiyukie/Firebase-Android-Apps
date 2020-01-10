package com.smart.reyog.Retrofit;

import com.smart.reyog.Model.Respon;
import com.smart.reyog.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA6xcRr88:APA91bHxfNEHegFYbSKUfd0Rtm-wPi-3t5_YiHfIpEDeXnrQ9u2u63xj67okkuzG6Dl1p0R_SeJ-4xZzKAJdytUGPzsK1XwlfVccvdm77j4GAfJGxUJS9uDyMQ61iJcfTtINkQmYOD5W"
    })

    @POST("fcm/send")
    Call<Respon> sendNotification(@Body Sender body);
}
