

package StoragePacs;

/**
 *
 * @author Yohan
 */
import java.util.Date;
import java.util.ArrayList;
import com.pixelmed.dicom.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.filecache.FileCache;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.VerificationService;
import org.dcm4che2.util.CloseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DcmRcv {

    private static final int NO_SUCH_OBJECT_INSTANCE = 0x0112;

    static Logger LOG = LoggerFactory.getLogger(DcmRcv.class);

    private static final int KB = 1024;

    private static final String USAGE = "dcmrcv [Options] [<aet>[@<ip>]:]<port>";

    private static final String DESCRIPTION = "DICOM Server listening on specified <port> for incoming association "
            + "requests. If no local IP address of the network interface is specified "
            + "connections on any/all local addresses are accepted. If <aet> is "
            + "specified, only requests with matching called AE title will be "
            + "accepted. If <aet> and a storage directory is specified by option "
            + "-dest <dir>, also Storage Commitment requests will be accepted and "
            + "processed.\n Options:";

    private static final String EXAMPLE = "\nExample: dcmrcv DCMRCV:11112 -dest /tmp \n"
            + "=> Starts server listening on port 11112, accepting association "
            + "requests with DCMRCV as called AE title. Received objects "
            + "are stored to /tmp.";

    private static String[] TLS1 = { "TLSv1" };

    private static String[] SSL3 = { "SSLv3" };

    private static String[] NO_TLS1 = { "SSLv3", "SSLv2Hello" };

    private static String[] NO_SSL2 = { "TLSv1", "SSLv3" };

    private static String[] NO_SSL3 = { "TLSv1", "SSLv2Hello" };

    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };

    private static final String[] ONLY_DEF_TS = { UID.ImplicitVRLittleEndian };

    private static final String[] NATIVE_TS = { UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian };

    private static final String[] NATIVE_LE_TS = { UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian };

    private static final String[] NON_RETIRED_TS = { UID.JPEGLSLossless,
            UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14,
            UID.JPEG2000LosslessOnly, UID.DeflatedExplicitVRLittleEndian,
            UID.RLELossless, UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian,
            UID.JPEGBaseline1, UID.JPEGExtended24, UID.JPEGLSLossyNearLossless,
            UID.JPEG2000, UID.MPEG2, };

    private static final String[] NON_RETIRED_LE_TS = { UID.JPEGLSLossless,
            UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14,
            UID.JPEG2000LosslessOnly, UID.DeflatedExplicitVRLittleEndian,
            UID.RLELossless, UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian, UID.JPEGBaseline1, UID.JPEGExtended24,
            UID.JPEGLSLossyNearLossless, UID.JPEG2000, UID.MPEG2, };

    private static final String[] CUIDS = {
            UID.BasicStudyContentNotificationSOPClassRetired,
            UID.StoredPrintStorageSOPClassRetired,
            UID.HardcopyGrayscaleImageStorageSOPClassRetired,
            UID.HardcopyColorImageStorageSOPClassRetired,
            UID.ComputedRadiographyImageStorage,
            UID.DigitalXRayImageStorageForPresentation,
            UID.DigitalXRayImageStorageForProcessing,
            UID.DigitalMammographyXRayImageStorageForPresentation,
            UID.DigitalMammographyXRayImageStorageForProcessing,
            UID.DigitalIntraOralXRayImageStorageForPresentation,
            UID.DigitalIntraOralXRayImageStorageForProcessing,
            UID.StandaloneModalityLUTStorageRetired,
            UID.EncapsulatedPDFStorage, UID.StandaloneVOILUTStorageRetired,
            UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
            UID.ColorSoftcopyPresentationStateStorageSOPClass,
            UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
            UID.BlendingSoftcopyPresentationStateStorageSOPClass,
            UID.XRayAngiographicImageStorage, UID.EnhancedXAImageStorage,
            UID.XRayRadiofluoroscopicImageStorage, UID.EnhancedXRFImageStorage,
            UID.XRayAngiographicBiPlaneImageStorageRetired,
            UID.PositronEmissionTomographyImageStorage,
            UID.StandalonePETCurveStorageRetired, UID.CTImageStorage,
            UID.EnhancedCTImageStorage, UID.NuclearMedicineImageStorage,
            UID.UltrasoundMultiFrameImageStorageRetired,
            UID.UltrasoundMultiFrameImageStorage, UID.MRImageStorage,
            UID.EnhancedMRImageStorage, UID.MRSpectroscopyStorage,
            UID.RTImageStorage, UID.RTDoseStorage, UID.RTStructureSetStorage,
            UID.RTBeamsTreatmentRecordStorage, UID.RTPlanStorage,
            UID.RTBrachyTreatmentRecordStorage,
            UID.RTTreatmentSummaryRecordStorage,
            UID.NuclearMedicineImageStorageRetired,
            UID.UltrasoundImageStorageRetired, UID.UltrasoundImageStorage,
            UID.RawDataStorage, UID.SpatialRegistrationStorage,
            UID.SpatialFiducialsStorage, UID.RealWorldValueMappingStorage,
            UID.SecondaryCaptureImageStorage,// A enlever pour tester ce qu'il nous envoie si la SOP n'est pas connue.
            UID.MultiFrameSingleBitSecondaryCaptureImageStorage,
            UID.MultiFrameGrayscaleByteSecondaryCaptureImageStorage,
            UID.MultiFrameGrayscaleWordSecondaryCaptureImageStorage,
            UID.MultiFrameTrueColorSecondaryCaptureImageStorage,
            UID.VLImageStorageTrialRetired, UID.VLEndoscopicImageStorage,
            UID.VideoEndoscopicImageStorage, UID.VLMicroscopicImageStorage,
            UID.VideoMicroscopicImageStorage,
            UID.VLSlideCoordinatesMicroscopicImageStorage,
            UID.VLPhotographicImageStorage, UID.VideoPhotographicImageStorage,
            UID.OphthalmicPhotography8BitImageStorage,
            UID.OphthalmicPhotography16BitImageStorage,
            UID.StereometricRelationshipStorage,
            UID.VLMultiFrameImageStorageTrialRetired,
            UID.StandaloneOverlayStorageRetired, UID.BasicTextSRStorage,
            UID.EnhancedSRStorage, UID.ComprehensiveSRStorage,
            UID.ProcedureLogStorage, UID.MammographyCADSRStorage,
            UID.KeyObjectSelectionDocumentStorage,
            UID.ChestCADSRStorage, UID.XRayRadiationDoseSRStorage,
            UID.EncapsulatedPDFStorage, UID.EncapsulatedCDAStorage,
            UID.StandaloneCurveStorageRetired,
            UID.TwelveLeadECGWaveformStorage, UID.GeneralECGWaveformStorage,
            UID.AmbulatoryECGWaveformStorage, UID.HemodynamicWaveformStorage,
            UID.CardiacElectrophysiologyWaveformStorage,
            UID.BasicVoiceAudioWaveformStorage, UID.HangingProtocolStorage,
            UID.SiemensCSANonImageStorage,
            UID.Dcm4cheAttributesModificationNotificationSOPClass };


    @SuppressWarnings(value = "unchecked")
    public static void main(String[] args) {
        DcmRcv pacs = new DcmRcv("PACS");

        String port = "104";
        String[] aetPort = split(port, ':', 1);
        pacs.setPort(104);
        if (aetPort[0] != null) {
            String aetHost = "CT";
            pacs.setAEtitle(aetHost);

            pacs.setHostname(aetHost);

        }
        

        pacs.setDestination("d:/Users/INFO-H-400/Desktop/DICOM/");
        
        pacs.initTransferCapability();
        //System.out.println("UID SOP : "+ UID.SecondaryCaptureImageStorage);
        try {
            pacs.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
 }

    private static Properties loadProperties(String url) {
        Properties props = new Properties();
        try {
            InputStream inStream = openFileOrURL(url);
            try {
                props.load(inStream);
            } finally {
                inStream.close();
            }
        } catch (Exception e) {
            exit("Failed to load properties from " + url);
        }
        return props;
    }

    private static KeyStore loadKeyStore(String url, char[] password) throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(toKeyStoreType(url));
        InputStream in = openFileOrURL(url);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    private static InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return DcmRcv.class.getClassLoader().getResourceAsStream(
                    url.substring(9));
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }   }

    private static String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12")
                ? "PKCS12" : "JKS";
    }

    private static String[] split(String s, char delim, int defPos) {
        String[] s2 = new String[2];
        s2[defPos] = s;
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmrcv -h' for more information.");
        System.exit(1);
    }

    private static int eventTypeIdOf(DicomObject result) {
        return result.contains(Tag.FailedSOPInstanceUIDList) ? 2 : 1;
    }

