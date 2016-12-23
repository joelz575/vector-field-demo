import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
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
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.lang.Math;

import net.objecthunter.exp4j.*;


public class VectorFieldDemo {

   public static int timeDelay = 4;
   
   public static void main(String[] args) throws Exception {
      VFFrame mframe = new VFFrame();
      while(true) {
         if(mframe.playing) {
            mframe.tick();
            mframe.repaint();
         }
         Thread.sleep(timeDelay);
      }
   }
}

class VFFrame extends JFrame {

   private JPanel mainPanel;
   private JPanel rightPanel;
   private JPanel controlPanel;
   private JPanel viewPanel;
   private JPanel eqnPanel;
   private JPanel statViewPanel;
   private JPanel miniButtonPanel;
   
   private JButton update;
   private JButton changeWindow;
   
   private JTextField xPos;
   private JTextField yPos;
   private JTextField curl;
   private JTextField div;
   
   private JTextField iComp;
   private JTextField jComp;
   
   private String iCompExpr;
   private String jCompExpr;
   
   private JSlider speedControl;
   private JSlider resControl;
   private JSlider figResControl;
   
   private VFPanel vf;
   private VectorDisplayPanel vdp;
   
   private boolean isPlaying = true;
   
   private ConcurrentLinkedDeque<TrackPoint> tracking = new ConcurrentLinkedDeque<TrackPoint>();
   
   private static final Color darkColor = new Color(0,0,0);
   private static final Color veryLightShade = new Color(215,215,215);
   private static final Color lightShade = new Color(175,175,175);
   private static final Color highlightColor = new Color(255,0,0);
   private static final int baseres = 100;
   private static final double factor = 100.0;
   
   private static int resolution = 100;
   
   private static final boolean enableTracking = true;
   private static final int numLastPoints = 15;
   
   private static final double delta = 0.001;
   
   private static final int viewPanelUpdateTime = 10; // in ticks
   
   private TrackPoint mpoint = new TrackPoint(0,0);
   
   private ArrayList<TrackPoint> figure = new ArrayList<TrackPoint>();
   private int figureResolution = 20;
   
   private TrackPoint startPoint = new TrackPoint(0,0);
   
   private boolean isPoint = false;
   private boolean isRect = false;
   private boolean isSelectingRect = false;
   
   private int tickTime = 0;
   
   public boolean playing = true;

