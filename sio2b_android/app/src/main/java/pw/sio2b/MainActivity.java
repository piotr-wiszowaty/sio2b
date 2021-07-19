//
// Copyright (C) 2014-2021 Piotr Wiszowaty
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//
package pw.sio2b;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import pw.atari.sio.GUIManager;
import pw.atari.sio.IO;
import pw.atari.sio.Logger;
import pw.atari.sio.SIO;

public class MainActivity
        extends AppCompatActivity
        implements Handler.Callback, View.OnLongClickListener, AdapterView.OnItemSelectedListener,
                IO, GUIManager, Logger, IOManager {

    private static final int BT_CHUNK_SIZE = 64;

    private static final int REQUEST_ENABLE_BT = 65022;

    private static final int SCAN_DURATION = 5000;

    private static final int MSG_PROGRESS = 401;
    private static final int MSG_DISMISS  = 402;

    private static final int PCLK = 48000000;
    private static final int F_OSC = 1773447;

    static final boolean debugEnabled = true;

    private static final int MESSAGE_SET_TEXT = 1;
    private static final int MESSAGE_SET_BPS = 2;
    private static final int FILE_CHOOSE_REQUEST_CODE = 65021;
    private static final int FILE_CHOOSE_REQUEST_CODE_D1 = 65023;
    private static final int FILE_CHOOSE_REQUEST_CODE_D2 = 65024;
    private static final int FILE_CHOOSE_REQUEST_CODE_D3 = 65025;
    private static final int FILE_CHOOSE_REQUEST_CODE_D4 = 65026;
    private static final String EXTRA_DISK_NUMBER = "pw.atari.sio.DISK_NUMBER";

    private static final String PREF_DISK_PATH_PREFIX = "pw.atari.sio.path.d";
    private static final String PREF_SIO_HIGH_SPEED_INDEX = "pw.atari.sio.hsindex";

    private static final String DIRNAME = "Atari";

    private SIO sio;

    private ProgressBar progressBar;

    private Button[] diskButtons = new Button[SIO.MAX_DISKS];

    private Pattern hsiExtractPattern = Pattern.compile("\\d+");

    private int calculateBaud(int highSpeedIndex) {
        return (F_OSC/2) / (highSpeedIndex+7);
    }

    private int calculateUBRR(int highSpeedIndex) {
        return PCLK / calculateBaud(highSpeedIndex);
    }

    private ProgressDialog progress;
    private Runnable stopRunnable = new Runnable() {
        public void run() {
            stopScan();
        }
    };
    private Runnable startRunnable = new Runnable() {
        public void run() {
            startScan();
        }
    };
    private BroadcastReceiver deviceFoundReceiver;
    private BluetoothAdapter btAdapter;
    private SparseArray<BluetoothDevice> devices = new SparseArray<BluetoothDevice>();

    private ConnectThread connectThread;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            d("handleMessage() " + msg);
            switch (msg.what) {
                case MSG_PROGRESS:
                    setProgressBarIndeterminateVisibility(true);
                    progress.setMessage((String) msg.obj);
                    progress.show();
                    break;
                case MSG_DISMISS:
                    progress.hide();
                    setProgressBarIndeterminateVisibility(false);
                    break;
            }
        }
    };

    protected boolean startup() {
        setProgressBarIndeterminate(true);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        deviceFoundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    d("found device: " + device.getName() + " " + device.getAddress());
                    devices.put(device.hashCode(), device);
                    invalidateOptionsMenu();
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(deviceFoundReceiver, filter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return false;
        }
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return true;
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            d("paired " + device.getName() + " " + device.getAddress());
            devices.put(device.hashCode(), device);
        }
        if (pairedDevices.size() > 0) {
            invalidateOptionsMenu();
        }

        File path = Environment.getExternalStoragePublicDirectory(DIRNAME);
        path.mkdirs();
        d("created " + path.getPath());

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        progress.dismiss();
        handler.removeCallbacks(stopRunnable);
        handler.removeCallbacks(startRunnable);
        stopScan();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int diskNumber = -1;

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != RESULT_OK) {
                    finish();
                }
                break;
            case FILE_CHOOSE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    if ("file".equalsIgnoreCase(uri.getScheme())) {
                        diskNumber = data.getIntExtra(EXTRA_DISK_NUMBER, 1);
                        sio.loadDiskImage(uri.getPath(), diskNumber);
                        String path = sio.getPath(diskNumber);
                        showText("D" + diskNumber + ":" + (path != null ? path : ""));
                        diskNumber = -1;
                    } else {
                        // Ignore content other than files
                    }
                }
                break;
            case FILE_CHOOSE_REQUEST_CODE_D1:
                diskNumber = 1;
                break;
            case FILE_CHOOSE_REQUEST_CODE_D2:
                diskNumber = 2;
                break;
            case FILE_CHOOSE_REQUEST_CODE_D3:
                diskNumber = 3;
                break;
            case FILE_CHOOSE_REQUEST_CODE_D4:
                diskNumber = 4;
                break;
        }
        if (diskNumber >= 0) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                try {
                    String[] tokens = uri.getLastPathSegment().split(":");
                    String contentPath = tokens[1].replace(DIRNAME, Environment.getExternalStoragePublicDirectory(DIRNAME).getPath());
                    d("content path: " + contentPath);
                    sio.loadDiskImage(contentPath, diskNumber);
                    String path = sio.getPath(diskNumber);
                    showText("D" + diskNumber + ":" + (path != null ? path : ""));
                } catch (Exception e) {
                    e(e.getMessage(), e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_scan:
                startScan();
                return true;
            default:
                BluetoothDevice device = devices.get(item.getItemId());
                if (device != null) {
                    if (connectThread != null) {
                        connectThread.cancel();
                    }
                    String msg = "connecting to " + device.getName() + " " + device.getAddress();
                    handler.sendMessage(Message.obtain(null, MSG_PROGRESS, msg));
                    d(msg);
                    connectThread = new ConnectThread(device, this, handler, MSG_DISMISS);
                    connectThread.start();
                    return true;
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
    }

    protected void createOptionsMenu(Menu menu) {
        for (int i = 0; i < devices.size(); i++) {
            BluetoothDevice device = devices.valueAt(i);
            String name = device.getName() + " " + device.getAddress();
            d("adding device [" + i + "]: " + name);
            menu.add(Menu.NONE, devices.keyAt(i), Menu.NONE, name);
        }
    }

    private void startScan() {
        d("startScan()");
        devices.clear();
        btAdapter.startDiscovery();
        handler.postDelayed(stopRunnable, SCAN_DURATION);
        handler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Scanning SIO2B devices"));
    }

    private void stopScan() {
        d("stopScan()");
        btAdapter.cancelDiscovery();
        handler.sendMessage(Message.obtain(null, MSG_DISMISS));
    }

    protected void shutdown() {
        if (deviceFoundReceiver != null) {
            unregisterReceiver(deviceFoundReceiver);
            deviceFoundReceiver = null;
        }
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        return connectThread.read(buffer, offset, length);
    }

    public void write(byte[] buffer, int offset, int length) throws IOException {
        for (int i = 0; i < length; i += BT_CHUNK_SIZE) {
            connectThread.write(buffer, i, Math.min(BT_CHUNK_SIZE, length - i));
        }
    }

    protected String tag() {
        return "SIO2B";
    }

    @Override
    public void e(String message) {
        Log.d(tag(), message);
    }

    @Override
    public void e(String message, Exception exception) {
        Log.d(tag(), message, exception);
    }

    @Override
    public void w(String message) { Log.w(tag(), message); }

    @Override
    public void w(String message, Exception exception) { Log.w(tag(), message, exception); }

    @Override
    public void d(String message) {
        if (debugEnabled) {
            Log.d(tag(), message);
        }
    }

    @Override
    public void d(String message, Exception exception) {
        if (debugEnabled) {
            Log.d(tag(), message, exception);
        }
    }

    @Override
    public Locale getLocale() {
        return getResources().getConfiguration().locale;
    }

    @Override
    public void setDiskLabel(int i, String label, String path) {
        diskButtons[i].setText(label);
    }

    @Override
    public void showMessage(String message) {
        handler.obtainMessage(MESSAGE_SET_TEXT, message).sendToTarget();
    }

    @Override
    public void ioStart() {
        sio.ioStart();
    }

    @Override
    public void ioStop() {
        sio.ioStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        d("onCreate()");

        sio = new SIO(this, this, this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        Button b = (Button) findViewById(R.id.button_d1);
        b.setOnLongClickListener(this);
        diskButtons[0] = b;
        b = (Button) findViewById(R.id.button_d2);
        b.setOnLongClickListener(this);
        diskButtons[1] = b;
        b = (Button) findViewById(R.id.button_d3);
        b.setOnLongClickListener(this);
        diskButtons[2] = b;
        b = (Button) findViewById(R.id.button_d4);
        b.setOnLongClickListener(this);
        diskButtons[3] = b;

        Spinner spinner = (Spinner) findViewById(R.id.sio_speed_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sio_speed_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String path;
        for (int i = 0; i < SIO.MAX_DISKS; i++) {
            String key = PREF_DISK_PATH_PREFIX + (i + 1);
            if ((path = prefs.getString(key, null)) != null) {
                sio.loadDiskImage(path, i + 1);
            }
        }
        String text = prefs.getString(PREF_SIO_HIGH_SPEED_INDEX, null);
        if (text != null) {
            int highSpeedIndex = Integer.parseInt(text);
            sio.setSIOSpeed(highSpeedIndex, calculateUBRR(highSpeedIndex));
            for (int i = 0; i < spinner.getCount(); i++) {
                String item = spinner.getItemAtPosition(i).toString();
                Matcher matcher = hsiExtractPattern.matcher(item);
                matcher.find();
                if (highSpeedIndex == Integer.parseInt(matcher.group())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        handler = new Handler(this);

        if (!startup()) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        createOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        d("onStart()");
        sio.start();
    }

    @Override
    public void onStop() {
        super.onStop();

        d("onStop()");
        sio.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        d("onDestroy()");

        // Save state
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < SIO.MAX_DISKS; i++) {
            String path = sio.getPath(i + 1);
            d("Saving " + PREF_DISK_PATH_PREFIX + (i + 1) + " " + path);
            editor.putString(PREF_DISK_PATH_PREFIX + (i + 1), path);
        }
        // Save selected SIO speed
        editor.putString(PREF_SIO_HIGH_SPEED_INDEX, Integer.toString(sio.getHighSpeedIndex()));
        editor.commit();

        shutdown();

        // Close disk images
        for (int i = 0; i < SIO.MAX_DISKS; i++) {
            sio.eject(i + 1);
        }
    }


    private void showText(String text) {
        TextView v = (TextView) findViewById(R.id.message_buffer);
        v.setText(text);
    }

    private void selectFile(int diskNumber) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent("org.openintents.action.PICK_FILE");
            intent.putExtra(EXTRA_DISK_NUMBER, diskNumber);
            try {
                startActivityForResult(Intent.createChooser(intent, "Select file"), FILE_CHOOSE_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
            }
        } else {
            int requestCode = diskNumber == 1 ? FILE_CHOOSE_REQUEST_CODE_D1 :
                              diskNumber == 2 ? FILE_CHOOSE_REQUEST_CODE_D2 :
                              diskNumber == 3 ? FILE_CHOOSE_REQUEST_CODE_D3 :
                              diskNumber == 4 ? FILE_CHOOSE_REQUEST_CODE_D4 : -1;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select file"), requestCode);
        }
    }

    public void selectD1(View view) {
        selectFile(1);
    }

    public void selectD2(View view) {
        selectFile(2);
    }

    public void selectD3(View view) {
        selectFile(3);
    }

    public void selectD4(View view) {
        selectFile(4);
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        if (id == R.id.button_d1) {
            showText("Eject D1");
            sio.eject(1);
            return true;
        } else if (id == R.id.button_d2) {
            showText("Eject D2");
            sio.eject(2);
            return true;
        } else if (id == R.id.button_d3) {
            showText("Eject D3");
            sio.eject(3);
            return true;
        } else if (id == R.id.button_d4) {
            showText("Eject D4");
            sio.eject(4);
            return true;
        } else {
            return false;
        }
    }

    public boolean handleMessage(Message message) {
        if (message.what == MESSAGE_SET_TEXT) {
            TextView v = (TextView) findViewById(R.id.message_buffer);
            v.setText((String) message.obj);
            return true;
        } else if (message.what == MESSAGE_SET_BPS) {
            TextView v = (TextView) findViewById(R.id.bps_display);
            v.setText((String) message.obj);
            return true;
        }
        return false;
    }

    @Override
    public void updateSIOSpeed(int highSpeedIndex) {
        int baud = calculateBaud(highSpeedIndex);
        String message = String.format("Current HS Index: %2d (%1.1f kbps)", highSpeedIndex, (float) baud / 1000.0);
        handler.obtainMessage(MESSAGE_SET_BPS, message).sendToTarget();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String item = parent.getItemAtPosition(pos).toString();
        d("selected " + item);
        Matcher matcher = hsiExtractPattern.matcher(item);
        matcher.find();
        int highSpeedIndex = Integer.parseInt(matcher.group());
        sio.setSIOSpeed(highSpeedIndex, calculateUBRR(highSpeedIndex));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        d("nothing selected");
    }

    @Override
    public void updateProgress(final int percent) {
        handler.post(new Runnable() {
            public void run() {
                progressBar.setProgress(percent);
            }
        });
    }
}
