/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maximizingp2pfilesharingclient.client;

import java.util.List;

/**
 *
 * @author Master PC
 */
public interface ClientCallback {
    public void updateFile(String fileName,byte[] fileContent);

    public void updateFiles(List<String> files);

    public void updateNodes(List<String> files);

    public void updateReply(String node, String file, String path);

    public void updateFile(String fileName, String node, String path, String hex);

    public void updateRequest(String node, String path, String file);

    public void updateGetFile(String node, String file, String path);

    
}
