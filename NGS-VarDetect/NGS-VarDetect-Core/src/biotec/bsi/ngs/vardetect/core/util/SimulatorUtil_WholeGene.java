/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.core.util;

import biotec.bsi.ngs.vardetect.core.ChromosomeSequence;
import biotec.bsi.ngs.vardetect.core.ConcatenateCut;
import biotec.bsi.ngs.vardetect.core.InputSequence;
import biotec.bsi.ngs.vardetect.core.ReferenceSequence;
import biotec.bsi.ngs.vardetect.core.SNPsample;
import biotec.bsi.ngs.vardetect.core.ShortgunSequence;
import biotec.bsi.ngs.vardetect.core.Smallindelsample;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Worawich
 */
public class SimulatorUtil_WholeGene {
    
    public static InputSequence simulateWholeGene(ReferenceSequence ref, int num_read, int ln_read, String numberchrA, String numberchrB){
        if (numberchrA == "24"){
            numberchrA = "Y";
        }else if(numberchrA == "23"){
            numberchrA = "X";
        }
        
        if (numberchrB == "24"){
            numberchrB = "Y";
        }else if(numberchrB == "23"){
            numberchrB = "X";
        }
        
        System.out.println("Begin Simulate");
        String namechrA = "chr"+numberchrA;
        String namechrB = "chr"+numberchrB;
        ChromosomeSequence chrA = null,chrB = null;
        InputSequence is = new InputSequence();
        Random rand = new Random();
        
        Vector<ChromosomeSequence> chrs = ref.getChromosomes();
        System.out.println(chrs.size());
        System.out.println("Chromosome loop");
        for(int chrNum=0;chrNum<chrs.size();chrNum++){
        
            System.out.println("chr number: " + chrNum);
            ChromosomeSequence chr = chrs.elementAt(chrNum);
            System.out.println("Chromosome name: "+chr.getName());

            if (chr.getName().equalsIgnoreCase(namechrA)){
                chrA = chr;
            }
            else if (chr.getName().equalsIgnoreCase(namechrB)){
                chrB = chr;
            }   
        }
        System.out.println("concatenate");
        CharSequence iniTemplate = SequenceUtil.concatenateChromosome(chrA, chrB, ln_read-1, ln_read-1);
        
        String read;
        String readName;
        for(int i = 0;i<num_read;i++){
            int iniread =  0;
            int overLimitCheck = 0;
            while(overLimitCheck == 0){
                iniread = rand.nextInt(ln_read);
                if (iniread<(ln_read-1)){
                    overLimitCheck = 1;
                }
            }
            
            System.out.println("iniread : "+iniread);
            System.out.println("ln_read : "+ln_read);
            read = iniTemplate.subSequence(iniread, iniread+ln_read).toString();
            
            
            System.out.println("Initial position: " + iniread);
            System.out.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
            System.out.println("Number of base from " + namechrB + " : " + (iniread+1));
            System.out.println(read);
            
            ShortgunSequence ss = new ShortgunSequence(read);
            readName = "Read"+i;
            ss.addReadName(readName);
            is.addRead(ss);
            
        }
        
        //is.addName(ref.getChromosomes());
        
        
        //while(chrs.elements()..hasMoreElements()){
            
            //ChromosomeSequence chr = chrs.elements().nextElement();
            
            //System.out.println(chr.getName());
            
        //}
        /*System.out.println(chrs.elements().hasMoreElements());*/
        
        
        
        
        
        
        
        return is;
    }
    
