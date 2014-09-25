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
import com.variable.framework.node.ClimaSensor;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.SensorReading;

import java.text.DecimalFormat;

/**
 * Created by coreymann on 8/13/13.
 */
public class ClimaFragment extends Fragment  implements
                                                        ClimaSensor.ClimaHumidityListener,
                                                        ClimaSensor.ClimaLightListener,
                                                        ClimaSensor.ClimaPressureListener,
                                                        ClimaSensor.ClimaTemperatureListener{
    public static final String TAG = ClimaFragment.class.getName();

    private TextView climaLightText;
    private TextView climaPressureText;
    private TextView climaTempText;
    private TextView climaHumidText;
    private ClimaSensor clima;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.clima, null, false);

       climaHumidText = (TextView) root.findViewById(R.id.txtClimaHumidity);
       climaLightText = (TextView) root.findViewById(R.id.txtClimaLight);
       climaPressureText = (TextView) root.findViewById(R.id.txtClimaPressure);
       climaTempText = (TextView) root.findViewById(R.id.txtClimaTemperature);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        DefaultNotifier.instance().addClimaHumidityListener(this);
        DefaultNotifier.instance().addClimaLightListener(this);
        DefaultNotifier.instance().addClimaTemperatureListener(this);
        DefaultNotifier.instance().addClimaPressureListener(this);

        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if(node != null){

            //Located the first available clima sensor.
            clima = node.findSensor(NodeEnums.ModuleType.CLIMA);

            //Turn on all streaming
            clima.setStreamMode(true, true, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //Unregister for clima events.
        DefaultNotifier.instance().removeClimaHumidityListener(this);
        DefaultNotifier.instance().removeClimaLightListener(this);
        DefaultNotifier.instance().removeClimaTemperatureListener(this);
        DefaultNotifier.instance().removeClimaPressureListener(this);

        //Turn off clima sensor
        clima.setStreamMode(false, false, false);
    }

    @Override
    public void onClimaHumidityUpdate(ClimaSensor clima, SensorReading<Float> humidityLevel) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_CLIMA_HUMIDITY);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, humidityLevel.getValue());
        // convert the UTF
        final Context thiscontext = this.getActivity();
        final DecimalFormat formatter = new DecimalFormat("0.00");
        final String serialnumOne = clima.getSerialNumber();
        final String serialnum = serialnumOne.replaceAll("[^\\u0000-\\uFFFF]", "");
        final String scann = formatter.format(humidityLevel.getValue());
        String json = "humidity;"+serialnum+";"+scann;

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

    @Override
    public void onClimaLightUpdate(ClimaSensor clima, SensorReading<Float> lightLevel) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_CLIMA_LIGHT);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, lightLevel.getValue());
        // convert the UTF
        final Context thiscontext = this.getActivity();
        final DecimalFormat formatter = new DecimalFormat("0.00");
        final String serialnumOne = clima.getSerialNumber();
        final String serialnum = serialnumOne.replaceAll("[^\\u0000-\\uFFFF]", "");
        final String scann = formatter.format(lightLevel.getValue());
        String json = "light;"+serialnum+";"+scann;

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

    @Override
    public void onClimaPressureUpdate(ClimaSensor clima, SensorReading<Integer> kPa) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_CLIMA_PRESSURE);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, kPa.getValue());
        // convert the UTF
        final Context thiscontext = this.getActivity();
        final DecimalFormat formatter = new DecimalFormat("0.00");
        final String serialnumOne = clima.getSerialNumber();
        final String serialnum = serialnumOne.replaceAll("[^\\u0000-\\uFFFF]", "");
        final String scann = formatter.format(kPa.getValue());
        String json = "pressure;"+serialnum+";"+scann;

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

    @Override
    public void onClimaTemperatureUpdate(ClimaSensor clima, SensorReading<Float> temperature) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_CLIMA_TEMPERATURE);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, temperature.getValue());
        // convert the UTF
        final Context thiscontext = this.getActivity();
        final DecimalFormat formatter = new DecimalFormat("0.00");
        final String serialnumOne = clima.getSerialNumber();
        final String serialnum = serialnumOne.replaceAll("[^\\u0000-\\uFFFF]", "");
        final String scann = formatter.format(temperature.getValue());
        String json = "temperature;"+serialnum+";"+scann;

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
        public void handleMessage(Message msg){

            float value = msg.getData().getFloat(MessageConstants.FLOAT_VALUE_KEY);
            switch(msg.what){
                case MessageConstants.MESSAGE_CLIMA_HUMIDITY:
                    climaHumidText.setText(formatter.format(value) + " %RH");
                    break;
                 case MessageConstants.MESSAGE_CLIMA_LIGHT:
                    climaLightText.setText(formatter.format(value) + " LUX");
                    break;

                case MessageConstants.MESSAGE_CLIMA_PRESSURE:
                    climaPressureText.setText(formatter.format(value / 1000) + " kPA");
                    break;

                case MessageConstants.MESSAGE_CLIMA_TEMPERATURE:
                    climaTempText.setText(formatter.format(value) + " C");
                    break;

            }

        }
    };
}
