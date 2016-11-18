/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.cmd;

import biotec.bsi.ngs.vardetect.core.ClusterGroup;
import biotec.bsi.ngs.vardetect.core.util.Clustering;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author worawich
 */
public class TestCluster {
    public static void main(String[] args) throws IOException{
        String FileName = "hg19_3661_mul_alignmentResult_th2_LinuxSortedV3.txt";
        String path = "/Users/worawich/VMdev/dataScieneToolBox/projects/NGS/";
        System.out.println("start cluster");
        ArrayList<ClusterGroup> cg = Clustering.clusterFromFile(path+FileName, 500, 3);
        System.out.println("Done");
        
    } 
    
}
