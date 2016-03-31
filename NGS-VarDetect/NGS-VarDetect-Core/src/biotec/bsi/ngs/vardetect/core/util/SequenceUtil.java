/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.core.util;

import biotec.bsi.ngs.vardetect.core.ChromosomeSequence;
import biotec.bsi.ngs.vardetect.core.EncodedSequence;
import biotec.bsi.ngs.vardetect.core.ReferenceSequence;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

/**
 *
 * @author soup
 */
public class SequenceUtil {
   
   public static ReferenceSequence  readReferenceSequence(String filename){
     
       
       
    ReferenceSequence ref = new ReferenceSequence();
    ref.setFilename(filename);
       
        
           
    Charset charset = Charset.forName("US-ASCII");
    Path path = Paths.get(filename);
    String chr = null;
//    String seq = "";
    
    StringBuffer seq = new StringBuffer();

    try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
    String line = null;
    
    while ((line = reader.readLine()) != null) {
        
        if(line.charAt(0)=='>'){
        
            if(chr!=null){
                
                System.out.println("CHR : "+chr+" Size : "+seq.length());
                
                ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
                
                ref.addChromosomeSequence(c);
                
            }
            seq = new StringBuffer();
            chr = line.substring(1,line.length());
            
        }else{
            
            seq.append(line.trim());
            
            
        }
        
    }
    
    if(seq.length()>0){
        
//        System.out.println("CHR : "+chr+" Size : "+seq.length());

        ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
                
        ref.addChromosomeSequence(c);
        
    }
    
    
    
    
    } catch (IOException x) {
        System.err.format("IOException: %s%n", x);
    }    
           
           
     
