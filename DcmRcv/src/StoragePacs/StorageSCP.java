/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package StoragePacs;;

/**
 *
 * @author Yohan
 */
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.service.StorageService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date:: 0000-00-00 $
 * @since Mar 22, 2010
 */
class StorageSCP extends StorageService {

    private final DcmRcv dcmrcv;

    public StorageSCP(DcmRcv dcmrcv, String[] sopClasses) {
        super(sopClasses);
        this.dcmrcv = dcmrcv;
    }

    /** Overwrite {@link StorageService#cstore} to send delayed C-STORE RSP 
     * by separate Thread, so reading of following received C-STORE RQs from
     * the open association is not blocked.
     */
    @Override
    public void cstore(final Association as, final int pcid, DicomObject rq,
            PDVInputStream dataStream, String tsuid)
            throws DicomServiceException, IOException {
        final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
        if (dcmrcv.getDimseRspDelay() > 0) {
            dcmrcv.executor().execute(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(dcmrcv.getDimseRspDelay());
                        as.writeDimseRSP(pcid, rsp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            as.writeDimseRSP(pcid, rsp);
        }
        onCStoreRSP(as, pcid, rq, dataStream, tsuid, rsp);
    }

    @Override
    protected void onCStoreRQ(Association as, int pcid, DicomObject rq,
            PDVInputStream dataStream, String tsuid, DicomObject rsp)
            throws IOException, DicomServiceException {
        if (dcmrcv.isStoreFile())
            dcmrcv.onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
        else
            super.onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
    }

}