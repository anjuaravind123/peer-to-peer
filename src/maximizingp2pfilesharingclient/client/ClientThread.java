/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maximizingp2pfilesharingclient.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import maximizingp2pfilesharingclient.Globals;
import maximizingp2pfilesharingclient.HexDecoder;
import maximizingp2pfilesharingserver.algorithms.PCS_Algorithms;
import maximizingp2pfilesharingserver.server.ServerThread;

/**
 *
 * @author Master PC
 */
public class ClientThread {
    
    private String clientId = "";
    private String clientClientID = "";
    
    private Socket client;
    private ClientCallback callback;
    
    private String ipAddress,port;

    public ClientThread(String clientId,String clientClientID,String ipAddress,String port) {
        this.clientId = clientId;
        this.clientClientID = clientClientID;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getClientClientID() {
        return clientClientID;
    }

    public void setClientClientID(String clientClientID) {
        this.clientClientID = clientClientID;
    }
    
    public ClientCallback getCallback() {
        return callback;
    }

    public void setCallback(ClientCallback callback) {
        this.callback = callback;
    }
    
    public void replicateFile(String fileName,byte[] fileContent) {
        PrintWriter pw1 = null;
        try {
            String hex = HexDecoder.encode(fileContent);
            
            pw1 = new PrintWriter(client.getOutputStream());
            
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
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //pw1.close();
        }
    }
    
    public void getFiles() {
        PrintWriter pw1 = null;
        
        try {
            pw1 = new PrintWriter(client.getOutputStream());
            
            pw1.println("[getallfiles]");
            pw1.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //pw1.close();
        }
    }
    
    public void getNodes() {
        PrintWriter pw1 = null;
        
        try {
            pw1 = new PrintWriter(client.getOutputStream());
            
            pw1.println("[getnodes]");
            pw1.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //pw1.close();
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    private boolean isRunning = false;

    public boolean isIsRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    
    public void start() {
        Thread clientThread = new Thread(new Runnable() {

            @Override
            public void run() {

                //if(clientId.equals(clientClientID)) {
                  //  setIsRunning(false);
                   // return;
                //} else
                   // setIsRunning(true);
                
                try {
                    final Socket client = new Socket(ipAddress,Integer.parseInt(port));
                    
                    System.out.println("Node Connected to Server.... [Node : "+clientId+"] --> [Node : "+clientClientID+"]");
                    
                    ClientThread.this.client = client;
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter pw = new PrintWriter(client.getOutputStream());
                    
                    String line = "";
                    
                    pw.println("[connect]:"+clientId+","+Globals.clientListeningPort);
                    pw.flush();
                    
                    while((line=reader.readLine())!=null) {
                        System.out.println("Node Thread: " +line);
                        
                        if(line.startsWith("[file]")) {
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
                                if(callback!=null) {
                                    callback.updateFile(fileName,node, path, hex);
                                }
                            }
                            
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
                            
                        }  else if(line.startsWith("[getfile]")) {
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
                                        callback.updateFile(file,node,path, null);
                                    } else {
                                        callback.updateGetFile(node,file,path);
                                    }
                                }
                        } else if(line.startsWith("[updatefilelist]")) {
               
                            List<String> files = new ArrayList<String>();
                            
                            while((line=reader.readLine()).equals("---end of files---")) {
                                files.add(line);
                            }
                            
                            if(callback!=null)
                                callback.updateFiles(files);
                            
                        } else if(line.startsWith("[updatenodeslist]")) {
               
                            List<String> files = new ArrayList<String>();
                            
                            while((line=reader.readLine()).equals("---end of nodes---")) {
                                files.add(line);
                            }
                            
                            if(callback!=null)
                                callback.updateNodes(files);
                        } else if(line.startsWith("[broadcastreply]")) {
                            String node = line.split(":")[1].split(",")[0];
                            String file = line.split(":")[1].split(",")[1];
                            String path = line.split(":")[1].split(",")[2];

                            if(callback!=null)
                                callback.updateReply(node,file,path);
                        } else if(line.startsWith("[broadcastrequest]")) {
                            String node = line.split(":")[1].split(",")[0];
                            String file = line.split(":")[1].split(",")[1];
                            String path = line.split(":")[1].split(",")[2];

                            if(callback!=null)
                                callback.updateRequest(node,path,file);
                        }
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        clientThread.start();
    }

    public void broadcastRequest(String selectedFile,String path,ServerThread server) {
            
            try {
                PrintWriter pw = new PrintWriter(this.client.getOutputStream());
                String path1 = clientId+";";
                pw.println("[broadcastrequest]:"+this.clientClientID+","+selectedFile+","+path1);
                pw.flush();
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                //pw.close();
            }
            //});
    }
    
    public void broadcastReply(String node,String path,String selectedFile) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(this.client.getOutputStream());
            
            pw.println("[broadcastreply]:"+node+","+selectedFile+","+path);
            pw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //pw.close();
        }
    }
}
