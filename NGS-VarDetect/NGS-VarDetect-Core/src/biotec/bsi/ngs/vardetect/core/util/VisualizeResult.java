/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.core.util;

import biotec.bsi.ngs.vardetect.core.AlignmentResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author worawich
 */
public class VisualizeResult {
    
    private static long merIndex = 0;
    private static long countChr = 0;
    private static long alignPos = 0;
    private static long countAlign = 0;
    private static long alignPosCode;
    private static long mask = 268435455;
    
    public static void visualizeAlignmentResult(AlignmentResult inRes){
        Map<String,ArrayList<Map>> frontMapRes = inRes.getAlignmentResult();
        Map<Long,Long> chrIndex = new HashMap();
        Map<Long,Long> matchResult = new HashMap();
        
        Set set = frontMapRes.keySet();
        Iterator iter = set.iterator();
        
        while (iter.hasNext()){                                     //Loop: each Read name
            Object rdName = iter.next();
            ArrayList<Map> merMap = frontMapRes.get(rdName);                        
            System.out.println("\nThis is result check(read name) : "+rdName);
                        
            long array_size = merMap.size();
            //System.out.println("This is size check for number of mer in current read size is : " + array_size);
            Map<Long,long[]> subMap = new HashMap();
            for(int i = 0;i<array_size;i++){                        //Loop to get each mer map
                subMap = merMap.get(i);
                //System.out.println("Check subMap is emty? : "+subMap.isEmpty());
                Set setMer = subMap.keySet();
                Iterator iterMer = setMer.iterator();
                //System.out.println("check Key size: "+setMer.size());
                if(subMap.isEmpty()){
                    
                    //System.out.println("subMap is empty");
                }else{
                    while(iterMer.hasNext()){                       //Loop to get each arraylist of match in each mer
                        System.out.print("\n");
                        Object dum = iterMer.next();
                        //System.out.println("Key is: "+dum);
                        long[] codePos = subMap.get(dum);
                        //System.out.println("CodePos: "+ codePos + " "+codePos.length);
                        //System.out.println("YOYO CheckCheck");
                        System.out.print("Mer code: "+ dum +"codePos Size:" + codePos.length);

                        for (int j=0;j<codePos.length;j++){         //Loop to get each match in mer ส่วนใหญ่ตรงนี้จะทำรอบเดียวถ้าทำหลายรอบแสดงว่า mer นี้มีซำ้ใน chr เดิมหลายอัน

                            long chrnumber = codePos[j]>>28;
                            long position = codePos[j]&268435455;
                            
                            //////////
                            if(chrIndex.containsKey(chrnumber)){ // Other time of each chrnumber
                                countChr++;
                                merIndex++;
                                chrIndex.put(chrnumber, countChr);
                                
                                alignPos = position-merIndex;
                                alignPosCode = (chrnumber<<28)+alignPos;
                                
                                if (matchResult.containsKey(alignPosCode)){
                                    countAlign++;
                                    matchResult.put(alignPos, countAlign);
                                }else{
                                    countAlign = 1;
                                    matchResult.put(alignPosCode, countAlign);                                    
                                }     
                            }
                            else{ // First time of each chrnumber
                                countChr = 1;
                                merIndex = 0;
                                chrIndex.put(chrnumber, countChr);
                                alignPos = position-merIndex;
                                alignPosCode = (chrnumber<<28)+alignPos;
                                if (matchResult.containsKey(alignPosCode)){
                                    countAlign++;
                                    matchResult.put(alignPos, countAlign);
                                }else{
                                    countAlign = 1;
                                    matchResult.put(alignPosCode, countAlign);                                    
                                }                               
                               
                            }
                            ////////////
                            //function(chrnumber,position)

                            //System.out.println("\tchr: "+chrnumber + "\tPosition: "+position);
                        }
                    
                    
                    }
                }
                
            }
            
            
        }
        //SefrontMapRes
        
        //frontMapRes.get(inRes);
    }
    
