package org.communication;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private final Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
	}

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		Client client = new Client(new Socket(args[0], Integer.parseInt(args[1])));// 176.34.176.163

	}

}
