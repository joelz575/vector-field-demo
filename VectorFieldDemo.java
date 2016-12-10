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
   private JPanel mainPanel;
   private JPanel controlPanel;
   private JPanel eqnPanel;
   private JButton update;
   private JTextField iComp;
   private JTextField jComp;
   private VFPanel vf;
   private final Color darkColor = new Color(0,0,0);
   private final Color lightShade = new Color(175,175,175);

   public VFFrame() {
      super("Vector Field Demo");
      this.setResizable(false);
      this.setSize(400,400);
      this.setVisible(true);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Container c = this.getContentPane();
      controlPanel = new JPanel();
      controlPanel.setLayout(new GridLayout(3, 1));
      iComp = new JTextField("-y",5);
      jComp = new JTextField("x",5);
      iComp.setHorizontalAlignment(JTextField.RIGHT);
      jComp.setHorizontalAlignment(JTextField.RIGHT);
      eqnPanel = new JPanel();
      eqnPanel.add(new JLabel("f(x,y) = "));
      eqnPanel.add(iComp);
      eqnPanel.add(new JLabel("i + "));
      eqnPanel.add(jComp);
      eqnPanel.add(new JLabel("j"));
      controlPanel.add(eqnPanel);
      update = new JButton("Update");
      update.addActionListener(new ActionListener() { 
         public void actionPerformed(ActionEvent e) {
            repaint();
         }
      });
      controlPanel.add(update);
      vf = new VFPanel();
      mainPanel = new JPanel();
      mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
      mainPanel.add(vf);
      mainPanel.add(controlPanel);
      this.add(mainPanel);
      this.pack();
   }

   private class VFPanel extends JPanel {
      int minX = -15;
      int maxX = 15;
      int minY = -15;
      int maxY = 15;
      private final double vHat = 5.0;
      private final double branchAngle = Math.PI / 6.0;
      
      public VFPanel() {
         super();
      }

      public void paintComponent(Graphics g) {
         g.setColor(new Color(255,255,255));
         g.fillRect(0,0,400,400);
         g.setColor(darkColor);
         int numXRegions = maxX - minX + 2;
         int numYRegions = maxY - minY + 2;
         double xWidth = 400.0 / numXRegions;
         double yWidth = 400.0 / numYRegions;
         
         g.setColor(lightShade);
         for(int i = 1; i < numXRegions; i ++) {
            g.drawLine((int)(i * 400.0 / numXRegions), 0,(int)( i * 400.0 / numXRegions), 400);
         }
         
         for(int i = 1; i < numYRegions; i ++) {
            g.drawLine(0, (int)(i * 400.0 / numYRegions), 400, (int)( i * 400.0 / numYRegions));
         }
         
         g.setColor(darkColor);
         g.drawLine(0, (int)((1-minY) * 400.0 / numYRegions) + 1, 400, (int)( (1-minY) * 400.0 / numYRegions) + 1);
         g.drawLine(0, (int)((1-minY) * 400.0 / numYRegions), 400, (int)( (1-minY) * 400.0 / numYRegions));
         g.drawLine(0, (int)((1-minY) * 400.0 / numYRegions) - 1, 400, (int)( (1-minY) * 400.0 / numYRegions) - 1);
         
         g.drawLine((int)((1-minX) * 400.0 / numXRegions) + 1, 0,(int)( (1-minX) * 400.0 / numXRegions) + 1, 400);
         g.drawLine((int)((1-minX) * 400.0 / numXRegions), 0,(int)( (1-minX) * 400.0 / numXRegions), 400);
         g.drawLine((int)((1-minX) * 400.0 / numXRegions) - 1, 0,(int)( (1-minX) * 400.0 / numXRegions) - 1, 400);
               
         double[][][] values = new double[maxX-minX+3][maxY-minY+3][2];
         double maxDist = -1;
         double d;
         for(int i = 0; i < maxX-minX+3; i ++) {
            for(int j = 0; j < maxY-minY+3; j ++) {
               values[i][j][0] = evalXAt(minX + i - 1, minY + j - 1);
               values[i][j][1] = evalYAt(minX + i - 1, minY + j - 1);
               d = Math.hypot(values[i][j][0], values[i][j][1]);
               if(d > maxDist) maxDist = d;
            }
         }
         double sx, sy, dx, dy, theta;
         for(int i = 0; i < numXRegions+1; i ++) {
            for(int j = 0; j < numYRegions+1; j ++) {
               sx = i * xWidth;
               sy = j * yWidth;
               dx = xWidth * values[i][j][0] / maxDist;
               dy = yWidth * values[i][j][1] / maxDist;
               theta = Math.atan2(dy, dx);
               g.drawLine((int)(sx), (int)(sy), (int)(sx+dx), (int)(sy+dy));
               g.drawLine((int)(sx+dx), (int)(sy+dy), (int)(sx+dx-vHat*Math.cos(theta+branchAngle)), (int)(sy+dy-vHat*Math.sin(theta+branchAngle)));
               g.drawLine((int)(sx+dx), (int)(sy+dy), (int)(sx+dx-vHat*Math.cos(theta-branchAngle)), (int)(sy+dy-vHat*Math.sin(theta-branchAngle)));
            }
         }
      }

      private double evalXAt(double x, double y) {
         Expression e = new ExpressionBuilder(iComp.getText()).variables("x","y").build().setVariable("x",x).setVariable("y",y);
         return e.evaluate();
      }
      
      private double evalYAt(double x, double y) {
         Expression e = new ExpressionBuilder(jComp.getText()).variables("x","y").build().setVariable("x",x).setVariable("y",y);
         return e.evaluate();
      }

      public Dimension getPreferredSize() {
         return new Dimension(400,400);
      }
   }
}
