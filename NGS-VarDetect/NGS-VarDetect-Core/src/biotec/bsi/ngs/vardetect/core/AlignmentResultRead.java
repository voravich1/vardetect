/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.core;

import biotec.bsi.ngs.vardetect.core.util.SequenceUtil;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 *
 * @author worawich
 */
public class AlignmentResultRead {
    private ArrayList<ShortgunSequence> shrtRead;
    private ArrayList<ShortgunSequence> shrtReadCut;
    private ArrayList<ClusterGroup> clusterResult;
    private long[] allClusterCodeSorted;
    private long[] allClusterCode;
    private double[][] distanceTable;
    private static long mask = 268435455;
    private static long mask_Count = 4380866641920L;    // Do & operation to get count number from value contain in alignmentResultMap
    //private static long mask_Chr =
    private static long mask_chrStrandAln = 17179869183L;   // Do & operation to get aligncode compose of chr|strand|alignposition from value contain in alignmentResultMap
    private static long mask_chrIdxStrandAln = 4398046511103L;      //  Do & operation to get aligncode compose of chr|Idx|strand|alignposition from value contain in alignmentResultMap
    private static long mask32bit = 4294967295L;
    private static int mask5bit = 31;
    private ClusterGroup group;
    private Map<String,ArrayList<Long>> alignmentResultMap;
    private Map<String,ArrayList<Long>> alignmentResultMap1;
    private Map<String,ArrayList<Long>> alignmentResultMap2;
    private Map<String,ArrayList<Long>> alignmentSortedCutResultMap;
    private Map<Long,long[]> countResultSortedCut;
    private Map<String,Integer> readLenList;
    private Map<String,Map<Long,ArrayList<Long>>> newAlignmentResultMap;    // Hash Map that store alignment result togather with 
    private ArrayList<String> unMapList;                    // Store readname of unmap read (use in local alignment part)
    private ArrayList<String> mapList;                      // Store readname of map read (use in local alignment part)
    private Map<Long,Integer> countMap;                     // Store count match of each chromosome and position. Key is chr|Position and Value is count number of that specific chr and position
    private Map<Long,ArrayList<Integer>> checkDupMap;                  // store Key = chr|position and Value = iniPosition on read. This map has been used for check duplication read has same ini position on read that match same chr same position 
    private ArrayList<AlignmentResult> AlignmentResultList;
    private ArrayList<AlignmentResult> listAlignmentResult;
    int numFullMatch;
    
    public AlignmentResultRead(){
        
       this.shrtRead = new ArrayList();
       this.group = new ClusterGroup();
       this.clusterResult = new ArrayList();
       this.alignmentResultMap = new HashMap();
       this.alignmentSortedCutResultMap = new LinkedHashMap();
       this.mask = 268435455;
       this.mask_Count = 4380866641920L;    // Do & operation to get count number from value contain in alignmentResultMap
       this.mask_chrStrandAln = 17179869183L;   // Do & operation to get aligncode compose of chr|strand|alignposition from value contain in alignmentResultMap
       this.mask_chrIdxStrandAln = 4398046511103L;      //  Do & operation to get aligncode compose of chr|Idx|strand|alignposition from value contain in alignmentResultMap
       this.unMapList = new ArrayList();
       this.mapList = new ArrayList();
       this.AlignmentResultList = new ArrayList();
       this.numFullMatch = 0;
    }
    
    public void addResult(ShortgunSequence inRead){
        this.shrtRead.add(inRead);
    }
    
    public void addMapResult(Map inMap){
        this.alignmentResultMap = inMap; 
    }
    
    public void addMapResult1(Map inMap){
        this.alignmentResultMap1 = inMap; 
    }
    
    public void addMapResult2(Map inMap){
        this.alignmentResultMap2 = inMap; 
    }
    
    public void addReadLenList(Map<String,Integer> inMap){
        this.readLenList = inMap; 
    }
    
    public void addGroupReult(ArrayList<ClusterGroup> inGroupResult){
        this.clusterResult = inGroupResult;
    }
    
    public ArrayList<ShortgunSequence> getResult(){
    
        return this.shrtRead;
    }
    
//    public void createdistancetable(){
//        int sizeMax = 2;
//        for(int i=0;i<this.shrtRead.size();i++){
//            ShortgunSequence dummyMainSS = shrtRead.get(i);
//            int sizeRes = dummyMainSS.countResultSortedCut.size();
//            if (sizeRes < sizeMax){
//                for (int k=0;k<sizeRes;k++){
//                    long dummyPos = (long)dummyMainSS.getListPosMatch().get(k);
//                    
//                }
//            }else if (sizeRes >= sizeMax){
//                for (int k=0;k<sizeRes;k++){
//                    long dummyPos = (long)dummyMainSS.getListPosMatch().get(k);
//                }
//            }
//            
//            
//            
//            for (int j=0;j<this.shrtRead.size();j++){
//                ShortgunSequence dummySubSS = shrtRead.get(j);
//                
//                if(dummyMainSS.getReadName() != dummySubSS.getReadName()){
//                    ArrayList dummyCheckSS = dummyMainSS.getListChrMatch();
//                    dummyCheckSS.retainAll(shrtRead);
//                    dummyMainSS.getListChrMatch().indexOf(dummyCheckSS);
//                    
//                    //dummyMainSS.getReadName()
//                    //dummyMainSS.getListChrMatch().reta
//                            
//                }
//            }
//        }
//        
//    }
    
    public double[][] getDistanceTable(){
        return this.distanceTable;
    }
    
    public ArrayList<String> getUnMapList(){
        return this.unMapList;
    }
    
    public ArrayList<String> getMapList(){
        return this.mapList;
    }
    
    public void enableReconstruct(){
        for(int i =0;i<shrtRead.size();i++){
            shrtRead.get(i).detectStrandPattern();
        }
    }
    
    public void createGroupCharacteristic(double threshold){
        /* Create grouping characteristic of each shortgun sequence(store in shrtRead) */
        for(int i=0;i<this.shrtRead.size();i++){
            shrtRead.get(i).createInGroupOutGroup(threshold);
        }
    }
    
    public void countSortedCutMapResult(long inTh){
        
        long threshold = inTh;
        Set readSet = this.alignmentResultMap.keySet();
        Iterator readIter = readSet.iterator();
        while(readIter.hasNext()){
            String readName = (String) readIter.next();
            ArrayList<Long> countChrStrandPosList = (ArrayList<Long>) this.alignmentResultMap.get(readName);
            for(int i=0;i<countChrStrandPosList.size();i++){
                long countChrStrandPos = countChrStrandPosList.get(i);
                long count = countChrStrandPos&mask_Count;
                long chrStrandPos = countChrStrandPos&mask_chrStrandAln;
                
                
            }
        }
        
    }
    
    
    
    public void calculateEuclidientdistance(){
        
        this.distanceTable = new double[shrtRead.size()][shrtRead.size()]; // Prelocate 2D double array for store distance value (size = [number of shortgun read] x [number of shortgun read])
        for(int i =0;i<shrtRead.size();i++){
            double[] distanceVector = new double[shrtRead.size()]; // distanceVector is a 1D double array stand for store distance value of main shortgun sequence clustervector and all other shortgun sequence clustervector
            ShortgunSequence dummyMainSS = shrtRead.get(i);
            for(int j=0;j<shrtRead.size();j++){
                ShortgunSequence dummySubSS = shrtRead.get(j);
                
                /*  distance function will calculate the distance vector of two in put vector               */
                /*  We will pass the clusterVector as a input for distance function                         */
                /*  Method getClusterVector will create the vector of align position of shortgun sequence   */
                /*  Ex: read0SS0 has align on chr1 pos: 1000 and chr21 pos: 200                             */
                /*  the clusterVector will look like this                                                   */
                /*  [1000,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,200,0,0,0]                                    */
                /*  as you can see the vector has 24 element which has value at first and 21 element follow by the chr that align */
                /*  In this Implementtation we selected only top two alignment result to create cluster vector */ 
                
                distanceVector[j] = distance(dummyMainSS.getClusterVector(),dummySubSS.getClusterVector()); // this will return single value of distance between two vector 
                this.distanceTable[i][j] = distanceVector[j];
//                System.out.println();
//                System.out.println("DummyMainSS/"+dummyMainSS.getReadName()+" pair with DummySubSS/"+dummySubSS.getReadName()+" : distanceVector["+j+"] = "+distanceVector[j]);
//                System.out.println();
            }
//            System.out.println("Check before add to "+dummyMainSS.getReadName());
//            for(int a=0;a<distanceVector.length;a++){
////                System.out.print("\t"+distanceVector[a]);
//            }
//            System.out.println();
            dummyMainSS.addDistanceVector(distanceVector); // store distance value in to main shrtgun sequence
//            System.out.println("Check after add to "+dummyMainSS.getReadName());
//            for(int a=0;a<distanceVector.length;a++){
////                System.out.print("\t"+distanceVector[a]);
//            }
//            System.out.println();
            
        }
//        for(int i =0;i<shrtRead.size();i++){
//            ShortgunSequence dummyMainSS = shrtRead.get(i);
//            System.out.println("************ ReadName:"+dummyMainSS.getReadName()+" check saved vector distance ************");
//            for (int check =0;check<shrtRead.size();check++){
//                System.out.print("\t"+dummyMainSS.getDistanceVector()[check]);   
//            }
//            System.out.println();
//        }
    }
    
