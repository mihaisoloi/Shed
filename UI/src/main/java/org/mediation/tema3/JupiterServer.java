package org.mediation.tema3;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class JupiterServer implements Runnable {
	static final ArrayList<Socket> clientSocketList = new ArrayList<Socket>();
	static final ArrayList<Relayer> clientNodes = new ArrayList<Relayer>();
	private ServerSocket ss;
	public static int bytesSent = 0;

	public JupiterServer() {
		try {
			this.ss = new ServerSocket(5891);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Socket s;
		while (true) {
			try {
				s = ss.accept();
				clientSocketList.add(s);
				Relayer client = new Relayer(s);
				clientNodes.add(client);
				new Thread(client).start();
				Thread.sleep(100);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		System.out.println("jupiter server started listening... ... ...");
		new Thread(new JupiterServer()).start();

	}

	protected class Relayer implements Runnable {
		private final Socket socket;
		private ObjectInputStream input;
		private final int myPort;
		int myMsgs = 0;
		int otherMsgs = 0;
		LinkedBlockingQueue<JupiterOperation> outgoing = new LinkedBlockingQueue<JupiterOperation>();

		public Relayer(Socket socket) {
			this.socket = socket;
			myPort = socket.getPort();

		}

		@Override
		public void run() {
			ObjectOutputStream dos = null;
			JupiterOperation msg = null;
			try {
				InputStream is = socket.getInputStream();
				while (true) {
					/* msg */
					if (is.available() > 0) {
						input = new ObjectInputStream(is);
						msg = (JupiterOperation) input.readObject();

						for (Relayer client : clientNodes)
							if (client.myPort != myPort) {
								client.outgoing.add(msg);
							}
						myMsgs++;

						/* Discard acknowledged messages. */
						for (JupiterOperation m : outgoing)
							if (m.myMsgs < msg.otherMsgs)
								outgoing.remove(m);

						JupiterOperation[] transform = new JupiterOperation[2];
						for (JupiterOperation outgoingMsgs : outgoing) {
							transform = xform(msg, outgoingMsgs);
							msg = transform[0];
							outgoingMsgs = transform[1];
						}

						/* apply “msg” received from “client” */
						for (Socket clientOutput : clientSocketList)
							if (clientOutput.getPort() != myPort) {
								try {
									dos = new ObjectOutputStream(clientOutput.getOutputStream());
									if (msg != null) {
										dos.writeObject(msg);
									}
								} finally {
									dos.flush();
								}
							}
						otherMsgs++;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public JupiterOperation[] xform(JupiterOperation msg, JupiterOperation outgoingMsg) {
		final int x = msg.position;
		final int y = outgoingMsg.position;
		JupiterOperation msgAdded = msg;
		msgAdded.position++;
		JupiterOperation msgDel = msg;
		msgDel.position--;
		JupiterOperation outgoingMsgAdded = outgoingMsg;
		outgoingMsgAdded.position++;
		JupiterOperation outgoingMsgDel = outgoingMsg;
		outgoingMsgAdded.position--;
		final JupiterOperation[] normalAdd = new JupiterOperation[]{msg, outgoingMsgAdded};
		final JupiterOperation[] normalDel = new JupiterOperation[]{msg, outgoingMsgDel};
		final JupiterOperation[] add = new JupiterOperation[]{msgAdded, outgoingMsg};
		final JupiterOperation[] delete = new JupiterOperation[]{msgDel, outgoingMsg};
		switch (cases(msg.message, outgoingMsg.message)) {
			case 1 :/* insert-insert */
				if (x < y)
					return normalAdd;
				else if (x > y)
					return add;
				else
					/* P_i=P_j */
					return new JupiterOperation[]{null, null};
			case 2 :/* delete-delete */
				if (x < y)
					return normalDel;
				else if (x > y)
					return delete;
				else
					return new JupiterOperation[]{null, null};
			case 3 :/* insert-delete */
				if (x < y)
					return normalAdd;
				else
					return delete;// it's only insert
			case 4 :/* delete-insert */
				if (x < y)
					return normalDel;
				else
					return add;
			default :
				return new JupiterOperation[]{};
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
