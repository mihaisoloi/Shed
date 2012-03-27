package org.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Broker implements Runnable {

	List<Socket> clientSocketList = new ArrayList<Socket>();
	private ServerSocket ss;

	public Broker() {
		try {
			this.ss = new ServerSocket(5891);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			Socket s;
			try {
				s = ss.accept();
				clientSocketList.add(s);
				new Thread(new Relayer(s)).start();
				Thread.sleep(100);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		System.out.println("broker started listening... ... ...");
		new Thread(new Broker()).start();
	}

	protected class Relayer implements Runnable {
		private DataInputStream input;

		public Relayer(Socket socket) {
			try {
				input = new DataInputStream(socket.getInputStream());

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			System.out.println("~~~~~~~Broker~~~~~~~");
			DataOutputStream dos = null;
			char c;
			int pos = -1;
			try {
				while (true) {
					c = input.readChar();
					pos = input.readInt();
					if (c != '\u0000' && pos != -1)
						for (Socket clientOutput : clientSocketList) {
							dos = new DataOutputStream(
									clientOutput.getOutputStream());
							System.out
									.println("Sending to client through port: "
											+ clientOutput.getPort()
											+ " character=" + c
											+ " at position=" + pos);
							dos.writeChar(c);
							dos.writeInt(pos);
							dos.flush();
						}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
