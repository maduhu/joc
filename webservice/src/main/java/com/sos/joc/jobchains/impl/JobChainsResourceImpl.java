package com.sos.joc.jobchains.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobchains.JOCXmlJobChainCommand;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobchains.resource.IJobChainsResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.jobChain.JobChainPath;
import com.sos.joc.model.jobChain.JobChainV;
import com.sos.joc.model.jobChain.JobChainsFilter;
import com.sos.joc.model.jobChain.JobChainsV;

@Path("job_chains")
public class JobChainsResourceImpl extends JOCResourceImpl implements IJobChainsResource {

	private static final String API_CALL = "./job_chains";

	@Override
	public JOCDefaultResponse postJobChains(String xAccessToken, String accessToken, JobChainsFilter jobChainsFilter)
			throws Exception {
		return postJobChains(getAccessToken(xAccessToken, accessToken), jobChainsFilter);
	}

	public JOCDefaultResponse postJobChains(String accessToken, JobChainsFilter jobChainsFilter) throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobChainsFilter, accessToken,
					jobChainsFilter.getJobschedulerId(),
					getPermissonsJocCockpit(jobChainsFilter.getJobschedulerId(), accessToken).getJobChain().getView()
							.isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			JobChainsV entity = new JobChainsV();

			JOCXmlJobChainCommand jocXmlCommand = new JOCXmlJobChainCommand(this, accessToken);
			List<JobChainPath> jobChains = jobChainsFilter.getJobChains();
			List<Folder> folders = addPermittedFolder(jobChainsFilter.getFolders());
            List<JobChainV> listOfJobChains = null;

			if (jobChains != null && !jobChains.isEmpty()) {
                listOfJobChains = jocXmlCommand.getJobChainsFromShowJobChain(jobChains, jobChainsFilter);
			} else if (folders != null && !folders.isEmpty()) {
                listOfJobChains = jocXmlCommand.getJobChainsFromShowState(folders, jobChainsFilter);
			} else {
                listOfJobChains = jocXmlCommand.getJobChainsFromShowState(jobChainsFilter);
			}
            listOfJobChains = addAllPermittedJobChains(listOfJobChains);
            entity.setJobChains(listOfJobChains);
			entity.setNestedJobChains(jocXmlCommand.getNestedJobChains());
			entity.setDeliveryDate(new Date());

			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

    private List<JobChainV> addAllPermittedJobChains(List<JobChainV> jobChainsToAdd) {
        if (folderPermissions == null) {
            return jobChainsToAdd;
        }
        Set<Folder> folders = folderPermissions.getListOfFolders();
        if (folders.isEmpty()) {
            return jobChainsToAdd;
        }
        List<JobChainV> listOfJobChains = new ArrayList<JobChainV>();
        for (JobChainV jobChain : jobChainsToAdd) {
            if (jobChain != null && canAdd(jobChain.getPath(), folders)) {
                listOfJobChains.add(jobChain);
            }
        }
        return listOfJobChains;

    }
}
