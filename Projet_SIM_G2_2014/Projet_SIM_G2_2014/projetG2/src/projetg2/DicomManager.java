/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projetg2;
/*
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
*/
import java.io.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import java.util.Calendar;
//import java.util.Date;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.util.CloseUtils;
import org.dcm4che2.util.UIDUtils;
import org.xml.sax.SAXException;

//import org.dcm4che2.tool.dcmmwl.DcmMWL;

import org.dcm4che2.util.CloseUtils;
import org.dcm4che2.util.UIDUtils;

import be.belgium.eid.eidlib.BeID;
import be.belgium.eid.exceptions.EIDException;
import java.util.*;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.UID;

/**
 *
 * @author david
 */
public class DicomManager {
    
    private DcmMWL dcm;
    private List<DicomObject> dcmList;
    //private BeID eID_patient;
    
    


    //Database DB;
    private DicomObject dcmobj;
    private String outputFile = "./OutPutDicomFiles/DicomObj";
    //private String outputList = "./OutPutDicomFiles/MWL";
    
    private String patientName;
    private String doctor = "papa";
    private Date patientBDay;
    private int pregn;
    private String patientID;

    //private int age = 22;
    //private String age;
    //private String age3;
    //Date age2;
    
    private String modality = "CT"; 
    private String bodyPart = "head";
    private String sex ="M";

    
    public DicomManager(){
        initComponent();
    }
    
    public void creeMWL(){
        //Initialise la create MWL with TEST information        
        String fname = outputFile+"/"+patientID+modality+fileNameDate()+".dcm";
        System.out.printf(fname);
        enregistreDicom(fname,creeDicomMWL());
    }
    
    public void createCustomMWL(BeID eID,String mod,String Part){
        //Initialise la Create MWL withe eID information
        DicomObject dcmobj_custom = createCustomDicomMWL(eID,mod,Part);
        String fname = outputFile+"/"+patientID+modality+fileNameDate()+".dcm";
        System.out.printf(fname);
        
        enregistreDicom(fname,dcmobj_custom);
    }
           
    public void initComponent(){
        //Initialise les attributs
        dcm = new DcmMWL("DCMMWL");
        dcmList = new ArrayList<DicomObject>();
        //dcmobj = new BasicDicomObject();
        connexionTest();
    }
    
     private DicomObject creeDicomMWL(){
         //Create Dicom MWL with Standard config
        dcmobj = new BasicDicomObject();
        dcmobj.putString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        dcmobj.putString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
        stdDicomConfig();
        configureDicomFile();
        return dcmobj;
     }
     
      public DicomObject createCustomDicomMWL(BeID eID,String mod,String part){  
          //Create Dicom MWL with custom (eID) config
        dcmobj = new BasicDicomObject();
        dcmobj.putString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        dcmobj.putString(Tag.TransferSyntaxUID, VR.UI, UIDUtils.createUID());
        getInfoPatientEid(eID);
        modality = mod;
        bodyPart = part;
        doctor = "Buggs Bunny";
        //age = "22";
        configureDicomFile();
        return dcmobj;
     }
     
     private void stdDicomConfig(){
         //config initiale std attributs
        patientName = "Un Patient d'infoh400";
        doctor = "Doc. Bugs Bunny";
        
        patientID = "666";
        
        //age = "18";
        patientBDay = new Date(2014,05,20);
    
     }
     
     public String fileNameDate(){
         //random name for patient file
         String fname;
         int Max = 10000000;
         int Min = 0;
         int random = Min + (int)(Math.random() * ((Max - Min) + 1));
         System.out.print(random);
         return Integer.toString(random);
     }

