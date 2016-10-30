/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maximizingp2pfilesharingserver.server;

/**
 *
 * @author Master PC
 */
public interface ServerCallback {
    public void updateFile(String fileName,byte[] fileContent);
    
    public void updateReply(String node,String path, String file);

    public void updateRequest(String node,String path, String file);

    public void updateFile(String fileName,String path, String toNode, byte[] fileContent);

    public void updateGetFile(String node, String file, String path);

    public void updateConnect(String ipAddress, String clientId, String clientPort);
}
