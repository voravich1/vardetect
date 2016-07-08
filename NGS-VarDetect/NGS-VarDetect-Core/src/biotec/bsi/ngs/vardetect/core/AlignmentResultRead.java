/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author worawich
 */
public class AlignmentResultRead {
    private ArrayList<ShortgunSequence> shrtRead;
    private ArrayList<ClusterGroup> clusterResult;
    long[] allClusterCodeSorted;
    long[] allClusterCode;
    private static long mask = 268435455;
    private ClusterGroup group;
    
    public AlignmentResultRead(){
        
       this.shrtRead = new ArrayList();
       this.group = new ClusterGroup();
       this.clusterResult = new ArrayList();
    }
    
    public void addResult(ShortgunSequence inRead){
        this.shrtRead.add(inRead);
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
    
    
    public void calculateEuclidientdistance(){ 
        for(int i =0;i<shrtRead.size();i++){
            double[] distanceVector = new double[shrtRead.size()];
            ShortgunSequence dummyMainSS = shrtRead.get(i);
            for(int j=0;j<shrtRead.size();j++){
                ShortgunSequence dummySubSS = shrtRead.get(j);
                distanceVector[j] = distance(dummyMainSS.getClusterVector(),dummySubSS.getClusterVector());
                
                System.out.println();
                System.out.println("DummyMainSS/"+dummyMainSS.getReadName()+" pair with DummySubSS/"+dummySubSS.getReadName()+" : distanceVector["+j+"] = "+distanceVector[j]);
                System.out.println();
            }
            System.out.println("Check before add to "+dummyMainSS.getReadName());
            for(int a=0;a<distanceVector.length;a++){
                System.out.print("\t"+distanceVector[a]);
            }
            System.out.println();
            dummyMainSS.addDistanceVector(distanceVector);
            System.out.println("Check after add to "+dummyMainSS.getReadName());
            for(int a=0;a<distanceVector.length;a++){
                System.out.print("\t"+distanceVector[a]);
            }
            System.out.println();
            
        }
        for(int i =0;i<shrtRead.size();i++){
            ShortgunSequence dummyMainSS = shrtRead.get(i);
            System.out.println("************ ReadName:"+dummyMainSS.getReadName()+" check saved vector distance ************");
            for (int check =0;check<shrtRead.size();check++){
                System.out.print("\t"+dummyMainSS.getDistanceVector()[check]);   
            }
            System.out.println();
        }
    }
    
    public double distance(long[] a, long[] b){
        double diff_square_sum = 0.0;
        for (int i = 0; i<a.length; i++){
            diff_square_sum += (a[i]-b[i]) * (a[i]-b[i]);
        }
        return Math.sqrt(diff_square_sum);
    }
    
    public void createGroupingResult(){
        long dummyCode = 0;
        long oldDummyCode = 0;
        
        this.group = new ClusterGroup();
        for(int i = 0;i<this.allClusterCodeSorted.length;i++){
            dummyCode = this.allClusterCodeSorted[i];
            for(int j =0;j<this.shrtRead.size();j++){
                ShortgunSequence dummySS = shrtRead.get(j);
                if(dummySS.getClusterCode() == dummyCode){
                    if(i == 0){
                        System.out.println(" Check : Do adding in first group (First time) i = " + i+ " : j =  "+j );
                        this.group.addShortgunRead(dummySS);
                        oldDummyCode = dummyCode;
                    }else if(i!=0 && Math.abs(dummyCode-oldDummyCode)<=100){
                        System.out.println(" Check : Do adding in group : i = " + i+ " : j =  "+j);
                        this.group.addShortgunRead(dummySS);
                        oldDummyCode = dummyCode;
                    }else if(i!=0 && Math.abs(dummyCode-oldDummyCode)>100){
                        this.clusterResult.add(this.group); // adding to array before renew it
                        System.out.println(" Check : Do create new group and add to new group : i = " + i+ " : j =  "+j);
                        
                        this.group = new ClusterGroup();
                        this.group.addShortgunRead(dummySS);
                        oldDummyCode = dummyCode;
                    }
                    
                    System.out.println(dummyCode + "\t" + dummySS.getReadName());
                }
            }
        }
        this.clusterResult.add(this.group); // adding to array (for last group)
    }
    
    public void createAllClusterCode(){
        this.allClusterCode = new long[this.shrtRead.size()];
        for(int i =0;i<this.shrtRead.size();i++){
            long dummyCode = shrtRead.get(i).getClusterCode();
            this.allClusterCode[i] = dummyCode;
        }
    }
    
    public void createAllClusterCodeSorted(){
        this.allClusterCodeSorted = new long[this.shrtRead.size()];
        for(int i =0;i<this.shrtRead.size();i++){
            long dummyCode = shrtRead.get(i).getClusterCode();
            this.allClusterCodeSorted[i] = dummyCode;
        }
        Arrays.sort(this.allClusterCodeSorted);
    }
    
    public ArrayList<ClusterGroup> getclusterResult(){
        
        return this.clusterResult;
    }
    
    public long[] getAllClusterCode(){
        
        //Arrays.sort(this.allClusterCode);
        return this.allClusterCode;
    }
    
    public long[] getAllClusterCodeSorted(){
        
        return this.allClusterCodeSorted;
    }
    
    public void writeSortedResultToPath(String path, String fa) throws FileNotFoundException, IOException {

       
        PrintStream ps = new PrintStream(path+"_AlignSortedResult."+ fa);
        
        
        for (int i=0;i<this.shrtRead.size();i++){           // Loop Mer by Mer
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
            
            Map<Long,long[]> countMap =  dummySS.getAlignmentCountSorted();
            ps.println(">Alignment result of "+ dummySS.getReadName());
            ps.printf("%-30s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
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
                long redInt = numCountPlusColor[5];
                long yellowInt = numCountPlusColor[6];
                long orangeInt = numCountPlusColor[7];
                long greenInt = numCountPlusColor[8];
                
                
                ps.format("Chr %d : Position %d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
            }
            ps.println();
        }
    }

    public void writeUnSortedResultToPath(String path, String fa) throws FileNotFoundException, IOException {

       
        PrintStream ps = new PrintStream(path+"_AlignUnSortedResult."+ fa);
        
        
        for (int i=0;i<this.shrtRead.size();i++){           // Loop Mer by Mer
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
            
            Map<Long,long[]> countMap =  dummySS.getAlignmentCount();
            ps.println(">Alignment result of "+ dummySS.getReadName());
            ps.printf("%-30s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
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
                long redInt = numCountPlusColor[5];
                long yellowInt = numCountPlusColor[6];
                long orangeInt = numCountPlusColor[7];
                long greenInt = numCountPlusColor[8];
                
                
                ps.format("Chr %d : Position %d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
            }
            ps.println();
        }
    }

    public void writeSortedCutResultToPath(String path, String fa, int threshold) throws FileNotFoundException, IOException {

        /* Must specify threshold for cut result (The result that less than threshold will be cut out)*/
        PrintStream ps = new PrintStream(path+"_AlignSortedCutResult."+ fa);
        
        
        for (int i=0;i<this.shrtRead.size();i++){           // Loop Mer by Mer
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
            
            Map<Long,long[]> countMap =  dummySS.getAlignmentCountSortedCut(threshold);
            ps.println(">Alignment result of "+ dummySS.getReadName());
            ps.printf("%-30s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s%n","Result","NumMatch","Green","Yellow","Orange","Red","GreenInt","YellowInt","OrangeInt","RedInt");
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
                long redInt = numCountPlusColor[5];
                long yellowInt = numCountPlusColor[6];
                long orangeInt = numCountPlusColor[7];
                long greenInt = numCountPlusColor[8];
                
                
                ps.format("Chr %d : Position %d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d\t%8d%n",chrNumber,alignPos,numCount,green,yellow,orange,red,greenInt,yellowInt,orangeInt,redInt);
            }
            ps.println();
        }
    }

    public void writeDistanceTableToPath(String path, String fa) throws FileNotFoundException, IOException {

       /* Must specify threshold for cut result (The result that less than threshold will be cut out)*/
        PrintStream ps = new PrintStream(path+"_DistanceTable."+ fa);
        
        ps.println("Distance Table");
        ps.format("Reads Name");
        for (int i=0;i<this.shrtRead.size();i++){           // Loop Mer by Mer
            
            ShortgunSequence dummySS = this.shrtRead.get(i);
        //--------------------------    

        //---------------------------------
 
            ps.format("\t%10s",dummySS.getReadName());
        }
        ps.println();
        for (int i=0;i<this.shrtRead.size();i++){ 
            ShortgunSequence dummySS = this.shrtRead.get(i);
            ps.print("Name: "+dummySS.getReadName());
            
            for(int j=0;j<this.shrtRead.size();j++){
                ps.format("\t%10.5f", dummySS.getDistanceVector()[j]);
            }
            ps.println();
        }   
    }    
        
    
}
