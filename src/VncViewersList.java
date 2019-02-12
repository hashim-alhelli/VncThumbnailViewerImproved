
//  Copyright (C) 2007-2008 David Czechowski  All Rights Reserved.
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

import java.awt.*;

import java.io.*;
import java.net.URL;

 // Source available at http://nanoxml.cyberelf.be/
import java.util.*;
import net.n3.nanoxml.*;
//TEMP

//
// This HashSet list is used to maintain of a list of VncViewers
//
// This also contains the ability to load/save it's list of VncViewers to/from
//    an external xml file.
//

public class VncViewersList extends HashSet<VncViewer> {

  /**
	 * 
	 */
	private static final long serialVersionUID = 6132115591176607232L;

	protected static HashSet<VncViewer> seconnd_list = new HashSet<VncViewer>(); 
	// the thing is we will add to this list when we auto refresh if this list size is greater than old list size then we refresh the screen otherwise then we just clear this list and retry again
	protected static HashSet<VncViewer> activeConnections = new HashSet<VncViewer>(); // this list will save all active connections
    protected static HashSet<String> selectedGroup = new HashSet<String>(); // this will contain the selected grps
	protected static HashMap<String,HashSet<Integer>> host_to_ports = new HashMap<String,HashSet<Integer>>();
	
    static boolean lock = false;
 
    
  public VncViewersList(VncThumbnailViewer v)
  {
    super();
    
    tnViewer = v; // make a thumbnail viewer and assign this list to it
    
  }

  public static VncThumbnailViewer tnViewer;//private
  public static boolean isHostsFileEncrypted(String filename) {
    boolean encrypted = false;
    
    try {
      File file = new File(filename);
      URL url = file.toURL();
      filename = url.getPath();
            
      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
      IXMLReader reader = StdXMLReader.fileReader(filename);
      parser.setReader(reader);
      IXMLElement root = (IXMLElement) parser.parse();

      if(root.getFullName().equalsIgnoreCase("Manifest") ) {
        String e = root.getAttribute("Encrypted", "0");
        if(Integer.parseInt(e) == 1) {
          encrypted = true;
        }
      }

    } catch (Exception e) {
      System.out.println("Error testing file for encryption.");
      System.out.println(e.getMessage());
    }
    
    return encrypted;
    // this returns false even if there is a problem reading the file
  }


