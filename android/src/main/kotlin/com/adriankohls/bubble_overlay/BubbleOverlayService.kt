package com.adriankohls.bubble_overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide

class BubbleOverlayService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mBubbleView: View? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@BubbleOverlayService
    }

    fun updateText(customText: String) {
        val textView = mBubbleView?.findViewById<TextView>(R.id.bubble_custom_text)
        textView?.text = customText
    }

    fun updateTitle(customText: String) {
        val textView = mBubbleView?.findViewById<TextView>(R.id.bubble_custom_text_top)
        textView?.text = customText
    }

    fun updateTitleColor(color: String) {
        val textView = mBubbleView?.findViewById<TextView>(R.id.bubble_custom_text_top)
        textView?.setTextColor(Color.parseColor(color))
    }

    fun updateBottomText(customText: String) {
        val textView = mBubbleView?.findViewById<TextView>(R.id.bubble_custom_text_bottom)
        textView?.text = customText
    }

    fun updateBottomTextColor(color: String) {
        val textView = mBubbleView?.findViewById<TextView>(R.id.bubble_custom_text_bottom)
        textView?.setTextColor(Color.parseColor(color))
    }

    fun updateTextColor(color: String) {
        val textView = mBubbleView?.findViewById<TextView>(R.id.bubble_custom_text)
        textView?.setTextColor(Color.parseColor(color))
    }

    fun updateIconTop(icon: ByteArray?) {
        val iconTop = mBubbleView?.findViewById<ImageView>(R.id.bubble_image_top)
        if (iconTop != null && icon != null)
            Glide.with(this).load(icon).into(iconTop)
        else
            iconTop?.setImageResource(0)
    }

    fun updateIconBottom(icon: ByteArray?) {
        val iconBottom = mBubbleView?.findViewById<ImageView>(R.id.bubble_image_bottom)
        if (iconBottom != null && icon != null)
            Glide.with(this).load(icon).into(iconBottom)
        else
            iconBottom?.setImageResource(0)
    }

    fun updateBubbleColor(color: String) {
        val cardView = mBubbleView?.findViewById<CardView>(R.id.card)
        cardView?.setCardBackgroundColor((Color.parseColor(color)))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mWakeLock?.acquire(30 * 60 * 1000L /*30 minutes*/)
        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "layout_bubble:service")

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mBubbleView = inflater.inflate(R.layout.layout_bubble, null)

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
        mWindowManager?.addView(mBubbleView, params)

        //Set the close button.
        val closeButton = mBubbleView?.findViewById<View>(R.id.bubble_close)
        closeButton?.setOnClickListener { //
            stopSelf()
        }

        val card = mBubbleView?.findViewById<CardView>(R.id.card)
        card?.setOnTouchListener(
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
                                mWindowManager?.updateViewLayout(mBubbleView, params)
                                lastAction = event.action
                                return true
                            }
                        }
                        return false
                    }
                }
        )
    }

    override fun onDestroy() {
        mWakeLock?.release()
        stopForeground(true)
        if (mBubbleView != null) mWindowManager?.removeView(mBubbleView)
        super.onDestroy()
    }
}