   public VFFrame() {
      super("Vector Field Demo");
      this.setResizable(false);
      this.setSize(400,400); // gets repacked
      this.setVisible(true);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Container c = this.getContentPane();
      controlPanel = new JPanel();
      controlPanel.setLayout(new GridLayout(8, 1));
      iComp = new JTextField("-y",8);
      jComp = new JTextField("x",8);
      iCompExpr = iComp.getText();
      jCompExpr = jComp.getText();
      iComp.setHorizontalAlignment(JTextField.RIGHT);
      jComp.setHorizontalAlignment(JTextField.RIGHT);
      eqnPanel = new JPanel();
      eqnPanel.add(new JLabel("f(x,y) = "));
      eqnPanel.add(iComp);
      eqnPanel.add(new JLabel("i + "));
      eqnPanel.add(jComp);
      eqnPanel.add(new JLabel("j"));
      controlPanel.add(eqnPanel);
      
      miniButtonPanel = new JPanel();
      
      update = new JButton("Update Vector Field");
      update.addActionListener(new ActionListener() { 
         public void actionPerformed(ActionEvent e) {
            iCompExpr = iComp.getText();
            jCompExpr = jComp.getText();
         }
      });
      
      changeWindow = new JButton("Change Window");
      changeWindow.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            WindowSizeSelectorFrame wssf = new WindowSizeSelectorFrame(VFFrame.this);
            playing = false;
         }
      });
      
      miniButtonPanel.add(update);
      miniButtonPanel.add(changeWindow);
      
      controlPanel.add(miniButtonPanel);
      
      speedControl = new JSlider(JSlider.HORIZONTAL, 1, 10, 6);
      speedControl.addChangeListener(new SpeedSliderListener());
      speedControl.setMajorTickSpacing(5);
      speedControl.setMinorTickSpacing(1);
      speedControl.setPaintTicks(true);
      controlPanel.add(new JLabel("Speed:"));
      controlPanel.add(speedControl);
      resControl = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
      resControl.addChangeListener(new ResSliderListener());
      resControl.setMajorTickSpacing(5);
      resControl.setMinorTickSpacing(1);
      resControl.setPaintTicks(true);
      controlPanel.add(new JLabel("Resolution:"));
      controlPanel.add(resControl);
      figResControl = new JSlider(JSlider.HORIZONTAL, 10, 40, 20);
      figResControl.addChangeListener(new FigureResSliderListener()); 
      figResControl.setMajorTickSpacing(5);
      figResControl.setMinorTickSpacing(1);
      figResControl.setPaintTicks(true);
      controlPanel.add(new JLabel("Figure (Side) Resolution:"));
      controlPanel.add(figResControl);
      vf = new VFPanel();
      vf.addMouseListener(new VFMouseListener(vf));
      vdp = new VectorDisplayPanel();
      viewPanel = new JPanel(new BorderLayout(5, 5));//new GridLayout(3,1));
      
      xPos = new JTextField("",4);
      yPos = new JTextField("",4);
      curl = new JTextField("",4);
      div = new JTextField("",4);
      xPos.setEditable(false);
      yPos.setEditable(false);
      curl.setEditable(false);
      div.setEditable(false);
      
      statViewPanel = new JPanel(new GridLayout(3, 1));
      JPanel tmp;
      
      tmp = new JPanel(new FlowLayout(FlowLayout.LEFT));
      tmp.add(new JLabel("Position: ("));
      tmp.add(xPos);
      tmp.add(new JLabel(", "));
      tmp.add(yPos);
      tmp.add(new JLabel(")"));
      statViewPanel.add(tmp);
      
      tmp = new JPanel(new FlowLayout(FlowLayout.LEFT));
      tmp.add(new JLabel("Curl: "));
      tmp.add(curl);
      tmp.add(new JLabel("k"));
      statViewPanel.add(tmp);
      
      tmp = new JPanel(new FlowLayout(FlowLayout.LEFT));
      tmp.add(new JLabel("Divergence: "));
      tmp.add(div);
      statViewPanel.add(tmp);
      
      
      viewPanel.add(statViewPanel, BorderLayout.WEST);
      viewPanel.add(vdp, BorderLayout.EAST);
      
      TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Controls");
      title.setTitleJustification(TitledBorder.LEFT);
      controlPanel.setBorder(title);
      TitledBorder title2 = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Values");
      title2.setTitleJustification(TitledBorder.LEFT);
      
      
      viewPanel.setBorder(title2);
      rightPanel = new JPanel();
      rightPanel.setLayout(new BorderLayout(5, 10));
      rightPanel.add(controlPanel, BorderLayout.NORTH);
      rightPanel.add(viewPanel, BorderLayout.SOUTH);
      mainPanel = new JPanel();
      mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
      mainPanel.add(vf);
      mainPanel.add(rightPanel);
      this.add(mainPanel);
      this.pack();
   }
   
   public void setScale(int minX, int maxX, int minY, int maxY) {
      vf.minX = minX;
      vf.maxX = maxX;
      vf.minY = minY;
      vf.maxY = maxY;
   }
   
   public int[] getScale() {
      return new int[] {vf.minX, vf.maxX, vf.minY, vf.maxY};
   }
   
   public void tick() {
      tickTime ++;
      double dx,dy;
      dx = -1.0;
      dy = -1.0;
      if(isPlaying) {
         if(isPoint) {
         
            if(tickTime % 5 == 0) {
               tracking.add(new TrackPoint(mpoint.x, mpoint.y));
               if(tracking.size() > numLastPoints) {
                  tracking.pollFirst();
               }
            }
            
            for(int i = 0; i < resolution; i ++) {
               dx = vf.evalXAt(mpoint.x, mpoint.y);
               dy = vf.evalYAt(mpoint.x, mpoint.y);
               mpoint.x += dx / (factor * resolution);
               mpoint.y += dy / (factor * resolution);
            }
            
            double mm = vf.getMaxMagnitude();
            vdp.setScaled(dx/mm,dy/mm);
            
            if(tickTime % viewPanelUpdateTime == 0) {
               xPos.setText("" + (int)(0.5+1000*mpoint.x)/1000.0);
               yPos.setText("" + (int)(0.5+1000*mpoint.y)/1000.0);
               // estimate dQ/dx and dP/dy
               double dQdx = (vf.evalYAt(mpoint.x + delta, mpoint.y) - vf.evalYAt(mpoint.x, mpoint.y))/delta;
               double dPdy = (vf.evalXAt(mpoint.x, mpoint.y + delta) - vf.evalXAt(mpoint.x, mpoint.y))/delta;
               curl.setText("" + (int)(0.5+1000*(dQdx-dPdy))/1000.0);
               double dPdx = (vf.evalXAt(mpoint.x + delta, mpoint.y) - vf.evalXAt(mpoint.x, mpoint.y))/delta;
               double dQdy = (vf.evalYAt(mpoint.x, mpoint.y + delta) - vf.evalYAt(mpoint.x, mpoint.y))/delta;
               div.setText("" + (int)(0.5+1000*(dPdx+dQdy))/1000.0);
            }
            
         }
         else if(isRect) {
            for(int j = 0; j < figure.size(); j ++) {
               for(int i = 0; i < resolution/10.0; i ++) {
                  dx = vf.evalXAt(figure.get(j).x, figure.get(j).y);
                  dy = vf.evalYAt(figure.get(j).x, figure.get(j).y);
                  figure.get(j).x += dx / (factor * resolution);
                  figure.get(j).y += dy / (factor * resolution);
               }
            }
         }
      }
   }
   
   private class SpeedSliderListener implements ChangeListener {
      public void stateChanged(ChangeEvent e) {
         JSlider source = (JSlider)e.getSource();
         if(!source.getValueIsAdjusting()) {
            VectorFieldDemo.timeDelay = 10 - source.getValue();
         }
      }
   }
   
   
   private class ResSliderListener implements ChangeListener {
      public void stateChanged(ChangeEvent e) {
         JSlider source = (JSlider)e.getSource();
         if(!source.getValueIsAdjusting()) {
            int val = source.getValue();
            if(val < 10) {
               resolution = baseres / (11-val);
            }
            else if(val > 10) {
               resolution = baseres * (val-9);
            }
            else {
               resolution = baseres;
            }
         }
      }
   }
   
   
   private class FigureResSliderListener implements ChangeListener {
      public void stateChanged(ChangeEvent e) {
         JSlider source = (JSlider)e.getSource();
         if(!source.getValueIsAdjusting()) {
            figureResolution = source.getValue();
         }
      }
   }
   
   private class TrackPoint {
      public double x;
      public double y;
      
      public TrackPoint(double x, double y) {
         this.x = x;
         this.y = y;
      }
   }
   
   private class VFMouseListener implements MouseListener {
      private VFPanel parent;
      
      public VFMouseListener(VFPanel myParent) {
         parent = myParent;
      }
      
      public void mousePressed(MouseEvent e) {
         isPoint = false;
         vdp.setDisplay(false);
         isRect = false;
         isSelectingRect = true;
         startPoint.x = (double)e.getX() / VFPanel.wWidth * (parent.maxX - parent.minX + 2) + parent.minX - 1;
         startPoint.y = - ((double)e.getY() / VFPanel.wHeight * (parent.maxY - parent.minY + 2) + parent.minY - 1);
         xPos.setText("");
         yPos.setText("");
         curl.setText("");
         div.setText("");
      }
      
      public void mouseReleased(MouseEvent e) {
         if(isPoint)
            return;
         
         figure = new ArrayList<TrackPoint>();
         
         isSelectingRect = false;
         isRect = true;
         
         double tx, ty;
         tx = (double)e.getX() / VFPanel.wWidth * (parent.maxX - parent.minX + 2) + parent.minX - 1;
         ty = - ((double)e.getY() / VFPanel.wHeight * (parent.maxY - parent.minY + 2) + parent.minY - 1);
         for(int i = 0; i < figureResolution; i ++) { // along x = startPoint.x
            figure.add(new TrackPoint(startPoint.x, startPoint.y + (i * (ty - startPoint.y) / figureResolution)));
         }
         for(int i = 0; i < figureResolution; i ++) { // along y = ty
            figure.add(new TrackPoint(startPoint.x + (i * (tx - startPoint.x) / figureResolution), ty));
         }
         for(int i = 0; i < figureResolution; i ++) { // along x = tx
            figure.add(new TrackPoint(tx, ty + (i * (startPoint.y - ty) / figureResolution)));
         }
         for(int i = 0; i < figureResolution; i ++) { // along y = startPoint.y
            figure.add(new TrackPoint(tx + (i * (startPoint.x - tx) / figureResolution), startPoint.y));
         }
            
         xPos.setText("");
         yPos.setText("");
         curl.setText("");
         div.setText("");
      }
      
      public void mouseClicked(MouseEvent e) {
         isSelectingRect = false;
         isRect = false;
         isPoint = true;
         vdp.setDisplay(true);
         mpoint.x = (double)e.getX() / VFPanel.wWidth * (parent.maxX - parent.minX + 2) + parent.minX - 1;
         mpoint.y = - ((double)e.getY() / VFPanel.wHeight * (parent.maxY - parent.minY + 2) + parent.minY - 1);
      }
      
      public void mouseExited(MouseEvent e) {}
      public void mouseEntered(MouseEvent e) {}
   }
   
   private class VectorDisplayPanel extends JPanel {
      public static final int wWidth = 61;
      public static final int wHeight = 61;
      
      private static final double vMagn = 30.0;
      
      private static final double vHat = 5.0;
      private static final double branchAngle = Math.PI / 6.0;
      
      private double scaledX = 0.0;
      private double scaledY = 0.0;
      
      private boolean isDisplay = false;
      
      public VectorDisplayPanel() {
         super();
      }  
      
      public void setScaled(double x, double y) {
         scaledX = x;
         scaledY = y;
      }
      
      public void setDisplay(boolean ndisp) {
         isDisplay = ndisp;
      }

      public void paintComponent(Graphics g) {
         if(!isDisplay) {
            g.setColor(veryLightShade);
            g.fillRect(0,0,wWidth,wHeight);
            return;
         }
         g.setColor(new Color(255,255,255));
         g.fillRect(0,0,wWidth,wHeight);
         g.setColor(lightShade);
         g.drawLine(wWidth/2,0,wWidth/2,wHeight);
         g.drawLine(0,wHeight/2,wWidth,wHeight/2);
         g.setColor(darkColor);
         double sx, sy, dx, dy, theta;
         sx = wWidth/2;
         sy = wHeight/2;
         dx = vMagn * scaledX;
         dy = vMagn * scaledY;
         theta = Math.atan2(scaledY, scaledX);
         g.drawLine((int)(sx), (int)(sy), (int)(sx+dx), (int)(sy-dy));
         g.drawLine((int)(sx+dx), (int)(sy-dy), (int)(sx+dx-vHat*Math.cos(theta+branchAngle)), (int)(sy-dy+vHat*Math.sin(theta+branchAngle)));
         g.drawLine((int)(sx+dx), (int)(sy-dy), (int)(sx+dx-vHat*Math.cos(theta-branchAngle)), (int)(sy-dy+vHat*Math.sin(theta-branchAngle)));
      }

      public Dimension getPreferredSize() {
         return new Dimension(wWidth,wHeight);
      }
   }

   private class VFPanel extends JPanel {
      public static final int wWidth = 400;
      public static final int wHeight = 400;
      
      public int minX = -5;
      public int maxX = 5;
      public int minY = -5;
      public int maxY = 5;
      
      private double maxMagnitude = -1.0;
      
      private static final double vHat = 5.0;
      private static final double branchAngle = Math.PI / 6.0;
      
      public VFPanel() {
         super();
      }
      
      public double getMaxMagnitude() {
         return maxMagnitude;
      }

      public void paintComponent(Graphics g) {
         g.setColor(new Color(255,255,255));
         g.fillRect(0,0,wWidth,wHeight);
         g.setColor(darkColor);
         int numXRegions = maxX - minX + 2;
         int numYRegions = maxY - minY + 2;
         double xWidth = ((double)wWidth) / numXRegions;
         double yWidth = ((double)wHeight) / numYRegions;
         
         g.setColor(lightShade);
         for(int i = 1; i < numXRegions; i ++) {
            g.drawLine((int)(i * wWidth / numXRegions), 0,(int)( i * 400.0 / numXRegions), 400);
         }
         
         for(int i = 1; i < numYRegions; i ++) {
            g.drawLine(0, (int)(i * wHeight / numYRegions), 400, (int)( i * 400.0 / numYRegions));
         }
         
         g.setColor(darkColor);
         g.drawLine(0, (int)((1-minY) *  ((double)wHeight) / numYRegions) + 1, wWidth, (int)( (1-minY) * ((double)wHeight) / numYRegions) + 1);
         g.drawLine(0, (int)((1-minY) *  ((double)wHeight) / numYRegions), wWidth, (int)( (1-minY) * ((double)wHeight) / numYRegions));
         g.drawLine(0, (int)((1-minY) *  ((double)wHeight) / numYRegions) - 1, wWidth, (int)( (1-minY) * ((double)wHeight) / numYRegions) - 1);
         
         g.drawLine((int)((1-minX) * ((double)wWidth) / numXRegions) + 1, 0,(int)( (1-minX) * ((double)wWidth) / numXRegions) + 1, wHeight);
         g.drawLine((int)((1-minX) * ((double)wWidth) / numXRegions), 0,(int)( (1-minX) * ((double)wWidth) / numXRegions), wHeight);
         g.drawLine((int)((1-minX) * ((double)wWidth) / numXRegions) - 1, 0,(int)( (1-minX) * ((double)wWidth) / numXRegions) - 1, wHeight);
               
         double[][][] values = new double[maxX-minX+3][maxY-minY+3][2];
         double maxDist = -1;
         double d;
         for(int i = 0; i < maxX-minX+3; i ++) {
            for(int j = 0; j < maxY-minY+3; j ++) {
               values[i][j][0] = evalXAt(minX + i - 1, maxY - j + 1);
               values[i][j][1] = evalYAt(minX + i - 1, maxY - j + 1);
               d = Math.hypot(values[i][j][0], values[i][j][1]);
               if(d > maxDist) maxDist = d;
            }
         }
         maxMagnitude = maxDist;
         double sx, sy, dx, dy, theta;
         for(int i = 0; i < numXRegions+1; i ++) {
            for(int j = 0; j < numYRegions+1; j ++) {
               sx = i * xWidth;
               sy = j * yWidth;
               dx = xWidth * values[i][j][0] / maxDist;
               dy = yWidth * values[i][j][1] / maxDist;
               theta = Math.atan2(dy, dx);
               g.drawLine((int)(sx), (int)(sy), (int)(sx+dx), (int)(sy-dy));
               g.drawLine((int)(sx+dx), (int)(sy-dy), (int)(sx+dx-vHat*Math.cos(theta+branchAngle)), (int)(sy-dy+vHat*Math.sin(theta+branchAngle)));
               g.drawLine((int)(sx+dx), (int)(sy-dy), (int)(sx+dx-vHat*Math.cos(theta-branchAngle)), (int)(sy-dy+vHat*Math.sin(theta-branchAngle)));
            }
         }
         double px, py;
         if(isPoint) {
            g.setColor(highlightColor);
            px = (mpoint.x - minX + 1) * wWidth / (maxX - minX + 2);
            py = (-mpoint.y - minY + 1) * wHeight / (maxY - minY + 2); 
            g.fillOval((int)(px-2.5), (int)(py-2.5), 6, 6);
         }
         else if(isRect) {
            g.setColor(highlightColor);
            int[] pxs = new int[figure.size()];
            int[] pys = new int[figure.size()];
            for(int j = 0; j < figure.size(); j ++) {
               pxs[j] = (int)(0.5 + (figure.get(j).x - minX + 1) * wWidth / (maxX - minX + 2));
               pys[j] = (int)(0.5 + (-figure.get(j).y - minY + 1) * wHeight / (maxY - minY + 2)); 
            }
            g.fillPolygon(pxs, pys, figure.size());
         }
         if(isPoint) {
            for(TrackPoint tp : tracking) {
               px = (tp.x - minX + 1) * wWidth / (maxX - minX + 2);
               py = (-tp.y - minY + 1) * wHeight / (maxY - minY + 2); 
               g.drawLine((int)(px+0.5), (int)(py+0.5), (int)(px+0.5), (int)(py+0.5));
            }
         }
      }

      public double evalXAt(double x, double y) {
         try {
            Expression e = new ExpressionBuilder(iCompExpr).variables("x","y").build().setVariable("x",x).setVariable("y",y);
            return e.evaluate();
         } catch (ArithmeticException e) {
            return 0;
         }
      }
      
      public double evalYAt(double x, double y) {
         try {
            Expression e = new ExpressionBuilder(jCompExpr).variables("x","y").build().setVariable("x",x).setVariable("y",y);
            return e.evaluate();
         } catch (ArithmeticException e) {
            return 0;
         }   
      }

      public Dimension getPreferredSize() {
         return new Dimension(wWidth,wHeight);
      }
   }
}



