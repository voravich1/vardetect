/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author soup
 */
public class AlignmentResult {
    
    Map<Long,long[]> merPosMap;
    Map<Long,ArrayList<Long>> merPosMapV2;
    Map<String,ArrayList<Map>> resultMap;
    Map<String,Map<Long,ArrayList<Long>>> resultMapV2;
    ArrayList<Map> arrayMap;
    ArrayList<Long> listCode;
            
    Map<String,Long> result;
    
    Map<String,ArrayList> newResult;
    private long index;
    private long chrNum;
    private long code = 0;
    private long count = 0;

    ArrayList tempResult;    
    ArrayList<Long> arrayResult;
    ArrayList chrNumber;
    ArrayList alignPosition;
    ArrayList numMatch;
    ArrayList readName;

    InputSequence input;
    
    public  AlignmentResult(InputSequence input){
        this.merPosMap = new HashMap();
        this.merPosMapV2 = new LinkedHashMap();
        this.resultMap = new HashMap();
        this.resultMapV2 = new LinkedHashMap();
        this.arrayMap = new ArrayList();
        this.listCode = new ArrayList();
        
        this.input = input;  
        this.result = new HashMap();
        
        
        this.newResult = new HashMap();
        this.tempResult = new ArrayList();         
        this.arrayResult = new ArrayList();
        this.alignPosition = new ArrayList();
        this.chrNumber = new ArrayList();
        this.numMatch = new ArrayList();
        this.readName = new ArrayList();    
    }
    
    public void addResultV2(long mer, long chrNumber, long[] pos, String readName){
        
        // mer has been 28 bit left shift for matching propose. Beware when handle with mer.
        int len;
        long[] code = pos;
        long dummyCode;
        this.merPosMapV2 = new LinkedHashMap();
        
        if(this.resultMapV2.containsKey(readName)){
            this.merPosMapV2 = this.resultMapV2.get(readName);
            
            if (this.merPosMapV2.containsKey(mer)){
                this.listCode = this.merPosMapV2.get(mer);
                if(pos!=null&&pos.length>0){
                    len = pos.length;
                    if(pos[0] > 0){
                        for(int i=0;i<len;i++){
                            //dummyCode = (chrNumber<<28)+pos[i];
                            this.listCode.add(pos[i]); // pos may already contain chr number
                            //System.out.println("This is dummyCode check: mer is : " + mer + "chrNumber = "+chrNumber+" : " + "calculate back chrNumber "+ (pos[i]>>28) + "position "+ (pos[i]&268435455));
                        }
                        //System.out.println(" this is code check: " + code[0]);
                        //System.out.println(" this is mer check: " + mer);
                        //System.out.println(merPosMap == null);                
                        this.merPosMapV2.put(mer, this.listCode);                        
                    }
                }else{
                    //this.listCode = new ArrayList();
                    //this.merPosMapV2.put(mer, this.listCode);
                }
                //this.resultMapV2.put(readName, merPosMapV2);
            }else{
                this.listCode = new ArrayList();
                if(pos!=null&&pos.length>0){
                    len = pos.length;
                    if(pos[0] > 0){
                        for(int i=0;i<len;i++){
                            //dummyCode = (chrNumber<<=28)+pos[i];
                            this.listCode.add(pos[i]);
                            //System.out.println("This is dummyCode check: mer is : " + mer + "chrNumber = "+chrNumber+" : " + "calculate back chrNumber "+ (pos[i]>>28) + "position "+ (pos[i]&268435455));
                        }
                        //System.out.println(" this is code check: " + code[0]);
                        //System.out.println(" this is mer check: " + mer);
                        //System.out.println(merPosMap == null);                
                        this.merPosMapV2.put(mer, this.listCode);
                    }
                }else{
                    this.listCode = new ArrayList();
                    this.merPosMapV2.put(mer, this.listCode);
                }          
                
            }
            //this.resultMapV2.put(readName, merPosMapV2);
        }else{
            this.listCode = new ArrayList();
            if(pos!=null&&pos.length>0){
                len = pos.length;
                if(pos[0] > 0){
                    for(int i=0;i<len;i++){
                        //dummyCode = (chrNumber<<=28)+pos[i];                 
                        this.listCode.add(pos[i]);
                        System.out.println("This is dummyCode check: mer is : " + mer + "chrNumber = "+chrNumber+" : " + "calculate back chrNumber "+ (pos[i]>>28) + "position "+ (pos[i]&268435455));
                    }
                        //System.out.println(" this is code check: " + code[0]);
                        //System.out.println(" this is mer check: " + mer);
                        //System.out.println(merPosMap == null);                
                    this.merPosMapV2.put(mer, this.listCode);
                }
            }else{
                this.listCode = new ArrayList();
                this.merPosMapV2.put(mer, this.listCode);
            }
            //this.resultMapV2.put(readName, merPosMapV2);
        }
        
        this.resultMapV2.put(readName, merPosMapV2);
        /*
        if (this.merPosMap.containsKey(mer)){
            this.listCode = this.merPosMap.get(mer);
            if(pos!=null&&pos.length>0){
                len = pos.length;
                if(pos[0] > 0){
                    for(int i=0;i<len;i++){
                        dummyCode = (chrNumber<<28)+pos[i];
                        this.listCode.add(dummyCode);
                    }
                    //System.out.println(" this is code check: " + code[0]);
                    //System.out.println(" this is mer check: " + mer);
                    //System.out.println(merPosMap == null);                
                    this.merPosMap.put(mer, this.listCode);
                }
            }          
        }else{
            if(pos!=null&&pos.length>0){
                len = pos.length;
                if(pos[0] > 0){
                    for(int i=0;i<len;i++){
                        dummyCode = (chrNumber<<28)+pos[i];
                        this.listCode.add(dummyCode);
                    }
                    //System.out.println(" this is code check: " + code[0]);
                    //System.out.println(" this is mer check: " + mer);
                    //System.out.println(merPosMap == null);                
                    this.merPosMap.put(mer, this.listCode);
                }
            }          
            
        }
        */
        
    }
    
