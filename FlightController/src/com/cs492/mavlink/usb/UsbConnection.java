package com.cs492.mavlink.usb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.LogManager.LogSeverity;
import com.cs492.mavlink_connection.MavLinkConnection;

public class UsbConnection extends MavLinkConnection {

	private static final int FTDI_DEVICE_VENDOR_ID = 0x0403;

	private Context mContext;
	protected int mBaudRate = 57600;

	private UsbConnectionImpl mUsbConnection;

	public UsbConnection(Context context) {
		mContext = context;
		mBaudRate = 57600;
	}

	@Override
	protected void closeConnection() throws IOException {
		if (mUsbConnection != null) {
			mUsbConnection.closeUsbConnection();
		}
	}

	@Override
	protected void loadPreferences() {
		String baud_type = "57600";//prefs.getString("pref_baud_type", "57600");
		if (baud_type.equals("38400"))
			mBaudRate = 38400;
		else if (baud_type.equals("57600"))
			mBaudRate = 57600;
		else
			mBaudRate = 115200;
		
		mBaudRate = 57600;
	}

	@Override
	protected void openConnection() throws IOException {
		if (mUsbConnection != null) {
			try {
				mUsbConnection.openUsbConnection();
				LogManager.INSTANCE.addEntry("Reusing previous usb connection.", LogSeverity.INFO);
				return;
			} catch (IOException e) {
				LogManager.INSTANCE.addEntry("Previous usb connection is not usable:" + e.toString(), LogSeverity.WARNING);
				mUsbConnection = null;
			}
		}

		if (isFTDIdevice(mContext)) {
			final UsbConnectionImpl tmp = new UsbFTDIConnection(mContext, mBaudRate);
			try {
				tmp.openUsbConnection();

				// If the call above is successful, 'mUsbConnection' will be
				// set.
				mUsbConnection = tmp;
				LogManager.INSTANCE.addEntry("Using FTDI usb connection.", LogSeverity.INFO);
			} catch (IOException e) {
				LogManager.INSTANCE.addEntry("Unable to open a ftdi usb connection. Falling back to the open "
						+ "usb-library:" + LogManager.stringFromException(e), LogSeverity.WARNING);
			}
		}

		// Fallback
		if (mUsbConnection == null) {
			final UsbConnectionImpl tmp = new UsbCDCConnection(mContext, mBaudRate);

			// If an error happens here, let it propagate up the call chain
			// since this is the fall back.
			tmp.openUsbConnection();
			mUsbConnection = tmp;
			LogManager.INSTANCE.addEntry("Using open-source usb connection.", LogSeverity.INFO);
		}
	}

	private static boolean isFTDIdevice(Context context) {
		UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		if (deviceList == null || deviceList.isEmpty()) {
			return false;
		}

		for (Entry<String, UsbDevice> device : deviceList.entrySet()) {
			LogManager.INSTANCE.addEntry("Device vendor:" + device.getValue().getProductId(), LogSeverity.ERROR);
			if (device.getValue().getVendorId() == FTDI_DEVICE_VENDOR_ID) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected int readDataBlock(byte[] buffer) throws IOException {
		if (mUsbConnection == null) {
			throw new IOException("Uninitialized usb connection.");
		}

		return mUsbConnection.readDataBlock(buffer);
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		if (mUsbConnection == null) {
			throw new IOException("Uninitialized usb connection.");
		}

		mUsbConnection.sendBuffer(buffer);
	}

	@Override
	public String toString() {
		if (mUsbConnection == null) 
			return "connection not open";

		return mUsbConnection.toString();
	}

	static abstract class UsbConnectionImpl {
		protected final int mBaudRate;
		protected final Context mContext;

		protected UsbConnectionImpl(Context context, int baudRate) {
			mContext = context;
			mBaudRate = baudRate;
		}

		protected abstract void closeUsbConnection() throws IOException;

		protected abstract void openUsbConnection() throws IOException;

		protected abstract int readDataBlock(byte[] readData) throws IOException;

		protected abstract void sendBuffer(byte[] buffer);
	}
}
