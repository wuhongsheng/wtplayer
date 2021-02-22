package com.wt.wtplayer.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.wt.wtplayer.R;
import com.wt.wtplayer.widget.IMediaController;
import com.wt.wtplayer.widget.PlayerSettings;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

/**
 * 自定义媒体控制
 *
 * @author whs
 * @date 2020/9/25
 */
public class CustomMediaController extends FrameLayout implements IMediaController {
    private static final String TAG = "CustomMediaController";
    private MediaController.MediaPlayerControl mPlayer;
    private final Context mContext;
    private View mAnchor;
    private View mRoot;
    private WindowManager mWindowManager;
    private Window mWindow;
    private View mDecor;
    private WindowManager.LayoutParams mDecorLayoutParams;
    private boolean mShowing;
    private boolean mDragging;
    private static final int sDefaultTimeout = 5000;
    private final boolean mUseFastForward;
    private boolean mFromXml;
    private boolean mListenersSet;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private ImageView mPauseButton;
    private ImageView mAudioButton;
    private TextView mVideoQuality;
    private ImageView mVideoQrien;


    private CharSequence mPlayDescription;
    private CharSequence mPauseDescription;
    private final AccessibilityManager mAccessibilityManager;

    private boolean isLand = false;

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    //是否正在录像
    private boolean isRecording = false;


    private PlayerSettings mPlayerSettings;


    public interface VideoControlListener{
        void changeOrientation(boolean landscape);
        void switchVideoQuality(String type);
    }

    public VideoControlListener getVideoControlListener() {
        return videoControlListener;
    }

    public void setVideoControlListener(VideoControlListener videoControlListener) {
        this.videoControlListener = videoControlListener;
    }

    private VideoControlListener videoControlListener;

    public CustomMediaController(Context context,AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;
        mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }
    @Override
    public void onFinishInflate() {
        if (mRoot != null){
            initControllerView(mRoot);
        }
    }

