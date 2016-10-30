/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maximizingp2pfilesharingclient;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Master PC
 */
public class Globals {
    public static List<String> lastResult = new ArrayList<String>();
    public static boolean lastResultCompleted = false;
    
    public static String p2pFileSharesPath = "";
    public static String clientListeningPort = "";
    
    public static int broadcastRequestCount = -1;
}