    public double distance(long[] a, long[] b){
        double diff_square_sum = 0.0;
        for (int i = 0; i<a.length; i++){
            diff_square_sum += (a[i]-b[i]) * (a[i]-b[i]);
        }
        return Math.sqrt(diff_square_sum);
    }
    
//    public void createGroupingResult(){
//        long dummyCode = 0;
//        long oldDummyCode = 0;
//        
//        this.group = new ClusterGroup();
//        for(int i = 0;i<this.allClusterCodeSorted.length;i++){
//            dummyCode = this.allClusterCodeSorted[i];
//            for(int j =0;j<this.shrtRead.size();j++){
//                ShortgunSequence dummySS = shrtRead.get(j);
//                if(dummySS.getClusterCode() == dummyCode){
//                    if(i == 0){
//                        System.out.println(" Check : Do adding in first group (First time) i = " + i+ " : j =  "+j );
//                        this.group.addShortgunRead(dummySS);
//                        oldDummyCode = dummyCode;
//                    }else if(i!=0 && Math.abs(dummyCode-oldDummyCode)<=100){
//                        System.out.println(" Check : Do adding in group : i = " + i+ " : j =  "+j);
//                        this.group.addShortgunRead(dummySS);
//                        oldDummyCode = dummyCode;
//                    }else if(i!=0 && Math.abs(dummyCode-oldDummyCode)>100){
//                        this.clusterResult.add(this.group); // adding to array before renew it
//                        System.out.println(" Check : Do create new group and add to new group : i = " + i+ " : j =  "+j);
//                        
//                        this.group = new ClusterGroup();
//                        this.group.addShortgunRead(dummySS);
//                        oldDummyCode = dummyCode;
//                    }
//                    
//                    System.out.println(dummyCode + "\t" + dummySS.getReadName());
//                }
//            }
//        }
//        this.clusterResult.add(this.group); // adding to array (for last group)
//    }
    
//    public void createAllClusterCode(){
//        this.allClusterCode = new long[this.shrtRead.size()];
//        for(int i =0;i<this.shrtRead.size();i++){
//            long dummyCode = shrtRead.get(i).getClusterCode();
//            this.allClusterCode[i] = dummyCode;
//        }
//    }
    
//    public void createAllClusterCodeSorted(){
//        this.allClusterCodeSorted = new long[this.shrtRead.size()];
//        for(int i =0;i<this.shrtRead.size();i++){
//            long dummyCode = shrtRead.get(i).getClusterCode();
//            this.allClusterCodeSorted[i] = dummyCode;
//        }
//        Arrays.sort(this.allClusterCodeSorted);
//    }
    
//    public ArrayList<ClusterGroup> getclusterResult(){
//        
//        return this.clusterResult;
//    }
    
//    public long[] getAllClusterCode(){
//        
//        //Arrays.sort(this.allClusterCode);
//        return this.allClusterCode;
//    }
    
//    public long[] getAllClusterCodeSorted(){
//        
//        return this.allClusterCodeSorted;
//    }
    
