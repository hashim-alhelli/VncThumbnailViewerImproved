# VncThumbnailViewerImproved
This is based on the project in this link https://code.google.com/archive/p/vncthumbnailviewer/

Please copy and paste the following data as base for your host file 

<?xml version="1.0" standalone="yes"?>
<Manifest Encrypted="0" Version="1.4">

    <Connection Host="host_ip" Port="port" SecType="2" Password="your_password" UserDomain="the_grp_name"/>
    
    
</Manifest>


How to use the program:

1) click on file
2) click on load host file
3) navigate to the location of the host file and double click on it.
4) you can sort the thumbnails by the grp name using the view menue and choosing the grp.
5) enjoy.

New feutures added:


1) when a connection does not exist the viewer for it is removed so they don't occupy space on screen hence making other thumbnails 
  bigger.
2) Added automatic check for new connections every 6 seconds if a new connection becomes live it will added 
automatically to the viewer without need of restart the application.
3) Added view by group name which dynamically finds the grp name from the host file based on the  "UserDomain" parameter that is in the host
file.
4)Added the ip and the port on top of the thumb inside the viewer so each thumb can be distinguished from the other.

