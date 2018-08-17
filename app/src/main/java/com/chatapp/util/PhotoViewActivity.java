package com.chatapp.util;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chatapp.R;
import com.github.chrisbanes.photoview.PhotoView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoViewActivity extends AppCompatActivity {

    @BindView(R.id.photo_view)
    PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        ButterKnife.bind(this);


        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }

        String url = extras.getString("url");
        Glide.with(getApplicationContext()).load(url).apply(new RequestOptions().placeholder(R.drawable.ic_gallery).error(R.drawable.ic_gallery).centerCrop().dontAnimate()).into(photoView);

    }
}
