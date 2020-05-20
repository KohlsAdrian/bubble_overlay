# bubble_overlay

Android only feature

## Getting Started

This plugin is very start project

What you need to include in your project to start working:

* Open your AndroidManifest.xml and add inside <application> tag:

        <service
            android:name="com.adriankohls.bubble_overlay.BubbleOverlayService"
            android:enabled="true"
            android:exported="false" />

Create a local variable:
    
    final BubbleOverlay bubbleOverlay = BubbleOverlay();

First open the bubble passing all first option parameters (null if all empty):
Order: Center Text, Text Color, Bubble Color, top image, bottom image

    bubbleOverlay.openBubble(null, '#000000', '#ffffff', null, null);

How to use in Flutter:
You can call single functions to change Text, Colors and images

    class _Home extends State<Home> {
      final BubbleOverlay bubbleOverlay = BubbleOverlay();
      int time = 0;
      bool alternateColor = false;
      Timer timer;
    
      void updateTextCounter() {
        timer?.cancel();
        time = 0;
        timer = Timer.periodic(Duration(milliseconds: 500), (timer) {
          time++;
          if (bubbleOverlay.isOpened) bubbleOverlay.updateBubbleText('$time');
        });
      }
    
      void updateTextHelloWorld() {
        timer?.cancel();
        if (bubbleOverlay.isOpened) bubbleOverlay.updateBubbleText('Hello World');
      }
    
      void closeBubble() {
        timer?.cancel();
        if (bubbleOverlay.isOpened) bubbleOverlay.closeBubble();
      }
    
      void updateTextAndBgColor() {
        if (bubbleOverlay.isOpened) {
          bubbleOverlay.updateBubbleColor(alternateColor ? '#ffffff' : '#000000');
          bubbleOverlay.updateTextColor(alternateColor ? '#000000' : '#ffffff');
          alternateColor = !alternateColor;
        }
      }
    
      @override
      Widget build(BuildContext context) {
        return Scaffold(
          appBar: AppBar(title: Text('Bubble Overlay')),
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                RaisedButton(
                  child: Text('Open Bubble Overlay'),
                  onPressed: () {
                    bubbleOverlay.openBubble(
                      null,
                      '#000000',
                      '#ffffff',
                      null,
                      null,
                    );
                  },
                ),
                RaisedButton(
                  onPressed: closeBubble,
                  child: Text('Close Bubble'),
                ),
                RaisedButton(
                  onPressed: updateTextHelloWorld,
                  child: Text('Set "Hello World"'),
                ),
                RaisedButton(
                  onPressed: updateTextCounter,
                  child: Text('Update text counter'),
                ),
                RaisedButton(
                  onPressed: updateTextAndBgColor,
                  child: Text('Update text and background color'),
                ),
              ],
            ),
          ),
        );
      }
    }


