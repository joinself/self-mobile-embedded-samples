//
//  Use this file to import your target's public headers that you would like to expose to Swift.
//

//
//  Use this file to import your target's public headers that you would like to expose to Swift.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(SelfSDKRNModule2, RCTEventEmitter)

RCT_EXTERN_METHOD(getSelfId:(RCTResponseSenderBlock *)callback)

@end
