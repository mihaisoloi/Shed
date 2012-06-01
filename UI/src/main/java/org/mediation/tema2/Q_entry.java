package org.mediation.tema2;

import java.io.Serializable;

public class Q_entry implements Serializable {

	private static final long serialVersionUID = -9128475055284664201L;
	/** the application message */
	char M;
	/** the application message position */
	int pos = -1;
	/** unique message identifier */
	int tag;
	/** sender of the message */
	int sender_id;
	/** receiver of the message */
	int receiver_id;
	/** tentative timestamp assigned to message */
	int timestamp = -1;
	/** whether message is ready for delivery */
	boolean deliverable;

	String str;

	public Q_entry(char M, int pos, int sender_id, int tag, int timestamp) {
		this(sender_id, tag, timestamp);
		this.pos = pos;
		this.M = M;
		str = "" + 4 * 4 + 2;
	}

	public Q_entry(int sender_id, int receiver_id, int tag, int timestamp) {
		this(sender_id, tag, timestamp);
		this.receiver_id = receiver_id;
		str = "" + 4 * 3;
	}

	public Q_entry(int sender_id, int tag, int timestamp) {
		this.sender_id = sender_id;
		this.tag = tag;
		this.timestamp = timestamp;
		str = "" + 4 * 3;
	}

	public Q_entry(char M, int pos, int tag, int sender_id, int timestamp, boolean deliverable) {
		this.M = M;
		this.pos = pos;
		this.tag = tag;
		this.sender_id = sender_id;
		this.timestamp = timestamp;
		this.deliverable = deliverable;
		str = "" + 4 * 4 + 2 + 1;
	}

	@Override
	public String toString() {
		return str;
	}

}