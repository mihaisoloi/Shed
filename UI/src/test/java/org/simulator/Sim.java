package org.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.communication.Broker;
import org.communication.SocketMediator;
import org.mediation.CentralCoordinatorClient;
import org.mediation.Client;
import org.mediation.TokenClient;

public class Sim {
	public static void main(String[] args) throws IOException,
			InterruptedException {
		FileReader fr = new FileReader("src/test/resources/ADTestCommands");
		BufferedReader br = new BufferedReader(fr);
		String str;
		StringBuilder sb = new StringBuilder();
		while ((str = br.readLine()) != null)
			sb.append(str).append("\n");

		str = sb.toString();
		testCoordinatorClient(str);
		// testTokenClient(str);
	}

	public static void testCoordinatorClient(String commands)
			throws UnknownHostException, IOException, InterruptedException {
		new Thread(new Broker()).start();
		Map<Client, Thread> cccMap = new HashMap<Client, Thread>();
		CentralCoordinatorClient ccc1 = new CentralCoordinatorClient(
				new Socket("127.0.0.1", 5891));
		Thread t1 = new Thread(ccc1.new MessageListener());
		CentralCoordinatorClient ccc2 = new CentralCoordinatorClient(
				new Socket("127.0.0.1", 5891));
		Thread t2 = new Thread(ccc2.new MessageListener());
		CentralCoordinatorClient ccc3 = new CentralCoordinatorClient(
				new Socket("127.0.0.1", 5891));
		Thread t3 = new Thread(ccc3.new MessageListener());

		cccMap.put(ccc1, t1);
		cccMap.put(ccc2, t2);
		cccMap.put(ccc3, t3);
		t1.start();
		t2.start();
		t3.start();

		executeCommands(commands, cccMap);
	}

	private static void executeCommands(String commands,
			Map<Client, Thread> cccMap) throws InterruptedException {
		StringTokenizer st = new StringTokenizer(commands, "\n");
		while (st.hasMoreElements()) {
			String s = (String) st.nextElement();
			for (Client client : cccMap.keySet()) {
				if (s.startsWith("ins"))
					client.setKeyPressed(s.charAt(4), getPos(s.charAt(6)));
				else if (s.startsWith("del"))
					client.setKeyPressed('\b', getPos(s.charAt(4)));
				Thread.sleep(((long) (Math.random() * 500)));
			}
		}
	}

	private static void testTokenClient(String commands)
			throws InterruptedException {
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

		executeCommands(commands, tcMap);
	}

	private static int getPos(char c) {
		return Integer.parseInt("" + c);
	}
}
