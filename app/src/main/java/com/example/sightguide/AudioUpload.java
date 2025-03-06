package com.example.sightguide;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.chromium.base.Callback;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class AudioUpload extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_audio_upload);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

//    private String filePath;
//    File audioFile = new File(filePath);
//    RequestBody requestFile = RequestBody.create(MediaType.parse("audio/mpeg"), audioFile);
//    MultipartBody.Part body = MultipartBody.Part.createFormData("audio", audioFile.getName(), requestFile);
//
//    Call<ResponseBody> call = service.uploadAudio(body);
//    call.enqueue(new Callback<ResponseBody>() {
//    @Override
//    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
//    {
//        Log.d("Upload", "Success");
//    }
//
//    @Override
//    public void onFailure(Call<ResponseBody> call, Throwable t)
//    {
//        Log.e("Upload", "Error", t);
//    }
//};

    // Ensure filePath is initialized
//    private String filePath = "/storage/emulated/0/Download/audio.mp3";  // Update with actual path
//
//    File audioFile = new File(filePath);
//    if (!audioFile.exists())
//    {
//        Log.e("Upload", "File does not exist: " + filePath);
//        return;
//    }
//
//    // Determine the correct MIME type dynamically
//    String mimeType = URLConnection.guessContentTypeFromName(audioFile.getName());
//    if (mimeType == null) mimeType = "application/octet-stream";  // Fallback MIME type
//
//    // Create RequestBody
//    RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), audioFile);
//    MultipartBody.Part body = MultipartBody.Part.createFormData("audio", audioFile.getName(), requestFile);
//
//    // Retrofit call
//    Call<ResponseBody> call = service.uploadAudio(body);
//    call.enqueue(new Callback<ResponseBody>() {
//    @Override
//    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//        if (response.isSuccessful())
//        {
//            Log.d("Upload", "Success");
//        }
//        else
//        {
//            Log.e("Upload", "Server Error: " + response.code());
//        }
//    }
//
//    @Override
//    public void onFailure(Call<ResponseBody> call, Throwable t) {
//        Log.e("Upload", "Error", t);
//    }
//});


    public void goBack(View view)
    {
        finish();
    }
}