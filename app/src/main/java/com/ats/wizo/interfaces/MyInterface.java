package com.ats.wizo.interfaces;

import com.ats.wizo.model.DataUploadDevices;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.ErrorMessage;
import com.ats.wizo.model.Order;
import com.ats.wizo.model.PostNewMood;
import com.ats.wizo.model.RespAddNewMood;
import com.ats.wizo.model.RespDeviceData;
import com.ats.wizo.model.RespMoodList;
import com.ats.wizo.model.RespMoodScheduleData;
import com.ats.wizo.model.RespMoodScheduler;
import com.ats.wizo.model.RespRoomData;
import com.ats.wizo.model.RespScanData;
import com.ats.wizo.model.RespScheduler;
import com.ats.wizo.model.RespSchedulerData;
import com.ats.wizo.model.Room;
import com.ats.wizo.model.ScanDevice;
import com.ats.wizo.model.User;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by maxadmin on 9/1/18.
 */

public interface MyInterface {

    // ESP

    @FormUrlEncoded
    @POST("/channel")
    Call<JsonObject> sendChannel(@Field("c") String c);

    @FormUrlEncoded
    @POST("/C")
    Call<JsonObject> getConfig(@Field("s") String s, @Field("p") String p, @Field("k") String k);

    @FormUrlEncoded
    @POST("/process")
    Call<JsonObject> process(@Field("d") String d, @Field("o") String o, @Field("a") String a);

    @GET("/on")
    Call<JsonObject> deviceON();


    @FormUrlEncoded
    @POST("/process")
    rx.Observable<JsonObject> fanOperation(@Field("d") String d, @Field("o") String o, @Field("a") String a);


    // VPS
    // URL http://wizzo.co.in:8080/WizzoWebApi/

    @POST("register")
    Call<JsonObject> userRegister(@Body User user);

    @POST("addNewScanDevice")
    Call<JsonObject> addNewScanDevice(@Body ScanDevice scanDevice);

    @POST("updateUserProfile")
    Call<JsonObject> updateUserProfile(@Body User user);

    @Multipart
    @POST("getScanDevices")
    Call<RespScanData> getScanDevices(@Part("userId") RequestBody userId);

    @Multipart
    @POST("login")
    Call<JsonObject> login(@Part("userMob") RequestBody user_mobile, @Part("userOtp") RequestBody otp);


    @Multipart
    @POST("demoUserLogin")
    Call<JsonObject> demoLogin(@Part("userMob") RequestBody userMob, @Part("userPassword") RequestBody userPassword, @Part("userName") RequestBody userName);


    @Multipart
    @POST("getRoomListByUserId")
    Call<RespRoomData> getRoomsDataByUserId(@Part("userId") RequestBody userId);

    @Multipart
    @POST("getDeviceDataByUserId")
    Call<RespDeviceData> getDeviceDataByUserId(@Part("userId") RequestBody userId);

    @Multipart
    @POST("getSchedulerList")
    Call<RespSchedulerData> getSchedulerList(@Part("userId") RequestBody userId, @Part("mac") RequestBody mac, @Part("type") RequestBody type);

    @POST("addNewScheduler")
    Call<RespSchedulerData> addNewScheduler(@Body RespScheduler respScheduler);

    @POST("deleteScheduler")
    Call<JsonObject> deleteScheduler(@Body RespScheduler respScheduler);

    @POST("updateScheduler")
    Call<JsonObject> updateScheduler(@Body RespScheduler respScheduler);

    @POST("uploadDeviceData")
    Call<JsonObject> uploadDeviceData(@Body List<DataUploadDevices> devicesList);

    @Multipart
    @POST("fileUpload")
    Call<JsonObject> uploadFile(@Part MultipartBody.Part image, @Part("imageName") RequestBody name);

    @POST("addNewRoom")
    Call<JsonObject> addNewRoom(@Body Room room);

    @Multipart
    @POST("deleteRoom")
    Call<JsonObject> deleteRoom(@Part("id") RequestBody id);


    @POST("newOrder")
    Call<JsonObject> newOrder(@Body Order order);


    @Multipart
    @POST("getUserDetails")
    Call<JsonObject> getUserDetails(@Part("userId") RequestBody userId);


