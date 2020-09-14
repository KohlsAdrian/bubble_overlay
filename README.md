# Android only feature

## Getting Started

What you need to include in your project to start working:

* Open your AndroidManifest.xml and add inside `application` tag:

        <service
            android:name="com.adriankohls.bubble_overlay.BubbleOverlayService"
            android:enabled="true"
            android:exported="false" />
        <service
            android:name="com.adriankohls.bubble_overlay.BubbleVideoOverlayService"
            android:enabled="true"
            android:exported="false" />

And add this permissions at `manifest` top level:

        <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
        <uses-permission android:name="android.permission.WAKE_LOCK" />

Create a local variable in your `dart` file:

        final BubbleOverlay bubbleOverlay = BubbleOverlay();
        bubbleOverlay.openBubble();
        bubbleOverlay.openVideoBubble(uriString)

The first time you call openBubble()/openVideoBubble(), it will request Android overlay permission

After granting permission, call it again, and it will open the bubble

More details you can find in example/lib/main.dart

## How it works

BubbleOverlayPlugin.kt:
    Make the 'talk' with Dart/Flutter with native Android OS, connect the OS with the Service, etc.

BubbleOverlayService.kt
    Updates Bubble data, all bubble data is parsed from XML file (layout_bubble.xml), as developing a native app for Android with XML

BubbleVideoOverlayService.kt
    Updates Bubble  Video data, all bubble data is parsed from XML file (layout_video_bubble.xml), as developing a native app for Android with XML

## Google Play Demo

Link: <https://play.google.com/store/apps/details?id=com.adriankohls.bubble_overlay_example>