private final Executor executor;

private final Device device;

private final NetworkApplicationEntity ae = new NetworkApplicationEntity();

private final NetworkConnection nc = new NetworkConnection();

private final StorageSCP storageSCP = new StorageSCP(this, CUIDS);

private final StgCmtSCP stgcmtSCP =
new StgCmtSCP(this);

private String[] tsuids = NON_RETIRED_LE_TS;

private FileCache cache = new FileCache();

private File devnull;

private Properties calling2dir;

private Properties called2dir;

private String callingdefdir = "OTHER";

private String calleddefdir = "OTHER";

private int fileBufferSize = 1024;

private int rspdelay = 0;

private String keyStoreURL = "resource:tls/test_sys_2.p12";

private char[] keyStorePassword = SECRET;

private char[] keyPassword;

private String trustStoreURL = "resource:tls/mesa_certs.jks";

private char[] trustStorePassword = SECRET;

private Timer stgcmtTimer;

private boolean stgcmtReuseFrom = false;

private boolean stgcmtReuseTo = false;

private int stgcmtPort = 104;

private long stgcmtDelay = 1000;

private int stgcmtRetry = 0;

private long stgcmtRetryPeriod = 60000;

private String stgcmtRetrieveAET;

private String stgcmtRetrieveAETs;

private final DimseRSPHandler nEventReportRspHandler = 
new DimseRSPHandler();