    public static void visualizeAlignmentResultV2(AlignmentResult inRes){
        Map<String,Map<Long,ArrayList<Long>>> readMapResult = inRes.getAlignmentResultV2();
        Map<Long,Long> chrIndex = new HashMap();
        Map<Long,Long> matchResult = new HashMap();
        long index;
        Set set = readMapResult.keySet();
        Iterator iter = set.iterator();
        
        while (iter.hasNext()){  
            Object rdName = iter.next();
            Map<Long,ArrayList<Long>> merMap = readMapResult.get(rdName);                        
            System.out.println("\nThis is result check(read name) : "+rdName);
            
            Set setMer = merMap.keySet();
            Iterator iterMer = setMer.iterator();
            index = 0;
            while (iterMer.hasNext()){                
                Object merCode = iterMer.next();
                ArrayList<Long> codePos = merMap.get(merCode);
                System.out.print("\n");
                System.out.print("Mer code: "+merCode +"Mer seauence: "+((long)merCode>>28)+"codePos Size:" + codePos.size());
                
                for (int i=0;i<codePos.size();i++){
                    
                    long chrnumber = codePos.get(i)>>28;
                    long position = codePos.get(i)&268435455;
                    long alignPosV2 = codePos.get(i)-index;
                    long alignPos = position-index;
                    
                    System.out.println("\tchr: "+chrnumber + "\tPosition: "+position + "\tcode: "+codePos.get(i)+"\tAlign at: "+alignPos);
                    //System.out.println("\tchrV2: "+(alignPosV2>>28) + "\tPositionV2: "+(alignPosV2&268435455) + "\tcodeV2: "+codePos.get(i)+"\tAlign atV2: "+(alignPosV2&268435455));
                    
                    
                }
                index++;
            }
            
        }
    }
    
    public static void visualizeAlignmentCountMatch(AlignmentResult inRes){
        Map<String,Map<Long,Long>> readList = inRes.getAlignmentCount();
        
        Set allKey = readList.keySet();
        Iterator iterRead = allKey.iterator();
        
        while(iterRead.hasNext()){
            Object readName = iterRead.next();
            Map<Long,Long> countMap =  readList.get(readName);
            System.out.println("Alignment result of "+ readName);
            
            Set allPos = countMap.keySet();
            Iterator iterPos = allPos.iterator();
            while(iterPos.hasNext()){
                long positionCode = (long)iterPos.next();
                long alignPos = positionCode&mask;
                long chrNumber = positionCode>>28;
                long numCount = countMap.get(positionCode);
                
                System.out.println("Align at position: \t" + alignPos + " \ton chrNumber: " + chrNumber + " \tAlign count: " + numCount);
            }
        }       
        
    }
    
    public static void visualizeAlignmentCountMatchPlusColor(AlignmentResult inRes){
        Map<String,Map<Long,long[]>> readList = inRes.getAlignmentCountPlusColor();
        
        Set allKey = readList.keySet();
        Iterator iterRead = allKey.iterator();
        
        while(iterRead.hasNext()){
            Object readName = iterRead.next();
            Map<Long,long[]> countMap =  readList.get(readName);
            System.out.println("Alignment result of "+ readName);
            
            Set allPos = countMap.keySet();
            Iterator iterPos = allPos.iterator();
            while(iterPos.hasNext()){
                long positionCode = (long)iterPos.next();
                long alignPos = positionCode&mask;
                long chrNumber = positionCode>>28;
                long[] numCountPlusColor = countMap.get(positionCode);
                long numCount = numCountPlusColor[0];
                long red = numCountPlusColor[1];
                long yellow = numCountPlusColor[2];
                long orange = numCountPlusColor[3];
                long green = numCountPlusColor[4];
                
//                System.out.println("Align at position: \t%d" + alignPos + " \ton chrNumber: " + chrNumber + " \tAlign count: " + numCount + " \tNumber of Red: " + red + " \tNumber of Yellow: " + yellow + " \tNumber of Orange: " + orange + " \tNumber of Green" + green);
                System.out.format("Align at position: %d\tOn chrNumber: %3d\tAlign count: %3d\tNumber of Red: %3d\tNumber of Yellow: %3d\tNumber of Orange: %3d\tNumber of Green: %3d%n",alignPos,chrNumber,numCount,red,yellow,orange,green);
            }
        }       
        
    }
    
    public static void countMatch(){
        
        
    }
    
}