    public Map addResult(long mer, long chrNumber, long[] pos){
        
        int len;
        long[] code = pos; 
        
        
        if(pos!=null&&pos.length>0){
            len = pos.length;
            if(pos[0] > 0){
                for(int i=0;i<len;i++){
                    code[i] = (chrNumber<<28)+pos[i];
                }
                //System.out.println(" this is code check: " + code[0]);
                //System.out.println(" this is mer check: " + mer);
                //System.out.println(merPosMap == null);                
                this.merPosMap.put(mer, code);
            }
        }
        
        return this.merPosMap;
        
        /*
        this.newResult.put(readName, this.arrayResult);
        this.index = idx;
        long dummy_subcode = (idx<<8)+chrNumber;
        
        if (dummy_subcode != this.code){
           
            if (count != 0){
                
                this.arrayResult = this.newResult.get(readName);
                this.code = (dummy_subcode<<16)+count;
                this.arrayResult.add(this.code);
                
                this.newResult.put(readName,this.arrayResult);
            }
            ///// Problem: how to collect the last result because we check variable changing but there is no change for the last result
            
            this.code = dummy_subcode;
            count = 1;
            
            //tempResult.
            // Create Array to collect pre result in case that one read has more than one match
        }
        else if(dummy_subcode == this.code){
            count++;
        }
            
        */
        //result.put(readName, code)
        
    }
    
    public void createMap(String readName, Map merMap){
        if (merMap != null){
            this.merPosMap = new HashMap(); // reset map do it every time 

            if (resultMap.containsKey(readName)){
                ArrayList<Map> dummy_merMap = new ArrayList();
                dummy_merMap = resultMap.get(readName);
                dummy_merMap.add(merMap);
                resultMap.put(readName, dummy_merMap);
            }
            else{
                ArrayList<Map> dummy_merMap = new ArrayList();
                dummy_merMap.add(merMap);                 
                resultMap.put(readName,dummy_merMap);
            }  
        }
    }
    
