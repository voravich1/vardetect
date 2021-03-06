/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.cmd;

import biotec.bsi.ngs.vardetect.core.AlignmentResultRead;
import biotec.bsi.ngs.vardetect.core.util.Clustering;
import biotec.bsi.ngs.vardetect.core.util.SequenceUtil;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author worawich
 */
public class CreateBedReport {
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        String filename = "/Volumes/PromisePegasus/worawich/Download_dataset/crab_transscriptome/early_premolt/crap_earlypremolt_alnCR_8_5_alnRes_part";
        String indexFile = "/Volumes/PromisePegasus/worawich/Download_dataset/Micro_RNA/drosophila/d.melanogaster/dm6_filter.index";            // use for traceback to natural chromosome name
        String sampleFile = "/Volumes/PromisePegasus/worawich/Download_dataset/Micro_RNA/NGS_result_050417/O3_S3_L001_R2_001.fa";
        String saveFileType = "txt";
        int numPart = 1;
//        int readLength = 24;
        int merLength = 18;
        int maxFullMatch = 100; // it's percentage value
        int propotion = 1000000;
//        String path = "/Volumes/PromisePegasus/worawich/Download_dataset/SimulateData/simLongRead/";
        
        
        File mainFile = new File(filename);
        String path = mainFile.getParent();
        String saveFileName = mainFile.getName().split("part")[0]+"forLinuxSort";
        String saveSampleFileName = mainFile.getName().split("part")[0]+"Sample";
//        File pathVar = new File(args[1]);
//        filename = pathVar.getName().split("\\.")[0];
//        path = pathVar.getParent()+"/";
//        saveFileName = pathVar.getPath().split("\\.")[0];
           
        for(int i=1;i<=numPart;i++){
            
//            AlignmentResultRead readAlign = SequenceUtil.readAlignmentReportV2(filename+i+".txt",merLength);
            AlignmentResultRead readAlign = SequenceUtil.readBinaryAlignmentReportV3(filename+i+".bin",merLength);
            
            /**
             * Generate Bed graph  
             */
            readAlign.countAlignMatch();
            readAlign.writeMatchCountReport(filename+i);              // write bed graph file format for overall vistualize
//            readAlign.writeMatchCountReport(filename+i,indexFile);              // write bed graph file format for overall vistualize + index file
            /***************************************/

            readAlign = null;
            System.gc();
        }
    }
}
