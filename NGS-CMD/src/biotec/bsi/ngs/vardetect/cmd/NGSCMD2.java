/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biotec.bsi.ngs.vardetect.cmd;

import biotec.bsi.ngs.vardetect.core.ExonIntron;
import biotec.bsi.ngs.vardetect.core.InputSequence;
import biotec.bsi.ngs.vardetect.core.ReferenceExonIntron;
import biotec.bsi.ngs.vardetect.core.ReferenceSequence;
import biotec.bsi.ngs.vardetect.core.util.SequenceUtil;
import biotec.bsi.ngs.vardetect.core.util.SimulatorUtil_aon;
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
 * @author soup
 */
public class NGSCMD2 {
    
    public static void main(String args[]) throws FileNotFoundException, IOException{
        ReferenceExonIntron output = new ReferenceExonIntron();
        ReferenceExonIntron ee = SequenceUtil.readExonIntron(args[3]);
        output = SequenceUtil.randomExonIntron(ee);
    }
}
