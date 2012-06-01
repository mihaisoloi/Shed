package org.mediation.tema3;

import java.io.Serializable;
import java.util.Arrays;

public class DoptRequest implements Serializable {

	private static final long serialVersionUID = -1569575280234137337L;
	final int identifier;
	int[] stateVector;
	char m;
	int pos = -1;
	final int priority;
	public DoptRequest(int identifier, int[] stateVector, char m, int pos, int priority) {
		super();
		this.identifier = identifier;
		this.stateVector = stateVector;
		this.m = m;
		this.pos = pos;
		this.priority = priority;
	}
	@Override
	public String toString() {
		return "DoptRequest [identifier=" + identifier + ", stateVector=" + Arrays.toString(stateVector) + ", m=" + m + ", pos=" + pos + ", priority=" + priority + "]";
	}
}