public DcmRcv() {
this("DCMRCV");
}

public DcmRcv(String name) {
    device = new Device(name);
    executor = new NewThreadExecutor(name);
    device.setNetworkApplicationEntity(ae);
    device.setNetworkConnection(nc);
    ae.setNetworkConnection(nc);
    ae.setAssociationAcceptor(true);
    ae.register(new VerificationService());
    ae.register(storageSCP);
    ae.register(stgcmtSCP);
}

public final void setAEtitle(String aet) {
ae.setAETitle(aet);
}

public final void setHostname(String hostname) {
nc.setHostname(hostname);
}

public final void setPort(int port) {
nc.setPort(port);
}

public final void setTlsProtocol(String[] tlsProtocol) {
nc.setTlsProtocol(tlsProtocol);
}

public final void setTlsWithoutEncyrption() {
nc.setTlsWithoutEncyrption();
}

public final void setTls3DES_EDE_CBC() {
nc.setTls3DES_EDE_CBC();
}

public final void setTlsAES_128_CBC() {
nc.setTlsAES_128_CBC();
}

public final void setTlsNeedClientAuth(boolean needClientAuth) {
nc.setTlsNeedClientAuth(needClientAuth);
}

public final void setKeyStoreURL(String url) {
keyStoreURL = url;
}

public final void setKeyStorePassword(String pw) {
keyStorePassword = pw.toCharArray();
}

public final void setKeyPassword(String pw) {
keyPassword = pw.toCharArray();
}

public final void setTrustStorePassword(String pw) {
trustStorePassword = pw.toCharArray();
}

public final void setTrustStoreURL(String url) {
trustStoreURL = url;
}

public final void setConnectTimeout(int connectTimeout) {
nc.setConnectTimeout(connectTimeout);
}

public final void setPackPDV(boolean packPDV) {
ae.setPackPDV(packPDV);
}

public final void setAssociationReaperPeriod(int period) {
device.setAssociationReaperPeriod(period);
}

public final void setTcpNoDelay(boolean tcpNoDelay) {
nc.setTcpNoDelay(tcpNoDelay);
}

public final void setAcceptTimeout(int timeout) {
nc.setAcceptTimeout(timeout);
}

public final void setRequestTimeout(int timeout) {
nc.setRequestTimeout(timeout);
}

public final void setReleaseTimeout(int timeout) {
nc.setReleaseTimeout(timeout);
}

public final void setSocketCloseDelay(int delay) {
nc.setSocketCloseDelay(delay);
}

public final void setIdleTimeout(int timeout) {
ae.setIdleTimeout(timeout);
}

public final void setDimseRspTimeout(int timeout) {
ae.setDimseRspTimeout(timeout);
}