class WindowSizeSelectorFrame extends JFrame {

   private VFFrame parent;
   
   private JTextField xMin;
   private JTextField xMax;
   private JTextField yMin;
   private JTextField yMax;
   
   private JPanel mainPanel;
   private JPanel inputPanel;
   
   private JButton updateButton;
   

   public WindowSizeSelectorFrame(VFFrame parent) {
      super("Window");
      this.setResizable(false);
      this.setSize(400,400); // gets repacked
      this.setVisible(true);
      
      this.parent = parent;
      
      int[] curscale = parent.getScale();
      
      inputPanel = new JPanel(new GridLayout(2,2,15,2));
      xMin = new JTextField(""+curscale[0],3);
      xMax = new JTextField(""+curscale[1],3);
      yMin = new JTextField(""+curscale[2],3);
      yMax = new JTextField(""+curscale[3],3);
      
      JPanel tmp;
      
      tmp = new JPanel();
      tmp.add(new JLabel("X min:"));
      tmp.add(xMin);
      inputPanel.add(tmp);
      
      tmp = new JPanel();
      tmp.add(new JLabel("X max:"));
      tmp.add(xMax);
      inputPanel.add(tmp);
      
      tmp = new JPanel();
      tmp.add(new JLabel("Y min:"));
      tmp.add(yMin);
      inputPanel.add(tmp);
      
      tmp = new JPanel();
      tmp.add(new JLabel("Y max:"));
      tmp.add(yMax);
      inputPanel.add(tmp);
      
      updateButton = new JButton("Update");
      updateButton.addActionListener(new ActionListener() { 
         public void actionPerformed(ActionEvent e) {
            WindowSizeSelectorFrame parent = WindowSizeSelectorFrame.this;
            int nxmin,nxmax,nymin,nymax;
            nxmin = Integer.parseInt(parent.xMin.getText());
            nxmax = Integer.parseInt(parent.xMax.getText());
            nymin = Integer.parseInt(parent.yMin.getText());
            nymax = Integer.parseInt(parent.yMax.getText());
            parent.parent.setScale(nxmin, nxmax, nymin, nymax);
            parent.parent.playing = true;
            parent.dispose();
         }
      });
      
      tmp = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
      tmp.add(inputPanel);
      inputPanel = tmp;
      
      tmp = new JPanel(new BorderLayout());
      tmp.add(inputPanel, BorderLayout.NORTH);
      tmp.add(updateButton, BorderLayout.SOUTH);
      
      mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
      mainPanel.add(tmp);
      
      this.add(mainPanel);
      this.pack();
   }
   
}