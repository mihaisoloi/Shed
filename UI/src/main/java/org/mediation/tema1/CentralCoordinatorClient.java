package org.mediation.tema1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.gui.Shed;
import org.mediation.Client;

public class CentralCoordinatorClient extends Client {

	public static int bytesSent = 0;

	public CentralCoordinatorClient(Socket socket) {
		this.socket = socket;
		// GUI-ul este Observer
		gui = new Shed(this);
	}

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		for (int i = 1; i <= 3; i++) {
			final Socket s = new Socket("127.0.0.1", 5891);
			CentralCoordinatorClient ccc = new CentralCoordinatorClient(s);
			new Thread(ccc.new MessageListener()).start();
		}
	}

	@Override
	public synchronized void setKeyPressed(char c, int pos) {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeChar(c);
			dos.writeInt(pos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class MessageListener implements Runnable {

		@Override
		public void run() {
			DataInputStream dis;
			char c;
			int pos;
			try {
				while (true) {
					dis = new DataInputStream(socket.getInputStream());
					bytesSent += 6;
					c = dis.readChar();
					pos = dis.readInt();
					if (c == '\b')
						gui.removeDistributed(pos);
					else
						gui.insertDistributed(c, pos);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