public final void setMaxPDULengthSend(int maxLength) {
ae.setMaxPDULengthSend(maxLength);
}

public void setMaxPDULengthReceive(int maxLength) {
ae.setMaxPDULengthReceive(maxLength);
}

public final void setReceiveBufferSize(int bufferSize) {
nc.setReceiveBufferSize(bufferSize);
}

public final void setSendBufferSize(int bufferSize) {
nc.setSendBufferSize(bufferSize);
}

public final void setDimseRspDelay(int delay) {
rspdelay = delay;
}

public final int getDimseRspDelay() {
return rspdelay;
}

public final void setStgCmtReuseFrom(boolean stgcmtReuseFrom) {
this.stgcmtReuseFrom = stgcmtReuseFrom;
}

public final boolean isStgCmtReuseFrom() {
return stgcmtReuseFrom;
}

public final void setStgCmtReuseTo(boolean stgcmtReuseTo) {
this.stgcmtReuseTo = stgcmtReuseTo;
}

public final boolean isStgCmtReuseTo() {
return stgcmtReuseTo;
}

public final int getStgCmtPort() {
return stgcmtPort;
}

public final void setStgCmtPort(int stgcmtPort) {
this.stgcmtPort = stgcmtPort;
}

public final void setStgCmtDelay(long delay) {
this.stgcmtDelay = delay;
}

public final long getStgCmtDelay() {
return stgcmtDelay;
}

public final int getStgCmtRetry() {
return stgcmtRetry;
}

public final void setStgCmtRetry(int stgcmtRetry) {
this.stgcmtRetry = stgcmtRetry;
}

public final long getStgCmtRetryPeriod() {
return stgcmtRetryPeriod;
}

public final void setStgCmtRetryPeriod(long stgcmtRetryPeriod) {
this.stgcmtRetryPeriod = stgcmtRetryPeriod;
}

public final String getStgCmtRetrieveAET() {
return stgcmtRetrieveAET;
}

public final void setStgCmtRetrieveAET(String aet) {
this.stgcmtRetrieveAET = aet;
}


public final String getStgCmtRetrieveAETs() {
return stgcmtRetrieveAETs;
}

public final void setStgCmtRetrieveAETs(String aet) {
this.stgcmtRetrieveAETs = aet;
}

final Executor executor() {
return executor;
}


public void setTransferSyntax(String[] tsuids) {
    this.tsuids = tsuids;
}

public void initTransferCapability() {
    TransferCapability[] tc;
    if (isStgcmtEnabled()) {
        tc = new TransferCapability[CUIDS.length + 2];
        tc[tc.length -1 ] = new TransferCapability(
                UID.StorageCommitmentPushModelSOPClass, ONLY_DEF_TS,
                TransferCapability.SCP);
    } else {
        tc = new TransferCapability[CUIDS.length + 1];
    }
    tc[0] = new TransferCapability(UID.VerificationSOPClass, ONLY_DEF_TS,
            TransferCapability.SCP);
    for (int i = 0; i < CUIDS.length; i++)
        tc[i + 1] = new TransferCapability(CUIDS[i], tsuids,
                TransferCapability.SCP);
    ae.setTransferCapability(tc);
}

public void setFileBufferSize(int size) {
    fileBufferSize = size;
}

public void setMaxOpsPerformed(int maxOps) {
    ae.setMaxOpsPerformed(maxOps);
}

public void setDestination(String filePath) {
    File f = new File(filePath);
    if ("/dev/null".equals(filePath)) {
        devnull = f;
        cache.setCacheRootDir(null);
    } else {
        devnull = null;
        cache.setCacheRootDir(f);
    }
}

public void setCalling2Dir(Properties calling2dir) {
    this.calling2dir = calling2dir;
}

public void setCalled2Dir(Properties called2dir) {
    this.called2dir = called2dir;
}

public void setCallingDefDir(String callingdefdir) {
    this.callingdefdir = callingdefdir;
}

public void setCalledDefDir(String calleddefdir) {
    this.calleddefdir = calleddefdir;
}

public void setJournal(String journalRootDir) {
    cache.setJournalRootDir(new File(journalRootDir));
// Prefix JournalFileName to distinguish from journal files created
// by other applications than DcmRcv
    cache.setJournalFileName("DcmRcv." + cache.getJournalFileName());
}

