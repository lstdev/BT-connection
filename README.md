<img src="https://www.locationsolutions.com/wp-content/uploads/2019/09/location-solutions-logo-1.svg" width="60%" height="60%" />


# Install

Add to your gradle dependencies:

```
implementation 'com.github.lstdev:BT-connection:1.0.0'
```

## Enable bluetooth

**Careful: You also have to enable phone location in newer versions of Android.**

### Asking user for bluetooth activation

```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    // ...
    // Need to ask for bluetooth permissions before calling constructor !
    // Permissions are {BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION}
    bluetooth = new Bluetooth(this);
    bluetooth.setBluetoothCallback(bluetoothCallback);
}

@Override
protected void onStart() {
    super.onStart();
    bluetooth.onStart();
    if(bluetooth.isEnabled()){
        // doStuffWhenBluetoothOn() ...
    } else {
        bluetooth.showEnableDialog(ScanActivity.this);
    }
}

@Override
protected void onStop() {
    super.onStop();
    bluetooth.onStop();
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    bluetooth.onActivityResult(requestCode, resultCode);
}

private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
    @Override public void onBluetoothTurningOn() {}
    @Override public void onBluetoothTurningOff() {}
    @Override public void onBluetoothOff() {}

    @Override
    public void onBluetoothOn() {
        // doStuffWhenBluetoothOn() ...
    }

    @Override
    public void onUserDeniedActivation() {
        // handle activation denial...
    }
};
```

### Without asking user for bluetooth activation

```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    // ...
    // Need to ask for bluetooth permissions before calling constructor !
    // Permissions are {BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION}
    bluetooth = new Bluetooth(this);
    bluetooth.setBluetoothCallback(bluetoothCallback);
}

@Override
protected void onStart() {
    super.onStart();
    bluetooth.onStart();
    if(bluetooth.isEnabled()){
        // doStuffWhenBluetoothOn() ...
    } else {
        bluetooth.enable()
    }
}

@Override
protected void onStop() {
    super.onStop();
    bluetooth.onStop();
}

private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
    @Override public void onBluetoothTurningOn() {}
    @Override public void onBluetoothTurningOff() {}
    @Override public void onBluetoothOff() {}
    @Override public void onUserDeniedActivation() {}
    
    @Override
    public void onBluetoothOn() {
        // doStuffWhenBluetoothOn() ...
    }
};
```

## Discover devices and pair

### Listener

```java
bluetooth.setDiscoveryCallback(new DiscoveryCallback() {
    @Override public void onDiscoveryStarted() {}
    @Override public void onDiscoveryFinished() {}
    @Override public void onDeviceFound(BluetoothDevice device) {}
    @Override public void onDevicePaired(BluetoothDevice device) {}
    @Override public void onDeviceUnpaired(BluetoothDevice device) {}
    @Override public void onError(String message) {}
});
```

### Scan and Pair

```java
bluetooth.startScanning();
bluetooth.pair(device);
bluetooth.pair(device, "optional pin");
```

### Get paired devices

```java
List<BluetoothDevice> devices = bluetooth.getPairedDevices();
```

## Connect to device and communicate

### Listener

```java
bluetooth.setDeviceCallback(new DeviceCallback() {
    @Override public void onDeviceConnected(BluetoothDevice device) {}
    @Override public void onDeviceDisconnected(BluetoothDevice device, String message) {}
    @Override public void onMessage(byte[] message) {}
    @Override public void onError(String message) {}
    @Override public void onConnectError(BluetoothDevice device, String message) {}
});
```
	
### Connect to device

```java
// three options
bluetooth.connectToName("name");
bluetooth.connectToAddress("address");
bluetooth.connectToDevice(device);
```

### Connect to device using port trick

See this post for details: https://stackoverflow.com/a/25647197/5552022
```java
bluetooth.connectToNameWithPortTrick("name");
bluetooth.connectToAddressWithPortTrick("address");
bluetooth.connectToDeviceWithPortTrick(device);
```

*Should be avoided*
	
### Send a message

```java
bluetooth.send("hello, world");
bluetooth.send(new byte[]{61, 62, 63});
```

## Receive messages

The default behavior of the library is the read from the input stream until it hits a new line, it will then propagate the message through listeners as a byte array.
You can change the way the library reads from the socket by creating your own reader class. It must extend from `SocketReader` and you should override the `byte[] read() throws IOException` method. **This method must block. It should not return if no values were received.**

The default behavior is actually one example of implementation :

```java
public class LineReader extends SocketReader{
    private BufferedReader reader;

    public LineReader(InputStream inputStream) {
        super(inputStream);
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public byte[] read() throws IOException {
        return reader.readLine().getBytes();
    }
}
```

This is an implementation for a custom delimiter :

```java
public class DelimiterReader extends SocketReader {
    private PushbackInputStream reader;
    private byte delimiter;

    public DelimiterReader(InputStream inputStream) {
        super(inputStream);
        reader = new PushbackInputStream(inputStream);
        delimiter = 0;
    }

    @Override
    public byte[] read() throws IOException {
        List<Byte> byteList = new ArrayList<>();
        byte[] tmp = new byte[1];

        while(true) {
            int n = reader.read();
            reader.unread(n);

            int count = reader.read(tmp);
            if(count > 0) {
                if(tmp[0] == delimiter){
                    byte[] returnBytes = new byte[byteList.size()];
                    for(int i=0 ; i<byteList.size() ; i++){
                        returnBytes[i] = byteList.get(i);
                    }
                    return returnBytes;
                } else {
                    byteList.add(tmp[0]);
                }
            }
        }
    }
}
```

Then you can use your reader :

```java
bluetooth.setReader(LineReader.class);
```