    public Map<String,Map<Long,Long>> getAlignmentCount(){
        
        Map<String,Map<Long,ArrayList<Long>>> readMapResult = getAlignmentResultV2();
        Map<Long,Long> chrIndex = new HashMap();
        Map<Long,Long> matchResult = new HashMap();
        Map<String,Map<Long,Long>> result = new LinkedHashMap();
        long index;
        long count;
        
        Set set = readMapResult.keySet();
        Iterator iter = set.iterator();
        
        while (iter.hasNext()){  //Loop read number
            Map<Long,Long> countMap = new LinkedHashMap();
            Object rdName = iter.next();
            Map<Long,ArrayList<Long>> merMap = readMapResult.get(rdName);                        
            //System.out.println("\nThis is result check(read name) : "+rdName);
            
            Set setMer = merMap.keySet();
            Iterator iterMer = setMer.iterator();
            index = 0;
            while (iterMer.hasNext()){  //Loop mer                
                Object merCode = iterMer.next();
                ArrayList<Long> codePos = merMap.get(merCode);
                //System.out.print("\n");
                //System.out.print("Mer code: "+merCode +"Mer seauence: "+((long)merCode>>28)+"codePos Size:" + codePos.size());
                
                /*for (int i=0;i<codePos.size();i++){
                    long chrnumber = codePos.get(i)>>28;
                    long position = codePos.get(i)&268435455;
                    
                }*/
                
                for (int i=0;i<codePos.size();i++){     //Loop all match
                    
                    //long chrnumber = codePos.get(i)>>28;
                    //long position = codePos.get(i)&268435455;
                    long alignPosV2 = codePos.get(i)-index;
                    //long alignPos = position-index;
                    
                    //System.out.println("\tchr: "+chrnumber + "\tPosition: "+position + "\tcode: "+codePos.get(i)+"\tAlign at: "+alignPos);
                    //System.out.println("\tchrV2: "+(alignPosV2>>28) + "\tPositionV2: "+(alignPosV2&268435455) + "\tcodeV2: "+codePos.get(i)+"\tAlign atV2: "+(alignPosV2&268435455));
                    
                    if(countMap.containsKey(alignPosV2)){
                        count = countMap.get(alignPosV2);
                        count++;
                        countMap.put(alignPosV2, count);  
                    }else{
                        count = 1;
                        countMap.put(alignPosV2,count);
                    }  
                }
                index++;
            }
            result.put(rdName.toString(),countMap);
        }
        return result;
    }
    