public void setJournalFilePathFormat(String format) {
    cache.setJournalFilePathFormat(format);
}

public void initTLS() throws GeneralSecurityException, IOException {
    KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword);
    KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword);
    device.initTLS(keyStore,
            keyPassword != null ? keyPassword : keyStorePassword,
            trustStore);
}

public void start() throws IOException {
    device.startListening(executor);
    System.out.println("Start Server listening on port " + nc.getPort());
}

public void stop() {
    if (device != null)
        device.stopListening();
    
    if (nc != null)
        System.out.println("Stop Server listening on port " + nc.getPort());
    else
        System.out.println("Stop Server");
}

boolean isStoreFile() {
        return devnull != null || cache.getCacheRootDir() != null;
    }

private boolean isStgcmtEnabled() {
    return ae.getAETitle() != null && cache.getCacheRootDir() != null;
}

void onCStoreRQ(Association as, int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid, DicomObject rsp) throws IOException {
        System.out.println(" CStore fonctionnel main");
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        
        long date= new Date().getTime();
        
        File file = devnull != null ? devnull
                : new File(mkDir(as), date + ".dcm");
        LOG.info("M-WRITE {}", file);
        try {
            DicomOutputStream dos = new DicomOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(file),
                            fileBufferSize));
            try {
                BasicDicomObject fmi = new BasicDicomObject();
                fmi.initFileMetaInformation(cuid, iuid, tsuid);
                dos.writeFileMetaInformation(fmi);
                dataStream.copyTo(dos);
            } finally {
                CloseUtils.safeClose(dos);
            }
        } catch (IOException e) {
            if (devnull == null && file != null) {
                if (file.delete()) {
                    LOG.info("M-DELETE {}", file);
                }
            }
            throw new DicomServiceException(rq, Status.ProcessingFailure,
                    e.getMessage());
        }
        
        RecupererInfosDicom(file.getAbsolutePath(),date);
}

private File getDir(Association as) {
    File dir = cache.getCacheRootDir();
    if (called2dir != null) {
        dir = new File(dir,
                called2dir.getProperty(as.getCalledAET(), calleddefdir));
    }
    if (calling2dir != null) {
        dir = new File(dir,
                calling2dir.getProperty(as.getCallingAET(), callingdefdir));
    }
    return dir;
}

private File mkDir(Association as) {
    File dir = getDir(as);
    if (dir.mkdirs()) {
        LOG.info("M-WRITE {}", dir);
    }
    return dir;
}

public void onNActionRQ(Association as, DicomObject rq, DicomObject info) {
    stgcmtTimer().schedule(new SendStgCmtResult(this, mkStgCmtAE(as),
            mkStgCmtResult(as, info)), stgcmtDelay, stgcmtRetryPeriod);
}

private NetworkApplicationEntity mkStgCmtAE(Association as) {
    NetworkApplicationEntity stgcmtAE = new NetworkApplicationEntity();
    NetworkConnection stgcmtNC = new NetworkConnection();
    stgcmtNC.setHostname(as.getSocket().getInetAddress().getHostAddress());
    stgcmtNC.setPort(stgcmtPort);
    stgcmtNC.setTlsCipherSuite(nc.getTlsCipherSuite());
    stgcmtAE.setNetworkConnection(stgcmtNC);
    stgcmtAE.setAETitle(as.getRemoteAET());
    stgcmtAE.setTransferCapability(new TransferCapability[]{
        new TransferCapability(
                UID.StorageCommitmentPushModelSOPClass, ONLY_DEF_TS,
                TransferCapability.SCU)});
    return stgcmtAE; 
}


