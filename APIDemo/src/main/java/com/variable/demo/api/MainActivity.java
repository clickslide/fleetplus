/* See http://variableinc.com/terms-use-license for the full license governing this code. */
package com.variable.demo.api;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.variable.demo.api.fragment.BarCodeFragment;
import com.variable.demo.api.fragment.ChromaScanFragment;
import com.variable.demo.api.fragment.ClimaFragment;
import com.variable.demo.api.fragment.MainOptionsFragment;
import com.variable.demo.api.fragment.MotionFragment;
import com.variable.demo.api.fragment.OxaFragment;
import com.variable.demo.api.fragment.ThermaFragment;
import com.variable.demo.api.fragment.ThermoCoupleFragment;
import com.variable.framework.android.bluetooth.BluetoothService;
import com.variable.framework.android.bluetooth.DefaultBluetoothDevice;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.BaseSensor;
import com.variable.framework.node.ChromaCalibrationAndBatchingTask;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.interfaces.ProgressUpdateListener;

import static com.variable.framework.node.NodeDevice.ConnectionListener;

public class MainActivity extends FragmentActivity implements View.OnClickListener, NodeDevice.SensorDetector, ConnectionListener, ProgressUpdateListener{
    private static final String TAG = MainActivity.class.getName();
    private static BluetoothService mService;

    private boolean isPulsing = false;
    private ProgressDialog mProgressDialog;

