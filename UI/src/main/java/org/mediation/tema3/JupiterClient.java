package org.mediation.tema3;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import org.gui.Shed;
import org.mediation.Client;

public class JupiterClient extends Client {

	int myMsgs = 0;
	int otherMsgs = 0;
	LinkedBlockingQueue<JupiterOperation> outgoing = new LinkedBlockingQueue<JupiterOperation>();

	public static int bytesSent;

	public JupiterClient(Socket socket) {
		this.socket = socket;
		// GUI-ul este Observer
		gui = new Shed(this);
	}

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		for (int i = 1; i <= 2; i++)
			new Thread(new JupiterClient(new Socket("127.0.0.1", 5891)).new Receiver()).start();
	}

	@Override
	public void setKeyPressed(char c, int pos) {

		operation(c, pos);
		JupiterOperation jo = new JupiterOperation(c, pos, myMsgs, otherMsgs);
		ObjectOutputStream dos = null;
		try {
			dos = new ObjectOutputStream(socket.getOutputStream());
			dos.writeObject(jo);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (dos != null)
					dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		outgoing.add(jo);
		myMsgs++;
	}

	public class Receiver implements Runnable {

		@Override
		public void run() {
			ObjectInputStream dis = null;
			JupiterOperation msg = null;
			try {
				InputStream is = socket.getInputStream();
				while (true) {
					/* msg */
					int bytes = is.available();
					if (bytes > 0) {
						bytesSent += bytes;
						dis = new ObjectInputStream(is);
						msg = (JupiterOperation) dis.readObject();

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
						operation(msg.message, msg.position);
						otherMsgs++;
					}
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

	private void operation(char c, int pos) {
		if (c == '\b')
			gui.removeDistributed(pos);
		else
			gui.insertDistributed(c, pos);
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
