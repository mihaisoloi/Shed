package org.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.mediation.Client;

public class DocumentInsertionFilter extends DocumentFilter {

	private final Client client;
	private static boolean keyPressed = false;

	public void setKeyPressed(boolean keyPressed) {
		DocumentInsertionFilter.keyPressed = keyPressed;
	}

	public DocumentInsertionFilter(Client client) {
		this.client = client;
	}

	@Override
	public void insertString(FilterBypass fb, int offset, String string,
			AttributeSet attr) throws BadLocationException {
		keyPressed = false;
		super.insertString(fb, offset, string, attr);
	}

	@Override
	public void replace(FilterBypass fb, int offs, int length, String str,
			AttributeSet a) throws BadLocationException {
		// pentru inserarea din tastatura
		if (!keyPressed)
			client.setKeyPressed(str.toCharArray()[0], offs);
		else {
			keyPressed = false;
			super.insertString(fb, offs, str, a);
		}
	}

	@Override
	public void remove(FilterBypass fb, int offset, int length)
			throws BadLocationException {
		// pentru backspace
		if (!keyPressed)
			client.setKeyPressed('\b', offset);
		else {
			keyPressed = false;
			super.remove(fb, offset, length);
		}
	}

}
