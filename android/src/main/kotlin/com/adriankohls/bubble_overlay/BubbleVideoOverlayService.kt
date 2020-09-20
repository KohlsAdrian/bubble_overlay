package com.adriankohls.bubble_overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.net.Uri
import android.os.*
import android.util.Log
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


class BubbleVideoOverlayService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mBubbleVideoView: View? = null
    private var isBubbleVideoViewBig: Boolean = false
    private var heightScreen: Int? = null
    private var widthScreen: Int? = null
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var controlsView: ConstraintLayout? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var binder = LocalBinder()
    private var inflater: LayoutInflater? = null
    private var rootView: ViewGroup? = null
    private var isToSeek: Boolean = false
    private var startTimeInMilliseconds = 0L

    private val RESIZE_SCREEN_TIMEOUT: Long = 2500L

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

        when (controlsType) {
            ControlsType.MINIMAL -> loadMinimalControls()
            else -> loadStandardControls()
        }
        controlsView?.visibility = INVISIBLE

        player?.prepare(videoSource)

        if (seekAtStart) {
            //seek
            player?.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
//                        Player.STATE_IDLE -> {}
//                        Player.STATE_BUFFERING -> {}
                        Player.STATE_READY -> onPlayerReady(playWhenReady, playbackState)
                        Player.STATE_ENDED -> closeServiceAndReturnData()
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

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onCreate() {
        super.onCreate()


        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "layout_video_bubble_video_player:service")

        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mBubbleVideoView = inflater?.inflate(R.layout.layout_video_bubble_video_player, null)

        playerView = mBubbleVideoView?.findViewById<PlayerView>(R.id.bubble_player_view)

        rootView = mBubbleVideoView?.findViewById(R.id.coordinatorLayout) as ViewGroup

        getScreenSize()

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

        setDefaultWindowSize(params)

        setWindowSizeOnRatioAspect(params)

        gestureOnFloatinWindow(params)
    }

    private fun getScreenSize() {
        val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display!!.getSize(size)
        heightScreen = size.y
        widthScreen = size.x
        Log.i("heightScreen", "heightScreen:$heightScreen, widthScreen:$widthScreen")
    }

    private fun setDefaultWindowSize(params: WindowManager.LayoutParams) {
        //Specify the window head position
        params.gravity = Gravity.BOTTOM or Gravity.END //Initially view will be added to Bottom-Right corner
        params.x = (widthScreen!!.toFloat() * 4.toFloat() / 100.toFloat()).toInt()
        params.y = (heightScreen!!.toFloat() * 3.toFloat() / 100.toFloat()).toInt()

        //Specify the window size
        // Invisible until give aspect ratio
        // IMPORTANT: HEIGHT AND WIDTH CAN'T BE OR BUBBLE WON'T BE SHOWN
        params.height = 1
        params.width = 1
    }

    private fun setWindowSizeOnRatioAspect(params: WindowManager.LayoutParams) {
        (player as SimpleExoPlayer)?.addVideoListener(object : SimpleExoPlayer.VideoListener {
            override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
                Log.i("onVideoSizeChanged", "MainActivity.onVideoSizeChanged.width:$width, height:$height   pixelWidthHeightRatio:$pixelWidthHeightRatio")

                var currentWidth: Int
                var currentHeight: Int
                if (width > height) {
                    currentWidth = (widthScreen!!.toFloat() * 60.toFloat() / 100.toFloat()).toInt()
                    currentHeight = (currentWidth.toFloat() * (height.toFloat() / width.toFloat())).toInt()

                    Log.d("(height / width)", (height.toFloat() / width.toFloat()).toString())
                    Log.d("currentWidth", ((widthScreen!!.toFloat() * 60.toFloat()) / 100.toFloat()).toString())
                } else {
                    currentHeight = (heightScreen!!.toFloat() * 40.toFloat() / 100.toFloat()).toInt()
                    currentWidth = (currentHeight.toFloat() * (height.toFloat() / width.toFloat())).toInt()
                }
                params.width = currentWidth
                params.height = currentHeight
                mWindowManager?.updateViewLayout(mBubbleVideoView, params)
                Log.d("params.width", params.width.toString())
                Log.d("params.height", params.height.toString())
            }

            override fun onRenderedFirstFrame() {
                Log.i("onRenderedFirstFrame", "MainActivity.onRenderedFirstFrame.")
            }
        })
    }

    private fun gestureOnFloatinWindow(params: WindowManager.LayoutParams) {
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
                                if (lastAction == MotionEvent.ACTION_DOWN) {
                                    showControlsOnTouch(params)
                                }

                                lastAction = event.action

                                return true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                //Calculate the X and Y coordinates of the view.
                                params.x = initialX - (event.rawX - initialTouchX).toInt()
                                params.y = initialY - (event.rawY - initialTouchY).toInt()

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

    private fun showControlsOnTouch(params: WindowManager.LayoutParams) {
        Log.d("showControlsOnTouch", "enter")
        if (!isBubbleVideoViewBig) {
            isBubbleVideoViewBig = true
            params.width = (params.width.toFloat() * 1.2.toFloat()).toInt()
            params.height = (params.height.toFloat() * 1.2.toFloat()).toInt()
            controlsView?.visibility = VISIBLE
            mWindowManager?.updateViewLayout(mBubbleVideoView, params)
            Log.d("showControlsOnTouch", "size icreased")

            Handler(Looper.getMainLooper()).postDelayed({
                controlsView?.visibility = INVISIBLE
                params.width = (params.width.toFloat() / 1.2.toFloat()).toInt()
                params.height = (params.height.toFloat() / 1.2.toFloat()).toInt()
                mWindowManager?.updateViewLayout(mBubbleVideoView, params)
                isBubbleVideoViewBig = false
                Log.d("showControlsOnTouch", "size restored")
            }, RESIZE_SCREEN_TIMEOUT)
        }
    }

    private fun loadStandardControls() {
        controlsView = inflater?.inflate(R.layout.layout_standard_controls, rootView, false) as ConstraintLayout
        rootView?.addView(controlsView)
        applyCorrectConstraint(controlsView!!)

        //Set the close button.
        val closeButton = mBubbleVideoView?.findViewById<View>(R.id.bubble_close)
        closeButton?.setOnClickListener {
            closeServiceAndReturnData()
        }
    }

    private fun loadMinimalControls() {
        controlsView = inflater?.inflate(R.layout.layout_video_bubble_minimal_controls, rootView, false) as ConstraintLayout
        rootView?.addView(controlsView)
        applyCorrectConstraint(controlsView!!)
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
        Log.d("togglePlayPause", "enter")
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