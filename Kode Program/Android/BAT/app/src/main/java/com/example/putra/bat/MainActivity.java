package com.example.putra.bat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mbtnloginMain;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private Switch mBluetoothSwitch;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private static String message = new String();

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    int iterator = 0;

    public static BigInteger e2;
    public static BigInteger n2;
    public static BigInteger r2;
    public static BigInteger key;
    public static int RSAend = 0;
    public static int PublickKeyEnd = 0;

    int count = 3;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        mbtnloginMain = (Button) findViewById(R.id.loginMain);
        mBluetoothSwitch = (Switch) findViewById(R.id.SwitchBT);
        mBluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);
        mDiscoverBtn = (Button)findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button)findViewById(R.id.PairedBtn);

        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView)findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        mbtnloginMain.setEnabled(false);

        mbtnloginMain.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view){
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_login,null);
                final EditText mPassword = (EditText)mView.findViewById(R.id.txtPass);
                Button mLogin = (Button)mView.findViewById(R.id.btnLogin);
                final String TPass = "123321";
                final String TPassFalse = "1102983";

                mLogin.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        if (mPassword.getText().toString().isEmpty()||mPassword.length()!=6){
                            Toast.makeText(MainActivity.this,
                                    "Password Terdiri Dari 6 Angka",
                                    Toast.LENGTH_SHORT).show();
                                message = mPassword.getText().toString().trim();
                            count--;
                            if(count == 0){
                                //mConnectedThread.write("f");
                                /*tes*/
                                mConnectedThread.write("k" + AES.encrypt(TPassFalse,String.valueOf(key)));
                            }
                        }else if(mPassword.getText().toString().equals(TPass)){
                            Toast.makeText(MainActivity.this,
                                    "Login Berhasil",
                                    Toast.LENGTH_SHORT).show();
                            //mConnectedThread.write("t");
                            /*tes*/
                            mConnectedThread.write("k" + AES.encrypt(TPass,String.valueOf(key)));
                        }else if(!mPassword.getText().toString().equals(TPass)){
                            Toast.makeText(MainActivity.this,
                                    "Password Salah",
                                    Toast.LENGTH_SHORT).show();
                            count--;
                            if(count == 0){
                                //mConnectedThread.write("f");
                                /*tes*/
                                mConnectedThread.write("k" + AES.encrypt(TPassFalse,String.valueOf(key)));
                            }
                        }
                    }
                });
                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });
        mHandler = new Handler(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    Log.d("MESSAGE_READ", " MESAGE FROM ARDUINO RECEIVED");
                    String readMessage = (String)msg.obj;
                    String readMessageValue;
                    iterator++ ;
                    Log.d("MESSAGE_READ", "Message" + iterator + ": " + readMessage);
                    //public key process
                    if (readMessage.charAt(0) == 'E'){//recive value e from arduino
                        readMessageValue = readMessage.replace("E","");
                        Log.d("Arduino", readMessageValue);
                        e2 = new BigInteger(readMessageValue);
                        Toast.makeText(MainActivity.this,"E" + e2,Toast.LENGTH_SHORT).show();
                        mConnectedThread.write("n" + String.valueOf(RSA.n));//send value n to arduino
                    }if (readMessage.charAt(0) == 'N'){//recive value n from arduino
                        readMessageValue = readMessage.replace("N","");
                        n2 = new BigInteger(readMessageValue);
                        Toast.makeText(getApplicationContext(),"N" + n2,Toast.LENGTH_SHORT).show();
                        mConnectedThread.write("r" + String.valueOf(RSA.encrypt(RSA.r,e2,n2)));//send encrypted value r to arduino
                    }if (readMessage.charAt(0) == 'R') {//recive encrypted value r from arduino
                        BigInteger tempR2;
                        String tempR2Str;
                        readMessageValue = readMessage.replace("R", "");
                        tempR2Str = readMessageValue;
                        tempR2 = new BigInteger(readMessageValue);
                        r2 = RSA.decrypt(tempR2, RSA.d, RSA.n);//decript value r
                        Toast.makeText(getApplicationContext(), "R" + r2, Toast.LENGTH_SHORT).show();
                        RSAend = 1;
                        if (RSAend == 1){//if RSA process end
                            DH.main();//start Deffie Hellman Process
                            Log.d("valRAndroid", String.valueOf(RSA.r));
                            Log.d("valRAndroidEnc", String.valueOf(RSA.encrypt(RSA.r,e2,n2)));
                            Log.d("valRArduino", String.valueOf(r2));
                            Log.d("valRArduinoEnc", String.valueOf(tempR2));
                            Log.d("valRArduinoEncStr", tempR2Str);
                            Log.d("valP", String.valueOf(DH.p));
                            Log.d("valQ", String.valueOf(DH.q));
                            Log.d("valX", String.valueOf(DH.ValX));
                        }
                    }if (readMessage.charAt(0) == 'Y'){
                        Log.d("masukY", readMessage.replace("Y",""));
                        BigInteger tempY;
                        readMessageValue = readMessage.replace("Y","");
                        tempY = new BigInteger(readMessageValue);
                        key = tempY.modPow(DH.x, DH.q);
                        Toast.makeText(getApplicationContext(),"Y" + tempY,Toast.LENGTH_SHORT).show();
                        mConnectedThread.write("x" + String.valueOf(DH.ValX));
                        //Log.d("valP", String.valueOf(DH.p));
                        //Log.d("valQ", String.valueOf(DH.q));
                        Log.d("valY", String.valueOf(tempY));
                        //Log.d("valX", String.valueOf(DH.ValX));
                        Log.d("keyStream", String.valueOf(key));
                        PublickKeyEnd = 1;
                    }
                    else{
                        mReadBuffer.setText(readMessage);
                    }
                }
                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1) {//if bluetooth connected to arduino hc 05
                        mBluetoothStatus.setText("Connected to Device: " + (String) ( msg.obj ));
                        mbtnloginMain.setEnabled(true);
                        RSA.main();//start RSA process
                        mConnectedThread.write("e" + String.valueOf(RSA.e));//send e value to arduino
                    }else {
                        mBluetoothStatus.setText("Connection Failed");
                        mbtnloginMain.setEnabled(false);
                    }
                }
            }
        };
        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {
            mBluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        // If the switch button is on
                        bluetoothOn();
                    }
                    else {
                        // If the switch button is off
                        bluetoothOff();
                    }
                }
            });
            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });
            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });
        }
    }
    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            }
            else
                mBluetoothStatus.setText("Disabled");
        }
    }
    @SuppressLint("MissingPermission")
    private void bluetoothOn(){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("MissingPermission")
    private void bluetoothOff(){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }
    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }
    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }
    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }
            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);
            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;
                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            //byte[] buffer = new byte[1024];  // buffer store for the stream
            //int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                /*try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }*/
                SystemClock.sleep(100);
                try {
                    readFromInputStream(mmInStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // read input stream from API web services and return it to human readable format
        private String readFromInputStream(InputStream inputStream) throws IOException {
            // to store information from input stream
            StringBuilder output = new StringBuilder();
            // read input stream
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            // represent input stream to human readable text format
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            mHandler.obtainMessage(MESSAGE_READ, line).sendToTarget(); // Send the obtained bytes to the UI activity
            while (line != null){
                output.append(line);
                line = bufferedReader.readLine();
                mHandler.obtainMessage(MESSAGE_READ, line).sendToTarget(); // Send the obtained bytes to the UI activity
            }
            // return information
            return  output.toString();
        }
        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
