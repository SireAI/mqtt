package com.sire.corelibrary.DataBindings;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.sire.corelibrary.DI.Environment.GlideConfigure;
import com.sire.corelibrary.R;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * ==================================================
 * All Right Reserved
 * Date:2017/11/07
 * Author:Sire
 * Description:数据绑定，设定imageUrl后自动加载图片
 * ==================================================
 */

public class CommonDatabindingAdapter {
    @BindingAdapter({"android:imageUrl", "android:cirlce"})
    public static void loadImage(ImageView view, String url, boolean circle) {
        if (circle) {
            Glide.with(view.getContext()).asBitmap()
                    .load(url)
                    .apply(GlideConfigure.getConfigure(DiskCacheStrategy.AUTOMATIC))
                    .into(new BitmapImageViewTarget(view) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            if (resource == null) {
                                resource = BitmapFactory.decodeResource(view.getContext().getResources(), R.drawable.logo_grey);
                            }
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(view.getResources(), resource);
                            roundedBitmapDrawable.setCircular(true);
                            view.setImageDrawable(roundedBitmapDrawable);

                        }
                    });
        } else {
            Glide.with(view.getContext())
                    .load(url)
                    .apply(GlideConfigure.getConfigure(DiskCacheStrategy.AUTOMATIC))
                    .transition(withCrossFade())
                    .into(view);
        }

    }

}
