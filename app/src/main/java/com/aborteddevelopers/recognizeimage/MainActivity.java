package com.aborteddevelopers.recognizeimage;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Contract.Caption;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceClient;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;

public class MainActivity extends AppCompatActivity {
    private static int RESULT_LOAD_IMAGE = 1;
    ImageView imageView;
    Button processbtn;
    TextView textResult;
    private final String API_KEY = "2c9d649b94f1485faa6d20419a9d2e0f";
    private final String API_LINK = "https://eastasia.api.cognitive.microsoft.com/vision/v1.0";
    public  Bitmap p;


    VisionServiceClient visionServiceClient = new VisionServiceRestClient(API_KEY,API_LINK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);

        imageView = (ImageView)findViewById(R.id.imgView);
        processbtn = (Button)findViewById(R.id.buttonreco);
        textResult = (TextView)findViewById(R.id.txt_result);
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.testi);


        processbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                p.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                //use async task

                AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.show();
                    }

                    @Override
                    protected String doInBackground(InputStream... inputStreams) {

                       try {
                           publishProgress("Recognizing....");
                           String[] features = {"Description"};//get description the api returns
                           String[] details = {};

                           AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0],features,details);
                           String jsonResult = new Gson().toJson(result);
                           return jsonResult;
                       } catch (IOException e) {
                           e.printStackTrace();
                       } catch (VisionServiceException e) {
                           e.printStackTrace();
                       }
                       return "";
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (TextUtils.isEmpty(s)){
                            Toast.makeText(MainActivity.this, "Api return null", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();

                        AnalysisResult result = new Gson().fromJson(s,AnalysisResult.class);
                        StringBuilder result_Text = new StringBuilder();
                        for (Caption caption:result.description.captions)
                            result_Text.append(caption.text);
                        textResult.setText(result_Text.toString());
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        progressDialog.setMessage(values[0]);
                    }
                };


                visionTask.execute(inputStream);


            }
        });



        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageView = (ImageView) findViewById(R.id.imgView);
             p = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
}