package com.hayanesh.autotagger;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;

import me.gujun.android.taggroup.TagGroup;

public class Display extends AppCompatActivity {
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_display);

        image = (ImageView)findViewById(R.id.imageView2);
        //byte[] bytes = this.getIntent().getByteArrayExtra("BMP");
        String imgdecodableString = this.getIntent().getStringExtra("bmp");
        int s = this.getIntent().getIntExtra("size",0);
        //String[] tags = new String[s];
        String[] tags = {"sample","sample"};
        try {
            tags = this.getIntent().getStringArrayExtra("tags").clone();
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        //Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        //Bundle extras = getIntent().getExtras();
       // Bitmap bmp = (Bitmap) extras.getParcelable("imagebitmap");
       // Bitmap bitmap = BitmapFactory.decodeFile(imgdecodableString);
       // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        //byte[] b = baos.toByteArray();
        //image.setImageBitmap(BitmapFactory.decodeByteArray(b,0,b.length));
        Glide.with(this).load(new File(imgdecodableString)).into(image);
        TagGroup mTagGroup = (TagGroup) findViewById(R.id.tag_group);
        mTagGroup.setTags(tags);

    }
}
