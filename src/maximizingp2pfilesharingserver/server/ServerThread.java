/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maximizingp2pfilesharingserver.server;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import maximizingp2pfilesharingclient.Globals;
import maximizingp2pfilesharingclient.HexDecoder;
import maximizingp2pfilesharingserver.algorithms.PCS_Algorithms;

/**
 *
 * @author Master PC
 */
public class ServerThread {
    
    private Hashtable<String,Socket> clients = new Hashtable<String, Socket>();
    private ServerCallback callback;

    public Hashtable<String, Socket> getClients() {
        return clients;
    }

    public void setClients(Hashtable<String, Socket> clients) {
        this.clients = clients;
    }

    public ServerCallback getCallback() {
        return callback;
    }

    public void setCallback(ServerCallback callback) {
        this.callback = callback;
    }
            
    public void replicateFile(String to,String fileName,byte[] fileContent) {
        PrintWriter pw1 = null;
        try {
            String hex = HexDecoder.encode(fileContent);
            
            pw1 = new PrintWriter(this.clients.get(to).getOutputStream());
            
            pw1.println("[file]:"+fileName);
            pw1.flush();
            
            int div = hex.length()/1024;
            int divi = hex.length()%1024;
            int i=0;
            for(i=0;i<div;i++) {
                String hex1 = hex.substring(i*1024, i*1024+1024);
            
                pw1.println(hex1);
                pw1.flush();
            }
            
            if(divi!=0) {
                String hex1 = hex.substring(i*1024, i*1024+divi);
            
                pw1.println(hex1);
                pw1.flush();
            }
            
            pw1.println("---end of file---");
            pw1.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //pw1.close();
        }
    }
    
