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
import java.util.TimerTask;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.NetworkApplicationEntity;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date:: 0000-00-00 $
 * @since Mar 22, 2010
 */
class SendStgCmtResult extends TimerTask {

    private final DcmRcv dcmrcv;
    private final NetworkApplicationEntity stgcmtAE;
    private final DicomObject result;
    private int failureCount;

    public SendStgCmtResult(DcmRcv dcmrcv, NetworkApplicationEntity stgcmtAE,
            DicomObject result) {
        this.dcmrcv = dcmrcv;
        this.stgcmtAE = stgcmtAE;
        this.result = result;
    }

    @Override
    public void run() {
        try {
            dcmrcv.sendStgCmtResult(stgcmtAE, result);
        } catch (Exception e) {
            DcmRcv.LOG.warn("Send Storage Commitment Result to "
                    + stgcmtAE.getAETitle() + " failed:", e);
            if (failureCount++ < dcmrcv.getStgCmtRetry()) {
                DcmRcv.LOG.info("Schedule retry in "
                        + (dcmrcv.getStgCmtRetryPeriod()/1000) + "s.");
               return;
            }
        }
        this.cancel();
    }

}

