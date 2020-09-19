package com.adriankohls.bubble_overlay

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.view.*
import android.view.View.*
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.flutter.embedding.android.FlutterActivity


class BubbleVideoOverlayService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mBubbleVideoView: View? = null
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var binder = LocalBinder()
    private var inflater: LayoutInflater? = null
    private var rootView: ViewGroup? = null
    private var isToSeek: Boolean = false
    private var startTimeInMilliseconds = 0L

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@BubbleVideoOverlayService
    }

    fun setVideo(uri: Uri, seekAtStart: Boolean, startTimeInMilliseconds: Long, controlsType: ControlsType = ControlsType.STANDARD) {
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, application.packageName))
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        this.isToSeek = seekAtStart
        this.startTimeInMilliseconds = startTimeInMilliseconds

        when(controlsType) {
            ControlsType.MINIMAL -> loadMinimalControls()
            else -> loadStandardControls()
        }

        player?.prepare(videoSource)

        if (seekAtStart) {
            //seek
            player?.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
//                        Player.STATE_IDLE -> {}
//                        Player.STATE_BUFFERING -> {}
                        Player.STATE_READY -> onPlayerReady(playWhenReady, playbackState)
//                        Player.STATE_ENDED -> {}
                    }
                }
            })
        }
    }

    fun onPlayerReady(playWhenReady: Boolean, playbackState: Int) {
//        Log.d("isToSeek", isToSeek.toString())
//        Log.d("startTimeInSeconds", startTimeInMilliseconds.toString())
        if (isToSeek) {
            player?.seekTo(this.startTimeInMilliseconds)
            isToSeek = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mWakeLock?.acquire(30 * 60 * 1000L /*30 minutes*/)
        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "layout_video_bubble_video_player:service")

        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mBubbleVideoView = inflater?.inflate(R.layout.layout_video_bubble_video_player, null)

        playerView = mBubbleVideoView?.findViewById<PlayerView>(R.id.bubble_player_view)

        rootView = mBubbleVideoView?.findViewById(R.id.coordinatorLayout) as ViewGroup

        player = SimpleExoPlayer.Builder(this).build()
        player?.playWhenReady = true
        playerView?.player = player

        val params = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify the chat head position
        params.gravity = Gravity.TOP or Gravity.START //Initially view will be added to top-left corner
        params.x = 0
        params.y = 100

        //Add the view to the window
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager?.addView(mBubbleVideoView, params)

        mBubbleVideoView?.rootView?.setOnTouchListener(
                object : OnTouchListener {
                    private var lastAction = 0
                    private var initialX = 0
                    private var initialY = 0
                    private var initialTouchX = 0f
                    private var initialTouchY = 0f
                    override fun onTouch(v: View, event: MotionEvent): Boolean {

                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {

                                //remember the initial position.
                                initialX = params.x
                                initialY = params.y

                                //get the touch location
                                initialTouchX = event.rawX
                                initialTouchY = event.rawY
                                lastAction = event.action

                                return true
                            }
                            MotionEvent.ACTION_UP -> {
                                //if (lastAction == MotionEvent.ACTION_DOWN) { }
                                lastAction = event.action
                                return true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                //Calculate the X and Y coordinates of the view.
                                params.x = initialX + (event.rawX - initialTouchX).toInt()
                                params.y = initialY + (event.rawY - initialTouchY).toInt()

                                //Update the layout with new X & Y coordinate
                                mWindowManager?.updateViewLayout(mBubbleVideoView, params)
                                lastAction = event.action
                                return true
                            }
                            MotionEvent.ACTION_POINTER_DOWN -> {

                            }
                        }
                        return false
                    }
                }
        )
    }

    private fun loadStandardControls() {
        val child: ConstraintLayout = inflater?.inflate(R.layout.layout_standard_controls, rootView, false) as ConstraintLayout
        rootView?.addView(child)
        applyCorrectConstraint(child)

        //Set the close button.
        val closeButton = mBubbleVideoView?.findViewById<View>(R.id.bubble_close)
        closeButton?.setOnClickListener {
            closeServiceAndReturnData()
        }
    }

    private fun loadMinimalControls() {
        val child: ConstraintLayout = inflater?.inflate(R.layout.layout_video_bubble_minimal_controls, rootView, false) as ConstraintLayout
        rootView?.addView(child)
        applyCorrectConstraint( child)
        playerView?.useController = false

        val pauseButton: ImageView? = mBubbleVideoView?.findViewById(R.id.bubble_pause)
        val playButton: ImageView? = mBubbleVideoView?.findViewById(R.id.bubble_play)
        pauseButton?.setOnClickListener {
            togglePlayPause(pauseButton, playButton)
        }
        playButton?.setOnClickListener {
            togglePlayPause(pauseButton, playButton)
        }
        val openAppButton: ImageView? = mBubbleVideoView?.findViewById(R.id.bubble_open_app)
        openAppButton?.setOnClickListener {
            closeServiceAndReturnData()
        }
    }

    private fun closeServiceAndReturnData() {
        sendBroadcast(player?.currentPosition!!)
        stopSelf()
    }

    private fun sendBroadcast(currentTime: Long) {
        val intent = Intent("message") //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("currentTime", currentTime)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun togglePlayPause(pauseButton: ImageView?, playButton: ImageView?) {
        if (player?.isPlaying!!) {
            pauseButton?.visibility = GONE
            playButton?.visibility = VISIBLE
            player?.playWhenReady = false
        } else {
            pauseButton?.visibility = VISIBLE
            playButton?.visibility = GONE
            player?.playWhenReady = true
        }
    }

    private fun applyCorrectConstraint(child: ConstraintLayout) {
        val rootView = this.rootView as ConstraintLayout
        val cs = ConstraintSet()
        cs.clone(rootView)
        cs.connect(child.id, ConstraintSet.TOP, rootView.id, ConstraintSet.TOP)
        cs.connect(child.id, ConstraintSet.BOTTOM, rootView.id, ConstraintSet.BOTTOM)
        cs.connect(child.id, ConstraintSet.END, rootView.id, ConstraintSet.END)
        cs.connect(child.id, ConstraintSet.START, rootView.id, ConstraintSet.START)
        cs.constrainHeight(child.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
        cs.constrainWidth(child.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
        cs.applyTo(rootView)
    }


    override fun onDestroy() {
        player?.release()
        mWakeLock?.release()
        stopForeground(true)
        if (mBubbleVideoView != null) mWindowManager?.removeView(mBubbleVideoView)
        super.onDestroy()
    }
}