    public List<String> getFiles(String node) {
        try {
            PrintWriter pw = new PrintWriter(this.getClients().get(node).getOutputStream());

            Globals.lastResultCompleted = false;
            
            pw.println("[getfiles]");
            pw.flush();
            
            while(!Globals.lastResultCompleted)
                ;
            
            return Globals.lastResult;
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Globals.lastResult;
    }
    
    public List<String> getFile(String node,String fileName) {
        try {
            PrintWriter pw = new PrintWriter(this.getClients().get(node).getOutputStream());

            Globals.lastResultCompleted = false;
            
            pw.println("[getfile]:"+fileName);
            pw.flush();
            
            while(!Globals.lastResultCompleted)
                ;
            
            return Globals.lastResult;
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Globals.lastResult;
    }
    
    public void start(int port) {
        
        Thread serverThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    final ServerSocket server = new ServerSocket(port);
                    
                    System.out.println("Node Started...");
                    
                    while(true) {
                        final Socket client = server.accept();

                        System.out.println("Node Connected...");
                        
                        Thread clientThread = new Thread(new Runnable() {

                            private String clientId = "";
                            
                            @Override
                            public void run() {
                                
                                BufferedReader reader = null;
                                try {
                                    reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                    PrintWriter pw = new PrintWriter(client.getOutputStream());
                                    
                                    String line = "";
                                    
                                    while((line=reader.readLine())!=null) {
                                        System.out.println("Node Receive Thread: "+line);
                                        
                                        if(line.startsWith("[connect]")) {
                                            
                                            String clientId = line.split(":")[1].split(",")[0];
                                            String clientPort = "-1";
                                            clientPort = line.split(":")[1].split(",")[1];
                                            
                                            clients.put(clientId, client);
                                            
                                            String ipAddress = client.getInetAddress().getHostAddress();
                                            
                                            this.clientId = clientId;
                                            
                                            if(callback!=null) {
                                                if(!clientPort.equals("-1"))
                                                   callback.updateConnect(ipAddress, clientId, clientPort);
                                            }
                                            
                                        } else if(line.startsWith("[file]")) {

                                            String fileName = line.split(":")[1].split(",")[0];
                                            String node = line.split(":")[1].split(",")[1];
                                            String path = line.split(":")[1].split(",")[2];

                                            String hex = "";

                                            while(!(line=reader.readLine()).equals("---end of file---")) {
                                                hex += line;
                                            } 

                                            if(node.equals(clientId) || (path.split(";")[0].equals(clientId) && path.split(";")[path.split(";").length-1].equals(node))) {
                                                byte[] fileContent = HexDecoder.decode(hex);

                                                if(callback!=null) 
                                                   callback.updateFile(fileName, fileContent);
                                            } else {
                                                byte[] fileContent = HexDecoder.decode(hex);
                                                
                                                if(callback!=null) {
                                                   callback.updateFile(fileName,node, path, fileContent);
                                                }
                                            }
                                            
                                        } else if(line.startsWith("[files]")) {
                                            Globals.lastResult.clear();
                                            
                                            while(!(line=reader.readLine()).equals("---end of files---")) {
                                                Globals.lastResult.add(line);
                                            }
                                            
                                            Globals.lastResultCompleted = true;
                                            
                                        } else if(line.startsWith("[disconnect]")) {
                                            String clientId = line.split(":")[1];
                                            
                                            if(clients.containsKey(clientId)) {
                                                clients.remove(clientId);
                                            }
                                        } else if(line.startsWith("[getallfiles]")) {
                                            Iterator<String> clientsList = clients.keySet().iterator();
                                            
                                            List<String> allFiles = new ArrayList<String>();
                                            
                                            while(clientsList.hasNext()) {
                                                String node = clientsList.next();
                                                
                                                List<String> filesInNode = getFiles(node);
                                                
                                                allFiles.addAll(filesInNode);
                                            }
                                            
                                            for(String file : allFiles) {
                                                pw.println(file);
                                                pw.flush();
                                            }
                                            
                                            pw.println("---end of files---");
                                            pw.flush();
                                            
                                        } else if(line.startsWith("[getfiles]")) {

                                            String[] files = new File(Globals.p2pFileSharesPath).list();

                                            pw.println("[files]");
                                            pw.flush();

                                            for(String file : files) {
                                                if(!new File(Globals.p2pFileSharesPath+"\\"+file).isDirectory()) {
                                                    pw.println(file);
                                                    pw.flush();
                                                }
                                            }

                                            pw.println("---end of files---");
                                            pw.flush();
                            
                                        } else if(line.startsWith("[getnodes]")) {
                                            Iterator<String> nodes = clients.keySet().iterator();
                                            
                                            while(nodes.hasNext()) {
                                                String node = nodes.next();
                                                
                                                pw.println(node);
                                                pw.flush();
                                            }
                                            
                                            pw.println("---end of nodes---");
                                            pw.flush();
                                        } else if(line.startsWith("[broadcastreply]")) {
                                            String node = line.split(":")[1].split(",")[0];
                                            String file = line.split(":")[1].split(",")[1];
                                            String path = line.split(":")[1].split(",")[2];

                                            if(callback!=null)
                                                callback.updateReply(node,path,file);
                                        } else if(line.startsWith("[broadcastrequest]")) {
                                            String node = line.split(":")[1].split(",")[0];
                                            String file = line.split(":")[1].split(",")[1];
                                            String path = line.split(":")[1].split(",")[2];

                                            if(callback!=null)
                                                callback.updateRequest(node,path,file);
                                        } else if(line.startsWith("[getfile]")) {
                                            String node = line.split(":")[1].split(",")[0];
                                            String file = line.split(":")[1].split(",")[1];
                                            String path = line.split(":")[1].split(",")[2];
                                            
                                            if(callback!=null) {
                                                try {
                                                    Object[] filesOneLost = PCS_Algorithms.PCS_Algorithm(null,file, node, "");

                                                    List<File> filesOne = (List<File>) filesOneLost[0];
                                                    List<File> filesLost = (List<File>) filesOneLost[1];


                                                    for(File file1 : filesOne) {
                                                        if(filesLost.contains(file1)) {
                                                            filesLost.remove(file1);
                                                        }
                                                    }

                                                    for(File file1 : filesLost) {
                                                        if(filesOne.contains(file1)) {
                                                            filesOne.remove(file1);
                                                        }
                                                    }


                                                    for(File fileOne : filesOne) {
                                                        if(!new File(Globals.p2pFileSharesPath+"\\won_drive").exists())
                                                            new File(Globals.p2pFileSharesPath+"\\won_drive").mkdir();

                                                        System.out.println("Resource Allocation : Won File : "+fileOne.getName());                                            

                                                        FileInputStream fileIn = new FileInputStream(fileOne);
                                                        byte[] filecontent = new byte[fileIn.available()];

                                                        fileIn.read(filecontent);
                                                        fileIn.close();
                                                        
                                                        fileOne.delete();

                                                        FileOutputStream fileOut = new FileOutputStream(new File(Globals.p2pFileSharesPath+"\\won_drive\\"+fileOne.getName()));
                                                        fileOut.write(filecontent);
                                                        fileOut.flush();
                                                        fileOut.close();
                                                    }

                                                    for(File fileLost : filesLost) {
                                                        if(!new File(Globals.p2pFileSharesPath+"\\lost_drive").exists())
                                                            new File(Globals.p2pFileSharesPath+"\\lost_drive").mkdir();

                                                        System.out.println("Resource Allocation : Lost File : "+fileLost.getName());                                            

                                                        FileInputStream fileIn = new FileInputStream(fileLost);
                                                        byte[] filecontent = new byte[fileIn.available()];

                                                        fileIn.read(filecontent);
                                                        fileIn.close();
                                                        
                                                        fileLost.delete();

                                                        FileOutputStream fileOut = new FileOutputStream(new File(Globals.p2pFileSharesPath+"\\lost_drive\\"+fileLost.getName()));
                                                        fileOut.write(filecontent);
                                                        fileOut.flush();
                                                        fileOut.close();
                                                    }

                                                } catch(Exception e) {

                                                }                                                
                                                
                                                if(node.equals(clientId)) {
                                                    callback.updateFile(file,path,node, null);
                                                } else {
                                                    callback.updateGetFile(node,file,path);
                                                }
                                            }
                                        }
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                                } finally {
                                }
                            }
                        });
                        
                        clientThread.start();
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        
        serverThread.start();
    }


}
