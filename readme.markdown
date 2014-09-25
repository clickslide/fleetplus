Framework Release Notes and Changes
==========================

*v1.6*
-------

* StatusListener, ChromaListener, ButtonListener, SensorDetector, DataloggingListener, and ConnectionListener are now invoked on the UI Thread.

* DeltaE CMC 2:1 and CMC 1:1 are now fully supported in VTRGBCReading class.

* Fixed model property on NodeDevice to match product name.

* Fixed null pointer for motion sensor after a NodeDevice has been initialized.

* ConnectionListener callback are now supported by the bluetooth service class.

* More bluetooth enhancements  

* Added a ConnectionFail event to ConnectionListener and supported in BluetoothService.

* Added a ConnectionFail message to be sent through the handler.

* Bug Fixes for Chroma Readings.

* Removed onStatusChanged from ConnectionListener.

* Deprecated methods on StatusListener. Register a sensor detector for those messages.

* Fixed NodeDevice QuietMode member to accurately reflect the state of the NODE.

*v1.5*
------
* Updated ProgressListener callback to accept text based message updates rather than incremental progress updates.

* Added more safety catches to dispatching events allows for more verbose messaging to application code.

* All Bluetooth writes are made asynchrounously. 