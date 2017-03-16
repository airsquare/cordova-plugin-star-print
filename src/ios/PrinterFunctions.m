//
//  PrinterFunctions.m
//  IOS_SDK
//
//  Created by Tzvi on 8/2/11.
//  Copyright 2011 - 2013 STAR MICRONICS CO., LTD. All rights reserved.
//

#import "PrinterFunctions.h"
#import <StarIO/SMPort.h>
#import <StarIO/SMBluetoothManager.h>
#import "RasterDocument.h"
#import "StarBitmap.h"
#import <sys/time.h>
#import <unistd.h>

@implementation PrinterFunctions

#pragma mark Get Firmware Version

/*!
 *  This function shows the printer firmware information
 *
 *  @param  portName        Port name to use for communication
 *  @param  portSettings    Set following settings
 *                          - Desktop USB Printer + Apple AirPort: @"9100" - @"9109" (Port Number)
 *                          - Portable Printer (Star Line Mode)  : @"portable"
 *                          - Others                             : @"" (blank)
 */

#pragma mark Check whether supporting bluetooth setting

+ (NSInteger)hasBTSettingSupportWithPortName:(NSString *)portName
                                portSettings:(NSString *)portSettings
{
    // Check Interface
    if ([portName.uppercaseString hasPrefix:@"BLE:"]) {
        return 0;
    }
    
    if ([portName.uppercaseString hasPrefix:@"BT:"] == NO) {
        return 1;
    }
    
    // Check firmware version
    SMPort *port = nil;
    NSDictionary *dict = nil;
    @try {
        port = [SMPort getPort:portName :portSettings :10000];
        if (port == nil) {
            return 2;
        }
        
        dict = [port getFirmwareInformation];
    }
    @catch (NSException *e) {
        return 2;
    }
    @finally {
        [SMPort releasePort:port];
    }
    
    NSString *modelName = dict[@"ModelName"];
    if ([modelName hasPrefix:@"SM-S21"] ||
        [modelName hasPrefix:@"SM-S22"] ||
        [modelName hasPrefix:@"SM-T30"] ||
        [modelName hasPrefix:@"SM-T40"]) {
        
        NSString *fwVersionStr = dict[@"FirmwareVersion"];
        float fwVersion = fwVersionStr.floatValue;
        if (fwVersion < 3.0) {
            return 3;
        }
    }
    
    return 0;
}

#pragma mark Check Status

/*!
 *  This function checks the status of the printer.
 *  The check status function can be used for both portable and non portable printers.
 *
 *  @param  portName        Port name to use for communication. This should be (TCP:<IP Address>), (BT:<iOS Port Name>),
 *                          or (BLE:<Device Name>).
 *  @param  portSettings    Set following settings
 *                          - Desktop USB Printer + Apple AirPort: @"9100" - @"9109" (Port Number)
 *                          - Portable Printer (Star Line Mode)  : @"portable"
 *                          - Others                             : @"" (blank)
 *
 */
+ (void)CheckStatusWithPortname:(NSString *)portName
                   portSettings:(NSString *)portSettings
                  sensorSetting:(SensorActive)sensorActiveSetting
{
    SMPort *starPort = nil;
    @try
    {
        starPort = [SMPort getPort:portName :portSettings :10000];
        if (starPort == nil) {
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Fail to Open Port.\nRefer to \"getPort API\" in the manual."
                                                            message:@""
                                                           delegate:nil 
                                                  cancelButtonTitle:@"OK" 
                                                  otherButtonTitles:nil];
            [alert show];
            [alert release];
            return;
        }
        usleep(1000 * 1000);
        
        StarPrinterStatus_2 status;
        [starPort getParsedStatus:&status :2];
        
        NSString *message = @"";
        if (status.offline == SM_TRUE)
        {
            message = @"The printer is offline";
            if (status.coverOpen == SM_TRUE)
            {
                message = [message stringByAppendingString:@"\nCover is Open"];
            }
            else if (status.receiptPaperEmpty == SM_TRUE)
            {
                message = [message stringByAppendingString:@"\nOut of Paper"];
            }
        }
        else
        {
            message = @"The Printer is online";
        }

        NSString *drawerStatus;
        if (sensorActiveSetting == SensorActiveHigh)
        {
            drawerStatus = (status.compulsionSwitch == SM_TRUE) ? @"Open" : @"Close";
            message = [message stringByAppendingFormat:@"\nCash Drawer: %@", drawerStatus];
        }
        else if (sensorActiveSetting == SensorActiveLow)
        {
            drawerStatus = (status.compulsionSwitch == SM_FALSE) ? @"Open" : @"Close";
            message = [message stringByAppendingFormat:@"\nCash Drawer: %@", drawerStatus];
        }
        
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Printer Status"
                                                        message:message
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];

        [alert show];
        [alert release];
    }
    @catch (PortException *exception)
    {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Printer Error" 
                                                        message:@"Get status failed"
                                                       delegate:nil 
                                              cancelButtonTitle:@"OK" 
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
    }
    @finally 
    {
        [SMPort releasePort:starPort];
    }
}

