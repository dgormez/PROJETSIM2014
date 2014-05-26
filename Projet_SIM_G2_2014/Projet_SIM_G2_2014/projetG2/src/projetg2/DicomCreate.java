/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projetg2;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.pixelmed.dicom.AgeStringAttribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DateAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.DicomOutputStream;
import com.pixelmed.dicom.ImageToDicom;
import static com.pixelmed.dicom.ImageToDicom.generateDICOMPixelModuleFromConsumerImageFile;
import com.pixelmed.dicom.PersonNameAttribute;
import static com.pixelmed.dicom.TagFromName.PatientAge;
import static com.pixelmed.dicom.TagFromName.PatientBirthDate;
import static com.pixelmed.dicom.TagFromName.PatientSex;
import static com.pixelmed.dicom.TagFromName.PregnancyStatus;
import static com.pixelmed.dicom.TagFromName.RequestingPhysician;
import static com.pixelmed.dicom.TagFromName.BodyPartExamined;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;



/**
 *
 * @author Maxime
 */
public class DicomCreate {        
      
        //variables pour récupérer les données du fichier qu'on traite
        //variables qui vont se retrouver dans la fichier DICOM
        String patientName;
        String patientID;
        String patientBirthDate;
        String patientSex;
        //String patientAge;
        String pregnancyStatus;
        String requestingPhysician;
        String modality;
        String bodyPart;
        String tsfSynt;
        String specCharSet;
        
        
        String studyID;
        String seriesNumber;
        String instanceNumber;
        
        private String outputFile = "./OutPutDicomFiles/DCM_FILE/";

        
        
        DicomObject dcmobj;
        File outputfile;
        //creation des tags pour ajout dans la DICOMFILE
        //AgeStringAttribute age;
        CodeStringAttribute sex;
        CodeStringAttribute pregnancy;
        CodeStringAttribute partExamined;
        DateAttribute date;
        PersonNameAttribute doctor;
        
        //liste pour stocker le dossier
       //private String outputList = "./OutPutDicomFiles/DCM_FILE";
        
        
        public DicomCreate(DicomObject dcmobj,int index){
            this.dcmobj = dcmobj;
            //recuperation des données de l'objet contenu dans la MWL
            //attributs dont les valeurs sont fournies par la MWL
            int[] tagPathID = new int[] {Tag.PatientID};
            patientID = dcmobj.getString(tagPathID);
            System.out.println(patientID);
            
            int[] tagPathName = new int[] {Tag.PatientName};
            patientName = dcmobj.getString(tagPathName);
            System.out.println(patientName);
   
            
            int[] tagPathBirthDate = new int[] {Tag.PatientBirthDate};
            patientBirthDate = dcmobj.getString(tagPathBirthDate);
            System.out.println(patientBirthDate);
            
            int[] tagPathSex = new int[] {Tag.PatientSex};
            patientSex = dcmobj.getString(tagPathSex);
            System.out.println(patientSex);
            
            //int[] tagPathAge = new int[] {Tag.PatientAge};
            //patientAge = dcmobj.getString(tagPathAge);
            //System.out.println(patientAge);
            
            int[] tagPathPreg = new int[] {Tag.PregnancyStatus};
            pregnancyStatus = dcmobj.getString(tagPathPreg);
            System.out.println(pregnancyStatus);
            
            int[] tagPathDr = new int[] {Tag.RequestingPhysician};
            requestingPhysician = dcmobj.getString(tagPathDr);
            System.out.println(requestingPhysician);
            
            int[] tagPathMod = new int[] {Tag.Modality};
            modality = dcmobj.getString(tagPathMod);
            System.out.println(modality);
            
            int[] tagPathBodyPart = new int[] {Tag.BodyPartExamined};
            bodyPart = dcmobj.getString(tagPathBodyPart);
            System.out.println(bodyPart);
            
            int[] tagPathSyntx = new int[] {Tag.TransferSyntaxUID};
            tsfSynt = dcmobj.getString(tagPathSyntx);
            System.out.println(tsfSynt);
            
            int[] tagPathSpecChar = new int[] {Tag.SpecificCharacterSet};
            specCharSet = dcmobj.getString(tagPathSpecChar);
            System.out.println(specCharSet);
            
           // attributs dont les valeurs ne sont pas fournies par la MWL
            studyID = "0001";
            
            seriesNumber = "0001";
   
            instanceNumber = Integer.toString(index);
           
            
           
            
            //creation des attributs qui vont aller dans le DICOMFILE
            //age = new AgeStringAttribute(PatientAge);
            sex = new CodeStringAttribute(PatientSex);
            date = new DateAttribute(PatientBirthDate);
            doctor = new PersonNameAttribute(RequestingPhysician); 
            pregnancy = new CodeStringAttribute(PregnancyStatus);
            partExamined = new CodeStringAttribute(BodyPartExamined);
            //on met les valeurs patients dans ces attributs
            try {
                sex.setValue(patientSex);
               // age.setValue(patientAge);
                date.setValue(patientBirthDate);
                doctor.setValue(requestingPhysician);
                pregnancy.setValue(pregnancyStatus);
                partExamined.setValue(bodyPart);
            
            } catch (DicomException ex) {
                Logger.getLogger(DicomCreate.class.getName()).log(Level.SEVERE, null, ex);
            }
           
   
        }
        
        //fonction pour acquerir l'image
        public void takePicture() throws Exception{
        
        //ouverture du medium pour prendre les photos
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        try {
            //on lance la webcam
            grabber.start();
            
            //on prend la photo
            IplImage img = grabber.grab();
            // erreur si la webcam n'arrive pas a prendre la photo
            if (img==null)return;
            //on met l'image prise dans par la webcam dans un buffer
            BufferedImage buffer = img.getBufferedImage();
            //on cree un fichier ou stocker cette image
            outputfile = new File(outputFile+modality+patientName+instanceNumber+".png");
            //on met l'image dans ce fichier
            ImageIO.write(buffer, "png", outputfile);
            
            createDicomFile();
            
        } catch (IOException ex) {
            Logger.getLogger(DicomCreate.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        }
        
        public void createDicomFile(){
        try {
            //on cree un fichier dicom apd de l'image prise et des informations du patient
            ImageToDicom DicomFile = new ImageToDicom(outputfile.getAbsolutePath(),outputFile+modality+patientName+instanceNumber,patientName,patientID,studyID,seriesNumber,instanceNumber,modality,null);
            AttributeList list = new AttributeList();
            list.read(outputFile+modality+patientName+instanceNumber);
            //ajout dans le dicom file
            //list.put(PatientAge,age);
            list.put(PatientSex,sex);
            list.put(PatientBirthDate,date);
            list.put(RequestingPhysician,doctor);
            list.put(PregnancyStatus,pregnancy);
            list.put(BodyPartExamined,partExamined);
            list.write(outputFile+modality+patientName+instanceNumber);
        } catch (IOException ex) {
            Logger.getLogger(DicomCreate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DicomException ex) {
            Logger.getLogger(DicomCreate.class.getName()).log(Level.SEVERE, null, ex);
        }
          
        }       
}