    public void writeSortedResultToPath(String path, String fa) throws FileNotFoundException, IOException {

       
        PrintStream ps = new PrintStream(path+"_AlignSortedResult."+ fa);           // Create file object
        
        
        for (int i=0;i<this.shrtRead.size();i++){           // Loop each ShortgunSequence
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
            
            Map<Long,long[]> countMap =  dummySS.getAlignmentCountSorted();         // get alignmentCountSorted (HashMap of sorted alignment result Map<positionCode,numCountPlusColor>)
            ps.println(">Alignment result of "+ dummySS.getReadName());
            ps.printf("%-35s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","Strand","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
            Set allPos = countMap.keySet();
            Iterator iterPos = allPos.iterator();
            while(iterPos.hasNext()){                                               // loop all align positionCode (contain in HashMap)
                long positionCode = (long)iterPos.next();
                long alignPos = positionCode&mask;                                  // And with 28bit binary to get position
                long chrNumber = positionCode>>29;                                  // Shift left 29 bit because positionCode have this structure [chr|strand|position] in order to get chromosome number
                long[] numCountPlusColor = countMap.get(positionCode);              // get array long which contain number of count and other color code and color code intensity
                long numCount = numCountPlusColor[0];
                long red = numCountPlusColor[1];
                long yellow = numCountPlusColor[2];
                long orange = numCountPlusColor[3];
                long green = numCountPlusColor[4];
                long redInt = numCountPlusColor[5];
                long yellowInt = numCountPlusColor[6];
                long orangeInt = numCountPlusColor[7];
                long greenInt = numCountPlusColor[8];
                
                String strandNot = "no";                                            // Identify the strand type of this align Position
                if(((positionCode>>28)&1) == 1){
                    strandNot = "+";
                }else if(((positionCode>>28)&1) == 0){
                    strandNot = "-";
                }   
                
                
                ps.format("Chr %2d : Position %12d|\t%8s\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,strandNot,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt); // print to file by this format
            }
            ps.println();
        }
    }

    public void writeUnSortedResultToPath(String path, String fa) throws FileNotFoundException, IOException {

       
        PrintStream ps = new PrintStream(path+"_AlignUnSortedResult."+ fa);     // Create file object
        
        
        for (int i=0;i<this.shrtRead.size();i++){                               // Loop each ShortgunSequence
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
            
            Map<Long,long[]> countMap =  dummySS.getAlignmentCount();           // get alignmentCount (HashMap of unsorted alignment result Map<positionCode,numCountPlusColor>)
            ps.println(">Alignment result of "+ dummySS.getReadName());
            ps.printf("%-35s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","Strand","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
            Set allPos = countMap.keySet();
            Iterator iterPos = allPos.iterator();
            while(iterPos.hasNext()){                                           // loop all align positionCode (contain in HashMap)
                long positionCode = (long)iterPos.next();
                long alignPos = positionCode&mask;                              // And with 28bit binary to get position
                long chrNumber = positionCode>>29;                              // Shift left 29 bit because positionCode have this structure [chr|strand|position] in order to get chromosome number
                long[] numCountPlusColor = countMap.get(positionCode);          // get array long which contain number of count and other color code and color code intensity
                long numCount = numCountPlusColor[0];
                long red = numCountPlusColor[1];
                long yellow = numCountPlusColor[2];
                long orange = numCountPlusColor[3];
                long green = numCountPlusColor[4];
                long redInt = numCountPlusColor[5];
                long yellowInt = numCountPlusColor[6];
                long orangeInt = numCountPlusColor[7];
                long greenInt = numCountPlusColor[8];
                
                String strandNot = "no";                                        // Identify the strand type of this align Position
                if(((positionCode>>28)&1) == 1){
                    strandNot = "+";
                }else if(((positionCode>>28)&1) == 0){
                    strandNot = "-";
                }   
                
                ps.format("Chr %2d : Position %12d|\t%8s\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,strandNot,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);    // print to file by this format
            }
            ps.println();
        }
    }

    public void writeSortedCutResultToPath(String path, String fa, int threshold) throws FileNotFoundException, IOException {

        /* Must specify threshold for cut result (The result that less than threshold will be cut out)*/
        PrintStream ps = new PrintStream(path+"_AlignSortedCutResult."+ fa);            // Create file object
        
        
        for (int i=0;i<this.shrtRead.size();i++){                                       // Loop each ShortgunSequence
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
            
            Map<Long,long[]> countMap =  dummySS.getAlignmentCountSortedCut(threshold); // get alignmentCountSortedCut (HashMap of sorted and cut with specific threshold of alignment result Map<positionCode,numCountPlusColor>)
            
//            ps.printf("%-30s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
            Set allPos = countMap.keySet();
            Iterator iterPos = allPos.iterator();
            if(allPos.isEmpty() == false){
                ps.println(">Alignment result of "+ dummySS.getReadName());
                ps.printf("%-35s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","Strand","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
            }
            
//            Set allPos = countMap.keySet();
//            Iterator iterPos = allPos.iterator();
            while(iterPos.hasNext()){                                                   // loop all align positionCode (contain in HashMap)
                long positionCode = (long)iterPos.next();
                long alignPos = positionCode&mask;                                      // And with 28bit binary to get position
                long chrNumber = positionCode>>29;                                      // Shift left 29 bit because positionCode have this structure [chr|strand|position] in order to get chromosome number
                long[] numCountPlusColor = countMap.get(positionCode);                  // get array long which contain number of count and other color code and color code intensity
                long numCount = numCountPlusColor[0];
                long red = numCountPlusColor[1];
                long yellow = numCountPlusColor[2];
                long orange = numCountPlusColor[3];
                long green = numCountPlusColor[4];
                long redInt = numCountPlusColor[5];
                long yellowInt = numCountPlusColor[6];
                long orangeInt = numCountPlusColor[7];
                long greenInt = numCountPlusColor[8];
                
                String strandNot = "no";                                                // Identify the strand type of this align Position
                if(((positionCode>>28)&1) == 1){
                    strandNot = "+";
                }else if(((positionCode>>28)&1) == 0){
                    strandNot = "-";
                }   
                
//                ps.format("Chr %d : Position %d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
                ps.format("Chr %2d : Position %12d|\t%8s\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,strandNot,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
            }
            if(allPos.isEmpty() == false){
                ps.println();
            }
        }
    }

    public void writeDistanceTableToPath(String path, String fa) throws FileNotFoundException, IOException {

       /* Must specify threshold for cut result (The result that less than threshold will be cut out)*/
        PrintStream ps = new PrintStream(path+"_DistanceTable."+ fa);           // Create file object
        
        ps.println("Distance Table");
        ps.format("Reads Name");
        for (int i=0;i<this.shrtRead.size();i++){                               // Loop each ShortgunSequence (Loop for create title of first line)
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
 
            ps.format("\t%10s",dummySS.getReadName());
        }
        ps.println();
        for (int i=0;i<this.shrtRead.size();i++){                               // Loop each ShortgunSequence (First Loop for create read name title for begin of line)   
            ShortgunSequence dummySS = this.shrtRead.get(i);
            ps.print("Name: "+dummySS.getReadName());
            
            for(int j=0;j<this.shrtRead.size();j++){                            // Loop each ShortgunSequence (Second Loop for all distance value of this read name)
                ps.format("\t%10.2f", dummySS.getDistanceVector()[j]);
            }
            ps.println();
        }   
    }    
        
    public void writeClusterGroupToPath(String path, String fa) throws FileNotFoundException, IOException {

       
        PrintStream ps = new PrintStream(path+"_ClusterGroup."+ fa);            // Create file object
       
        for(int i=0;i<this.clusterResult.size();i++){                           // Loop on clusterResult (ArrayList)
            ClusterGroup dummyCG = this.clusterResult.get(i);                   // clusterResult contain object ClusterGroup which contain bunch of ShortgunSequence in the same group 
            ps.print("Group" + i + ":");
            ArrayList<ShortgunSequence> listSS = dummyCG.getShortgunRead();     // get ArryList of ShortgunSequence
            for(int j=0;j<listSS.size();j++){                                   // Loop each ShortgunSequence contained in ArrayList
                ShortgunSequence dummySS = listSS.get(j);
                ps.print("\t"+dummySS.getReadName());
            }
            ps.println();
        }  
    }
    
    public void writePatternReport(String path, String fa) throws FileNotFoundException, IOException {
        
        PrintStream ps = new PrintStream(path+"_PossiblePattern."+ fa);         // Create file object
        ps.println("Possible Fusion Pattern");
        ps.println();
        for(int i =0;i<this.shrtRead.size();i++){                               // Loop each ShortgunSequence
        
            ShortgunSequence dummySS = this.shrtRead.get(i);
            ArrayList<ReconstructSequence> dummyListRecon = dummySS.getListReconSeq();  // Get ArrayList of object ReconStructSequence
            
            if (dummyListRecon.size() != 0){
                ps.println("Read Name: " + dummySS.getReadName());
            
            
                for(int j=0;j<dummyListRecon.size();j++){                           // Loop on each ReconStructSequence
                    ReconstructSequence dummyRecon = dummyListRecon.get(j);
                    ps.println(dummyRecon.getResultString());                       // get ResultString whic is String of pattern Report that already generate via build in function of ReconStructSequence
                    ps.println("Reconstruct Sequence: " + dummyRecon.getFullReconSequence());   // get FullReconSequence which is a String of DNA sequence of this ReconStructSequence 
                    ps.println();
                }
                ps.println();
            }
        }     
    }
    
    public void writeMatchCountReport(String fullPathFilename) throws FileNotFoundException, IOException{
    
        /**
         * Write Match count report which is a match count of each chromosome and position
         * 
         * Write it in Bed graph format as follow
         * 
         *          chromosome      startPosition       stopPosition        countNumber
         *          chromosome      startPosition       stopPosition        countNumber
         */
        
        int mask28bit = 268435455;
        int mask5bit = 31;
        int dummyCount = 0;
        int dummyPosition = 0;
        int selectChr = 0;
        int selectIniPos = 0;
        int selectLastPos = 0;
        int selectCount = 0;
        boolean writeFlag = false;
        boolean firstFlag = true;
        
        String filename = fullPathFilename+".bed";
        PrintStream ps;
        FileWriter writer;        
        /**
         * Check File existing
         */
        
        File f = new File(filename); //File object        
//        if(f.exists()){
//            // append if exist
////            ps = new PrintStream(new FileOutputStream(filename,true));
//            writer = new FileWriter(filename,true);
//        }else{
//            // create new
////            ps = new PrintStream(filename);
//            writer = new FileWriter(filename);
//        }
        writer = new FileWriter(filename);
        // getData then write file
        for(Map.Entry<Long,Integer> entry : countMap.entrySet()){
            long chrPos = entry.getKey();
            int dummyPos = (int)chrPos&mask28bit;
            int dummyChr = (int)(chrPos>>28);
            int count = entry.getValue();
            
            if(firstFlag == true){
                selectIniPos = dummyPos;
                selectLastPos = selectIniPos+1;
                selectChr = dummyChr;
                selectCount = count;
                firstFlag = false;
            }else{
                if(count == selectCount && dummyChr == selectChr){
                    selectLastPos = dummyPos+1; 
                }else{
                    writer.write(selectChr + "\t" + selectIniPos + "\t" + selectLastPos + "\t" + selectCount);
                    writer.write("\n");
                    
                    selectIniPos = dummyPos;
                    selectLastPos = selectIniPos+1;
                    selectChr = dummyChr;
                    selectCount = count;
                   
                }
            }
        }
        
        writer.flush();
        writer.close();
        
        
    }
    
    public void writeMatchCountReport(String fullPathFilename, String fullPathIndexFile) throws FileNotFoundException, IOException{
    
        /**
         * Write Match count report which is a match count of each chromosome and position
         * 
         * Write it in Bed graph format as follow
         * 
         *          chromosome      startPosition       stopPosition        countNumber
         *          chromosome      startPosition       stopPosition        countNumber
         */

        int mask28bit = 268435455;
        int mask5bit = 31;
        int dummyCount = 0;
        int dummyPosition = 0;
        long selectChr = 0;
        int selectIniPos = 0;
        int selectLastPos = 0;
        int selectCount = 0;
        boolean writeFlag = false;
        boolean firstFlag = true;
        boolean contigFlag = true;
        
        Map<Long,String> indexMap = SequenceUtil.readIndexFile(fullPathIndexFile);
        Map.Entry<Long,String> entryCheck = indexMap.entrySet().iterator().next();
        
        long code = entryCheck.getKey();
        long dummyLast = (code>>28)&mask28bit;
        
        
        if(dummyLast ==0) contigFlag = false; 
        
        String filename = fullPathFilename+".bed";
        PrintStream ps;
        FileWriter writer;        
        /**
         * Check File existing
         */
        
        File f = new File(filename); //File object        
//        if(f.exists()){
//            // append if exist
////            ps = new PrintStream(new FileOutputStream(filename,true));
//            writer = new FileWriter(filename,true);
//        }else{
//            // create new
////            ps = new PrintStream(filename);
//            writer = new FileWriter(filename);
//        }
        writer = new FileWriter(filename);
        writer.write("track type=bedGraph\n");

        if(contigFlag == false){
            // getData then write file
            for(Map.Entry<Long,Integer> entry : countMap.entrySet()){
                long chrPos = entry.getKey();
                int dummyPos = (int)chrPos&mask28bit;
                int dummyChr = (int)(chrPos>>28);
                int count = entry.getValue();

                if(firstFlag == true){
                    selectIniPos = dummyPos;
                    selectLastPos = selectIniPos+1;
                    selectChr = dummyChr;
                    selectCount = count;
                    firstFlag = false;
                }else{
                    if(count == selectCount && dummyChr == selectChr && selectLastPos == dummyPos){
                        selectLastPos = dummyPos+1; 
                    }else{
                        
                        String chrName = indexMap.get(selectChr);
                        
                        writer.write(chrName + " " + selectIniPos + " " + selectLastPos + " " + selectCount);
                        writer.write("\n");

                        selectIniPos = dummyPos;
                        selectLastPos = selectIniPos+1;
                        selectChr = dummyChr;
                        selectCount = count;

                    }
                }
            }

            writer.flush();
            writer.close();
        }
        
    }
    
    public void writeAlignmentSummaryReport(String fullPathFilename) throws FileNotFoundException, IOException{
    
        /**
         * Write summary Report 
         * 1. how many percentage align
         */

        String filename = fullPathFilename+"summary.txt";
        PrintStream ps;
        FileWriter writer;        
        /**
         * Check File existing
         */
        
        File f = new File(filename); //File object        
//        if(f.exists()){
//            // append if exist
////            ps = new PrintStream(new FileOutputStream(filename,true));
//            writer = new FileWriter(filename,true);
//        }else{
//            // create new
////            ps = new PrintStream(filename);
//            writer = new FileWriter(filename);
//        }
        writer = new FileWriter(filename);
        int numSample = this.shrtRead.size();
        int percentFullMatch = (this.numFullMatch*100)/numSample;
        
        writer.write("Overall Percent Match on Reference : "+percentFullMatch);
       
        writer.flush();
        writer.close();
        
        
    }
    
    public AlignmentResultRead generateSortedCutResult(int threshold) {

        /* Must specify threshold for cut result (The result that less than threshold will be cut out)*/
        //PrintStream ps = new PrintStream(path+"_AlignSortedCutResult."+ fa);            // Create file object
        AlignmentResultRead cutAlnRes = new AlignmentResultRead();
        
        for (int i=0;i<this.shrtRead.size();i++){                                       // Loop each ShortgunSequence
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
            
            Map<Long,long[]> countMap =  dummySS.getAlignmentCountSortedCut(threshold); // get alignmentCountSortedCut (HashMap of sorted and cut with specific threshold of alignment result Map<positionCode,numCountPlusColor>)
            
//            ps.printf("%-30s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
            Set allPos = countMap.keySet();
            Iterator iterPos = allPos.iterator();
            if(allPos.isEmpty() == false){
                cutAlnRes.addResult(dummySS);
                //ps.println(">Alignment result of "+ dummySS.getReadName());
                //ps.printf("%-35s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","Strand","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
            }
            
//            Set allPos = countMap.keySet();
//            Iterator iterPos = allPos.iterator();
//            while(iterPos.hasNext()){                                                   // loop all align positionCode (contain in HashMap)
//                long positionCode = (long)iterPos.next();
//                long alignPos = positionCode&mask;                                      // And with 28bit binary to get position
//                long chrNumber = positionCode>>29;                                      // Shift left 29 bit because positionCode have this structure [chr|strand|position] in order to get chromosome number
//                long[] numCountPlusColor = countMap.get(positionCode);                  // get array long which contain number of count and other color code and color code intensity
//                long numCount = numCountPlusColor[0];
//                long red = numCountPlusColor[1];
//                long yellow = numCountPlusColor[2];
//                long orange = numCountPlusColor[3];
//                long green = numCountPlusColor[4];
//                long redInt = numCountPlusColor[5];
//                long yellowInt = numCountPlusColor[6];
//                long orangeInt = numCountPlusColor[7];
//                long greenInt = numCountPlusColor[8];
//                
//                String strandNot = "no";                                                // Identify the strand type of this align Position
//                if(((positionCode>>28)&1) == 1){
//                    strandNot = "+";
//                }else if(((positionCode>>28)&1) == 0){
//                    strandNot = "-";
//                }   
//                
////                ps.format("Chr %d : Position %d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
//                //ps.format("Chr %2d : Position %12d|\t%8s\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,strandNot,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
//            }
//            if(allPos.isEmpty() == false){
//                //ps.println();
//            }
        }
        return cutAlnRes;
    }
    
    public void writeSortedCutResultToPathInFormat(String path, String fa, int threshold) throws FileNotFoundException, IOException {

        /* Must specify threshold for cut result (The result that less than threshold will be cut out)*/
        PrintStream ps = new PrintStream(path+"_Format_AlignSortedCutResult."+ fa);            // Create file object
        
        
        for (int i=0;i<this.shrtRead.size();i++){                                       // Loop each ShortgunSequence
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
            
            Map<Long,long[]> countMap =  dummySS.getAlignmentCountSortedCut(threshold); // get alignmentCountSortedCut (HashMap of sorted and cut with specific threshold of alignment result Map<positionCode,numCountPlusColor>)
            
//            ps.printf("%-30s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
            Set allPos = countMap.keySet();
            Iterator iterPos = allPos.iterator();
            if(allPos.isEmpty() == false){
                ps.println(">"+ dummySS.getReadName());
                //ps.printf("%-35s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","Strand","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
            }
            
//            Set allPos = countMap.keySet();
//            Iterator iterPos = allPos.iterator();
            while(iterPos.hasNext()){                                                   // loop all align positionCode (contain in HashMap)
                long positionCode = (long)iterPos.next();
                long alignPos = positionCode&mask;                                      // And with 28bit binary to get position
                long chrNumber = positionCode>>29;                                      // Shift left 29 bit because positionCode have this structure [chr|strand|position] in order to get chromosome number
                long[] numCountPlusColor = countMap.get(positionCode);                  // get array long which contain number of count and other color code and color code intensity
                long numCount = numCountPlusColor[0];
                long red = numCountPlusColor[1];
                long yellow = numCountPlusColor[2];
                long orange = numCountPlusColor[3];
                long green = numCountPlusColor[4];
                long redInt = numCountPlusColor[5];
                long yellowInt = numCountPlusColor[6];
                long orangeInt = numCountPlusColor[7];
                long greenInt = numCountPlusColor[8];
                
                
                String strandNot = "no";                                                // Identify the strand type of this align Position
                if(((positionCode>>28)&1) == 1){
                    strandNot = "+";
                }else if(((positionCode>>28)&1) == 0){
                    strandNot = "-";
                }   
                
//                ps.format("Chr %d : Position %d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
                ps.format("%d,%d,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d", chrNumber,alignPos,strandNot,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
                if (iterPos.hasNext()){
                    ps.format(";");
                }
                //ps.format("Chr %2d : Position %12d|\t%8s\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,strandNot,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
            }
            ps.format("\n");
            if(allPos.isEmpty() == false){
                //ps.println();
            }
        }
    }
    
    public void writeSortedCutResultMapToPathInFormat(String path,String nameFile, String fa) throws FileNotFoundException, IOException {

        PrintStream ps = new PrintStream(path+nameFile+"."+ fa);            // Create file object
        
        
        Set set = this.alignmentSortedCutResultMap.keySet();
        Iterator readNameIter = set.iterator();
       
        while(readNameIter.hasNext()){ 
            String readName = (String) readNameIter.next();
            ArrayList dummyResList = this.alignmentSortedCutResultMap.get(readName);
            
            if(dummyResList.isEmpty() == false){
                ps.println(">"+ readName);               
            }
            
            Iterator codeIter = dummyResList.iterator();
            while(codeIter.hasNext()){
                long code = (long) codeIter.next();                                     // code has structure like this [count|Chr|strand|alignPosition]
                long numCount = code>>34;                                               //Shift 34 bit to get count number
                long chrStrandAln = code&this.mask_chrStrandAln;
                long alignPos = chrStrandAln&mask;                                      // And with 28bit binary to get position
                long chrNumber = chrStrandAln>>29;
                
                String strandNot = "no";                                                // Identify the strand type of this align Position
                if(((chrStrandAln>>28)&1) == 1){
                    strandNot = "+";
                }else if(((chrStrandAln>>28)&1) == 0){
                    strandNot = "-";
                }
                
                ps.format("%d,%d,%s,%d", chrNumber,alignPos,strandNot,numCount);
                if (codeIter.hasNext()){
                    ps.format(";");
                }
            }
            
            if(dummyResList.isEmpty() == false){
                ps.format("\n");
            }
            
        }
    }
    
    public void writeSortedCutResultMapToPathInFormatV3(String path,String nameFile, String fileType) throws FileNotFoundException, IOException {
        /**
         * Suitable for version 3 data structure (data structure that has iniIdx in its)
         */
        
//        PrintStream ps = new PrintStream(path+nameFile+"."+ fa);            // Create file object
        if(fileType.equals("txt")){
            FileWriter writer;        
            /**
             * Check File existing
             */

            File f = new File(path+nameFile+"."+ fileType); //File object        

            writer = new FileWriter(path+nameFile+"."+ fileType);


            Set set = this.alignmentResultMap.keySet();
            Iterator readNameIter = set.iterator();

            while(readNameIter.hasNext()){ 
                String readName = (String) readNameIter.next();
                ArrayList dummyResList = this.alignmentResultMap.get(readName);
                int readLength = this.readLenList.get(readName);
                if(dummyResList.isEmpty() == false){
                    writer.write(">" + readName);
                    writer.write("\n");
    //                ps.println(">"+ readName);               
                }

                Iterator codeIter = dummyResList.iterator();
                while(codeIter.hasNext()){
                    long code = (long) codeIter.next();                                     // code has structure like this [count|Chr|strand|alignPosition]
                    long numCount = code>>42;                                               //Shift 34 bit to get count number
                    long chrIdxStrandAln = code&this.mask_chrIdxStrandAln;
                    long alignPos = chrIdxStrandAln&mask;                                      // And with 28bit binary to get position
                    long chrNumber = chrIdxStrandAln>>37;
                    long iniIdx = (chrIdxStrandAln>>29)&255;                     
                    
                    String strandNot = "no";                                                // Identify the strand type of this align Position
                    if(((chrIdxStrandAln>>28)&1) == 1){
                        strandNot = "+";
                    }else if(((chrIdxStrandAln>>28)&1) == 0){
                        strandNot = "-";
                    }

    //                ps.format("%d,%d,%s,%d,%d", chrNumber,alignPos,strandNot,numCount,iniIdx);
                    writer.write(String.format("%d,%d,%s,%d,%d,%d", chrNumber,alignPos,strandNot,numCount,iniIdx,readLength));
                    if (codeIter.hasNext()){
    //                    ps.format(";");
                        writer.write(";");
                    }
                }

                if(dummyResList.isEmpty() == false){
    //                ps.format("\n");
                    writer.write("\n");
                }

            }
            writer.flush();
            writer.close();
        }else if(fileType.equals("bin")){
            
            /**
             * The order of data when write .bin is 
             * ReandName|readLength|resultSize|resultCode1|resultCode2|resultCode3|...|ReadName|readLength|...
             */
            File outputFile = new File(path+nameFile+"."+fileType);
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
            
            
            for(Map.Entry<String,ArrayList<Long>> entry : this.alignmentResultMap.entrySet()){
                String readName = entry.getKey();
                ArrayList<Long> resultList = entry.getValue();
                int resultSize = resultList.size();
                int readLength = this.readLenList.get(readName);
                
                os.writeUTF(readName);
                os.writeInt(readLength);
                os.writeInt(resultSize);
                
                for(int i =0;i<resultSize;i++){
                    os.writeLong(resultList.get(i));
                }
            }
            os.close();   
        }
    }
    
    public void writeSortedCutResultMapToPathInFormatLongRead(String path,String nameFile, String fileType) throws FileNotFoundException, IOException {
        /**
         * Suitable for version 3 data structure (data structure that has iniIdx in its)
         * It is the extend version of data 3 structure we 2 map to store all alignment result because it long read we need 2 long to store it iniIndex and countMerMatch 
         */
        
//        PrintStream ps = new PrintStream(path+nameFile+"."+ fa);            // Create file object
        if(fileType.equals("txt")){
            FileWriter writer;        
            /**
             * Check File existing
             */

            File f = new File(path+nameFile+"."+ fileType); //File object        

            writer = new FileWriter(path+nameFile+"."+ fileType);


            Set set = this.alignmentResultMap1.keySet();
            Iterator readNameIter = set.iterator();

            while(readNameIter.hasNext()){ 
                String readName = (String) readNameIter.next();
                ArrayList<Long> dummyResList1 = this.alignmentResultMap1.get(readName);
                ArrayList<Long> dummyResList2 = this.alignmentResultMap2.get(readName);
                int readLength = this.readLenList.get(readName);
                if(dummyResList1.isEmpty() == false){
                    writer.write(">" + readName);
                    writer.write("\n");
    //                ps.println(">"+ readName);               
                }
                Iterator codeIter1 = dummyResList1.iterator();
                Iterator codeIter2 = dummyResList2.iterator();
                while(codeIter1.hasNext()){
                    long idxStrandAln = (long)codeIter1.next();                    
                    long chrCount = (long)codeIter2.next();
                    long alignPos = idxStrandAln&mask;
                    long iniIdx = idxStrandAln>>29;
                    long numCount = chrCount&mask32bit;
                    long chrNumber = (chrCount>>32);
                    
                    String strandNot = "no";                                                // Identify the strand type of this align Position
                    if(((idxStrandAln>>28)&1) == 1){
                        strandNot = "+";
                    }else if(((idxStrandAln>>28)&1) == 0){
                        strandNot = "-";
                    }
                    writer.write(String.format("%d,%d,%s,%d,%d,%d", chrNumber,alignPos,strandNot,numCount,iniIdx,readLength));
                    if (codeIter1.hasNext()){
    //                    ps.format(";");
                        writer.write(";");
                    }
                }
                
                if(dummyResList1.isEmpty() == false){
    //                ps.format("\n");
                    writer.write("\n");
                }

            }
            writer.flush();
            writer.close();
        }else if(fileType.equals("bin")){
            
            /**
             * The order of data when write .bin is 
             * ReandName|readLength|resultSize|resultCode1(idxStrandAln)|resultCode1(chrCOunt)|resultCode2(idxStrandAln)|resultCode2(chrCount)|...|ReadName|readLength|...
             */
            File outputFile = new File(path+nameFile+"."+fileType);
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
            
            
            for(Map.Entry<String,ArrayList<Long>> entry : this.alignmentResultMap1.entrySet()){
                String readName = entry.getKey();
                ArrayList<Long> idxStrandAlnList = entry.getValue();
                int resultSize = idxStrandAlnList.size();
                int readLength = this.readLenList.get(readName);
                ArrayList<Long> chrCountList = this.alignmentResultMap2.get(readName);
                os.writeUTF(readName);
                os.writeInt(readLength);
                os.writeInt(resultSize);
                
                for(int i =0;i<resultSize;i++){
                    os.writeLong(idxStrandAlnList.get(i));
                    os.writeLong(chrCountList.get(i));
                }
            }
            os.close();   
        }
    }
    
    public void writeSortedCutColorResultToPathInFormat(String path, String nameFile, String fileFormat) throws FileNotFoundException{
        PrintStream ps = new PrintStream(path+nameFile+"."+fileFormat);
        
        //this.shrtRead.get(0)
        
    }
    
    public void writeSortedCutColorResultToPathInFormatForLinuxSort(String path, String nameFile, String fileFormat) throws FileNotFoundException, IOException{
    
        /**
         * Suitable for version 3 data structure (data structure that has iniIdx in its)
         * write result to file format for linux sort purpose
         */
        
        String filename = path+nameFile+"."+fileFormat;
        PrintStream ps;
        FileWriter writer;
        /**
         * Check File existing
         */
        
        File f = new File(filename); //File object        
        if(f.exists()){
//            ps = new PrintStream(new FileOutputStream(filename,true));
            writer = new FileWriter(filename,true);
        }else{
//            ps = new PrintStream(filename);
            writer = new FileWriter(filename);
        }
        
        /**
         * Begin extract data to write
         */
        for(int i=0;i<this.shrtRead.size();i++){
            ShortgunSequence dummySS = this.shrtRead.get(i);
            String readName = dummySS.getReadName();
            ArrayList<Integer> listChr = dummySS.getListChrMatch();
            ArrayList<Long> listIniPos = dummySS.getListPosMatch();
            ArrayList<Long> listLastPos = dummySS.getListLastPosMatch();
            ArrayList<Integer> listIniIndex = dummySS.getListIniIdx();
            ArrayList<String> listStrand = dummySS.getListStrand();
            ArrayList<Integer> listGreen = dummySS.getListGreen();
            ArrayList<Integer> listYellow = dummySS.getListYellow();
            ArrayList<Integer> listOrange = dummySS.getListOrange();
            ArrayList<Integer> listRed = dummySS.getListRed();
            ArrayList<Integer> listSNPFlag = dummySS.getSNPFlag();
            ArrayList<Integer> listIniBackFlag = dummySS.getIniBackFlag();
            
            for(int numP=0;numP<listChr.size();numP++){
                int numChr = listChr.get(numP);
                long iniPos = listIniPos.get(numP);
                long lastPos = listLastPos.get(numP);
                int iniIndex = listIniIndex.get(numP);
                int green = listGreen.get(numP);
                int yellow = listYellow.get(numP);
                int orange = listOrange.get(numP);
                int red = listRed.get(numP);
                int snpFlag = listSNPFlag.get(numP);
                int iniBackFlag = listIniBackFlag.get(numP);
//                int flag = 0;
//                
//                if(snpFlag == true){
//                    flag = 1;
//                }
                
                String strand = listStrand.get(numP);
                int matchCount = green+yellow+orange+red;
                int missingMer = 0;
                
                if(snpFlag!=0){
                    missingMer = (snpFlag+dummySS.getMerLength())-1;
                    matchCount = matchCount+missingMer;
                }
                
                if(strand.equals("-")){
                    iniIndex = dummySS.getReadLength() - (iniIndex+(dummySS.getMerLength()+matchCount-1));
//                    stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
                }
               
//                ps.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName);
//                ps.format("\n");
                writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag));
                writer.write("\n");
            }
        }
        writer.flush();
        writer.close();
    }
    
    public void writeSortedCutColorResultToPathInFormatForLinuxSort(String path, String nameFile, String fileFormat, String option1) throws FileNotFoundException, IOException{
    
        /**
         * Suitable for version 3 data structure (data structure that has iniIdx in its)
         * write result to file format for linux sort purpose
         * 
         * String option1 filter option code "gy" mean we cut out the read match result that has orange and red count
         *                                   "g"  mean we cut out the read match result contain yellow orange and red count
         */
        
        String filename = path+File.separator+nameFile+"."+fileFormat;
        PrintStream ps;
        FileWriter writer;
        /**
         * Check File existing
         */
        
        File f = new File(filename); //File object        
        if(f.exists()){
//            ps = new PrintStream(new FileOutputStream(filename,true));
            writer = new FileWriter(filename,true);
        }else{
//            ps = new PrintStream(filename);
            writer = new FileWriter(filename);
        }
        
        /**
         * Begin extract data to write
         */
        for(int i=0;i<this.shrtRead.size();i++){
            ShortgunSequence dummySS = this.shrtRead.get(i);
            String readName = dummySS.getReadName();
            int readLength = dummySS.getReadLength();
            ArrayList<Integer> listChr = dummySS.getListChrMatch();
            ArrayList<Long> listIniPos = dummySS.getListPosMatch();
            ArrayList<Long> listLastPos = dummySS.getListLastPosMatch();
            ArrayList<Integer> listIniIndex = dummySS.getListIniIdx();
            ArrayList<String> listStrand = dummySS.getListStrand();
            ArrayList<Integer> listGreen = dummySS.getListGreen();
            ArrayList<Integer> listYellow = dummySS.getListYellow();
            ArrayList<Integer> listOrange = dummySS.getListOrange();
            ArrayList<Integer> listRed = dummySS.getListRed();
            ArrayList<Integer> listSNPFlag = dummySS.getSNPFlag();
            ArrayList<Integer> listIniBackFlag = dummySS.getIniBackFlag();
            
            for(int numP=0;numP<listChr.size();numP++){
                int numChr = listChr.get(numP);
                long iniPos = listIniPos.get(numP);
                long lastPos = listLastPos.get(numP);
                int iniIndex = listIniIndex.get(numP);
                int green = listGreen.get(numP);
                int yellow = listYellow.get(numP);
                int orange = listOrange.get(numP);
                int red = listRed.get(numP);
                int snpFlag = listSNPFlag.get(numP);
                int iniBlackFlag = listIniBackFlag.get(numP);
//                int flag = 0;
//                
//                if(snpFlag == true){
//                    flag = 1;
//                }
                
                String strand = listStrand.get(numP);
                int matchCount = green+yellow+orange+red;
                int missingMer = 0;
                
                if(snpFlag!=0){
                    missingMer = (snpFlag+dummySS.getMerLength())-1;
                    matchCount = matchCount+missingMer;
                }
                
                if(strand.equals("-")){
                    iniIndex = dummySS.getReadLength() - (iniIndex+(dummySS.getMerLength()+matchCount-1));
//                    stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
                }
//                ps.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName);
//                ps.format("\n");
                if(option1.equals("gy")){
                    if(orange==0&&red==0){
                        writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBlackFlag,readLength));
                        writer.write("\n");
                    }
                }else if(option1.equals("g")){
                    if(orange==0&&red==0&&yellow==0){
                        writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBlackFlag,readLength));
                        writer.write("\n");
                    }
                }else if(option1.equals("all")){
                    writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBlackFlag,readLength));
                    writer.write("\n");
                }
                
            }
        }
        writer.flush();
        writer.close();
    }
    
    public void writeSortedCutColorResultToPathInFormatForLinuxSort(String path, String nameFile, String fileFormat, String option1, int option2) throws FileNotFoundException, IOException{
    
        /**
         * Suitable for version 3 data structure (data structure that has iniIdx in its)
         * write result to file format for linux sort purpose
         * 
         * String option1 filter option code "gy" mean we filter out the read match result that has orange and red count
         *                                   "g"  mean we filter out the read match result contain yellow orange and red count
         *                                   "gyo" mean we filter out the read match result contain only red count
         *                                  "all" mean no filter
         * int option2 filter option threshold indicate the percentage of maximum mer count. If read match result have mer count equal or more than this threshold the result has been filter out. (100 will prevent full match read)
         * 
         * We adjust iniIndex into a view of strand + [the last index of strand - will be iniIndex of strand +]
         */
        
        String filename = path+File.separator+nameFile+"."+fileFormat;
        PrintStream ps;
        FileWriter writer;        
        /**
         * Check File existing
         */
        
        File f = new File(filename); //File object        
        if(f.exists()){
//            ps = new PrintStream(new FileOutputStream(filename,true));
            writer = new FileWriter(filename,true);
        }else{
//            ps = new PrintStream(filename);
            writer = new FileWriter(filename);
        }
        
        /**
         * Begin extract data to write
         */
        for(int i=0;i<this.shrtRead.size();i++){
            boolean ignoreFlag = false;
            ShortgunSequence dummySS = this.shrtRead.get(i);
            String readName = dummySS.getReadName();
            int readLength = dummySS.getReadLength();
            ArrayList<Integer> listChr = dummySS.getListChrMatch();
            ArrayList<Long> listIniPos = dummySS.getListPosMatch();
            ArrayList<Long> listLastPos = dummySS.getListLastPosMatch();
            ArrayList<Integer> listIniIndex = dummySS.getListIniIdx();
            ArrayList<String> listStrand = dummySS.getListStrand();
            ArrayList<Integer> listGreen = dummySS.getListGreen();
            ArrayList<Integer> listYellow = dummySS.getListYellow();
            ArrayList<Integer> listOrange = dummySS.getListOrange();
            ArrayList<Integer> listRed = dummySS.getListRed();
            ArrayList<Integer> listSNPFlag = dummySS.getSNPFlag();
            ArrayList<Integer> listIniBackFlag = dummySS.getIniBackFlag();
            int numMaxMatch = ((option2 * dummySS.readLength)/100)-dummySS.merLength+1;
            
            /**
             * Find writing order (order by iniIndex)
             */
                    // Code here
            /**
             * 
             */
            
            for(int numP=0;numP<listChr.size();numP++){
                int numChr = listChr.get(numP);
                long iniPos = listIniPos.get(numP);
                long lastPos = listLastPos.get(numP);
                int iniIndex = listIniIndex.get(numP);
                int green = listGreen.get(numP);
                int yellow = listYellow.get(numP);
                int orange = listOrange.get(numP);
                int red = listRed.get(numP);
                
                String strand = listStrand.get(numP);
                int merMatch = green+yellow+orange+red;
                if(merMatch == numMaxMatch){
                    ignoreFlag = true;
                }
            }
            if(ignoreFlag==false){
                for(int numP=0;numP<listChr.size();numP++){
                    int numChr = listChr.get(numP);
                    long iniPos = listIniPos.get(numP);
                    long lastPos = listLastPos.get(numP);
                    int iniIndex = listIniIndex.get(numP);
                    int green = listGreen.get(numP);
                    int yellow = listYellow.get(numP);
                    int orange = listOrange.get(numP);
                    int red = listRed.get(numP);
                    int snpFlag = listSNPFlag.get(numP);
                    int iniBackFlag = listIniBackFlag.get(numP);
//                    int flag = 0;

//                    if(snpFlag == true){
//                        flag = 1;
//                    }

                    String strand = listStrand.get(numP);
                    int matchCount = green+yellow+orange+red;
                    int missingMer = 0;
                    
//                    if(snpFlag!=0){         
//                        missingMer = (snpFlag+dummySS.getMerLength())-1;
//                        matchCount = matchCount+missingMer;
//                    }
                    /**
                     * Cut this pat out because we already do it in joinPeakV2
                     * we add missing mer into red color count
                     */
//                    if(snpFlag!=0){         
//                        missingMer = snpFlag;
//                        matchCount = matchCount+missingMer;
//                    }
                    
//                    if(dummySS.getReadName().equals("Read12SS07")){
//                        System.out.println();
//                    }
                    if(strand.equals("-")){
                        iniIndex = dummySS.getReadLength() - (iniIndex+(dummySS.getMerLength()+matchCount-1));
    //                    stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
                    }
//                    int merMatch = green+yellow+orange+red;
    //                ps.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName);
    //                ps.format("\n");
                    if(option1.equals("gy")){
                        if(orange==0&&red==0){
                            writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength));
                            writer.write("\n");
                        }
                    }else if(option1.equals("g")){
                        if(orange==0&&red==0&&yellow==0){
                            writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength));
                            writer.write("\n");
                        }
                    }else if(option1.equals("gyo")){
                        if(red==0){
                            writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength));
                            writer.write("\n");
                        }
                    }else if(option1.equals("all")){
                        writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength));
                        writer.write("\n");
                    }

                }
            }else{
                numFullMatch++;
            }
        }
        writer.flush();
        writer.close();      
    }
    
    public void writeAlignSequenceReadFasta(String path, String nameFile, String sampleFile, int propotion, String option1, int option2) throws FileNotFoundException, IOException{
    
        /**
         * Suitable for version 3 data structure (data structure that has iniIdx in its)
         * write result to file format for linux sort purpose
         * 
         * String option1 filter option code "gy" mean we filter out the read match result that has orange and red count
         *                                   "g"  mean we filter out the read match result contain yellow orange and red count
         *                                   "gyo" mean we filter out the read match result contain only red count
         *                                  "all" mean no filter
         * int option2 filter option threshold indicate the percentage of maximum mer count. If read match result have mer count equal or more than this threshold the result has been filter out. (100 will prevent full match read)
         * 
         * This function will map read name from result to input file and write the sequence that exist in the result to new fasta file
         */
        
        String filename = path+File.separator+nameFile+".fa";
        PrintStream ps;
        FileWriter writer; 
        ArrayList<String> selectReadNameList = new ArrayList();
        /**
         * Check File existing
         */
        
        File f = new File(filename); //File object        
        if(f.exists()){
            //append to existing file
//            ps = new PrintStream(new FileOutputStream(filename,true));
            writer = new FileWriter(filename,true);
        }else{
//            ps = new PrintStream(filename);
            writer = new FileWriter(filename);
        }
        
        /**
         * Begin extract data to write
         */
        for(int i=0;i<this.shrtRead.size();i++){
            boolean ignoreFlag = false;
            boolean selectFlag = false;
            ShortgunSequence dummySS = this.shrtRead.get(i);
            String readName = dummySS.getReadName();
            ArrayList<Integer> listChr = dummySS.getListChrMatch();
            ArrayList<Integer> listGreen = dummySS.getListGreen();
            ArrayList<Integer> listYellow = dummySS.getListYellow();
            ArrayList<Integer> listOrange = dummySS.getListOrange();
            ArrayList<Integer> listRed = dummySS.getListRed();
            int numMaxMatch = ((option2 * dummySS.readLength)/100)-dummySS.merLength+1;
            for(int numP=0;numP<listChr.size();numP++){

                int green = listGreen.get(numP);
                int yellow = listYellow.get(numP);
                int orange = listOrange.get(numP);
                int red = listRed.get(numP);
                
                int merMatch = green+yellow+orange+red;
                if(merMatch == numMaxMatch){
                    ignoreFlag = true;
                }
            }
            if(ignoreFlag==false){
                for(int numP=0;numP<listChr.size();numP++){

                    int yellow = listYellow.get(numP);
                    int orange = listOrange.get(numP);
                    int red = listRed.get(numP);
                    if(option1.equals("gy")){
                        if(orange==0&&red==0){
//                            if(selectReadNameList.contains(readName)!= true){
//                                selectReadNameList.add(readName);
//                            }
                            selectFlag=true;
                            break;
                        }
                    }else if(option1.equals("g")){
                        if(orange==0&&red==0&&yellow==0){
//                            if(selectReadNameList.contains(readName)!= true){
//                                selectReadNameList.add(readName);
//                            }
                            selectFlag=true;
                            break;
                        }
                    }else if(option1.equals("gyo")){
                        if(red==0){
//                            if(selectReadNameList.contains(readName)!= true){
//                                selectReadNameList.add(readName);
//                            }
                            selectFlag=true;
                            break;
                        }
                    }else if(option1.equals("all")){
//                        if(selectReadNameList.contains(readName)!= true){
//                            selectReadNameList.add(readName);
//                        }
                        selectFlag=true;
                        break;
                    }
                }
            }
            
            if(selectFlag==true){
                selectReadNameList.add(readName);
            }
        }
        
        int numSample = SequenceUtil.getNumberSample(sampleFile);
        int count = 0;
        for (int i = 0 ; i < numSample ; i += propotion){                       // loop over the input sample ( number of loop is up to the number of read per time )
            long startTime = System.currentTimeMillis();
            count++;
            String savefilename = filename+count;
            Map<String,String> inputMap = SequenceUtil.readSampleFiletoMap(sampleFile,i,Math.min(numSample, i+propotion));
            
            for(int index= 0;index<selectReadNameList.size();index++){
                String readNameCheck = selectReadNameList.get(index);
                String selectSeq = inputMap.get(readNameCheck);
                
                if(selectSeq!=null){
                    writer.write(">"+readNameCheck+"\n");
                    writer.write(selectSeq+"\n");
                }    
            }
        }
        writer.flush();
        writer.close();
    }
    
    public void sortCountCutResultForMap(long inTh){
        /* Contain special part that create preriquisit data for cluster propose*/
        long threshold = inTh;
        long oldCount = 0;
        long newCount = 0;
        long containCheck = 0;
        long selectCode = 0;
        int i =0;
        Object selectKey = null;
        int numKey = this.alignmentResultMap.size();

//        Iterator roundIter = this.countResult.keySet().iterator();
//        Iterator keyIter = this.countResult.keySet().iterator();
        
        Set set = this.alignmentResultMap.keySet();
        Iterator readNameIter = set.iterator();
        
//        System.out.println("Check Key Size: "+ set.size());
        
        while(readNameIter.hasNext()){ 
            ArrayList<Long> dummySortedList = new ArrayList();
            String readName = (String) readNameIter.next();
            ArrayList<Long> readList = this.alignmentResultMap.get(readName);
            
//            System.out.println("Do sorting round: "+ i);
            Iterator roundIter = readList.iterator();
            while(roundIter.hasNext()){
                roundIter.next();
                
                Iterator codeIter = readList.iterator();
                while(codeIter.hasNext()){   
                    long code = (long) codeIter.next();
                    long numCount = code>>34; 
                    long chrStrandAln = code&this.mask_chrStrandAln;
                    
                    if(dummySortedList.contains(code)||numCount < threshold){

                    }else{
                        newCount = numCount;
//                        System.out.println("Check newCount: "+newCount);
//                        System.out.println("Check oldCount: "+oldCount);

                        if(newCount > oldCount){
                            oldCount = newCount;
                            selectCode = code;
//                            System.out.println("New>Old (Not Exist) Check OldCount: " + oldCount);
//                            System.out.println("New>Old (Not Exist) Check Key: " + selectKey);
                        }else if(newCount == oldCount){
                            oldCount = newCount;
                            selectCode = code;
//                            System.out.println("New=Old (Not Exist) Check OldCount: " + oldCount);
//                            System.out.println("New=Old (Not Exist) Check Key: " + selectKey);
                        }
                    }      
                }
                if(selectCode != 0){
                    dummySortedList.add(selectCode);
                }
                selectCode = 0;
                oldCount = 0;
            }
            this.alignmentSortedCutResultMap.put(readName, dummySortedList);
            selectCode = 0;
            oldCount = 0;
        }
        
    }
    
    public void sortCountCutResultForMapV2(long inTh){
        /* Contain special part that create preriquisit data for cluster propose*/
        long threshold = inTh;
        long oldCount = 0;
        long newCount = 0;
        long containCheck = 0;
        long selectCode = 0;
        int i =0;
        Object selectKey = null;
        int numKey = this.alignmentResultMap.size();

//        Iterator roundIter = this.countResult.keySet().iterator();
//        Iterator keyIter = this.countResult.keySet().iterator();
        
        Set set = this.alignmentResultMap.keySet();
        Iterator readNameIter = set.iterator();
        
//        System.out.println("Check Key Size: "+ set.size());
        
        while(readNameIter.hasNext()){ 
            ArrayList<Long> dummySortedList = new ArrayList();
            String readName = (String) readNameIter.next();
            ArrayList<Long> readList = this.alignmentResultMap.get(readName);
            
            Collections.sort(readList);
//            System.out.println("Do sorting round: "+ i);
            Iterator elementIter = readList.iterator();
            while(elementIter.hasNext()){
                long code = (long) elementIter.next();
                long numCount = code>>34;
                
                if(numCount<threshold){
                    
                }else{
                    dummySortedList.add(code);   
                }
            }
            Collections.reverse(dummySortedList);
            this.alignmentSortedCutResultMap.put(readName, dummySortedList);
        }
                
                
        
    }
    
    public void sortCountCutResultForMapV3(long inTh){
        /* Contain special part that create preriquisit data for cluster propose*/
        long threshold = inTh;
       
        for(Map.Entry<String,ArrayList<Long>> entry : this.alignmentResultMap.entrySet()){
            String readName = entry.getKey();
            ArrayList<Long> readList = entry.getValue();        
            ArrayList<Long> dummySortedList = new ArrayList();
            //System.out.println("Read name : " + readName);
            Collections.sort(readList);
//            System.out.println("Do sorting round: "+ i);
            Iterator elementIter = readList.iterator();
            while(elementIter.hasNext()){
                long code = (long) elementIter.next();
                long numCount = code>>42;
                //System.out.println("code: "+code+" numcount: " + numCount);
                if(numCount<threshold){
                    
                }else{
                    dummySortedList.add(code);   
                }
            }
            Collections.reverse(dummySortedList);
            this.alignmentSortedCutResultMap.put(readName, dummySortedList);
        }
 
    }
    
    public void sortCountCutLocalResultForMap(long inTh){
        /* Contain special part that create preriquisit data for cluster propose*/
        long threshold = inTh;
        long oldCount = 0;
        long newCount = 0;
        long containCheck = 0;
        long selectCode = 0;
        int i =0;
        Object selectKey = null;
        int numKey = this.alignmentResultMap.size();

//        Iterator roundIter = this.countResult.keySet().iterator();
//        Iterator keyIter = this.countResult.keySet().iterator();
        
        Set set = this.alignmentResultMap.keySet();
        Iterator readNameIter = set.iterator();
        
//        System.out.println("Check Key Size: "+ set.size());
        
        while(readNameIter.hasNext()){ 
            ArrayList<Long> dummySortedList = new ArrayList();
            String readName = (String) readNameIter.next();
            ArrayList<Long> readList = this.alignmentResultMap.get(readName);
            
            Collections.sort(readList);
//            System.out.println("Do sorting round: "+ i);
            Iterator elementIter = readList.iterator();
            while(elementIter.hasNext()){
                long code = (long) elementIter.next();
                long numCount = code>>56;
                
                if(numCount<threshold){
                    this.unMapList.add(readName);
                }else{
                    dummySortedList.add(code);
                    this.mapList.add(readName);
                }
            }
            if(readList.isEmpty()){
                this.unMapList.add(readName);
            }
            Collections.reverse(dummySortedList);
            this.alignmentSortedCutResultMap.put(readName, dummySortedList);
        }
                
                
        
    }
    
    public void countAlignMatch(){
        /**
         * This function will be count the number of read that match in specific chr at specific position
         * It also remove the duplication read
         */
        
        this.countMap = new TreeMap();
        this.checkDupMap = new LinkedHashMap();
        for(int i=0;i<this.shrtRead.size();i++){
            ShortgunSequence ss = this.shrtRead.get(i);
            ArrayList<Integer> listIniIdx = ss.getListIniIdx();
            ArrayList<Integer> listChr = ss.getListChrMatch();
            ArrayList<Long> listPos = ss.getListPosMatch();
            ArrayList<Long> listLastPos = ss.getListLastPosMatch();
            for(int j=0;j<listChr.size();j++){
                long chr = listChr.get(j);
                long iniPos = listPos.get(j);
                long lastPos = listLastPos.get(j);
                int iniIdx = listIniIdx.get(j);
                
                long numBaseMatch = (lastPos - iniPos)+1;
                
                for(int num=(int)iniPos;num<=(int)lastPos;num++){
                    long chrPos = (chr<<28)+num;
                    
                    if(countMap.containsKey(chrPos)){
                        ArrayList<Integer> listIni = checkDupMap.get(chrPos);
                        
                        if(listIni.contains(iniIdx)!=true){ 
                            /**
                             * Case check for remove duplicate read
                             * The read that align at same chr and same position and have the same initial position on read
                             * It is duplicate read.
                            */
                            int count = countMap.get(chrPos)+1;
                            countMap.put(chrPos, count);
                        }
                    }else{
                        int count = 1;
                        countMap.put(chrPos, count);
                        ArrayList<Integer> listIni = new ArrayList();
                        listIni.add(iniIdx);
                        checkDupMap.put(chrPos, listIni);
                    }
                }
            }
        }
    }
    
    public void sortFilterResult(String option1, int option2){
        /**
         * String option1 filter option code "gy" mean we filter out the read match result that has orange and red count
         *                                   "g"  mean we filter out the read match result contain yellow orange and red count
         *                                   "gyo" mean we filter out the read match result contain only red count
         *                                  "all" mean no filter
         * int option2 filter option threshold indicate the percentage of maximum mer count. If read match result have mer count equal or more than this threshold the result has been filter out. (100 will prevent full match read)
         * 
         * We adjust iniIndex into a view of strand + [the last index of strand - will be iniIndex of strand +]
         * 
         * Begin extract data to sort
         * 
         */
        for(int i=0;i<this.shrtRead.size();i++){
            boolean ignoreFlag = false;
            ShortgunSequence dummySS = this.shrtRead.get(i);
            String readName = dummySS.getReadName();
            int readLength = dummySS.getReadLength();
            ArrayList<Integer> listChr = dummySS.getListChrMatch();
            ArrayList<Long> listIniPos = dummySS.getListPosMatch();
            ArrayList<Long> listLastPos = dummySS.getListLastPosMatch();
            ArrayList<Integer> listIniIndex = dummySS.getListIniIdx();
            ArrayList<String> listStrand = dummySS.getListStrand();
            ArrayList<Integer> listGreen = dummySS.getListGreen();
            ArrayList<Integer> listYellow = dummySS.getListYellow();
            ArrayList<Integer> listOrange = dummySS.getListOrange();
            ArrayList<Integer> listRed = dummySS.getListRed();
            ArrayList<Integer> listSNPFlag = dummySS.getSNPFlag();
            ArrayList<Integer> listIniBackFlag = dummySS.getIniBackFlag();
            int numMaxMatch = ((option2 * dummySS.readLength)/100)-dummySS.merLength+1;
                        
            for(int numP=0;numP<listChr.size();numP++){

                int green = listGreen.get(numP);
                int yellow = listYellow.get(numP);
                int orange = listOrange.get(numP);
                int red = listRed.get(numP);

                int merMatch = green+yellow+orange+red;
                if(merMatch == numMaxMatch){
                    ignoreFlag = true;
                }
            }
            if(ignoreFlag==false){
                for(int numP=0;numP<listChr.size();numP++){
                    int numChr = listChr.get(numP);
                    long iniPos = listIniPos.get(numP);
                    long lastPos = listLastPos.get(numP);
                    int iniIndex = listIniIndex.get(numP);
                    int green = listGreen.get(numP);
                    int yellow = listYellow.get(numP);
                    int orange = listOrange.get(numP);
                    int red = listRed.get(numP);
                    int snpFlag = listSNPFlag.get(numP);
                    int iniBackFlag = listIniBackFlag.get(numP);
                    String strand = listStrand.get(numP);
                    
                    int matchCount = green+yellow+orange+red;
                    int missingMer = 0;
                    
                    if(snpFlag!=0){         
                        missingMer = (snpFlag+dummySS.getMerLength())-1;
                        matchCount = matchCount+missingMer;
                    }
                    
                    if(strand.equals("-")){
                        iniIndex = dummySS.getReadLength() - (iniIndex+(dummySS.getMerLength()+matchCount-1));
    //                    stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
                    }
                    
                    if(option1.equals("gy")){
                        if(orange==0&&red==0){
                            AlignmentResult alnResult = new AlignmentResult();                  
                            alnResult.addResultData(numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength);
                            this.AlignmentResultList.add(alnResult);
                        }
                    }else if(option1.equals("g")){
                        if(orange==0&&red==0&&yellow==0){
                            AlignmentResult alnResult = new AlignmentResult();
                            alnResult.addResultData(numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength);
                            this.AlignmentResultList.add(alnResult);
                        }
                    }else if(option1.equals("gyo")){
                        if(red==0){
                            AlignmentResult alnResult = new AlignmentResult();
                            alnResult.addResultData(numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength);
                            this.AlignmentResultList.add(alnResult);
                        }
                    }else if(option1.equals("all")){
                        AlignmentResult alnResult = new AlignmentResult();
                        alnResult.addResultData(numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength);
                        this.AlignmentResultList.add(alnResult);
                    }
                              
                } 
            }
        }
        
        Collections.sort(this.AlignmentResultList, AlignmentResult.AlnResNameIniIdxComparator);

    }
    
