/********* Echo.h Cordova Plugin Header *******/

#import <Cordova/CDVPlugin.h>
#import <StarIO/SMPort.h>
#import "PrinterFunctions.h"

@interface StarPrint : CDVPlugin{
	NSString *ip_address;
	NSString *image_to_print;
	NSString *paper;
}

- (void)printReceipt:(CDVInvokedUrlCommand*)command;
- (void)findPrinters:(CDVInvokedUrlCommand*)command;
@end
