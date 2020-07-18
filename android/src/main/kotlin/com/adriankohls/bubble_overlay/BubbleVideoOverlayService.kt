package com.adriankohls.bubble_overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.view.*
import android.view.View.OnTouchListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class BubbleVideoOverlayService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mBubbleVideoView: View? = null
    private var player: ExoPlayer? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@BubbleVideoOverlayService
    }

    fun setVideo(uri: Uri) {
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, application.packageName))
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

        player?.prepare(videoSource)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mWakeLock?.acquire(30 * 60 * 1000L /*30 minutes*/)
        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "layout_video_bubble:service")

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mBubbleVideoView = inflater.inflate(R.layout.layout_video_bubble, null)

        val playerView = mBubbleVideoView?.findViewById<PlayerView>(R.id.bubble_player_view)
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

        //Set the close button.
        val closeButton = mBubbleVideoView?.findViewById<View>(R.id.bubble_close)
        closeButton?.setOnClickListener { //
            stopSelf()
        }

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



    override fun onDestroy() {
        player?.release()
        mWakeLock?.release()
        stopForeground(true)
        if (mBubbleVideoView != null) mWindowManager?.removeView(mBubbleVideoView)
        super.onDestroy()
    }
}