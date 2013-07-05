package name.herve.imagematch.gui.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import plugins.nherve.toolbox.gui.GUIUtil;

public class FileChooserWithTextField extends JPanel implements ActionListener {
	private static final long serialVersionUID = 7978928047153910878L;

	private Set<FileChooserListener> listeners;
	private JTextField tf;
	private JButton bt;

	public FileChooserWithTextField(String buttonLabel, String defaultLocation) {
		super();

		listeners = new HashSet<FileChooserListener>();

		bt = new JButton(buttonLabel);
		bt.addActionListener(this);
		tf = new JTextField();
		tf.setText(defaultLocation);

		Dimension maxDim = new Dimension(5000, 25);
		Dimension minDim = new Dimension(100, 25);

		tf.setPreferredSize(maxDim);
		tf.setMaximumSize(maxDim);
		tf.setMinimumSize(minDim);

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(tf);
		add(bt);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == null) {
			return;
		}

		if (o instanceof JButton) {
			JButton b = (JButton) e.getSource();
			if (b == bt) {
				File f = GUIUtil.fileChooser(this, "Choose", getChoosenFile(), JFileChooser.FILES_AND_DIRECTORIES);
				if (f != null) {
					tf.setText(f.getAbsolutePath());
					fireFileChoosenEvent();
				}
			}
		}
	}

	public boolean addFileChooserListener(FileChooserListener e) {
		return listeners.add(e);
	}

	private void fireFileChoosenEvent() {
		for (FileChooserListener l : listeners) {
			l.fileChoosen(this);
		}
	}

	public File getChoosenFile() {
		return new File(tf.getText());
	}

	public boolean removeFileChooserListener(FileChooserListener o) {
		return listeners.remove(o);
	}
}
