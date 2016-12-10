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

import net.objecthunter.exp4j.*;


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
      mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
      mainPanel.add(vf);
      mainPanel.add(controlPanel);
      this.add(mainPanel);
      this.pack();
      //c.add(vf);
      //c.add(controlPanel);
      //this.pack();
   }

   private class VFPanel extends JPanel {
      int minX = -6;
      int maxX = 6;
      int minY = -6;
      int maxY = 6;
      private final double vHat = 5.0;
      private final double branchAngle = Math.PI / 6.0;
      
      public VFPanel() {
         super();
      }

      public void paintComponent(Graphics g) {
         g.setColor(new Color(255,255,255));
         g.fillRect(0,0,400,400);
         g.setColor(new Color(0,0,0));
         int numXRegions = maxX - minX + 2;
         int numYRegions = maxY - minY + 2;
         double xWidth = 400.0 / numXRegions;
         double yWidth = 400.0 / numYRegions;
         for(int i = 1; i < numXRegions; i ++) {
            g.drawLine((int)(i * 400.0 / numXRegions), 0,(int)( i * 400.0 / numXRegions), 400);
            if(i + minX == 1) {
               g.drawLine((int)(i * 400.0 / numXRegions) + 1, 0,(int)( i * 400.0 / numXRegions) + 1, 400);
               g.drawLine((int)(i * 400.0 / numXRegions) - 1, 0,(int)( i * 400.0 / numXRegions) - 1, 400);
            }
         }
         for(int i = 1; i < numYRegions; i ++) {
            g.drawLine(0, (int)(i * 400.0 / numYRegions), 400, (int)( i * 400.0 / numYRegions));
            if(i + minY == 1) {
               g.drawLine(0, (int)(i * 400.0 / numYRegions) + 1, 400, (int)( i * 400.0 / numYRegions) + 1);
               g.drawLine(0, (int)(i * 400.0 / numYRegions) - 1, 400, (int)( i * 400.0 / numYRegions) - 1);
            }
         }
         double[][][] values = new double[maxX-minX+1][maxY-minY+1][2];
         double maxDist = -1;
         double d;
         for(int i = 0; i < maxX-minX+1; i ++) {
            for(int j = 0; j < maxY-minY+1; j ++) {
               values[i][j][0] = evalXAt(minX + i, minY + j);
               values[i][j][1] = evalYAt(minX + i, minY + j);
               d = Math.hypot(values[i][j][0], values[i][j][1]);
               if(d > maxDist) maxDist = d;
            }
         }
         double sx, sy, dx, dy, theta;
         for(int i = 1; i < numXRegions; i ++) {
            for(int j = 1; j < numYRegions; j ++) {
               sx = i * xWidth;
               sy = j * yWidth;
               dx = xWidth * values[i-1][j-1][0] / maxDist;
               dy = yWidth * values[i-1][j-1][1] / maxDist;
               theta = Math.atan2(dy, dx);
               g.drawLine((int)(0.5+sx), (int)(0.5+sy), (int)(0.5+sx+dx), (int)(0.5+sy+dy));
               g.drawLine((int)(0.5+sx+dx), (int)(0.5+sy+dy), (int)(0.5+sx+dx-vHat*Math.cos(theta+branchAngle)), (int)(0.5+sy+dy-vHat*Math.sin(theta+branchAngle)));
               g.drawLine((int)(0.5+sx+dx), (int)(0.5+sy+dy), (int)(0.5+sx+dx-vHat*Math.cos(theta-branchAngle)), (int)(0.5+sy+dy-vHat*Math.sin(theta-branchAngle)));
            }
         }
      }

      private double evalXAt(double x, double y) {
         return -y;
      }
      
      private double evalYAt(double x, double y) {
         return x;
      }

      public Dimension getPreferredSize() {
         return new Dimension(400,400);
      }
   }
}
