package Servlets;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.display.SourceImage;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author De Almeida Luis
 * 
/**
 * The Image servlet for serving from absolute path.
 * @author BalusC
 * @link http://balusc.blogspot.com/2007/04/imageservlet.html
 */
 
public class ImageServlet extends HttpServlet 
{
    // Constants ----------------------------------------------------------------------------------
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
    // Properties ---------------------------------------------------------------------------------
    private String imagePath;
    
    public void init() throws ServletException 
    {
        // Define base path 
        this.imagePath = "d:/Users/INFO-H-400/Desktop/DICOM/";
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {        
        String requestedImage = request.getParameter("image_list");
        String im = imagePath + requestedImage;

        BufferedImage buf=null;
        
        File DicomFile = new File(im);
        
        // Check if file actually exists in filesystem.
        if (!DicomFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // Erreur 404.
            return;
        }
        
        DicomInputStream distr = new DicomInputStream(DicomFile);
        AttributeList attList = new AttributeList();
        SourceImage image = null;
        
        try {
            attList.read(distr);
            image = new SourceImage(attList);
        } catch (IOException iOException) 
        { System.out.println("Echec création bufferimage.");
        } 
        catch (DicomException dicomException) {
            System.out.println("Echec création bufferimage.");
        }
        
        buf = image.getBufferedImage();        
        System.out.println("Image buffered obtenue");
        
        //Conversion to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( buf, "png", baos );
        baos.flush();
        byte[] bytebuf = baos.toByteArray();
        baos.close();
        
        // Init servlet response.
        
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setContentType("image/png");//MIME content type
        /*response.setHeader("Content-Length", String.valueOf(image.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + image.getName() + "\"");*/

        // Prepare streams.
        //BufferedInputStream input = null;
        BufferedOutputStream output = null;
        
        try {
            // Open streams.
            //input = new BufferedInputStream(new FileInputStream(image), DEFAULT_BUFFER_SIZE);
            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
            // Write file contents to response.
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            output.write(bytebuf);
            /*int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }*/
        } finally 
        {
            // Gently close streams.
            if ( output != null) 
            {
            
                try 
                {
                    output.close();
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
    
        }

    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}