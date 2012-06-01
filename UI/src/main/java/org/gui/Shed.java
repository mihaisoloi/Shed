package org.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.mediation.Client;

/**
 * Shared editor
 * 
 * @author ms
 * 
 */
public class Shed extends JFrame {

	JTextPane textPane;
	AbstractDocument doc;
	JTextArea changeLog;
	DocumentInsertionFilter dif;
	Caret caret;

	/**
	 * Create the frame.
	 */
	public Shed(Client client) {
		super("Shed");
		// clientul este Subiectul
		client.setGUI(this);
		dif = new DocumentInsertionFilter(client);
		// crearea de text pane
		textPane = new JTextPane();
		textPane.setCaretPosition(0);
		textPane.setMargin(new Insets(5, 5, 5, 5));
		StyledDocument styledDoc = textPane.getStyledDocument();
		if (styledDoc instanceof AbstractDocument) {
			doc = (AbstractDocument) styledDoc;
			doc.setDocumentFilter(dif);
		} else {
			System.err.println("Text pane's document isn't an AbstractDocument!");
			System.exit(-1);
		}
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(200, 200));

		// crearea de text area
		changeLog = new JTextArea(5, 30);
		changeLog.setEditable(false);
		JScrollPane scrollPaneForLog = new JScrollPane(changeLog);

		// crearea de split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, scrollPaneForLog);
		splitPane.setOneTouchExpandable(true);

		// crearea de status area
		JPanel statusPane = new JPanel(new GridLayout(1, 1));
		KeyEventListener keyEventLabel = new KeyEventListener();
		statusPane.add(keyEventLabel);

		// adaugarea componentelor la pane
		getContentPane().add(splitPane, BorderLayout.CENTER);
		getContentPane().add(statusPane, BorderLayout.PAGE_END);

		textPane.addKeyListener(keyEventLabel);
		// il folosesc la a muta liniuta dupa o inserare sau o stergere
		caret = new DefaultCaret();
		textPane.setCaret(caret);
		doc.addDocumentListener(new DocListener());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// ca sa ia forma ferestrei
		pack();
		setVisible(true);
	}

	protected class KeyEventListener extends JLabel implements KeyListener {

		@Override
		public void keyTyped(java.awt.event.KeyEvent e) {
			System.out.println("Type~~~~~~~~" + e.getKeyChar());
			keyEventInfo(e.getKeyChar(), e.getKeyLocation());
		}

		@Override
		public void keyPressed(java.awt.event.KeyEvent e) {
			System.out.println("Press~~~~~~~~~~~~" + e.getKeyChar());
			keyEventInfo(e.getKeyChar(), e.getKeyLocation());
		}

		@Override
		public void keyReleased(java.awt.event.KeyEvent e) {
			System.out.println("Release~~~~~~~~~~~~" + e.getKeyChar());
			keyEventInfo(e.getKeyChar(), e.getKeyLocation());
		}

		protected void keyEventInfo(final char c, final int position) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setText("Key " + c + " was pressed at " + position);
				}
			});
		}
	}

	// And this one listens for any changes to the document.
	protected class DocListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		private void displayEditInfo(DocumentEvent e) {
			Document document = e.getDocument();
			int changeLength = e.getLength();
			changeLog.append(e.getType().toString() + ": " + changeLength + " character" + ((changeLength == 1) ? ". " : "s. ") + " Text length = " + document.getLength() + "."
					+ "\n");
		}
	}

	public void insertDistributed(char c, int pos) {
		try {
			dif.setKeyPressed(true);
			doc.insertString(pos, String.valueOf(c), null);
			caret.setDot(pos + 1);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void removeDistributed(int pos) {
		try {
			dif.setKeyPressed(true);
			doc.remove(pos, 1);
			caret.setDot(pos);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

}