    public static InputSequence simulateWholeGeneRandom(ReferenceSequence ref, int num_read, int ln_read, int num_shortgun){
        /* num_read is number of read per random time */
        InputSequence is = new InputSequence();
        Random rand1 = new Random(); /* For random chromosome */
        for(int i = 0;i<num_read;i++){
            String numberChrA = Integer.toString(rand1.nextInt(25-1)+1);
            String numberChrB = Integer.toString(rand1.nextInt(25-1)+1);

            //InputSequence is = simulateWholeGene(ref,num_shortgun,ln_read,Integer.toString(numberChrA),Integer.toString(numberChrB));
            
       
        
            System.out.println("Begin Simulate");
            
            if (numberChrA.equalsIgnoreCase("24")){
                numberChrA = "Y";
            }else if(numberChrA.equalsIgnoreCase("23")){
                numberChrA = "X";
            }

            if (numberChrB.equalsIgnoreCase("24")){
                numberChrB = "Y";
            }else if(numberChrB.equalsIgnoreCase("23")){
                numberChrB = "X";
            }
            
            String namechrA = "chr"+ numberChrA;
            String namechrB = "chr"+ numberChrB;
            ChromosomeSequence chrA = null,chrB = null;
            
            Random rand2 = new Random(); /* For random positionn on cancatenate sequence */
        
            Vector<ChromosomeSequence> chrs = ref.getChromosomes();
            System.out.println(chrs.size());
            System.out.println("Chromosome loop");
            System.out.println("namechrA => " + namechrA);
            System.out.println("namechrB => " + namechrB);
            for(int chrNum=0;chrNum<chrs.size();chrNum++){

                System.out.println("chr number: " + chrNum);
                ChromosomeSequence chr = chrs.elementAt(chrNum);
                System.out.println("Chromosome name: "+chr.getName());

                if (chr.getName().equalsIgnoreCase(namechrA) && chr.getName().equalsIgnoreCase(namechrB)){
                    chrA = chr;
                    chrB = chr;
                }else{
                    if(chr.getName().equalsIgnoreCase(namechrA)){
                        chrA = chr;
                    }else if(chr.getName().equalsIgnoreCase(namechrB)){
                        chrB = chr;
                    } 
                }
            }
            System.out.println("concatenate");
            CharSequence iniTemplate = SequenceUtil.concatenateChromosome(chrA, chrB, ln_read-1, ln_read-1);
            
            chrA.lazyLoad();
            chrB.lazyLoad();
            
            String read;
            String readName;
            for(int j = 0;j<num_shortgun;j++){
                int iniread =  0;
                int overLimitCheck = 0;
                while(overLimitCheck == 0){
                    iniread = rand2.nextInt(ln_read);
                    if (iniread<(ln_read-1)){
                        overLimitCheck = 1;
                    }
                }

                System.out.println("iniread : "+iniread);
                System.out.println("ln_read : "+ln_read);
                read = iniTemplate.subSequence(iniread, iniread+ln_read).toString();


                System.out.println("Initial position: " + iniread);
                System.out.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                System.out.println("Number of base from " + namechrB + " : " + (iniread+1));
                System.out.println(read);

                ShortgunSequence ss = new ShortgunSequence(read);
                readName = "Read"+i+"SS"+j;
                ss.addReadName(readName);
                is.addRead(ss);
                ss = null;
                chrs =null;
                
                System.gc();
            }
           
        }
        
        //is.addName(ref.getChromosomes());
        
        
        //while(chrs.elements()..hasMoreElements()){
            
            //ChromosomeSequence chr = chrs.elements().nextElement();
            
            //System.out.println(chr.getName());
            
        //}
        /*System.out.println(chrs.elements().hasMoreElements());*/
        
        
        
        
        
               
        return is;
    }
    
