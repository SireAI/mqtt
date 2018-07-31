package com.jd.im.mqtt;

public class MultiByteInteger {
	private long value;
	private int length;
	
	public MultiByteInteger(long value) {
		this(value, -1);
	}
	
	public MultiByteInteger(long value, int length) {
		this.value = value;
		this.length = length;
	}
	
	/**
	 * @return the number of bytes read when decoding this MBI.
	 */
	public int getEncodedLength() {
		return length;
	}

	/**
	 * @return the value of this MBI.
	 */
	public long getValue() {
		return value;
	}
}
