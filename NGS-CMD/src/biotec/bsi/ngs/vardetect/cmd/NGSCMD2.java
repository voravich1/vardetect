/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.cmd;

import biotec.bsi.ngs.vardetect.alignment.AlignerFactory;
import biotec.bsi.ngs.vardetect.core.Aligner;
import biotec.bsi.ngs.vardetect.core.AlignmentResultRead;
import biotec.bsi.ngs.vardetect.core.Annotation;
import biotec.bsi.ngs.vardetect.core.InputSequence;
import biotec.bsi.ngs.vardetect.core.ReferenceAnnotation;
import biotec.bsi.ngs.vardetect.core.ReferenceSequence;
import biotec.bsi.ngs.vardetect.core.ShortgunSequence;
import biotec.bsi.ngs.vardetect.core.util.SequenceUtil;
import biotec.bsi.ngs.vardetect.core.util.SimulatorUtil_WholeGene;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author Aon
 * Test bed 
 * 
 */
public class NGSCMD2 {
    
    public static void main(String args[]) throws FileNotFoundException, IOException{
//        String refPath = args[0];
//        ReferenceSequence ref = SequenceUtil.getReferenceSequence(refPath); //runFile hg19.fa
//        
//        InputSequence tempInSS = new InputSequence();
//        int numMer = 18;
//        String s = "CTCTATACTATATAGTATAGAGTATATTATTATATACTCTATATAATATAACATAGAGTATATAATAATATACTCTATATTATATTATATAGAATATATA";
//        
//        for(int i=0;i<(s.length()-numMer)+1;i++){                                  // (Windowing with one stepping) for loop over String sequence which has limit round at (string length - mer length) + one [maximum possible mer sequence]
//                int index = i;
//                String sub = s.substring(i, i+numMer);                                 // cut String sequence into sub string sequence (mer length long) 
//                //System.out.println("check sub length"+sub.length());
//                long m = SequenceUtil.encodeMer(sub, numMer);
//                
//                System.out.println("index: "+i+" sequence: "+sub+" sequence code: "+m);
//        }
//        
//        ShortgunSequence inSS = new ShortgunSequence(s);
//        inSS.addReadName("err01");
//        tempInSS.addRead(inSS);

//        tempInSS = SimulatorUtil_WholeGene.simulateComplexWholeGeneRandomMixed(ref, 200, 100, 10, 10000, 9);
//        System.out.println("done");
                    
        //Aligner aligner = AlignerFactory.getAligner();          // Will link to BinaryAligner

        //AlignmentResultRead align = aligner.alignV3(ref, tempInSS);  // function align is located in binary aligner
        
        
        
        
        /**
         * Test Zone 2
         */
//        String refPath = "/Volumes/PromisePegasus/worawich/VMdev/dataScieneToolBox/projects/NGS/hg19test/hg19.fa";
//        ReferenceSequence ref = SequenceUtil.getReferenceSequence(refPath);

/**
 * Use for generate short read sample (manual generate short read)
 */
//        String filename = "err2Read68SS3";       
//        String path = "/Volumes/PromisePegasus/worawich/Download_dataset/err/";
//        
//        String seq = "CGCCTGCCTCGGCCTCCCAAAGTGCTGGGATTACAGGCATGAGCCACCGCGCCCGGCCAAGAGTTCCTTTCTTAAATAAGAGGACAGAGATAATAAAATC";
//        
//        SequenceUtil.createShortReadFromLongSequence(seq, 100, path+filename);

        
        String[] seq = {"taaccctaaccctaac","gtaccctcagccggcc","GCAAAGGCGCGCCGCG","CTCAGAGCCCAGGCCA","GGGCAGAGCCGCAGGG","TGGCAGAGCCGCAGGG","TGGCGGAGCCGCAGGG","TGGCGGAGCTGCAGGG","TGGCGGAGCTGCAGCA"};
        
        for(int i=0;i<seq.length;i++){
            long code = SequenceUtil.encodeMer(seq[i], 16);
            int intCode = (int)code;
            
            System.out.println("Seq " + seq[i] + " => (long) " + code + " ||  (int) " + intCode);
        }
        
        
    }
}
