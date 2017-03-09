package com.StarMicronics.StarIOSDK;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.StarMicronics.StarIOSDK.RasterDocument.RasPageEndMode;
import com.StarMicronics.StarIOSDK.RasterDocument.RasSpeed;
import com.StarMicronics.StarIOSDK.RasterDocument.RasTopMargin;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.stario.PortInfo;

import org.apache.cordova.CallbackContext;

public class PrinterFunctions {
    public enum NarrowWide {
        _2_6, _3_9, _4_12, _2_5, _3_8, _4_10, _2_4, _3_6, _4_8
    };

    public enum BarCodeOption {
        No_Added_Characters_With_Line_Feed, Adds_Characters_With_Line_Feed, No_Added_Characters_Without_Line_Feed, Adds_Characters_Without_Line_Feed
    }

    public enum Min_Mod_Size {
        _2_dots, _3_dots, _4_dots
    };

    public enum NarrowWideV2 {
        _2_5, _4_10, _6_15, _2_4, _4_8, _6_12, _2_6, _3_9, _4_12
    };

    public enum CorrectionLevelOption {
        Low, Middle, Q, High
    };

    public enum Model {
        Model1, Model2
    };

    public enum Limit {
        USE_LIMITS, USE_FIXED
    };

    public enum CutType {
        FULL_CUT, PARTIAL_CUT, FULL_CUT_FEED, PARTIAL_CUT_FEED
    };

    public enum Alignment {
        Left, Center, Right
    };

    public enum RasterCommand {
        Standard, Graphics
    };

    private static StarIOPort portForMoreThanOneFunction = null;

    private static int printableArea = 576; // for raster data

    /**
     * This function is used to print a PDF417 barcode to standard Star POS printers
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress>)
     * @param portSettings
     *     Should be blank
     * @param limit
     *     Selection of the Method to use specifying the barcode size. This is either 0 or 1. 0 is Use Limit method and 1 is Use Fixed method. See section 3-122 of the manual (Rev 1.12).
     * @param p1
     *     The vertical proportion to use. The value changes with the limit select. See section 3-122 of the manual (Rev 1.12).
     * @param p2
     *     The horizontal proportion to use. The value changes with the limit select. See section 3-122 of the manual (Rev 1.12).
     * @param securityLevel
     *     This represents how well the barcode can be recovered if it is damaged. This value should be 0 to 8.
     * @param xDirection
     *     Specifies the X direction size. This value should be from 1 to 10. It is recommended that the value be 2 or less.
     * @param aspectRatio
     *     Specifies the ratio of the PDF417 barcode. This values should be from 1 to 10. It is recommended that this value be 2 or less.
     * @param barcodeData
     *     Specifies the characters in the PDF417 barcode.
     */

