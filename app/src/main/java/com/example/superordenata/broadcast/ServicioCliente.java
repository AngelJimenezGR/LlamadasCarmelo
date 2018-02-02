package com.example.superordenata.broadcast;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServicioCliente extends Service {

    public ServicioCliente() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String state = intent.getStringExtra("state");
        String phone = intent.getStringExtra("phoneNumber");
        String fecha = intent.getStringExtra("date");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);

        LLamadas nuevo = new LLamadas(0, state, phone, fecha);

        postMethod(service, nuevo);

        return START_REDELIVER_INTENT;
    }


    private void postMethod(APIService service, LLamadas nuevo) {
        Call<LLamadas> registroCall = service.postRegistro(nuevo);

        registroCall.enqueue(new Callback<LLamadas>() {
            @Override
            public void onResponse(Call<LLamadas> call, Response<LLamadas> response) {
                Log.d("Prueba", "WIN");
            }

            @Override
            public void onFailure(Call<LLamadas> call, Throwable t) {
                Log.d("Prueba", "FAIL: " + t.getMessage());
            }
        });

    }
}
