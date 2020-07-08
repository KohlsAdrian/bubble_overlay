#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint bubble_overlay.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'bubble_overlay'
  s.version          = '0.0.1'
  s.summary          = 'A new flutter plugin project.'
  s.description      = 'A new flutter plugin project'
  s.homepage         = 'https://github.com/KohlsAdrian/bubble_overlay'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Adrian Kohls' => 'adriankohls95@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '8.0'

  # Flutter.framework does not contain a i386 slice. Only x86_64 simulators are supported.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
  s.swift_version = '5.0'
end