    /**
     * This function checks the Firmware Informatin of the printer
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress>)
     * @param portSettings
     *     Should be blank
     */
    public static void CheckFirmwareVersion(Context context, String portName, String portSettings) {
        StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            Map<String, String> firmware = port.getFirmwareInformation();

            String modelName = firmware.get("ModelName");
            String firmwareVersion = firmware.get("FirmwareVersion");

            String message = "Model Name:" + modelName;
            message += "\nFirmware Version:" + firmwareVersion;

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Firmware Information");
            alert.setMessage(message);
            alert.setCancelable(false);
            alert.show();

        } catch (StarIOPortException e) {
            Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage("Failed to connect to printer");
            alert.setCancelable(false);
            alert.show();
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }
    }



    /**
     * This function checks the status of the printer
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress>)
     * @param portSettings
     *     Should be blank
     * @param sensorActiveHigh
     *     boolean variable to tell the sensor active of CashDrawer which is High
     */
    public static void CheckStatus(Context context, String portName, String portSettings, boolean sensorActiveHigh) {
        StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            StarPrinterStatus status = port.retreiveStatus();

            if (status.offline == false) {
                String message = "Printer is online";

                if (status.compulsionSwitch == false) {
                    if (true == sensorActiveHigh) {
                        message += "\nCash Drawer: Close";
                    } else {
                        message += "\nCash Drawer: Open";
                    }
                } else {
                    if (true == sensorActiveHigh) {
                        message += "\nCash Drawer: Open";
                    } else {
                        message += "\nCash Drawer: Close";
                    }
                }

                Builder dialog = new AlertDialog.Builder(context);
                dialog.setNegativeButton("OK", null);
                AlertDialog alert = dialog.create();
                alert.setTitle("Printer");
                alert.setMessage(message);
                alert.setCancelable(false);
                alert.show();
            } else {
                String message = "Printer is offline";

                if (status.receiptPaperEmpty == true) {
                    message += "\nPaper is empty";
                }

                if (status.coverOpen == true) {
                    message += "\nCover is open";
                }

                if (status.compulsionSwitch == false) {
                    if (true == sensorActiveHigh) {
                        message += "\nCash Drawer: Close";
                    } else {
                        message += "\nCash Drawer: Open";
                    }
                } else {
                    if (true == sensorActiveHigh) {
                        message += "\nCash Drawer: Open";
                    } else {
                        message += "\nCash Drawer: Close";
                    }
                }

                Builder dialog = new AlertDialog.Builder(context);
                dialog.setNegativeButton("OK", null);
                AlertDialog alert = dialog.create();
                alert.setTitle("Printer");
                alert.setMessage(message);
                alert.setCancelable(false);
                alert.show();
            }

        } catch (StarIOPortException e) {
            Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage("Failed to connect to printer");
            alert.setCancelable(false);
            alert.show();
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }
    }

    /**
     * This function checks the status of the printer which does not have compulsion switch
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress>)
     * @param portSettings
     *     Should be blank
     * @param sensorActiveHigh
     *     boolean variable to tell the sensor active of CashDrawer which is High
     */
    public static void CheckStatus(Context context, String portName, String portSettings) {
        StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            StarPrinterStatus status = port.retreiveStatus();

            if (status.offline == false) {
                String message = "Printer is online";

                Builder dialog = new AlertDialog.Builder(context);
                dialog.setNegativeButton("OK", null);
                AlertDialog alert = dialog.create();
                alert.setTitle("Printer");
                alert.setMessage(message);
                alert.setCancelable(false);
                alert.show();
            } else {
                String message = "Printer is offline";

                if (status.receiptPaperEmpty == true) {
                    message += "\nPaper is empty";
                }

                if (status.coverOpen == true) {
                    message += "\nCover is open";
                }

                Builder dialog = new AlertDialog.Builder(context);
                dialog.setNegativeButton("OK", null);
                AlertDialog alert = dialog.create();
                alert.setTitle("Printer");
                alert.setMessage(message);
                alert.setCancelable(false);
                alert.show();
            }

        } catch (StarIOPortException e) {
            Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage("Failed to connect to printer");
            alert.setCancelable(false);
            alert.show();
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }
    }

    /**
     * This function enable USB serial number of the printer
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (USB:)
     * @param portSettings
     *     Should be blank
     */
    public static void EnableUSBSerialNumber(Context context, String portName, String portSettings, byte[] serialNumber) {
        StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            ArrayList<byte[]> setSerialNumCommand = new ArrayList<byte[]>();

            setSerialNumCommand.add(new byte[] { 0x1b, 0x23, 0x23, 0x57, 0x38, 0x2c });
            for (int i = 0; i < 8 - serialNumber.length; i++) {
                setSerialNumCommand.add(new byte[] { 0x30 });	// Fill in the top at "0" to be a total 8 digits.
            }
            setSerialNumCommand.add(serialNumber);
            setSerialNumCommand.add(new byte[] { 0x0a, 0x00 });

            byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(setSerialNumCommand);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

            // Wait for 5 seconds until printer recover from software reset
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            byte[] mswenableCommand = new byte[] { 0x1b, 0x1d, 0x23, 0x2b, 0x43, 0x30, 0x30, 0x30, 0x32, 0x0a, 0x00, 0x1b, 0x1d, 0x23, 0x57, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0a, 0x00 };

            port.writePort(mswenableCommand, 0, mswenableCommand.length);

            // Wait for 3 seconds until printer recover from software reset
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

        } catch (StarIOPortException e) {
            Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage("Failed to connect to printer");
            alert.setCancelable(false);
            alert.show();
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }
    }

    /**
     * This function disable USB serial number of the printer
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (USB:)
     * @param portSettings
     *     Should be blank
     */
    public static void DisableUSBSerialNumber(Context context, String portName, String portSettings) {
        StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            byte[] clearSerialNumCommand = new byte[] { 0x1b, 0x23, 0x23, 0x57, 0x38, 0x2c, '?', '?', '?', '?', '?', '?', '?', '?', 0x0a, 0x00 };

            port.writePort(clearSerialNumCommand, 0, clearSerialNumCommand.length);

            // Wait for 5 seconds until printer recover from software reset
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            byte[] mswedisableCommand = new byte[] { 0x1b, 0x1d, 0x23, 0x2d, 0x43, 0x30, 0x30, 0x30, 0x32, 0x0a, 0x00, 0x1b, 0x1d, 0x23, 0x57, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0a, 0x00 };

            port.writePort(mswedisableCommand, 0, mswedisableCommand.length);

            // Wait for 3 seconds until printer recover from software reset
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

        } catch (StarIOPortException e) {
            Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage("Failed to connect to printer");
            alert.setCancelable(false);
            alert.show();
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }
    }

    /**
     * This function sends raw text to the printer, showing how the text can be formated. Ex: Changing size
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress>)
     * @param portSettings
     *     Should be blank
     * @param slashedZero
     *     boolean variable to tell the printer to slash zeroes
     * @param underline
     *     boolean variable that tells the printer to underline the text
     * @param twoColor
     *     boolean variable that tells the printer to should print red or black text.
     * @param emphasized
     *     boolean variable that tells the printer to should emphasize the printed text. This is somewhat like bold. It isn't as dark, but darker than regular characters.
     * @param upperline
     *     boolean variable that tells the printer to place a line above the text. This is only supported by newest printers.
     * @param upsideDown
     *     boolean variable that tells the printer to print text upside down.
     * @param heightExpansion
     *     boolean variable that tells the printer to should expand double-tall printing.
     * @param widthExpansion
     *     boolean variable that tells the printer to should expand double-wide printing.
     * @param leftMargin
     *     Defines the left margin for text on Star portable printers. This number can be from 0 to 65536. However, remember how much space is available as the text can be pushed off the page.
     * @param alignment
     *     Defines the alignment of the text. The printers support left, right, and center justification.
     * @param textData
     *     The text to send to the printer.
     * @param encode
     *     Set encode for multi-byte character or blank for single byte character.
     */

    protected static byte[] ReplaceCommand(byte[] tempDataBytes) {

        byte[] buffer = new byte[tempDataBytes.length];
        int j = 0;

        byte[] specifyJISkanjiCharacterModeCommand = new byte[] {0x1b, 0x70};
        byte[] cancelJISkanjiCharacterModeCommand = new byte[] {0x1b, 0x71};

        //replace command
        //Because LF(0x0A) command is not performed.
        if(tempDataBytes.length > 0){
            for(int i=0; i<tempDataBytes.length; i++){
                if(tempDataBytes[i] == 0x1b){
                    if(tempDataBytes[i+1] == 0x24){// Replace [0x1b 0x24 0x42] to "Specify JIS Kanji Character Mode" command
                        buffer[j]   = specifyJISkanjiCharacterModeCommand[0];
                        buffer[j+1] = specifyJISkanjiCharacterModeCommand[1];
                        j += 2;
                    }
                    else if(tempDataBytes[i+1] == 0x28){//Replace [0x1b 0x28 0x42] to "Cancel JIS Kanji Character Mode" command
                        buffer[j]   = cancelJISkanjiCharacterModeCommand[0];
                        buffer[j+1] = cancelJISkanjiCharacterModeCommand[1];
                        j += 2;
                    }

                    i += 2;
                }else{
                    buffer[j] = tempDataBytes[i];
                    j++;
                }
            }
        }

        //check 0x00 position
        int datalength = 0;
        for(int i=0; i< buffer.length; i++){
            if(buffer[i] == 0x00){
                datalength = i;
                break;
            }
        }

        //copy data
        if(datalength == 0){
            datalength = buffer.length;
        }
        byte[] data = new byte[datalength];
        System.arraycopy(buffer, 0, data, 0, datalength);

        return data;
    }

    /**
     * This function is used to print a Java bitmap directly to the printer. There are 2 ways a printer can print images: through raster commands or line mode commands This function uses raster commands to print an image. Raster is supported on the TSP100 and all Star Thermal POS printers. Line mode printing is not supported by the TSP100. There is no example of using this method in this sample.
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress>)
     * @param portSettings
     *     Should be blank
     * @param source
     *     The bitmap to convert to Star Raster data
     * @param maxWidth
     *     The maximum width of the image to print. This is usually the page width of the printer. If the image exceeds the maximum width then the image is scaled down. The ratio is maintained.
     */
    public static void PrintBitmap(Context context, String portName, String portSettings, Bitmap source, int maxWidth, boolean compressionEnable, RasterCommand rasterType, CallbackContext callbackcontext) {
        try {
            ArrayList<byte[]> commands = new ArrayList<byte[]>();

            RasterDocument rasterDoc = new RasterDocument(RasSpeed.Medium, RasPageEndMode.FeedAndFullCut, RasPageEndMode.FeedAndFullCut, RasTopMargin.Standard, 0, 0, 0);
            StarBitmap starbitmap = new StarBitmap(source, false, maxWidth);

            if (rasterType == RasterCommand.Standard) {
                commands.add(rasterDoc.BeginDocumentCommandData());

                commands.add(starbitmap.getImageRasterDataForPrinting_Standard(compressionEnable));

                commands.add(rasterDoc.EndDocumentCommandData());
            } else {
                commands.add(starbitmap.getImageRasterDataForPrinting_graphic(compressionEnable));

                commands.add(new byte[] { 0x1b, 0x64, 0x02 }); // Feed to cutter position
            }

            sendCommand(context, portName, portSettings, commands, callbackcontext);
        } catch (OutOfMemoryError e) {
            throw e;
        }

    }

    /**
     * This function shows how to read the MSR data(credit card) of a portable printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
     * @param portSettings
     *     Should be portable as the port settings. It is used for portable printers
     */
    public static void MCRStart(final Context context, String portName, String portSettings) {
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            portForMoreThanOneFunction = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 portForMoreThanOneFunction = StarIOPort.getPort(portName, portSettings, 10000);
			 */

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            portForMoreThanOneFunction.writePort(new byte[] { 0x1b, 0x4d, 0x45 }, 0, 3);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("Cancel", new OnClickListener() {
                // If the user cancels MSR mode, the character 0x04 is sent to the printer
                // This function also closes the port
                public void onClick(DialogInterface dialog, int which) {
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                    try {
                        portForMoreThanOneFunction.writePort(new byte[] { 0x04 }, 0, 1);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                        }
                    } catch (StarIOPortException e) {

                    } finally {
                        if (portForMoreThanOneFunction != null) {
                            try {
                                StarIOPort.releasePort(portForMoreThanOneFunction);
                            } catch (StarIOPortException e1) {
                            }
                        }
                    }
                }
            });
            AlertDialog alert = dialog.create();
            alert.setTitle("");
            alert.setMessage("Slide credit card");
            alert.setCancelable(false);
            alert.setButton("OK", new OnClickListener() {
                // If the user presses ok then the magnetic stripe is read and displayed to the user
                // This function also closes the port
                public void onClick(DialogInterface dialog, int which) {
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                    try {
                        byte[] mcrData = new byte[256];
                        int counts = 0;

                        for (int i = 0; i < 5; i++) {
                            counts += portForMoreThanOneFunction.readPort(mcrData, counts, mcrData.length);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                        }

                        byte[] headerPattern = new byte[]{0x02, 0x45, 0x31, 0x31, 0x1c, 0x1c};
                        byte[] footerPattern = new byte[]{0x1c, 0x03, 0x0d, 0x0a };
                        int headerPatternPos = -1;
                        int fotterPatternPos = -1;

                        byte[] data2 = new byte[headerPattern.length];
                        byte[] data3 = new byte[footerPattern.length];

                        //Check Start header position
                        for(int i = 0; i< (mcrData.length - headerPattern.length +1); i++){
                            System.arraycopy(mcrData, i, data2, 0, (headerPattern.length));

                            if(Arrays.equals(data2, headerPattern))
                            {
                                headerPatternPos = i;
                                break;
                            }
                        }

                        //Check Start fotter position
                        for(int i = headerPatternPos + headerPattern.length; i< (mcrData.length - footerPattern.length +1); i++){
                            System.arraycopy(mcrData, i, data3, 0, (footerPattern.length));

                            if(Arrays.equals(data3, footerPattern)){
                                fotterPatternPos = i;
                                break;
                            }
                        }

                        if((headerPatternPos < 0) || (fotterPatternPos < 0) ){
                            Builder dialog1 = new AlertDialog.Builder(context);
                            dialog1.setNegativeButton("Ok", null);
                            AlertDialog alert = dialog1.create();
                            alert.setTitle("No data");
                            alert.setMessage("There is nothing available data.");
                            alert.show();
                        } else {
                            byte[] reciveDataList = new byte[fotterPatternPos - headerPatternPos];

                            System.arraycopy(mcrData, headerPatternPos, reciveDataList, 0, fotterPatternPos - headerPatternPos);

                            Builder dialog1 = new AlertDialog.Builder(context);
                            dialog1.setNegativeButton("Ok", null);
                            AlertDialog alert = dialog1.create();
                            alert.setTitle("");
                            alert.setMessage(new String(reciveDataList));
                            alert.show();
                        }

                        portForMoreThanOneFunction.writePort(new byte[] { 0x04 }, 0, 1);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                        }

                    } catch (StarIOPortException e) {

                    } finally {
                        if (portForMoreThanOneFunction != null) {
                            try {
                                StarIOPort.releasePort(portForMoreThanOneFunction);
                            } catch (StarIOPortException e1) {
                            }
                        }
                    }
                }
            });
            alert.show();
        } catch (StarIOPortException e) {
            Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("Ok", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage("Failed to connect to printer");
            alert.setCancelable(false);
            alert.show();
            if (portForMoreThanOneFunction != null) {
                try {
                    StarIOPort.releasePort(portForMoreThanOneFunction);
                } catch (StarIOPortException e1) {
                }
            }
        } finally {

        }
    }

    /**
     * MSR functionality is supported on Star portable printers only.
     *
     * @param context
     *     Activity for displaying messages to the user that this function is not supported
     */
    public static void MCRnoSupport(Context context) {
        Builder dialog = new AlertDialog.Builder(context);
        dialog.setNegativeButton("OK", null);
        AlertDialog alert = dialog.create();
        alert.setTitle("Feature Not Available");
        alert.setMessage("MSR functionality is supported only on portable printer models");
        alert.setCancelable(false);
        alert.show();
    }

    private static byte[] createShiftJIS(String inputText) {
        byte[] byteBuffer = null;

        try {
            byteBuffer = inputText.getBytes("Shift_JIS");
        } catch (UnsupportedEncodingException e) {
            byteBuffer = inputText.getBytes();
        }

        return byteBuffer;
    }

    private static byte[] createGB2312(String inputText) {
        byte[] byteBuffer = null;

        try {
            byteBuffer = inputText.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            byteBuffer = inputText.getBytes();
        }

        return byteBuffer;
    }

    private static byte[] createBIG5(String inputText) {
        byte[] byteBuffer = null;

        try {
            byteBuffer = inputText.getBytes("Big5");
        } catch (UnsupportedEncodingException e) {
            byteBuffer = inputText.getBytes();
        }

        return byteBuffer;
    }

    private static byte[] createCp1251(String inputText) {
        byte[] byteBuffer = null;

        try {
            byteBuffer = inputText.getBytes("Windows-1251");
        } catch (UnsupportedEncodingException e) {
            byteBuffer = inputText.getBytes();
        }

        return byteBuffer;
    }

    private static byte[] createCp1252(String inputText) {
        byte[] byteBuffer = null;

        try {
            byteBuffer = inputText.getBytes("Windows-1252");
        } catch (UnsupportedEncodingException e) {
            byteBuffer = inputText.getBytes();
        }

        return byteBuffer;
    }

    private static byte[] createCpUTF8(String inputText) {
        byte[] byteBuffer = null;

        try {
            byteBuffer = inputText.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            byteBuffer = inputText.getBytes();
        }

        return byteBuffer;
    }
    private static byte[] createRasterCommand(String printText, int textSize, int bold, RasterCommand rasterType) {
        byte[] command;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        Typeface typeface;

        try {
            typeface = Typeface.create(Typeface.SERIF, bold);
        } catch (Exception e) {
            typeface = Typeface.create(Typeface.DEFAULT, bold);
        }

        paint.setTypeface(typeface);
        paint.setTextSize(textSize * 2);
        paint.setLinearText(true);

        TextPaint textpaint = new TextPaint(paint);
        textpaint.setLinearText(true);
        android.text.StaticLayout staticLayout = new StaticLayout(printText, textpaint, printableArea, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        int height = staticLayout.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(staticLayout.getWidth(), height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        c.drawColor(Color.WHITE);
        c.translate(0, 0);
        staticLayout.draw(c);

        StarBitmap starbitmap = new StarBitmap(bitmap, false, printableArea);

        if (rasterType == RasterCommand.Standard) {
            command = starbitmap.getImageRasterDataForPrinting_Standard(true);
        } else {
            command = starbitmap.getImageRasterDataForPrinting_graphic(true);
        }

        return command;
    }

    private static byte[] convertFromListByteArrayTobyteArray(List<byte[]> ByteArray) {
        int dataLength = 0;
        for (int i = 0; i < ByteArray.size(); i++) {
            dataLength += ByteArray.get(i).length;
        }

        int distPosition = 0;
        byte[] byteArray = new byte[dataLength];
        for (int i = 0; i < ByteArray.size(); i++) {
            System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition, ByteArray.get(i).length);
            distPosition += ByteArray.get(i).length;
        }

        return byteArray;
    }

    public static StarPrinterStatus GetStatus(Context context, String portName, String portSettings, boolean sensorActiveHigh) throws StarIOPortException {
        StarIOPort port = null;
        StarPrinterStatus status = null;
        try {
            port = StarIOPort.getPort(portName, portSettings, 10000, context);

            try {
                Thread.sleep(500);
            }
            catch(InterruptedException e) {}

            status = port.retreiveStatus();
        }
        catch (StarIOPortException e) {
            // Bubbling the exception up
            throw e;
        }
        finally {
            if(port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {}
            }
        }
        return status;
    }

    public static List<PortInfo> getAllPrinters() {
        ArrayList<PortInfo> portList;
        ArrayList<PortInfo> portNames = new ArrayList<PortInfo>();

        try {
            portList = StarIOPort.searchPrinter("BT:");

            for (PortInfo portInfo : portList) {
                portNames.add(portInfo);
            }
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }

        try {
            portList = StarIOPort.searchPrinter("TCP:");

            for (PortInfo portInfo : portList) {
                portNames.add(portInfo);
            }
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }

        return portNames;
    }

    private static void sendCommand(Context context, String portName, String portSettings, ArrayList<byte[]> byteList, CallbackContext callbackContext) {
        StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }

			/*
			 * Using Begin / End Checked Block method When sending large amounts of raster data,
			 * adjust the value in the timeout in the "StarIOPort.getPort" in order to prevent
			 * "timeout" of the "endCheckedBlock method" while a printing.
			 *
			 * If receipt print is success but timeout error occurs(Show message which is "There
			 * was no response of the printer within the timeout period." ), need to change value
			 * of timeout more longer in "StarIOPort.getPort" method.
			 * (e.g.) 10000 -> 30000
			 */
            StarPrinterStatus status = port.beginCheckedBlock();

            if (true == status.offline) {
                callbackContext.error("Printer is offline");
                throw new StarIOPortException("A printer is offline");
            }

            byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

            port.setEndCheckedBlockTimeoutMillis(30000);// Change the timeout time of endCheckedBlock method.
            status = port.endCheckedBlock();

            if (status.coverOpen == true) {
                callbackContext.error("Printer cover is open");
                throw new StarIOPortException("Printer cover is open");
            } else if (status.receiptPaperEmpty == true) {
                callbackContext.error("Printer paper is empty");
                throw new StarIOPortException("Receipt paper is empty");
            } else if (status.offline == true) {
                callbackContext.error("Printer is offline");
                throw new StarIOPortException("Printer is offline");
            }
            else {
                callbackContext.success("print success");
            }
        } catch (StarIOPortException e) {
            callbackContext.error("Could not connect to printer. Make sure the printer is on.");
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }
    }
}
