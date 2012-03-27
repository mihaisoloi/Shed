package org.mediation;

import java.net.Socket;

import org.gui.Shed;

public abstract class Client {

	Socket socket;
	Shed gui;

	public void setGUI(Shed shed) {
		gui = shed;
	}

	public abstract void setKeyPressed(char c, int pos);

}
