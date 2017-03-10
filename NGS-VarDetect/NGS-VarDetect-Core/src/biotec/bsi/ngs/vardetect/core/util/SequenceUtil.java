/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.core.util;

import biotec.bsi.ngs.vardetect.alignment.AlignerFactory;
import biotec.bsi.ngs.vardetect.core.Aligner;
import biotec.bsi.ngs.vardetect.core.AlignmentResultRead;
import biotec.bsi.ngs.vardetect.core.ChromosomeSequence;
import biotec.bsi.ngs.vardetect.core.ConcatenateCut;
import biotec.bsi.ngs.vardetect.core.EncodedSequence;
import biotec.bsi.ngs.vardetect.core.ReferenceSequence;
import biotec.bsi.ngs.vardetect.core.Annotation;
import biotec.bsi.ngs.vardetect.core.InputSequence;
import biotec.bsi.ngs.vardetect.core.MapResult;
import biotec.bsi.ngs.vardetect.core.ReferenceAnnotation;
import biotec.bsi.ngs.vardetect.core.SNPsample;
import biotec.bsi.ngs.vardetect.core.ShortgunSequence;
import biotec.bsi.ngs.vardetect.core.Smallindelsample;
import biotec.bsi.ngs.vardetect.core.VariationResult;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import static java.lang.Math.abs;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;


/**
 *
 * @author soup
 */
public class SequenceUtil {
    
//    lazy load chromosome
    
    public static ReferenceSequence getReferenceSequence(String filename,int mer) throws IOException {

    ReferenceSequence ref = new ReferenceSequence();
    ref.setFilename(filename);
    
    
//   has index then index file .index  : list of chromosome
//   read each chromosome then extract .fa to file and build bin

    System.out.println(ref.getPath());
    
    File index_file = new File(filename+".index");
    
    boolean create_index = false;
    boolean extract_chr = false;
    
    
    if(index_file.exists()){
        String chr= null;

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(index_file)));
        while((chr=br.readLine())!=null){
            System.out.println(chr);
            File chr_file = new File(ref.getPath()+File.separator+chr+".fa");           
            if(chr_file.exists()){
                
                System.out.println("No chr" +chr_file);
                
                ChromosomeSequence c = new ChromosomeSequence(ref,chr,null);
                ref.addChromosomeSequence(c);
                 
                 
                 
                File bin_file = new File(c.getFilePath()+".bin");
                File comp_bin_file = new File(c.getFilePath()+"_comp.bin");
                File chr_repeatfile = new File(c.getFilePath()+"_repeatMarker.bin");
                if(bin_file.exists()==false){
                    if(c.getSequence()==null){
                   
                    }
                     
                    EncodedSequence encoded = encodeSerialChromosomeSequenceV3(c,mer);
                    long[] repeatMarker = SequenceUtil.createRepeatMarkerReferenceV2(c, mer);
                    encoded.addRepeatMarker(repeatMarker);
                     
                     
                }
//                if(comp_bin_file.exists()==false){
//                    EncodedSequence encoded = encodeSerialChromosomeSequenceV3(c,mer);
//                }

                if(chr_repeatfile.exists()==false){
                    long[] repeatMarker = SequenceUtil.createRepeatMarkerReferenceV2(c, mer);
                }
                 
            }else{
                extract_chr=true;
                System.out.println("No chr" +chr_file);

            }
            
            
        } 
    }else{
        create_index = true;
    }

    
    
    
    
    if(create_index||extract_chr){
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String chr = null;
    
        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;

            while ((line = reader.readLine()) != null) {

                if(line.charAt(0)=='>'){

                    if(chr!=null){

                        System.out.println("CHR : "+chr+" Size : "+seq.length());

                        ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);

                        File chr_file = new File(c.getFilePath()+".fa");

                        if(!chr_file.exists()){
                            c.writeToFile("FA");
                        }                   

                        seq=null;
                        c.lazyLoad();

                        EncodedSequence encoded = encodeSerialChromosomeSequenceV3(c,mer);
                        long[] repeatMarker = SequenceUtil.createRepeatMarkerReferenceV2(c, mer);
                        encoded.addRepeatMarker(repeatMarker);

                        c.lazyLoad();

                        ref.addChromosomeSequence(c);


                    }
                    seq = new StringBuffer();
                    chr = line.substring(1,line.length());


                }else{

                    seq.append(line.trim());


                }

            }
    
        if(seq.length()>0){

            System.out.println("CHR : "+chr+" Size : "+seq.length());
            ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);

            File chr_file = new File(c.getFilePath()+".fa");

            if(!chr_file.exists()){
                c.writeToFile("FA");
            }     

            seq = null;

            c.lazyLoad();

            EncodedSequence encoded = encodeSerialChromosomeSequenceV3(c,mer);
            long[] repeatMarker = SequenceUtil.createRepeatMarkerReferenceV2(c, mer);
            encoded.addRepeatMarker(repeatMarker);

            c.lazyLoad();

            ref.addChromosomeSequence(c);
        }

    
    }catch (IOException x) {
        System.err.format("IOException: %s%n", x);
    }    
    
//    if(extract_chr){
//        Enumeration<ChromosomeSequence> e = ref.getChromosomes().elements();
//        while(e.hasMoreElements()){
//            ChromosomeSequence s = e.nextElement();
//            File chr_file = new File(s.getFilePath()+".fa");
//            if(!chr_file.exists()){
//                s.writeToFile("FA");
//            }
//        }
//    }
    
//    ========================= read all chromosome
    if(create_index){
//          File index_file = new File(ref.getPath()+".index");

        PrintStream ps = new PrintStream(new FileOutputStream(index_file));
         
        
        Enumeration<ChromosomeSequence> e2 = ref.getChromosomes().elements();
        while(e2.hasMoreElements()){
            ChromosomeSequence s = e2.nextElement();
            ps.println(s.getName());
            
        }
        
        ps.close();
    }

    }
    
    

    
    
    return ref;
    
    }
    
    
