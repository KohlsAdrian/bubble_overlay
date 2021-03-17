import 'dart:async';
import 'dart:developer';

import 'package:bubble_overlay/bubble_overlay.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

Future<void> main() async {
  runApp(MaterialApp(
    home: Home(),
    routes: <String, WidgetBuilder>{
      '/home': (BuildContext context) => new Home(),
    },
  ));
}

class Home extends StatefulWidget {
  _Home createState() => _Home();
}

class _Home extends State<Home> {
  final BubbleOverlay bubbleOverlay = BubbleOverlay();
  bool alternateColor = false;

  void setMiddleTextCounter() {
    int time = 0;
    if (bubbleOverlay.isOpen)
      bubbleOverlay
          .setCallback(Timer.periodic(Duration(milliseconds: 500), (timer) {
        time++;
        if (bubbleOverlay.isOpen) bubbleOverlay.updateMiddleText('$time');
        log('callback');
      }));
  }

  void setBottomTextHelloWorld() {
    if (bubbleOverlay.isOpen) {
      bubbleOverlay.removeCallback();
      bubbleOverlay.updateBottomText('Hello World');
    }
  }

  void closeBubble() {
    if (bubbleOverlay.isOpen) {
      bubbleOverlay.closeBubble();
    }
  }

  void closeVideoBubble() {
    if (bubbleOverlay.isVideoOpen) {
      bubbleOverlay.closeVideoBubble();
    }
  }

  void updateTextAndBgColor() {
    if (bubbleOverlay.isOpen) {
      bubbleOverlay.removeCallback();
      String textColor = alternateColor ? '#000000' : '#ffffff';
      String bgColor = alternateColor ? '#ffffff' : '#000000';
      bubbleOverlay.updateMiddleTextColor(textColor);
      bubbleOverlay.updateTopTextColor(textColor);
      bubbleOverlay.updateBottomTextColor(textColor);
      bubbleOverlay.updateBackgroundColor(bgColor);
      alternateColor = !alternateColor;
    }
  }

  void setTopText() {
    if (bubbleOverlay.isOpen) {
      bubbleOverlay.removeCallback();
      bubbleOverlay.updateTopText('Set Bubble Title');
    }
  }

  void setTopIcon() async {
    String url =
        'https://meterpreter.org/wp-content/uploads/2018/09/flutter.png';
    http.get(Uri.parse(url)).then(
        (response) => bubbleOverlay.updateTopIconWithBytes(response.bodyBytes));
  }

  void setBottomIcon() async {
    String url =
        'https://github.githubassets.com/images/modules/open_graph/github-mark.png';
    http.get(Uri.parse(url)).then((response) =>
        bubbleOverlay.updateBottomIconWithBytes(response.bodyBytes));
  }

  void openBubbleVideo() async {
    String url =
        'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4';
    bubbleOverlay.openVideoBubble(url,
        startTimeInMilliseconds: 15000, controlsType: ControlsType.MINIMAL);
  }

  void openBubbleVideoAssets() async {
    String asset;
    // asset = 'assets/video.mp4';
    asset = 'assets/video_vertical.mp4';
    bubbleOverlay.openVideoBubbleAsset(asset,
        controlsType: ControlsType.MINIMAL);
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: Text('Bubble Overlay')),
        body: SingleChildScrollView(
          padding: EdgeInsets.all(50),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              ElevatedButton(
                  child: Text('Open Bubble Overlay'),
                  onPressed: () => bubbleOverlay.openBubble()),
              ElevatedButton(
                onPressed: setTopText,
                child: Text('Set Bubble Top Text'),
              ),
              ElevatedButton(
                onPressed: setMiddleTextCounter,
                child: Text('Set Middle Text Counter'),
              ),
              ElevatedButton(
                onPressed: setBottomTextHelloWorld,
                child: Text('Set Bottom Text "Hello World"'),
              ),
              ElevatedButton(
                onPressed: updateTextAndBgColor,
                child: Text('Update text and background color'),
              ),
              ElevatedButton(
                child: Text('Set Top Icon'),
                onPressed: setTopIcon,
              ),
              ElevatedButton(
                child: Text('Set Bottom Icon'),
                onPressed: setBottomIcon,
              ),
              ElevatedButton(
                child: Text('Open Bubble Video'),
                onPressed: openBubbleVideo,
              ),
              ElevatedButton(
                child: Text('Open Bubble Video Assets'),
                onPressed: openBubbleVideoAssets,
              ),
              ElevatedButton(
                child: Text('Is Bubble Open'),
                onPressed: () => print(bubbleOverlay.isOpen),
              ),
              ElevatedButton(
                child: Text('Is Video Open'),
                onPressed: () => print(bubbleOverlay.isVideoOpen),
              ),
              ElevatedButton(
                onPressed: closeBubble,
                child: Text('Close Bubble'),
              ),
              ElevatedButton(
                onPressed: closeVideoBubble,
                child: Text('Close Video Bubble'),
              ),
            ],
          ),
        ),
      );
}