    public static InputSequence simulateComplexWholeGeneRandom(ReferenceSequence ref, int num_read, int ln_read, int num_shortgun) throws FileNotFoundException{
        PrintStream ps = new PrintStream(ref.getPath()+"_Simulatedata.txt");        
        ps.println("Simulated Data\n");
        
        /* num_read is number of read per random time */
        InputSequence is = new InputSequence();
        Random rand1 = new Random(); /* For random chromosome */
        for(int i = 0;i<num_read;i++){
            String numberChrA = Integer.toString(rand1.nextInt(25-1)+1);
            String numberChrB = Integer.toString(rand1.nextInt(25-1)+1);

            //InputSequence is = simulateWholeGene(ref,num_shortgun,ln_read,Integer.toString(numberChrA),Integer.toString(numberChrB));
            
       
        
            System.out.println("Begin Simulate");
            
            if (numberChrA.equalsIgnoreCase("24")){
                numberChrA = "Y";
            }else if(numberChrA.equalsIgnoreCase("23")){
                numberChrA = "X";
            }

            if (numberChrB.equalsIgnoreCase("24")){
                numberChrB = "Y";
            }else if(numberChrB.equalsIgnoreCase("23")){
                numberChrB = "X";
            }
            
            String namechrA = "chr"+ numberChrA;
            String namechrB = "chr"+ numberChrB;
            ChromosomeSequence chrA = null,chrB = null;
            
            Random rand2 = new Random(); /* For random positionn on cancatenate sequence */
        
            Vector<ChromosomeSequence> chrs = ref.getChromosomes();
            System.out.println(chrs.size());
            System.out.println("Chromosome loop");
            System.out.println("namechrA => " + namechrA);
            System.out.println("namechrB => " + namechrB);
            for(int chrNum=0;chrNum<chrs.size();chrNum++){

                System.out.println("chr number: " + chrNum);
                ChromosomeSequence chr = chrs.elementAt(chrNum);
                System.out.println("Chromosome name: "+chr.getName());

                if (chr.getName().equalsIgnoreCase(namechrA) && chr.getName().equalsIgnoreCase(namechrB)){
                    chrA = chr;
                    chrB = chr;
                }else{
                    if(chr.getName().equalsIgnoreCase(namechrA)){
                        chrA = chr;
                    }else if(chr.getName().equalsIgnoreCase(namechrB)){
                        chrB = chr;
                    } 
                }
            }
            System.out.println("concatenate");
            ConcatenateCut concatenateSequence = SequenceUtil.concatenateComplexChromosome(chrA, chrB, ln_read-1, ln_read-1);
            CharSequence iniTemplate = concatenateSequence.getSequence();
            
            ps.println("Random cut of " + concatenateSequence.getchrA() + " at position " + String.valueOf(concatenateSequence.getiniA()) + " : " + concatenateSequence.getcutA().toString());
            ps.println("Random cut of " + concatenateSequence.getchrB() + " at position " + String.valueOf(concatenateSequence.getiniB()) + " : " + concatenateSequence.getcutB().toString());
            ps.println("Type is " + concatenateSequence.getType());
            
            chrA.lazyLoad();
            chrB.lazyLoad();
            
            String read;
            String readName;
            /* Loop for shortgun */
            for(int j = 0;j<num_shortgun;j++){
                int iniread =  0;
                int overLimitCheck = 0;
                while(overLimitCheck == 0){
                    iniread = rand2.nextInt(ln_read);
                    if (iniread<(ln_read-1)){
                        overLimitCheck = 1;
                    }
                }

                System.out.println("iniread : "+iniread);
                System.out.println("ln_read : "+ln_read);
                
                read = iniTemplate.subSequence(iniread, iniread+ln_read).toString();


                System.out.println("Initial position: " + iniread);
                System.out.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                System.out.println("Number of base from " + namechrB + " : " + (iniread+1));
                System.out.println(read);

                ShortgunSequence ss = new ShortgunSequence(read);
                readName = "Read"+i+"SS"+j;
                ss.addReadName(readName);
                is.addRead(ss);
                ss = null;
                chrs =null;
                
                ps.println("Read name : "+ readName);
                ps.println("iniread : "+iniread);
                ps.println("ln_read : "+ln_read);
                ps.println("Initial position: " + iniread);
                ps.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                ps.println("Number of base from " + namechrB + " : " + (iniread+1));
                ps.println(read);
                ps.println();
                
                
                
                System.gc();
            }
           
        }
        
        //is.addName(ref.getChromosomes());
        
        
        //while(chrs.elements()..hasMoreElements()){
            
            //ChromosomeSequence chr = chrs.elements().nextElement();
            
            //System.out.println(chr.getName());
            
        //}
        /*System.out.println(chrs.elements().hasMoreElements());*/
        
        
        
        
        
               
        return is;
    }
    