private DicomObject mkStgCmtResult(Association as, DicomObject rqdata) {
    DicomObject result = new BasicDicomObject();
    result.putString(Tag.TransactionUID, VR.UI,
            rqdata.getString(Tag.TransactionUID));
    DicomElement rqsq = rqdata.get(Tag.ReferencedSOPSequence);
    DicomElement resultsq = result.putSequence(Tag.ReferencedSOPSequence);
    if (stgcmtRetrieveAET != null)
        result.putString(Tag.RetrieveAETitle, VR.AE, stgcmtRetrieveAET);
    DicomElement failedsq = null;
    File dir = getDir(as);
    for (int i = 0, n = rqsq.countItems(); i < n; i++) {
        DicomObject rqItem = rqsq.getDicomObject(i);
        String uid = rqItem.getString(Tag.ReferencedSOPInstanceUID);
        DicomObject resultItem = new BasicDicomObject();
        rqItem.copyTo(resultItem);
        if (stgcmtRetrieveAETs != null)
            resultItem.putString(Tag.RetrieveAETitle, VR.AE,
                    stgcmtRetrieveAETs);
        File f = new File(dir, uid);
        if (f.isFile()) {
            resultsq.addDicomObject(resultItem);
        } else {
            resultItem.putInt(Tag.FailureReason, VR.US,
                    NO_SUCH_OBJECT_INSTANCE);
            if (failedsq == null)
                failedsq = result.putSequence(Tag.FailedSOPSequence);
            failedsq.addDicomObject(resultItem);
        }
    }
    return result;
}

private synchronized Timer stgcmtTimer() {
    if (stgcmtTimer == null)
        stgcmtTimer = new Timer("SendStgCmtResult", true);
    return stgcmtTimer;
}

void sendStgCmtResult(NetworkApplicationEntity stgcmtAE, DicomObject result)
throws Exception {
        synchronized(ae) {
            ae.setReuseAssocationFromAETitle(stgcmtReuseFrom
                    ? new String[] { stgcmtAE.getAETitle() }
                    : new String[] {});
            ae.setReuseAssocationToAETitle(stgcmtReuseTo
                    ? new String[] { stgcmtAE.getAETitle() }
                    : new String[] {});
            Association as = ae.connect(stgcmtAE, executor);
            as.nevent(UID.StorageCommitmentPushModelSOPClass,
                    UID.StorageCommitmentPushModelSOPInstance,
                    eventTypeIdOf(result), result, UID.ImplicitVRLittleEndian,
                    nEventReportRspHandler);
            if (!stgcmtReuseFrom && !stgcmtReuseTo)
                as.release(true);
        }
    }

