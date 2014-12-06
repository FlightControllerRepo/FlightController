package com.cs492.mavlink_connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.LogManager.LogSeverity;

/**
 * Base for mavlink connection implementations.
 */
public abstract class MavLinkConnection {

	/*
	 * MavLink connection states
	 */
	public static final int MAVLINK_DISCONNECTED = 0;
	public static final int MAVLINK_CONNECTING = 1;
	public static final int MAVLINK_CONNECTED = 2;

	/**
	 * Size of the buffer used to read messages from the mavlink connection.
	 */
	private static final int READ_BUFFER_SIZE = 4096;

	/**
	 * Maximum possible sequence number for a packet.
	 */
	private static final int MAX_PACKET_SEQUENCE = 255;

	/**
	 * Set of listeners subscribed to this mavlink connection. We're using a
	 * ConcurrentSkipListSet because the object will be accessed from multiple
	 * threads concurrently.
	 */
	private final ArrayList<MavLinkConnectionListener> listeners_;

	/**
	 * Queue the set of packets to send via the mavlink connection. A thread
	 * will be blocking on it until there's element(s) available to send.
	 */
	private final LinkedBlockingQueue<MAVLinkPacket> packetsToSend_;

	private final AtomicInteger mConnectionStatus;

	/**
	 * Listen for incoming data on the mavlink connection.
	 */
	private final Runnable mConnectingTask = new Runnable() {

		@Override
		public void run() {
			Thread sendingThread = null;

			// Load the connection specific preferences
			loadPreferences();
			try {
				// Open the connection
				openConnection();
				mConnectionStatus.set(MAVLINK_CONNECTED);
				reportConnect();

				// Launch the 'Sending', and 'Logging' threads
				sendingThread = new Thread(mSendingTask, "MavLinkConnection-Sending Thread");
				sendingThread.start();

				final Parser parser = new Parser();
				parser.stats.mavlinkResetStats();

				final byte[] readBuffer = new byte[READ_BUFFER_SIZE];
				while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
					int bufferSize = readDataBlock(readBuffer);
					handleData(parser, bufferSize, readBuffer);
				}
			} catch (IOException e) {
				LogManager.INSTANCE.addEntry("Exception when connecting:" + 
						LogManager.stringFromException(e), LogSeverity.ERROR);
				// Ignore errors while shutting down
				if (mConnectionStatus.get() != MAVLINK_DISCONNECTED) {

				}
			} finally {
				if (sendingThread != null && sendingThread.isAlive()) 
					sendingThread.interrupt();

				disconnect();
			}
		}