    // add new mood
    @POST("addNewMood")
    Call<RespAddNewMood> addNewMood(@Body PostNewMood postNewMood);

    //update mood status

    @Multipart
    @POST("updateMoodStatus")
    Call<ErrorMessage> updateMoodStatus(@Part("userId") RequestBody userId, @Part("moodId") RequestBody moodId, @Part("moodStatus") RequestBody moodStatus);

    //Update mood operation
    @Multipart
    @POST("updateMoodOperation")
    Call<ErrorMessage> updateMoodOperation(@Part("moodDetailId") RequestBody moodDetailId, @Part("operation") RequestBody operation);

    @POST("getMoodDeviceListByMoodId")
    Call<ArrayList<Device>> getMoodDeviceListByMoodId(@Query("moodId") int moodId);


    // getMoodStatus

    @Multipart
    @POST("getMoodStatus")
    Call<JsonObject> getMoodStatus(@Part("userId") RequestBody userId, @Part("moodId") RequestBody moodId);


    // Delete Device From Mood
    @Multipart
    @POST("deleteDeviceFromMood")
    Call<JsonObject> deleteDeviceFromMood(@Part("userId") RequestBody userId, @Part("moodId") RequestBody moodId, @Part("devMac") RequestBody devMac, @Part("devType") RequestBody devType);


    // Add New Device to Mood
    @POST("addNewDeviceToMood")
    Call<RespAddNewMood> addNewDeviceToMood(@Body PostNewMood postNewMood);


    // Update Mood Caption
    @Multipart
    @POST("updateMoodCaption")
    Call<JsonObject> updateMoodCaption(@Part("userId") RequestBody userId, @Part("moodId") RequestBody moodId, @Part("moodName") RequestBody moodName);

    // Delete Mood
    @Multipart
    @POST("deleteMood")
    Call<JsonObject> deleteMood(@Part("userId") RequestBody userId, @Part("moodId") RequestBody moodId);


    // get all moods

    @Multipart
    @POST("getMoodsByUserId")
    Call<RespMoodList> getMoodsByUserId(@Part("userId") RequestBody userId);


    //get all mood schedulers
    @Multipart
    @POST("getMoodScheduler")
    Call<RespMoodScheduleData> getMoodSchedulerList(@Part("userId") RequestBody userId, @Part("moodId") RequestBody moodId);


    // get schedulers


    // add new mood scheduler
    @POST("addNewMoodScheduler")
    Call<RespMoodScheduleData> addNewMoodScheduler(@Body RespMoodScheduler respMoodScheduler);


    // update mood scheduler


    // Delete Scheduler
    @POST("deleteMoodScheduler")
    Call<JsonObject> deleteMoodScheduler(@Body RespMoodScheduler respScheduler);


    // Synch


    // End Of VPS API


    @GET("/off")
    Call<JsonObject> deviceOFF();


    @GET("/wizzoReset")
    Call<JsonObject> resetDevice();

    @GET("/WizolightReboot")
    Call<JsonObject> softReset();

    @GET("/getDeviceDat")
    Call<JsonObject> getStatus(@Query("") String s);

    @GET("/getDeviceDat")
    rx.Observable<JsonObject> getSynch(@Query("") String s);


    @Multipart
    @POST("sendhttp.php")
    Call<JsonObject> sendOtp(@Part("authkey") RequestBody authkey, @Part("mobiles") RequestBody mobiles, @Part("message") RequestBody message, @Part("sender") RequestBody sender, @Part("route") RequestBody route);


    // web server
    @Multipart
    @POST("ser_authen/get_login")
    Call<JsonObject> userLogin(@Part("frm_mode") RequestBody frm_mode, @Part("user_mobile") RequestBody user_mobile, @Part("user_pwd") RequestBody user_pwd);

    @Multipart
    @POST("ser_authen/forgot_pwd")
    Call<JsonObject> ForgotPassword(@Part("frm_mode") RequestBody frm_mode, @Part("user_mobile") RequestBody user_mobile);


    @Multipart
    @POST("ser_authen/set_password")
    Call<JsonObject> changePassword(@Part("frm_mode") RequestBody frm_mode, @Part("user_id") RequestBody user_id, @Part("curr_pwd") RequestBody curr_pwd, @Part("new_pwd") RequestBody new_pwd);

