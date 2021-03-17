import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

class BubbleOverlay {
  bool _isOpen = false;
  bool _isVideoOpen = false;
  Timer? _callback;
  Timer? _timer;
  Timer? _timerVideo;

  bool get isOpen => _isOpen;
  bool get isVideoOpen => _isVideoOpen;
  Timer? get callback => _callback;

  BubbleOverlay() {
    if (!Platform.isAndroid)
      throw PlatformException(
          code: 'Bubble overlay only available for Android');
  }

  static const _platform =
      const MethodChannel('com.adriankohls/bubble_overlay');

  ///Start Bubble service and show the bubble, hiding the app, with optional
  ///[topText], [middleText], [bottomText],
  ///[topTextColor], [middleTextColor], [bottomTextColor],
  ///[backgroundColor], [topIconAsset], [bottomIconAsset] and [callback]
  void openBubble({
    String topText = '',
    String middleText = '',
    String bottomText = '',
    String topTextColor = '#000000',
    String middleTextColor = '#000000',
    String bottomTextColor = '#000000',
    String backgroundColor = '#ffffff',
    String? topIconAsset,
    String? bottomIconAsset,
    Timer? callback,
  }) async {
    var bytesTop = topIconAsset == null
        ? null
        : (await rootBundle.load(topIconAsset)).buffer.asUint8List();
    var bytesBottom = bottomIconAsset == null
        ? null
        : (await rootBundle.load(bottomIconAsset)).buffer.asUint8List();
    _platform.invokeMethod('openBubble', [
      topText,
      middleText,
      bottomText,
      topTextColor,
      middleTextColor,
      bottomTextColor,
      backgroundColor,
      bytesTop,
      bytesBottom,
    ]);
    setCallback(callback);

    ///Creates [_timer] to check periodically if
    ///bubble [isOpen] if Service is bounded, [true] if bounded,
    ///[false] otherwise
    _timer = Timer.periodic(Duration(seconds: 1), (timer) async {
      _isOpen = await _platform.invokeMethod('isBubbleOpen') ?? false;
      if (!_isOpen) {
        timer.cancel();
      }
    });
  }

  /// Start Video Bubble service and show the bubble
  /// [path] specifies the uri of the file
  /// [startTimeInMilliseconds] specify where to start watching the video in milliseconds
  /// [controlsType] type of (ui) controls to show on the mini video player (all type avaiable in the enum ControlsType)
  /// [onEndServiceTask] callback to call after end of the service. Useful for setting a new time on the main media player
  void openVideoBubble(String path,
      {int startTimeInMilliseconds = 0,
      ControlsType controlsType = ControlsType.STANDARD,
      onEndServiceTask}) async {
    // To call from native android after service close.
    _platform.setMethodCallHandler((MethodCall call) async {
      print('_handleCloseService method called');
      switch (call.method) {
        case "getCurrentTime":
          print('From Native====');
          print(call.arguments.toString());
          sendCurrentTimeToCallback(
              onEndServiceTask,
              call.arguments["isCurrentTimeDirty"].toLowerCase() == "true",
              int.parse(call.arguments["currentTime"]));
      }
    });

    bool _seekAtStart = startTimeInMilliseconds == 0 ? true : false;
    _platform.invokeMethod('openVideoBubble', [
      path,
      _seekAtStart,
      startTimeInMilliseconds,
      controlsType
          .toString()
          .substring(controlsType.toString().lastIndexOf(".") + 1)
    ]);

    ///Creates [_timerVideo] to check periodically if
    ///bubble [_isVideoOpen] if Service is bounded, [true] if bounded,
    ///[false] otherwise
    _timerVideo = Timer.periodic(Duration(seconds: 1), (timer) async {
      _isVideoOpen = await _platform.invokeMethod('isVideoBubbleOpen') ?? false;
      if (!_isVideoOpen) {
        _timerVideo?.cancel();
      }
    });
  }

  /// Send to callback (if setted) new current if is modified
  Future<void> sendCurrentTimeToCallback(
      seekFunction, bool isCurrentTimeDirty, int currentTime) async {
    print("seekFunction!!!!!!");
    if (isCurrentTimeDirty) {
      print("seekFunction to " + currentTime.toString());
      if (seekFunction != null) {
        seekFunction(currentTime);
      }
    }
  }

