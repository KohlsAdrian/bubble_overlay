import 'dart:async';

import 'package:bubble_overlay/bubble_overlay.dart';
import 'package:flutter/material.dart';

Future<void> main() async {
  runApp(MaterialApp(home: Home()));
}

class Home extends StatefulWidget {
  _Home createState() => _Home();
}

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
      if (bubbleOverlay.isOpened) bubbleOverlay.updateText('$time');
    });
  }

  void updateTextHelloWorld() {
    timer?.cancel();
    if (bubbleOverlay.isOpened) bubbleOverlay.updateText('Hello World');
  }

  void closeBubble() {
    timer?.cancel();
    if (bubbleOverlay.isOpened) bubbleOverlay.closeBubble();
  }

  void updateTextAndBgColor() {
    if (bubbleOverlay.isOpened) {
      String textColor = alternateColor ? '#000000' : '#ffffff';
      String bgColor = alternateColor ? '#ffffff' : '#000000';
      bubbleOverlay.updateTextColor(textColor);
      bubbleOverlay.updateTitleColor(textColor);
      bubbleOverlay.updateBottomTextColor(textColor);
      bubbleOverlay.updateColor(bgColor);
      alternateColor = !alternateColor;
    }
  }

  void updateTitle() {
    bubbleOverlay.updateTitle('Set Bubble Title');
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
                onPressed: () => bubbleOverlay.openBubble(
                  'Bubble Overlay!',
                  'Bubble Opened',
                  'Bottom Text',
                  '#000000',
                  '#000000',
                  '#000000',
                  '#ffffff',
                  null,
                  null,
                )),
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
              onPressed: updateTitle,
              child: Text('Update Bubble Title'),
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
