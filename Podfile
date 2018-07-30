source 'https://github.com/CocoaPods/Specs.git'

project 'SecureImage.xcodeproj'
platform :ios, '9.0'
use_frameworks!

target 'SecureImage' do
#    pod 'SingleSignOn', :path => '../sso-ios'
    pod 'SingleSignOn', :git => 'https://github.com/bcgov/mobile-authentication-ios.git', :tag => 'v1.0.4'
    pod 'RealmSwift'
    pod 'SwiftKeychainWrapper'
    pod 'Alamofire'
end

target 'SecureImageTests' do
    pod 'RealmSwift'
    pod 'SwiftKeychainWrapper'
    pod 'Alamofire'
end
