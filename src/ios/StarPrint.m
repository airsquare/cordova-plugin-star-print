/********* Echo.m Cordova Plugin Implementation *******/

#import "StarPrint.h"
#import <Cordova/CDVPlugin.h>

@implementation StarPrint

- (void)printReceipt:(CDVInvokedUrlCommand*)command
{
	// run in a new thread
	[self.commandDelegate runInBackground:^{

		//get params
 		ip_address = [command.arguments objectAtIndex:0];
 		image_to_print = [command.arguments objectAtIndex:1];
 		paper = [command.arguments objectAtIndex:2];
    
        NSMutableString* message = [NSMutableString stringWithString:@""];

        // convert to a UIImage
        NSData *data = [[NSData alloc]initWithBase64EncodedString:image_to_print options:NSDataBase64DecodingIgnoreUnknownCharacters];
        UIImage *image_ready = [UIImage imageWithData:data];
        int max_paper_width;
        if ([paper isEqualToString:@"3 Inch"]) {
            max_paper_width = 576;
        }
        else if([paper isEqualToString:@"4 Inch"]) {
            max_paper_width = 832;
        }
        else {
            max_paper_width = 576;
        }
        int result = [PrinterFunctions PrintImageWithPortname:ip_address
                                                portSettings:@""
                                                imageToPrint:image_ready
                                                maxWidth:max_paper_width
                                                compressionEnable:NO
                                                withDrawerKick:NO];
        CDVPluginResult* pluginResult = nil;
        if(result == 0) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"print_success"];
        }
        else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"There was a problem while printing the receipt."];
        }
 		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];  

    }];
}
- (void)findPrinters:(CDVInvokedUrlCommand*)command{
	// run in a new thread
	[self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil;
        NSArray *discoveredDevices = nil;
        NSMutableArray *found_printers = [[NSMutableArray alloc]init];

        discoveredDevices = [[SMPort searchPrinter] retain];

        for (PortInfo *port in discoveredDevices){
            NSDictionary *jsonObj = [ [NSDictionary alloc]
                       initWithObjectsAndKeys :
                         @"Star", @"brand",
                         [port portName], @"target",
                         [port modelName], @"printer_name",
                         [port macAddress], @"mac",
                         nil
                    ];
            [found_printers addObject:jsonObj];

        }

        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:found_printers];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId]; 
    }];
}

@end