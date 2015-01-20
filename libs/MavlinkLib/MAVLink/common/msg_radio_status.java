        // MESSAGE RADIO_STATUS PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Status generated by radio
        */
        public class msg_radio_status extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_RADIO_STATUS = 109;
        public static final int MAVLINK_MSG_LENGTH = 9;
        private static final long serialVersionUID = MAVLINK_MSG_ID_RADIO_STATUS;
        
        
         	/**
        * receive errors
        */
        public short rxerrors;
         	/**
        * count of error corrected packets
        */
        public short fixed;
         	/**
        * local signal strength
        */
        public byte rssi;
         	/**
        * remote signal strength
        */
        public byte remrssi;
         	/**
        * how full the tx buffer is as a percentage
        */
        public byte txbuf;
         	/**
        * background noise level
        */
        public byte noise;
         	/**
        * remote background noise level
        */
        public byte remnoise;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_RADIO_STATUS;
        		packet.payload.putShort(rxerrors);
        		packet.payload.putShort(fixed);
        		packet.payload.putByte(rssi);
        		packet.payload.putByte(remrssi);
        		packet.payload.putByte(txbuf);
        		packet.payload.putByte(noise);
        		packet.payload.putByte(remnoise);
        
		return packet;
        }
        
        /**
        * Decode a radio_status message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.rxerrors = payload.getShort();
        	    this.fixed = payload.getShort();
        	    this.rssi = payload.getByte();
        	    this.remrssi = payload.getByte();
        	    this.txbuf = payload.getByte();
        	    this.noise = payload.getByte();
        	    this.remnoise = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_radio_status(){
    	msgid = MAVLINK_MSG_ID_RADIO_STATUS;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_radio_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RADIO_STATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("com.MAVLink", "RADIO_STATUS");
        //Log.d("MAVLINK_MSG_ID_RADIO_STATUS", toString());
        }
        
                      
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_RADIO_STATUS -"+" rxerrors:"+rxerrors+" fixed:"+fixed+" rssi:"+rssi+" remrssi:"+remrssi+" txbuf:"+txbuf+" noise:"+noise+" remnoise:"+remnoise+"";
        }
        }
        