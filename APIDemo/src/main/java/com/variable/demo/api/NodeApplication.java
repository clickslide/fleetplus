/* See http://variableinc.com/terms-use-license for the full license governing this code. */
package com.variable.demo.api;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import com.variable.framework.android.bluetooth.BluetoothService;
import com.variable.framework.node.NodeDevice;

import java.util.ArrayList;

/**
 * Created by coreymann on 6/10/13.
 */
public class NodeApplication extends Application {

    private static BluetoothService mBluetoothService;
    public  final ArrayList<BluetoothDevice> mDiscoveredDevices = new ArrayList<BluetoothDevice>();
    public static NodeDevice mActiveNode;

    public static final BluetoothService getService(){
        return mBluetoothService;
    }

    public static final BluetoothService setServiceAPI(BluetoothService api){
        mBluetoothService = api;
        return mBluetoothService;
    }

    public static void setActiveNode(NodeDevice node){ mActiveNode = node; }

    public static NodeDevice getActiveNode(){  return mActiveNode; }
}
