Android only feature
This plugin is very start project

## Getting Started

What you need to include in your project to start working:

* Open your AndroidManifest.xml and add inside <application> tag add this:

        <service
            android:name="com.adriankohls.bubble_overlay.BubbleOverlayService"
            android:enabled="true"
            android:exported="false" />

And add this permissions:
    
        <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
        <uses-permission android:name="android.permission.WAKE_LOCK" />

Create a local variable:
    
    final BubbleOverlay bubbleOverlay = BubbleOverlay();
    bubbleOverlay.openBubble();

The first time you call openBubble(), it will request Android overlay permission
After granting permission, call it again, and it will open the bubble

More details you can find in example/lib/main.dart

## How it works

BubbleOverlayPlugin.kt:
    Make the 'talk' with Dart/Flutter with native Android OS, connect the OS with the Service, etc.

BubbleOverlayService.kt
    Updates Bubble data, all bubble data is parsed from XML file (layout_bubble.xml), as developing a native app for Android with XML