//    public static ChromosomeSequence loadChromosomeSequence(ReferenceSequence ref, String filename);
    
    
    public static ReferenceSequence  readAndIndexReferenceSequence(String filename){
     
        
           
       
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
               break;
                
            }
            seq = new StringBuffer();
            chr = line.substring(1,line.length());
              
               
        }else{
            
            seq.append(line.trim());
            
            
        }
        
    }
    
    if(seq.length()>0){
        
        System.out.println("CHR : "+chr+" Size : "+seq.length());

        ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
                
        ref.addChromosomeSequence(c);
        
    }
    
    
    
    
    System.out.println("Number of chromosomes : "+ref.getChromosomes().size());
    
    
    
    Enumeration<ChromosomeSequence> e = ref.getChromosomes().elements();
    
    
    
    while(e.hasMoreElements()){
        
        ChromosomeSequence s = e.nextElement();
        
//        EncodedSequence encoded = encodeSerialChromosomeSequenceV3(s);
        
    }
    
    
    } catch (IOException x) {
        System.err.format("IOException: %s%n", x);
    }    
           
           
     
       return ref;
        
    }
    
    
    
    public static EncodedSequence encodeSerialChromosomeSequenceV3(ChromosomeSequence chr,int mer) throws FileNotFoundException, IOException{
         
        /**
         * Create/import kmer reference by chromosome
         */
       
        EncodedSequence seq = new EncodedSequence();

        int kmer = mer;
        int sliding = 1;
        int repeat = 0;


        File f = new File(chr.getFilePath()+".bin"); //File object
        File compF = new File(chr.getFilePath()+"_comp.bin");

        if(f.exists()){
           
            int count = 0 ;

            DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
            int size = is.readInt();

            long list[] = new long[size];
//            System.out.println("Totalxx bmer : "+size);

            for(int i=0;i<size;i++){

               
                
                list[i] = is.readLong();
                int percent = (int)(1.0*count/size); 
//                System.out.println(Long.toBinaryString(list[i]));

                if(percent%10==0&&percent!=0)System.out.println("Read binary Mer "+chr.getName()+" "+count);
                count ++;
            }
            
            seq.setMers(list);
//            long[] listComp = createComplimentStrand(list);
//            seq.setMersComp(listComp);
//            System.out.println("Total bmer : "+size);

            is.close();
            
            /* Compliment Part *//*
            if(compF.exists()){
                is = new DataInputStream(new BufferedInputStream(new FileInputStream(compF)));
                size = is.readInt();
            
                long[] complist = new long[size];
    //            System.out.println("Totalxx bmer : "+size);

                for(int i=0;i<size;i++){



                    complist[i] = is.readLong();
                    int percent = (int)(1.0*count/size); 
    //                System.out.println(Long.toBinaryString(list[i]));

                    if(percent%10==0&&percent!=0)System.out.println("Read compliment binary Mer "+chr.getName()+" "+count);
                    count ++;
                }
                seq.setMersComp(complist);
                
                complist=null;
                System.gc();
                
                is.close();
            }else{
                
                size = list.length;
                long[] complist = new long[size];
                complist = createComplimentStrand(list);
                
                list=null;
                System.gc();
                
                DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(compF)));
                os.writeInt(size);
                    for(int i=0;i<size;i++){
                        if(i%1000000==0)System.out.println("Write "+chr.getName()+" "+i);
                        os.writeLong(complist[i]); // write list variable to file .bin
                    }
                os.close();
                
                seq.setMersComp(complist);   
                
                complist=null;
                System.gc();
            }*/
            
//            seq.setMers(list);
   
        }else{

            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f))); // create object for output data stream

            StringBuffer sb = chr.getSequence();


            int n = (sb.length()-kmer)/sliding;       
            long cmer = -1;
            long mask = 0; 
            int count = 0;

            long list[] = new long[n]; // Pre - allocate Array by n


            for(int i =0;i<kmer;i++)mask=mask*4+3;

            System.out.println(mask);

            for(int i =0;i<n;i++){

                long pos = i*sliding;
                char chx = sb.charAt(i*sliding+kmer-1);
                if(chx!='N'){
                    if(cmer==-1){
                        String s = sb.substring(i*sliding,i*sliding+kmer);
                        cmer = encodeMer(s,kmer);
                    }else{

                        int t =-1;
                        switch(chx){
                            case 'A':
                            case 'a':
                                t=0; // 00
                                break;
                            case 'T':
                            case 't': 
                                t=3; // 11
                                break;
                            case 'C':
                            case 'c':
                                t=1; // 01 
                                break;
                            case 'G':
                            case 'g':
                                t=2; // 10 
                                break;
                            default : 
                                t=-1;
                            break;

                        }
                        if(t>=0){

                            cmer *= 4;
                            cmer &= mask;
                            cmer += t;

                        }else{
                            cmer = -1;
                            i+=kmer;
                        }  
                    }  
                    if(i%1000000==0)System.out.println("Encode "+chr.getName()+" "+i*sliding);

                    if(cmer>=0){  
                        long x = (cmer<<(64- kmer*2))|pos;
                        list[count++] = x;
                    }

                }
            }


            Arrays.sort(list);


            os.writeInt(list.length);
            for(int i=0;i<list.length;i++){
                if(i%1000000==0)System.out.println("Write "+chr.getName()+" "+i);
                os.writeLong(list[i]); // write list variable to file .bin
            }
            os.close();

            seq.setMers(list);

            list=null;
            System.gc();

    //       long[] listComp = createComplimentStrand(list);
    //       seq.setMersComp(listComp);

        /* Compliment Part *//*
                if(compF.exists()){
                    DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(compF)));
                    int size = is.readInt();

                    long[] complist = new long[size];
        //            System.out.println("Totalxx bmer : "+size);

                    for(int i=0;i<size;i++){
                        complist[i] = is.readLong();
                        int percent = (int)(1.0*count/size); 
        //                System.out.println(Long.toBinaryString(list[i]));

                        if(percent%10==0&&percent!=0)System.out.println("Read compliment binary Mer "+chr.getName()+" "+count);
                            count ++;
                        }
                    seq.setMersComp(complist);

                    complist=null;
                    System.gc();

                    is.close();
                }else{

                    int size = list.length;
                    long[] complist = new long[size];
                    complist = createComplimentStrand(list);

                    list=null;
                    System.gc();

                    os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(compF)));
                    os.writeInt(size);
                        for(int i=0;i<size;i++){
                            if(i%1000000==0)System.out.println("Write "+chr.getName()+" "+i);
                            os.writeLong(complist[i]); // write list variable to file .bin
                        }
                    os.close();

                    seq.setMersComp(complist);

                    complist=null;
                    System.gc();
                }*/


 
        }

        return seq;
   }
    
    

    
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
       
       //Hashtable<Long,Long> map =new Hashtable<Long,Long>();
       //TreeMap<Long,Long> map = new TreeMap();
       Map<Long,Long> map = new HashMap();
       
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
                  //cmer<<=8;
                  //cmer+=chr.getChrNumber();
                  pos<<=8;
                  pos+=chr.getChrNumber();
                  map.put(cmer, pos);
               }
               
           }
           
           
           }
       }
       //System.out.println("In encodeSerialChromosome " + chrName);
       seq.setMap(map);
       
       
       System.out.println("Total mer :" +n);
       System.out.println("Total uniq mer :" +map.size());
       System.out.println("Total rep :" +repeat);
       
       
       return seq;
   }
    
    public static EncodedSequence encodeSerialChromosomeSequenceV2(ChromosomeSequence chr){
       
       EncodedSequence seq = new EncodedSequence();
       
       int kmer = 20;
       int sliding = 1;
       int repeat = 0;
       
       //Hashtable<Long,Long> map =new Hashtable<Long,Long>();
       //TreeMap<Long,Long> map = new TreeMap();
       Map<Long,Long> map = new HashMap();
       
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
                  
                  cmer<<=8;
                  cmer+=chr.getChrNumber();
                  
                  map.put(cmer, pos);
               }
               
           }
           
           
           }
       }
       //System.out.println("In encodeSerialChromosome " + chrName);
       seq.setMap(map);
       
       
       System.out.println("Total mer :" +n);
       System.out.println("Total uniq mer :" +map.size());
       System.out.println("Total rep :" +repeat);
       
       
       return seq;
   }
    
    public static EncodedSequence encodeSerialReadSequence(CharSequence chr){
       // Output is CharSequence
       EncodedSequence seq = new EncodedSequence();
       
       int kmer = 20;
       int sliding = 1;
       int repeat = 0;
       
       //Hashtable<Long,Long> map =new Hashtable<Long,Long>();
       //TreeMap<Long,Long> map = new TreeMap();
       Map<Long,Long> map = new HashMap();
       
       StringBuffer sb = new StringBuffer(chr);
       //StringBuffer sb = chr.getSequence();
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
                  //cmer<<=8; 
                  //pos<<=8;
                  map.put(cmer, pos);
               }
               
           }
           
           
           }
       }
       
       seq.setReadMap(map);
       
        
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
       
       //Hashtable<Long,Long> map =new Hashtable<Long,Long>();
       //TreeMap<Long,Long> map = new TreeMap();
       Map<Long,Long> map = new HashMap();
       
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
    
   
    public static long encodeMer(String seq, int kmer){
       
//       cctgtagtacagtttgaagt
       long mer = 0 ;
       int t;
//       String seq = seq_input.toLowerCase();
//       if(seq.compareTo(seq_input)==0)return -1;
       for(int i=0;i < kmer && i<seq.length();i++){
            char a = seq.charAt(i);
           
            switch(a){
                case 'a':
                case 'A':
                    t=0; // 00
                    break;
                case 't': 
                case 'T':
                    t=3; // 11
                    break;
                case 'c':
                case 'C':
                    t=1; // 01 
                    break;
                case 'g':
                case 'G':
                    t=2; // 10 
                    break;
                default : 
                    t=-1;
                    return -1;
               
            }
            
            if(t>=0){

                mer=mer*4+t;
                
            }
           
       }
       
       return mer;
   }

    
    public static EncodedSequence getEncodeSequence(ChromosomeSequence chr) {

        EncodedSequence encode = null;
//        System.out.println(chr.getFilePath());
        Path fp = Paths.get(chr.getFilePath()+".bmap");
        File f = fp.toFile();
        try{

        if(f.exists()){
            encode = new EncodedSequence();
            encode.readFromPath(chr.getFilePath(), "bmap");
        }else{
            encode = SequenceUtil.encodeSerialChromosomeSequence(chr);
//            encode.writeToPath(chr.getFilePath(), "map");
            encode.writeToPath(chr.getFilePath(), "bmap");
        }
        
        }catch(Exception e){
            System.out.println("Error");
        }
        //System.out.println("From Encode "+encode.getEncodeChrName());
//        return null;
        return encode;

    }
    
    public static EncodedSequence getEncodeSequenceV2(ChromosomeSequence chr,int mer) {

        EncodedSequence encode = null;
//        System.out.println(chr.getFilePath());
        Path fp = Paths.get(chr.getFilePath()+".bin");
        File f = fp.toFile();
        try{

            if(f.exists()){
                encode = new EncodedSequence();
                encode.readFromPath(chr.getFilePath(), "bin");
            }else{
                encode = SequenceUtil.encodeSerialChromosomeSequenceV3(chr,mer);
    //            encode.writeToPath(chr.getFilePath(), "map");
                encode.writeToPath(chr.getFilePath(), "bin");
            }
        
        }catch(Exception e){
            System.out.println("Error YoYOYO" + e);
        }
        //System.out.println("From Encode "+encode.getEncodeChrName());
//        return null;
        return encode;

    }
    
    public static CharSequence concatenateChromosome(ChromosomeSequence chrA,ChromosomeSequence chrB, int cutLengthA, int cutLengthB){
        //chrA.getsequence
        int check,checkA = 1,checkB = 1;
        CharSequence cutA,cutB,concatenateCut;
        CharSequence checkN = "N";
        
        int lengthA = chrA.getSequence().length();
        int lengthB = chrB.getSequence().length();
        int rangeA = lengthA - 0 ;
        int rangeB = lengthB - 0 ;
        
        Random r = new Random();
        int iniA = r.nextInt(rangeA);
        int iniB = r.nextInt(rangeB);
        cutA = chrA.getSequence().subSequence(iniA, iniA+cutLengthA);
        cutB = chrB.getSequence().subSequence(iniB, iniB+cutLengthB);
        
        
        while(checkA == 1 || checkB == 1){
            //System.out.println(rangeA);
            //System.out.println(rangeB);
            //System.out.println(lengthA);
            //System.out.println(lengthB);

            System.out.println(cutA.toString().contains("N"));
            System.out.println(cutB.toString().contains("N"));
                                
            if(cutA.toString().contains("N") || cutA.toString().equals(cutA.toString().toLowerCase())){
                iniA = r.nextInt(rangeA);
                cutA = chrA.getSequence().subSequence(iniA, iniA+cutLengthA);                
            }else checkA = 0;                        
        
            if(cutB.toString().contains("N") || cutB.toString().equals(cutB.toString().toLowerCase())){
                iniB = r.nextInt(rangeB);
                cutB = chrB.getSequence().subSequence(iniB, iniB+cutLengthB);
            }else checkB = 0;
        }
        
        
        
            concatenateCut = cutA.toString()+cutB.toString();
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " from position " + iniA + " : " + cutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " from position " + iniB + " : " + cutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
            cutA = null;
            cutB = null;
            System.gc();
            
            // not finish
        //}
        
        
        return concatenateCut;
    }
    
    public static ConcatenateCut concatenateComplexChromosome(ChromosomeSequence chrA,ChromosomeSequence chrB, int cutLengthA, int cutLengthB){
        //chrA.getsequence
        int check,checkA = 1,checkB = 1;
        CharSequence cutA,cutB,dummyConcatenateCut;
        ConcatenateCut concatenateCut = new ConcatenateCut(); 
        CharSequence checkN = "N";
        
        int lengthA = chrA.getSequence().length();
        int lengthB = chrB.getSequence().length();
        int rangeA = lengthA - 0 ;
        int rangeB = lengthB - 0 ;
        
        Random r = new Random();
        int iniA = r.nextInt(rangeA);
        int iniB = r.nextInt(rangeB);
        cutA = chrA.getSequence().subSequence(iniA, iniA+cutLengthA);
        cutB = chrB.getSequence().subSequence(iniB, iniB+cutLengthB);
        
        
        while(checkA == 1 || checkB == 1){
            //System.out.println(rangeA);
            //System.out.println(rangeB);
            //System.out.println(lengthA);
            //System.out.println(lengthB);

            System.out.println(cutA.toString().contains("N"));
            System.out.println(cutB.toString().contains("N"));
                                
            if(cutA.toString().contains("N") || cutA.toString().equals(cutA.toString().toLowerCase())){
                iniA = r.nextInt(rangeA);
                cutA = chrA.getSequence().subSequence(iniA, iniA+cutLengthA);                
            }else checkA = 0;                        
        
            if(cutB.toString().contains("N") || cutB.toString().equals(cutB.toString().toLowerCase())){
                iniB = r.nextInt(rangeB);
                cutB = chrB.getSequence().subSequence(iniB, iniB+cutLengthB);
            }else checkB = 0;
        }
        concatenateCut.addBasicInfo(chrA.getName(), chrB.getName(), iniA, iniB);
        Random fusionType = new Random();
        int type = fusionType.nextInt(4);
        
        if(type == 0){
            /* strand ++ */
            dummyConcatenateCut = cutA.toString()+cutB.toString();
            concatenateCut.addSequence(dummyConcatenateCut);
            concatenateCut.addType(type);
            concatenateCut.addCutInfo(cutA, cutB);
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (+) from position " + iniA + " : " + cutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (+) from position " + iniB + " : " + cutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else if(type == 1){
            /* strand +- */
            String invCutB = SequenceUtil.inverseSequence(cutB.toString());
            String compCutB = SequenceUtil.createComplimentV2(invCutB);
            
            dummyConcatenateCut = cutA.toString()+compCutB;
            concatenateCut.addSequence(dummyConcatenateCut);
            concatenateCut.addType(type);
            concatenateCut.addCutInfo(cutA, cutB);
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (+) from position " + iniA + " : " + cutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (-) from position " + (rangeB - iniB) + " : " + compCutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else if(type == 2){
            /* strand -+ */
            String invCutA = SequenceUtil.inverseSequence(cutA.toString());
            String compCutA = SequenceUtil.createComplimentV2(invCutA);
            
            dummyConcatenateCut = compCutA + cutB.toString();
            concatenateCut.addSequence(dummyConcatenateCut);
            concatenateCut.addType(type);
            concatenateCut.addCutInfo(cutA, cutB);
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (-) from position " + (rangeA - iniA) + " : " + compCutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (+) from position " + iniB + " : " + cutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else{
            /* strand -- */
            String invCutA = SequenceUtil.inverseSequence(cutA.toString());
            String compCutA = SequenceUtil.createComplimentV2(invCutA);
            String invCutB = SequenceUtil.inverseSequence(cutB.toString());
            String compCutB = SequenceUtil.createComplimentV2(invCutB);
        
            dummyConcatenateCut = compCutA + compCutB;
            concatenateCut.addSequence(dummyConcatenateCut);
            concatenateCut.addType(type);
            concatenateCut.addCutInfo(cutA, cutB);
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (-) from position " + (rangeA - iniA) + " : " + compCutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (-) from position " + (rangeB - iniB) + " : " + compCutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }
         
        cutA = null;
        cutB = null;
        System.gc();
            
            // not finish
        //}
        
        
        return concatenateCut;
    }
    
    public static SNPsample createComplexSNPSample(ChromosomeSequence chrA, int cutLengthA){
        SNPsample snpSample = new SNPsample();        
        int check,checkA = 1,checkB = 1;
        CharSequence cutA,cutB;
        StringBuilder dummyCut = new StringBuilder();
        
        CharSequence checkN = "N";
        
        int lengthA = chrA.getSequence().length();
        
        int rangeA = lengthA - 0 ;
       
        
        Random r = new Random();
        int iniA = r.nextInt(rangeA);
        
        cutA = chrA.getSequence().subSequence(iniA, iniA+cutLengthA); // suitble cut length should be 198 base if you want read 100 base

        while(checkA == 1){
            //System.out.println(rangeA);
            //System.out.println(rangeB);
            //System.out.println(lengthA);
            //System.out.println(lengthB);

            System.out.println(cutA.toString().contains("N"));
                                
            if(cutA.toString().contains("N") || cutA.toString().equals(cutA.toString().toLowerCase())){
                iniA = r.nextInt(rangeA);
                cutA = chrA.getSequence().subSequence(iniA, iniA+cutLengthA);                
            }else checkA = 0;                        
        }
        
        snpSample.addBasicInfo(chrA.getName(), iniA);
        snpSample.addCutInfo(cutA);
        
        Random fusionType = new Random();
        int type = fusionType.nextInt(2);
        
        if(type == 0){
            /* strand + */
            dummyCut = new StringBuilder(cutA.toString());
            char base = dummyCut.charAt(cutLengthA/2);

            if(base == 'a' || base == 'A'){
                base = 'G';
            }else if(base == 't'||base == 'T'){
                base = 'C';
            }else if(base == 'g'||base == 'G'){
                base = 'A';
            }else if(base == 'c'||base == 'C'){
                base = 'T';
            }
            
            dummyCut.setCharAt(cutLengthA/2, base);
            snpSample.addSNPcharecteristic(type, base, cutLengthA/2);

        }else if(type == 1){
            /* strand - */
            String invCutA = SequenceUtil.inverseSequence(cutA.toString());
            String compCutA = SequenceUtil.createComplimentV2(invCutA);
            
            dummyCut = new StringBuilder(compCutA);
            char base = dummyCut.charAt(cutLengthA/2);

            if(base == 'a' || base == 'A'){
                base = 'G';
            }else if(base == 't'||base == 'T'){
                base = 'C';
            }else if(base == 'g'||base == 'G'){
                base = 'A';
            }else if(base == 'c'||base == 'C'){
                base = 'T';
            }
            
            dummyCut.setCharAt(cutLengthA/2, base);
            snpSample.addSNPcharecteristic(type, base, cutLengthA/2);
        }
        
        snpSample.addSequence(dummyCut);
        cutA = null;
        cutB = null;
        System.gc();

        return snpSample;
    }
    
    public static ConcatenateCut createComplexLargeIndel(ChromosomeSequence chrA,ChromosomeSequence chrB, int cutLengthA, int cutLengthB, int posDiff){
        //chrA.getsequence
        int check,checkA = 1,checkB = 1;
        CharSequence cutA,cutB,dummyConcatenateCut;
        ConcatenateCut concatenateCut = new ConcatenateCut(); 
        CharSequence checkN = "N";
        
        int lengthA = chrA.getSequence().length();
        int lengthB = chrB.getSequence().length();
        int rangeA = lengthA - 0 ;
        int rangeB = lengthB - 0 ;
        int dif = 0;
        int iniA=0,iniB=0;
        Random r = new Random();
        while(dif < posDiff){
            iniA = r.nextInt(rangeA);
            iniB = r.nextInt(rangeB);
            dif = iniA - iniB;                  // the value of dif has benn strict to positive by the case check of while loop
        }
        
        cutA = chrA.getSequence().subSequence(iniA, iniA+cutLengthA);
        cutB = chrB.getSequence().subSequence(iniB, iniB+cutLengthB);

        while(checkA == 1 || checkB == 1){
            int difA=0,difB=0;
                    
            System.out.println(cutA.toString().contains("N"));
            System.out.println(cutB.toString().contains("N"));
                                
            if(cutA.toString().contains("N") || cutA.toString().equals(cutA.toString().toLowerCase())){
                while(difA < posDiff){
                    iniA = r.nextInt(rangeA);                   
                    difA = iniA - iniB;                  // the value of dif has benn strict to positive by the case check of while loop
                }
                cutA = chrA.getSequence().subSequence(iniA, iniA+cutLengthA);                
            }else checkA = 0;                        
        
            if(cutB.toString().contains("N") || cutB.toString().equals(cutB.toString().toLowerCase())){
                while(difB < posDiff){
                    iniB = r.nextInt(rangeB);
                    difB = iniA - iniB;                  // the value of dif has benn strict to positive by the case check of while loop
                }
                cutB = chrB.getSequence().subSequence(iniB, iniB+cutLengthB);
            }else checkB = 0;
        }
        
        concatenateCut.addBasicInfo(chrA.getName(), chrB.getName(), iniA, iniB);
        
        Random indelType = new Random();
        int type = indelType.nextInt(4);
        
        if(type == 0){
            /* strand ++ */
            dummyConcatenateCut = cutA.toString()+cutB.toString();
            concatenateCut.addSequence(dummyConcatenateCut);
            concatenateCut.addType(type);
            concatenateCut.addCutInfo(cutA, cutB);
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (+) from position " + iniA + " : " + cutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (+) from position " + iniB + " : " + cutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else if(type == 1){
            /* strand +- */
            String invCutB = SequenceUtil.inverseSequence(cutB.toString());
            String compCutB = SequenceUtil.createComplimentV2(invCutB);
            
            dummyConcatenateCut = cutA.toString()+compCutB;
            concatenateCut.addSequence(dummyConcatenateCut);
            concatenateCut.addType(type);
            concatenateCut.addCutInfo(cutA, cutB);
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (+) from position " + iniA + " : " + cutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (-) from position " + (rangeB - iniB) + " : " + compCutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else if(type == 2){
            /* strand -+ */
            String invCutA = SequenceUtil.inverseSequence(cutA.toString());
            String compCutA = SequenceUtil.createComplimentV2(invCutA);
            
            dummyConcatenateCut = compCutA + cutB.toString();
            concatenateCut.addSequence(dummyConcatenateCut);
            concatenateCut.addType(type);
            concatenateCut.addCutInfo(cutA, cutB);
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (-) from position " + (rangeA - iniA) + " : " + compCutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (+) from position " + iniB + " : " + cutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else{
            /* strand -- */
            String invCutA = SequenceUtil.inverseSequence(cutA.toString());
            String compCutA = SequenceUtil.createComplimentV2(invCutA);
            String invCutB = SequenceUtil.inverseSequence(cutB.toString());
            String compCutB = SequenceUtil.createComplimentV2(invCutB);
        
            dummyConcatenateCut = compCutA + compCutB;
            concatenateCut.addSequence(dummyConcatenateCut);
            concatenateCut.addType(type);
            concatenateCut.addCutInfo(cutA, cutB);
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (-) from position " + (rangeA - iniA) + " : " + compCutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (-) from position " + (rangeB - iniB) + " : " + compCutB);
            System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }
         
        cutA = null;
        cutB = null;
        System.gc();
            
            // not finish
        //}
        
        
        return concatenateCut;
    }
    
    public static Smallindelsample createComplexSmallIndel(ChromosomeSequence chrA,ChromosomeSequence chrB, int cutLengthA, int cutLengthB, int posDiff, char indelType, int indelSize){
        //chrA.getsequence
        Smallindelsample smallIndelSample = new Smallindelsample();
        int check,checkA = 1,checkB = 1;
        CharSequence cutA=null,cutB=null,dummyCut=null;
        ConcatenateCut concatenateCut = new ConcatenateCut(); 
        CharSequence checkN = "N";
        
        int lengthA = chrA.getSequence().length();
        int lengthB = chrB.getSequence().length();
        int rangeA = lengthA - 0 ;
        int rangeB = lengthB - 0 ;
        int dif = 0;
        int iniA=0,iniB=0;
        Random r = new Random();
        while(dif < posDiff){
            iniA = r.nextInt(rangeA);
            iniB = r.nextInt(rangeB);
            dif = abs(iniA - iniB);                  
        }
        
        if(indelType == 'D'){            // 'D' mean Deletion
            cutA = chrA.getSequence().subSequence(iniA, (iniA+cutLengthA*2)+indelSize);
        }else if (indelType == 'I'){    // 'I' mean Insertion
            cutA = chrA.getSequence().subSequence(iniA, (iniA+cutLengthA*2)-indelSize);  
        }
        
        cutB = chrB.getSequence().subSequence(iniB, iniB+indelSize);
        
        while(checkA == 1 || checkB == 1){
            int difA=0,difB=0;
            System.out.println(cutA.toString().contains("N"));
            System.out.println(cutB.toString().contains("N"));
                                
            if(cutA.toString().contains("N") || cutA.toString().equals(cutA.toString().toLowerCase())){
                while(difA < posDiff){
                    iniA = r.nextInt(rangeA);
                    difA = abs(iniA - iniB);
                }
                
                if(indelType == 'D'){            // 'D' mean Deletion
                    cutA = chrA.getSequence().subSequence(iniA, (iniA+cutLengthA*2)+indelSize);
                }else if (indelType == 'I'){    // 'I' mean Insertion
                    cutA = chrA.getSequence().subSequence(iniA, (iniA+cutLengthA*2)-indelSize);  
                }
                
            }else checkA = 0;                        
        
            if(cutB.toString().contains("N") || cutB.toString().equals(cutB.toString().toLowerCase())){
                while(difB < posDiff){
                    iniB = r.nextInt(rangeB);
                    difB = abs(iniA - iniB);
                }
                
                cutB = chrB.getSequence().subSequence(iniB, iniB+indelSize);
            }else checkB = 0;
        }
        
        smallIndelSample.addBasicInfo(chrA.getName(), chrB.getName(), iniA, iniB, indelType , indelSize);
        
        
        Random indelStyle= new Random();
        int type = indelStyle.nextInt(4);
        
        if(type == 0){
            /* strand ++ */
            
            if(indelType == 'D'){            // 'D' mean Deletion
                dummyCut = cutA.toString().substring(0, (cutA.length()/2)-(indelSize/2)) + cutA.toString().substring((cutA.length()/2)+(indelSize-indelSize/2),cutA.length());
            }else if (indelType == 'I'){    // 'I' mean Insertion
                
                dummyCut = cutA.toString().substring(0, cutA.length()/2) + cutB.toString() + cutA.toString().substring(cutA.length()/2,cutA.length());
//                dummyCut = cutA.toString().substring(0, (cutA.length()/2)-(indelSize/2)) + cutB.toString() + cutA.toString().substring((cutA.length()/2)+(indelSize-indelSize/2),cutA.length());
            }

            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (+) from position " + iniA + " : " + cutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (+) from position " + iniB + " : " + cutB);
            //System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else if(type == 1){
            /* strand +- */
            String invCutB = SequenceUtil.inverseSequence(cutB.toString());
            String compCutB = SequenceUtil.createComplimentV2(invCutB);
            
            if(indelType == 'D'){            // 'D' mean Deletion
                dummyCut = cutA.toString().substring(0, (cutA.length()/2)-(indelSize/2)) + cutA.toString().substring((cutA.length()/2)+(indelSize-indelSize/2),cutA.length());
            }else if (indelType == 'I'){    // 'I' mean Insertion
                
                dummyCut = cutA.toString().substring(0, cutA.length()/2) + cutB.toString() + cutA.toString().substring(cutA.length()/2,cutA.length());
//                dummyCut = cutA.toString().substring(0, (cutA.length()/2)-(indelSize/2)) + compCutB.toString() + cutA.toString().substring((cutA.length()/2)+(indelSize-indelSize/2),cutA.length());
            }

            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (+) from position " + iniA + " : " + cutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (-) from position " + (rangeB - iniB) + " : " + compCutB);
            //System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else if(type == 2){
            /* strand -+ */
            String invCutA = SequenceUtil.inverseSequence(cutA.toString());
            String compCutA = SequenceUtil.createComplimentV2(invCutA);
            
            if(indelType == 'D'){            // 'D' mean Deletion

                dummyCut = compCutA.toString().substring(0, (cutA.length()/2)-(indelSize/2)) + compCutA.toString().substring((cutA.length()/2)+(indelSize-indelSize/2),cutA.length());
            }else if (indelType == 'I'){    // 'I' mean Insertion
                
                dummyCut = cutA.toString().substring(0, cutA.length()/2) + cutB.toString() + cutA.toString().substring(cutA.length()/2,cutA.length());
//                dummyCut = compCutA.toString().substring(0, (cutA.length()/2)-(indelSize/2)) + cutB.toString() + compCutA.toString().substring((cutA.length()/2)+(indelSize-indelSize/2),cutA.length());
            }
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (-) from position " + (rangeA - iniA) + " : " + compCutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (+) from position " + iniB + " : " + cutB);
            //System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }else{
            /* strand -- */
            String invCutA = SequenceUtil.inverseSequence(cutA.toString());
            String compCutA = SequenceUtil.createComplimentV2(invCutA);
            String invCutB = SequenceUtil.inverseSequence(cutB.toString());
            String compCutB = SequenceUtil.createComplimentV2(invCutB);
        
            if(indelType == 'D'){            // 'D' mean Deletion

                dummyCut = compCutA.toString().substring(0, (cutA.length()/2)-(indelSize/2)) + compCutA.toString().substring((cutA.length()/2)+(indelSize-indelSize/2),cutA.length());
            }else if (indelType == 'I'){    // 'I' mean Insertion
                
                dummyCut = cutA.toString().substring(0, cutA.length()/2) + cutB.toString() + cutA.toString().substring(cutA.length()/2,cutA.length());
//                dummyCut = compCutA.toString().substring(0, (cutA.length()/2)-(indelSize/2)) + compCutB.toString() + compCutA.toString().substring((cutA.length()/2)+(indelSize-indelSize/2),cutA.length());
            }
            
            System.out.println("Random cut of chr" + chrA.getChrNumber() + " Strand (-) from position " + (rangeA - iniA) + " : " + compCutA);
            System.out.println("Random cut of chr" + chrB.getChrNumber() + " Strand (-) from position " + (rangeB - iniB) + " : " + compCutB);
            //System.out.println("Concatenate chromosome: " + concatenateCut);
            
        }
        smallIndelSample.addType(type);
        smallIndelSample.addCutInfo(cutA);
        smallIndelSample.addIndelInfo(cutB);
        smallIndelSample.addSequence(dummyCut);
        
        cutA = null;
        cutB = null;
        System.gc();
 
        return smallIndelSample;
    }
    
    public static Map mapGenomeShotgun(EncodedSequence refChr, InputSequence read){
        
       
        Map<Long,Long> outputMap = new HashMap();
        Map<Long,Long> chrMap = refChr.getEncodeMap();
        Long[][] value = new Long[1][2];
        String name;
                
        //Long chrnum = refChr.getEncodeChrName().split("chr");
        Vector readVector = read.getInputSequence();
        Enumeration<ShortgunSequence> e = readVector.elements();
        while(e.hasMoreElements()){
            ShortgunSequence ss = e.nextElement();
          
            EncodedSequence encodeSim = SequenceUtil.encodeSerialReadSequence(ss.getSequence());
            
            SortedSet<Long> vals = new TreeSet(encodeSim.getEncodeMap().values()) ;
            Map<Long,Long> readMap = encodeSim.getEncodeMap();
         
            int count = 1;
            long index = 0;
            long indexB = 0;
            long roundMatch = 1;
            long roundNMatch = 1;
            //Obiect readKey = 0;
            for (Long val : vals){
                //readKey = getKeyFromValue(readMap,val);
                //System.out.println(key);
                System.out.println("Number of mapping iteration : " + count++);
    //            System.out.println("the value is " + val + " Key is " + getKeyFromValue(read.getEncodeMap(),val));
                if (chrMap.containsKey(getKeyFromValue(readMap,val))){

                    //System.out.println("Key " + getKeyFromValue(read.getEncodeMap(),val) +": Match at position " + chr.getEncodeMap().get(getKeyFromValue(read.getEncodeMap(),val)));
                    System.out.println("val : " + val);
                            
                    System.out.println("Key " + getKeyFromValue(readMap,val) +": Match at position " + chrMap.get(getKeyFromValue(readMap,val)));
                    index = chrMap.get(getKeyFromValue(readMap,val)) - val;
                    
                    indexB = (chrMap.get(getKeyFromValue(readMap,val))>>8) - (val>>8);
                    System.out.println("New indexB without add number of chromosome : " + indexB);
                    indexB = (indexB<<8)+(chrMap.get(getKeyFromValue(readMap,val))&255);
                    System.out.println("New indexB with add number of chromosome : " + indexB);
                     
                    System.out.println("Cross check with reference key : " + (getKeyFromValue(chrMap,(index+val))) );
                    //System.out.println("Position that map on reference : " + chrMap.get(getKeyFromValue(readMap,val)));
                    
                    System.out.println("Align at : " + index );
                    //if (checkMap == 0){
                        //count++;
                    //}
                    //value[0][0] = roundMatch++;
                    //value[0][1] = read.getChrName();
                    outputMap.put(index,roundMatch++);
                     

                }else{
                    System.out.println(" Key " + getKeyFromValue(readMap,val) + ": Not match");
                    //index = val;
                    roundMatch = 1;  // reset round
                    //System.out.println("");
                }   

            }

            System.out.println("Key : "+index+ "Has : "+outputMap.get(index));
        }
        
        
        
        return outputMap;
    }
    
    
    public static MapResult mapGenomeShotgunV2(EncodedSequence refChr, InputSequence read){
        
        MapResult result = new MapResult();
        Map<Long,Long> outputMap = new HashMap();
        Map<Long,Long> chrMap = refChr.getEncodeMap();
        Long[][] value = new Long[1][2];
        String name;
                
        //Long chrnum = refChr.getEncodeChrName().split("chr");
        Vector readVector = read.getInputSequence();
        Enumeration<ShortgunSequence> e = readVector.elements();
        while(e.hasMoreElements()){
            ShortgunSequence ss = e.nextElement();
          
            EncodedSequence encodeSim = SequenceUtil.encodeSerialReadSequence(ss.getSequence());
            
            SortedSet<Long> vals = new TreeSet(encodeSim.getEncodeMap().values()) ;
            Map<Long,Long> readMap = encodeSim.getEncodeMap();
         
            int count = 1;
            long index = 0;
            long indexB = 0;
            long roundMatch = 1;
            long roundNMatch = 1;
            //Obiect readKey = 0;
            for (Long val : vals){
                //readKey = getKeyFromValue(readMap,val);
                //System.out.println(key);
                System.out.println("Number of mapping iteration : " + count++);
    //            System.out.println("the value is " + val + " Key is " + getKeyFromValue(read.getEncodeMap(),val));
                if (chrMap.containsKey(getKeyFromValue(readMap,val))){

                    //System.out.println("Key " + getKeyFromValue(read.getEncodeMap(),val) +": Match at position " + chr.getEncodeMap().get(getKeyFromValue(read.getEncodeMap(),val)));
                    System.out.println("val : " + val);
                            
                    System.out.println("Key " + getKeyFromValue(readMap,val) +": Match at position " + chrMap.get(getKeyFromValue(readMap,val)));
                    index = chrMap.get(getKeyFromValue(readMap,val)) - val;
                    
                    indexB = (chrMap.get(getKeyFromValue(readMap,val))>>8) - (val>>8);
                    System.out.println("New indexB without add number of chromosome : " + indexB);
                    indexB = (indexB<<8)+(chrMap.get(getKeyFromValue(readMap,val))&255);
                    System.out.println("New indexB with add number of chromosome : " + indexB);
                     
                    System.out.println("Cross check with reference key : " + (getKeyFromValue(chrMap,(index+val))) );
                    //System.out.println("Position that map on reference : " + chrMap.get(getKeyFromValue(readMap,val)));
                    
                    System.out.println("Align at : " + index );
                    //if (checkMap == 0){
                        //count++;
                    //}
                    //value[0][0] = roundMatch++;
                    //value[0][1] = read.getChrName();
                    /////outputMap.put(index,roundMatch++);
                    result.addResultMap(index,roundMatch++);
                     

                }else{
                    System.out.println(" Key " + getKeyFromValue(readMap,val) + ": Not match");
                    //index = val;
                    roundMatch = 1;  // reset round
                    //System.out.println("");
                }   
                
            }

            System.out.println("Key : "+index+ "Has : "+result.getResultMap().get(index));
        }
        
        
        
        return result;
    }
    
    public static MapResult mapGenomeShotgunV3(ReferenceSequence refGene, InputSequence read,int startElement, int stopElement) throws IOException{
        
        // Process each read separately and add result to array list
        // chr 21,and 22 is at element 12,13 of whole genome reference
        MapResult result = new MapResult();
        Vector readVector = read.getInputSequence();
        
        for(int numElement = startElement;numElement<=stopElement;numElement++){
            ChromosomeSequence chr = refGene.getChromosomes().elementAt(numElement);
            System.out.println("Name of Pick chromosome : " + chr.getName());

            EncodedSequence encode = SequenceUtil.getEncodeSequence(chr);
            System.out.println("Size of encode referecce : " + encode.getEncodeMap().size());

            //Long chrnum = refChr.getEncodeChrName().split("chr");
            
            
            //Vector readVector = read.getInputSequence();
            Enumeration<ShortgunSequence> e = readVector.elements();
            while(e.hasMoreElements()){

                Map<Long,Long> outputMap = new HashMap();
                Map<Long,Long> chrMap = encode.getEncodeMap();
                Long[][] value = new Long[1][2];
                String name;




                ShortgunSequence ss = e.nextElement();

                EncodedSequence encodeSim = SequenceUtil.encodeSerialReadSequence(ss.getSequence());

                SortedSet<Long> vals = new TreeSet(encodeSim.getEncodeMap().values()) ;
                Map<Long,Long> readMap = encodeSim.getEncodeMap();

                int count = 1;
                long index = 0;
                long indexB = 0;
                long roundMatch = 0;
                long roundNMatch = 0;
                //Obiect readKey = 0;

                for (Long val : vals){
                    //readKey = getKeyFromValue(readMap,val);
                    //System.out.println(key);
                    System.out.println("Number of mapping iteration : " + count++);
        //            System.out.println("the value is " + val + " Key is " + getKeyFromValue(read.getEncodeMap(),val));
                    if (chrMap.containsKey(getKeyFromValue(readMap,val))){

                        //System.out.println("Key " + getKeyFromValue(read.getEncodeMap(),val) +": Match at position " + chr.getEncodeMap().get(getKeyFromValue(read.getEncodeMap(),val)));
                        System.out.println("val : " + val);

                        System.out.println("Key " + getKeyFromValue(readMap,val) +": Match at position " + chrMap.get(getKeyFromValue(readMap,val)));
                        ///index = chrMap.get(getKeyFromValue(readMap,val)) - val;
                        ////index = chrMap.get(getKeyFromValue(readMap,val)) - roundNMatch;
                        indexB = (chrMap.get(getKeyFromValue(readMap,val))>>8) - (val>>8);
                        index = ((indexB-roundMatch)<<8)+(chrMap.get(getKeyFromValue(readMap,val))&255);
                        System.out.println("New indexB without add number of chromosome : " + indexB);
                        indexB = (indexB<<8)+(chrMap.get(getKeyFromValue(readMap,val))&255);
                        System.out.println("New indexB with add number of chromosome : " + indexB);

                        //System.out.println("Cross check with reference key : " + (getKeyFromValue(chrMap,(index+val))) );
                        System.out.println("Cross check with reference key : " + (getKeyFromValue(chrMap,(index))) );
                        //System.out.println("Position that map on reference : " + chrMap.get(getKeyFromValue(readMap,val)));

                        System.out.println("Align at : " + index );
                        //if (checkMap == 0){
                            //count++;
                        //}
                        //value[0][0] = roundMatch++;
                        //value[0][1] = read.getChrName();

                        if (roundMatch ==0 && outputMap.containsKey(index)){
                            roundMatch = 1 + (outputMap.get(index));
                            outputMap.put(index,roundMatch);
                        }
                        else{
                            roundMatch++;
                            outputMap.put(index,roundMatch);
                        }
                        
                        ///////outputMap.put(index,roundMatch);
                        /////result.addResultMap(index,roundMatch++);


                    }else{
                        System.out.println(" Key " + getKeyFromValue(readMap,val) + ": Not match");
                        //index = val;
                        roundMatch = 0;  // reset round
                        
                        //System.out.println("");
                    }   
                    
                }

                //result.addResultArray(outputMap);

                System.out.println("Key : "+index+ "Has : " + outputMap.get(index));
                result.addResult(outputMap,ss.getReadName());

            }
        }
        
        /* Save result to file */
        
        //result.writeToPath(chr.getFilePath(),"map");
        result.writeToPath(refGene.getPath(), "map");
        return result;
    }    
    

    public static MapResult mapGenomeShotgunV4(){
        
        return null;
    } 
    /*public static MapResult mapping(ReferenceSequence refGene, InputSequence read){
        
        MapResult result = new MapResult();
        Map outputMap;
        Enumeration<ShortgunSequence> e = read.seqs.elements();
        while(e.hasMoreElements()){
            
            ShortgunSequence ss = e.nextElement();
            outputMap = SequenceUtil.mapGenomeShotgunV3(refGene, ss);
            result.addResultArray(outputMap);
        }
        
        return result;
    }*/
    
    public static Map mapGenome(EncodedSequence chr, EncodedSequence read){
        
        
        //TreeMap<Long,Long> ref = new TreeMap(chr.getEncodeMap());
        //TreeMap<Long,Long> test = new TreeMap(read.getEncodeMap());
        SortedSet<Long> vals = new TreeSet(read.getEncodeMap().values()) ;
        Map<Long,Long> outputMap = new HashMap();
        Map<Long,Long> chrMap = chr.getEncodeMap();
        Map<Long,Long> readMap = read.getEncodeMap();
        MapResult result = new MapResult();
        
        int count = 1;
        long index = 0;
        long roundMatch = 1;
        long roundNMatch = 1;
        long dummyPos;
        long dummyIndex;
        long dummyChrName;
        Object dummykey;
        //Obiect readKey = 0;
        for (Long val : vals){
            //readKey = getKeyFromValue(readMap,val);
            //System.out.println(key);
            System.out.println("Number of mapping iteration : " + count++);
//            System.out.println("the value is " + val + " Key is " + getKeyFromValue(read.getEncodeMap(),val));
            dummykey = getKeyFromValue(readMap,val);
            
            //if (chrMap.containsKey(getKeyFromValue(readMap,val))){
            if (chrMap.containsKey(dummykey)){    
                //System.out.println("Key " + getKeyFromValue(read.getEncodeMap(),val) +": Match at position " + chr.getEncodeMap().get(getKeyFromValue(read.getEncodeMap(),val)));
                //System.out.println("Key " + getKeyFromValue(chrMap,val) +": Match at position " + chrMap.get(getKeyFromValue(readMap,val)));
                System.out.println("Key " + dummykey +": Match at position " + chrMap.get(dummykey));
                //index = chrMap.get(getKeyFromValue(readMap,val)) - val;
                
                /* Do more OOP for convert position and get chr number
                    Now I put chromosome number in position */
                
                
                
                dummyPos = chrMap.get(dummykey)>>8;
                dummyChrName = chrMap.get(dummykey)&255;
                index = dummyPos - (val>>=8);
                index = (index<<=8)+dummyChrName;
                System.out.println("Align at : " + index );
                //if (checkMap == 0){
                    //count++;
                //}
                outputMap.put(index,roundMatch++);
                
                
            }else{
                System.out.println(" Key " + getKeyFromValue(readMap,val) + ": Not match");
                //index = val;
                roundMatch = 1;  // reset round
                //System.out.println("");
            }   
            
        }
        
        System.out.println("Key : "+index+ "Has : "+outputMap.get(index)); // Print the number of align at index variable(Key) 
                                                                            // outputMap is hashmap the store the align index (Key) and number of base that align on that index.
        //TreeMap<Long> ref = new TreeMap(chr.);
        //System.out.println(test);
        //Enumeration e = read.getEncodeMap().keys();
        
        //Iterator e = test.;
        
        /*for (Map.Entry<Long,Long> entry : test.entrySet()){
            Long key = entry.getKey();
            
            if (ref.containsKey(key)){
                System.out.println("Key " + key +": Match at position " + ref.get(key));
            }else{
                System.out.println("Key " + key + ": Not match");
            }   
        }*/
            
            
            
       
        /*while(e.hasMoreElements()){
            
        
        //for (int i=0 ;i<read.getEncodeMap().size();i++){
            
            //System.out.println(e.nextElement());
            //System.out.println(chr.getEncodeMap().containsKey(e.nextElement()));
            if (chr.getEncodeMap().containsKey(e.nextElement())){
                System.out.println("Key " + e.nextElement()+": Match at position " +chr.getEncodeMap().get(e.nextElement()));
            }else{
                System.out.println("Key " + e.nextElement()+ ": Not match");
            }
        }*/
        return outputMap;
    }

    
    private static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
          if (hm.get(o).equals(value)) {
            return o;
          }
        }
        return null;    
    }
    
    
