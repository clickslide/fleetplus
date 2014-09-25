/* See http://variableinc.com/terms-use-license for the full license governing this code. */
package com.variable.demo.api.fragment;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.variable.demo.api.ColorUtils;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.ChromaDevice;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.ColorSense;
import com.variable.framework.node.reading.VTRGBCReading;


/**
 * ChromaFragment is a base fragment with no implementation specfied for layouts and work flow.
 * Any instance will listen for new chroma scans and process them accordingly by passing the neccessary values to its handler.
 *
 * Additionally, this will allow for requesting a chroma reading via a button pressed by default. If this behavior is not to be expected then
 * Created by coreymann on 6/28/13.
 */

public class ChromaFragment extends Fragment implements ChromaDevice.ChromaListener{
        public static final String TAG = ChromaFragment.class.getName();


        private NodeDevice.ButtonListener mButtonListener = new NodeDevice.ButtonListener() {
            //TODO: Test Button Pressed and Released Events
            @Override
            public void onPushed(NodeDevice nodeDevice) {
                Log.d(TAG, "onPushed()");
            }

            @Override
            public void onReleased(NodeDevice nodeDevice) {
                Log.d(TAG, "onReleased()");

                if(allowScanWhenButtonReleased()){
                    //By sleeping, this will allow the downward pressure to be released and
                    // avoid the propagation of errors during a chroma scan.
                    try {   Thread.sleep(500);  }
                    catch (InterruptedException e) {    e.printStackTrace();    }

                    //Issue a request for a new reading.
                    ChromaDevice chroma = nodeDevice.findSensor(NodeEnums.ModuleType.CHROMA);
                    if(chroma != null){
                        chroma.requestChromaReading();
                    }
                }
            }
        };

    @Override
    public void onResume(){
        super.onResume();

        //Register for Chroma Scans.
        DefaultNotifier.instance().addButtonListener(mButtonListener);
        DefaultNotifier.instance().addChromaListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();

        //UnRegister for Chroma Scans
        DefaultNotifier.instance().removeButtonListener(mButtonListener);
        DefaultNotifier.instance().removeChromaListener(this);
    }

    /**
     * invokes
     *
     *    onRGBUpdate
     *    onTimeStampUpdate
     *    onColorUpdate
     *    onLABUpdate
     *
     *
     *
     * @param chromaDevice
     * @param reading
     */
    @Override
    public void  onChromaReadingReceived(ChromaDevice chromaDevice,VTRGBCReading reading) {
        ColorSense sense = reading.getColorSense();
        Log.d(TAG, "SENSE_VALUES: " + sense.getSenseRed().floatValue() + " , " + sense.getSenseGreen() + " , " + sense.getSenseBlue() + " , " + sense.getSenseClear());
        int color = ColorUtils.RGBToColor(reading.getD65srgbR(), reading.getD65srgbG(), reading.getD65srgbB());

        //D50 onLABUpdate(reading.getD50L(), reading.getD50a(), reading.getD50b());
        onLABUpdate(reading.getD65L(), reading.getD65a(), reading.getD65b());
        onHexValue("#" + Integer.toHexString(color).substring(2).toUpperCase());
        final Context thiscontext = this.getActivity();
        final String serialnumOne = chromaDevice.getSerialNumber();
        final String serialnum = serialnumOne.replaceAll("[^\\u0000-\\uFFFF]", "");
        final String scann = "#" + Integer.toHexString(color).substring(2).toUpperCase();
        String json = "color;"+serialnum+";"+scann;

        // POST to variable dashboard
        Ion.getDefault(thiscontext).getConscryptMiddleware().enable(false);
        Ion.with(thiscontext)
                .load("https://datadipity.com/clickslide/fleetplusdata.json?PHPSESSID=gae519f8k5humje0jqb195nob6&update&postparam[payload]=" + json)
                .setLogging("MyLogs", Log.DEBUG)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if (e == null) {
                            Log.i(TAG, "ION SENT MESSAGE WITH RESULT CODE: " + result.toString());
                        } else {
                            Log.i(TAG, "ION SENT MESSAGE WITH EXCEPTION");
                            e.printStackTrace();
                        }
                    }
                });
        onRGBUpdate(Color.red(color), Color.green(color), Color.blue(color));
        onColorUpdate(color);

    }

    /**
     * Invoked when a new reading has been recieved. Additionally, this method is invoked on the UI Thread.
     * @param red
     * @param green
     * @param blue
     */
    public void onRGBUpdate(float red, float green, float blue){ }

    /**
     * Invoked when a new reading has been recieved. Additionally, this method is invoked on the UI Thread.
     *
     * @param hexString - formatted such that #RRGGBB
     */
    public void onHexValue(String hexString){  }

    /**
     * Invoked when a new reading has been recieved. Additionally, this method is invoked on the UI Thread.
     * @param l
     * @param a
     * @param b
     */
    public void onLABUpdate(double l, double a, double b){ }

    /**
     * Invoked when a new reading has bee recieved. Additionally, this method is invoked on the UI Thread.
     * @param color
     */
    public void onColorUpdate(int color ) {  }



    public boolean allowScanWhenButtonReleased(){
        return true;
    }

    /**
     *
     *
     * @param device
     * @param temperature
     */
    @Override
    public void onChromaTemperatureReading(ChromaDevice device, Float temperature) {
    }

    @Override
    public void onWhitePointCalComplete(ChromaDevice device, boolean status) {
        Toast.makeText(getActivity(), "Calibration " + (status ? "succeeded" : "failed"), Toast.LENGTH_LONG).show();}
}
