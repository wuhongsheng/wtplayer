package com.wt.wtplayer;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;


import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description
 *
 * @author whs
 * @date 2019-05-24
 */
public class CommonUtil {
    private static final String TAG = "CommonUtil";


    /**
     * 校验密码格式
     *
     * @param mobiles
     * @return
     */
    public static boolean isPwd(String mobiles) {
        Log.i(TAG, "isPassword: " + mobiles);
        Pattern p = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$");
        Matcher m = p.matcher(mobiles);
        Log.i(TAG, "isPassword: 是否密码正则匹配" + m.matches());

        return m.matches();
    }


    /**
     * 密码显示或隐藏 （切换）
     */
    public static void showOrHide(EditText etPassword, boolean showPwd) {
        //记住光标开始的位置
        int pos = etPassword.getSelectionStart();
        Log.e(TAG, "pos:" + pos);
        Drawable mRightDrawable = etPassword.getCompoundDrawables()[2];
        Drawable mLeftDrawable = etPassword.getCompoundDrawables()[0];
        if (showPwd) {
            //显示密码
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            //隐藏密码
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        etPassword.setSelection(pos);
        etPassword.setCompoundDrawables(mLeftDrawable, null, mRightDrawable, null);
    }

    /**
     * 密码显示或隐藏 （切换）
     */
    public static void showOrHide(EditText etPassword) {
        //记住光标开始的位置
        int pos = etPassword.getSelectionStart();
        Log.e(TAG, "pos:" + pos);

        if (etPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
            //显示密码
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        } else {
            //隐藏密码
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        etPassword.setSelection(pos);

    }





    public static List<String> String2List(String str) {
        String[] strings;
        if (TextUtils.isEmpty(str))
            str = "";
        strings = str.split(",");
        return Arrays.asList(strings);
    }


    public static boolean isLocImage(String path) {
        return path.contains("jpg");
    }

    /**
     * 获取keys
     *
     * @param map
     */
    public static String getMapKey2String(Map<String, Object> map) {
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (String key : map.keySet()) {
            System.out.println("key= " + key + " and value= " + map.get(key));
            if (isFirst) {
                sb.append(key);
                isFirst = false;
            } else {
                sb.append(",");
                sb.append(key);
            }
        }
        return TextUtils.isEmpty(sb.toString()) ? "" : sb.toString();
    }


    /**
     * 获取keys
     *
     * @param map
     */
    public static String getMapKey2list(Map<String, String> map) {
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (String key : map.keySet()) {
            System.out.println("key= " + key + " and value= " + map.get(key));
            if (isFirst) {
                sb.append(key);
                isFirst = false;
            } else {
                sb.append(",");
                sb.append(key);
            }
        }
        return TextUtils.isEmpty(sb.toString()) ? "" : sb.toString();
    }

    /**
     * 获取Value
     *
     * @param map
     */
    public static String getMapValue2String(Map<String, String> map) {
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (String key : map.keySet()) {
            System.out.println("key= " + key + " and value= " + map.get(key));
            if (isFirst) {
                sb.append(map.get(key));
                isFirst = false;
            } else {
                sb.append(",");
                sb.append(map.get(key));
            }
        }
        return TextUtils.isEmpty(sb.toString()) ? "" : sb.toString();

    }



    public static ContentValues getVideoContentValues(Context paramContext, File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "video/3gp");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    public static ContentValues getImageContentValues(Context paramContext, File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "image/jpeg");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("orientation", Integer.valueOf(0));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }
}