private void RecupererInfosDicom(String pathFile, long date) throws IOException
{
        try {
            try {
                BufferedImage buf=null;
                System.out.println("Fonction Recuperer");
                File DicomFile = new File(pathFile);
                DicomInputStream distr = new DicomInputStream(DicomFile);
                AttributeList attList = new AttributeList();
                attList.read(distr);
    
                
                String fileImage= date+".dcm";
                

                
                
                String PatientName = Attribute.getSingleStringValueOrNull(attList,TagFromName.PatientName);
                String PatientID = Attribute.getSingleStringValueOrNull(attList,TagFromName.PatientID);
                String BirthDatePatient = Attribute.getSingleStringValueOrNull(attList,TagFromName.PatientBirthDate); 
                String PatientSex = Attribute.getSingleStringValueOrNull(attList,TagFromName.PatientSex); 
                String PregnancyStatus = Attribute.getSingleStringValueOrNull(attList,TagFromName.PregnancyStatus);

                
                String RequestingPhysician = Attribute.getSingleStringValueOrNull(attList,TagFromName.RequestingPhysician);
                String StudyDate = Attribute.getSingleStringValueOrNull(attList,TagFromName.StudyDate);
                String StudyID = Attribute.getSingleStringValueOrNull(attList,TagFromName.StudyID);

                String Modality = Attribute.getSingleStringValueOrNull(attList,TagFromName.Modality);
                String SeriesNumber = Attribute.getSingleStringValueOrNull(attList,TagFromName.SeriesNumber);

                
                System.out.println("Patient Name = " + PatientName);
                System.out.println("ID Patient = " + PatientID);
                System.out.println("Study Date = " + StudyDate);
                System.out.println("Modality = " + Modality);
                

                String username ="root"; 

                String password ="1234";

                //Données pour la connexion

                String database ="Dicom";
                String port = "3306";
                String host = "localhost";

                String url = "jdbc:mysql://"+host+ ":" + port + "/" + database;
            try {
                try {
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                } catch (InstantiationException ex) {
                    java.util.logging.Logger.getLogger(DcmRcv.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    java.util.logging.Logger.getLogger(DcmRcv.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(DcmRcv.class.getName()).log(Level.SEVERE, null, ex);
            }

                Connection conn;           
                conn = DriverManager.getConnection (url,username,password);

                Statement s= conn.createStatement();
                
                String Query = "SELECT Nom FROM Dicom.Patient WHERE (Nom ='"+PatientName+"' AND Naissance = "+BirthDatePatient +")";
                s.executeQuery(Query);
                ResultSet rs = s.getResultSet();
               
                rs.first();
                try
                {
                    String nom = rs.getString("Nom");

                    System.out.println("Mise à jour de CamImage");
                    
                    String queryID = "SELECT idPatient FROM Dicom.Patient WHERE (Nom ='"+PatientName+"' AND Naissance = "+BirthDatePatient +")";
                    s.executeQuery(queryID);
                    ResultSet rs2 = s.getResultSet();
                    rs2.first();
                    int idpat = rs2.getInt("idPatient");

                    String queryID2 = "SELECT idEtude FROM Dicom.Etude WHERE (Date ="+StudyDate+" AND Patient_idPatient = "+idpat + ")";
                    
                    s.executeQuery(queryID2);
                    ResultSet rs3 = s.getResultSet();
                    rs3.first();
                    int idet = rs3.getInt("idEtude");

                    String queryID3 = "SELECT idSerie FROM Dicom.Serie WHERE Etude_idEtude =" + idet;

                    s.executeQuery(queryID3);
                    ResultSet rs4 = s.getResultSet();
                    rs4.first();
                    int idser = rs4.getInt("idSerie");

                    //Mise à jour table camImage
                    String update="INSERT INTO Dicom.CamImage VALUES (null,'"+fileImage+"',"+idser +")";
                    System.out.println(update);
                    s.executeUpdate(update);
                    
                    rs2.close();
                    rs3.close();
                    rs4.close();
                }
               // int naiss = rs.getInt("Naissance");
                catch(java.sql.SQLException sqlE)
                {

                    
                    System.out.println("Mise à jour de toutes les tables");

                    //Mise à jour table Patient
                    String update = "INSERT INTO Dicom.Patient VALUES(null,'"+PatientName+"',"+BirthDatePatient+",'" + PatientSex +"','" + PregnancyStatus+"')"; 
                    System.out.println(update);
                    s.executeUpdate(update);
                    
                    String queryID = "SELECT idPatient FROM Dicom.Patient WHERE (Nom ='"+PatientName+"' AND Naissance = "+BirthDatePatient +")";
                    System.out.println(queryID);
                    
                    s.executeQuery(queryID);
                    ResultSet rs2 = s.getResultSet();
                    rs2.first();
                    int idpat = rs2.getInt("idPatient");
                    //Mise à jour table Etude
                    String update2= "INSERT INTO Dicom.Etude VALUES(null,"+StudyDate+", '"+RequestingPhysician+"',"+idpat +" )";
                    System.out.println(update2);
                    s.executeUpdate(update2);

                    String queryID2 = "SELECT idEtude FROM Dicom.Etude WHERE (Date ="+StudyDate+" AND Patient_idPatient = "+idpat + ")";
                    System.out.println(queryID2);
                    
                    s.executeQuery(queryID2);
                    ResultSet rs3 = s.getResultSet();
                    rs3.first();
                    int idet = rs3.getInt("idEtude");
                    //Mise à jour table Serie
                    String update3= "INSERT INTO Dicom.Serie VALUES(null,'"+Modality+"',"+idet+" )";
                    System.out.println(update3);
                    s.executeUpdate(update3);
                    
                    String queryID3 = "SELECT idSerie FROM Dicom.Serie WHERE Etude_idEtude =" + idet;
                    System.out.println(queryID3);

                    s.executeQuery(queryID3);
                    ResultSet rs4 = s.getResultSet();
                    rs4.first();
                    int idser = rs4.getInt("idSerie");
                    //Mise à jour table camImage
                    String update4="INSERT INTO Dicom.CamImage VALUES (null,'"+fileImage+"',"+idser +")";
                    System.out.println(update4);
                    s.executeUpdate(update4);
                    
                    rs2.close();
                    rs3.close();
                    rs4.close();
                }
                
                s.close();
                
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(DcmRcv.class.getName()).log(Level.SEVERE, null, ex);
            }

                } catch (DicomException ex) {
                    java.util.logging.Logger.getLogger(DcmRcv.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Infos mal recuperees");
                }





}

}


