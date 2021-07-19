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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class ConnectThread extends Thread {
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private IOManager ioManager;
    private Handler handler;
    private int msgDismiss;

    public ConnectThread(BluetoothDevice device, IOManager ioManager, Handler handler, int msgDismiss) {
        super("SIO2B-connect");
        this.ioManager = ioManager;
        this.handler = handler;
        this.msgDismiss = msgDismiss;
        try {
            //socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        boolean success = false;
        try {
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            success = true;
        } catch (IOException e) {
            Log.d("SIO2B", e.getMessage(), e);
        }

        if (!success) {
            try {
                Log.d("SIO2B", "fallback connect");
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                Method m = BluetoothDevice.class.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};
                socket = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                success = true;
            } catch (InvocationTargetException e) {
                Log.e("SIO2B", e.getMessage(), e);
            } catch (IllegalAccessException e) {
                Log.e("SIO2B", e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                Log.e("SIO2B", e.getMessage(), e);
            } catch (IOException e) {
                Log.e("SIO2B", e.getMessage(), e);
            }
        }

        if (success) {
            Log.d("SIO2B", "connected");
            ioManager.ioStart();
        }

        handler.sendMessage(Message.obtain(null, msgDismiss));
    }

    public void cancel() {
        ioManager.ioStop();
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public void write(byte[] buffer, int offset, int length) throws IOException {
        outputStream.write(buffer, offset, length);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        return inputStream.read(buffer, offset, length);
    }
}
