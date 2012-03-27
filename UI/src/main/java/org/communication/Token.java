package org.communication;

public class Token {

	char c;
	int pos;
	int timestamp = 0;

	Token(char c, int pos) {
		this.c = c;
		this.pos = pos;
	}

	public char getChar() {
		return c;
	}

	public int getPos() {
		return pos;
	}

	void incrementTimestamp() {
		timestamp++;
	}
}
