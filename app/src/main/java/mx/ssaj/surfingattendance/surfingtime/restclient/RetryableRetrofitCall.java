package mx.ssaj.surfingattendance.surfingtime.restclient;

import java.io.IOException;

import retrofit2.Call;

@FunctionalInterface
public interface RetryableRetrofitCall<T> {
    Call<T> request(String authHeader) throws IOException;
}
