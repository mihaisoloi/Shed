package org.communication;

import java.net.Socket;
import java.util.Vector;

public class SocketMediator {

	Vector<Socket> peers = new Vector<Socket>();
	Vector<Socket> peersAlt = new Vector<Socket>();

	public synchronized void registerPeer(Socket sock, boolean alternate) {
		if (alternate)
			peersAlt.add(sock);
		else
			peers.add(sock);
	}

	/**
	 * helper method to save us from synchronizing: normal mode for sending
	 * alternate for receiving
	 * 
	 * @param alternate
	 * @return
	 */
	public Vector<Socket> getPeers(boolean alternate) {
		if (alternate)
			return peersAlt;
		return peers;
	}

	public Socket getPeerByPortId(int id, boolean alternate) {
		Vector<Socket> peers = alternate ? this.peersAlt : this.peers;
		for (Socket peer : peers)
			if (peer.getPort() == id)
				return peer;
		return null;
	}
}
