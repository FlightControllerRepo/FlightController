package com.flightcontroller.mavlink.usb;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.flightcontroller.utils.LogManager;
import com.flightcontroller.utils.LogManager.LogSeverity;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

class UsbCDCConnection extends UsbConnection.UsbConnectionImpl {
	private static final String TAG = UsbCDCConnection.class.getSimpleName();

	private UsbSerialPort port_;

	protected UsbCDCConnection(Context context, int baudRate) {
		super(context, baudRate);
	}

	@Override
	protected void openUsbConnection() throws IOException {
		// Get UsbManager from Android.
		UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

		// Find the first available driver.
        List<UsbSerialDriver> devices = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
		if (devices == null || devices.isEmpty()) {
			LogManager.INSTANCE.addEntry("No Devices found", LogSeverity.WARNING);
			throw new IOException("No Devices found");
		} else {
            UsbSerialDriver driver = devices.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                LogManager.INSTANCE.addEntry("No connection found", LogSeverity.WARNING);
                throw new IOException("No connection found");
            }

            //read something: we know there is a port
            port_ = driver.getPorts().get(0);
			LogManager.INSTANCE.addEntry("Opening using Baud rate " + mBaudRate, LogSeverity.INFO);
			try {
                port_.open(connection);
                port_.setParameters(mBaudRate, 8, UsbSerialPort.STOPBITS_1,
                        UsbSerialPort.PARITY_NONE);
			} catch (IOException e) {
				LogManager.INSTANCE.addEntry("Error setting up device: " + e.getMessage(), LogSeverity.WARNING);
				try {
                    port_.close();
				} catch (IOException e2) {
					// Ignore.
				}
                port_ = null;
			}
		}
	}

	@Override
	protected int readDataBlock(byte[] readData) throws IOException {
		// Read data from driver. This call will return upto readData.length
		// bytes.
		// If no data is received it will timeout after 200ms (as set by
		// parameter 2)
		int iavailable = 0;
		try {
			iavailable = port_.read(readData, 200);
		} catch (NullPointerException e) {
			final String errorMsg = "Error Reading: " + e.getMessage()
					+ "\nAssuming inaccessible USB device.  Closing connection.";
			LogManager.INSTANCE.addEntry(errorMsg, LogSeverity.ERROR);
			throw new IOException(errorMsg, e);
		}

		if (iavailable == 0)
			iavailable = -1;
		return iavailable;
	}

	@Override
	protected void sendBuffer(byte[] buffer) {
		// Write data to driver. This call should write buffer.length bytes
		// if data cant be sent , then it will timeout in 500ms (as set by
		// parameter 2)
		if (port_ != null) {
			try {
                port_.write(buffer, 500);
			} catch (IOException e) {
				LogManager.INSTANCE.addEntry("Error Sending: " + e.getMessage(), LogSeverity.ERROR);
			}
		}
	}

	@Override
	protected void closeUsbConnection() throws IOException {
		if (port_ != null) {
			try {
                port_.close();
			} catch (IOException e) {
				// Ignore.
			}
            port_ = null;
		}
	}

	@Override
	public String toString() {
		return TAG;
	}
}
