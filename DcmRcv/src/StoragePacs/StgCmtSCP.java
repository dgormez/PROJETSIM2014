/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package StoragePacs;;


import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.service.DicomService;
import org.dcm4che2.net.service.NActionSCP;
/**
 *
 * @author Yohan
 */
class StgCmtSCP extends DicomService implements NActionSCP {

    private final DcmRcv dcmrcv;

    public StgCmtSCP(DcmRcv dcmrcv) {
        super(UID.StorageCommitmentPushModelSOPClass);
        this.dcmrcv = dcmrcv;
    }

    @Override
    public void naction(Association as, int pcid, DicomObject rq,
            DicomObject info) throws IOException {
        DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        dcmrcv.onNActionRQ(as, rq, info);
        as.writeDimseRSP(pcid, rsp);
    }

}









               