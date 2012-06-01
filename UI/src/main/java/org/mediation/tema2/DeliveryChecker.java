package org.mediation.tema2;

import java.util.LinkedList;

import org.gui.Shed;

public class DeliveryChecker implements Runnable {

	final LinkedList<Q_entry> delivery_Q;
	final Shed gui;
	int clock;

	public DeliveryChecker(LinkedList<Q_entry> delivery_Q, Shed gui, int clock) {
		this.delivery_Q = delivery_Q;
		this.gui = gui;
		this.clock = clock;
	}

	@Override
	public void run() {
		while (true) {
			Thread.yield();
			boolean empty = delivery_Q.isEmpty();
			if (!empty) {
				Q_entry entry = delivery_Q.pop();
				if (entry.M == '\b')
					gui.removeDistributed(entry.pos);
				else
					gui.insertDistributed(entry.M, entry.pos);
				clock = Math.max(clock, entry.timestamp) + 1;
			}
		}
	}
}