package org.communication;

import java.net.Socket;
import java.util.Vector;

public class SocketMediator {

	Vector<Socket> peers = new Vector<Socket>();

	public void registerPeer(Socket sock) {
		peers.add(sock);
	}

	public Vector<Socket> getPeers() {
		return peers;
	}
}
