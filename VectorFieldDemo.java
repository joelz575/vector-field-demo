import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Random;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.Math;


public class VectorFieldDemo {

	public static void main(String[] args) {
		VFFrame mframe = new VFFrame();
	}
}

class VFFrame extends JFrame {
	JPanel mainPanel;
	JPanel controlPanel;
	JPanel eqnPanel;
	VFPanel vf;

	public VFFrame() {
		super("Vector Field Demo");
		this.setResizable(false);
		this.setSize(400,400);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = this.getContentPane();
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(3, 1));
		eqnPanel = new JPanel();
		eqnPanel.add(new JLabel("sample text"));
		controlPanel.add(eqnPanel);
		controlPanel.add(new JLabel("more sample text"));
		vf = new VFPanel();
		mainPanel = new JPanel();
		mainPanel.add(vf);
		mainPanel.add(controlPanel);
		this.add(mainPanel);
		this.pack();
		//c.add(vf);
		//c.add(controlPanel);
		//this.pack();
	}
	// TODO: check some other jframe code to make control panel,
	// pack control panel on the right of the vfpanel

	private class VFPanel extends JPanel {
		public VFPanel() {
			super();
		}

		public void paintComponent(Graphics g) {
			g.drawLine(1,1,100,100);
		}


		public Dimension getPreferredSize() {
			return new Dimension(400,400);
		}
	}
}