//    public void writeFilterTargetResult(String path, String nameFile, String fileFormat, String option1, int option2) throws FileNotFoundException, IOException{
//        //NotFinish
//        /**
//         * Suitable for version 3 data structure (data structure that has iniIdx in its)
//         * write result to file format for linux sort purpose
//         * 
//         * String option1 filter option code "gy" mean we filter out the read match result that has orange and red count
//         *                                   "g"  mean we filter out the read match result contain yellow orange and red count
//         *                                   "gyo" mean we filter out the read match result contain only red count
//         *                                  "all" mean no filter
//         * int option2 filter option threshold indicate the percentage of maximum mer count. If read match result have mer count equal or more than this threshold the result has been filter out. (100 will prevent full match read)
//         * 
//         * We adjust iniIndex into a view of strand + [the last index of strand - will be iniIndex of strand +]
//         */
//        
//        String filename = path+File.separator+nameFile+"."+fileFormat;
//        PrintStream ps;
//        FileWriter writer;        
//        /**
//         * Check File existing
//         */
//        
//        File f = new File(filename); //File object        
//        if(f.exists()){
////            ps = new PrintStream(new FileOutputStream(filename,true));
//            writer = new FileWriter(filename,true);
//        }else{
////            ps = new PrintStream(filename);
//            writer = new FileWriter(filename);
//        }
//        
//        /**
//         * Begin extract data to write
//         */
//        for(int i=0;i<this.shrtRead.size();i++){
//            boolean ignoreFlag = false;
//            ShortgunSequence dummySS = this.shrtRead.get(i);
//            String readName = dummySS.getReadName();
//            int readLength = dummySS.getReadLength();
//            ArrayList<Integer> listChr = dummySS.getListChrMatch();
//            ArrayList<Long> listIniPos = dummySS.getListPosMatch();
//            ArrayList<Long> listLastPos = dummySS.getListLastPosMatch();
//            ArrayList<Integer> listIniIndex = dummySS.getListIniIdx();
//            ArrayList<String> listStrand = dummySS.getListStrand();
//            ArrayList<Integer> listGreen = dummySS.getListGreen();
//            ArrayList<Integer> listYellow = dummySS.getListYellow();
//            ArrayList<Integer> listOrange = dummySS.getListOrange();
//            ArrayList<Integer> listRed = dummySS.getListRed();
//            ArrayList<Integer> listSNPFlag = dummySS.getSNPFlag();
//            ArrayList<Integer> listIniBackFlag = dummySS.getIniBackFlag();
//            int numMaxMatch = ((option2 * dummySS.readLength)/100)-dummySS.merLength+1;
//            
//            /**
//             * Find writing order (order by iniIndex)
//             */
//                    // Code here
//            /**
//             * 
//             */
//            
//            for(int numP=0;numP<listChr.size();numP++){
//                int numChr = listChr.get(numP);
//                long iniPos = listIniPos.get(numP);
//                long lastPos = listLastPos.get(numP);
//                int iniIndex = listIniIndex.get(numP);
//                int green = listGreen.get(numP);
//                int yellow = listYellow.get(numP);
//                int orange = listOrange.get(numP);
//                int red = listRed.get(numP);
//                
//                String strand = listStrand.get(numP);
//                int merMatch = green+yellow+orange+red;
////                if(merMatch == numMaxMatch){
////                    ignoreFlag = true;
////                }
//                
//                
//            if(ignoreFlag==false){
//                for(int numP=0;numP<listChr.size();numP++){
//                    int numChr = listChr.get(numP);
//                    long iniPos = listIniPos.get(numP);
//                    long lastPos = listLastPos.get(numP);
//                    int iniIndex = listIniIndex.get(numP);
//                    int green = listGreen.get(numP);
//                    int yellow = listYellow.get(numP);
//                    int orange = listOrange.get(numP);
//                    int red = listRed.get(numP);
//                    int snpFlag = listSNPFlag.get(numP);
//                    int iniBackFlag = listIniBackFlag.get(numP);
////                    int flag = 0;
//
////                    if(snpFlag == true){
////                        flag = 1;
////                    }
//
//                    String strand = listStrand.get(numP);
//                    int matchCount = green+yellow+orange+red;
//                    int missingMer = 0;
//                    
////                    if(snpFlag!=0){         
////                        missingMer = (snpFlag+dummySS.getMerLength())-1;
////                        matchCount = matchCount+missingMer;
////                    }
//                    /**
//                     * Cut this pat out because we already do it in joinPeakV2
//                     * we add missing mer into red color count
//                     */
////                    if(snpFlag!=0){         
////                        missingMer = snpFlag;
////                        matchCount = matchCount+missingMer;
////                    }
//                    
////                    if(dummySS.getReadName().equals("Read12SS07")){
////                        System.out.println();
////                    }
//                    if(strand.equals("-")){
//                        iniIndex = dummySS.getReadLength() - (iniIndex+(dummySS.getMerLength()+matchCount-1));
//    //                    stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
//                    }
////                    int merMatch = green+yellow+orange+red;
//    //                ps.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName);
//    //                ps.format("\n");
//                    if(option1.equals("gy")){
//                        if(orange==0&&red==0){
//                            writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength));
//                            writer.write("\n");
//                        }
//                    }else if(option1.equals("g")){
//                        if(orange==0&&red==0&&yellow==0){
//                            writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength));
//                            writer.write("\n");
//                        }
//                    }else if(option1.equals("gyo")){
//                        if(red==0){
//                            writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength));
//                            writer.write("\n");
//                        }
//                    }else if(option1.equals("all")){
//                        writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d", numChr,iniPos,lastPos,green,yellow,orange,red,strand,iniIndex,readName,snpFlag,iniBackFlag,readLength));
//                        writer.write("\n");
//                    }
//
//                }
//            }
//        }
//        writer.flush();
//        writer.close();      
//        }
//    }
    
}
