package org.mediation.tema2;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.communication.SocketMediator;
import org.gui.Shed;
import org.mediation.Client;

public class ThreePhaseClient extends Client implements Runnable {

	public static int bytesSent = 0;
	LinkedList<Q_entry> temp_Q = new LinkedList<Q_entry>();
	public LinkedList<Q_entry> delivery_Q = new LinkedList<Q_entry>();
	public int clock;

	public ThreePhaseClient(int port, SocketMediator sm) {
		// GUI-ul este Observer
		gui = new Shed(this);
		this.sm = sm;
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		SocketMediator sockMed = new SocketMediator();
		int[] ports = {5891, 5892, 5893};
		for (int port : ports)
			new ThreePhaseClient(port, sockMed).initThreads();
	}

	private void initThreads() throws UnknownHostException, IOException, InterruptedException {
		final int port = ss.getLocalPort();
		new Thread(this, "TPC-" + port).start();
		Thread.sleep(100);
		sm.registerPeer(new Socket("127.0.0.1", port), false);
		sm.registerPeer(new Socket("127.0.0.1", port), true);
		Thread receiver = new Thread(this.new Receiver());
		receiver.setPriority(Thread.MIN_PRIORITY);
		receiver.setDaemon(true);
		receiver.start();
		Thread deliveryChecker = new Thread(new DeliveryChecker(delivery_Q, gui, clock));
		deliveryChecker.setPriority(Thread.MAX_PRIORITY);
		deliveryChecker.setDaemon(true);
		deliveryChecker.start();
	}

	@Override
	public void run() {
		try {
			// one socket for receiver
			setSocket(ss.accept());
			// one socket for key listener
			setAltSocket(ss.accept());
			// simply could not synchronize the access to the socket
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * the sending phase
	 */
	@Override
	public void setKeyPressed(char M, int pos) {
		System.out.println(Thread.currentThread().getName());
		Q_entry REVISE_TS;
		Q_entry PROPOSED_TS;
		Q_entry FINAL_TS;
		if (M == '\b')
			gui.removeDistributed(pos);
		else
			gui.insertDistributed(M, pos);
		clock++;

		int temp_ts = -1;
		REVISE_TS = new Q_entry(M, pos, ss.getLocalPort(), (int) (Math.random() * 1000), clock);
		System.out.println("Proposing " + REVISE_TS + " from " + ss.getLocalPort());
		// sending the revise_ts to all peers
		try {
			sendToAllTs(REVISE_TS);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			temp_ts = 0;
		}
		// waiting for the proposed_ts from all peers minus me
		ObjectInputStream ois = null;
		int nrOfReceivedProposedTS = 0;
		try {
			// non-blocking
			System.out.println("~Getting the lock on the socket input stream~");
			InputStream is = getAltSocket().getInputStream();
			ois = new ObjectInputStream(is);
			System.out.println("Reading the general Proposed TimeStamp");
			while (nrOfReceivedProposedTS != sm.getPeers(false).size() - 1) {
				PROPOSED_TS = (Q_entry) ois.readObject();
				bytesSent += Integer.parseInt(PROPOSED_TS.toString());
				System.out.println("~~~~" + PROPOSED_TS);
				if (REVISE_TS.tag == PROPOSED_TS.tag) {
					temp_ts = Math.max(temp_ts, PROPOSED_TS.timestamp);
					nrOfReceivedProposedTS++;
				}
			}
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}

		FINAL_TS = new Q_entry(ss.getLocalPort(), REVISE_TS.tag, temp_ts);
		try {
			sendToAllTs(FINAL_TS);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			clock = Math.max(clock, temp_ts);
		}
	}

	public class Receiver implements Runnable {

		@Override
		public void run() {
			Q_entry REVISE_TS;
			Q_entry PROPOSED_TS;
			Q_entry FINAL_TS;
			InputStream is;
			ObjectInputStream ois;
			ObjectOutputStream ous;
			int priority = 0;
			Socket sock;
			try {
				while (true) {
					sock = getItem();
					is = sock.getInputStream();
					if (is.available() > 0) {// non-blocking
						ois = new ObjectInputStream(is);
						Q_entry tempTS = (Q_entry) ois.readObject();
						bytesSent += Integer.parseInt(tempTS.toString());
						putItem(sock);
						// when revise_ts arrives
						if (tempTS.M != 0 && tempTS.pos != -1) {
							REVISE_TS = tempTS;
							priority = Math.max(priority + 1, REVISE_TS.timestamp);
							temp_Q.push(new Q_entry(REVISE_TS.M, REVISE_TS.pos, REVISE_TS.tag, REVISE_TS.sender_id, priority, false));

							PROPOSED_TS = new Q_entry(ss.getLocalPort(), REVISE_TS.sender_id, REVISE_TS.tag, priority);
							System.out.println("~~~PROPOSED_TS by " + ss.getLocalPort() + "~~~" + PROPOSED_TS);
							ous = new ObjectOutputStream(sm.getPeerByPortId(REVISE_TS.sender_id, true).getOutputStream());
							ous.writeObject(PROPOSED_TS);
							ous.flush();
						} else {// when final_ts arrives
							FINAL_TS = tempTS;
							Q_entry Q_e = null;
							for (Q_entry entry : temp_Q)
								if (entry.tag == FINAL_TS.tag) {
									Q_e = entry;
									break;
								}
							Q_e.deliverable = true;
							Q_e.timestamp = FINAL_TS.timestamp;
							Collections.sort(temp_Q, new Comparator<Q_entry>() {

								@Override
								public int compare(Q_entry o1, Q_entry o2) {
									if (o1.timestamp < o2.timestamp)
										return -1;
									else if (o1.timestamp > o2.timestamp)
										return 1;
									else
										return 0;
								}
							});
							if (temp_Q.peekFirst().tag == Q_e.tag) {
								delivery_Q.push(temp_Q.pop());
								if (temp_Q.size() > 0)
									while (temp_Q.peekFirst().deliverable == true)
										delivery_Q.push(temp_Q.pop());
							}
						}
					}
					putItem(sock);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
