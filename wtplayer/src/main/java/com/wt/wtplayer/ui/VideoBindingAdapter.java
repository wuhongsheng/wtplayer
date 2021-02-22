package com.wt.wtplayer.ui;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.wt.wtplayer.R;
import com.wt.wtplayer.model.VideoNode;

/**
 * description
 *
 * @author whs
 * @date 2020/9/22
 */
public class VideoBindingAdapter {
    @BindingAdapter("videoItemType")
    public static void bindVideoItemType(TextView textView, VideoNode videoNode){
        if(videoNode.getBaseDeviceInfo() != null){
            //收藏列表
            //1 球机   2 枪机  3 半球
            Drawable drawable = null;
            if(videoNode.getVideoType() != null){
                switch (videoNode.getVideoType()) {
                    case 1:
                        if (videoNode.getBaseDeviceInfo().getDeviceState()  == 1) {
                            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj3_zx);
                        } else {
                            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj3_lx);
                        }
                        break;
                    case 2:
                        if (videoNode.getBaseDeviceInfo().getDeviceState()  == 1) {
                            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj1_zx);
                        } else {
                            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj1_lx);
                        }
                        break;
                    case 3:
                        if (videoNode.getBaseDeviceInfo().getDeviceState()  == 1) {
                            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj2_zx);
                        } else {
                            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj2_lx);
                        }
                        break;
                    default:
                        break;
                }

                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                textView.setCompoundDrawables(drawable,null,null,null);
            }


            if(videoNode.getBaseDeviceInfo().getDeviceState() == 1){
                //在线
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.white));
            }else {
                //离线
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.gray));
            }
            textView.setText(videoNode.getBaseDeviceInfo().getName());
        }else {
            //设备列表
            if(videoNode.getType() == 1){
                //地点
                textView.setCompoundDrawables(null,null,null,null);
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.white));
            }else {
                if(videoNode.isCheck()){
                    textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.colorAccent));
                }
                textView.setText(videoNode.getTitle());
                if(videoNode.getVideotype() != null && videoNode.getVideotype() > 0){
                    //1 球机   2 枪机  3 半球
                    Drawable drawable = null;
                    switch (videoNode.getVideotype()){
                  /*  case 1:
                        if(videoNode.getStatus()==1){
                            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj3_zx);
                        }else {
                            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj3_lx);
                        }
                        break;*/
                        case 2:
                            if(videoNode.getStatus()==1){
                                drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj1_zx);
                            }else {
                                drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj1_lx);
                            }
                            break;
                        case 3:
                            if(videoNode.getStatus()==1){
                                drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj2_zx);
                            }else {
                                drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj2_lx);
                            }
                            break;
                        default:
                            if(videoNode.getStatus()==1){
                                drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj3_zx);
                            }else {
                                drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_sxj3_lx);
                            }
                            break;

                    }
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    textView.setCompoundDrawables(drawable,null,null,null);
                }

            }
        }
    }


    @BindingAdapter("videoIsControl")
    public static void bindVideoIsControl(TextView textView, boolean isControl){
        Drawable drawable = null;
        if(isControl){
            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_kz_sec);
        }else {
            drawable = ContextCompat.getDrawable(textView.getContext(), R.mipmap.icon_kz_nor);
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null,drawable,null,null);
    }

}
