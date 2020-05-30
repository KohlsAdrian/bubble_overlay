#import "BubbleOverlayPlugin.h"
#if __has_include(<bubble_overlay/bubble_overlay-Swift.h>)
#import <bubble_overlay/bubble_overlay-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "bubble_overlay-Swift.h"
#endif

@implementation BubbleOverlayPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBubbleOverlayPlugin registerWithRegistrar:registrar];
}
@end
