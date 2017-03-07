
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import java.io.IOException;
import android.util.Base64;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import org.apache.cordova.CallbackContext;

import android.os.AsyncTask;

import com.StarMicronics.StarIOSDK.PrinterFunctions;
import com.StarMicronics.StarIOSDK.PrinterFunctions.RasterCommand;

public class StarPrinter {

    private Context currentContext;
    private CallbackContext mCallbackContext;
    private StarIOPort port = null;

    public StarPrinter(Context context, String image_to_print, String ip_address, String paper, CallbackContext callbackcontext, String action){
        currentContext = context;
        mCallbackContext = callbackcontext;
        if(action.equals("print_receipt")) {
            printReceipt(ip_address, image_to_print, paper);
        }
        else if(action.equals("find_printers")) {
            findPrinters();
        }
    }

    private void printReceipt(String ip_address, String image_to_print, String paper){

        byte[] data = Base64.decode(image_to_print, Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
        // scale up image
        int w = Math.round(((float)image.getHeight() * (float)1.5));
        int h = Math.round(((float)image.getHeight() * (float)1.5));
        image = Bitmap.createScaledBitmap(image, w, h, false);

        RasterCommand rasterType = RasterCommand.Standard;
        int max_paper_width;
        if (paper.equals("3_Inch")) {
            max_paper_width = 576;
        }
        else if(paper.equals("4_Inch")) {
            max_paper_width = 576;
        }
        else {
            max_paper_width = 832;
        }

        PrinterFunctions.PrintBitmap(currentContext, ip_address, "", image, 576, false, rasterType, mCallbackContext);
    }

    private void findPrinters() {

        try {
            List<PortInfo> ports = PrinterFunctions.getAllPrinters();
            String portSettings = "";

            JSONArray result = new JSONArray();

            for (PortInfo port : ports) {
                JSONObject printer = new JSONObject();
                printer.put("mac", port.getMacAddress());
                printer.put("target", port.getPortName());
                printer.put("printer_name", port.getModelName());
                printer.put("brand", "Star");

                try {
                    StarPrinterStatus status = PrinterFunctions.GetStatus(currentContext, port.getPortName(), portSettings, true);
                    printer.put("online", status != null && !status.offline);
                } catch (StarIOPortException e) {
                    printer.put("online", false);
                }

                result.put(printer);
            }

            mCallbackContext.success(result);
        }
        catch (JSONException e) {
            mCallbackContext.error(e.getMessage());
        }
    }
}

