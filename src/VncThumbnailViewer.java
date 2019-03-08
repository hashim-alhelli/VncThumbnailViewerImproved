//
//  Copyright (C) 2007 David Czechowski.  All Rights Reserved.
//  Copyright (C) 2001-2004 HorizonLive.com, Inc.  All Rights Reserved.
//  Copyright (C) 2002 Constantin Kaplinsky.  All Rights Reserved.
//  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//

//
// VncThumbnailViewer.java - a unique VNC viewer.  This class creates an empty frame
// into which multiple vncviewers can be added.
//


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.lang.Math.*;
import java.lang.management.ManagementFactory;
import java.net.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.MaskFormatter;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import java.util.*;
public class VncThumbnailViewer extends Frame
    implements WindowListener, ComponentListener, ContainerListener, MouseListener, ActionListener  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8279240708665465946L;

	
	 static Timer timer = new Timer();// this timer is for auto refresh
	
	 
	static int real_size = 0;
	 
	static boolean is_Recording = false;
	int total_seconds = 0;//total seconds to display on the title frame
	static HashSet<String> domains = new HashSet<String>();
	static HashSet<MenuItem> view_items = new HashSet<MenuItem>();
	static volatile boolean busy = false;
  public static void main(String argv[]) throws InstantiationException, UnsupportedLookAndFeelException, ClassNotFoundException, IllegalAccessException
  {	  
	  
	//load the files and get only the user domain  
	  
	  
	// loadDomains("host",""); enable this if you want your file to load on startup
	  
	 boolean firsttime= true; 
	  
     VncThumbnailViewer t = new VncThumbnailViewer();

 
    // VncThumbnailViewer.viewersList.loadHosts2("host" , "");// enable this if you want file to load on startup

	try {
//		System.out.println("Before going to the timer");
		timer_Start(6,true);
	
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		System.out.println("Timer Fail");
		//e.printStackTrace();
	}
	
    String h =  new  String("");//host
    String pw = new String("");//password
    String us = new String("");//username
    int p = 0;//port

    for(int i = 0; i < argv.length; i += 2) 
    {
      if(argv.length < (i+2) ) 
      {
        System.out.println("ERROR: No value found for parameter " + argv[i]);
        break;
      }
      String param = argv[i];
      String value = argv[i+1];
      if(param.equalsIgnoreCase("host")) {
        h = value;
      }
      if(param.equalsIgnoreCase("port")) {
        p = Integer.parseInt(value);
      }
      if(param.equalsIgnoreCase("password")) {
        pw = value;
      }
      if(param.equalsIgnoreCase("username")) {
        us = value;
      }
      if(param.equalsIgnoreCase("encpassword")) {
        pw = AddHostDialog.readEncPassword(value);
      }
      
      if(i+2 >= argv.length || argv[i+2].equalsIgnoreCase("host")) {
        //if this is the last parameter, or if the next parameter is a next host...
        if(h != "" && p != 0) {
          System.out.println("Command-line: host " + h + " port " + p);
          t.launchViewer(h, p, pw, us);
          h = "";
          p = 0;
          pw = "";
          us = "";
        } else {
          System.out.println("ERROR: No port specified for last host (" + h + ")");
        }
      }
    }
    


    
    
  }//end of main
 
  
  
  
  
  final static float VERSION = 1.4f;
  
  static VncViewersList viewersList;//list to store all the views
  
  AddHostDialog hostDialog;
  MenuItem newhostMenuItem, loadhostsMenuItem, savehostsMenuItem, 
  exitMenuItem,auto_refresh_chk,restart_app,record_screen,ShowGroup;
  
  MenuItem grp0,grp1,grp2,grp3,grp4,grp5,grp6,grp7,grp8,grp9,grp10;
  
  
  Frame soloViewer;//
  int widthPerThumbnail, heightPerThumbnail;
  int thumbnailRowCount;

  VncThumbnailViewer() {
    viewersList = new VncViewersList(this);
    thumbnailRowCount = 0;
    widthPerThumbnail = 0;
    heightPerThumbnail = 0;

    setTitle("");//Feb 2019
    addWindowListener(this);
    addComponentListener(this);
    addMouseListener(this);

    GridLayout grid = new GridLayout();
  //  GridBagLayout grid = new GridBagLayout();
    
    setLayout(grid);
   // setLayout(new ScrollPaneLayout());
    setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize());
    setMenuBar(new MenuBar());
    getMenuBar().add( createFileMenu() );
    getMenuBar().add( createViewMenu() );
    
    
    setVisible(true);
 
    
    // this is the frame the snmall ones it creates it here
    soloViewer = new Frame();
    soloViewer.setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize());
    soloViewer.addWindowListener(this);
    soloViewer.addComponentListener(this);
    soloViewer.validate();
    soloViewer.setResizable(true);
    setResizable(true);
  }
  /*
   * This method will start the timer of the auto refresh Created by Hashim
   */
  public static void timer_Start(int time,boolean seconds) throws InterruptedException{
	 // check if it is recording then just pause it
	  
	  	
	  
	  int new_time;
	  if(seconds==true){
	   new_time = time * 1000;
	  }
	  
	  else{
		  new_time = time * 60 * 1000;
	  }
	timer = new Timer(true);
	  

	if(busy) {
		return;
	}else {
		
	
		timer.scheduleAtFixedRate(new TimerTask() {
			
			
		  	  @Override
		  	  public void run() {
		  	     
		  		
		  		  //System.out.println(viewersList.size());
		  		  if (!busy) {
		  		  System.out.println("RUNNING TIMER.....");
		  	
		  		 viewersList.reloadConnection();
		  		 
		  		 
		  		  }
		  	  }
		  	}, new Date(), new_time);//1*60*1000
		    
	
	
	
		
	
		
	}

  }
  /*
   * this method will disconnect every view before clearing the frame made by Hashim
   */
  
 private boolean clear_disconnect(){
	HashSet<VncViewer> set =  (HashSet<VncViewer>) viewersList.clone(); // hashset before
	  
	  for(VncViewer v : set){
		 
		 
		 v.disconnect();
		 
		  
	  }
	  
	  viewersList = (VncViewersList) set.clone();
	  set.clear();
	  viewersList.clear();
	  return true;
	  
  }

  
  
  public void launchViewer(String host, int port, String password, String user) {
    launchViewer(host, port, password, user, "");
  }

  public void launchViewer(String host, int port, String password, String user, String userdomain) {
    //VncViewer v = 
	  viewersList.launchViewer(host, port, password, user, userdomain);
    //addViewer(v); called by viewersList.launchViewer
  }


  synchronized void addViewer(VncViewer v) {
   // int r = (int)Math.sqrt(viewersList.size() - 1) + 1;
    int r = (int)Math.sqrt(this.getComponentCount() -1) + 1;
	 // int r = (int)Math.sqrt(real_size - 1) + 1;
	  if(r != thumbnailRowCount) {
      thumbnailRowCount = r;
      ((GridLayout)this.getLayout()).setRows(thumbnailRowCount);
      ((GridLayout)this.getLayout()).setColumns(thumbnailRowCount);
      resizeThumbnails();
      
    }
	
	  
	//  updateCanvasScaling(v, widthPerThumbnail, heightPerThumbnail);
	  VncViewersList.activeConnections.add(v);
	  
	  add(v);
	  
  
    revalidate();
    repaint();
	
	
  }


 synchronized void removeViewer(VncViewer v) {


	 v.disconnect();
	    remove(v);

    VncViewersList.activeConnections.remove(v);

   // int r = (int)Math.sqrt(viewersList.size() - 1) + 1;
    int r = (int)Math.sqrt(this.getComponentCount()-1) + 1;
  //  int r = (int)Math.sqrt(real_size - 1) + 1;
    
    if(r != thumbnailRowCount) {
      thumbnailRowCount = r;
      ((GridLayout)this.getLayout()).setRows(thumbnailRowCount);
      ((GridLayout)this.getLayout()).setColumns(thumbnailRowCount);
      resizeThumbnails();
    }
   // updateCanvasScaling(v, widthPerThumbnail, heightPerThumbnail);

   
    revalidate();
    repaint();
    
  }


  void soloHost(VncViewer v) {// solohost is the big screen when u click on it
    if(v.vc == null)
      return;

    if(soloViewer.getComponentCount() > 0)
    soloHostClose();

    soloViewer.setVisible(true);
    soloViewer.setTitle(v.host);
    this.remove(v);
    soloViewer.add(v);
    v.vc.removeMouseListener(this);
    this.validate();
    soloViewer.validate();

    if(!v.rfb.closed()) {
      v.vc.enableInput(true);
    }
    updateCanvasScaling(v, getWidthNoInsets(soloViewer), getHeightNoInsets(soloViewer));
  }


  void soloHostClose() { // closes the solo host window
    VncViewer v = (VncViewer)soloViewer.getComponent(0);
    v.enableInput(false);
    updateCanvasScaling(v, widthPerThumbnail, heightPerThumbnail);
    soloViewer.removeAll();
    addViewer(v);
    v.vc.addMouseListener(this);
    soloViewer.setVisible(false);
   // System.exit(0);
  }


  private void updateCanvasScaling(VncViewer v, int maxWidth, int maxHeight) 
  {
    maxHeight -= v.buttonPanel.getHeight();
    int fbWidth = v.vc.rfb.framebufferWidth;
    int fbHeight = v.vc.rfb.framebufferHeight;
    int f1 = maxWidth * 100 / fbWidth;
    int f2 = maxHeight * 100 / fbHeight;
    int sf = Math.min(f1, f2);
    if (sf > 100) {
      sf = 100;
    }

    v.vc.maxWidth = maxWidth;
    v.vc.maxHeight = maxHeight;
    v.vc.scalingFactor = sf;
    v.vc.scaledWidth = (fbWidth * sf + 50) / 100;
    v.vc.scaledHeight = (fbHeight * sf + 50) / 100;

    //Fix: invoke a re-paint of canvas?
    //Fix: invoke a re-size of canvas?
    //Fix: invoke a validate of viewer's gridbag?
   
  }


  void resizeThumbnails() {//
	 
	  int newWidth = getWidthNoInsets(this) / thumbnailRowCount;
	  int newHeight = getHeightNoInsets(this) / thumbnailRowCount;
	 
    if(newWidth != widthPerThumbnail || newHeight != heightPerThumbnail) {
      widthPerThumbnail = newWidth;
      heightPerThumbnail = newHeight;

      Iterator<VncViewer> l = viewersList.iterator();//viewersList.iterator()
      while(l.hasNext()) {
        VncViewer v = (VncViewer)l.next();
        
        if(!soloViewer.isAncestorOf(v)) {
          if(v.vc != null) { // if the connection has been established
            updateCanvasScaling(v, widthPerThumbnail, heightPerThumbnail);
          }
        }
      }

    }

  }
  
  
  private void loadsaveHosts(int mode) {
	  
	  timer.cancel();
	  while(busy) {
		  ;//do nothing
	  }
	busy = true;
	  this.removeAll();
	  viewersList.clear();
    FileDialog fd = new FileDialog(this, "Load hosts file...", mode);
    if(mode == FileDialog.SAVE) {
      fd.setTitle("Save hosts file...");
    }
    fd.show();

    String file = fd.getFile();
    if(file != null) {
      String dir = fd.getDirectory();
      
      if(mode == FileDialog.SAVE) {
        //ask about encrypting
        HostsFilePasswordDialog pd = new HostsFilePasswordDialog(this, true);
        if(pd.getResult()) {
          viewersList.saveToEncryptedFile(dir+file, pd.getPassword());
        } else {
          viewersList.saveToFile(dir+file);
        }
      } else {
        if(VncViewersList.isHostsFileEncrypted(dir+file)) {
          HostsFilePasswordDialog pd = new HostsFilePasswordDialog(this, false);
          loadDomains(dir+file, pd.getPassword());//
          PopViewMenu();
          viewersList.loadHosts(dir+file, pd.getPassword());
        } else {
            loadDomains(dir+file, "");//
            PopViewMenu();	
          viewersList.loadHosts(dir+file, "");
        }
      }
    }
    busy = false;
    try {
		timer_Start(6, true);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  private void quit() {
    // Called by either File->Exit or Closing of the main window
    System.out.println("Closing window");
    Iterator<VncViewer> l = viewersList.iterator();
    while(l.hasNext()) {
      ((VncViewer)l.next()).disconnect();
    }
    this.dispose();
    
    
    
  
    System.exit(0);
  }


  static public int getWidthNoInsets(Frame frame) {//private
    Insets insets = frame.getInsets();
    int width = frame.getWidth() - (insets.left + insets.right);
    return width;
 }

  static public int getHeightNoInsets(Frame frame) {//private
    Insets insets = frame.getInsets();
    int height = frame.getHeight() - (insets.top + insets.bottom);
    return height;
  }



  private Menu createFileMenu()
  {
    Menu fileMenu = new Menu("File");
    newhostMenuItem = new MenuItem("Add New Host");
    loadhostsMenuItem = new MenuItem("Load List of Hosts");
    savehostsMenuItem = new MenuItem("Save List of Hosts");
    exitMenuItem = new MenuItem("Exit");
    //auto_refresh_chk = new MenuItem("Auto Refresh");
    restart_app = new MenuItem("Restart App");
    record_screen = new MenuItem("Start Recording");
    
   
    newhostMenuItem.addActionListener(this);
    loadhostsMenuItem.addActionListener(this);
    savehostsMenuItem.addActionListener(this);
   // auto_refresh_chk.addActionListener(this);
    exitMenuItem.addActionListener(this);
    restart_app.addActionListener(this);
    record_screen.addActionListener(this);
  //  ShowGroup.addActionListener(this);
    
    
    //fileMenu.add(auto_refresh_chk);
    //fileMenu.addSeparator();
//    fileMenu.add(ShowGroup);
    //fileMenu.addSeparator();
    fileMenu.add(newhostMenuItem);
    fileMenu.addSeparator();
    fileMenu.add(loadhostsMenuItem);
    fileMenu.add(savehostsMenuItem);
    
    fileMenu.addSeparator();
   
    //fileMenu.add(record_screen);
    //fileMenu.addSeparator();
    fileMenu.add(restart_app);
    fileMenu.addSeparator();
    fileMenu.add(exitMenuItem);
    
    loadhostsMenuItem.enable(true);
    savehostsMenuItem.enable(true);
        
    return fileMenu;
  }
  private Menu createViewMenu()
  {
    Menu viewMenu = new Menu("View");
   
    grp0=new MenuItem("Group All"); // to view all
    grp0.addActionListener(this);
    viewMenu.add(grp0);
    
    
    for (String s: domains) {
    	
    	MenuItem temp = new MenuItem(s);
    	temp.addActionListener(this);
    	viewMenu.add(temp);
    	view_items.add(temp);
    }
   
       
    return viewMenu;
  }

  
  private Menu PopViewMenu()
  {
    Menu viewMenu = getMenuBar().getMenu(1); // 1 for the view menu
   
    //grp0=new MenuItem("Group All"); // to view all
    //grp0.addActionListener(this);
  //  viewMenu.add(grp0);
    
    
    for (String s: domains) {
    	
    	MenuItem temp = new MenuItem(s);
    	temp.addActionListener(this);
    	viewMenu.add(temp);
    	view_items.add(temp);
    }
   
       
    return viewMenu;
  }
  
  // Window Listener Events:
  public void windowClosing(WindowEvent evt) 
  {
   
	 
	  if(soloViewer.isShowing()) {//this is to close one frame at a time not the whole program
      soloHostClose();
    }
	  

    else{
    	
    	 
    	
    	System.exit(0);
    }

  }

  public void windowActivated(WindowEvent evt) {}
  public void windowDeactivated (WindowEvent evt) {}
  public void windowOpened(WindowEvent evt) {}
  public void windowClosed(WindowEvent evt) {}
  public void windowIconified(WindowEvent evt) {}
  public void windowDeiconified(WindowEvent evt) {}


  // Component Listener Events:
  public void componentResized(ComponentEvent evt) {
    if(evt.getComponent() == this) {
      if(thumbnailRowCount > 0) {
        resizeThumbnails();
      }
    }
    
    else { // resize soloViewer
      VncViewer v = (VncViewer)soloViewer.getComponent(0);
      updateCanvasScaling(v, getWidthNoInsets(soloViewer), getHeightNoInsets(soloViewer));
    }

  }

  public void componentHidden(ComponentEvent  evt) {}
  public void componentMoved(ComponentEvent evt) {}
  public void componentShown(ComponentEvent evt) {}


  // Mouse Listener Events:
  public void mouseClicked(MouseEvent evt) {
    if(evt.getClickCount() == 2) {
      Component c = evt.getComponent();
      if(c instanceof VncCanvas) {
        soloHost( ((VncCanvas)c).viewer );
      }
    }
    
  }

  public void mouseEntered(MouseEvent evt) {}
  public void mouseExited(MouseEvent evt) {}
  public void mousePressed(MouseEvent evt) {}
  public void mouseReleased(MouseEvent evt) {}


  // Container Listener Events:
  public void componentAdded(ContainerEvent evt) {
    // This detects when a vncviewer adds a vnccanvas to it's container
    if(evt.getChild() instanceof VncCanvas) {
      VncViewer v = (VncViewer)evt.getContainer();
      v.vc.addMouseListener(this);
      v.buttonPanel.addContainerListener(this);
      v.buttonPanel.disconnectButton.addActionListener(this);
      updateCanvasScaling(v, widthPerThumbnail, heightPerThumbnail);
    }

    // This detects when a vncviewer's Disconnect button had been pushed
    else if(evt.getChild() instanceof Button) {
      Button b = (Button)evt.getChild();
      if(b.getLabel() == "Hide desktop") {
        b.addActionListener(this);
      }
    }

  }
  
  public void componentRemoved(ContainerEvent evt) {}
  
  
  // Action Listener Event:
  public void actionPerformed(ActionEvent evt) {
    if( evt.getSource() instanceof Button && ((Button)evt.getSource()).getLabel() == "Hide desktop") {
      VncViewer v = (VncViewer)((Component)((Component)evt.getSource()).getParent()).getParent();
      this.remove(v);
      viewersList.remove(v);
    }
    if(evt.getSource() == newhostMenuItem) {
      hostDialog = new AddHostDialog(this);
    }
    if(evt.getSource() == savehostsMenuItem) {
      loadsaveHosts(FileDialog.SAVE);
    }
    if(evt.getSource() == loadhostsMenuItem) {
      loadsaveHosts(FileDialog.LOAD);
    }
    if(evt.getSource() == exitMenuItem) {
      quit();
      
    }
    
    if(evt.getSource() == restart_app) {
        try {
			restartApplication();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
        
      }
    

    
  // get the grp source
    
    if (view_items.contains(evt.getSource())) {
    	
   	
    	for (MenuItem itm:view_items) {
    		
    		
    		if (evt.getSource()==itm) {
    			String label = "";
    			
    			if (itm.getLabel().contains((char)Character.toUpperCase(0x2713)+"")) 
    			{
//    	    		
    	    		itm.setLabel(itm.getLabel().substring(0, itm.getLabel().indexOf((char)Character.toUpperCase(0x2713))));
    	    		label = itm.getLabel();
    			}
    			else 
    			{
    				label = itm.getLabel().toString();	
    				itm.setLabel(itm.getLabel() + "" + (char)Character.toUpperCase(0x2713));
	    
    	    	}
    			System.out.println("LABEL IS : " + label + "Label size : " + label.length());
    			String l = label;
    			
    	    		
    	    		
    	
    	    			try 
    	    			{
							viewersList.showOnly("host" , "",true,l);
						} 
    	    			catch (InterruptedException e) 
    	    			{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    	    	        
    	    		}
    	 
    		
    	}
    	
    }
    
    
    
    
    
    
    
    if(evt.getSource()==grp0)
    {

    		
    		try 
    		{
				viewersList.showOnly("host" , "",true,"0");
			} catch (InterruptedException e) 
    		{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	   	 
    	 
    		for (MenuItem itm:view_items) 
    		{     			
        			if (itm.getLabel().contains((char)Character.toUpperCase(0x2713)+"")) 
        			{
        	    		
        	    		itm.setLabel(itm.getLabel().substring(0, itm.getLabel().indexOf((char)Character.toUpperCase(0x2713))));
        	    		VncViewersList.selectedGroup.clear();
        			}
    		}    	 
    }

    /*
     * check for the auto refresh click
     */
		/*
		 * if(evt.getSource()==auto_refresh_chk){
		 * 
		 * //System.out.println(auto_refresh_chk.getLabel());
		 * if(auto_refresh_chk.getLabel().contains("ON")){//TURN OFF AUTO REFRESH
		 * 
		 * // setTitle("DJC Thumbnail Viewer");
		 * auto_refresh_chk.setLabel("Auto Refresh"); timer.cancel();
		 * 
		 * } else{//turn on the auto refresh
		 * 
		 * 
		 * auto_refresh_chk.setLabel(auto_refresh_chk.getLabel() + " Is ON"); //
		 * 
		 * 
		 * try {
		 * 
		 * String arr[] = {"seconds","minutes"}; String timev[] = {"90","120","200"};
		 * boolean second_selection = true;// this is flag if the user specified the
		 * time in seconds Object time = JOptionPane.showInputDialog(null,
		 * "Please Select Amount...", "choose how many seconds",
		 * JOptionPane.QUESTION_MESSAGE, null,(String[])timev , "true");//returns value
		 * selected total_seconds = Integer.parseInt((String)time);
		 * timer_Start(Integer.parseInt((String)time),second_selection); } catch
		 * (Exception e) { }
		 * 
		 * }
		 * 
		 * }
		 */
  }
  
  
 /*
  * this method restarts the application got it from stackoverflow
  */
  private void restartApplication() throws Exception
  {
	   StringBuilder cmd = new StringBuilder();
       cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
       for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
           cmd.append(jvmArg + " ");
       }
       cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
       cmd.append(VncThumbnailViewer.class.getName()).append(" ");
      
       Thread.currentThread().sleep(10000); // 10 seconds delay before restart
       Runtime.getRuntime().exec(cmd.toString());
       System.exit(0);
  }
  
  
  
  /**
   * LOAD THE DOMAINS FOR THE VIEW MENU
   */
  public static void loadDomains(String filename, String encPassword) 
  {
	    
	  
	  
	  File file = new File(filename);
	  try {// this is when we load the file what happens
	     
	      URL url = file.toURL();
	      filename = url.getPath();

	      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
	      IXMLReader reader = StdXMLReader.fileReader(filename);
	      parser.setReader(reader);
	      
	      IXMLElement root = (IXMLElement) parser.parse();//parse the xml information

	      if(root.getFullName().equalsIgnoreCase("Manifest") ) {
	        boolean encrypted = (1 == Integer.parseInt( root.getAttribute("Encrypted", "0") ) );
	        float version = Float.parseFloat( root.getAttribute("Version", "0.9") );
	     //   System.out.println("Loading file...  file format version " + version + " encrypted(" + encrypted + ")");
	        
	        if(encrypted && encPassword == null) {
	          // FIX-ME: do something
	          System.out.println("ERROR: Password needed to properly read file.");
	        }
	        
	        Enumeration enm = root.enumerateChildren();
	        while (enm.hasMoreElements()) {
	          IXMLElement e = (IXMLElement)enm.nextElement();
	          
	          
	          if(e.getFullName().equalsIgnoreCase("Connection")) {
	            boolean success = parseDomains(e, encrypted, encPassword);
	
	          }
	          else {
	    //        System.out.println("Load: Ignoring " + e.getFullName());
	          }
	        }
	        
	      } else {
	        System.out.println("Malformed file, missing manifest tag.");
	        System.out.println("Found " + root.getFullName());
	      }

	    } catch (Exception e) {
	      System.out.println("Error loading file.\n" + e.getMessage() );
	    }
	    
	  }


  private static boolean parseDomains(IXMLElement e, boolean isEncrypted, String encPass) {
	    String host = e.getAttribute("Host", null);// get the attribute host from the element connection
	    String prt = e.getAttribute("Port", null);

	    
	    
	    boolean success = true;
	    
	    if(prt == null || host == null) {// if there was a mistake with the host or port then i output this error msg
	      System.out.println("Missing Host or Port attribute");
	      success = false;
	    } else {
	      int port = Integer.parseInt(prt);

	      int secType = Integer.parseInt(e.getAttribute("SecType", "1"));
	      String password = e.getAttribute("Password", null);
	      String username = e.getAttribute("Username", null);
	      String userdomain = e.getAttribute("UserDomain", null);
	      String compname = e.getAttribute("CompName", null);
	      String comment = e.getAttribute("Comment", null);
	            
	      if(isEncrypted) 
	      {
	        if(password != null)
	          password = DesCipher.decryptData(password, encPass);
	        if(username != null)
	          username = DesCipher.decryptData(username, encPass);
	        if(userdomain != null)
	          userdomain = DesCipher.decryptData(userdomain, encPass);
	        if(compname != null)
	          compname = DesCipher.decryptData(compname, encPass);
	        if(comment != null)
	          comment = DesCipher.decryptData(comment, encPass);
	      }
	      
	      domains.add(userdomain);
	   
	    }
	    return success;
	  }
  
}