    //region Lifecycle Events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mService = new BluetoothService();
        NodeApplication.setServiceAPI(mService);
    }

    @Override
    public void onResume(){
        super.onResume();

        ensureBluetoothIsOn();

        //Start Options Fragment
        Fragment frag = new MainOptionsFragment().setOnClickListener(this);
        animateToFragment(frag, MainOptionsFragment.TAG);

        //Registering for Events.
        DefaultNotifier.instance().addConnectionListener(this);
        DefaultNotifier.instance().addSensorDetectorListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();

        NodeDevice node = ((NodeApplication) getApplication()).getActiveNode();
        if(isNodeConnected(node)){
            node.disconnect(); //Clean up after ourselves.
        }

        //Registering for Events
        DefaultNotifier.instance().removeConnectionListener(this);
        DefaultNotifier.instance().removeSensorDetectorListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 200){ ensureBluetoothIsOn();  }
    }

    //endregion

    //region Bluetooth Conneection Callbacks

    public void onConnected(final NodeDevice node)
    {
        //We have made a physical connection. The framework must get be able to get the current properties and state of the NODE.
        updateProgressDialog("Preparing NODE", "Initializing " + node.getName());
    }

    /**
     * Signifies that NODE is ready for communication.
     * @param node
     */
    public void onCommunicationInitCompleted(NodeDevice node){
        dismissProgressDialog();
        Toast.makeText(this, node.getName() + " is now ready for use.", Toast.LENGTH_SHORT).show();
    }

    public void onDisconnect(NodeDevice node){
        dismissProgressDialog();
        Toast.makeText(this, node.getName() + " disconnected.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(NodeDevice nodeDevice, Exception e) {
        //Alert User of Failed Attempt
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();

        //Start the Dialog Selection Fragment.
        MainOptionsFragment.showPairedNodesDialog(this);
    }

    @Override
    public void onNodeDiscovered(NodeDevice nodeDevice) {
        //Ignore...Not Supported in Framework yet
    }

    @Override
    public void nodeDeviceFailedToInit(NodeDevice nodeDevice) {
        Toast.makeText(this, "Failed to Initialize NODE...Disconnecting Now", Toast.LENGTH_SHORT).show();

        nodeDevice.disconnect();
    }

    @Override
    public void onConnecting(NodeDevice nodeDevice) {
        updateProgressDialog("Bluetooth Connection", "Connecting to " + nodeDevice.getName());
    }

    //endregion

    @Override
    public void onClick(View view) {
        NodeDevice node = ((NodeApplication) getApplication()).getActiveNode();
        if(!isNodeConnected(node))
        {
            Toast.makeText(this, "No Connection Available", Toast.LENGTH_SHORT ).show();
            return;
        }
        switch(view.getId()){
            case R.id.btnMotion:
                animateToFragment(new MotionFragment(), MotionFragment.TAG);
                break;

            case R.id.btnClima:
                if(checkForSensor(node, NodeEnums.ModuleType.CLIMA, true))
                    animateToFragment(new ClimaFragment(), ClimaFragment.TAG);
                break;

            case R.id.btnTherma:
                if(checkForSensor(node, NodeEnums.ModuleType.THERMA, true))
                    animateToFragment(new ThermaFragment(), ThermaFragment.TAG);
                break;

            case R.id.btnOxa:
                if(checkForSensor(node, NodeEnums.ModuleType.OXA, true))
                    animateToFragment(new OxaFragment(), OxaFragment.TAG);
                break;

            case R.id.btnThermoCouple:
                if(checkForSensor(node, NodeEnums.ModuleType.THERMOCOUPLE, true))
                    animateToFragment(new ThermoCoupleFragment(), ThermoCoupleFragment.TAG);
                break;

            case R.id.btnBarCode:
                if(checkForSensor(node, NodeEnums.ModuleType.BARCODE, true))
                    animateToFragment(new BarCodeFragment(), BarCodeFragment.TAG);
                break;

            case R.id.btnChroma:
                if(checkForSensor(node, NodeEnums.ModuleType.CHROMA, true))
                    animateToFragment(new ChromaScanFragment(), ChromaScanFragment.TAG);
                break;

            //NODE must be polled to maintain an up to date array of sensors.
            case R.id.btnRefreshSensors:
                node.requestSensorUpdate();
                break;

            case R.id.btnPulseLed:
                if(isPulsing){
                    ((Button) view).setText("Pulse LEDs" );
                    node.ledRestoreDefaultBehavior();
                }else{
                    ((Button) view).setText("Restore LEDs");
                    node.ledsPulse((byte) 0xFF, (byte) 0x0F, (byte) 0xFF, (byte) 0xF0, (short) 2000, (short) 25);
                }

                isPulsing = !isPulsing;
        }
    }


    //region Private Methods

    /**
     * Invokes a new intent to request to start the bluetooth, if not already on.
     */
    private boolean ensureBluetoothIsOn(){
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            btIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(btIntent, 200);
            return false;
        }

        return true;
    }


    /**
     * Checks if a fragment with the specified tag exists already in the Fragment Manager. If present, then removes fragment.
     *
     * Animates out to the specified fragment.
     *
     *
     * @param frag
     * @param tag
     */
    public void animateToFragment(final Fragment frag, final String tag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment existingFrag = getSupportFragmentManager().findFragmentByTag(tag);
        if(existingFrag != null){
            getSupportFragmentManager().beginTransaction().remove(existingFrag).commitAllowingStateLoss();
        }

        ft.replace(R.id.center_fragment_container, frag, tag);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Checks for a specific sensor on a node.
     * @param node - the node
     * @param type - the module type to check for on the node parameter.
     * @param displayIfNotFound - allows toasting a message if module is not found on node.
     * @return true, if the node contains the module
     */
    private boolean checkForSensor(NodeDevice node, NodeEnums.ModuleType type, boolean displayIfNotFound){
       BaseSensor sensor = node.findSensor(type);
        if(sensor == null && displayIfNotFound){
            Toast.makeText(MainActivity.this, type.toString() + " not found on " + node.getName(), Toast.LENGTH_SHORT).show();
        }

        return sensor != null;
    }

    /**
     * Determines if the node is connected. Null is permitted.
     * @param node
     * @return
     */
    private boolean isNodeConnected(NodeDevice node) { return node != null && node.isConnected(); }


    //Convience Method
    private final void dismissProgressDialog(){
        if(mProgressDialog != null){
            try { mProgressDialog.dismiss(); } catch(Exception e){ e.printStackTrace(); }
        }
    }

    //Convience Method
    private final void updateProgressDialog(String title, String message){
        if(mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Restart and Kill all connections....
                    mService.stop();

                    //Restart Service...
                    mService.start();
                }
            });
        }

        if(title != null) { mProgressDialog.setTitle(title);    }
        if(message != null) {   mProgressDialog.setMessage(message);    }

        if(mProgressDialog.isShowing() == false){
            try { mProgressDialog.show(); } catch (Exception e){e.printStackTrace(); }
        }
    }

    //endregion

    //region Sensor Detector Callbacks

    @Override
    public void onSensorConnected(NodeDevice nodeDevice, final BaseSensor baseSensor) {
        Log.d(TAG, "Sensor Found: " + baseSensor.getModuleType() + " SubType: " + baseSensor.getSubtype() + " Serial: " + baseSensor.getSerialNumber());
        Toast.makeText(MainActivity.this, baseSensor.getModuleType() + " has been detected", Toast.LENGTH_SHORT).show();


        if(baseSensor.getModuleType().equals(NodeEnums.ModuleType.CHROMA))
        {
            updateProgressDialog("Initializing Chroma", "Starting Initialization");
            ChromaCalibrationAndBatchingTask task = new ChromaCalibrationAndBatchingTask(MainActivity.this, baseSensor, nodeDevice, this);
            new Thread(task).start();
            return;
        }
    }

    @Override
    public void onSensorDisconnected(NodeDevice nodeDevice, final BaseSensor baseSensor) {
        Toast.makeText(MainActivity.this, baseSensor.getModuleType() + " has been removed", Toast.LENGTH_SHORT).show();
    }

    //endregion

    //region Chroma Initialization Callbacks
    @Override
    public void onProgressUpdated(String status) {
        updateProgressDialog(null, status);
    }

    @Override
    public void onTaskFinished(boolean isSuccessful) {
        if(isSuccessful){
            dismissProgressDialog();
            Toast.makeText(this, "Chroma is ready to use", Toast.LENGTH_SHORT).show();
        }
        else{
            dismissProgressDialog();
            Toast.makeText(this, "Chroma failed to find suitable internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    //endregion
}
