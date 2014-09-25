/* See http://variableinc.com/terms-use-license for the full license governing this code. */
package com.variable.demo.api.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.variable.demo.api.MessageConstants;
import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.ThermocoupleSensor;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.SensorReading;

import java.text.DecimalFormat;

/**
 * Created by coreymann on 9/16/13.
 */
public class ThermoCoupleFragment extends Fragment implements ThermocoupleSensor.ThermaCoupleListener {
    public static final String TAG = ThermoCoupleFragment.class.getName();
    private TextView temperatureText;

    private ThermocoupleSensor thermocoupleSensor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.thermocouple, null, false);
        temperatureText = (TextView) root.findViewById(R.id.txtThermoCoupleReading);

        return root;
    }


    @Override
    public void onPause() {
        super.onPause();

        //Unregister for thermoCouple event.
        DefaultNotifier.instance().removeThermaCoupleListener(this);
        thermocoupleSensor.stopSensor();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Register for ThermoCouple Event
        DefaultNotifier.instance().addThermaCoupleListener(this);
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if(node != null)
        {
            thermocoupleSensor = node.findSensor(NodeEnums.ModuleType.THERMOCOUPLE);
            thermocoupleSensor.startSensor();
        }
    }

    @Override
    public void onThermoCoupleReading(ThermocoupleSensor sensor, SensorReading<Float> reading) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_THERMA_TEMPERATURE);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, reading.getValue());
        final Context thiscontext = this.getActivity();
        final String serialnumOne = sensor.getSerialNumber();
        final String serialnum = serialnumOne.replaceAll("[^\\u0000-\\uFFFF]", "");
        final String scan = Float.toString(reading.getValue());
        String json = "thermocouple;"+serialnum+";"+scan;

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
        m.sendToTarget();
    }

    private final Handler mHandler = new Handler(){
        private final DecimalFormat formatter = new DecimalFormat("0.00");
        @Override
        public void handleMessage(Message msg)
        {
            float value = msg.getData().getFloat(MessageConstants.FLOAT_VALUE_KEY);
            switch(msg.what){
                case MessageConstants.MESSAGE_THERMA_TEMPERATURE:
                    temperatureText.setText(formatter.format(value) + " °C");
                    break;

            }
        }
    };


}
