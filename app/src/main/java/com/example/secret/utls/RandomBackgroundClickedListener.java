package com.example.secret.utls;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.secret.R;
import com.example.secret.model.ExternalBackgroundModel;
import com.squareup.picasso.Picasso;

import java.util.function.Consumer;

public class RandomBackgroundClickedListener implements View.OnClickListener {

    Activity activity;
    ProgressBar progressBar;
    ImageView targetView;
    Consumer<Boolean> imageSelectedSetter;

    public RandomBackgroundClickedListener(Activity activity, ProgressBar progressBar, ImageView targetView, Consumer<Boolean> imageSelectedSetter) {
        this.activity = activity;
        this.progressBar = progressBar;
        this.targetView = targetView;
        this.imageSelectedSetter = imageSelectedSetter;
    }

    @Override
    public void onClick(View view) {
        progressBar.setVisibility(View.VISIBLE);
        ExternalBackgroundModel.getInstance().getRandomBackground(
                backgroundMeta -> {
                    Picasso.get().load(backgroundMeta.getUrls().getThumb()).placeholder(R.drawable.sharing_secret_image).into(targetView);
                    imageSelectedSetter.accept(true);
                    progressBar.setVisibility(View.INVISIBLE);
                },
                fail -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(activity, "Try again later", Toast.LENGTH_SHORT).show();
                }
        );
    }
}
