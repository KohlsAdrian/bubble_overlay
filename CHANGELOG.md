# Changelog

## 0.1.4+0

* Check [this merged pull request](https://github.com/KohlsAdrian/bubble_overlay/pull/6) by [0Franky](https://github.com/0Franky)

## 0.1.3+0

* Removed unused iOS resources and folder
* Added local file playback [use openVideoBubbleAsset(assetString)]

## 0.1.2+0

* Added video playback support, String Uri parsing

## 0.1.1+3

* Fixed iOS signing for offline use

## 0.1.1+2

* Fixed iOS signing for offline use

## 0.1.1+1

* Fixed 'No implementation found for method isBubbleOpen on channel com.adriankohls/bubble_overlay'

## 0.1.1

Fixed nullability crash on some devices due to memory/OS apps GC management
Added missing resource releasers to avoid memory leak

## 0.1.0+1

Flutter format

## 0.1.0

Fixed isOpen behaviour by checking if Android Service class is running periodically (1 second)
Added callback, and close with proper dispose to avoid memory leak

## 0.0.4+1

Fixed Readme

## 0.0.4

Added iOS project folder to make plugin runnable in Flutter project for Android and iOS. Remember, this plugin only works for Android platform

## 0.0.3

* Fixed close button layout
* Fixed bubble layouts
* Renamed title to topText
* Fixed bubble text sizes
* Added Timer callback, for custom inside bubble functions
* Fixed some access to object

## 0.0.2

* Removed close bubble on tapping on bubble, close bubble overlay only tapping on X
* Added top and bottom text to bubble
* Fixed title, middle and bottom text size

## 0.0.1+0

* Initial release

## 0.0.1+1

* Initial release

## 0.0.1+2

* Initial release

## 0.0.1+3

* Initial release

## 0.0.1+4

* Initial release

## 0.0.1+5

* Initial release

## 0.0.1+6

* Initial release