  ///Start Video Bubble service and show the bubble
  /// [asset] specifies the uri of the file
  /// [startTimeInMilliseconds] specify where to start watching the video in milliseconds
  /// [controlsType] type of (ui) controls to show on the mini video player (all type avaiable in the enum ControlsType)
  /// [onEndServiceTask] callback to call after end of the service. Useful for setting a new time on the main media player
  void openVideoBubbleAsset(String asset,
      {int startTimeInMilliseconds = 0,
      ControlsType controlsType = ControlsType.STANDARD,
      onEndServiceTask}) async {
    ByteData data = await rootBundle.load(asset);
    List<int> bytes =
        data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes);
    Directory directory = await getApplicationDocumentsDirectory();
    String dbPath = join(directory.path, 'video.mp4');
    File file = await File(dbPath).writeAsBytes(bytes);
    String path = file.path;
    openVideoBubble(path,
        startTimeInMilliseconds: startTimeInMilliseconds,
        controlsType: controlsType,
        onEndServiceTask: onEndServiceTask);
  }

  ///Add custom service inside bubble, usually used for
  ///Timer.peridoc automated calls
  void setCallback(Timer? callback) => _callback = callback;

  ///Removes custom service inside bubble
  void removeCallback() {
    if (_isOpen) {
      _callback?.cancel();
      _callback = null;
    } else
      throw Exception('Bubble not running');
  }

  ///Stop Bubble service and close the bubble
  void closeBubble() {
    if (_isOpen) {
      removeCallback();
      _platform.invokeMethod('closeBubble');
      _timer?.cancel();
      _isOpen = false;
    } else
      throw Exception('Bubble not running');
  }

  ///Stop Video Bubble service and close the bubble
  void closeVideoBubble() {
    if (_isVideoOpen) {
      _platform.invokeMethod('closeVideoBubble');
      _timerVideo?.cancel();
      _isVideoOpen = false;
    } else
      throw Exception('Bubble Video not running');
  }

  ///Updates bubble [topIcon] with String asset image
  Future<void> updateTopIcon(String assetImage) async {
    if (_isOpen) {
      var bytes = (await rootBundle.load(assetImage)).buffer.asUint8List();
      _platform.invokeMethod('updateBubbleTopIcon', bytes);
    } else
      throw Exception('Bubble not running');
  }

  ///Updates bubble [bottomIcon] with String asset image
  Future<void> updateBottomIcon(String assetImage) async {
    if (_isOpen) {
      var bytes = (await rootBundle.load(assetImage)).buffer.asUint8List();
      _platform.invokeMethod('updateBubbleBottomIcon', bytes);
    } else
      throw Exception('Bubble not running');
  }

  ///Updates bubble [topIcon] with bytes
  Future<void> updateTopIconWithBytes(Uint8List bytes) async => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleTopIcon', bytes);

  ///Updates bubble [bottomIcon] with bytes
  Future<void> updateBottomIconWithBytes(Uint8List bytes) async => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleBottomIcon', bytes);

  ///Updates bubble [topText]
  void updateTopText(String text) => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleTitle', text);

  ///Updates bubble [topTextColor]
  void updateTopTextColor(String text) => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleTitleColor', text);

  ///Updates bubble [middleText]
  void updateMiddleText(String text) => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleText', text);

  ///Updates bubble [middleTextColor]
  void updateMiddleTextColor(String textColor) => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleTextColor', textColor);

  ///Updates bubble [bottomText]
  void updateBottomText(String text) => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleBottomText', text);

  ///Updates bubble [bottomTextColor]
  void updateBottomTextColor(String text) => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleBottomTextColor', text);

  ///Updates bubble [backgroundColor]
  void updateBackgroundColor(String bubbleColor) => !_isOpen
      ? throw Exception('Bubble not running')
      : _platform.invokeMethod('updateBubbleColor', bubbleColor);
}

enum ControlsType { STANDARD, MINIMAL }