//    public static ReferenceAnnotation readExonIntron(String filename){
//        
//        ReferenceAnnotation ref = new ReferenceAnnotation();
//        Path path = Paths.get(filename);
//        String chr = null;
//        StringBuffer seq = new StringBuffer();
//        
//        try (BufferedReader reader = new BufferedReader(new FileReader(filename));) {
//            String line = null;
//            String setA[] = null;
//            String setB[] = null;
//            String chrName = null;
//            String geneName = null;
//            long startPos = 0;
//            long stopPos = 0;
//            int direction = 0;
//        
//            while ((line = reader.readLine()) != null) {
//
//                int count = 0;
//                setA = line.split("\\s+");
//                setB = line.split(";");             
//
//                for (String part : setA) {
//                    count++;
//                    if (count == 1){
//                        chrName = part;
//                    }
//                    else if(count == 4){
//                        startPos = Long.parseLong(part);
//                        //System.out.println(startPos);
//                    }
//                    else if (count == 5){
//                        stopPos = Long.parseLong(part);
//                    }
//                    else if (count ==6){
//                        direction = Integer.parseInt(part);
//                    }
//
//                }
//
//                count = 0;
//                for (String part : setB) {
//                    count++;
//                    if (count ==3){
//                        geneName = part;
//                    }
//                    //System.out.println(part);
//                }
//
//                System.out.println("Get :" + chrName+ "  "  + geneName+ "  " + startPos+ "  " + stopPos+ "  " + direction);
//
//                Annotation data = new Annotation(chrName,geneName,startPos,stopPos,direction);
//                ref.addData(data);
//            }
//        } catch (IOException x) {
//            System.err.format("IOException: %s%n", x);
//        }    
//
//        return ref;
//    }
    
     public static ReferenceAnnotation readAnnotationFile(String filename, String specificSource){
        /**
         * This function will read .gff3 file
         * Then store each Annotation that came from the specificSource in to Annotation object
         * After that store Annotation object in ReferenceAnnotation
         */
        
        Annotation anno;
        ReferenceAnnotation refAnno = new ReferenceAnnotation();
        Path path = Paths.get(filename);
        String chr = null;
        StringBuffer seq = new StringBuffer();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename));) {
            String line = null;
            String setA[] = null;
            String setB[] = null;
//            String chrName = null;
            String geneName = null;
            long startPos = 0;
            long stopPos = 0;
            int direction = 0;
        
            while ((line = reader.readLine()) != null) {
                
                if(line.charAt(0) == '#'){
                    
                    if(line.charAt(1) == '#'){
                        
                    }else{
                        
                        if(line.charAt(3) == '#'){
                            
                        }
                    }
                }else{
                    setA = line.split("\\s+");
                    
                    long chrName = Long.parseLong(setA[0]);
                    String source = setA[1];
                    String feature = setA[2];
                    long start = Long.parseLong(setA[3]);
                    long stop = Long.parseLong(setA[4]);
                    String score = setA[5];
                    String strand = setA[6];
                    int frame = Integer.parseInt(setA[7]);
                    String attribute = setA[8];
                    
                    if(source.equals(specificSource)){
                        anno = new Annotation(chrName,source,feature,start,stop,score,strand,frame,attribute);
                        long startCode = (chrName<<28)+start;
                        long stopCode = (chrName<<28)+stop;
                        
                        refAnno.putData(anno,startCode,stopCode);
                    }    
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }    

        return refAnno;
    }
    
//    public static ReferenceAnnotation randomExonIntron(ReferenceAnnotation ref){
//        ReferenceAnnotation output = new ReferenceAnnotation();
//        ArrayList<Annotation> data = ref.getdata();
//        Random rand = new Random();
//        //Vector<ExonIntron> output;
//        int r = rand.nextInt(data.size());
//        System.out.println("Vector size" + data.size());
//        
//        //int r = 500;
//        System.out.print("Pick" + data.elementAt(r).getdirection());
//        int iniguess = Math.abs(data.elementAt(r).getdirection());
//        int iniguesscheck = Math.abs(data.elementAt(r).getdirection());
//        System.out.println("iniGuesscheck" + iniguess);
//        int mainPoint = 0;
//        int axonNum = 0;
//        int rguess = r;
//        
//        while(true){
//            rguess++;
//            System.out.println("num of r : "+rguess);
//            int newguess = Math.abs(data.elementAt(rguess).getdirection());
//            System.out.println("num of guess : "+newguess);
//            if(newguess<iniguess||newguess==iniguess){
//                mainPoint = rguess-1;
//                axonNum = iniguess;
//                break;
//            }
//            else{
//                iniguess = newguess;
//            }
//        }
//        System.out.println("mainPoint : "+ mainPoint);
//        System.out.println("axonNum : "+ axonNum);
//        
//        String name = data.elementAt(mainPoint).getGeneName();
//        //output = new Vector<ExonIntron>();
//        while(axonNum>0){
//            
//            
//            if (data.elementAt(mainPoint).getGeneName().equalsIgnoreCase(name)){
//                // check in case that direction of exon is more than 2 but have just one exon EX. around element 1000
//                System.out.println("At Point : " + mainPoint);
//                System.out.println("Final Pick " + data.elementAt(mainPoint).getGeneName()+" Direction" + data.elementAt(mainPoint).getdirection());
//                //output.add(data.elementAt(mainPoint));
//                output.addData(data.elementAt(mainPoint));
//                axonNum--;
//                mainPoint--;
//            }else break;
//            
//            //axonNum--;
//            //mainPoint--;
//            //r++;
//        }
//        
//        //System.out.println("New vector size : "+output.getdata().size());
//        output.reverseorder();
//        
//        //System.out.println("New vector size : "+output.getdata().elementAt(0).getdirection());
//        //System.out.println("New vector size : "+output.getdata().elementAt(1).getdirection());
//        
//        return output;
//    }

    public static void createHistrogram(MapResult inResult, String readName){
        ArrayList indexArray = new ArrayList();
        
        indexArray = indexOfAll(readName,inResult.getReadName());
        
        System.out.println("\tHistogram of "+readName);
        
        for (int i = 0;i<indexArray.size();i++){
            int index = Integer.valueOf(indexArray.get(i).toString());
            long Position = Long.valueOf(inResult.getAlignPosition().get(index).toString());
            String readN = inResult.getReadName().get(index).toString();
            long match = Long.valueOf(inResult.getNumMatch().get(index).toString());
            long chrName = Long.valueOf(inResult.getchrNumber().get(index).toString().toString());
            String output = "";
            
            for (int j=0;j<match;j++){
                output +="*";
            }
            
            System.out.println(readN+"\t|\tchr"+chrName+"\t|\t"+Position+"\t| "+output);
            
        }     
    }
    


    public static ArrayList indexOfAll(String obj, ArrayList list){
        ArrayList indexList = new ArrayList();
        for (int i = 0; i < list.size(); i++)
            if(obj.equals(list.get(i)))
                indexList.add(i);
        return indexList;
    }

  
    public static long[] createComplimentStrand(long[] inRef){
        /*  Reconstruct whole chromosome sequence to compliment strand */
        /* inRef is contain with DNA sequence of specific chromosome which already encodeed in kmer lebgth and position */
        
        long mask = -268435456;
        long mask2 = 268435455;
        long mask36Bit = 68719476735L;
        long[] mersComp = Arrays.copyOf(inRef,inRef.length);
        
        inRef = null;
        System.gc();
        
        System.out.println("\n Create compliment strand ");
        System.out.println("******* First element before compliment : " + mersComp[0]);
       
        for(int i=0;i<mersComp.length;i++){
            long dummyMerPos = mersComp[i];
//            System.out.println("Check fullcode dummyMerPos: " + dummyMerPos);
            //long dummyMer = dummyMerPos>>28;
            long dummyMer = (dummyMerPos>>28)&mask36Bit;
            long dummyPos = dummyMerPos&mask2;

            /* Reconstruct compliment DNA sequence of whole chromosome */
//            System.out.println("Check mer sequemce befor compliment : " + dummyMer);
//            long dummyNewMer = (~dummyMer)&mask36Bit;
            
            String binaryMer = Long.toBinaryString(dummyMer);
            int kmer = binaryMer.length()/2;
//            System.out.println("Create compliment at : " + i);
//            System.out.println("Check binaryMer (before compliment) : " + binaryMer);
//            System.out.println("Check dummyPos : " + dummyPos);
            String revBin = new StringBuilder(binaryMer).reverse().toString(); //   reverse Sequence Ex 1011001 to 1001101 
//            System.out.println("Check revBin (reverse binary) : " + revBin);
            long revNum = new BigInteger(revBin,2).longValue(); //  Cast binary string to decimal number
//            System.out.println("Check revNum (decimal number of reverse binary) : " + revNum);
            long dummyNewMer = (~revNum)&mask36Bit; //  Create compliment of it
//            System.out.println("Check dummyNewMer (after compliment) : " + dummyNewMer);
            
            
            
            
//            System.out.println("Check mer sequemce after compliment : " + dummyNewMer);
//            System.out.println("Check Position before reverse : " + dummyPos);
            long dummyNewPos = (mersComp.length-1)-dummyPos; /* length-1 because assume array has 10 member ; length is 10 but maximum it index is 9 becaus index start at 0 */
            /* To get new inverse of position value, use this fomular (max index - old index) **Ex. old index is 9 so the inverse of it is (9 - 9) = 0 that's correct!! */
            // Replace to long[] 
//            System.out.println("Check Position after reverse : " + dummyNewPos);
//            System.out.println("Check max index is : " + (this.mers.length-1));
            long dummyNewMerPos = (dummyNewMer<<28)+dummyNewPos;
//            System.out.println("Check fullcode dummyNewMerPost : " + dummyNewMerPos);
            if (i%100000000==0){
                System.out.println("Create compliment at : " + i);
            }
//            System.out.println("check old code in mersComp before add " + mersComp[i]);
//            System.out.println("check code before add " + dummyNewMerPos);
            mersComp[i] = dummyNewMerPos;
//            System.out.println("check code after add " + mersComp[i]);
        }

        // re-sorted long[]
        System.out.println("******* First element after compliment unsorted  : " + mersComp[0]);
        Arrays.sort(mersComp);
        System.out.println("******* First element after compliment sorted : " + mersComp[0]);
        //return mersComp;
        return mersComp;
    }

    public static String decodeMer(long inCode, int kmer){
        
        long template = 68719476735L;
        long dummyInCode = inCode&template;
        String binaryCode = Long.toBinaryString(dummyInCode);/// transform wrong
        long lengthBinary = binaryCode.length();
        long fullLen = kmer*2;
        
        if(lengthBinary<fullLen){
            for(int i =0;i<(fullLen-lengthBinary);i++){
                binaryCode = "0"+binaryCode;
            }
        }

        int lenCode = binaryCode.length();
        String sequenceBase = "";
        String dummySequenceBase = "";
//        System.out.println("this is fulLen : " + fullLen);
//        System.out.println("this is lenBin : " + lengthBinary);
//        System.out.println("this is bin code inCode : " + inCode);
//        System.out.println("this is bin code check : " + binaryCode);
        for(int i=0;i<lenCode/2;i++){
            
            int indexF = i*2;
            int indexL = indexF+2;
            String dummyBase = binaryCode.subSequence(indexF, indexL).toString();
//            System.out.println("This is base code check : " + dummyBase);
            switch(dummyBase){
                case "00":
                   dummySequenceBase = "A"; // 00     
                   break;
               case "11": 
                   dummySequenceBase = "T"; // 11
                   break;
               case "01":               
                   dummySequenceBase = "C"; // 01 
                   break;
               case "10":               
                   dummySequenceBase = "G"; // 10 
                   break;
               default : 
                   dummySequenceBase = "N";
                   //return -1;
            }
//            System.out.println("this is dummy sequence check : round = "+i+" : dummySequenceBase = " + dummySequenceBase);
            sequenceBase = sequenceBase + dummySequenceBase;    
        }
        
        return sequenceBase;
    }
    
    public static String createCompliment(String inSeq){
        /* Create compliment of short length sequence of DNA */  
        CharSequence testSeq = inSeq;
        long mask36Bit = 68719476735L;
        int kmer = testSeq.length();
        long mask = ((long)(Math.pow(2,(kmer*2)))-1) ;
        
        System.out.println("createCompliment : Check merCode = " + kmer);
        System.out.println("createCompliment : Check merCode = " + mask);
        
        long merCode = encodeMer(inSeq,kmer);
        long dummyNewMer = (~merCode)&mask;
        
        System.out.println("createCompliment : Check merCode = " + merCode);
        System.out.println("createCompliment : Check newMer = " + dummyNewMer);
        
        String outSeq = decodeMer(dummyNewMer,kmer);
        
        
        return outSeq;
    }
    
    public static String createComplimentV2(String inSeq){
        /* Create compliment of short length sequence of DNA */  
        
        int lenSeq = inSeq.length();
        String outSeq = "";
        String dummyBase;
        
        //System.out.println("this s inSeq check :\t"+inSeq);
        for (int i=0;i<lenSeq;i++){

            switch(inSeq.charAt(i)){
                case 'a':
                case 'A':
                   dummyBase = "T"; // 00     
                   break;
                case 't':   
                case 'T': 
                   dummyBase = "A"; // 11
                   break;
                case 'c':
                case 'C':               
                   dummyBase = "G"; // 01 
                   break;
                case 'g':
                case 'G':               
                   dummyBase = "C"; // 10 
                   break;
                default : 
                   dummyBase = "N";
                   //return -1;
            }
            outSeq = outSeq+dummyBase; 
        }
        //System.out.println("this s outSeq check :\t"+outSeq);
        
        
        return outSeq;
    }
    
    public static String inverseSequence(String inSeq){
        CharSequence testSeq = inSeq;
        int kmer = testSeq.length();
        String invSeq = "";
        
        for (int i = 0;i<kmer;i++){
            invSeq = invSeq + String.valueOf(testSeq.charAt((testSeq.length()-1)-i));
        }
        
        return invSeq;
    }
    
    public static ShortgunSequence readInputFile(String filename) throws IOException {
        ShortgunSequence inSS = new ShortgunSequence(null);
        int count = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
    //    String seq = "";

        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
                   
            while ((line = reader.readLine()) != null) {
                String[] aon = line.split("\t");
                //if(line.charAt(0)=='>'){

    //                if(name!=null){
    //
    //                    System.out.println("Header name : "+name+" Size : "+seq.length());
    //
    //                    ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
    //
    //                    ref.addChromosomeSequence(c);
    //
    //                }
                    //seq = new StringBuffer();
                    //name = line.substring(1,line.length());

                //}else{

                    //seq.append(line.trim());


                //}
                System.out.println("check: " + aon[7]);
                inSS = new ShortgunSequence(aon[7]);
                name = "Unmapped_Test_Sample_" + count++;
                inSS.addReadName(name);
            }

            if(seq.length()>0){

        //        System.out.println("CHR : "+chr+" Size : "+seq.length());
                inSS = new ShortgunSequence(seq.toString());
                inSS.addReadName(name);


            }          
            

            return inSS;
        }
    }
    
    public static InputSequence readSampleFile(String filename) throws IOException {
        ShortgunSequence inSS = new ShortgunSequence(null);
        InputSequence tempInSS = new InputSequence();
        int count = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
    //    String seq = "";

        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
                   
            while ((line = reader.readLine()) != null) {
                String[] aon = line.split("\t");
                //if(line.charAt(0)=='>'){

    //                if(name!=null){
    //
    //                    System.out.println("Header name : "+name+" Size : "+seq.length());
    //
    //                    ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
    //
    //                    ref.addChromosomeSequence(c);
    //
    //                }
                    //seq = new StringBuffer();
                    //name = line.substring(1,line.length());

                //}else{

                    //seq.append(line.trim());


                //}
                System.out.println("check: " + aon[7]);
                inSS = new ShortgunSequence(aon[7]);
                name = "Unmapped_Test_Sample_" + count++;
                inSS.addReadName(name);
                tempInSS.addRead(inSS);
            }

            if(seq.length()>0){

        //        System.out.println("CHR : "+chr+" Size : "+seq.length());
                inSS = new ShortgunSequence(seq.toString());
                inSS.addReadName(name);


            }          
            

            return tempInSS;
        }
    }
    
    public static int getNumberSample(String filename) throws IOException {
        /* for specific input file 3661 3662 */
        
        int count = 0;
        int count2 = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        

        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
                   
            while ((line = reader.readLine()) != null) {
                String[] aon = line.split("\t");
                if(line.charAt(0)=='>'){
                    count++;   
                } 
            }
        }
        return count;
    }
    
    public static InputSequence readSampleFileV2(String filename, int readStart, int readLimit) throws IOException {
        /* for specific input file 3661 3662 */
        ShortgunSequence inSS = new ShortgunSequence(null);
        InputSequence tempInSS = new InputSequence();
        int count = 0;
        int count2 = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
        int actStart = readStart*2;     //this is actual start of line in file (compatible only specific file 3661 and 3662 .fasta file)
        int actStop = readLimit*2;
    //    String seq = "";

        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
                   
            while ((line = reader.readLine()) != null) {
                String[] aon = line.split("\t");
                if(line.charAt(0)=='>'){
                    
                    name = line.substring(1);
                    
                }else{
                    count++;
                    if(count >= readStart){
                        inSS = new ShortgunSequence(line.toString());
                        inSS.addReadName(name);
                        tempInSS.addRead(inSS);
                    }     
                }
                if(count==readLimit){
                    break;
                }
                

    //                if(name!=null){
    //
    //                    System.out.println("Header name : "+name+" Size : "+seq.length());
    //
    //                    ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
    //
    //                    ref.addChromosomeSequence(c);
    //
    //                }
                    //seq = new StringBuffer();
                    //name = line.substring(1,line.length());

                //}else{

                    //seq.append(line.trim());


                //}
//                System.out.println("check: " + aon[7]);
//                inSS = new ShortgunSequence(aon[7]);
//                name = "Unmapped_Test_Sample_" + count++;
//                inSS.addReadName(name);
//                tempInSS.addRead(inSS);
            }

            if(seq.length()>0){

        //        System.out.println("CHR : "+chr+" Size : "+seq.length());
                inSS = new ShortgunSequence(seq.toString());
                inSS.addReadName(name);


            }          
            

            return tempInSS;
        }
    }
    
    public static InputSequence readSamFile(String filename, ArrayList<String> inCriteria) throws IOException {
    /* for specific input file .sam file */
        ShortgunSequence inSS = new ShortgunSequence(null);
        InputSequence tempInSS = new InputSequence();
        int count = 0;
        int count2 = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
//        int actStart = readStart*2;     //this is actual start of line in file (compatible only specific file 3661 and 3662 .fasta file)
//        int actStop = readLimit*2;
    //    String seq = "";

        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
                   
            while ((line = reader.readLine()) != null) {
                String[] aon = line.split("\t");
                //System.out.println("check data "+aon[9]);
                String dummySeq = aon[9];
                if (dummySeq.toLowerCase().contains("n")){

                }else{
                    name = aon[0];
                    if(inCriteria.contains(name)){
                        inSS = new ShortgunSequence(dummySeq);
                        inSS.addReadName(name);
                        tempInSS.addRead(inSS);
                    } 
                }       
            }
        }
    
        return tempInSS;
    }
     
    public static InputSequence readSampleFileV2(String filename) throws IOException {
        /* for specific input file 3661 3662 (Whole file)*/
        ShortgunSequence inSS = new ShortgunSequence(null);
        InputSequence tempInSS = new InputSequence();
        int count = 0;
        int count2 = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
//        int actStart = readStart*2;     //this is actual start of line in file (compatible only specific file 3661 and 3662 .fasta file)
//        int actStop = readLimit*2;
    //    String seq = "";

        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
                   
            while ((line = reader.readLine()) != null) {
                String[] aon = line.split("\t");
                if(line.charAt(0)=='>'){
                    
                    name = line.substring(1);
                    
                }else{
                    //count++;
//                    if(count >= readStart){
                    inSS = new ShortgunSequence(line.toString());
                    inSS.addReadName(name);
                    tempInSS.addRead(inSS);
//                    }     
                }
//                if(count==readLimit){
//                    break;
//                }
                

    //                if(name!=null){
    //
    //                    System.out.println("Header name : "+name+" Size : "+seq.length());
    //
    //                    ChromosomeSequence c = new ChromosomeSequence(ref,chr,seq);
    //
    //                    ref.addChromosomeSequence(c);
    //
    //                }
                    //seq = new StringBuffer();
                    //name = line.substring(1,line.length());

                //}else{

                    //seq.append(line.trim());


                //}
//                System.out.println("check: " + aon[7]);
//                inSS = new ShortgunSequence(aon[7]);
//                name = "Unmapped_Test_Sample_" + count++;
//                inSS.addReadName(name);
//                tempInSS.addRead(inSS);
            }

            if(seq.length()>0){

        //        System.out.println("CHR : "+chr+" Size : "+seq.length());
                inSS = new ShortgunSequence(seq.toString());
                inSS.addReadName(name);


            }          
            

            return tempInSS;
        }
    }
    
    public static AlignmentResultRead readAlignmentReport(String filename) throws IOException {
        ArrayList listChr = new ArrayList();
        ArrayList listPos = new ArrayList();
        ArrayList listStrand = new ArrayList();
        ArrayList listResultCode = new ArrayList();
        ShortgunSequence inSS = new ShortgunSequence(null);
        AlignmentResultRead alnResult = new AlignmentResultRead();
        int count = 0;
        int count2 = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
//        int actStart = readStart*2;     //this is actual start of line in file (compatible only specific file 3661 and 3662 .fasta file)
//        int actStop = readLimit*2;
    //    String seq = "";
        
        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
            String[] data = null;      
            while ((line = reader.readLine()) != null) {
                
                if(line.charAt(0)=='>'){
                    inSS = new ShortgunSequence(null);
                    name = line.substring(1);
//                    System.out.println("Read name got : " + name);
                    listChr = new ArrayList();
                    listPos = new ArrayList();
                    listStrand = new ArrayList();
                    listResultCode = new ArrayList();
                }else{
                    data = line.split(";");
                
                
                    for(int i=0;i<data.length;i++){
                        String[] dummyData = data[i].split(",");
//                        System.out.println("data check : "+dummyData[0] + " "+ dummyData[1] +" "+dummyData[2]);                      
                        listChr.add(Long.parseLong(dummyData[0]));
                        listPos.add(Long.parseLong(dummyData[1]));
                        listStrand.add(dummyData[2]);
                    }
                    inSS.addReadName(name);
                    inSS.addListChr(listChr);
                    inSS.addListPos(listPos);
                    inSS.addListStrand(listStrand);
                    alnResult.addResult(inSS);
                }
                
            }
            
            return alnResult;
        }
    }
    
    public static AlignmentResultRead readAlignmentReportV2(String filename, int readLength, int mer) throws IOException {
        /**
         *  Suitable for version 3 data structure (data structure that has iniIdx in its)
         */
        
        ArrayList<Integer> listChr = new ArrayList();
        ArrayList<Long> listPos = new ArrayList();
        ArrayList<Long> listLastPos = new ArrayList();
        ArrayList<String> listStrand = new ArrayList();
        ArrayList<Integer> listNumCount = new ArrayList();
        ArrayList<Integer> listIniIdx = new ArrayList();
        ArrayList listResultCode = new ArrayList();
        ShortgunSequence inSS = new ShortgunSequence(null);
        AlignmentResultRead alnResult = new AlignmentResultRead();
        int count = 0;
        int count2 = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
//        int actStart = readStart*2;     //this is actual start of line in file (compatible only specific file 3661 and 3662 .fasta file)
//        int actStop = readLimit*2;
    //    String seq = "";
        
        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
            String[] data = null;      
            while ((line = reader.readLine()) != null) {
                
                if(line.charAt(0)=='>'){
                    inSS = new ShortgunSequence(null);
                    name = line.substring(1);
//                    System.out.println("Read name got : " + name);
                    listChr = new ArrayList();
                    listPos = new ArrayList();
                    listLastPos = new ArrayList();
                    listStrand = new ArrayList();
                    listNumCount = new ArrayList();
                    listIniIdx = new ArrayList();
                    listResultCode = new ArrayList();
                }else{
                    data = line.split(";");
                
                
                    for(int i=0;i<data.length;i++){
                        String[] dummyData = data[i].split(",");
//                        System.out.println("data check : "+dummyData[0] + " "+ dummyData[1] +" "+dummyData[2]);                      
                        listChr.add(Integer.parseInt(dummyData[0]));
                        listPos.add(Long.parseLong(dummyData[1]));
                        listStrand.add(dummyData[2]);
                        listNumCount.add(Integer.parseInt(dummyData[3]));
                        listIniIdx.add(Integer.parseInt(dummyData[4]));
                        
                        int numBase = (mer+Integer.parseInt(dummyData[3]))-1;
                        long lastPos = (Long.parseLong(dummyData[1]) + numBase)-1;
                        
                        listLastPos.add(lastPos);
                        
                    }
                    inSS.addReadName(name);
                    inSS.addListChr(listChr);
                    inSS.addListPos(listPos);
                    inSS.addListLastPos(listLastPos);
                    inSS.addListStrand(listStrand);
                    inSS.addListNumMatch(listNumCount);
                    inSS.addListIniIdx(listIniIdx);
                    alnResult.addResult(inSS);
                } 
            }
            
            return alnResult;
        }
    }
    
    public static AlignmentResultRead readBinaryAlignmentReportV2(String filename, int readLength, int mer){
        /**
         *  Suitable for version 3 data structure (data structure that has iniIdx in its)
         * we have specify marker which will be use for decode the alignment result code (it specific for v3 data structure only)
         */
        
        
        long mask_chrIdxStrandAln = 4398046511103L;      //  Do & operation to get aligncode compose of chr|Idx|strand|alignposition from value contain in alignmentResultMap
        long mask = 268435455;
        
        
        ArrayList<Integer> listChr = new ArrayList();
        ArrayList<Long> listPos = new ArrayList();
        ArrayList<Long> listLastPos = new ArrayList();
        ArrayList<String> listStrand = new ArrayList();
        ArrayList<Integer> listNumCount = new ArrayList();
        ArrayList<Integer> listIniIdx = new ArrayList();
        ArrayList listResultCode = new ArrayList();
        ShortgunSequence inSS = new ShortgunSequence(null);
        AlignmentResultRead alnResult = new AlignmentResultRead();
        int count = 0;
        int count2 = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
//        int actStart = readStart*2;     //this is actual start of line in file (compatible only specific file 3661 and 3662 .fasta file)
//        int actStop = readLimit*2;
    //    String seq = "";
        
        StringBuffer seq = new StringBuffer();
        
        File inputFile = new File(filename);
        boolean eof = false;
        
        try{
            
            DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
            
            while(!eof){
                String readName = is.readUTF();
                int resultSize = is.readInt();
                
                inSS = new ShortgunSequence(null);               
                listChr = new ArrayList();
                listPos = new ArrayList();
                listLastPos = new ArrayList();
                listStrand = new ArrayList();
                listNumCount = new ArrayList();
                listIniIdx = new ArrayList();
                
                for(int i=0;i<resultSize;i++){
                    long code = is.readLong();  // code has structure like this [count|Chr|strand|alignPosition]                         
                    long numCount = code>>42;                                               //Shift 34 bit to get count number
                    long chrIdxStrandAln = code&mask_chrIdxStrandAln;
                    long alignPos = chrIdxStrandAln&mask;                                      // And with 28bit binary to get position
                    long chrNumber = chrIdxStrandAln>>37;
                    long iniIdx = (chrIdxStrandAln>>29)&255;

                    String strandNot = "no";                                                // Identify the strand type of this align Position
                    if(((chrIdxStrandAln>>28)&1) == 1){
                        strandNot = "+";
                    }else if(((chrIdxStrandAln>>28)&1) == 0){
                        strandNot = "-";
                    }
                    
                    listChr.add((int)chrNumber);
                    listPos.add(alignPos);
                    listStrand.add(strandNot);
                    listNumCount.add((int)numCount);
                    listIniIdx.add((int)iniIdx);

                    int numBase = (mer+(int)numCount)-1;
                    long lastPos = (alignPos + numBase)-1;

                    listLastPos.add(lastPos);
                }
                
                inSS.addReadName(readName);
                inSS.addListChr(listChr);
                inSS.addListPos(listPos);
                inSS.addListLastPos(listLastPos);
                inSS.addListStrand(listStrand);
                inSS.addListNumMatch(listNumCount);
                inSS.addListIniIdx(listIniIdx);
                alnResult.addResult(inSS);
            }
        }catch(EOFException e){
            eof = true;
        }
        
        catch (FileNotFoundException e) {
            System.out.println("Couldn't Find the File");

            System.exit(0);}
        

        catch(IOException e){
           System.out.println("An I/O Error Occurred");
             System.exit(0);

        }
            
            return alnResult;
        
    }
    
    public static InputSequence readSamFile(String filename) throws IOException {
        /* for specific input file .sam file */
        ShortgunSequence inSS = new ShortgunSequence(null);
        InputSequence tempInSS = new InputSequence();
        int count = 0;
        int count2 = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(filename);
        String name = null;
//        int actStart = readStart*2;     //this is actual start of line in file (compatible only specific file 3661 and 3662 .fasta file)
//        int actStop = readLimit*2;
    //    String seq = "";

        StringBuffer seq = new StringBuffer();

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
                   
            while ((line = reader.readLine()) != null) {
               String[] aon = line.split("\t");
               //System.out.println("check data "+aon[9]);
               String dummySeq = aon[9];
               if (dummySeq.toLowerCase().contains("n")){
                   
               }else{
                   name = aon[0];
                   inSS = new ShortgunSequence(dummySeq);
                   inSS.addReadName(name);
                   tempInSS.addRead(inSS);
               }       
            }
        }
    
        return tempInSS;
    }
    
    public static EncodedSequence createLocalAlignmentReference(ShortgunSequence inSS,int kmer,int sliding){
        EncodedSequence refSS = new EncodedSequence();  
        
        String sb = inSS.getSequence();


        int n = (sb.length()-kmer)/sliding;       
        long cmer = -1;
        long mask = 0; 
        int count = 0;

        long list[] = new long[n]; // Pre - allocate Array by n


        for(int i =0;i<kmer;i++)mask=mask*4+3;

//        System.out.println(mask);


        for(int i =0;i<n;i++){

            long pos = i*sliding;
            char chx = sb.charAt(i*sliding+kmer-1);
            if(chx!='N'){
                if(cmer==-1){
                    String s = sb.substring(i*sliding,i*sliding+kmer);
                    cmer = encodeMer(s,kmer);
                }else{

                    int t =-1;
                    switch(chx){
                        case 'A':
                        case 'a':
                            t=0; // 00
                            break;
                        case 'T':
                        case 't': 
                            t=3; // 11
                            break;
                        case 'C':
                        case 'c':
                            t=1; // 01 
                            break;
                        case 'G':
                        case 'g':
                            t=2; // 10 
                            break;
                        default : 
                            t=-1;
                        break;

                    }
                    if(t>=0){

                        cmer *= 4;
                        cmer &= mask;
                        cmer += t;

                    }else{
                        cmer = -1;
                        i+=kmer;
                    }  
                }     
//            if(i%1000000==0)System.out.println("Encode "+inSS.getReadName()+" "+i*sliding);

            if(cmer>=0){  
                long x = (cmer<<(64- kmer*2))|pos;
                list[count++] = x;
            }

            }
        }


        Arrays.sort(list);

        refSS.setMers(list);

        list=null;
        System.gc();

        return refSS;
    }

    public static Map<Integer,ArrayList<String>> localAlignment(InputSequence inSeq,int kmer,int sliding, int matchQuality){
        int numMatchQ = matchQuality;
        ArrayList<String> checkList = new ArrayList();
        Map<Integer,ArrayList<String>> preGroupMap = new HashMap();
        Map<String,ArrayList<String>> preGroupUnMap = new HashMap();
        AlignmentResultRead align = new AlignmentResultRead();
        Vector<ShortgunSequence> listSS = inSeq.getInputSequence();
        ArrayList<AlignmentResultRead> listAlignmentResultRead = new ArrayList();
        int numGroup = 0;
        for(int mainLoop=0;mainLoop<listSS.size();mainLoop++){                  // loop over shortgun sequence which alternate to be reference
                        
            ShortgunSequence dummyMainSS = listSS.get(mainLoop);
            if(checkList.contains(dummyMainSS.getReadName())!=true){
                
               
                EncodedSequence localRef = createLocalAlignmentReference(dummyMainSS,kmer,sliding);
                //inSeq.getInputSequence().remove(mainLoop)
                InputSequence localInput = new InputSequence();
                for(int minorLoop=mainLoop+1;minorLoop<listSS.size();minorLoop++){
                    
                    ShortgunSequence dummySubSS = listSS.get(minorLoop);
                    if(checkList.contains(dummySubSS.getReadName())!=true){
                        localInput.addRead(listSS.get(minorLoop));
                    } 
                }   

                Aligner aligner = AlignerFactory.getAligner();          // Will link to BinaryAligner
                align = aligner.localAlign(localRef, localInput, kmer, mainLoop);  // function align is located in binary aligner

                /**
                 * Bring align to analyze unMap read for this dummyMainSS
                 */
                if(align != null){
                    align.sortCountCutLocalResultForMap(numMatchQ);
                    ArrayList<String> dummyMapList = align.getMapList();
                    ArrayList<String> dummyUnMapList = align.getUnMapList();
                    
                    if(dummyUnMapList.isEmpty()!=true){
                        preGroupUnMap.put(dummyMainSS.getReadName(), dummyUnMapList);
                    }
                    if(dummyMapList.isEmpty()!=true){
                        numGroup++;
                        dummyMapList.add(dummyMainSS.getReadName());
                        preGroupMap.put(numGroup, dummyMapList);
                    }
                    

                    /***********************************************************/
                    checkList.add(dummyMainSS.getReadName());
                    checkList.addAll(dummyMapList);
                }
            }
            if(align.getUnMapList().isEmpty()!=true&&align.getMapList().isEmpty()!=true){
                listAlignmentResultRead.add(align);
            }
        }
        
        /**
         * At this point preGroupMap is successfully fill
         * Next step, bring preGroupMap to classify for group and store group in list
         */
        
        

        return preGroupMap; 
    }
    
    public static VariationResult analysisResultFromFile(String filename, int merLength, int readLength, int allowOverLap ) throws IOException{
        /**
        * Suitable with result format only (result format is a file that store peak result arrange by sample order (come first be the first). the peak result is in format data structure V3
        * startIndex and stopIndex defined in this method is the index of DNA base in Read Ex. read length 100 base will has index 0 to 99 and has index of mer 0 to 83 [83 is come from 100 - 18]
        * allowOverLap is indicate the number of DNA base that allow to over lap at the junction
        * 
        * This function is stand for extract data and flag each peak that does not have green count with false and vise versa
        * It also calculate start and stop index then create MapF and MapB which has been use for detect variation
        * then passing the information to detect variation function and store result to VariationResult object
        * The input data must be sorted by order of Read (same read will group together) and iniIndex (numeric order)
        */
        
        VariationResult varResult = new VariationResult();
        varResult.addMerLength(merLength);
        varResult.addReadLength(readLength);
        
        Charset charset = Charset.forName("US-ASCII");
        //String[] ddSS = filename.split(".");
        String saveFileName = filename.split("\\.")[0] + "_ClusterGroup.txt";
        Path path = Paths.get(filename);

        StringBuffer seq = new StringBuffer();
        ArrayList<String> inData = new ArrayList();    
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;    
            int count = 0;

            System.out.println("reading");
            while ((line = reader.readLine()) != null) {

                inData.add(line);
                count++;
                if(count%1000000==0){
                    System.out.println(count + " line past");
                    //System.out.println("Recent chromosome: " + numChr);
                }       

            }
//            writeClusterGroupToFile(filename,listGroup);
        }
        
        System.out.println(" Done read ");
        
        ArrayList<String> selectData = new ArrayList();                     // Store list of long string that contain peak information (list of all peak in specific read)
        ArrayList<Byte> selectChr = new ArrayList();                        // store list of chr number same order as selectData
        ArrayList<Boolean> selectGreenChar = new ArrayList();
        Map<Integer,ArrayList<Integer>> mapF = new LinkedHashMap();
        Map<Integer,Integer> mapB = new LinkedHashMap();
        int index = 0;
        String oldReadName = null;
        
        for(int i=0;i<inData.size();i++){
            String dataGet = inData.get(i);
            
            /***    Extract data    ****/
            String[] data = dataGet.split(",");           
            byte numChr = Byte.parseByte(data[0]);
            long iniPos = Long.parseLong(data[1]);
            long lastPos = Long.parseLong(data[2]);
            byte numG = Byte.parseByte(data[3]);
            byte numY = Byte.parseByte(data[4]);
            byte numO = Byte.parseByte(data[5]);
            byte numR = Byte.parseByte(data[6]);
            String strand = data[7];
            byte iniIdx = Byte.parseByte(data[8]);
            String readName = data[9];
            byte snpFlag = Byte.parseByte(data[10]);
            /******************************/
            
            int matchCount = numG+numY+numO+numR;
            int startIndex = iniIdx;
            int stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
            boolean greenChar = false;
            
            if(numG>0){
                greenChar = true;
            }
            
            
            
//            if(strand.equals("-")){
//                startIndex = readLength - (iniIdx+(merLength+matchCount-1));
//                stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
//            }
            
            if(readName.equals(oldReadName)){
                /* Same set of read (continue add data) */
                index++;
                selectData.add(dataGet);
                selectChr.add(numChr);
                selectGreenChar.add(greenChar);
                mapB.put(stopIndex, index);
                if(mapF.containsKey(startIndex)){
                    ArrayList<Integer> val = mapF.get(startIndex);
                    val.add(index);
                    mapF.put(startIndex, val);
                }else{
                    ArrayList<Integer> val = new ArrayList();
                    val.add(index);
                    mapF.put(startIndex, val);
                }
                
            }else{
                /**
                 * Found new set of Read 
                 * 1. Do detect variation
                 * 2. reset 
                 * 3. Add new set of data       
                 */
                if(selectData.size()!=0){
                    /* Case check for avoid first time */
                     Map<Integer,ArrayList<String[]>> variation = detectVariation(selectData,selectChr,selectGreenChar,mapF,mapB,merLength,readLength,allowOverLap);
                     varResult.addVariationMap(variation);
                }

                selectData = new ArrayList();
                selectChr = new ArrayList();
                selectGreenChar = new ArrayList();
                mapF = new LinkedHashMap();
                mapB = new LinkedHashMap();
                
                index = 0;
                selectData.add(dataGet);
                selectChr.add(numChr);
                selectGreenChar.add(greenChar);
                mapB.put(stopIndex, index);
                if(mapF.containsKey(startIndex)){
                    ArrayList<Integer> val = mapF.get(startIndex);
                    val.add(index);
                    mapF.put(startIndex, val);
                }else{
                    ArrayList<Integer> val = new ArrayList();
                    val.add(index);
                    mapF.put(startIndex, val);
                }
                
            }
            
            oldReadName = readName;
            
            
            
            
            /* Detect SNP case and think about doing something with out of criteria peaks */
          
            
            
        } 
        
        return varResult;
    }
    
    public static Map<Integer,ArrayList<String[]>> detectVariation(ArrayList<String> selectData , ArrayList<Byte> selectChr , ArrayList<Boolean> selectGreenChar , Map<Integer,ArrayList<Integer>> mapF , Map<Integer,Integer> mapB , int merLength , int readLength , int allowOverlapBase){
        /**
         * the variable in Map<Integer,ArrayList<String[]>> 
         * => Integer is represent type of variation
         *      '0' = SNP contain and others
         *      '1' = fusion
         *      '2' = large or small indel
         *      '3' = others (wasted)
         * => ArrayList<String[]> is represent the result of variation
         */
        
        Map<Integer,ArrayList<String[]>> variation = new LinkedHashMap();
        ArrayList<Integer> indexCheckList = new ArrayList();
        
        /**
         * Begin variable detection 
         * Loop each String data (peak information)
         */
        for(int i=0;i<selectData.size();i++){
            String dataGet = selectData.get(i);
            
            /***    Extract data    ****/
            String[] data = dataGet.split(",");
            byte numChr = Byte.parseByte(data[0]);
            long iniPos = Long.parseLong(data[1]);
            long lastPos = Long.parseLong(data[2]);
            byte numG = Byte.parseByte(data[3]);
            byte numY = Byte.parseByte(data[4]);
            byte numO = Byte.parseByte(data[5]);
            byte numR = Byte.parseByte(data[6]);
            String strand = data[7];
            byte iniIdx = Byte.parseByte(data[8]);
            String readName = data[9];
            byte snpFlag = Byte.parseByte(data[10]);
            /******************************/
            
            int matchCount = numG+numY+numO+numR;  
  
            int startIndex = iniIdx;
            int stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
            
//            if(strand.equals("-")){
//                startIndex = readLength - (iniIdx+(merLength+matchCount-1));
//                stopIndex = ((startIndex+matchCount)-1)+(merLength-1);
//            }
            
            int expectNextIndex = stopIndex+1;
            int limitExpectNextIndex = expectNextIndex-allowOverlapBase;
            boolean greenChar = false;
            if(numG>0){
                greenChar = true;
            }
            /**
             * Check junction
             */
            
            ArrayList<String> wastedCheckList = new ArrayList();
            
            for(expectNextIndex = expectNextIndex ; expectNextIndex>=limitExpectNextIndex ; expectNextIndex--){       // this for loop has been use to vary the number of expectNextIndex in the range of allowOverLapBase
                
                if(mapF.containsKey(expectNextIndex)&&snpFlag==0){
                    /**
                     * Junction found
                     */
                    ArrayList<Integer> listSelectIndex = mapF.get(expectNextIndex);
                    for(int j=0;j<listSelectIndex.size();j++){
                        int selectIndex = listSelectIndex.get(j);
                        String selectRead = selectData.get(selectIndex);
                        byte numChrB = selectChr.get(selectIndex);
                        boolean greenCharB = selectGreenChar.get(selectIndex);


                        /**
                         * Check fusion or large indel
                         */
                        if(numChr == numChrB){

                            if(greenCharB == true || greenChar == true){                // case check to ensure that at least one side is green characteristic 
                                /**
                                * large or small indel
                                */
                                String[] pairedPeak = new String[2];  
                                pairedPeak[0]=dataGet;
                                pairedPeak[1]=selectRead;

                                if(variation.containsKey(2)){
                                    ArrayList<String[]> listPP = variation.get(2);
                                    listPP.add(pairedPeak);
                                    variation.put(2, listPP);
                                }else{
                                    ArrayList<String[]> listPP = new ArrayList();
                                    listPP.add(pairedPeak);
                                    variation.put(2, listPP);
                                } 
                            }else{

                                /**
                                 * Has no green at all (wasted) type 3
                                 */
                                String[] pairedPeak = new String[2];
                                String pairedCheck = dataGet+"|"+selectRead;
                                pairedPeak[0]=dataGet;
                                pairedPeak[1]=selectRead;

                                if(variation.containsKey(3)){
                                    ArrayList<String[]> listPP = variation.get(3);
                                    if(wastedCheckList.contains(pairedCheck)!=true){
                                        listPP.add(pairedPeak);
                                        wastedCheckList.add(pairedCheck);
                                    }                                                            
                                    variation.put(3, listPP);
                                }else{
                                    ArrayList<String[]> listPP = new ArrayList();
                                    if(wastedCheckList.contains(pairedCheck)!=true){
                                        listPP.add(pairedPeak);
                                        wastedCheckList.add(pairedCheck);
                                    }  
                                    variation.put(3, listPP);
                                } 
                            }

                        }else if(numChr != numChrB){
                            /**
                             * Fusion
                             */
                            if(greenCharB == true || greenChar == true){
                                String[] pairedPeak = new String[2];  
                                pairedPeak[0]=dataGet;
                                pairedPeak[1]=selectRead;

                                if(variation.containsKey(1)){
                                    ArrayList<String[]> listPP = variation.get(1);
                                    listPP.add(pairedPeak);
                                    variation.put(1, listPP);
                                }else{
                                    ArrayList<String[]> listPP = new ArrayList();
                                    listPP.add(pairedPeak);
                                    variation.put(1, listPP);
                                }
                            }else{
                                /**
                                 * Has no green at all (wasted) type 3
                                 */
                                String[] pairedPeak = new String[2];
                                String pairedCheck = dataGet+"|"+selectRead;
                                pairedPeak[0]=dataGet;
                                pairedPeak[1]=selectRead;

                                if(variation.containsKey(3)){
                                    ArrayList<String[]> listPP = variation.get(3);
                                    if(wastedCheckList.contains(pairedCheck)!=true){
                                        listPP.add(pairedPeak);
                                        wastedCheckList.add(pairedCheck);
                                    }                                 
                                    variation.put(3, listPP);
                                }else{
                                    ArrayList<String[]> listPP = new ArrayList();
                                    if(wastedCheckList.contains(pairedCheck)!=true){
                                        listPP.add(pairedPeak);
                                        wastedCheckList.add(pairedCheck);
                                    }                                 
                                    variation.put(3, listPP);
                                } 
                            }

                        }
                    }
                }
            }
            if(snpFlag >= 1){

                if(greenChar == true){
                    String[] pairedPeak = new String[2];
                    pairedPeak[0]=dataGet;

                    if(variation.containsKey(0)){
                        ArrayList<String[]> listPP = variation.get(0);
                        listPP.add(pairedPeak);
                        variation.put(0, listPP);
                    }else{
                        ArrayList<String[]> listPP = new ArrayList();
                        listPP.add(pairedPeak);
                        variation.put(0, listPP);
                    }
                }else{
                    /**
                     * Has no green at all (wasted) type 3
                     * No need to have a check contain of wasted pair (No chance to get repeat because it has no for loop)
                     */
                    String[] pairedPeak = new String[2];
                    String pairedCheck = dataGet;
                    pairedPeak[0]=dataGet;

                    if(variation.containsKey(3)){
                        ArrayList<String[]> listPP = variation.get(3);
                        listPP.add(pairedPeak);
                        variation.put(3, listPP);
                    }else{
                        ArrayList<String[]> listPP = new ArrayList();
                        listPP.add(pairedPeak);
                        variation.put(3, listPP);
                    }
                }

            }
                
            
            
        }
        
        return variation;
    }
    
    public static InputSequence createShortReadFromLongSequence(String seq,int readLength,String fileName) throws IOException{
        /**
         * 
         */
        String filename = fileName+".fa";
        FileWriter writer;  
        File f = new File(filename); //File object        
        if(f.exists()){
//            ps = new PrintStream(new FileOutputStream(filename,true));
            writer = new FileWriter(filename,true);
        }else{
//            ps = new PrintStream(filename);
            writer = new FileWriter(filename);
        }
        
        InputSequence inputSeq = new InputSequence();
        String readName = "Read";
        int seqLen = seq.length();
        for(int ini = 0;ini<=seqLen-readLength;ini++){
            
            if(ini<10){
                readName = "Read0"+ini;
            }else{
                readName = "Read"+ini;
            }
            String cutSeq = (String)seq.subSequence(ini, ini+readLength);
            ShortgunSequence newSeq = new ShortgunSequence(cutSeq);
            newSeq.addReadName(readName);
            newSeq.addReadLength(readLength);
            inputSeq.addRead(newSeq);
            
            writer.write(">"+readName);
            writer.write("\n");
            writer.write(cutSeq);
            writer.write("\n");
        }
        writer.flush();
        writer.close();
        return inputSeq;
    }
    
    public static void markAnnotation(ReferenceAnnotation inRef,VariationResult inVar){
        /** 
         * get arraylist of variation 
         * get breakpoint and try to map from inRef
         * by check contain of key (2 layer check)
         */
        
        
    }
    
    public static ArrayList<Map<Long,Long>> createRepeatMarkerReference(ChromosomeSequence chr, int mer) throws IOException{
        /**
         * Create repeat Index and repeat Marker
         * Or read from file if file exist
         */
        
        long maskMinus28bit = -268435456; // Do & operation to get mer  (it is minus 28 bit plus 1 bit)
        long mask28bit = 268435455; // Do & operation to get position (28 bit value)
        long mask36bit = 68719476735L;
        int sliding = 1;
        long oldCodeMer = 0;
        long newCodeMer = 0;
        long repeatCodeMer = 0;
        long uniqueMer = 0;
        long distant = 0;
        long distantB = 0;
        long[] merPos;
        Map<Long,Boolean> repeatIndex = new LinkedHashMap();
        Map<Long,Integer> merPosIndex = new LinkedHashMap();                    // Store merPos as key and index as Value. "index" is number that indicate the index of merPos on long[] merPos
        Map<Long,Long> repeatMarkerFront = new LinkedHashMap();
        Map<Long,Long> repeatMarkerBack = new LinkedHashMap();
        ArrayList<Long> listRepeatMer = new ArrayList();                        // use for back unique part
        ArrayList<Map<Long,Long>> listRepeatMarker = new ArrayList();           // list of repeat marker first element is repeatMarker front unique and second is repeatMarker back unique 
        
        
//        Enumeration<ChromosomeSequence> chrs = ref.getChromosomes().elements();
//        
//        while(chrs.hasMoreElements()){
            
        /**
         * Create Repeat Index by Chromosome and Map<Long,Long> of key=merPos and value=index of long[] merPos 
         */
           
//            ChromosomeSequence chr = chrs.nextElement();
        File indexFile = new File(chr.getFilePath() + "_repeatIdx.bin");
        File merPosIndexFile = new File(chr.getFilePath() + "_merPosIdx.bin");
        System.out.println("Chromosome: "+chr.getName());

        if(indexFile.exists()!=true){
            System.out.println("Begin create repeat index");
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
                                                // Loop chromosome contain in ReferenceSequence
            long startTime = System.currentTimeMillis();

            int count = 0;

            EncodedSequence encoded = encodeSerialChromosomeSequenceV3(chr,mer);            // encoded selected chromosome (just for sure it is encode)
            long chrnumber = chr.getChrNumber();
            merPos = encoded.getMers();

            for(int i=0;i<merPos.length;i++){

                long codeMerPos = merPos[i];
                long codeMer = (codeMerPos>>28)&mask36bit;
                merPosIndex.put(codeMer, i);
                
                if(oldCodeMer == codeMer && repeatCodeMer != codeMer){      // check for repeat codeMer. if it repeat oldCodeMer and codeMer must equal more than one time. So, we can pick it from second time eaual and add to index file
                    repeatCodeMer = codeMer;
                    os.writeLong(codeMer);
                    repeatIndex.put(codeMer, true);
                }

                oldCodeMer = codeMer;

            }
            os.close();
        }else if(indexFile.exists()==true && merPosIndexFile.exists()==true){
            System.out.println("Begin read repeat index");
            boolean eof = false;

            try{
                DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));

                while(!eof){
                    long repeatMer = is.readLong();
                    repeatIndex.put(repeatMer, true);
                }   
            }
            catch(EOFException e){
                eof = true;
            }
            
            try{
                DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(merPosIndexFile)));

                while(!eof){
                    long mP = is.readLong();
                    int index = is.readInt();
                    merPosIndex.put(mP, index);
                }   
            }
            catch(EOFException e){
                eof = true;
            }
        }
        
        if(merPosIndexFile.exists() != true){
            
            DataOutputStream osMPI = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(merPosIndexFile)));
            for(Map.Entry<Long,Integer> entry : merPosIndex.entrySet()){
               long mP = entry.getKey();
               int index = entry.getValue();
               osMPI.writeLong(mP);
               osMPI.writeInt(index);
            }
            osMPI.close();       
        }
        
        
        
        /**
         * Create repeat marker (concatenate marker)
         */
        
        
        

        /**
         * Create repeat Marker by Chromosome
         */
        File repeatMarkerFrontFile = new File(chr.getFilePath() + "_repeatMarkerFront.bin");
        File repeatMarkerBackFile = new File(chr.getFilePath() + "_repeatMarkerBack.bin");

        if(!repeatMarkerFrontFile.exists()){
            System.out.println("Begin create repeat marker");
            DataOutputStream osF = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(repeatMarkerFrontFile))); // create object for output data stream
            DataOutputStream osB = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(repeatMarkerBackFile)));
            StringBuffer sb = chr.getSequence();

            int n = (sb.length()-mer)/sliding;       
            long cmer = -1;
            long mask = 0; 
            int count = 0;

            long recentUnique = 0;

            long list[] = new long[n]; // Pre - allocate Array by n


            for(int i =0;i<mer;i++)mask=mask*4+3;

            System.out.println(mask);


            for(int i =0;i<n;i++){

                long pos = i*sliding;
                char chx = sb.charAt(i*sliding+mer-1);
                if(chx!='N'){
                    if(cmer==-1){
                        String s = sb.substring(i*sliding,i*sliding+mer);
                        cmer = encodeMer(s,mer);
                    }else{
                        int t =-1;
                        switch(chx){
                            case 'A':
                            case 'a':
                                t=0; // 00
                                break;
                            case 'T':
                            case 't': 
                                t=3; // 11
                                break;
                            case 'C':
                            case 'c':
                                t=1; // 01 
                                break;
                            case 'G':
                            case 'g':
                                t=2; // 10 
                                break;
                            default : 
                                t=-1;
                            break;

                        }
                        if(t>=0){

                            cmer *= 4;
                            cmer &= mask;
                            cmer += t;

                        }else{
                            cmer = -1;
                            i+=mer;
                        }  
                    }  
                    if(i%1000000==0)System.out.println("Encode "+chr.getName()+" "+i*sliding);

                    if(cmer>=0){
                        long x = (cmer<<(64- mer*2))|pos;
                        list[count++] = x;
                        
                        /**
                         * Front unique part
                         */
                        if(repeatIndex.containsKey(cmer)){
                            distant++;
                            long unqCode = distant<<(mer*2)|uniqueMer;
                            repeatMarkerFront.put(unqCode,x);     
                        }else{
                            uniqueMer = cmer;
                            distant = 0;
                        }
                        
                        /**
                         * Back unique Part
                         */
                        if(repeatIndex.containsKey(cmer)){
                            listRepeatMer.add(x);
                            distantB++;
                        }else{
                            if(distantB>0){
                                uniqueMer = cmer;
                                for(int j=0;j<distantB;j++){
                                    long code = listRepeatMer.get(j);
                                    long unqCode = (distantB-j)<<(mer*2)|uniqueMer;
                                    repeatMarkerBack.put(unqCode, code);
                                }
                                listRepeatMer = new ArrayList();
                                distantB = 0;
                            }  
                        }
                        
                    }
                }
            }
            
            System.out.println("write .bin file : repeatMarkerFront");
            osF.writeInt(repeatMarkerFront.size());
            for(Map.Entry<Long,Long> entry : repeatMarkerFront.entrySet()){
                long unqCode = entry.getKey();
                long repeatCode = entry.getValue();
                osF.writeLong(unqCode);
                osF.writeLong(repeatCode);                   
            }
            osF.close();
            
            System.out.println("write .bin file : repeatMarkerBack");
            osB.writeInt(repeatMarkerBack.size());
            for(Map.Entry<Long,Long> entry : repeatMarkerBack.entrySet()){
                long unqCode = entry.getKey();
                long repeatCode = entry.getValue();
                osB.writeLong(unqCode);
                osB.writeLong(repeatCode);                   
            }
            osB.close();
            
        }else if(repeatMarkerFrontFile.exists()){
            System.out.println("Begin read repeat marker");
            DataInputStream isF = new DataInputStream(new BufferedInputStream(new FileInputStream(repeatMarkerFrontFile)));
            int size = isF.readInt();
            
            for(int i=0;i<size;i++){
                long unqCode = isF.readLong();
                long repeatCode = isF.readLong();
                repeatMarkerFront.put(unqCode,repeatCode); 
            }
            
            DataInputStream isB = new DataInputStream(new BufferedInputStream(new FileInputStream(repeatMarkerBackFile)));
            size = isB.readInt();
            
            for(int i=0;i<size;i++){
                long unqCode = isB.readLong();
                long repeatCode = isB.readLong();
                repeatMarkerBack.put(unqCode,repeatCode); 
            }     
        }
        
        listRepeatMarker.add(repeatMarkerFront);
        listRepeatMarker.add(repeatMarkerBack);
        
        return listRepeatMarker;

    }

     public static long[] createRepeatMarkerReferenceV2(ChromosomeSequence chr, int mer) throws IOException{
        /**
         * Create repeat Index and repeat Marker
         * Or read from file if file exist
         */
        
        long maskMinus28bit = -268435456; // Do & operation to get mer  (it is minus 28 bit plus 1 bit)
        long mask28bit = 268435455; // Do & operation to get position (28 bit value)
        long mask36bit = 68719476735L;
        int sliding = 1;
        long oldCodeMer = 0;
        long newCodeMer = 0;
        long repeatCodeMer = 0;
        long uniqueMer = 0;
        long distant = 0;
        long distantB = 0;
        long[] merPos;
        Map<Long,Boolean> repeatIndex = new LinkedHashMap();
        Map<Long,Integer> merPosIndex = new LinkedHashMap();                    // Store merPos as key and index as Value. "index" is number that indicate the index of merPos on long[] merPos
        Map<Long,Long> repeatMarkerFront = new LinkedHashMap();
        Map<Long,Long> repeatMarkerBack = new LinkedHashMap();
        ArrayList<Long> listRepeatMer = new ArrayList();                        // use for back unique part
        ArrayList<Map<Long,Long>> listRepeatMarker = new ArrayList();           // list of repeat marker first element is repeatMarker front unique and second is repeatMarker back unique 
        
        /**
         * Create Repeat Index by Chromosome and Map<Long,Long> of key=merPos and value=index of long[] merPos 
         */
           
        File indexFile = new File(chr.getFilePath() + "_repeatIdx.bin");
        System.out.println("Chromosome: "+chr.getName());

        if(indexFile.exists()!=true){
            System.out.println("Begin create repeat index");
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
                                                      
            long startTime = System.currentTimeMillis();

            int count = 0;

            EncodedSequence encoded = encodeSerialChromosomeSequenceV3(chr,mer);            // encoded selected chromosome (just for sure it is encode)
            long chrnumber = chr.getChrNumber();
            merPos = encoded.getMers();

            for(int i=0;i<merPos.length;i++){

                long codeMerPos = merPos[i];
                long codeMer = (codeMerPos>>28)&mask36bit;
                merPosIndex.put(codeMerPos, i);

                if(oldCodeMer == codeMer && repeatCodeMer != codeMer){      // check for repeat codeMer. if it repeat oldCodeMer and codeMer must equal more than one time. So, we can pick it from second time eaual and add to index file
                    repeatCodeMer = codeMer;
                    os.writeLong(codeMer);
                    repeatIndex.put(codeMer, true);
                }
                oldCodeMer = codeMer;
            }
            os.close();
        }else if(indexFile.exists()==true){
            System.out.println("Begin read repeat index");
            boolean eof = false;

            try{
                DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));

                while(!eof){
                    long repeatMer = is.readLong();
                    repeatIndex.put(repeatMer, true);
                }   
            }
            catch(EOFException e){
                eof = true;
            }

        }

        /**
         * Create repeat marker (concatenate marker)
         */

        File repeatMarkerFile = new File(chr.getFilePath() + "_repeatMarker.bin");
       

        if(!repeatMarkerFile.exists()){
            System.out.println("Begin create repeat marker");
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(repeatMarkerFile))); // create object for output data stream
            StringBuffer sb = chr.getSequence();

            int n = (sb.length()-mer)/sliding;       
            long cmer = -1;
            long mask = 0; 
            int count = 0;
            int countMarker = 0;

            long recentUnique = 0;

            long list[] = new long[n]; // Pre - allocate Array by n
            long repeatMarker[] = new long[n];


            for(int i =0;i<mer;i++)mask=mask*4+3;

            System.out.println(mask);

            boolean firstFlag = true;
            long oldMer = 0;
            for(int i =0;i<n;i++){

                long pos = i*sliding;
                char chx = sb.charAt(i*sliding+mer-1);
                if(chx!='N'){
                    if(cmer==-1){
                        String s = sb.substring(i*sliding,i*sliding+mer);
                        cmer = encodeMer(s,mer);
                    }else{
                        int t =-1;
                        switch(chx){
                            case 'A':
                            case 'a':
                                t=0; // 00
                                break;
                            case 'T':
                            case 't': 
                                t=3; // 11
                                break;
                            case 'C':
                            case 'c':
                                t=1; // 01 
                                break;
                            case 'G':
                            case 'g':
                                t=2; // 10 
                                break;
                            default : 
                                t=-1;
                            break;

                        }
                        if(t>=0){

                            cmer *= 4;
                            cmer &= mask;
                            cmer += t;

                        }else{
                            cmer = -1;
                            i+=mer;
                        }  
                    }  
                    if(i%1000000==0)System.out.println("Encode "+chr.getName()+" "+i*sliding);

                    if(cmer>=0){
                        long x = (cmer<<(64- mer*2))|pos;
                        list[count++] = x;
                        
                        if(repeatIndex.containsKey(cmer)){
                            if(firstFlag == true){
                                oldMer = cmer;
                                firstFlag = false;
                            }else if(firstFlag == false){
                                long merIdx = (oldMer<<28)|i;
                                repeatMarker[countMarker++] = merIdx;
                            }
                        }else{
                            if(firstFlag == false){
                                long merIdx = (oldMer<<28)|mask28bit;
                                repeatMarker[countMarker++] = merIdx;
                                firstFlag = true;
                            }else{
                                firstFlag = true;
                            }  
                        }           
                    }
                }
            }
            
            Arrays.sort(repeatMarker);

            System.out.println("write .bin file : repeatMarker");
            os.writeInt(repeatMarker.length);
            for(int i =0;i<repeatMarker.length;i++){
                os.writeLong(repeatMarker[i]);
            }           
            os.close();

            return repeatMarker;
            
        }else if(repeatMarkerFile.exists()){
            System.out.println("Begin read repeat marker");
            DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(repeatMarkerFile)));
            int size = is.readInt();
            long repeatMarker[] = new long[size];
            
            for(int i=0;i<size;i++){
                long merIdx = is.readLong();
                repeatMarker[i] = merIdx;
            }

            return repeatMarker;
        }

        return null;
    }
}