    @Multipart
    @POST("ser_device/add_device")
    Call<JsonObject> deviceRegister(@Part("frm_mode") RequestBody frm_mode, @Part("user_id") RequestBody user_id, @Part("dev_mac") RequestBody dev_mac, @Part("ssid") RequestBody ssid, @Part("rou_pwd") RequestBody rou_pwd, @Part("dev_ip") RequestBody dev_ip, @Part("group_id") RequestBody group_id, @Part("type") RequestBody type, @Part("curr_status") RequestBody curr_status, @Part("dev_is_used") RequestBody dev_is_used, @Part("dev_caption") RequestBody dev_caption);

    @Multipart
    @POST("ser_device/add_group")
    Call<JsonObject> groupRegister(@Part("frm_mode") RequestBody frm_mode, @Part("user_id") RequestBody user_id, @Part("gr_id") RequestBody gr_id, @Part("gr_name") RequestBody gr_name, @Part("gr_device_list") RequestBody gr_device_list, @Part("gr_is_used") RequestBody gr_is_used);

    @Multipart
    @POST("ser_device/update_group")
    Call<JsonObject> updateGroup(@Part("frm_mode") RequestBody frm_mode, @Part("gr_id") RequestBody user_id, @Part("user_id") RequestBody gr_id, @Part("gr_name") RequestBody gr_name, @Part("gr_device_list") RequestBody gr_device_list, @Part("gr_is_used") RequestBody gr_is_used);


    @Multipart
    @POST("ser_sche/add_sche")
    Call<JsonObject> addscheduler(@Part("frm_mode") RequestBody frm_mode, @Part("user_id") RequestBody user_id, @Part("dev_id") RequestBody dev_id, @Part("type") RequestBody type, @Part("req_operation") RequestBody req_operation, @Part("day") RequestBody day, @Part("timestamp") RequestBody timestamp, @Part("sch_status") RequestBody sch_status);

    @Multipart
    @POST("ser_sche/list_sche")
    Call<JsonObject> schedulerList(@Part("frm_mode") RequestBody frm_mode, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("ser_sche/edit_sche")
    Call<JsonObject> EditScheduler(@Part("frm_mode") RequestBody frm_mode, @Part("sch_id") RequestBody sch_id, @Part("user_id") RequestBody user_id, @Part("dev_id") RequestBody dev_id, @Part("req_operation") RequestBody req_operation, @Part("day") RequestBody day, @Part("timestamp") RequestBody timestamp, @Part("sch_status") RequestBody sch_status);

    @Multipart
    @POST("ser_sche/delete_sche")
    Call<JsonObject> deleteScheduler(@Part("frm_mode") RequestBody frm_mode, @Part("user_id") RequestBody user_id, @Part("sch_id") RequestBody sch_id);

    @Multipart
    @POST("ser_device/pull_record")
    Call<JsonObject> deviceStatus(@Part("frm_mode") RequestBody frm_mode, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("ser_device/delete_dev")
    Call<JsonObject> deleteDevData(@Part("frm_mode") RequestBody frm_mode, @Part("user_id") RequestBody user_id);

    @POST("ser_device/push_record")
    Call<JsonObject> pushData(@Body String json);

    @Multipart
    @POST("user/deviceOnOff")
    Call<JsonObject> deviceOnOff(@Part("user_id") RequestBody user_id, @Part("dev_id") RequestBody dev_id, @Part("req_operation") RequestBody req_operation);

    @Multipart
    @POST("user/addgroup")
    Call<JsonObject> addGroup(@Part("user_id") RequestBody user_id, @Part("gr_name") RequestBody gr_name);

    @Multipart
    @POST("user/addGrpDevice")
    Call<JsonObject> addDeviceToGroup(@Part("user_id") RequestBody user_id, @Part("gr_id") RequestBody gr_id, @Part("dev_id") RequestBody dev_id);

    @Multipart
    @POST("user/grouplist")
    Call<JsonObject> groupList(@Part("user_id") RequestBody user_id);

    @Multipart
    @POST("user/delgroup")
    Call<JsonObject> deleteGroup(@Part("user_id") RequestBody user_id, @Part("gr_id") RequestBody gr_id);


}

