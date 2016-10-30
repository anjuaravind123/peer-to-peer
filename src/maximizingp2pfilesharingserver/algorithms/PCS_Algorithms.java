/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maximizingp2pfilesharingserver.algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import maximizingp2pfilesharingclient.Globals;
import maximizingp2pfilesharingserver.server.ServerThread;

/**
 *
 * @author Master PC
 */
public class PCS_Algorithms {
    
    private static Hashtable<String,Double> fileaccessFrequencies = new Hashtable<String, Double>();
    
    
    private static List<File> getFiles(String file) {
        File[] files = new File(Globals.p2pFileSharesPath).listFiles();
        File[] replicatesFiles = new File(Globals.p2pFileSharesPath+"\\replicates").listFiles();
        File[] wonFiles = new File(Globals.p2pFileSharesPath+"\\won_drive").listFiles();
        File[] lostFiles = new File(Globals.p2pFileSharesPath+"\\lost_drive").listFiles();
        
        List<File> files2 = new ArrayList<File>();
        
        if(files!=null)
        for(File file1 : files) {
            if(!file1.isDirectory()) {
                if(file1.getName().equals(file)) {
                    files2.add(0, file1);
                } else {
                    files2.add(file1);
                }
            }
        }
        
        if(replicatesFiles!=null)
        for(File file1 : replicatesFiles) {
            if(!file1.isDirectory() && !file1.getName().equals(file)) {
                files2.add(file1);
            }
        }

        if(wonFiles!=null)
        for(File file1 : wonFiles) {
            if(!file1.isDirectory() && !file1.getName().equals(file)) {
                files2.add(file1);
            }
        }

        if(lostFiles!=null)
        for(File file1 : lostFiles) {
            if(!file1.isDirectory() && !file1.getName().equals(file)) {
                files2.add(file1);
            }
        }
        
        
        return files2;
    }
    
    public static Object[] PCS_Algorithm(ServerThread serverThread,String fileName,String from,String to) {
       long nCount = 0;
       
       List<File> filesInNode = getFiles(fileName);
       
       int j = filesInNode.size();
       
       double freq = 0.0d;
       
       if(fileaccessFrequencies.containsKey(fileName))
           freq = fileaccessFrequencies.get(fileName);
       
       fileaccessFrequencies.put(fileName, freq+1.0d);
       
       return competeForFile(serverThread, filesInNode.get(0), filesInNode, from, to, j);
    }
    
    public static Object[] competeForFile(ServerThread serverThread,File file,List<File> filesInNode,String from,String to,int j) {
        long mRemainingMem = 0;
        
        List<File> filesOne = new ArrayList<File>();
        List<File> filesLost = new ArrayList<File>();
        
        filesOne.add(file);
        
        while(mRemainingMem<file.length()) {
            long nSum=0,nTotal=0,nRandom=0,fFile = 0;
            
            for(File file2 : filesInNode) {
                nTotal = nTotal + file2.length();
            }
            
            nRandom = (int) (Math.random() * nTotal) % nTotal;
            
            int index = 0;
            
            for(File file2 : filesInNode) {
                nSum = nSum + file2.length();
                
                if((nSum+(fileaccessFrequencies.containsKey(file2.getName())?fileaccessFrequencies.get(file2.getName()):0.0d))>nRandom) {
                    if(!filesOne.contains(file2))
                       filesOne.add(file2);
                    break;
                } else {
                    if(!filesLost.contains(file2))
                       filesLost.add(file2);
                }
                
                index++;
            }
            
            if(index==j) {

                try {
                    if(filesOne.size()==0) {
                        filesOne.add(filesInNode.get(0));
                    }

                    if(filesLost.size()==0) {
                        if(!filesOne.contains(filesInNode.get(0)))
                           filesLost.add(filesInNode.get(0));
                    }
                } catch(Exception e) {
                    
                }
                
                long max = 0;
                File maxFile = null;
                
                for(File file2 : filesLost) {
                    if(file2.length()>max) {
                        max = file2.length();
                        maxFile = file2;
                    }
                }

                if(maxFile!=null) {
                    filesLost.remove(maxFile);
                    filesOne.add(maxFile);
                }
                
                File lostFile = null;
                
                for(File file2 : filesOne) {
                    if(file2.length()<file.length()) {
                        lostFile = file2;
                    }
                }

                if(lostFile!=null) {
                    filesOne.remove(lostFile);
                    filesLost.add(lostFile);
                }
                
                return new Object[] { filesOne , filesLost };
            } else {
                
                try {
                    if(filesOne.size()==0) {
                        filesOne.add(filesInNode.get(0));
                    }

                    if(filesLost.size()==0) {
                        if(!filesOne.contains(filesInNode.get(0)))
                           filesLost.add(filesInNode.get(0));
                    }
                } catch(Exception e) {
                    
                }
                
                long max = 0;
                File maxFile = null;
                
                for(File file2 : filesLost) {
                    if(file2.length()>max) {
                        max = file2.length();
                        maxFile = file2;
                    }
                }

                if(maxFile!=null) {
                    filesLost.remove(maxFile);
                    filesOne.add(maxFile);
                }
                
                File lostFile = null;
                
                for(File file2 : filesOne) {
                    if(file2.length()<file.length()) {
                        lostFile = file2;
                    }
                }

                if(lostFile!=null) {
                    filesOne.remove(lostFile);
                    filesLost.add(lostFile);
                }
                
                return new Object[] { filesOne , filesLost };
            }
        }
        
        return new Object[] { };
    }
}
