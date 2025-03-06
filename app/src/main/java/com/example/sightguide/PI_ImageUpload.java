package com.example.sightguide;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import java.io.InputStream;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class PI_ImageUpload extends AppCompatActivity
{

    private ImageView imageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pi_image_upload);
        imageView = findViewById(R.id.imageView);
        fetchImage();
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.43.100:5000/")
            .build(); // No Gson needed, as we're dealing with raw bytes

    public interface ImageService
    {
        @GET("get_image")
        Call<ResponseBody> getImage();
    }

    private void fetchImage()
    {
        ImageService service = retrofit.create(ImageService.class);
        Call<ResponseBody> call = service.getImage();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bitmap);
                }
                else
                {
                    Log.e("Retrofit", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {
                Log.e("Retrofit", "Request failed: " + t.getMessage());
            }
        });
    }

//    Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl("http://192.168.43.100:5000/")  // Use the Pi's IP address
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
//
//    public interface ImageService
//    {
//        @GET("get_image")
//        Call<ResponseBody> getImage();
//    }

    public void goBack(View view)
    {
        finish();
    }
}