package org.communication;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Broker {
	ExecutorService executor = Executors.newFixedThreadPool(2);

	public void start(int port) throws IOException {
		final ServerSocket ss = new ServerSocket(port);
		while (!executor.isShutdown())
			executor.submit(new Relayer(ss.accept()));
	}

	public void shutdown() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(30, TimeUnit.SECONDS);
		executor.shutdownNow();
	}

	public static void main(String[] args) throws IOException {
		System.out.println("broker started listening... ... ...");
		new Broker().start(Integer.parseInt(args[0]));
	}

	protected class Relayer implements Runnable {
		private final Socket socket;
		private BufferedInputStream input;
		private DataOutputStream output;

		public Relayer(Socket socket) {
			this.socket = socket;
		}

		public void run() {

		}

	}
}
