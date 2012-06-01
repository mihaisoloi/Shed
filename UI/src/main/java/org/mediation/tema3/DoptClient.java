package org.mediation.tema3;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.communication.SocketMediator;
import org.gui.Shed;
import org.mediation.Client;

public class DoptClient extends Client implements Runnable {

	public static int bytesSent = 0;
	LinkedBlockingQueue<DoptRequest> requestQueue = new LinkedBlockingQueue<DoptRequest>();
	LinkedList<DoptRequest> requestLog = new LinkedList<DoptRequest>();
	int priority;
	final int[] stateVector;
	final int identifier;

	public DoptClient(int port, SocketMediator sm, int priority, int nrOfClients) {
		// priority of the client is 10000 apart, so that the operations can
		// increment the priority for the operation
		this.priority = priority;
		this.stateVector = new int[nrOfClients];
		this.identifier = port;
		// GUI-ul este Observer
		gui = new Shed(this);
		this.sm = sm;
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			// one socket for receiver
			setSocket(ss.accept());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		SocketMediator sockMed = new SocketMediator();
		int[] ports = {5891, 5892, 5893};
		for (int i = 0; i < ports.length; i++)
			new DoptClient(ports[i], sockMed, (i + 1) * 10000, ports.length).initThreads();
	}

	public void initThreads() throws UnknownHostException, IOException, InterruptedException {
		final int port = ss.getLocalPort();
		new Thread(this, "DOPT-" + port).start();
		Thread.sleep(100);
		sm.registerPeer(new Socket("127.0.0.1", port), false);
		sm.registerPeer(new Socket("127.0.0.1", port), true);
		Thread receiver = new Thread(this.new Receiver());
		receiver.setPriority(Thread.MIN_PRIORITY);
		receiver.setDaemon(true);
		receiver.start();
		Thread deliveryChecker = new Thread(this.new RequestQueueChecker());
		deliveryChecker.setPriority(Thread.MAX_PRIORITY);
		deliveryChecker.setDaemon(true);
		deliveryChecker.start();
	}

	/**
	 * generating operations
	 */
	@Override
	public void setKeyPressed(char m, int pos) {
		priority++;
		DoptRequest doptr = new DoptRequest(identifier, stateVector, m, pos, priority);
		requestQueue.add(doptr);
		try {
			sendToAllTs(doptr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * retrieving operations
	 * 
	 * @author ms
	 * 
	 */
	public class Receiver implements Runnable {

		@Override
		public void run() {

			InputStream is;
			ObjectInputStream ois;
			Socket sock;
			try {
				while (true) {
					sock = getItem();
					is = sock.getInputStream();
					int bytes = is.available();
					if (bytes > 0) {// non-blocking
						bytesSent += bytes;
						ois = new ObjectInputStream(is);
						DoptRequest req = (DoptRequest) ois.readObject();
						requestQueue.add(req);
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

	/**
	 * executing operations
	 * 
	 * @author ms
	 * 
	 */
	public class RequestQueueChecker implements Runnable {

		@Override
		public void run() {
			while (true) {
				boolean empty = requestQueue.isEmpty();
				if (!empty) {
					Q : for (DoptRequest doptrQueue : requestQueue) {
						if (isSmallerOrEquals(doptrQueue.stateVector, stateVector)) {
							requestQueue.remove(doptrQueue);
							if (isSmaller(doptrQueue.stateVector, stateVector)) {
								// get most recent entry in L_i where...
								DoptRequest doptrRecent = null;
								for (DoptRequest doptrLog : requestLog)
									if (isSmallerOrEquals(doptrLog.stateVector, doptrQueue.stateVector)) {
										doptrRecent = doptrLog;
										break;
									}
								while (doptrRecent != null && doptrQueue.m != 0) {
									final int k = doptrRecent.identifier - 5891;
									final int[] sj = doptrQueue.stateVector;
									final int[] sk = doptrRecent.stateVector;
									if (sj[k] <= sk[k]) {
										final int[] transform = transformMatrix(doptrQueue, doptrRecent);
										if (transform == null)
											continue Q;
										doptrQueue.m = (char) transform[0];
										doptrQueue.pos = transform[1];
									}
									doptrRecent = requestLog.poll();
								}
							}
							if (doptrQueue.m == '\b')
								gui.removeDistributed(doptrQueue.pos);
							else
								gui.insertDistributed(doptrQueue.m, doptrQueue.pos);
							doptrQueue.stateVector = Arrays.copyOf(stateVector, stateVector.length);
							requestLog.add(doptrQueue);
							stateVector[doptrQueue.identifier - 5891] = stateVector[doptrQueue.identifier - 5891] + 1;
						}
					}
				}
			}
		}
	}

	/**
	 * state vectors comparison and we know that they're the same length
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean isSmaller(int[] a, int[] b) {

		for (int i = 0; i < a.length; i++) {
			if (a[i] > b[i])
				return false;
			else if (a[i] < b[i])
				return true;
		}
		return true;
	}

	private boolean isSmallerOrEquals(int[] a, int[] b) {
		return isSmaller(a, b) || Arrays.equals(a, b);
	}

	public int[] transformMatrix(DoptRequest doptrQueue, DoptRequest doptrRecent) {
		final int P_i = doptrQueue.pos;
		final int P_j = doptrRecent.pos;
		final char X_i = doptrQueue.m;
		final char X_j = doptrRecent.m;
		final int[] normal = new int[]{X_i, P_i};
		final int[] add = new int[]{X_i, P_i + 1};
		final int[] delete = new int[]{X_i, P_i - 1};
		switch (cases(X_i, X_j)) {
			case 1 :
				if (P_i < P_j)
					return normal;
				else if (P_i > P_j)
					return add;
				else /* P_i=P_j */{
					if (X_i == X_j)
						return null;
					else {
						if (doptrQueue.priority > doptrRecent.priority)
							return add;
						else
							/* prio_i<prio_j */
							return normal;
					}
				}
			case 2 :
				if (P_i < P_j)
					return normal;
				else if (P_i > P_j)
					return delete;
				else
					return null;
			case 3 :
				if (P_i < P_j)
					return normal;
				else
					return delete;// it's only insert
			case 4 :
				if (P_i < P_j)
					return normal;
				else
					return add;
			default :
				return null;
		}
	}

	private int cases(char i, char j) {
		if (i == '\b') {
			if (j == i)/* delete-delete */
				return 2;
			else if (j != i)/* delete-insert */
				return 4;
		} else {
			if (j == '\b')/* insert-delete */
				return 3;
			else
				/* insert-insert */
				return 1;
		}
		return 0;
	}
}