    public static InputSequence simulateComplexWholeGeneRandomMixed(ReferenceSequence ref, int num_read, int ln_read, int num_shortgun , int posDiffL, int indelSizeS ) throws FileNotFoundException{
        PrintStream ps = new PrintStream(ref.getPath()+"_Simulatedata.txt");
        PrintStream ps2 = new PrintStream(ref.getPath()+"_Simulatedata.fa");
        ps.println("Simulated Data\n");
        
        int numRead = 0;
        
        /* num_read is number of read per random time */
        InputSequence is = new InputSequence();
        Random rand1 = new Random(); /* For random chromosome */
        
        int proportion = num_read/4;
        
        /**********************************************************************/
        
        /**
         * Generate fusion samples
         */
        ps.println("Simulated Fusion Reads");
        for(int i = 0;i<proportion;i++){
            String numberChrA = Integer.toString(rand1.nextInt(25-1)+1);
            String numberChrB = Integer.toString(rand1.nextInt(25-1)+1);
                            
            while(numberChrA.equals(numberChrB)){                               // same chromosome prevention loop 
                numberChrA = Integer.toString(rand1.nextInt(25-1)+1);
                numberChrB = Integer.toString(rand1.nextInt(25-1)+1);
            }

            System.out.println("Begin Simulate");
            
            if (numberChrA.equalsIgnoreCase("24")){
                numberChrA = "Y";
            }else if(numberChrA.equalsIgnoreCase("23")){
                numberChrA = "X";
            }

            if (numberChrB.equalsIgnoreCase("24")){
                numberChrB = "Y";
            }else if(numberChrB.equalsIgnoreCase("23")){
                numberChrB = "X";
            }
            
            String namechrA = "chr"+ numberChrA;
            String namechrB = "chr"+ numberChrB;
            ChromosomeSequence chrA = null,chrB = null;
            
            Random rand2 = new Random(); /* For random positionn on cancatenate sequence */
        
            Vector<ChromosomeSequence> chrs = ref.getChromosomes();
            System.out.println(chrs.size());
            System.out.println("Chromosome loop");
            System.out.println("namechrA => " + namechrA);
            System.out.println("namechrB => " + namechrB);
            for(int chrNum=0;chrNum<chrs.size();chrNum++){

                System.out.println("chr number: " + chrNum);
                ChromosomeSequence chr = chrs.elementAt(chrNum);
                System.out.println("Chromosome name: "+chr.getName());

                if (chr.getName().equalsIgnoreCase(namechrA) && chr.getName().equalsIgnoreCase(namechrB)){
                    chrA = chr;
                    chrB = chr;
                }else{
                    if(chr.getName().equalsIgnoreCase(namechrA)){
                        chrA = chr;
                    }else if(chr.getName().equalsIgnoreCase(namechrB)){
                        chrB = chr;
                    } 
                }
            }
            System.out.println("concatenate");
            ConcatenateCut concatenateSequence = SequenceUtil.concatenateComplexChromosome(chrA, chrB, ln_read-1, ln_read-1); 
            CharSequence iniTemplate = concatenateSequence.getSequence();
            
            ps.println("Random cut of " + concatenateSequence.getchrA() + " at position " + String.valueOf(concatenateSequence.getiniA()) + " : " + concatenateSequence.getcutA().toString());
            ps.println("Random cut of " + concatenateSequence.getchrB() + " at position " + String.valueOf(concatenateSequence.getiniB()) + " : " + concatenateSequence.getcutB().toString());
            ps.println("Type is " + concatenateSequence.getType());
            
            chrA.lazyLoad();
            chrB.lazyLoad();
            
            String read;
            String readName;
            /* Loop for shortgun read */
            for(int j = 0;j<num_shortgun;j++){
                int iniread =  0;
                int overLimitCheck = 0;
                while(overLimitCheck == 0){
                    iniread = rand2.nextInt(ln_read);
                    if (iniread<(ln_read-1)){
                        overLimitCheck = 1;
                    }
                }

                System.out.println("iniread : "+iniread);
                System.out.println("ln_read : "+ln_read);
                
                read = iniTemplate.subSequence(iniread, iniread+ln_read).toString();


                System.out.println("Initial position: " + iniread);
                System.out.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                System.out.println("Number of base from " + namechrB + " : " + (iniread+1));
                System.out.println(read);

                ShortgunSequence ss = new ShortgunSequence(read);
                readName = "Read"+numRead+"SS"+j;
                ss.addReadName(readName);
                is.addRead(ss);
                ss = null;
                chrs =null;
                
                ps.println("Read name : "+ readName);
                ps.println("iniread : "+ iniread);
                ps.println("ln_read : "+ ln_read);
                ps.println("Initial position: " + iniread);
                ps.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                ps.println("Number of base from " + namechrB + " : " + (iniread+1));
                ps.println(read);
                ps.println();
                                
                ps2.println(">"+readName);
                ps2.println(read);
                
                System.gc();
            }
            numRead++;
        }
        
        /**********************************************************************/
        
        /**
         * Generate SNP contained samples
         */
        
        ps.println("Simulated SNP Reads");
        for(int i = 0;i<proportion;i++){
            String numberChrA = Integer.toString(rand1.nextInt(25-1)+1);

            System.out.println("Begin Simulate SNP contained samples");
            
            if (numberChrA.equalsIgnoreCase("24")){
                numberChrA = "Y";
            }else if(numberChrA.equalsIgnoreCase("23")){
                numberChrA = "X";
            }

            String namechrA = "chr"+ numberChrA;
            
            ChromosomeSequence chrA = null;
            
            Random rand2 = new Random(); /* For random positionn on cancatenate sequence */
        
            Vector<ChromosomeSequence> chrs = ref.getChromosomes();
            System.out.println(chrs.size());
            System.out.println("Chromosome loop");
            System.out.println("namechrA => " + namechrA);

            for(int chrNum=0;chrNum<chrs.size();chrNum++){

                System.out.println("chr number: " + chrNum);
                ChromosomeSequence chr = chrs.elementAt(chrNum);
                System.out.println("Chromosome name: "+chr.getName());

                if (chr.getName().equalsIgnoreCase(namechrA)){
                    chrA = chr;
                    
                }
            }
            
            SNPsample snpSample = SequenceUtil.createComplexSNPSample(chrA, (ln_read-1)*2); // SNP simulate sample

            CharSequence iniTemplate = snpSample.getSequence();
            
            ps.println("Random cut from " + snpSample.getchrA() + " at position " + String.valueOf(snpSample.getiniA()));
            ps.println("Raw Read: " + snpSample.getcutA());
            ps.println("Add SNP at : "+snpSample.getPosBaseChange() + " Base change is : " + snpSample.getBaseChange());
            ps.println("SNP contained read: " + snpSample.getSequence());
            ps.println("Type is " + snpSample.getType());
            
            
            chrA.lazyLoad();
            
            String read;
            String readName;
            /* Loop for shortgun read */
            for(int j = 0;j<num_shortgun;j++){
                int iniread =  0;
                int overLimitCheck = 0;
                while(overLimitCheck == 0){
                    iniread = rand2.nextInt(ln_read);
                    if (iniread<(ln_read-1)){                                   // prevent over limit
                        overLimitCheck = 1;
                    }
                }

                System.out.println("iniread : "+iniread);
                System.out.println("ln_read : "+ln_read);
                
                read = iniTemplate.subSequence(iniread, iniread+ln_read).toString();


                System.out.println("Initial position: " + iniread);
                System.out.println("Raw Read: " + iniTemplate);
                System.out.println("Complete SNP Read: " + read);
                

                ShortgunSequence ss = new ShortgunSequence(read);
                readName = "Read"+numRead+"SS"+j;
                ss.addReadName(readName);
                is.addRead(ss);
                ss = null;
                chrs =null;
                
                ps.println("Read name : "+ readName);
                ps.println("iniread : "+ iniread);
                ps.println("ln_read : "+ ln_read);
                ps.println("Initial position: " + iniread);
                ps.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                ps.println(read);
                ps.println();
                
                ps2.println(">"+readName);
                ps2.println(read);
                
                System.gc();
            }
            numRead++;
        }

        /**********************************************************************/
        
        /**
         * Generate large indel samples
         */
        ps.println("Simulated large indel Reads");
        for(int i = 0;i<proportion;i++){
            
            String numberChrA = Integer.toString(rand1.nextInt(25-1)+1);
            String numberChrB = numberChrA;                                     // We fixed that indel must have same chromosome
           
            System.out.println("Begin Simulate large indel samples");
            
            if (numberChrA.equalsIgnoreCase("24")){
                numberChrA = "Y";
            }else if(numberChrA.equalsIgnoreCase("23")){
                numberChrA = "X";
            }

            if (numberChrB.equalsIgnoreCase("24")){
                numberChrB = "Y";
            }else if(numberChrB.equalsIgnoreCase("23")){
                numberChrB = "X";
            }
            
            String namechrA = "chr"+ numberChrA;
            String namechrB = "chr"+ numberChrB;
            ChromosomeSequence chrA = null,chrB = null;
            
            Random rand2 = new Random(); /* For random positionn on cancatenate sequence */
        
            Vector<ChromosomeSequence> chrs = ref.getChromosomes();
            System.out.println(chrs.size());
            System.out.println("Chromosome loop");
            System.out.println("namechrA => " + namechrA);
            System.out.println("namechrB => " + namechrB);
            for(int chrNum=0;chrNum<chrs.size();chrNum++){

                System.out.println("chr number: " + chrNum);
                ChromosomeSequence chr = chrs.elementAt(chrNum);
                System.out.println("Chromosome name: "+chr.getName());

                if (chr.getName().equalsIgnoreCase(namechrA) && chr.getName().equalsIgnoreCase(namechrB)){
                    chrA = chr;
                    chrB = chr;
                }else{
                    if(chr.getName().equalsIgnoreCase(namechrA)){
                        chrA = chr;
                    }else if(chr.getName().equalsIgnoreCase(namechrB)){
                        chrB = chr;
                    } 
                }
            }
            
            ConcatenateCut concatenateSequence = SequenceUtil.createComplexLargeIndel(chrA, chrB, ln_read-1, ln_read-1, posDiffL);        // most is the same as fusion but same chromosome only
            CharSequence iniTemplate = concatenateSequence.getSequence();
            
            ps.println("Random cut of " + concatenateSequence.getchrA() + " at position " + String.valueOf(concatenateSequence.getiniA()) + " : " + concatenateSequence.getcutA().toString());
            ps.println("Random cut of " + concatenateSequence.getchrB() + " at position " + String.valueOf(concatenateSequence.getiniB()) + " : " + concatenateSequence.getcutB().toString());
            ps.println("Type is " + concatenateSequence.getType());
            
            chrA.lazyLoad();
            chrB.lazyLoad();
            
            String read;
            String readName;
            /* Loop for shortgun read */
            for(int j = 0;j<num_shortgun;j++){
                int iniread =  0;
                int overLimitCheck = 0;
                while(overLimitCheck == 0){
                    iniread = rand2.nextInt(ln_read);
                    if (iniread<(ln_read-1)){
                        overLimitCheck = 1;
                    }
                }

                System.out.println("iniread : "+iniread);
                System.out.println("ln_read : "+ln_read);
                
                read = iniTemplate.subSequence(iniread, iniread+ln_read).toString();


                System.out.println("Initial position: " + iniread);
                System.out.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                System.out.println("Number of base from " + namechrB + " : " + (iniread+1));
                System.out.println("Raw read: " + iniTemplate);
                System.out.println("Complete large indel read: " + read);

                ShortgunSequence ss = new ShortgunSequence(read);
                readName = "Read"+numRead+"SS"+j;
                ss.addReadName(readName);
                is.addRead(ss);
                ss = null;
                chrs =null;
                
                ps.println("Read name : "+ readName);
                ps.println("iniread : "+ iniread);
                ps.println("ln_read : "+ ln_read);
                ps.println("Initial position: " + iniread);
                ps.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                ps.println("Number of base from " + namechrB + " : " + (iniread+1));
                ps.println("Raw read: " + iniTemplate);
                ps.println("Complete large indel read: " + read);                
                ps.println();
                
                ps2.println(">"+readName);
                ps2.println(read);
                
                System.gc();
            }
            numRead++;
        }
        
        /**********************************************************************/
        
        /**
         * Generate small indel samples
         */
        ps.println("Simulated small indel reads");
        int proportionDel = proportion/2;
        int proportionIns = proportion-proportionDel;
        
        /**
         * Generate small insertion
         */
        for(int i = 0;i<proportionDel;i++){
            String numberChrA = Integer.toString(rand1.nextInt(25-1)+1);
            String numberChrB = numberChrA;

            //InputSequence is = simulateWholeGene(ref,num_shortgun,ln_read,Integer.toString(numberChrA),Integer.toString(numberChrB));

            System.out.println("Begin Simulate small Insertion samples");
            
            if (numberChrA.equalsIgnoreCase("24")){
                numberChrA = "Y";
            }else if(numberChrA.equalsIgnoreCase("23")){
                numberChrA = "X";
            }

            if (numberChrB.equalsIgnoreCase("24")){
                numberChrB = "Y";
            }else if(numberChrB.equalsIgnoreCase("23")){
                numberChrB = "X";
            }
            
            String namechrA = "chr"+ numberChrA;
            String namechrB = "chr"+ numberChrB;
            ChromosomeSequence chrA = null,chrB = null;
            
            Random rand2 = new Random(); /* For random positionn on cancatenate sequence */
        
            Vector<ChromosomeSequence> chrs = ref.getChromosomes();
            System.out.println(chrs.size());
            System.out.println("Chromosome loop");
            System.out.println("namechrA => " + namechrA);
            System.out.println("namechrB => " + namechrB);
            for(int chrNum=0;chrNum<chrs.size();chrNum++){

                System.out.println("chr number: " + chrNum);
                ChromosomeSequence chr = chrs.elementAt(chrNum);
                System.out.println("Chromosome name: "+chr.getName());

                if (chr.getName().equalsIgnoreCase(namechrA) && chr.getName().equalsIgnoreCase(namechrB)){
                    chrA = chr;
                    chrB = chr;
                }else{
                    if(chr.getName().equalsIgnoreCase(namechrA)){
                        chrA = chr;
                    }else if(chr.getName().equalsIgnoreCase(namechrB)){
                        chrB = chr;
                    } 
                }
            }
            
            Smallindelsample smallIndelSample = SequenceUtil.createComplexSmallIndel(chrA, chrB, ln_read, ln_read, posDiffL, 'I', indelSizeS);
            CharSequence iniTemplate = smallIndelSample.getSequence();
            
            ps.println("Random cut of " + smallIndelSample.getchrA() + " at position " + String.valueOf(smallIndelSample.getiniA()) + " : " + smallIndelSample.getcutA().toString());
            ps.println("Random cut of " + smallIndelSample.getchrB() + " at position " + String.valueOf(smallIndelSample.getiniB()) + " : " + smallIndelSample.getIndelSequence().toString());
            ps.println("Indel type is "+ smallIndelSample.getIndelType()+" and Type is " + smallIndelSample.getType()+" Indel size is "+smallIndelSample.getIndelSize());
            
            chrA.lazyLoad();
            chrB.lazyLoad();
            
            String read;
            String readName;
            /* Loop for shortgun read */
            for(int j = 0;j<num_shortgun;j++){
                int iniread =  0;
                int overLimitCheck = 0;
                while(overLimitCheck == 0){
                    iniread = rand2.nextInt(ln_read);
                    if (iniread<(ln_read-1)){
                        overLimitCheck = 1;
                    }
                }

                System.out.println("iniread : "+iniread);
                System.out.println("ln_read : "+ln_read);
                
                read = iniTemplate.subSequence(iniread, iniread+ln_read).toString();


                System.out.println("Initial position: " + iniread);
                System.out.println("Raw read: "+iniTemplate);
                System.out.println("Complete Read: "+read);

                ShortgunSequence ss = new ShortgunSequence(read);
                readName = "Read"+numRead+"SS"+j;
                ss.addReadName(readName);
                is.addRead(ss);
                ss = null;
                chrs =null;
                
                ps.println("Read name : "+ readName);
                ps.println("iniread : "+ iniread);
                ps.println("ln_read : "+ ln_read);
                ps.println("Initial position: " + iniread);
                ps.println("Number of base from front part : " + ((ln_read-iniread)-1));
                ps.println("Number of base from back part : " + (iniread+1));
                ps.println(read);
                ps.println();
                
                ps2.println(">"+readName);
                ps2.println(read);
                
                System.gc();
            }
            numRead++;
        }
        
        /**
         * Generate small deletion
         */
        for(int i = 0;i<proportionIns;i++){
            String numberChrA = Integer.toString(rand1.nextInt(25-1)+1);
            String numberChrB = numberChrA;

            //InputSequence is = simulateWholeGene(ref,num_shortgun,ln_read,Integer.toString(numberChrA),Integer.toString(numberChrB));

            System.out.println("Begin Simulate small deletion samples");
            
            if (numberChrA.equalsIgnoreCase("24")){
                numberChrA = "Y";
            }else if(numberChrA.equalsIgnoreCase("23")){
                numberChrA = "X";
            }

            if (numberChrB.equalsIgnoreCase("24")){
                numberChrB = "Y";
            }else if(numberChrB.equalsIgnoreCase("23")){
                numberChrB = "X";
            }
            
            String namechrA = "chr"+ numberChrA;
            String namechrB = "chr"+ numberChrB;
            ChromosomeSequence chrA = null,chrB = null;
            
            Random rand2 = new Random(); /* For random positionn on cancatenate sequence */
        
            Vector<ChromosomeSequence> chrs = ref.getChromosomes();
            System.out.println(chrs.size());
            System.out.println("Chromosome loop");
            System.out.println("namechrA => " + namechrA);
            System.out.println("namechrB => " + namechrB);
            for(int chrNum=0;chrNum<chrs.size();chrNum++){

                System.out.println("chr number: " + chrNum);
                ChromosomeSequence chr = chrs.elementAt(chrNum);
                System.out.println("Chromosome name: "+chr.getName());

                if (chr.getName().equalsIgnoreCase(namechrA) && chr.getName().equalsIgnoreCase(namechrB)){
                    chrA = chr;
                    chrB = chr;
                }else{
                    if(chr.getName().equalsIgnoreCase(namechrA)){
                        chrA = chr;
                    }else if(chr.getName().equalsIgnoreCase(namechrB)){
                        chrB = chr;
                    } 
                }
            }
            
            Smallindelsample smallIndelSample = SequenceUtil.createComplexSmallIndel(chrA, chrB, ln_read, ln_read, posDiffL, 'D', indelSizeS);
            CharSequence iniTemplate = smallIndelSample.getSequence();
            
            ps.println("Random cut of " + smallIndelSample.getchrA() + " at position " + String.valueOf(smallIndelSample.getiniA()) + " : " + smallIndelSample.getcutA().toString());
            ps.println("Random cut of " + smallIndelSample.getchrB() + " at position " + String.valueOf(smallIndelSample.getiniB()) + " : " + smallIndelSample.getIndelSequence().toString());
            ps.println("Indel type is "+ smallIndelSample.getIndelType()+" and Type is " + smallIndelSample.getType()+" Indel size is "+smallIndelSample.getIndelSize());

            chrA.lazyLoad();
            chrB.lazyLoad();
            
            String read;
            String readName;
            /* Loop for shortgun read */
            for(int j = 0;j<num_shortgun;j++){
                int iniread =  0;
                int overLimitCheck = 0;
                while(overLimitCheck == 0){
                    iniread = rand2.nextInt(ln_read);
                    if (iniread<(ln_read-1)){
                        overLimitCheck = 1;
                    }
                }

                System.out.println("iniread : "+iniread);
                System.out.println("ln_read : "+ln_read);
                
                read = iniTemplate.subSequence(iniread, iniread+ln_read).toString();


                System.out.println("Initial position: " + iniread);
                System.out.println("Raw read: "+iniTemplate);
                System.out.println("Complete Read: "+read);

                ShortgunSequence ss = new ShortgunSequence(read);
                readName = "Read"+numRead+"SS"+j;
                ss.addReadName(readName);
                is.addRead(ss);
                ss = null;
                chrs =null;
                
                ps.println("Read name : "+ readName);
                ps.println("iniread : "+ iniread);
                ps.println("ln_read : "+ ln_read);
                ps.println("Initial position: " + iniread);
                ps.println("Number of base from " + namechrA + " : " + ((ln_read-iniread)-1));
                ps.println("Number of base from " + namechrB + " : " + (iniread+1));
                ps.println(read);
                ps.println();
                
                ps2.println(">"+readName);
                ps2.println(read);
                
                System.gc();
            }
            numRead++;
        }
      
        return is;
    }
  
}