    public Map<String,Map<Long,long[]>> getAlignmentCountPlusColor(){
        
        Map<String,Map<Long,ArrayList<Long>>> readMapResult = getAlignmentResultV2();
        Map<Long,Long> chrIndex = new HashMap();
        Map<Long,Long> matchResult = new HashMap();
        Map<String,Map<Long,long[]>> result = new LinkedHashMap();
        long index;
        long count;
        long green,yellow,red,orange;
        //long[] countAndColor = new long[4];
        
        Set set = readMapResult.keySet();
        Iterator iter = set.iterator();
        
        while (iter.hasNext()){  //Loop read number
            Map<Long,long[]> countMap = new LinkedHashMap(); // Map<Alignment Position, Array of (count,red,yellow,green)>
            Object rdName = iter.next();
            Map<Long,ArrayList<Long>> merMap = readMapResult.get(rdName);                        
            //System.out.println("\nThis is result check(read name) : "+rdName);
            
            Set setMer = merMap.keySet();
            Iterator iterMer = setMer.iterator();
            index = 0;
            while (iterMer.hasNext()){  //Loop mer                
                Object merCode = iterMer.next();
                ArrayList<Long> codePos = merMap.get(merCode);
                //System.out.print("\n");
                //System.out.print("Mer code: "+merCode +"Mer seauence: "+((long)merCode>>28)+"codePos Size:" + codePos.size());
                
                for (int i=0;i<codePos.size();i++){     //Loop all match
                    
                    //
                    //Function get color is here
                    int[] colorCode = detectColor(codePos,i);  // A colorCode is the array of (red,yellow,orange,green) it has three dimension
                    //System.out.println();
                    //System.out.println("This is colorCode check from function detectColor Red: "+colorCode[0]+" Yellow: "+colorCode[1]+" Green: "+colorCode[2]);
                    //
                    
                    //long chrnumber = codePos.get(i)>>28;
                    //long position = codePos.get(i)&268435455;
                    long alignPos = codePos.get(i)-index;
                    //long alignPos = position-index;
                    
                    //System.out.println("\tchr: "+chrnumber + "\tPosition: "+position + "\tcode: "+codePos.get(i)+"\tAlign at: "+alignPos);
                    //System.out.println("\tchrV2: "+(alignPosV2>>28) + "\tPositionV2: "+(alignPosV2&268435455) + "\tcodeV2: "+codePos.get(i)+"\tAlign atV2: "+(alignPosV2&268435455));
//                    System.out.println();
//                   System.out.println("This is colorCode check from function detectColor Red: "+colorCode[0]+" Yellow: "+colorCode[1]+" Orange: "+colorCode[2]+" Green: "+colorCode[3]);
                    long[] countAndColor = new long[5];
                    if(countMap.containsKey(alignPos)){         
//                        System.out.println("Align At: " + alignPosV2);
                        countAndColor = countMap.get(alignPos);                       
                        count = countAndColor[0];
                        red = countAndColor[1];
                        yellow = countAndColor[2];
                        orange = countAndColor[3];
                        green = countAndColor[4];
                        count++;                    
 //                       System.out.println("old Red: "+red);
 //                       System.out.println("old yellow: "+yellow);
 //                       System.out.println("old orange: "+orange);
 //                       System.out.println("old green: "+green);
                        
                        countAndColor[0] = count;
                        countAndColor[1] = red + colorCode[0];
                        countAndColor[2] = yellow + colorCode[1];
                        countAndColor[3] = orange + colorCode[2];
                        countAndColor[4] = green + colorCode[3];
                        
 //                       System.out.println("new Red: "+countAndColor[1]);
 //                       System.out.println("new yellow: "+countAndColor[2]);
 //                       System.out.println("new orange: "+countAndColor[3]);
 //                       System.out.println("new green: "+countAndColor[4]);
                        
 //                       System.out.println("This is countAndColor check before put to map: Align at: " + alignPosV2 +" Count = " + count + " Red: "+countAndColor[1]+" Yellow: " + countAndColor[2] + " Orange: "+countAndColor[3]+" Green: "+ countAndColor[4]);
                        countMap.put(alignPos, countAndColor);  
                    }else{
 //                       System.out.println("First time Align At: " + alignPosV2);
                        count = 1;
                        countAndColor[0] = count;
                        countAndColor[1] = colorCode[0];
                        countAndColor[2] = colorCode[1];
                        countAndColor[3] = colorCode[2];
                        countAndColor[4] = colorCode[3];
 //                       System.out.println("This is first time of countAndColor check before put to map: Align at: "+ alignPosV2 +" Count = " + count + " Red: "+countAndColor[1]+" Yellow: " + countAndColor[2] +" Orange: "+countAndColor[3]+ " Green: "+ countAndColor[4]);
                        countMap.put(alignPos,countAndColor);
                    }  
                }
                index++;
            }
            result.put(rdName.toString(),countMap);
        }
        return result;
    }
    
    public Map getAlignmentResult(){
        return this.resultMap;
    }
    
    public Map getAlignmentResultV2(){
        return this.resultMapV2;
    }
    
    public int[] detectColor(ArrayList<Long> arrayCodePos, int index){
        int[] colorCode = new int[4]; //Have four color code red = repeat in same chr; yellow = repeat with other chr; orange = repeate both same and other chr; green = unique
        int arraySize = arrayCodePos.size();
        int red = 0;
        int yellow = 0;
        int orange = 0;
        int green = 0;
        if (arraySize == 1){
            green = 1;
        }else{
            for(int i = 0; i<arraySize;i++){
                long main_chrNumber = arrayCodePos.get(index)>>28;
                long compare_chrNumber = arrayCodePos.get(i)>>28;

                if(i != index){
                    if (main_chrNumber == compare_chrNumber){
                        red = 1; 
                    }else if(main_chrNumber != compare_chrNumber){
                        yellow = 1; 
                    }
                }
            }
        }
        if (red == 1 & yellow == 1){
            red = 0;
            yellow = 0;
            orange = 1;
        }
        colorCode[0] = red;
        colorCode[1] = yellow;
        colorCode[2] = orange;
        colorCode[3] = green;
        
        return colorCode;
    }
}
