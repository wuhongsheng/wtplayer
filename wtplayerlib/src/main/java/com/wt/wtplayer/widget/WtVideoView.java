package com.wt.wtplayer.widget;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.wt.wtplayer.CommonUtil;
import com.wt.wtplayer.R;
import com.wt.wtplayer.services.MediaPlayerService;
import com.wt.wtplayer.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * 自定义视频播放控件
 */
public class WtVideoView extends FrameLayout implements MediaController.MediaPlayerControl , LifecycleObserver {
    private String TAG = "WtVideoView";
    private Uri mUri;
    private Map<String, String> mHeaders;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;


    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private IRenderView.ISurfaceHolder mSurfaceHolder = null;
    private IMediaPlayer mMediaPlayer = null;
    private IjkMediaPlayer ijkMediaPlayer;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mVideoRotationDegree;
    private IMediaController mMediaController;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause = true;
    private boolean mCanSeekBack = true;
    private boolean mCanSeekForward = true;


    private Context mAppContext;
    //配置项
    //private Settings mSettings;
    private IRenderView mRenderView;
    private int mVideoSarNum;
    private int mVideoSarDen;
    /**
     * 视频相关参数显示
     */
    private InfoHudViewHolder mHudViewHolder;

    private String videoRecordPath;


    private long mPrepareStartTime = 0;
    private long mPrepareEndTime = 0;

    private long mSeekStartTime = 0;
    private long mSeekEndTime = 0;

    private TextView subtitleDisplay;

    private PlayerSettings mPlayerSettings;

    /**
     * 默认静音
     */
    private boolean isMute = true;

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    private boolean hasAudio = false;

    public void setmScaleGestureDetector(ScaleGestureDetector mScaleGestureDetector) {
        this.mScaleGestureDetector = mScaleGestureDetector;
    }

    //缩放事件手势识别器
    private ScaleGestureDetector mScaleGestureDetector;

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void initPlayer(){
        Log.e(TAG,"initPlayer");
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop(){
        Log.e(TAG,"onStop");
      /*  stopPlayback();
        release(true);
        stopBackgroundPlay();*/
        IjkMediaPlayer.native_profileEnd();
    }


    private static final int[] s_allAspectRatio = {
            IRenderView.AR_ASPECT_FIT_PARENT,
            IRenderView.AR_ASPECT_FILL_PARENT,
            IRenderView.AR_ASPECT_WRAP_CONTENT,
            IRenderView.AR_MATCH_PARENT,
            IRenderView.AR_16_9_FIT_PARENT,
            IRenderView.AR_4_3_FIT_PARENT};
    //private int mCurrentAspectRatioIndex = 0;
    private int mCurrentAspectRatio = s_allAspectRatio[3];


    public interface OnScaleGestureListener{
        void onZoomIn();
        void onZoomOut();
    }

    public void setmOnScaleGestureListener(OnScaleGestureListener mOnScaleGestureListener) {
        this.mOnScaleGestureListener = mOnScaleGestureListener;
    }

    private OnScaleGestureListener mOnScaleGestureListener;


    public WtVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public WtVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public WtVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WtVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView(context);
    }


    private void initVideoView(Context context) {
        mAppContext = context.getApplicationContext();
        mPlayerSettings = new PlayerSettings(mAppContext);

        initBackground();
        initRenders();
        setRender();


        mVideoWidth = 0;
        mVideoHeight = 0;
        // REMOVED: getHolder().addCallback(mSHCallback);
        // REMOVED: getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        // REMOVED: mPendingSubtitleTracks = new Vector<Pair<InputStream, MediaFormat>>();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;

        subtitleDisplay = new TextView(context);
        subtitleDisplay.setTextSize(24);
        subtitleDisplay.setGravity(Gravity.CENTER);
        LayoutParams layoutParams_txt = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        addView(subtitleDisplay, layoutParams_txt);
    }

