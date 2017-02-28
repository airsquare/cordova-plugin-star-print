import java.util.TimeZone;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;

public class StarController extends CordovaPlugin  {

	private Context mContext = null;

	// set static actions
	private static final String PRINTSAMPLERECIEPT = "printReceipt";
	private static final String FINDPRINTERS = "findPrinters";

	private StarPrinter mPrinter = null;

	public StarController() {
	}

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
	}

	public boolean execute(String action, final JSONArray arguments,
		final CallbackContext callbackContext) throws JSONException {
		mContext = this.cordova.getActivity();

		try {
			if (PRINTSAMPLERECIEPT.equals(action)) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						try{
							String ip_address = (arguments.get(0).toString());
							String base64_image_str = (arguments.get(1).toString());
							mPrinter = null;
							StarPrinter mPrinter = new StarPrinter(mContext,base64_image_str,ip_address,callbackContext, "print_receipt");
						}
						catch(JSONException e){

						}
					}
				});
				return true;
			}
			else if(FINDPRINTERS.equals(action)) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						mPrinter = null;
						StarPrinter mPrinter = new StarPrinter(mContext,"", "",callbackContext, "find_printers");
					}
				});
				return true;
			}
			callbackContext.error("Invalid action: " + action);
			return false;
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------

}