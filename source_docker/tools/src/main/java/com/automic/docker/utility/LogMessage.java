package com.automic.docker.utility;

import java.util.Arrays;


/**
 * 
 * Class that denotes a Message to be logged.
 *
 */
public class LogMessage {
    
	private final byte streamId;

	private final byte[] message;
	
	public LogMessage(byte streamId, byte[] message) {
        super();
        byte[] msg = Arrays.copyOf(message, message.length);
        byte stream = streamId;
        
        this.streamId = stream;
        this.message = msg;
    }
	
	public byte getStreamId() {
		return streamId;
	}

	public byte[] getMessage() {
		return Arrays.copyOf(message, message.length);
	}
	
}