    public void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(null);
            }

            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mSHCallback);
            mRenderView = null;
            removeView(renderUIView);
        }

        if (renderView == null) {
            return;
        }

        mRenderView = renderView;
        renderView.setAspectRatio(mCurrentAspectRatio);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            renderView.setVideoSize(mVideoWidth, mVideoHeight);
        }
        if (mVideoSarNum > 0 && mVideoSarDen > 0) {
            renderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
        }

        View renderUIView = mRenderView.getView();
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        addView(renderUIView);

        mRenderView.addRenderCallback(mSHCallback);
        mRenderView.setVideoRotation(mVideoRotationDegree);

    }

    /**
     * 设置渲染控件
     */
    public void setRender() {
        TextureRenderView renderView = new TextureRenderView(getContext());
        if (mMediaPlayer != null) {
            renderView.getSurfaceHolder().bindToMediaPlayer(mMediaPlayer);
            renderView.setVideoSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            renderView.setVideoSampleAspectRatio(mMediaPlayer.getVideoSarNum(), mMediaPlayer.getVideoSarDen());
            renderView.setAspectRatio(mCurrentAspectRatio);
        }
        setRenderView(renderView);
        /*SurfaceRenderView renderView = new SurfaceRenderView(getContext());
        setRenderView(renderView);*/
        /*switch (render) {
            case RENDER_NONE:
                setRenderView(null);
                break;
            case RENDER_TEXTURE_VIEW: {
                TextureRenderView renderView = new TextureRenderView(getContext());
                if (mMediaPlayer != null) {
                    renderView.getSurfaceHolder().bindToMediaPlayer(mMediaPlayer);
                    renderView.setVideoSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                    renderView.setVideoSampleAspectRatio(mMediaPlayer.getVideoSarNum(), mMediaPlayer.getVideoSarDen());
                    renderView.setAspectRatio(mCurrentAspectRatio);
                }
                setRenderView(renderView);
                break;
            }
            case RENDER_SURFACE_VIEW: {
                SurfaceRenderView renderView = new SurfaceRenderView(getContext());
                setRenderView(renderView);
                break;
            }
            default:
                Log.e(TAG, String.format(Locale.getDefault(), "invalid render %d\n", render));
                break;
        }*/
    }

    public void setHudView(TableLayout tableLayout) {
        mHudViewHolder = new InfoHudViewHolder(getContext(), tableLayout);
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        //切换播放地址,重新设置渲染层
        setRender();
        openVideo();
        requestLayout();
        invalidate();
    }


    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            if (mHudViewHolder != null) {
                mHudViewHolder.setMediaPlayer(null);
            }
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);

        AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        try {
            mMediaPlayer = createPlayer();

            // TODO: create SubtitleController in MediaPlayer, but we need
            // a context for the subtitle renderers
            final Context context = getContext();
            // REMOVED: SubtitleController

            // REMOVED: mAudioSession
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
            mCurrentBufferPercentage = 0;
            String scheme = mUri.getScheme();
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    mSettings.getUsingMediaDataSource() &&
                    (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                IMediaDataSource dataSource = new FileMediaDataSource(new File(mUri.toString()));
                mMediaPlayer.setDataSource(dataSource);
            }  else */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mMediaPlayer.setDataSource(mAppContext, mUri, mHeaders);
            } else {
                mMediaPlayer.setDataSource(mUri.toString());
            }
            bindSurfaceHolder(mMediaPlayer, mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mPrepareStartTime = System.currentTimeMillis();
            mMediaPlayer.prepareAsync();
            if (mHudViewHolder != null) {
                mHudViewHolder.setMediaPlayer(mMediaPlayer);
            }

            // REMOVED: mPendingSubtitleTracks

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } finally {
            // REMOVED: mPendingSubtitleTracks.clear();
        }
    }

    public void setMediaController(IMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    public void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    mVideoSarNum = mp.getVideoSarNum();
                    mVideoSarDen = mp.getVideoSarDen();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        if (mRenderView != null) {
                            mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                            mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                        }
                        requestLayout();
                    }
                }
            };
    /**
     * 视频准备完成监听
     */
    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {

            mPrepareEndTime = System.currentTimeMillis();
            if(mHudViewHolder != null){
                mHudViewHolder.updateLoadCost(mPrepareEndTime - mPrepareStartTime);
            }
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            // REMOVED: Metadata

            if(isMute){
                onMute();
            }

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mRenderView != null) {
                    mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                    mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                    if (!mRenderView.shouldWaitForResize() || mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                        // We didn't actually change the size (it was already at the size
                        // we need), so we won't get a "surface changed" callback, so
                        // start the video here instead of in the callback.
                        if (mTargetState == STATE_PLAYING) {

                            start();
                            if (mMediaController != null) {
                                mMediaController.show();
                            }
                        } else if (!isPlaying() &&
                                (seekToPosition != 0 || getCurrentPosition() > 0)) {
                            if (mMediaController != null) {
                                // Show the media controls when we're paused into a video and make 'em stick.
                                mMediaController.show(0);
                            }
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    /**
     * 静音
     */
    public void onMute(){
        AudioManager audioManager = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }

    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };

    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, arg1, arg2);
                    }
                    switch (arg1) {
                        case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                            Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                            Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                            Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                            Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                            Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                            Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                            mVideoRotationDegree = arg2;
                            Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                            if (mRenderView != null) {
                                mRenderView.setVideoRotation(arg2);
                            }
                            break;
                        case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                            Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                            break;
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */
                    if (getWindowToken() != null) {
                        Resources r = mAppContext.getResources();
                        int messageId;

                        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                            messageId = R.string.VideoView_error_text_invalid_progressive_playback;
                        } else {
                            messageId = R.string.VideoView_error_text_unknown;
                        }

                        new AlertDialog.Builder(getContext())
                                .setMessage(messageId)
                                .setPositiveButton(R.string.VideoView_error_button,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                /* If we get here, there is no onError listener, so
                                                 * at least inform them that the video is over.
                                                 */
                                                if (mOnCompletionListener != null) {
                                                    mOnCompletionListener.onCompletion(mMediaPlayer);
                                                }
                                            }
                                        })
                                .setCancelable(false)
                                .show();
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            mSeekEndTime = System.currentTimeMillis();
            if(mHudViewHolder != null){
                mHudViewHolder.updateSeekCost(mSeekEndTime - mSeekStartTime);
            }
        }
    };

    private IMediaPlayer.OnTimedTextListener mOnTimedTextListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
            if (text != null) {
                subtitleDisplay.setText(text.getText());
            }
        }
    };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    // REMOVED: mSHCallback
    private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
        if (mp == null) {
            return;
        }

        if (holder == null) {
            mp.setDisplay(null);
            return;
        }

        holder.bindToMediaPlayer(mp);
    }

    IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int w, int h) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceChanged: unmatched render callback\n");
                return;
            }

            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = !mRenderView.shouldWaitForResize() || (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceCreated: unmatched render callback\n");
                return;
            }

            mSurfaceHolder = holder;
            if (mMediaPlayer != null) {
                bindSurfaceHolder(mMediaPlayer, holder);
            } else {
                openVideo();
            }
        }

        @Override
        public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceDestroyed: unmatched render callback\n");
                return;
            }

            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            // REMOVED: if (mMediaController != null) mMediaController.hide();
            // REMOVED: release(true);
            releaseWithoutStop();
        }
    };

    public void releaseWithoutStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(null);
        }
    }

    /*
     * release the media player in any state
     */
    public void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            // REMOVED: mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.e(TAG,"onTouchEvent");
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            if (isInPlaybackState() && mMediaController != null) {
                toggleMediaControlsVisiblity();
            }
        }

        if(mScaleGestureDetector != null){
            mScaleGestureDetector.onTouchEvent(ev);
        }

        return true;
    }

    /**
     * 轨迹球事件
     * @param ev
     * @return
     */
    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG,"onKeyDown");
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        Log.e(TAG,"toggleMediaControlsVisiblity");
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mSeekStartTime = System.currentTimeMillis();
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


    //-------------------------
    // Extend: Aspect Ratio
    //-------------------------



   /* public int toggleAspectRatio() {
        mCurrentAspectRatioIndex++;
        mCurrentAspectRatioIndex %= s_allAspectRatio.length;

        mCurrentAspectRatio = s_allAspectRatio[mCurrentAspectRatioIndex];
        if (mRenderView != null) {
            mRenderView.setAspectRatio(mCurrentAspectRatio);
        }
        return mCurrentAspectRatio;
    }*/

    /**
     * 根据屏幕方向调整视频纵横比
     *
     * @param isLandscape
     */
    public void toggleAspectRatioByOrientation(boolean isLandscape) {
        Log.e(TAG, "toggleAspectRatioByOrientation isLandscape:" + isLandscape);
        /*if (isLandscape) {
            mCurrentAspectRatio = IRenderView.AR_MATCH_PARENT;
        } else {
            mCurrentAspectRatio = IRenderView.AR_4_3_FIT_PARENT;
        }
        if (mRenderView != null) {
            mRenderView.setAspectRatio(mCurrentAspectRatio);
        }*/
    }

    //-------------------------
    // Extend: Render
    //-------------------------
    public static final int RENDER_NONE = 0;
    public static final int RENDER_SURFACE_VIEW = 1;
    public static final int RENDER_TEXTURE_VIEW = 2;

    private List<Integer> mAllRenders = new ArrayList<Integer>();
    private int mCurrentRenderIndex = 0;
    //渲染控件类型
    private int mCurrentRender = RENDER_NONE;

    private void initRenders() {
        mAllRenders.clear();

        if (mAllRenders.isEmpty())
            mAllRenders.add(RENDER_SURFACE_VIEW);
        mCurrentRender = mAllRenders.get(mCurrentRenderIndex);
        //setRender(mCurrentRender);
        setRender();
    }

 /*   public int toggleRender() {
        mCurrentRenderIndex++;
        mCurrentRenderIndex %= mAllRenders.size();

        mCurrentRender = mAllRenders.get(mCurrentRenderIndex);
        setRender(mCurrentRender);
        return mCurrentRender;
    }*/

  /*  @NonNull
    public static String getRenderText(Context context, int render) {
        String text;
        switch (render) {
            case RENDER_NONE:
                text = context.getString(R.string.VideoView_render_none);
                break;
            case RENDER_SURFACE_VIEW:
                text = context.getString(R.string.VideoView_render_surface_view);
                break;
            case RENDER_TEXTURE_VIEW:
                text = context.getString(R.string.VideoView_render_texture_view);
                break;
            default:
                text = context.getString(R.string.N_A);
                break;
        }
        return text;
    }*/

    //-------------------------
    // Extend: Player
    //-------------------------
   /* public int togglePlayer() {
        if (mMediaPlayer != null)
            mMediaPlayer.release();

        if (mRenderView != null)
            mRenderView.getView().invalidate();
        openVideo();
        return mSettings.getPlayer();
    }
*/
    /*@NonNull
    public static String getPlayerText(Context context, int player) {
        String text;
        switch (player) {
            case Settings.PV_PLAYER__AndroidMediaPlayer:
                text = context.getString(R.string.VideoView_player_AndroidMediaPlayer);
                break;
            case Settings.PV_PLAYER__IjkMediaPlayer:
                text = context.getString(R.string.VideoView_player_IjkMediaPlayer);
                break;
            case Settings.PV_PLAYER__IjkExoMediaPlayer:
                text = context.getString(R.string.VideoView_player_IjkExoMediaPlayer);
                break;
            default:
                text = context.getString(R.string.N_A);
                break;
        }
        return text;
    }*/

    public IMediaPlayer createPlayer() {
        IMediaPlayer mediaPlayer = null;

        //IjkMediaPlayer ijkMediaPlayer = null;
        if (mUri != null) {
            ijkMediaPlayer = new IjkMediaPlayer();
            //日志输出
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);

           /* if (mSettings.getUsingMediaCodec()) {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                if (mSettings.getUsingMediaCodecAutoRotate()) {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                } else {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
                }
                if (mSettings.getMediaCodecHandleResolutionChange()) {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
                } else {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
                }
            } else {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            }*/

            if (mPlayerSettings.getUsingMediaCodec()) {
                //硬解
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                //
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);

                //硬解码情况下自动旋转
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                //硬解码下自动切换分辨率
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            } else {
                //软解
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            }

            //音频使用opensles来播放
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);


         /*   if (mSettings.getUsingOpenSLES()) {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
            } else {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            }*/



            /*String pixelFormat = mSettings.getPixelFormat();
            if (TextUtils.isEmpty(pixelFormat)) {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
            } else {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixelFormat);
            }*/

            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
            //drop frames when cpu is too slow：0-120  丢帧,默认是1
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            //缓冲区
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
            //是否开启缓冲
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);

            //对指定帧不做环路滤波, 可以节省CPU.
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);


            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "iformat", "flv");

            //rtmp秒开配置
            //设置播放前的最大探测时间 分析码流时长
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 5000L);

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 3000);


            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
         /*   if(hasAudio){
                //播放前的探测Size，默认是1M, 改小一点会出画面更快 会影响声音播放
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024L * 80);
            }else {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 512L);
            }*/

            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 512L);


            //播放前的探测Size，默认是1M, 改小一点会出画面更快 会影响声音播放
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024L * 10);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);//是否限制输入缓存数
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 100);//最大缓存时长

            //每处理一个packet之后刷新io上下文
           /* ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
            //是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
            //跳帧处理,放CPU处理较慢时，进行跳帧处理，保证播放流程，画面和声音同步
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);*/
            //rtmp秒开配置 end

            //RTSP优化
           /* ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"max-fps",30);
            //不额外优化
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1);
            //声音
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "volume", 0);//不额外优化
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 200);//10240
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);
            //pause output until enough packets have been read after stalling
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);//是否开启缓冲
            //drop frames when cpu is too slow：0-120
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);//丢帧,默认是1
            //automatically start playing on prepared
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);//默认值48
            //max buffer size should be pre-read：默认为15*1024*1024
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 0);//最大缓存数
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 2);//默认最小帧数2
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 30);//最大缓存时长
            //input buffer:don't limit the input buffer size (useful with realtime streams)
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);//是否限制输入缓存数
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");*/
            //RTSP优化 END
            //tcp传输数据 会增加延时
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");

            //清空DNS,有时因为在APP里面要播放多种类型的视频(如:MP4,直播,直播平台保存的视频,和其他http视频), 有时会造成因为DNS的问题而报10000问题的
            //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        }
        mediaPlayer = ijkMediaPlayer;

      /*  if (mSettings.getEnableDetachedSurfaceTextureView()) {
            mediaPlayer = new TextureMediaPlayer(mediaPlayer);
        }*/

        return mediaPlayer;
    }

    //-------------------------
    // Extend: Background
    //-------------------------

    private boolean mEnableBackgroundPlay = true;

    private void initBackground() {
        //mEnableBackgroundPlay = mSettings.getEnableBackgroundPlay();
        if (mEnableBackgroundPlay) {
            MediaPlayerService.intentToStart(getContext());
            mMediaPlayer = MediaPlayerService.getMediaPlayer();
            if (mHudViewHolder != null) {
                mHudViewHolder.setMediaPlayer(mMediaPlayer);
            }
        }
    }

    public boolean isBackgroundPlayEnabled() {
        return mEnableBackgroundPlay;
    }

    /**
     * 界面进入后台
     */
    public void enterBackground() {
        MediaPlayerService.setMediaPlayer(mMediaPlayer);
    }

    public void stopBackgroundPlay() {
        MediaPlayerService.setMediaPlayer(null);
    }

    //-------------------------
    // Extend: Background
    //-------------------------
   /* public void showMediaInfo() {
        if (mMediaPlayer == null)
            return;

        int selectedVideoTrack = MediaPlayerCompat.getSelectedTrack(mMediaPlayer, ITrackInfo.MEDIA_TRACK_TYPE_VIDEO);
        int selectedAudioTrack = MediaPlayerCompat.getSelectedTrack(mMediaPlayer, ITrackInfo.MEDIA_TRACK_TYPE_AUDIO);
        int selectedSubtitleTrack = MediaPlayerCompat.getSelectedTrack(mMediaPlayer, ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT);

        TableLayoutBinder builder = new TableLayoutBinder(getContext());
        builder.appendSection(R.string.mi_player);
        builder.appendRow2(R.string.mi_player, MediaPlayerCompat.getName(mMediaPlayer));
        builder.appendSection(R.string.mi_media);
        builder.appendRow2(R.string.mi_resolution, buildResolution(mVideoWidth, mVideoHeight, mVideoSarNum, mVideoSarDen));
        builder.appendRow2(R.string.mi_length, buildTimeMilli(mMediaPlayer.getDuration()));

        ITrackInfo trackInfos[] = mMediaPlayer.getTrackInfo();
        if (trackInfos != null) {
            int index = -1;
            for (ITrackInfo trackInfo : trackInfos) {
                index++;

                int trackType = trackInfo.getTrackType();
                if (index == selectedVideoTrack) {
                    builder.appendSection(getContext().getString(R.string.mi_stream_fmt1, index) + " " + getContext().getString(R.string.mi__selected_video_track));
                } else if (index == selectedAudioTrack) {
                    builder.appendSection(getContext().getString(R.string.mi_stream_fmt1, index) + " " + getContext().getString(R.string.mi__selected_audio_track));
                } else if (index == selectedSubtitleTrack) {
                    builder.appendSection(getContext().getString(R.string.mi_stream_fmt1, index) + " " + getContext().getString(R.string.mi__selected_subtitle_track));
                } else {
                    builder.appendSection(getContext().getString(R.string.mi_stream_fmt1, index));
                }
                builder.appendRow2(R.string.mi_type, buildTrackType(trackType));
                builder.appendRow2(R.string.mi_language, buildLanguage(trackInfo.getLanguage()));

                IMediaFormat mediaFormat = trackInfo.getFormat();
                if (mediaFormat == null) {
                } else if (mediaFormat instanceof IjkMediaFormat) {
                    switch (trackType) {
                        case ITrackInfo.MEDIA_TRACK_TYPE_VIDEO:
                            builder.appendRow2(R.string.mi_codec, mediaFormat.getString(IjkMediaFormat.KEY_IJK_CODEC_LONG_NAME_UI));
                            builder.appendRow2(R.string.mi_profile_level, mediaFormat.getString(IjkMediaFormat.KEY_IJK_CODEC_PROFILE_LEVEL_UI));
                            builder.appendRow2(R.string.mi_pixel_format, mediaFormat.getString(IjkMediaFormat.KEY_IJK_CODEC_PIXEL_FORMAT_UI));
                            builder.appendRow2(R.string.mi_resolution, mediaFormat.getString(IjkMediaFormat.KEY_IJK_RESOLUTION_UI));
                            builder.appendRow2(R.string.mi_frame_rate, mediaFormat.getString(IjkMediaFormat.KEY_IJK_FRAME_RATE_UI));
                            builder.appendRow2(R.string.mi_bit_rate, mediaFormat.getString(IjkMediaFormat.KEY_IJK_BIT_RATE_UI));
                            break;
                        case ITrackInfo.MEDIA_TRACK_TYPE_AUDIO:
                            builder.appendRow2(R.string.mi_codec, mediaFormat.getString(IjkMediaFormat.KEY_IJK_CODEC_LONG_NAME_UI));
                            builder.appendRow2(R.string.mi_profile_level, mediaFormat.getString(IjkMediaFormat.KEY_IJK_CODEC_PROFILE_LEVEL_UI));
                            builder.appendRow2(R.string.mi_sample_rate, mediaFormat.getString(IjkMediaFormat.KEY_IJK_SAMPLE_RATE_UI));
                            builder.appendRow2(R.string.mi_channels, mediaFormat.getString(IjkMediaFormat.KEY_IJK_CHANNEL_UI));
                            builder.appendRow2(R.string.mi_bit_rate, mediaFormat.getString(IjkMediaFormat.KEY_IJK_BIT_RATE_UI));
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        AlertDialog.Builder adBuilder = builder.buildAlertDialogBuilder();
        adBuilder.setTitle(R.string.media_information);
        adBuilder.setNegativeButton(R.string.close, null);
        adBuilder.show();
    }*/

    private String buildResolution(int width, int height, int sarNum, int sarDen) {
        StringBuilder sb = new StringBuilder();
        sb.append(width);
        sb.append(" x ");
        sb.append(height);

        if (sarNum > 1 || sarDen > 1) {
            sb.append("[");
            sb.append(sarNum);
            sb.append(":");
            sb.append(sarDen);
            sb.append("]");
        }

        return sb.toString();
    }

    private String buildTimeMilli(long duration) {
        long total_seconds = duration / 1000;
        long hours = total_seconds / 3600;
        long minutes = (total_seconds % 3600) / 60;
        long seconds = total_seconds % 60;
        if (duration <= 0) {
            return "--:--";
        }
        if (hours >= 100) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    private String buildTrackType(int type) {
        Context context = getContext();
        switch (type) {
            case ITrackInfo.MEDIA_TRACK_TYPE_VIDEO:
                return context.getString(R.string.TrackType_video);
            case ITrackInfo.MEDIA_TRACK_TYPE_AUDIO:
                return context.getString(R.string.TrackType_audio);
            case ITrackInfo.MEDIA_TRACK_TYPE_SUBTITLE:
                return context.getString(R.string.TrackType_subtitle);
            case ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT:
                return context.getString(R.string.TrackType_timedtext);
            case ITrackInfo.MEDIA_TRACK_TYPE_METADATA:
                return context.getString(R.string.TrackType_metadata);
            case ITrackInfo.MEDIA_TRACK_TYPE_UNKNOWN:
            default:
                return context.getString(R.string.TrackType_unknown);
        }
    }

    private String buildLanguage(String language) {
        if (TextUtils.isEmpty(language))
            return "und";
        return language;
    }

    public ITrackInfo[] getTrackInfo() {
        if (mMediaPlayer == null)
            return null;

        return mMediaPlayer.getTrackInfo();
    }

    /**
     * 开始录像
     */
    public int startRecord() {
        if (mMediaPlayer != null && mRenderView != null && ijkMediaPlayer != null) {
            //Log.e(TAG,"startRecord");
            videoRecordPath = mAppContext.getExternalFilesDir("record'").getAbsolutePath() + "/"
                    + new SimpleDateFormat("yyyyMMddHHmmss")
                    .format(new Date()) + ".mov";
            int result = ijkMediaPlayer.startRecord(videoRecordPath);
            Log.e(TAG, "startRecord result:" + result);
            return result;
        }
        return -1;
    }

    /**
     * 结束录像
     */
    public int stopRecord() {
        Log.e(TAG, "stopRecord");
        if (mMediaPlayer != null && mRenderView != null && ijkMediaPlayer != null) {
            int result = ijkMediaPlayer.stopRecord();
            Log.e(TAG, "stopRecord result:" + result);
            if (result == 0) {
                //广播到相册
                ContentValues localContentValues = CommonUtil.getVideoContentValues(mAppContext, new File(videoRecordPath), System.currentTimeMillis());
                Uri localUri = mAppContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
                Log.e(TAG, "save record uri:" + localUri);
                Toast.makeText(mAppContext, "录制成功并已保存到相册", Toast.LENGTH_SHORT).show();
            }
            return result;
        }
        return -1;
    }

    /**
     * 截图
     */
    public boolean snapshotPicture() {
        Bitmap srcBitmap =  this.mRenderView instanceof TextureRenderView ? ((TextureRenderView)this.mRenderView).getBitmap() : null;
       /* Bitmap srcBitmap = Bitmap.createBitmap(mVideoWidth,
                mVideoHeight, Bitmap.Config.ARGB_8888);*/
         // boolean flag = ijkMediaPlayer.getCurrentFrame(srcBitmap);
        if (srcBitmap != null) {
            // 保存图片
            //String path = getInnerSDCardPath() + "/ijkplayer/snapshot";
            String path = mAppContext.getExternalFilesDir("snapshot'").getAbsolutePath() + "/";
            File screenshotsDirectory = new File(path);
            if (!screenshotsDirectory.exists()) {
                screenshotsDirectory.mkdirs();
            }
            File savePath = new File(
                    screenshotsDirectory.getPath()
                            + "/"
                            + new SimpleDateFormat("yyyyMMddHHmmss")
                            .format(new Date()) + ".jpg");
            //ImageUtils.saveBitmap(savePath.getPath(), srcBitmap);
            boolean isSave = ImageUtils.save(srcBitmap, savePath.getPath(), Bitmap.CompressFormat.JPEG);
            if (isSave) {
                //广播到相册
                ContentValues localContentValues = CommonUtil.getImageContentValues(mAppContext, savePath, System.currentTimeMillis());
               /* ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, savePath.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");*/
                //values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/");

                Uri uri = mAppContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
                //mAppContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(savePath)));
                //mAppContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
                Log.e(TAG, "save snapshotPicture uri:" + uri);
                //mAppContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(savePath)));
                Toast.makeText(mAppContext, "截图成功并已保存到相册", Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "save snapshotPicture:" + isSave);
        }
        return true;
    }
}
