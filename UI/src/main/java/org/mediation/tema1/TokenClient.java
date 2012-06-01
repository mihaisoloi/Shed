package org.mediation.tema1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import org.communication.SocketMediator;
import org.gui.Shed;
import org.mediation.Client;

public class TokenClient extends Client implements Runnable {

	SocketMediator sm;
	private ServerSocket ss;
	private boolean token;

	public TokenClient(int port, SocketMediator sm) {
		this.sm = sm;
		gui = new Shed(this);
		try {
			ss = new ServerSocket(port);
			this.sm.registerPeer(new Socket("127.0.0.1", port), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		SocketMediator sm = new SocketMediator();
		final TokenClient tc = new TokenClient(5891, sm);
		tc.token = true;
		new Thread(tc).start();
		new Thread(new TokenClient(5892, sm)).start();
		new Thread(new TokenClient(5893, sm)).start();
	}

	@Override
	public void run() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					setKeyPressed('|', 1);
				}

			}
		}).start();
		while (true) {
			try {
				socket = ss.accept();
				new Thread(new TokenMessageListener()).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void setKeyPressed(char c, int pos) {

		if (token) {
			DataOutputStream dos = null;
			// inseram inainte de a trimite catre ceilalti peeri
			Vector<Socket> peers = sm.getPeers(false);
			if (c != '|') {
				gui.insertDistributed(c, pos);
				try {
					for (Socket peer : peers) {
						if (peer.getPort() != ss.getLocalPort()) {
							dos = new DataOutputStream(peer.getOutputStream());
							dos.writeBoolean(token);
							dos.writeChar(c);
							dos.writeInt(pos);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Socket sock = peers.get((int) (Math.random() * 3) % peers.size());
				if (ss.getLocalPort() != sock.getPort())
					try {
						dos = new DataOutputStream(sock.getOutputStream());
						dos.writeBoolean(token);
						token = false;
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

	class TokenMessageListener implements Runnable {

		@Override
		public void run() {
			DataInputStream dis;
			char c;
			int pos;
			try {
				while (true) {
					dis = new DataInputStream(socket.getInputStream());
					token = dis.readBoolean();// asteapta pana cand citeste
												// byte-ul
					c = dis.readChar();
					pos = dis.readInt();
					if (c == '\b')
						gui.removeDistributed(pos);
					else
						gui.insertDistributed(c, pos);
					// dos = new DataOutputStream(socket.getOutputStream());
					// dos.writeBoolean(token);
					// dos.flush();
					token = false;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
