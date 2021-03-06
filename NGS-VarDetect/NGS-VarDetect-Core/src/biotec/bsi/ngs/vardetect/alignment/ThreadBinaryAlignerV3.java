/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.alignment;

import biotec.bsi.ngs.vardetect.core.EncodedSequence;
import biotec.bsi.ngs.vardetect.core.MerRead;
import biotec.bsi.ngs.vardetect.core.ShortgunSequence;
import biotec.bsi.ngs.vardetect.core.util.SequenceUtil;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author worawich
 * 
 * Multi-Thread Instruction:
 * 
 * This object will be call in BinaryAlingner on function alignMultithread()
 * 
 * The point that we must separately define new object for multi-thread implement is:
 *  we want to store result or some variable and make it capable to access later 
 *  So we store the result in this object
 *  in order to give the potential of this object to be run in multi-thread 
 *  we Implement Runnable and over ride method run() by put your code tat you want to run it in multi-thread
 *  Next, we have to create some method like start() to create a thread object and start it 
 *  Keep note that when you want to re-run the thread again you have to make sure that the old thread is finish running
 *  by using join() method (the program will stuck at the join() method until all code in run() have done execute)
 *  After that you have to re create the new thread object and start it again.
 *  Don;t forget to access the same object that in case that you have some relative result that you want to store it continuously 
 *  
 * 
 */
public class ThreadBinaryAlignerV3 implements Runnable {
    private Thread t;
    private String threadName;
    private List inputSequence;
    private EncodedSequence encodedRef;
    private long chrNum;
    private int numMer;
    private int threshold;                          // It is a minimum number of count that we accept
    private Map<Long,Long> alignMap;
    private Map<Long,ArrayList<Integer>> alnMerMap;     // Key is align code [strand|alignposition] and value is mer code
    private Map<String,ArrayList<Long>> alnRes;      // Key is ReadName and value is array of long [count|chr|strand|Pos]
    String flag;
            
    
    public ThreadBinaryAlignerV3(String name,List inSeq, EncodedSequence inEncodeRef,long inchr, int inMer , int inThreshold){
        threadName = name;
        inputSequence = inSeq;
        encodedRef = inEncodeRef;
        chrNum = inchr;
        numMer = inMer;
        alnRes = new LinkedHashMap();
        threshold = inThreshold;
        
        System.out.println("Creating " + threadName);
    }
    
    public void setdata(List inSeq, EncodedSequence inEncodeRef,long inchr, int inMer){    
        inputSequence = inSeq;
        encodedRef = inEncodeRef;
        chrNum = inchr;
        numMer = inMer;
    }
    
