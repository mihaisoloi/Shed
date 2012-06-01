package org.mediation;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import org.communication.SocketMediator;
import org.gui.Shed;

public abstract class Client {

	protected Socket socket;
	protected Socket altSocket;
	public SocketMediator sm;
	public ServerSocket ss;
	public synchronized Socket getAltSocket() {
		return altSocket;
	}

	public void setAltSocket(Socket altSocket) {
		this.altSocket = altSocket;
	}

	public Shed gui;
	protected Semaphore available = new Semaphore(1, true);

	private boolean used = false;

	public Socket getItem() throws InterruptedException {
		available.acquire();
		return getSocket();
	}

	public void putItem(Socket socket) {
		if (markAsUnused(socket))
			available.release();
	}

	private boolean markAsUnused(Socket socket) {
		if (used) {
			used = false;
			return true;
		} else
			return false;
	}

	public void setGUI(Shed shed) {
		gui = shed;
	}

	protected void setSocket(Socket socket) {
		this.socket = socket;
	}

	private synchronized Socket getSocket() {
		used = true;
		return socket;
	}

	public void sendToAllTs(Object entry) throws IOException {
		ObjectOutputStream os = null;
		for (Socket peer : sm.getPeers(false)) {
			if (peer.getPort() != ss.getLocalPort()) {
				OutputStream ous = peer.getOutputStream();
				os = new ObjectOutputStream(ous);
				os.writeObject(entry);
				os.flush();
			}
		}
	}

	public abstract void setKeyPressed(char c, int pos);

}
