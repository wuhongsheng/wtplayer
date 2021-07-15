package com.wt.wtplayer.ui;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.wt.wtplayer.R;

/**
 * description
 *
 * @author whs
 * @date 2020/9/22
 */
public class WtPlayerBindingAdapter {

    @BindingAdapter("isGone")
    public static void setIsGone(View view, Boolean isGone) {
        if (isGone != null && isGone) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter("isVisiable")
    public static void setIsVisiable(View view, Boolean visiable) {
        if (visiable == null || visiable) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("isVideoRecording")
    public static void bindIsVideoRecording(TextView textView,Boolean isRecord){
        Drawable drawable = null;
        if(isRecord){
            drawable = ContextCompat.getDrawable(textView.getContext(), R.drawable.video_recording);
        }else {
            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_lz_nor);
        }

        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, drawable, null, null);
        if(isRecord){
            AnimationDrawable frameAnimation = (AnimationDrawable) textView.getCompoundDrawables()[1];
            frameAnimation.start();
        }
    }

    @BindingAdapter("isFavorite")
    public static void bindIsFavorite(TextView textView,Boolean isFavorite){
        Drawable drawable = null;
        if(isFavorite){
            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sc_sec);
        }else {
            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sc_nor);
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, drawable, null, null);
    }

    @BindingAdapter("isControl")
    public static void bindIsControl(TextView textView,Boolean isControl){
        Drawable drawable = null;
        if(isControl){
            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_kz_sec);
        }else {
            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_kz_nor);
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, drawable, null, null);
    }



}
