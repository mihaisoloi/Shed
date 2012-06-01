package org.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.communication.Broker;
import org.communication.SocketMediator;
import org.mediation.Client;
import org.mediation.tema1.CentralCoordinatorClient;
import org.mediation.tema1.TokenClient;
import org.mediation.tema2.DeliveryChecker;
import org.mediation.tema2.ThreePhaseClient;
import org.mediation.tema3.DoptClient;
import org.mediation.tema3.JupiterClient;
import org.mediation.tema3.JupiterServer;

public class Sim {

	static int bytesSent = 0;

	public static void main(String[] args) throws IOException, InterruptedException {
		FileReader fr = new FileReader("src/test/resources/ADTestCommands");
		BufferedReader br = new BufferedReader(fr);
		String str;
		StringBuilder sb = new StringBuilder();
		while ((str = br.readLine()) != null)
			sb.append(str).append("\n");
		str = sb.toString();

		br = new BufferedReader(new FileReader("src/test/resources/ADTestCommands2"));
		String str2;
		sb = new StringBuilder();
		while ((str2 = br.readLine()) != null)
			sb.append(str2).append("\n");
		str2 = sb.toString();
		// testCoordinatorClient(str);
		// testTokenClient(str);
		// testThreePhaseCommitClient(str);
		// testDOPT(str, str2);
		testJupiter(str, str2);
	}
	public static void testCoordinatorClient(String commands, String commands2) throws UnknownHostException, IOException, InterruptedException {
		new Thread(new Broker()).start();
		Map<Client, Thread> cccMap = new HashMap<Client, Thread>();
		CentralCoordinatorClient ccc1 = new CentralCoordinatorClient(new Socket("127.0.0.1", 5891));
		Thread t1 = new Thread(ccc1.new MessageListener());
		CentralCoordinatorClient ccc2 = new CentralCoordinatorClient(new Socket("127.0.0.1", 5891));
		Thread t2 = new Thread(ccc2.new MessageListener());
		// CentralCoordinatorClient ccc3 = new CentralCoordinatorClient(new
		// Socket("127.0.0.1", 5891));
		// Thread t3 = new Thread(ccc3.new MessageListener());
		// CentralCoordinatorClient ccc4 = new CentralCoordinatorClient(new
		// Socket("127.0.0.1", 5891));
		// Thread t4 = new Thread(ccc4.new MessageListener());
		// CentralCoordinatorClient ccc5 = new CentralCoordinatorClient(new
		// Socket("127.0.0.1", 5891));
		// Thread t5 = new Thread(ccc5.new MessageListener());
		// CentralCoordinatorClient ccc6 = new CentralCoordinatorClient(new
		// Socket("127.0.0.1", 5891));
		// Thread t6 = new Thread(ccc6.new MessageListener());

		cccMap.put(ccc1, t1);
		cccMap.put(ccc2, t2);
		// cccMap.put(ccc3, t3);
		// cccMap.put(ccc4, t4);
		// cccMap.put(ccc5, t5);
		// cccMap.put(ccc6, t6);
		t1.start();
		t2.start();
		// t3.start();
		// t4.start();
		// t5.start();
		// t6.start();
		executeCommands(commands, commands2, cccMap);
		System.out.println("~~~BytesSent=" + (Broker.bytesSent + CentralCoordinatorClient.bytesSent));
	}