  public void loadHosts(String filename, String encPassword) {
    try {// this is when we load the file what happens
      File file = new File(filename);
      URL url = file.toURL();
      filename = url.getPath();

      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
      IXMLReader reader = StdXMLReader.fileReader(filename);
      parser.setReader(reader);
      IXMLElement root = (IXMLElement) parser.parse();//parse the xml information

      if(root.getFullName().equalsIgnoreCase("Manifest") ) {
        boolean encrypted = (1 == Integer.parseInt( root.getAttribute("Encrypted", "0") ) );
        float version = Float.parseFloat( root.getAttribute("Version", "0.9") );
      //  System.out.println("Loading file...  file format version " + version + " encrypted(" + encrypted + ")");
        
        if(encrypted && encPassword == null) {
          // FIX-ME: do something
          System.out.println("ERROR: Password needed to properly read file.");
        }
        
        Enumeration enm = root.enumerateChildren();
        while (enm.hasMoreElements()) {
          IXMLElement e = (IXMLElement)enm.nextElement();
          
          
          if(e.getFullName().equalsIgnoreCase("Connection")) {
            boolean success = parseConnection(e, encrypted, encPassword);
     //       System.out.println("success is " + success);
          }
          else {
            System.out.println("Load: Ignoring " + e.getFullName());
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


  public void removeme(VncViewer v){
	  System.out.println("before remove size is " + this.size());
	  this.remove(v);
	  
	  //added
	  seconnd_list.remove(v);
	  //
	  	  
	  System.out.println("removed and size is " + this.size());
  }
  public void loadHosts2(String filename, String encPassword) {//this method loads the host file automaticallu my version
	  tnViewer.busy=true;
	  System.out.println("CAME HERE LOADING HOSTS");
	  
	  File file = new File(filename);
	  try {// this is when we load the file what happens
	     
	      URL url = file.toURL();
	      filename = url.getPath();
	      
		  System.out.println("CAME HERE LOADING HOSTS2");
		  
	      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
	      IXMLReader reader = StdXMLReader.fileReader(filename);
	      parser.setReader(reader);
	      
	      LinkedList<IXMLElement> xml_connections = new LinkedList<IXMLElement>();
	      
	      IXMLElement root = (IXMLElement) parser.parse();//parse the xml information

	      if(root.getFullName().equalsIgnoreCase("Manifest") ) {
	        boolean encrypted = (1 == Integer.parseInt( root.getAttribute("Encrypted", "0") ) );
	        float version = Float.parseFloat( root.getAttribute("Version", "0.9") );
	        System.out.println("Loading file...  file format version " + version + " encrypted(" + encrypted + ")");
	        
	        if(encrypted && encPassword == null) {
	          // FIX-ME: do something
	          System.out.println("ERROR: Password needed to properly read file.");
	        }
	        
	        Enumeration enm = root.enumerateChildren();
	        while (enm.hasMoreElements()) { //loop
	        	
	        //	tnViewer.ok=false;
	      
	    	  
	          IXMLElement e = (IXMLElement)enm.nextElement();
	          
	          
	          if(e.getFullName().equalsIgnoreCase("Connection")) {
	        	//  System.out.println("CAME HERE LOADING HOSTS2");
	  	        
	            boolean success = parseConnection(e, encrypted, encPassword);//this gives problem whenthere is crash it slows down new ones
	          //  System.out.println("success is " + success);
	        //  xml_connections.add(e);
	          }
	          else {
	            System.out.println("Load: Ignoring " + e.getFullName());
	          }
	        }
	        
	   
	        
	      } else {
	        System.out.println("Malformed file, missing manifest tag.");
	        System.out.println("Found " + root.getFullName());
	      }

	    } catch (Exception e) {
	    	
	      System.out.println("Error loading file.\n" + e.getMessage() );
	      e.printStackTrace();
	    }
	    
	tnViewer.busy = false;
	  }
  
  // re write it
 synchronized  public void showOnly(String filename, String encPassword,boolean section,String grp) throws InterruptedException  {//this method loads the host file automaticallu my version
	    
	Component c[] = tnViewer.getComponents(); // all my active connections are here
	tnViewer.timer.cancel();
/*	for (int i=0; i<c.length; i++){
				
	if (c[i].getClass().getName().equalsIgnoreCase("VncViewer")) {
		
		VncViewer v = (VncViewer) c[i];		
		int r = (int)Math.sqrt(tnViewer.getComponentCount()-1) + 1;
		 if(r != tnViewer.thumbnailRowCount) {
		      tnViewer.thumbnailRowCount = r;
		      ((GridLayout)tnViewer.getLayout()).setRows(tnViewer.thumbnailRowCount);
		      ((GridLayout)tnViewer.getLayout()).setColumns(tnViewer.thumbnailRowCount);
		    tnViewer.  resizeThumbnails();
		    }
//		 tnViewer.remove(view);

			tnViewer.remove(v);
			
	}
		
	
}
	*/ 
	
	
	while(VncThumbnailViewer.busy) {
		;//wait 
	}
	
	//Thread.currentThread().sleep(5000);
	
	tnViewer.removeAll();	
	tnViewer.revalidate();
	tnViewer.repaint();
	
	HashSet<VncViewer>activeconnections_clone = new HashSet<VncViewer>(activeConnections);
	
	if (grp.equalsIgnoreCase("0")) 
	{
		
		selectedGroup.clear();
		
		
	
		for(VncViewer view:activeconnections_clone)
		{
				try 
				{		
					//launchViewer(view.host, view.port, view.passwordParam, view.usernameParam, view.userdomain);		
					tnViewer.addViewer(view);
				}catch(Exception e) 
				{
					System.out.println(e.getMessage());
				}
		}
	}
	
		else 
		{	
			
			for(VncViewer view:activeconnections_clone)
			{
	
			if (view.userdomain.equalsIgnoreCase(grp)) 
			{
				
			
				if (view.connStatusLabel.toString().contains("Performing standard VNC authentication")) 
				{
			
					if(view.isShowing()) 
					{
							
				
					}
					else //if the domain equals my grp i want to see if my list contains other domain
					{
						
						if (selectedGroup.contains(grp)) { // if i want to cancel the group
							
							int r = (int)Math.sqrt(tnViewer.getComponentCount()-1) + 1;
							 if(r != tnViewer.thumbnailRowCount) {
							      tnViewer.thumbnailRowCount = r;
							      ((GridLayout)tnViewer.getLayout()).setRows(tnViewer.thumbnailRowCount);
							      ((GridLayout)tnViewer.getLayout()).setColumns(tnViewer.thumbnailRowCount);
							      tnViewer.resizeThumbnails();
							    }
							 tnViewer.remove(view);
							 System.out.println("removed grp " + grp);
							
							
						}else 
						{
							
							  //launchViewer(view.host, view.port, view.passwordParam, view.usernameParam, view.userdomain);
						tnViewer.addViewer(view);
						}
					}
				}
			}
			else // if the domain doesnt equal my grp see if if i have it clickdd before then keep it
			{
				
				if (selectedGroup.contains(view.userdomain)) 
				{
					//launchViewer(view.host, view.port, view.passwordParam, view.usernameParam, view.userdomain);
					tnViewer.addViewer(view);
					//tnViewer.resizeThumbnails();
			
				}else 
				{			
					 
					int r = (int)Math.sqrt(tnViewer.getComponentCount()-1) + 1;
					 if(r != tnViewer.thumbnailRowCount) {
					      tnViewer.thumbnailRowCount = r;
					      ((GridLayout)tnViewer.getLayout()).setRows(tnViewer.thumbnailRowCount);
					      ((GridLayout)tnViewer.getLayout()).setColumns(tnViewer.thumbnailRowCount);
					    tnViewer.resizeThumbnails();
					 }
					
					 tnViewer.remove(view);
				//	tnViewer.resizeThumbnails();
					
				}
			}
		}	
	
		if (selectedGroup.contains(grp)) 
		{
			selectedGroup.remove(grp);
		}else 
		{
			selectedGroup.add(grp);
		}

		}//end for
		
	VncThumbnailViewer.busy = false;
	tnViewer.timer_Start(6, true);
		
  }
  
 

 synchronized private boolean parseConnection(IXMLElement e, boolean isEncrypted, String encPass) { //added synchronized
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
            
      if(isEncrypted) {
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
      
      // Error Checking:
      switch(secType) {
        case 1: // none
          if(password != null || username != null) {
            System.out.println("WARNING: Password or Username specified for NoAuth");
          }
        case 2: // vnc auth
          if(password == null) {
            System.out.println("ERROR: Password missing for VncAuth");
            success = false;
          }
          if(username != null) {
            System.out.println("WARNING: Username specified for VncAuth");
          }
          break;
        case -6: // ms-logon
          if(password == null || username != null) {
            System.out.println("ERROR: Password or Username missing for MsAuth");
            success = false;
          }
          break;
        case 5: // ra2
        case 6: // ra2ne
        case 16: // tight
        case 17: // ultra
        case 18: // tls
        case 19: // vencrypt
          System.out.println("ERROR: Incomplete security type (" + secType + ") for Host: " + host + " Port: " + port);
        case 0: // invalid
        default:
          // Error
          success = false;
          break;
      }
	

      // Launch the Viewer:
     // System.out.println("LOAD Host: " + host + " Port: " + port + " SecType: " + secType);
      if(success){
    	  
        if(getViewer(host, port) == null) {
	
        	VncViewer v =  launchViewer(host, port, password, username, userdomain);        		
		  
        }
        
        else{
        	try {
		
        		if(getViewer(host, port).isShowing()){ // important
				
				}
				else{
	
					/*
					 * // we need to remove it from list because it will be added later when we lunch viewer
					 * this will make sure we dont get the duplicate screens problem
					 */
					getViewer(host, port).connectAndAuthenticate();
					
				//	remove(getViewer(host, port)); 
				//	tnViewer.real_size--;
					VncViewer v =  launchViewer(host, port, password, username, userdomain);	
				}
				//VncViewer v =  launchViewer(host, port, password, username, userdomain);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				getViewer(host, port).disconnect();
				
			
				e1.printStackTrace();
				
			}
        }
        // else - the host is already open
      }
    }

    return success;
  }
  
	static int j =0;  

   boolean reloadConnection()  {

	   
	   
	VncThumbnailViewer.busy=true;
	for (int i =0; i <this.size(); i++) {
		
		VncViewer view= (VncViewer) this.toArray()[i]; // this will have all the vncviewrs
			
	
		Thread t = new Thread(view.host+":"+view.port) {
			
			public void run() {
				
				
				try {
					
					Component c[] = tnViewer.getComponents(); // all my active connections are here
					boolean component_is_showing = false;
					for (int j=0; j<c.length; j++){			
					if (c[j].getClass().getName().equalsIgnoreCase("VncViewer")) {
						
						VncViewer v = (VncViewer) c[j];
						
						
						if (v.host.equals(view.host) && v.port==view.port) {
							component_is_showing = true;
							
							break; // means this component is found
							
						}else {
							if(tnViewer.soloViewer.getComponents().length > 0) 
							{
								VncViewer vsolo = (VncViewer)tnViewer.soloViewer.getComponent(0);
								
								
								
								if (vsolo.host.equals(view.host) && vsolo.port==view.port) {
									component_is_showing = true;
									
									break; // means this component is found
									
								}
								
								
								
							}
							
						}
						
					}
						
					}//for loop end
					
					if (component_is_showing) {
						
						
					}else {
						
						//check if group is selected
						
						if ((selectedGroup.size()>0)) {
						
							if(selectedGroup.contains(view.userdomain)) {
								view.connectAndAuthenticate();
								launchViewer(view.host, view.port, view.passwordParam, view.usernameParam, view.userdomain);
							}
						
						}
						else {
							
							view.connectAndAuthenticate();
							launchViewer(view.host, view.port, view.passwordParam, view.usernameParam, view.userdomain);
					
						}
						
					}				
					
					
				} catch (Exception e) 
				{
					
					System.out.println(e.getMessage());
				}	
				
			}
			
		};
		t.start();
		j++;
		
		
		System.out.println("i is " + i);
		
	}
	  
	  
		  		VncThumbnailViewer.busy = false;
		  		return true;
	  
  }

  public void saveToEncryptedFile(String filename, String encPassword) {
    if(encPassword == null || encPassword.length() == 0) {
      System.out.println("WARNING: Saving to encrypted file with empty passkey");
    }
    saveHosts(true, filename, encPassword);
  }
  
  
  public void saveToFile(String filename) {
    saveHosts(false, filename, null);
  }
 
  private void saveHosts(boolean isEncrypted, String filename, String encPassword) {

    IXMLElement manifest = new XMLElement("Manifest");
    manifest.setAttribute("Encrypted", (isEncrypted? "1" : "0") );
    manifest.setAttribute("Version", Float.toString(VncThumbnailViewer.VERSION));
    
    Iterator l = iterator();
    while(l.hasNext()) {
      VncViewer v = (VncViewer)l.next();
      String host = v.host;
      String port = Integer.toString(v.port);
      String password = v.passwordParam;
      String username = v.usernameParam;
      //String userdomain = 
      //String compname =
      //String comment =
      String sectype = "1";
      if(password != null && password.length() != 0) {
        sectype = "2";
        if(username != null && username.length() != 0) {
          sectype = "-6";
        }
      }
      
      if(isEncrypted) {
        if(sectype != "1")
          password = DesCipher.encryptData(password,encPassword);
        if(sectype == "-6") {
          username = DesCipher.encryptData(username,encPassword);
          //userdomain = encryptData(userdomain,encPassword);
        }
        //compname = encryptData(compname,encPassword);
        //comment = encryptData(comment,encPassword);
      }

      IXMLElement c = manifest.createElement("Connection");
      manifest.addChild(c);
      c.setAttribute("Host", host);
      c.setAttribute("Port", port);
      c.setAttribute("SecType", sectype);
      if(sectype == "2" || sectype == "-6") {
        c.setAttribute("Password", password);
        if(sectype == "-6") {
          c.setAttribute("Username", username);
          //c.setAttribute("UserDomain", userdomain);
        }
      }
      //c.setAttribute("CompName", "");
      //c.setAttribute("Comment", "");
    }
    
    try {
      PrintWriter o = new PrintWriter( new FileOutputStream(filename) );
      XMLWriter writer = new XMLWriter(o);
      o.println("<?xml version=\"1.0\" standalone=\"yes\"?>");
      writer.write(manifest, true);
      
    } catch (IOException e) {
      System.out.print("Error saving file.\n" + e.getMessage() );
    }
    
  }


  public VncViewer launchViewer(String host, int port, String password, String user, String userdomain) {
    VncViewer v = launchViewer(tnViewer, host, port, password, user, userdomain);
	
    tnViewer.addViewer(v);
    add(v);// adding the view to the vector
    
  return v;
  }
  

  public static VncViewer launchViewer(VncThumbnailViewer tnviewer, String host, int port, String password, String user, String userdomain) {
    String args[] = new String[4];
    args[0] = "host";
    args[1] = host;
    args[2] = "port";
    args[3] = Integer.toString(port);

    if(password != null && password.length() != 0) {
      int newlen = args.length + 2;
      String[] newargs = new String[newlen];
      System.arraycopy(args, 0, newargs, 0, newlen-2);
      newargs[newlen-2] = "password";
      newargs[newlen-1] = password;
      args = newargs;
    }

    if(user != null && user.length() != 0) {
      int newlen = args.length + 2;
      String[] newargs = new String[newlen];
      System.arraycopy(args, 0, newargs, 0, newlen-2);
      newargs[newlen-2] = "username";
      newargs[newlen-1] = user;
      args = newargs;
    }

    if(userdomain != null && userdomain.length() != 0) {
      int newlen = args.length + 2;
      String[] newargs = new String[newlen];
      System.arraycopy(args, 0, newargs, 0, newlen-2);
      newargs[newlen-2] = "userdomain";
      newargs[newlen-1] = userdomain;
      args = newargs;
    }

    // launch a new viewer
  //  System.out.println("Launch Host: " + host + ":" + port);
    VncViewer v = new VncViewer();// its like new jframe its the small view
    
    
    v.mainArgs = args;
    v.inAnApplet = false;//false
    v.inSeparateFrame = false;//false
    v.showControls = true;
    v.showOfflineDesktop = true;//true
    v.vncFrame = tnviewer;
    v.init();
    v.options.viewOnly = true;//
    v.options.autoScale = true; // true // false, because ThumbnailViewer maintains the scaling
    v.options.scalingFactor = 10;//10
    v.userdomain=userdomain;
    v.addContainerListener(tnviewer);
    v.start();
    return v;

  }


  public VncViewer getViewer(String hostname, int port) {
    VncViewer v = null;
 
    Iterator<VncViewer> l = this.iterator();
    while(l.hasNext()) {
      v = (VncViewer)l.next();
      if(v.host.equals( hostname) && v.port == port) { // v.host == hostname
        return v;
      }
    }

    return null;
  }

  public VncViewer getViewer(Container c) {
    VncViewer v = null;

    Iterator l = iterator();
    while(l.hasNext()) {
      v = (VncViewer)l.next();
      if(c.isAncestorOf(v)) {
        return v;
      }
    }

    return null;
  }

  public VncViewer getViewer(Button b) {
    VncViewer v;

    Iterator l = iterator();
    while(l.hasNext()) {
      v = (VncViewer)l.next();
      if(v.getParent().isAncestorOf(b)) {
        return v;
      }
    }

    return null;
  }
  
  
 


  
  
  
  
  
  
  
  

}
