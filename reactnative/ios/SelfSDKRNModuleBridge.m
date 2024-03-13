//
//  SelfSDKRNModuleBridge.m
//  reactnative
//
//  Created by DO HAI VU on 13/3/24.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(SelfSDKRNModule2, RCTEventEmitter)

RCT_EXTERN_METHOD(getSelfId:(RCTResponseSenderBlock *)callback)

@end