	private static void executeCommands(String commands, String commands2, final Map<Client, Thread> cccMap) throws InterruptedException {
		final StringTokenizer st = new StringTokenizer(commands, "\n");
		final StringTokenizer st2 = new StringTokenizer(commands2, "\n");
		final Map<Client, Thread> clientMap = Collections.synchronizedMap(cccMap);
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				int i = 0;
				while (/* i != clientMap.size() */st.hasMoreElements()) {
					String s = (String) st.nextElement();
					for (Client client : clientMap.keySet()) {
						if (s.startsWith("ins"))
							client.setKeyPressed(s.charAt(4), getPos(s.charAt(6)));
						else if (s.startsWith("del"))
							client.setKeyPressed('\b', getPos(s.charAt(4)));
						// client.setKeyPressed('a', 1);
						try {
							Thread.sleep(((long) (Math.random() * 5000)));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		t1.start();
		t1.join();
		// Thread t2 = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// int i = 0;
		// while (i != clientMap.size()/* st2.hasMoreElements() */) {
		// String s = (String) st2.nextElement();
		// for (Client client : clientMap.keySet()) {
		// if (s.startsWith("ins"))
		// client.setKeyPressed(s.charAt(4), getPos(s.charAt(6)));
		// else if (s.startsWith("del"))
		// client.setKeyPressed('\b', getPos(s.charAt(4)));
		// // client.setKeyPressed('b', 1);
		// try {
		// Thread.sleep(((long) (Math.random() * 1000)));
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }
		// });
		// t2.start();
		// t2.join();

	}
	private static int getPos(char c) {
		return Integer.parseInt("" + c);
	}
	private static void testTokenClient(String commands, String commands2) throws InterruptedException {
		SocketMediator sm = new SocketMediator();
		Map<Client, Thread> tcMap = new HashMap<Client, Thread>();
		TokenClient tc1 = new TokenClient(5891, sm);
		Thread t1 = new Thread(tc1);
		TokenClient tc2 = new TokenClient(5892, sm);
		Thread t2 = new Thread(tc2);
		TokenClient tc3 = new TokenClient(5893, sm);
		Thread t3 = new Thread(tc3);

		tcMap.put(tc1, t1);
		tcMap.put(tc2, t2);
		tcMap.put(tc3, t3);
		t1.start();
		t2.start();
		t3.start();

		executeCommands(commands, commands2, tcMap);
	}

	private static void testThreePhaseCommitClient(String commands, String commands2) throws UnknownHostException, IOException, InterruptedException {
		SocketMediator sockMed = new SocketMediator();
		Map<Client, Thread> tpccMap = new HashMap<Client, Thread>();
		initThreads(new ThreePhaseClient(5891, sockMed), tpccMap);
		initThreads(new ThreePhaseClient(5892, sockMed), tpccMap);
		initThreads(new ThreePhaseClient(5893, sockMed), tpccMap);
		initThreads(new ThreePhaseClient(5894, sockMed), tpccMap);
		initThreads(new ThreePhaseClient(5895, sockMed), tpccMap);
		initThreads(new ThreePhaseClient(5896, sockMed), tpccMap);

		executeCommands(commands, commands2, tpccMap);
		System.out.println("~~~~bytesSent=" + ThreePhaseClient.bytesSent);
	}

	private static void initThreads(ThreePhaseClient tpc, Map<Client, Thread> tpccMap) throws UnknownHostException, IOException, InterruptedException {
		final int port = tpc.ss.getLocalPort();
		Thread tpcThread = new Thread(tpc, "TPC-" + port);
		tpccMap.put(tpc, tpcThread);
		tpcThread.start();
		Thread.sleep(100);
		tpc.sm.registerPeer(new Socket("127.0.0.1", port), false);
		tpc.sm.registerPeer(new Socket("127.0.0.1", port), true);
		Thread receiver = new Thread(tpc.new Receiver());
		receiver.setPriority(Thread.MIN_PRIORITY);
		receiver.setDaemon(true);
		receiver.start();
		Thread deliveryChecker = new Thread(new DeliveryChecker(tpc.delivery_Q, tpc.gui, tpc.clock));
		deliveryChecker.setPriority(Thread.MAX_PRIORITY);
		deliveryChecker.setDaemon(true);
		deliveryChecker.start();
	}

	private static void testDOPT(String commands, String commands2) throws UnknownHostException, IOException, InterruptedException {

		SocketMediator sockMed = new SocketMediator();
		int[] ports = {5891, 5892, 5893, 5894, 5895};
		Map<Client, Thread> tpccMap = new HashMap<Client, Thread>();
		for (int i = 0; i < ports.length; i++)
			initThreads(new DoptClient(ports[i], sockMed, (i + 1) * 10000, ports.length), tpccMap);

		try {
			executeCommands(commands, commands2, tpccMap);
		} finally {
			System.out.println("~~~~bytesSent=" + DoptClient.bytesSent);
		}
	}

	private static void initThreads(DoptClient client, Map<Client, Thread> tpccMap) throws UnknownHostException, IOException, InterruptedException {
		final int port = client.ss.getLocalPort();
		Thread doptThread = new Thread(client, "DOPT-" + port);
		tpccMap.put(client, doptThread);
		doptThread.start();
		Thread.sleep(100);
		client.sm.registerPeer(new Socket("127.0.0.1", port), false);
		client.sm.registerPeer(new Socket("127.0.0.1", port), true);
		Thread receiver = new Thread(client.new Receiver());
		receiver.setPriority(Thread.MIN_PRIORITY);
		receiver.setDaemon(true);
		receiver.start();
		Thread deliveryChecker = new Thread(client.new RequestQueueChecker());
		deliveryChecker.setPriority(Thread.MAX_PRIORITY);
		deliveryChecker.setDaemon(true);
		deliveryChecker.start();
	}

	private static void testJupiter(String commands, String commands2) throws UnknownHostException, IOException, InterruptedException {
		System.out.println("jupiter server started listening... ... ...");
		new Thread(new JupiterServer()).start();
		Map<Client, Thread> tpccMap = new HashMap<Client, Thread>();
		for (int i = 1; i <= 5; i++) {
			JupiterClient jc = new JupiterClient(new Socket("127.0.0.1", 5891));
			Thread jupiterThread = new Thread(jc.new Receiver());
			tpccMap.put(jc, jupiterThread);
			jupiterThread.start();
		}

		executeCommands(commands, commands2, tpccMap);
		System.out.println("~~~~bytesSent=" + JupiterClient.bytesSent);
	}
}