		private void handleData(Parser parser, int bufferSize, byte[] buffer) {
			if (bufferSize < 1) {
				return;
			}

			for (int i = 0; i < bufferSize; i++) {
				MAVLinkPacket receivedPacket = parser.mavlink_parse_char(buffer[i] & 0x00ff);
				if (receivedPacket != null) {
					MAVLinkMessage msg = receivedPacket.unpack();
					reportReceivedMessage(msg);
				}
			}
		}
	};

	/**
	 * Blocks until there's packet(s) to send, then dispatch them.
	 */
	private final Runnable mSendingTask = new Runnable() {
		@Override
		public void run() {
			int msgSeqNumber = 0;

			try {
				while (mConnectionStatus.get() == MAVLINK_CONNECTED) {
					final MAVLinkPacket packet = packetsToSend_.take();
					packet.seq = msgSeqNumber;
					byte[] buffer = packet.encodePacket();

					try {
						sendBuffer(buffer);
					} catch (IOException e) {
						
					}

					msgSeqNumber = (msgSeqNumber + 1) % (MAX_PACKET_SEQUENCE + 1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				disconnect();
			}
		}
	};

	private Thread mTaskThread;

	public MavLinkConnection() {
		listeners_ = new ArrayList<MavLinkConnectionListener>();
		packetsToSend_ = new LinkedBlockingQueue<MAVLinkPacket>();
		mConnectionStatus = new AtomicInteger(MAVLINK_DISCONNECTED);
	}
	
	/**
	 * Establish a mavlink connection. If the connection is successful, it will
	 * be reported through the MavLinkConnectionListener interface.
	 */
	public void connect() {
		if (mConnectionStatus.compareAndSet(MAVLINK_DISCONNECTED, MAVLINK_CONNECTING)) {
			mTaskThread = new Thread(mConnectingTask, "MavLinkConnection-Connecting Thread");
			mTaskThread.start();
		}
	}

	/**
	 * Disconnect a mavlink connection. If the operation is successful, it will
	 * be reported through the MavLinkConnectionListener interface.
	 */
	public void disconnect() {
		if (mConnectionStatus.get() == MAVLINK_DISCONNECTED || mTaskThread == null) {
			return;
		}

		try {
			mConnectionStatus.set(MAVLINK_DISCONNECTED);
			if (mTaskThread.isAlive() && !mTaskThread.isInterrupted()) {
				mTaskThread.interrupt();
			}

			closeConnection();
			reportDisconnect();
		} catch (IOException e) {
			reportComError(e.getMessage());
		}
	}

	public int getConnectionStatus() {
		return mConnectionStatus.get();
	}

	public void sendMavPacket(MAVLinkPacket packet) {
		if (!packetsToSend_.offer(packet)) {
			
		}
	}

	/**
	 * Adds a listener to the mavlink connection.
	 * 
	 * @param listener
	 * @param tag
	 *            Listener tag
	 */
	public void addMavLinkConnectionListener(MavLinkConnectionListener listener) {
		synchronized (listeners_) {
			listeners_.add(listener);
			if (getConnectionStatus() == MAVLINK_CONNECTED) {
				listener.onConnect();
			}
		}
	}

	/**
	 * Removes the specified listener.
	 * 
	 * @param tag
	 *            Listener tag
	 */
	public void removeMavLinkConnectionListener(MavLinkConnectionListener listener) {
		synchronized (listeners_) {
			listeners_.remove(listener);
		}
	}

	protected abstract void openConnection() throws IOException;

	protected abstract int readDataBlock(byte[] buffer) throws IOException;

	protected abstract void sendBuffer(byte[] buffer) throws IOException;

	protected abstract void closeConnection() throws IOException;

	protected abstract void loadPreferences();

	/**
	 * Utility method to notify the mavlink listeners about communication
	 * errors.
	 * 
	 * @param errMsg
	 */
	private void reportComError(String errMsg) {
		LogManager.INSTANCE.addEntry("Reporting Error:" + errMsg, LogSeverity.INFO);
		synchronized (listeners_) {
			for (MavLinkConnectionListener listener : listeners_) {
				listener.onComError(errMsg);
			}
		}
	}

	/**
	 * Utility method to notify the mavlink listeners about a successful
	 * connection.
	 */
	private void reportConnect() {
		LogManager.INSTANCE.addEntry("Reporting Connection", LogSeverity.INFO);
		synchronized (listeners_) {
			for (MavLinkConnectionListener listener : listeners_) 
				listener.onConnect();
		}
	}

	/**
	 * Utility method to notify the mavlink listeners about a connection
	 * disconnect.
	 */
	private void reportDisconnect() {
		LogManager.INSTANCE.addEntry("Reporting Disconnection", LogSeverity.INFO);
		synchronized (listeners_) {
			for (MavLinkConnectionListener listener : listeners_) 
				listener.onDisconnect();
		}
	}

	/**
	 * Utility method to notify the mavlink listeners about received messages.
	 * 
	 * @param msg
	 *            received mavlink message
	 */
	private void reportReceivedMessage(MAVLinkMessage msg) {		
		//LogManager.INSTANCE.addEntry("Reporting Received Message:" + msg, LogSeverity.INFO);
		synchronized (listeners_) {
			for (MavLinkConnectionListener listener : listeners_) 
				listener.onReceiveMessage(msg);
		}
	}

}
