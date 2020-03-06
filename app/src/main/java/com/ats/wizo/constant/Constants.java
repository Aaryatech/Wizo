package com.ats.wizo.constant;

import com.ats.wizo.interfaces.MyInterface;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by maxadmin on 8/1/18.
 */

public class Constants {


    // Dev Id = 1 wizzo switch
    // Dev Id = 2 wizzo 4s
    // Dev Id = 3 wizzo 4 (switch) + 1 (regulator)
    // Dev Id = 4 wizzo 7s

    public static String serverUri = "tcp://wizzo.co.in:1883";
    // public static String serverUri = "http://192.168.2.10:8078";
    public static String clientID = "exampleAndroidfbdsd";

    public static String publishTopic = "//operations//dlZ3Ync9";
    public static String subscriptionTopic = "//op_status//dlZ3Ync9";
    public static String imagePath = "http://wizzo.co.in:8080/WizzoWebApi/uploads/";

    public static String authKey = "osi9bgevo2vkvg79c6dnl3tmo0";
    public static String onOperation = authKey + "piMjVtYV";
    public static String allOnOperation = authKey + "piMjVtYV#nolla";
    public static String offOperation = authKey + "JhTVo1V1";
    public static String intensityOperation = authKey + "lfcGkYEw";
    public static String allOffOperation = authKey + "JhTVo1V1#ffolla";

    public static String dimmer1Operation = authKey + "lfcGkYD1";
    public static String dimmer2Operation = authKey + "lfcGkYD2";

    public static int requestCount = 0;
    public static boolean isChannelReboot = false;

    public static int fanSpeed = 5;
    public static int dimmer1Speed = 100;
    public static int dimmer2Speed = 100;


    public static MqttAndroidClient mqttAndroidClient;

    public static  String serverURL="http://wizzo.co.in:8080/WizzoWebApi/";
   // public static String serverURL = "http://192.168.2.6:8078/";

    public static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .method(original.method(), original.body())
                            .build();

                    Response response = chain.proceed(request);

                    // Inresponse=response.body().string();
                    // Customize or return the response
                    return response;
                }
            })
            .readTimeout(10000, TimeUnit.SECONDS)
            .connectTimeout(10000, TimeUnit.SECONDS)
            .writeTimeout(10000, TimeUnit.SECONDS)
            .build();


    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(serverURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()).build();

    public static MyInterface myInterface = retrofit.create(MyInterface.class);


    public static String homeSSID;
    public static String userId;
}




/*
* Parameter :: --------- User Id : okhttp3.RequestBody$2@1347b43              MAC : okhttp3.RequestBody$2@9b915c0             TYPE : okhttp3.RequestBody$2@aeef2f9
2019-03-15 17:12:38.766 20663-20663/com.ats.wizo E/## Scheduler List:  is

     RespSchedulerData{schedulerList=[RespScheduler{schId=560, userId=131, devMac='18:FE:34:F2:8A:42', devType=1, operation=1, day=0, time='16:55:00', schStatus=1}], error=false, message='success'}
*
* */



/*
 *
 * E/Parameter :: --------- User Id : okhttp3.RequestBody$2@fd9d33a              MAC : okhttp3.RequestBody$2@6f6cbeb             TYPE : okhttp3.RequestBody$2@bf89d48
 * */
