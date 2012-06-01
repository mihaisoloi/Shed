package org.mediation.tema3;

import java.io.Serializable;

public class JupiterOperation implements Serializable {
	private static final long serialVersionUID = 4555091790579730064L;
	public final char message;
	public int position;
	public final int myMsgs;
	public final int otherMsgs;

	public JupiterOperation(char message, int position, int myMsgs, int otherMsgs) {
		this.message = message;
		this.position = position;
		this.myMsgs = myMsgs;
		this.otherMsgs = otherMsgs;
	}

	@Override
	public String toString() {
		return "JupiterOperation [message=" + message + ", position=" + position + ", myMsgs=" + myMsgs + ", otherMsgs=" + otherMsgs + "]";
	}

}