#pragma mark 1D Barcode

/**
 *  This function is used to print bar codes in the 39 format
 *
 *  @param  portName        Port name to use for communication. This should be (TCP:<IP Address>), (BT:<iOS Port Name>),
 *                          or (BLE:<Device Name>).
 *  @param  portSettings    Set following settings
 *                          - Desktop USB Printer + Apple AirPort: @"9100" - @"9109" (Port Number)
 *                          - Portable Printer (Star Line Mode)  : @"portable"
 *                          - Others                             : @"" (blank)
 *  @param  barcodeData     These are the characters that will be printed in the bar code. The characters available for
 *                          this bar code are listed in section 3-43 (Rev. 1.12).
 *  @param  barcodeDataSize This is the number of characters in the barcode.  This should be the size of the preceding
 *                          parameter
 *  @param  option          This tell the printer weather put characters under the printed bar code or not.  This may
 *                          also be used to line feed after the bar code is printed.
 *  @param  height          The height of the bar code.  This is measured in pixels
 *  @param  width           The Narrow wide width of the bar code.  This value should be between 1 to 9.  See section
 *                          3-42 (Rev. 1.12) for more information on the values.
 */
+ (void)PrintCode39WithPortname:(NSString *)portName
                   portSettings:(NSString *)portSettings
                    barcodeData:(unsigned char *)barcodeData
                barcodeDataSize:(unsigned int)barcodeDataSize
                 barcodeOptions:(BarCodeOptions)option
                         height:(unsigned char)height
                     narrowWide:(NarrowWide)width
{
    unsigned char n1 = 0x34;
    unsigned char n2 = 0;
    switch (option) {
        case No_Added_Characters_With_Line_Feed:
            n2 = 49;
            break;
        case Adds_Characters_With_Line_Feed:
            n2 = 50;
            break;
        case No_Added_Characters_Without_Line_Feed:
            n2 = 51;
            break;
        case Adds_Characters_Without_Line_Feed:
            n2 = 52;
            break;
    }
    unsigned char n3 = 0;
    switch (width)
    {
        case NarrowWide_2_6:
            n3 = 49;
            break;
        case NarrowWide_3_9:
            n3 = 50;
            break;
        case NarrowWide_4_12:
            n3 = 51;
            break;
        case NarrowWide_2_5:
            n3 = 52;
            break;
        case NarrowWide_3_8:
            n3 = 53;
            break;
        case NarrowWide_4_10:
            n3 = 54;
            break;
        case NarrowWide_2_4:
            n3 = 55;
            break;
        case NarrowWide_3_6:
            n3 = 56;
            break;
        case NarrowWide_4_8:
            n3 = 57;
            break;
    }
    unsigned char n4 = height;
    
    unsigned char *command = (unsigned char *)malloc(6 + barcodeDataSize + 1);
    command[0] = 0x1b;
    command[1] = 0x62;
    command[2] = n1;
    command[3] = n2;
    command[4] = n3;
    command[5] = n4;
    for (int index = 0; index < barcodeDataSize; index++)
    {
        command[index + 6] = barcodeData[index];
    }
    command[6 + barcodeDataSize] = 0x1e;
    
    int commandSize = 6 + barcodeDataSize + 1;
    
    NSData *dataToSentToPrinter = [[NSData alloc] initWithBytes:command length:commandSize];
    
    [self sendCommand:dataToSentToPrinter portName:portName portSettings:portSettings timeoutMillis:10000];
    
    [dataToSentToPrinter release];
    free(command);
}
#pragma mark common

/**
 * This function is used to print a UIImage directly to the printer.
 * There are 2 ways a printer can usually print images, one is through raster commands the other is through line mode
 * commands.
 * This function uses raster commands to print an image. Raster is support on the tsp100 and all legacy thermal
 * printers. The line mode printing is not supported by the TSP100 so its not used
 *
 *  @param  portName        Port name to use for communication. This should be (TCP:<IP Address>), (BT:<iOS Port Name>),
 *                          or (BLE:<Device Name>).
 *  @param  portSettings    Set following settings
 *                          - Desktop USB Printer + Apple AirPort: @"9100" - @"9109" (Port Number)
 *                          - Portable Printer (Star Line Mode)  : @"portable"
 *                          - Others                             : @"" (blank)
 *  @param  source          The uiimage to convert to star raster data
 *  @param  maxWidth        The maximum with the image to print. This is usually the page with of the printer. If the
 *                          image exceeds the maximum width then the image is scaled down. The ratio is maintained.
 */