       return ref;
   }
    





 public static void extractReferenceSequence(String filename){
     
       
       
    ReferenceSequence ref = new ReferenceSequence();
    ref.setFilename(filename);
       
        
           
    Charset charset = Charset.forName("US-ASCII");
    Path path = Paths.get(filename);
    String chr = null;
//    String seq = "";
    
    StringBuffer seq = new StringBuffer();

    try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
    String line = null;
    
    while ((line = reader.readLine()) != null) {
        
        if(line.charAt(0)=='>'){
        
            if(chr!=null){
                
                System.out.println("CHR : "+chr+" Size : "+seq.length());
                
                ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
                
                c.writeToFile("FA");
                
                
                
            }
            seq = new StringBuffer();
            chr = line.substring(1,line.length());
            
        }else{
            
            seq.append(line.trim());
            
            
        }
        
    }
    
    if(seq.length()>0){
        
//        System.out.println("CHR : "+chr+" Size : "+seq.length());

        ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
       
        c.writeToFile("FA");
        
    }
    
    
    
    
    } catch (IOException x) {
        System.err.format("IOException: %s%n", x);
    }    
           
           
     
   }

 
 public static EncodedSequence encodeSerialChromosomeSequence(ChromosomeSequence chr){
       
       EncodedSequence seq = new EncodedSequence();
       
       int kmer = 20;
       int sliding = 1;
       int repeat = 0;
       
       Hashtable<Long,Long> map =new Hashtable<Long,Long>();
       
       StringBuffer sb = chr.getSequence();
       String smallb = sb.toString().toLowerCase();
       
       
       
       int n = (sb.length()-kmer)/sliding;
       
       long cmer = -1;
       
       
       long mask = 0; 
       
       for(int i =0;i<kmer;i++)mask=mask*4+3;
       
       
       System.out.println(mask);
       
       
       for(int i =0;i<n;i++){
           

          String s = sb.substring(i*sliding,i*sliding+kmer);
          String s2 = smallb.substring(i*sliding, i*sliding+kmer);
          
          long pos = i*sliding;
          
          if(s.charAt(0)!='N'&&s.compareTo(s2)!=0){
              
//          System.out.println(s+" "+s2);
          if(cmer==-1){
            cmer = encodeMer(s,kmer);
          }else{
            
            char a = smallb.charAt(i*sliding+kmer-1);
            int t =-1;
            switch(a){
                case 'a':
                    t=0; // 00
                    break;
                case 't': 
                    t=3; // 11
                    break;
                case 'c':
                    t=1; // 01 
                    break;
                case 'g':
                    t=2; // 10 
                    break;
                default : 
                    t=-1;
                break;
               
            }
            if(t>=0){
                
//                String s2 = sb.substring((i-1)*sliding,(i-1)*sliding+kmer);
//                long omer = cmer;
                
                
                cmer *= 4;
                cmer &= mask;
                cmer += t;
            
                    
//                System.out.println(""+s+" "+cmer+"\t"+s2+" "+omer+" "+t);
                
            }else{
                System.out.println(a);
                cmer = -1;
                i+=kmer;
                
                
            }
            
              
              
          }
           
          
          
           
           if(i%10000==0)System.out.println("Loop "+i*sliding+" "+repeat);
           
           if(cmer>=0){
               
               if(map.containsKey(cmer)){
                 
                  repeat ++;
                 
//                   System.out.println(s+"\t"+mer+" at "+pos+" with "+map.get(mer)); 
               }else{
                  map.put(cmer, pos);
               }
               
           }
           
           
           }
       }
       
       seq.setMap(map);
       
       
       System.out.println("Total mer :" +n);
       System.out.println("Total uniq mer :" +map.size());
       System.out.println("Total rep :" +repeat);
       
       
       return seq;
   }
 
 
 
   public static EncodedSequence encodeChromosomeSequence(ChromosomeSequence chr){
       
       EncodedSequence seq = new EncodedSequence();
       
       int kmer = 20;
       int sliding = 1;
       int repeat = 0;
       
       Hashtable<Long,Long> map =new Hashtable<Long,Long>();
       
       StringBuffer sb = chr.getSequence();
       
       int n = (sb.length()-kmer)/sliding;
       
       
       for(int i =0;i<n;i++){
           String s = sb.substring(i*sliding,i*sliding+kmer);
           long mer = encodeMer(s,kmer);
           long pos = i*sliding;
           
           if(i%100000==0)System.out.println("Loop "+i*sliding+" "+repeat);
           
           if(mer>=0){
               
               if(map.containsKey(mer)){
                 
                  repeat ++;
                 
//                   System.out.println(s+"\t"+mer+" at "+pos+" with "+map.get(mer)); 
               }else{
                  map.put(mer, pos);
               }
               
           }
       }
       
       seq.setMap(map);
       
       
       System.out.println("Total mer :" +n);
       System.out.println("Total uniq mer :" +map.size());
       System.out.println("Total rep :" +repeat);
       
       
       return seq;
   }
   
   
   public static long encodeMer(String seq_input, int kmer){
       
//       cctgtagtacagtttgaagt
       long mer = 0 ;
       int t;
       String seq = seq_input.toLowerCase();
       if(seq.compareTo(seq_input)==0)return -1;
       for(int i=0;i < kmer && i<seq.length();i++){
            char a = seq.charAt(i);
           
            switch(a){
                case 'a':
                    t=0; // 00
                    break;
                case 't': 
                    t=3; // 11
                    break;
                case 'c':
                    t=1; // 01 
                    break;
                case 'g':
                    t=2; // 10 
                    break;
                default : 
                    t=-1;
                    return -1;
               
            }
            
            if(t>=0){
                // ctag
                // c 1
                // ct 7
                // cta 28
                // ctag 114
                mer=mer*4+t;
                
            }
           
       }
       
       return mer;
   }

    public static EncodedSequence getEncodeSequence(ChromosomeSequence chr) {

        EncodedSequence encode = null;
//        System.out.println(chr.getFilePath());
        Path fp = Paths.get(chr.getFilePath()+".map");
        File f = fp.toFile();
        try{

        if(f.exists()){
       
            
            encode = new EncodedSequence();
            encode.readFromPath(chr.getFilePath(), "map");
            
            
        }else{
            encode = SequenceUtil.encodeSerialChromosomeSequence(chr);
            encode.writeToPath(chr.getFilePath(), "map");
        }
        
        }catch(Exception e){
            System.out.println("Error");
        }
        
//        return null;
        return encode;

    }
 

}