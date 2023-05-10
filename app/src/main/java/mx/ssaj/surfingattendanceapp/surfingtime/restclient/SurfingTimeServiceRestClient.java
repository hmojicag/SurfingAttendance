package mx.ssaj.surfingattendanceapp.surfingtime.restclient;

import java.util.List;

import mx.ssaj.surfingattendanceapp.surfingtime.dto.ApiAttLog;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.ApiBioPhoto;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.ApiCommand;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.ApiCommandUpdate;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.ApiInfoRequest;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.ApiInfoResponse;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.ApiUser;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.TokenRequest;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.TokenResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SurfingTimeServiceRestClient {

    @POST("api/auth")
    Call<TokenResponse> requestToken(@Body TokenRequest tokenRequest);

    @POST("api/attdevice/info")
    Call<ApiInfoResponse> info(@Header("Authorization") String authorization, @Body ApiInfoRequest apiInfoRequest);

    @GET("api/attdevice/commands")
    Call<List<ApiCommand>> getCommands(@Header("Authorization") String authorization);

    @PUT("api/attdevice/commands")
    Call<Void> updateCommands(@Header("Authorization") String authorization, @Body List<ApiCommandUpdate> updates);

    @PUT("api/attlogs")
    Call<Void> upsertAttLogs(@Header("Authorization") String authorization, @Body List<ApiAttLog> apiAttLogs);

    @GET("api/user/{id}")
    Call<ApiUser> getUserById(@Header("Authorization") String authorization, @Path("id") int id);

    @PUT("api/user/{id}")
    Call<Void> upsertUser(@Header("Authorization") String authorization, @Path("id") int id, @Body ApiUser user);

    @GET("api/user/{id}/biophoto")
    Call<ApiBioPhoto> getBioPhotoForUser(@Header("Authorization") String authorization, @Path("id") int id);

    @PUT("api/user/{id}/biophoto")
    Call<Void> upsertBioPhotoForUser(@Header("Authorization") String authorization, @Path("id") int id, @Body ApiBioPhoto apiBioPhoto);
}
