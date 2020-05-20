import 'dart:io';
import 'package:flutter/services.dart';

class BubbleOverlay {
  bool isOpened = false;
  BubbleOverlay() {
    if (!Platform.isAndroid)
      throw PlatformException(
          code: 'Bubble overlay only available for Android');
  }

  static const _platform =
  const MethodChannel('com.adriankohls/bubble_overlay');

  void openBubble(
      String title,
      String customText,
      String bottomText,
      String titleColor,
      String customTextColor,
      String bottomTextColor,
      String backgroundColor,
      String topIconAsset,
      String bottomIconAsset,
      ) async {
    var bytesTop = topIconAsset == null
        ? null
        : (await rootBundle.load(topIconAsset)).buffer.asUint8List();
    var bytesBottom = bottomIconAsset == null
        ? null
        : (await rootBundle.load(bottomIconAsset)).buffer.asUint8List();
    _platform.invokeMethod('openBubble', [
      title,
      customText,
      bottomText,
      titleColor,
      customTextColor,
      bottomTextColor,
      backgroundColor,
      bytesTop,
      bytesBottom,
    ]);
    isOpened = true;
  }

  void closeBubble() {
    _platform.invokeMethod('closeBubble');
    isOpened = false;
  }

  void updateTitle(String text) =>
      _platform.invokeMethod('updateBubbleTitle', text);

  void updateTitleColor(String text) =>
      _platform.invokeMethod('updateBubbleTitleColor', text);

  void updateBottomText(String text) =>
      _platform.invokeMethod('updateBubbleBottomText', text);

  void updateBottomTextColor(String text) =>
      _platform.invokeMethod('updateBubbleBottomTextColor', text);

  void updateText(String text) =>
      _platform.invokeMethod('updateBubbleText', text);

  void updateTextColor(String textColor) =>
      _platform.invokeMethod('updateBubbleTextColor', textColor);

  void updateColor(String bubbleColor) =>
      _platform.invokeMethod('updateBubbleColor', bubbleColor);

  Future<void> updateTopIcon(String assetImage) async {
    var bytes = (await rootBundle.load(assetImage)).buffer.asUint8List();
    _platform.invokeMethod('updateBubbleTopIcon', bytes);
  }

  Future<void> updateBottomIcon(String assetImage) async {
    var bytes = (await rootBundle.load(assetImage)).buffer.asUint8List();
    _platform.invokeMethod('updateBubbleBottomIcon', bytes);
  }
}
