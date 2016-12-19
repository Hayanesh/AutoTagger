package com.hayanesh.autotagger;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rey.material.widget.ProgressView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.exception.ClarifaiException;

public class MainActivity extends AppCompatActivity {
    private Button choose,upload,left_button,right_button;
    private ImageView photo;
    private static int RESULT_LOAD_IMAGE = 222;
    //private final static int CAMERA_RQ = 6969;
    private FirebaseStorage storage;
    private RequestQueue requestqueue;
    private StringRequest stringrequest;
    private String URL = "https://api.clarifai.com/v1/tag/?url=";
    String selectedImage = null;
    Uri downloadUrl = null;
    public String[] tags = null;
    String imgDecodableString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        choose = (Button)findViewById(R.id.button);
        photo = (ImageView)findViewById(R.id.imageView);
        upload = (Button) findViewById(R.id.button2);
        selectedImage = getIntent().getStringExtra("image");
        //Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bottom_up);

       // int height = getResources().getDisplayMetrics().heightPixels*(2/5);
        //int width = getResources().getDisplayMetrics().widthPixels;
        //photo.setLayoutParams(new RelativeLayout.LayoutParams(width,height));
        Glide.with(this).load(new File(selectedImage)).centerCrop().priority(Priority.IMMEDIATE).into(photo);
        requestqueue = Volley.newRequestQueue(this);

        //set height and width to imageview


        left_button = (Button)findViewById(R.id.left_button);
        right_button = (Button)findViewById(R.id.right_button);

        left_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo.setRotation(photo.getRotation()-90f);
            }
        });
        right_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo.setRotation(photo.getRotation()+90f);
            }
        });
      /* choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,RESULT_LOAD_IMAGE);
            }
        });*/

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected())
                {
                    try{
                        //Toast.makeText(MainActivity.this, "Inside async", Toast.LENGTH_SHORT).show();
                        FirebaseStore();


                    }catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Internet connection required", Toast.LENGTH_SHORT).show();
                }
            }
        });


        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MainActivity.this, Display.class);
                //Bundle extra = new Bundle();
                System.out.println(tags);
//                i.putExtra("size",tags.length);
                i.putExtra("tags",tags);
                i.putExtra("bmp",selectedImage);
                //i.putExtras(extra);
                View sharedView = photo;
                String transitionName = getString(R.string.blue);

                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, sharedView, transitionName);
                startActivity(i, transitionActivityOptions.toBundle());
            }
        });
    }

  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null)
            {
                Uri selectedImage = data.getData();
                Toast.makeText(this, selectedImage.toString(), Toast.LENGTH_SHORT).show();
                String[] filepath = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,filepath,null,null,null);
                cursor.moveToFirst();

                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImage);
                //ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                //byte[] d = baos.toByteArray();
                //BitmapFactory.decodeByteArray(d,0,d.length);
                int columnIndex = cursor.getColumnIndex(filepath[0]);
                imgDecodableString = cursor.getString(columnIndex);
                Glide.with(this).load(new File(imgDecodableString)).into(photo);
                //photo.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
            }
            else
            {
                Toast.makeText(MainActivity.this, "You didn't pick an image", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e)
        {
            Toast.makeText(MainActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
        }
    }*/



    private void FirebaseStore()
    {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                ProgressView.MODE_DETERMINATE);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://autotagger-525fc.appspot.com/");
        StorageReference photoRef = storageRef.child("photos").child(Uri.parse(selectedImage).getLastPathSegment());
        //photo.setDrawingCacheEnabled(true);
        //photo.buildDrawingCache();
        //Bitmap bitmap = photo.getDrawingCache();
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        //byte[] data = baos.toByteArray();
       Log.d("TAG","Upload path"+photoRef.getPath());

        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(new File(selectedImage)));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                downloadUrl = taskSnapshot.getDownloadUrl();
                Log.i("URL",downloadUrl.toString());
                progressDialog.dismiss();
                new MyAsyncClass().execute();
                //Toast.makeText(MainActivity.this, downloadUrl.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }


    class MyAsyncClass extends AsyncTask<Void, Void, Void> {

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                ProgressView.MODE_DETERMINATE);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Processing Image..");
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... mApi) {
            try {
                final ClarifaiClient client =
                        new ClarifaiBuilder("vgS57uTjtR6HHFLydny3KQYWNlbkC9LBZ5ReHfyB", "PPo1UWXVVeRwlSxbyD2pkdZQEvD7oYdZUb51OYYN").buildSync();

                final List<ClarifaiOutput<Concept>> predictionResults =
                        client.getDefaultModels().generalModel() // You can also do client.getModelByID("id") to get custom models
                                .predict()
                                .withInputs(
                                        ClarifaiInput.forImage(ClarifaiImage.of(downloadUrl.toString()))
                                )
                                .executeSync()
                                .get();
                List<Concept> al = predictionResults.get(0).data();
                tags = new String[al.size()];
                for(int i=0;i<al.size();i++)
                {
                    tags[i] = al.get(i).name();
                    Log.d("TAGS",tags[i]);
                }

            }

            catch (Exception ex) {
                Log.e("Message Failed", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();


        }
    }



    private boolean isNetworkConnected(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