    public CustomMediaController(Context context,boolean useFastForward) {
        super(context);
        mContext = context;
        mPlayerSettings = new PlayerSettings(mContext);
        mUseFastForward = useFastForward;
        initFloatingWindowLayout();
        initFloatingWindow();
        mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public CustomMediaController(Context context,boolean useFastForward,boolean isLand) {
        super(context);
        mContext = context;
        mPlayerSettings = new PlayerSettings(mContext);
        mUseFastForward = useFastForward;
        initFloatingWindowLayout();
        initFloatingWindow();
        this.isLand = isLand;
        mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }


    public CustomMediaController(Context context) {
        this(context,true);
    }

    private void initFloatingWindow() {
        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        //通过反射获取PhoneWindow
        //mWindow = new PhoneWindow(mContext);
        try {
            Class<?> usbManagerClass = Class.forName("com.android.internal.policy.PhoneWindow");
            Constructor<?> cons = usbManagerClass.getConstructor(Context.class);
            mWindow  = (Window) cons.newInstance(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mWindow.setWindowManager(mWindowManager, null, null);
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        mDecor = mWindow.getDecorView();
        mDecor.setOnTouchListener(mTouchListener);
        mWindow.setContentView(this);

        mWindow.setBackgroundDrawableResource(android.R.color.transparent);

        // While the media controller is up, the volume control keys should
        // affect the media stream type
        mWindow.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        requestFocus();
    }

    // Allocate and initialize the static parts of mDecorLayoutParams. Must
    // also call updateFloatingWindowLayout() to fill in the dynamic parts
    // (y and width) before mDecorLayoutParams can be used.
    private void initFloatingWindowLayout() {
        mDecorLayoutParams = new WindowManager.LayoutParams();
        WindowManager.LayoutParams p = mDecorLayoutParams;
        p.gravity = Gravity.TOP | Gravity.LEFT;
        p.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        p.x = 0;
        p.format = PixelFormat.TRANSLUCENT;
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
        p.token = null;
        p.windowAnimations = 0; // android.R.style.DropDownAnimationDown;
    }

    // Update the dynamic parts of mDecorLayoutParams
    // Must be called with mAnchor != NULL.
    private void updateFloatingWindowLayout() {
        int [] anchorPos = new int[2];
        mAnchor.getLocationOnScreen(anchorPos);

        // we need to know the size of the controller so we can properly position it
        // within its space
        mDecor.measure(MeasureSpec.makeMeasureSpec(mAnchor.getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(mAnchor.getHeight(), MeasureSpec.AT_MOST));

        WindowManager.LayoutParams p = mDecorLayoutParams;
        p.width = mAnchor.getWidth();
        p.x = anchorPos[0] + (mAnchor.getWidth() - p.width) / 2;
        p.y = anchorPos[1] + mAnchor.getHeight() - mDecor.getMeasuredHeight();
    }

    // This is called whenever mAnchor's layout bound changes
    private final OnLayoutChangeListener mLayoutChangeListener =
            new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight,
                                           int oldBottom) {
                    //根据屏幕方向动态更改布局
                    /*boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
                    LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    if(isLand){
                        mRoot = inflate.inflate(R.layout.custom_media_controller_full, null);
                    }else {
                        mRoot = inflate.inflate(R.layout.custom_media_controller, null);
                    }*/
                    mVideoQuality = mRoot.findViewById(R.id.tv_quality);

                    //String type = WtSetting.getInstance().getString(PlayerSettings.SETTING_CUSTOM_STREAM_TYPE,WtSetting.getInstance().getString(PlayerSettings.SETTING_STREAM_TYPE,"1"));
                    if(mPlayerSettings.getStreamType().equals("1")){
                        mVideoQuality.setText(mContext.getString(R.string.sd));
                    }else {
                        mVideoQuality.setText(mContext.getString(R.string.hd));
                    }
                    updateFloatingWindowLayout();
                    if (mShowing) {
                        mWindowManager.updateViewLayout(mDecor, mDecorLayoutParams);
                    }
                }
            };

    private final OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                }
            }
            return false;
        }
    };

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * When VideoView calls this method, it will use the VideoView's parent
     * as the anchor.
     * @param view The view to which to anchor the controller when it is visible.
     */
    @Override
    public void setAnchorView(View view) {
        if (mAnchor != null) {
            mAnchor.removeOnLayoutChangeListener(mLayoutChangeListener);
        }
        mAnchor = view;
        if (mAnchor != null) {
            mAnchor.addOnLayoutChangeListener(mLayoutChangeListener);
        }

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(mRoot == null){
            if(isLand){
                mRoot = inflate.inflate(R.layout.custom_media_controller_full, null);
            }else {
                mRoot = inflate.inflate(R.layout.custom_media_controller, null);
            }
            mVideoQuality = mRoot.findViewById(R.id.tv_quality);

            //String type = WtSetting.getInstance().getString(PlayerSettings.SETTING_CUSTOM_STREAM_TYPE,WtSetting.getInstance().getString(PlayerSettings.SETTING_STREAM_TYPE,"1"));
            if(mPlayerSettings.getStreamType().equals("1")){
                mVideoQuality.setText(mContext.getString(R.string.sd));
            }else {
                mVideoQuality.setText(mContext.getString(R.string.hd));
            }
        }
        initControllerView(mRoot);
        return mRoot;
    }




    private void initControllerView(View v) {
        Resources res = mContext.getResources();
        mPlayDescription = res
                .getText(R.string.video_play);
        mPauseDescription = res
                .getText(R.string.video_pause);
        mPauseButton = v.findViewById(R.id.iv_play);
        mAudioButton = v.findViewById(R.id.iv_sound);
        mVideoQuality = v.findViewById(R.id.tv_quality);
        mVideoQrien = v.findViewById(R.id.iv_extend);
        //视频播放控制
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        //音频控制
        if(mAudioButton != null){
            mAudioButton.requestFocus();
            mAudioButton.setOnClickListener(mAudioListener);
        }
        //视频质量切换
        if(mVideoQuality != null){
            mVideoQuality.requestFocus();
            mVideoQuality.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(videoControlListener != null){
                        if(isRecording){
                            ToastUtils.showShort("请先结束录像操作");
                            return;
                        }
                        if(mVideoQuality.getText().equals(mContext.getString(R.string.sd))){
                            mPlayerSettings.setStreamType("2");
                            //WtSetting.getInstance().setSetting(PlayerSettings.SETTING_CUSTOM_STREAM_TYPE,"2");
                            mVideoQuality.setText(mContext.getString(R.string.hd));
                        }else {
                            mPlayerSettings.setStreamType("1");
                            //WtSetting.getInstance().setSetting(PlayerSettings.SETTING_CUSTOM_STREAM_TYPE,"1");
                            mVideoQuality.setText(mContext.getString(R.string.sd));
                        }
                        videoControlListener.switchVideoQuality((String) mVideoQuality.getText());
                    }
                }
            });
        }
        //横竖屏切换
        if(mVideoQrien != null){
            mVideoQrien.requestFocus();
            mVideoQrien.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(videoControlListener != null){
                        boolean tag = mVideoQrien.getTag() == null ? false : (boolean) mVideoQrien.getTag();
                        mVideoQrien.setTag(!tag);
                        videoControlListener.changeOrientation(!tag);
                    }
                }
            });
        }







        /*mFfwdButton = v.findViewById(com.android.internal.R.id.ffwd);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
            if (!mFromXml) {
                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        mRewButton = v.findViewById(com.android.internal.R.id.rew);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            if (!mFromXml) {
                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }*/

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        /*mNextButton = v.findViewById(com.android.internal.R.id.next);
        if (mNextButton != null && !mFromXml && !mListenersSet) {
            mNextButton.setVisibility(View.GONE);
        }
        mPrevButton = v.findViewById(com.android.internal.R.id.prev);
        if (mPrevButton != null && !mFromXml && !mListenersSet) {
            mPrevButton.setVisibility(View.GONE);
        }*/

       /* mProgress = v.findViewById(com.android.internal.R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }*/

       /* mEndTime = v.findViewById(com.android.internal.R.id.time);
        mCurrentTime = v.findViewById(com.android.internal.R.id.time_current);*/
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Extends
     */
    private ArrayList<View> mShowOnceArray = new ArrayList<View>();

    @Override
    public void showOnce(View view) {
        mShowOnceArray.add(view);
        view.setVisibility(View.VISIBLE);
        show();
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
            /*if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }*/
            // TODO What we really should do is add a canSeek to the MediaPlayerControl interface;
            // this scheme can break the case when applications want to allow seek through the
            // progress bar but disable forward/backward buttons.
            //
            // However, currently the flags SEEK_BACKWARD_AVAILABLE, SEEK_FORWARD_AVAILABLE,
            // and SEEK_AVAILABLE are all (un)set together; as such the aforementioned issue
            // shouldn't arise in existing applications.
            /*if (mProgress != null && !mPlayer.canSeekBackward() && !mPlayer.canSeekForward()) {
                mProgress.setEnabled(false);
            }*/
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     * @param timeout The timeout in milliseconds. Use 0 to show
     * the controller until hide() is called.
     */
    @Override
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            updateFloatingWindowLayout();
            mWindowManager.addView(mDecor, mDecorLayoutParams);
            mShowing = true;
        }
        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        post(mShowProgress);

        if (timeout != 0 && !mAccessibilityManager.isTouchExplorationEnabled()) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {
        if (mAnchor == null){
            return;
        }
        if (mShowing) {
            try {
                removeCallbacks(mShowProgress);
                mWindowManager.removeView(mDecor);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }

        for (View view : mShowOnceArray){
            view.setVisibility(View.GONE);
        }
        mShowOnceArray.clear();
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPlayer.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
       /* if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));*/

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(TAG,"onTouchEvent");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // show until hide is called
                show(0);
                break;
            case MotionEvent.ACTION_UP:
                // start timeout
                show(sDefaultTimeout);
                break;
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private final View.OnClickListener mPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    /**
     * 音频控制监听
     */
    private final View.OnClickListener mAudioListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mRoot == null || mPauseButton == null){
                return;
            }
            boolean tag = mAudioButton.getTag() == null ? false : (boolean) mAudioButton.getTag();
            mAudioButton.setImageResource(!tag ? R.mipmap.icon_jy : R.mipmap.icon_yinxiao);
            mAudioButton.setTag(!tag);
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, !tag ? 0 : 5, 0);
        }
    };

    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null){
            return;
        }
        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.mipmap.icon_zt);
            mPauseButton.setContentDescription(mPauseDescription);
        } else {
            mPauseButton.setImageResource(R.mipmap.icon_bf_1);
            mPauseButton.setContentDescription(mPlayDescription);
        }
    }

    public void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }



    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return MediaController.class.getName();
    }

    private final View.OnClickListener mRewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };
    //快进
    private final View.OnClickListener mFfwdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    public interface MediaPlayerControl {
        void    start();
        void    pause();
        int     getDuration();
        int     getCurrentPosition();
        void    seekTo(int pos);
        boolean isPlaying();
        int     getBufferPercentage();
        boolean canPause();
        boolean canSeekBackward();
        boolean canSeekForward();

        /**
         * Get the audio session id for the player used by this VideoView. This can be used to
         * apply audio effects to the audio track of a video.
         * @return The audio session, or 0 if there was an error.
         */
        int     getAudioSessionId();
    }
}
