package com.example.sightguide;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import okhttp3.OkHttpClient;
import java.io.IOException;

public class PI_ImageUpload extends AppCompatActivity
{
    private Bitmap selectedImage;
    private ImageView imageView;
    private Button selectImage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pi_image_upload);
        imageView = findViewById(R.id.imageView);
        selectImage = findViewById(R.id.selectImageButton2);
        fetchImage();

        selectImage.setOnClickListener(v -> pickImage.launch("image/*"));
    }

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImageSelected);

    private void onImageSelected(Uri imageUri)
    {
        if (imageUri != null)
        {
            try
            {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(selectedImage);
                //uploadImage(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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