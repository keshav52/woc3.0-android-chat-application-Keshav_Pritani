package com.example.chatapplication.Notifications;

import com.example.chatapplication.Notifications.MyResponse;
import com.example.chatapplication.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA40-wptQ:APA91bEmfPmXOzSglLjWJ43GjqXsqXY0Ya2QI0xQZZiiirA9fj_C9Rk2hYt_fdBy0fG-5IgiZp9Y97dZNtqdSCDnnkt9w9Rb-nOfz59EWaFuQ6ERrzKjEOfriYbT3o4Gf4537tfYEoTB"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
