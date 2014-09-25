/* See http://variableinc.com/terms-use-license for the full license governing this code. */
package com.variable.demo.api.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.BarCodeScanner;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.SensorReading;

// handle HTTP POST

/**
 *
 */
public class BarCodeFragment extends Fragment {
    public static final String TAG = BarCodeFragment.class.getName();

    private BarCodeScanner scanner;
    private BarCodeScanner.BarCodeScannerListener listener;

    public BarCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Obtain the Active Scanner.
        scanner = ((NodeApplication) getActivity().getApplication()).getActiveNode().findSensor(NodeEnums.ModuleType.BARCODE);
        assert scanner != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.barcode, null, false);
        final Context thiscontext = container.getContext();
        //Create a Listener for recieving barcodes and update the edit text box.
        final EditText barCodeEditText = (EditText) root.findViewById(R.id.editBarCode);
        listener = new BarCodeScanner.BarCodeScannerListener() {
            @Override
            public void onBarCodeTransmitted(BarCodeScanner barCodeScanner, final SensorReading<String> barCodeReading) {
                // convert the UTF
                final String serialnumOne = barCodeScanner.getSerialNumber();
                final String serialnum = serialnumOne.replaceAll("[^\\u0000-\\uFFFF]", "");
                final String scann = barCodeReading.getValue();
                final String scan = scann.replaceAll("[\u0000-\u001f]", "");
                String json = "barcode;"+serialnum+";"+scan;
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        barCodeEditText.setText(barCodeReading.getValue());

                    }
                });

            }
        };
        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        DefaultNotifier.instance().addBarCodeScannerListener(listener);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        DefaultNotifier.instance().addBarCodeScannerListener(listener);
    }



}