    @Override
    public void run(){
        
        System.out.println("Start " + threadName);
        System.out.println("Number of read : " + inputSequence.size());
        /* Alignment algorithm */
        Iterator seqs = inputSequence.iterator();
        while(seqs.hasNext()){                                              // Loop over ShortgunSequence contain in InputSequence 
            boolean skipRead = false;        
            ShortgunSequence seq = (ShortgunSequence)seqs.next();
            this.alnMerMap = new LinkedHashMap();                                         // initialize this hashmap every time when start new loop of Shortgun Read

//                    System.out.println(""+chr.getName()+" "+encoded.getMers().length);

            String s = seq.getSequence();                                           // get String sequence of selected ShortgunSequence

//                    System.out.print(chr.getName()+" + strand\t");           

            Map<Long,Long> alnCodeCheckList = new HashMap();                    // This map is a checklist for alncode to indicate the iniIndex Map<Long,Long> => Map<alnCode,iniIndex>
            /* NewPart */
            long oldIniIdx = 0;
            long newIniIdx = 0;
            long iniIndex = 0;
            long recentIdx = 0;
            boolean firstMatchCheck = false;
            /************/
            for(int i=0;i<(s.length()-numMer)+1;i++){                                  // (Windowing with one stepping) for loop over String sequence which has limit round at (string length - mer length) + one [maximum possible mer sequence]
                int index = i;
                String sub = s.substring(i, i+numMer);                                 // cut String sequence into sub string sequence (mer length long) 
                
                if(sub.toUpperCase().equals("AAAAAAAAAAAAAAAAAA")||sub.toUpperCase().equals("TTTTTTTTTTTTTTTTTT")||sub.toUpperCase().equals("GGGGGGGGGGGGGGGGGG")||sub.toUpperCase().equals("CCCCCCCCCCCCCCCCCC")){
                    skipRead = true;
                    break;
                }

//System.out.println("check sub length"+sub.length());
                long m = SequenceUtil.encodeMer(sub, numMer);                          // encode sub string sequence (code is 36 bit max preserve the rest 28 bit for position)
//                        System.out.println(""+sub+" "+sub.length()+": "+m);
                
                
                if(m!=-1){                                                          
                    m = m<<28;                                                      // shift left 28 bit for optimization binary search purpose 
//                            long pos = encoded.align(m);
                    long pos2[] = encodedRef.align2(m);                                // Do alignment with binary search (pos2[] cantain 64 bit long [mer code | position])
//                            long pos2[] = encoded.fullAlign(m);
                    long pos = -1;
                    if(pos2!=null&&pos2.length>0){
                        pos = pos2[0];
                        pos = pos2.length;
                        //merMap = res.addResult(m, chr.getChrNumber(), pos2);
                    }

                    int idx = (int) (pos-i);
                    if(pos<0){
                      idx = 0;
                    }
                         
                    int totalMer = (seq.getShortgunLength()-numMer)+1;

                    /*************************************************************************************************************/
                    /* -------------------------New Implement Part (Not Stroe in object)---------------------------------------------*/
                    
                    if(pos2 != null){
                        //if(pos2.length == 1){                // (Not work) already check for repeat in same chromosome by checking alignment result must have one match result in this chromosome
                        /* Old Part *//*
                        for(int j=0;j<pos2.length;j++){
                            long alnCode = pos2[j] - index;     // pos is 29 bit [strand|position] ; algncode is 29 bit [strand|alignPosition]


                            if(alnCodeCheckList.containsKey(alnCode)){
                                long iniIndex = alnCodeCheckList.get(alnCode);

                                long indexAlnCode = (iniIndex<<29)+alnCode;                 // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)

                                ArrayList<Long> merList = this.alnMerMap.get(indexAlnCode);
                                merList.add(m);
                                this.alnMerMap.put(indexAlnCode, merList);

                            }else{
                                long iniIndex = index;
                                alnCodeCheckList.put(alnCode, iniIndex);

                                long indexAlnCode = (iniIndex<<29)+alnCode;                  // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)

                                ArrayList<Long> merList = new ArrayList();
                                merList.add(m);
                                this.alnMerMap.put(indexAlnCode,merList);

                            }
//                                    merList = null;
//                                    System.gc();                                       
                        }
                        */
                        
                        
                        /******** New Part (fixed wrong mer count) Version 3 **********/
                        for(int j=0;j<pos2.length;j++){
                            long alnCode = pos2[j] - index;     // pos is 29 bit [strand|position] ; algncode is 29 bit [strand|alignPosition] but already subtract index (offset)


                            if(alnCodeCheckList.containsKey(alnCode)){

                                iniIndex = alnCodeCheckList.get(alnCode);

                                long indexAlnCode = (iniIndex<<29)+alnCode;                 // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)

                                ArrayList<Integer> merList = this.alnMerMap.get(indexAlnCode);

                                /**
                                 * Case check to solve the problem. In case, when position-index is the same value but actually it different peak.
                                 * To check continuity of this alnCode. We reserve index 0 of merList to store the recent index.
                                 * Check continuity of index from different between recent index and current index.
                                 */

                                if(index-merList.get(0)==1){                                // Case check to solve the problem. In case, when position-index is the same value but actually it different peak
                                    iniIndex = alnCodeCheckList.get(alnCode);

                                    indexAlnCode = (iniIndex<<29)+alnCode;                 // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)
                                    merList.remove(0);
                                    merList.add(0,index);
                                    merList.add(1);
                                    this.alnMerMap.put(indexAlnCode, merList);
                                }else{
                                    iniIndex = index;
                                    alnCodeCheckList.put(alnCode, iniIndex);

                                    indexAlnCode = (iniIndex<<29)+alnCode;                  // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)

                                    merList = new ArrayList();
                                    merList.add(0,index);
                                    merList.add(1);
                                    this.alnMerMap.put(indexAlnCode,merList);
                                }
                                /*******************/

                            }else{
                                iniIndex = index;
                                alnCodeCheckList.put(alnCode, iniIndex);

                                long indexAlnCode = (iniIndex<<29)+alnCode;                  // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)

                                ArrayList<Integer> merList = new ArrayList();
                                merList.add(0,index);
                                merList.add(1);
                                this.alnMerMap.put(indexAlnCode,merList);

                            }
//                                    merList = null;
//                                    System.gc();                                       
                        }

                        /***************************************************************/
                        
                        
                        /******** New Part **********/
                                
                        /* Specify iniIdx check from continueously of index */
//                        newIniIdx = index;
//                        if(firstMatchCheck==false){
//                            // it's first time
//                            iniIndex = index;
//                            oldIniIdx = index;
//                            firstMatchCheck = true;
//                        }else{
//                            if(newIniIdx-recentIdx==1){
//                                iniIndex = oldIniIdx;
//                            }else{
//                                iniIndex = index;
//                                oldIniIdx = index;
//                            }
//                        }
//
//                        for(int j=0;j<pos2.length;j++){
//                            long alnCode = pos2[j] - index;     // pos is 29 bit [strand|position] ; algncode is 29 bit [strand|alignPosition]
//                            long indexAlnCode = (iniIndex<<29)+alnCode;                 // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)
//                            //** Line 1202 got strange result it shoul produce 24477283769
//                            if(alnMerMap.containsKey(indexAlnCode)){
//                                ArrayList<Long> merList = this.alnMerMap.get(indexAlnCode);
//                                merList.add(m);
//                                this.alnMerMap.put(indexAlnCode, merList);
//
//                            }else{
//                                ArrayList<Long> merList = new ArrayList();
//                                merList.add(m);
//                                this.alnMerMap.put(indexAlnCode,merList);                                        
//                            }   
//                        }
//
//                        /****************************/
//                        recentIdx = index;
                    }

                    /*-----------------------------------------------------------------------------------------------------------*/
                    /*************************************************************************************************************/

                }
            //System.out.println(" This mer Map check: "+ (merMap == null));
            //res.createMap(seq.getReadName(), merMap);
                
            }

            /*************************************************************************************************************/
            /* -------------------------New Implement Part Cons. (Not Stroe in object)---------------------------------------------*/
            
            if(this.alnRes.containsKey(seq.getReadName())&&skipRead==false){                     // Check for existing of ReadName (if exist put result code on existing ArrayList<Long>
                ArrayList<Long> countChrIdxStrandAlnList = this.alnRes.get(seq.getReadName()); //get existing Arraylist
                Set keySet = this.alnMerMap.keySet();
                Iterator keyIter =keySet.iterator();
                while(keyIter.hasNext()){
                    long idxStrandAln = (long)keyIter.next();                      // strandAln has 29 bit compose of [strand|alignPosition]
                    long count = this.alnMerMap.get(idxStrandAln).size()-1;          // we can get number of count from number of member in merList and should minus with 1 (because index 0 has been reseve for checking index continuity)
                    long chrIdxStrandAln = (chrNum<<37)+idxStrandAln;     // shift left 37 bit beacause we want to add count number on the front of strandAln which has 37 bit
                    long countChrIdxStrandAln = (count<<42)+chrIdxStrandAln;          // shift left 42 bit beacause we want to add count number on the front of chrStrandAln which has 42 bit 
                    
                    if(count>=threshold){                                            // case check to filter small count peak out (use user specify threshold)
                        countChrIdxStrandAlnList.add(countChrIdxStrandAln);
                    }
                }
                this.alnRes.put(seq.getReadName(), countChrIdxStrandAlnList);
            }else if(this.alnRes.containsKey(seq.getReadName())==false&&skipRead==false){
                ArrayList<Long> countChrIdxStrandAlnList = new ArrayList();
                Set keySet = this.alnMerMap.keySet();
                Iterator keyIter =keySet.iterator();
                while(keyIter.hasNext()){
                    long idxStrandAln = (long)keyIter.next();                      // strandAln has 29 bit compose of [strand|alignPosition]
                    long count = this.alnMerMap.get(idxStrandAln).size()-1;          // we can get number of count from number of member in merList and should minus with 1 (because index 0 has been reseve for checking index continuity)
                    long chrIdxStrandAln = (chrNum<<37)+idxStrandAln;     // shift left 37 bit beacause we want to add count number on the front of strandAln which has 37 bit
                    long countChrIdxStrandAln = (count<<42)+chrIdxStrandAln;          // shift left 42 bit beacause we want to add count number on the front of chrStrandAln which has 42 bit 
                    
                    if(count>=threshold){                                              // case check to filter small count peak out (use user specify threshold)
                        countChrIdxStrandAlnList.add(countChrIdxStrandAln);
                    }
                }
                this.alnRes.put(seq.getReadName(), countChrIdxStrandAlnList);
            }

            /* Finish one read clear all data */
//                    this.alnMerMap = null;
//                    System.gc();
            /*-----------------------------------------------------------------------------------------------------------*/
            /*************************************************************************************************************/

//                     System.out.println();
            /* New Implement Part */
            //seq.countAlignmentData(); // Create Alignment count data before change ShortgunSequence

            /*--------------------*/
        }

        /*-------------------- Do compliment alignment -------------------------------*/
        /* Do the same algorithm but use function for compliment */
        Iterator seqsComp = inputSequence.iterator();
        while(seqsComp.hasNext()){
            boolean skipRead = false;
            ShortgunSequence seq = (ShortgunSequence)seqsComp.next();                                  // get ShortgunSequence from InputSequence
            this.alnMerMap = new LinkedHashMap();

//                    System.out.println(""+chr.getName()+" "+encoded.getMers().length);

            String s = seq.getSequence();                                                   // get sequence form ShortgunSequence
            String invSeq = SequenceUtil.inverseSequence(s);                                // Do invert sequence (ATCG => GCTA)
            String compSeq = SequenceUtil.createComplimentV2(invSeq);                       // Do compliment on invert sequence (GCTA => CGAT)  
//                    System.out.println("******Input Sequence check " + compSeq);
//                    System.out.print(chr.getName()+" - strand\t"); 

            Map<Long,Long> alnCodeCheckList = new HashMap();                    // This map is a checklist for alncode to indicate the iniIndex Map<Long,Long> => Map<alnCode,iniIndex>
            /* NewPart */
            long oldIniIdx = 0;
            long newIniIdx = 0;
            long iniIndex = 0;
            long recentIdx = 0;
            boolean firstMatchCheck = false;
            /************/
            for(int i=0;i<(compSeq.length()-numMer)+1;i++){                                    // Windowing
                int index = i;                                                              // index at aligncompliment and non compliment is not different. It not effect any thing. we just know strand notation is enough
                String sub = compSeq.substring(i, i+numMer);
                
                if(sub.toUpperCase().equals("AAAAAAAAAAAAAAAAAA")||sub.toUpperCase().equals("TTTTTTTTTTTTTTTTTT")||sub.toUpperCase().equals("GGGGGGGGGGGGGGGGGG")||sub.toUpperCase().equals("CCCCCCCCCCCCCCCCCC")){
                    skipRead = true;
                    break;
                }
                
                //System.out.println("check sub length"+sub.length());
                long m = SequenceUtil.encodeMer(sub, numMer);
//                        System.out.println(""+sub+" "+sub.length()+": "+m);
                if(m!=-1){
                    m = m<<28;
//                            long pos = encoded.align(m);
                    long pos2[] = encodedRef.align2ComplimentV2(m);                            // Do alignment by alignment function specific for compliment sequence
//                            long pos2[] = encoded.fullAlign(m);
                    long pos = -1;
                    if(pos2!=null&&pos2.length>0){
                        pos = pos2[0];
                        pos = pos2.length;
                        //merMap = res.addResult(m, chr.getChrNumber(), pos2);
                    }

                    int idx = (int) (pos-i);
                    if(pos<0){
                      idx = 0;
                    }

                    int totalMer = (seq.getShortgunLength()-numMer)+1;

                    /*************************************************************************************************************/
                    /* -------------------------New Implement Part (Not Stroe in object)---------------------------------------------*/
                                        
                    if(pos2 != null){
//                      if(pos2.length == 1){           // (Not work) already check for repeat in same chromosome by checking alignment result must have one match result in this chromosome
//                        for(int j=0;j<pos2.length;j++){
//                        long alnCode = pos2[j] - index;     // pos is 29 bit [strand|position] ; algncode is 29 bit [strand|alignPosition]
//
//                               if(alnCodeCheckList.containsKey(alnCode)){
//                                long iniIndex = alnCodeCheckList.get(alnCode);
//
//                                long indexAlnCode = (iniIndex<<29)+alnCode;                 // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)
//
//                                ArrayList<Long> merList = this.alnMerMap.get(indexAlnCode);
//                                merList.add(m);
//                                this.alnMerMap.put(indexAlnCode, merList);
//
//                            }else{
//                                long iniIndex = index;
//                                alnCodeCheckList.put(alnCode, iniIndex);
//
//                                long indexAlnCode = (iniIndex<<29)+alnCode;                  // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)
//
//                                ArrayList<Long> merList = new ArrayList();
//                                merList.add(m);
//                                this.alnMerMap.put(indexAlnCode,merList);
//
//                            }
//                        }
                        

                        /******** New Part (fixed wrong mer count) Version 3 **********/
                        for(int j=0;j<pos2.length;j++){
                            long alnCode = pos2[j] - index;     // pos is 29 bit [strand|position] ; algncode is 29 bit [strand|alignPosition]


                            if(alnCodeCheckList.containsKey(alnCode)){

                                iniIndex = alnCodeCheckList.get(alnCode);

                                long indexAlnCode = (iniIndex<<29)+alnCode;                 // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)

                                ArrayList<Integer> merList = this.alnMerMap.get(indexAlnCode);

                                /**
                                 * Case check to solve the problem. In case, when position-index is the same value but actually it different peak.
                                 * To check continuity of this alnCode. We reserve index 0 of merList to store the recent index.
                                 * Check continuity of index from different between recent index and current index.
                                 */

                                if(index-merList.get(0)==1){                                // Case check to solve the problem. In case, when position-index is the same value but actually it different peak
                                    iniIndex = alnCodeCheckList.get(alnCode);

                                    indexAlnCode = (iniIndex<<29)+alnCode;                 // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)
                                    merList.remove(0);
                                    merList.add(0,index);
                                    merList.add(1);
                                    this.alnMerMap.put(indexAlnCode, merList);
                                }else{
                                    iniIndex = index;
                                    alnCodeCheckList.put(alnCode, iniIndex);

                                    indexAlnCode = (iniIndex<<29)+alnCode;                  // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)

                                    merList = new ArrayList();
                                    merList.add(0,index);
                                    merList.add(1);
                                    this.alnMerMap.put(indexAlnCode,merList);
                                }
                                /*******************/

                            }else{
                                iniIndex = index;
                                alnCodeCheckList.put(alnCode, iniIndex);

                                long indexAlnCode = (iniIndex<<29)+alnCode;                  // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)

                                ArrayList<Integer> merList = new ArrayList();
                                merList.add(0,index);
                                merList.add(1);
                                this.alnMerMap.put(indexAlnCode,merList);

                            }
//                                    merList = null;
//                                    System.gc();                                       
                        }

                        /***************************************************************/



                        /******** New Part **********/
                                
                        /* Specify iniIdx check from continueously of index */
//                        newIniIdx = index;
//                        if(firstMatchCheck==false){
//                            // it's first time
//                            iniIndex = index;
//                            oldIniIdx = index;
//                            firstMatchCheck = true;
//                        }else{
//                            if(newIniIdx-recentIdx==1){
//                                iniIndex = oldIniIdx;
//                            }else{
//                                iniIndex = index;
//                                oldIniIdx = index;
//                            }
//                        }
//
//                        for(int j=0;j<pos2.length;j++){
//                            long alnCode = pos2[j] - index;     // pos is 29 bit [strand|position] ; algncode is 29 bit [strand|alignPosition]
//                            long indexAlnCode = (iniIndex<<29)+alnCode;                 // indexAlnCode has 37 bit [iniIndex|Strand|Position] iniIndex(8bit),Strnd(1bit),Position(28bit)
//                            //** Line 1202 got strange result it shoul produce 24477283769
//                            if(alnMerMap.containsKey(indexAlnCode)){
//                                ArrayList<Long> merList = this.alnMerMap.get(indexAlnCode);
//                                merList.add(m);
//                                this.alnMerMap.put(indexAlnCode, merList);
//
//                            }else{
//                                ArrayList<Long> merList = new ArrayList();
//                                merList.add(m);
//                                this.alnMerMap.put(indexAlnCode,merList);                                        
//                            }   
//                        }
//
//                        /****************************/
//                                
//                        recentIdx = index;
                    }

                    /*-----------------------------------------------------------------------------------------------------------*/
                    /*************************************************************************************************************/
                }
            //System.out.println(" This mer Map check: "+ (merMap == null));
            //res.createMap(seq.getReadName(), merMap);
                
            }

            /*************************************************************************************************************/
            /* -------------------------New Implement Part Cons. (Not Stroe in object)---------------------------------------------*/
            
            if(this.alnRes.containsKey(seq.getReadName())&&skipRead==false){                     // Check for existing of ReadName (if exist put result code on existing ArrayList<Long>

                ArrayList<Long> countChrIdxStrandAlnList = this.alnRes.get(seq.getReadName()); //get existing Arraylist
                Set keySet = this.alnMerMap.keySet();
                Iterator keyIter =keySet.iterator();
                while(keyIter.hasNext()){
                    long idxStrandAln = (long)keyIter.next();                      // strandAln has 37 bit compose of [iniIndex|strand|alignPosition]
                    long count = this.alnMerMap.get(idxStrandAln).size()-1;          // we can get number of count from number of member in merList and should minus with 1 (because index 0 has been reseve for checking index continuity)
                    long chrIdxStrandAln = (chrNum<<37)+idxStrandAln;     // shift left 37 bit beacause we want to add chr number on the front of strandAln which has 37 bit
                    long countChrIdxStrandAln = (count<<42)+chrIdxStrandAln;          // shift left 42 bit beacause we want to add count number on the front of chrStrandAln which has 42 bit

                    if(count>=threshold){                                            // case check to filter small count peak out (use user specify threshold)
                        countChrIdxStrandAlnList.add(countChrIdxStrandAln);
                    }
                }
                this.alnRes.put(seq.getReadName(), countChrIdxStrandAlnList);
            }else if(this.alnRes.containsKey(seq.getReadName())==false && skipRead==false){
                ArrayList<Long> countChrIdxStrandAlnList = new ArrayList();
                Set keySet = this.alnMerMap.keySet();
                Iterator keyIter =keySet.iterator();
                while(keyIter.hasNext()){
                    long idxStrandAln = (long)keyIter.next();                      // strandAln has 37 bit compose of [iniIndex|strand|alignPosition]
                    long count = this.alnMerMap.get(idxStrandAln).size()-1;          // we can get number of count from number of member in merList and should minus with 1 (because index 0 has been reseve for checking index continuity)
                    long chrIdxStrandAln = (chrNum)+idxStrandAln;     // shift left 37 bit beacause we want to add chr number on the front of strandAln which has 37 bit
                    long countChrIdxStrandAln = (count<<42)+chrIdxStrandAln;          // shift left 42 bit beacause we want to add count number on the front of chrStrandAln which has 42 bit 
                    
                    if(count>=threshold){                                                // case check to filter small count peak out (use user specify threshold)
                        countChrIdxStrandAlnList.add(countChrIdxStrandAln);
                    }
                }
                this.alnRes.put(seq.getReadName(), countChrIdxStrandAlnList);
            }

            /*-----------------------------------------------------------------------------------------------------------*/
            /*************************************************************************************************************/

            /* New Implement Part */
            //seq.countAlignmentData(); // Create Alignment count data before change ShortgunSequence

            /*--------------------*/
        }
        
        //System.out.println("Thread-"+this.threadName+" : stop");
        this.flag = "run() is Done";
                
    }
    
    public void start(){
        
        t = new Thread (this,threadName);
        t.start();
        this.flag = "run() is running";
        System.out.println("Starting " + threadName + " : " + this.flag);
       
    }
    
    public void join() throws InterruptedException{ 
        t.join();
        System.out.println("Thread-"+this.threadName+" : stop" + " : " + this.flag); 
    }
    
    public Map<String,ArrayList<Long>> getMapResult(){
        return this.alnRes;
    }
        
}