+ (int)PrintImageWithPortname:(NSString *)portName
                  portSettings:(NSString *)portSettings
                  imageToPrint:(UIImage *)imageToPrint
                      maxWidth:(int)maxWidth
             compressionEnable:(BOOL)compressionEnable
                withDrawerKick:(BOOL)drawerKick
{
    NSMutableData *commandsToPrint = [NSMutableData new];
    
    StarBitmap *starbitmap = [[StarBitmap alloc] initWithUIImage:imageToPrint :maxWidth :false];
    
    RasterDocument *rasterDoc = [[RasterDocument alloc] initWithDefaults:RasSpeed_Medium endOfPageBehaviour:RasPageEndMode_FeedAndFullCut endOfDocumentBahaviour:RasPageEndMode_FeedAndFullCut topMargin:RasTopMargin_Standard pageLength:0 leftMargin:0 rightMargin:0];
    
    NSData *shortcommand = [rasterDoc BeginDocumentCommandData];
    [commandsToPrint appendData:shortcommand];
    
    shortcommand = [starbitmap getImageDataForPrinting:compressionEnable];
    [commandsToPrint appendData:shortcommand];
    
    shortcommand = [rasterDoc EndDocumentCommandData];
    [commandsToPrint appendData:shortcommand];
    
    [rasterDoc release];

    
    [starbitmap release];
    
    // Kick Cash Drawer
    if (drawerKick == YES) {
        [commandsToPrint appendBytes:"\x07"
                              length:sizeof("\x07") - 1];
    }
    
    int result = [self sendCommand:commandsToPrint portName:portName portSettings:portSettings timeoutMillis:10000];

    [commandsToPrint release];

    return result;
}

+ (int)sendCommand:(NSData *)commandsToPrint
           portName:(NSString *)portName
       portSettings:(NSString *)portSettings
      timeoutMillis:(u_int32_t)timeoutMillis
{
    int commandSize = (int)commandsToPrint.length;
    unsigned char *dataToSentToPrinter = (unsigned char *)malloc(commandSize);
    [commandsToPrint getBytes:dataToSentToPrinter length:commandSize];
    
    SMPort *starPort = nil;
    @try
    {
        starPort = [SMPort getPort:portName :portSettings :timeoutMillis];
        if (starPort == nil)
        {
            return 1;
        }
        
        StarPrinterStatus_2 status;
        [starPort beginCheckedBlock:&status :2];
        if (status.offline == SM_TRUE) {
            return 2;
        }
        
        struct timeval endTime;
        gettimeofday(&endTime, NULL);
        endTime.tv_sec += 30;
        
        int totalAmountWritten = 0;
        while (totalAmountWritten < commandSize)
        {
            int remaining = commandSize - totalAmountWritten;
            int amountWritten = [starPort writePort:dataToSentToPrinter :totalAmountWritten :remaining];
            totalAmountWritten += amountWritten;
            
            struct timeval now;
            gettimeofday(&now, NULL);
            if (now.tv_sec > endTime.tv_sec)
            {
                break;
            }
        }
        
        if (totalAmountWritten < commandSize) {
            return 3;
        }
        
        starPort.endCheckedBlockTimeoutMillis = 30000;
        [starPort endCheckedBlock:&status :2];
        if (status.offline == SM_TRUE) {
            return 4;
        }
    }
    @catch (PortException *exception)
    {
        return 5;
    }
    @finally
    {
        free(dataToSentToPrinter);
        [SMPort releasePort:starPort];
    }
    return 0;
}


#pragma mark diconnect bluetooth

+ (void)disconnectPort:(NSString *)portName
          portSettings:(NSString *)portSettings
               timeout:(u_int32_t)timeout
{
    SMPort *port = [SMPort getPort:portName :portSettings :timeout];
    if (port == nil) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Fail to Open Port.\nRefer to \"getPort API\" in the manual."
                                                        message:@""
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
        return;
    }
    
    BOOL result = [port disconnect];
    if (result == NO) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Fail to Disconnect"
                                                        message:@""
                                                       delegate:nil
                                              cancelButtonTitle:nil
                                              otherButtonTitles:@"OK", nil];
        [alert show];
        [alert release];
        return;
    }
    
    [SMPort releasePort:port];
}

@end
