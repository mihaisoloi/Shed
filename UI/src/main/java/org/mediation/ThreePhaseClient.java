package org.mediation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import org.communication.SocketMediator;
import org.communication.Token;
import org.gui.Shed;

public class ThreePhaseClient extends Client {

	Vector<Token> buffer = new Vector<Token>();
	SocketMediator sm;

	public ThreePhaseClient(final Socket socket, SocketMediator sm) {
		this.socket = socket;
		// GUI-ul este Observer
		gui = new Shed(this);
		this.sm = sm;
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		SocketMediator sockMed = new SocketMediator();
		new ThreePhaseClient(new Socket("127.0.0.1", 5891), sockMed).start();
		new ThreePhaseClient(new Socket("127.0.0.1", 5892), sockMed).start();
		new ThreePhaseClient(new Socket("127.0.0.1", 5893), sockMed).start();
		new ThreePhaseClient(new Socket("127.0.0.1", 5894), sockMed).start();
		new ThreePhaseClient(new Socket("127.0.0.1", 5895), sockMed).start();
	}

	private void start() {
		new Thread(new TokenListener()).run();
	}

	@Override
	public void setKeyPressed(char c, int pos) {
		if (c == '\b')
			gui.removeDistributed(pos);
		else
			gui.insertDistributed(c, pos);
	}

	class TokenListener implements Runnable {

		@Override
		public void run() {
			ObjectInputStream dis;
			Token token;
			char c;
			int pos;
			try {
				while (true) {
					dis = new ObjectInputStream(socket.getInputStream());
					token = (Token) dis.readObject();
					c = token.getChar();
					pos = token.getPos();
					if (c == '\b')
						gui.removeDistributed(pos);
					else
						gui.insertDistributed(c, pos);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