     private void enregistreDicom(String name, DicomObject dcmOBJ){
        //crée un fichier ou sera enregistre la MWL
                 
        File ofile = new File(name);
        ofile.setWritable(true);
        System.out.print(name+"\n");

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(ofile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DicomOutputStream dos = new DicomOutputStream(bos);
            
            //fixe les parametres pour l'ecriture dcm file :
            
            String tsuid = dcmOBJ.getString(Tag.TransferSyntaxUID);
            
            TransferSyntax ts = TransferSyntax.ExplicitVRLittleEndian; 
            
            dcmOBJ.initFileMetaInformation(ts.toString());
            dos.setIncludeGroupLength(false);
            dos.setExplicitItemLength(false);
            dos.setExplicitItemLengthIfZero(false);
            dos.setExplicitSequenceLength(false);
            dos.setExplicitSequenceLengthIfZero(false);
            
            //dos.setTransferSyntax(dcmOBJ.getS);
            
            dos.writeDicomFile(dcmOBJ);
            
            dos.close();
            
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(DicomManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex){
            java.util.logging.Logger.getLogger(DicomManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }       
     }
     
     public void connexionTest(){
         //mise en place des parametres de la connexion :
         dcm.setLocalHost("localhost");
         dcm.setRemoteHost("localhost");
         dcm.setRemotePort(1112);
         dcm.setPackPDV(false);
         dcm.setTcpNoDelay(false);
         dcm.setTlsNeedClientAuth(false);
         dcm.setTlsNeedClientAuth(false);
         dcm.setTransferSyntax(DcmMWL.getLE_TS());
         dcm.setCalledAET("DCMMWL");
         dcm.setCalling("DCMMWL");
         dcm.setKeyStoreURL("D:\\Users\\INFO-H-400\\Documents\\NetBeansProjects\\Projet_SIM_G2_2014\\projetG2\\OutPutDicomFiles\\DicomObj");
     }
     
     public List<DicomObject> queryMWL(String MOD,String Part){
         //Allow Query of MWL
        try {
            //requete :

            int[] tagPathMOD = new int[] {Tag.Modality};
            int[] tagPathID = new int[] {Tag.PatientID};
            int[] tagPathBodyPart = new int []{Tag.BodyPartExamined};
            
            dcm.open();
            dcm.addMatchingKey(tagPathMOD, MOD);
            dcm.addMatchingKey(tagPathBodyPart, Part);
            dcmList = dcm.query();
            dcm.close();            
            /*
            //Enregistre Les fichiers dicom de la MWL dans un dossier a part :
            for (int i = 0; i < dcmList.size(); i++) {
                DicomObject dcmobjTMP;
                dcmobjTMP = dcmList.get(i);
                enregistreDicom(outputList+"/"+dcmobjTMP.getString(tagPathID)+MOD+".dcm", dcmobjTMP);             
            }*/
            //enregistreDicom(outputList+"modalityWorklist");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DicomManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            System.out.printf("Dans enregistre Dicom de Dicom Manager. Excep 1");
        } catch (ConfigurationException ex) {
            java.util.logging.Logger.getLogger(DicomManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            System.out.printf("Dans enregistre Dicom de Dicom Manager. Excep 1");
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(DicomManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            System.out.printf("Dans enregistre Dicom de Dicom Manager. Excep 1");
        }
        
        return dcmList;
     }
     
     public void configureDicomFile(){
         
         dcmobj.putString(Tag.PatientName, VR.PN, patientName);//VR.PN = person Name
         dcmobj.putString(Tag.PatientID, VR.LO, patientID);//VR.LO = long string
         dcmobj.putDate(Tag.PatientBirthDate, VR.DA,patientBDay);
         dcmobj.putInt(Tag.PregnancyStatus, VR.US, pregn); 
         dcmobj.putString(Tag.RequestingPhysician, VR.PN, doctor);
         dcmobj.putString(Tag.Modality,VR.CS,modality);
         dcmobj.putString(Tag.PatientSex, VR.CS, sex);
         dcmobj.putString(Tag.BodyPartExamined,VR.CS,bodyPart);
         System.out.println("Test"+""+bodyPart);
         
         //System.out.print(age+"\n");
         //dcmobj.putString(Tag.PatientAge, VR.AS,age);
         
         //int[] tagPathAge = new int[] {Tag.PatientAge};

         //System.out.print(dcmobj.get(tagPathAge));
         //System.out.print(Integer.toString(age)+"\n");
   
         
     }
     
    
     
     public void getInfoPatientEid(BeID eID){
        try {
            //int = eID.getIDData().getBirthDate().get;
            
            //this.age = (2014 - 1900 - eID.getIDData().getBirthDate().getYear()).toString();
            //System.out.print(age+"\n");
            
            this.patientName =  eID.getIDData().get1stFirstname().toString()+" "+ eID.getIDData().getName().toString();
            this.sex =  Character.toString(eID.getIDData().getSex());
            this.patientBDay = eID.getIDData().getBirthDate();
            this.patientID = eID.getIDData().getNationalNumber();            
        } catch (EIDException ex) {
            java.util.logging.Logger.getLogger(DicomManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        doctor = "Bugs Bunny";
     }
}
