
package com.MAVLink.Messages;

import com.MAVLink.MAVLinkPacket;

import java.io.Serializable;

public abstract class MAVLinkMessage implements Serializable {
	private static final long serialVersionUID = -7754622750478538539L;
	// The com.MAVLink message classes have been changed to implement Serializable,
	// this way is possible to pass a mavlink message trought the Service-Acctivity interface
	
	/**
	 *  Simply a common interface for all com.MAVLink Messages
	 */
	
	public  int sysid;
	public int compid;
	public int msgid;
	public abstract MAVLinkPacket pack();
	public abstract void unpack(MAVLinkPayload payload);
}
	