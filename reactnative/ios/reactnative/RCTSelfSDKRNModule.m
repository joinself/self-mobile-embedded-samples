//
//  RCTSelfSDKRNModule.m
//  reactnative
//
//  Created by DO HAI VU on 11/3/24.
//

#import "RCTSelfSDKRNModule.h"

@implementation RCTSelfSDKRNModule

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(getSelfId: (RCTResponseSenderBlock)callback)
{
  NSInteger eventId = 123;
  callback(@[@(eventId)]);
}

bool hasListeners;
-(NSArray<NSString *> *)supportedEvents {
  return @[@"EventSelfId"];
}

// Will be called when this module's first listener is added.
-(void)startObserving {
  hasListeners = YES;
  // Set up any upstream listeners or background tasks as necessary
}

// Will be called when this module's last listener is removed, or on dealloc.
-(void)stopObserving {
  hasListeners = NO;
  // Remove upstream listeners, stop unnecessary background tasks
}

- (void)calendarEventReminderReceived:(NSNotification *)notification
{
  if (hasListeners) {// Only send events if anyone is listening
    [self sendEventWithName:@"EventSelfId" body:@{@"selfId": @"1234567"}];
  }
}
@end
