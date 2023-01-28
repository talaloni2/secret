package com.example.secret.utls;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.secret.R;
import com.example.secret.model.ExternalBackgroundModel;
import com.example.secret.model.ImageModel;
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
                    ImageModel.instance().getImage(backgroundMeta.getUrls().getThumb(), bitmap -> {
                        targetView.setImageDrawable(new BitmapDrawable(activity.getResources(), bitmap));
                        progressBar.setVisibility(View.INVISIBLE);
                        imageSelectedSetter.accept(true);
                    }, this::onRandomBackgroundLoadFailed);
                },
                fail -> {
                    onRandomBackgroundLoadFailed("Try again later");
                }
        );
    }

    private void onRandomBackgroundLoadFailed(String reason) {
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(activity, reason, Toast.LENGTH_SHORT).show();
    }
}
