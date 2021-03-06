/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.cmd;

import biotec.bsi.ngs.vardetect.core.AlignmentResultRead;
import biotec.bsi.ngs.vardetect.core.util.Clustering;
import biotec.bsi.ngs.vardetect.core.util.SequenceUtil;
import java.io.IOException;

/**
 *
 * @author worawich
 */
public class RunPostProcess {
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        String savePath = args[0];
        String inputFileName = args[1];
        String outputFileName = args[2];
        int readLenght = Integer.parseInt(args[3]);
        int merLenght = Integer.parseInt(args[4]);
        int amountOfPart = Integer.parseInt(args[5]);
        
    
        for(int i=1;i<=amountOfPart;i++){
            
            AlignmentResultRead readAlign = SequenceUtil.readAlignmentReportV2(savePath+inputFileName+i+".txt",merLenght);
            System.out.println("Begin create color array");
            Clustering.createColorArrayV2(readAlign, 18);        
            System.out.println("Done create color array");
            readAlign.writeSortedCutColorResultToPathInFormatForLinuxSort(savePath, outputFileName, "txt","gy",83);
            readAlign = null;
            System.gc();

       }
    }
}
