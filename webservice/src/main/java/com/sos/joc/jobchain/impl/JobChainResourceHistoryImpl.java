package com.sos.joc.jobchain.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JOCXmlCommand;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobchain.resource.IJobChainResourceHistory;
import com.sos.joc.model.jobChain.JobChainHistoryFilter;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderHistoryState;
import com.sos.joc.model.order.OrderHistoryStateText;
import com.sos.scheduler.model.commands.JSCmdShowJobChain;

@Path("job_chain")
public class JobChainResourceHistoryImpl extends JOCResourceImpl implements IJobChainResourceHistory {

    private static final int DEFAULT_MAX_HISTORY_ITEMS = 25;
    private static final String ORDER_HISTORY = "order_history";
    private static final String KEY_FOR_ERROR_NODE_LIST = "nodes";
    private static final String XPATH_FOR_ERROR_NODES = "//job_chain_node[@error_state='%s']";
    private static final String XPATH_FOR_ORDER_HISTORY = "/spooler/answer/job_chain/order_history/order";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobChainResourceHistoryImpl.class);
    private static final String API_CALL = "./job_chain/history";

    @Override
    public JOCDefaultResponse postJobChainHistory(String accessToken, JobChainHistoryFilter jobChainHistoryFilter) throws Exception {

        LOGGER.debug(API_CALL);
        try {
            JOCDefaultResponse jocDefaultResponse = init(accessToken, jobChainHistoryFilter.getJobschedulerId(), getPermissons(accessToken)
                    .getJobChain().getView().isHistory());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JOCXmlCommand jocXmlCommand = new JOCXmlCommand(dbItemInventoryInstance.getUrl());
            if (jobChainHistoryFilter.getMaxLastHistoryItems() == null) {
                jobChainHistoryFilter.setMaxLastHistoryItems(DEFAULT_MAX_HISTORY_ITEMS);
            }
            // TODO nested job chains have to consider too
            String postCommand = createJobchainHistoryPostCommand(jobChainHistoryFilter);
            jocXmlCommand.executePostWithThrowBadRequest(postCommand, accessToken);

            jocXmlCommand.createNodeList(XPATH_FOR_ORDER_HISTORY);

            int count = jocXmlCommand.getNodeList().getLength();

            List<OrderHistoryItem> listOfHistory = new ArrayList<OrderHistoryItem>();

            for (int i = 0; i < count; i++) {
                OrderHistoryState state = new OrderHistoryState();
                jocXmlCommand.getElementFromList(i);
                String node = jocXmlCommand.getAttribute("state");
                OrderHistoryItem history = new OrderHistoryItem();
                history.setEndTime(jocXmlCommand.getAttributeAsDate("end_time"));
                if (history.getEndTime() != null) {
                    jocXmlCommand.createNodeList(KEY_FOR_ERROR_NODE_LIST, String.format(XPATH_FOR_ERROR_NODES, node));
                    if (jocXmlCommand.getNodeList(KEY_FOR_ERROR_NODE_LIST).getLength() == 0) {
                        state.setSeverity(0);
                        state.set_text(OrderHistoryStateText.SUCCESSFUL);
                    } else {
                        state.setSeverity(2);
                        state.set_text(OrderHistoryStateText.FAILED);
                    }
                    history.setState(state);
                } else {
                    state.setSeverity(1);
                    state.set_text(OrderHistoryStateText.INCOMPLETE);
                    history.setState(state);

                }
                history.setHistoryId(jocXmlCommand.getAttribute("history_id"));
                history.setJobChain(normalizePath(jocXmlCommand.getAttribute("job_chain")));
                history.setNode(node);
                history.setOrderId(jocXmlCommand.getAttribute("id"));
                history.setPath(jocXmlCommand.getAttribute("path"));
                history.setStartTime(jocXmlCommand.getAttributeAsDate("start_time"));

                listOfHistory.add(history);
            }

            OrderHistory entity = new OrderHistory();
            entity.setDeliveryDate(new Date());
            entity.setHistory(listOfHistory);

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getMetaInfo(API_CALL, jobChainHistoryFilter));
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            JocError err = new JocError();
            err.addMetaInfoOnTop(getMetaInfo(API_CALL, jobChainHistoryFilter));
            return JOCDefaultResponse.responseStatusJSError(e, err);

        }
    }

    public String createJobchainHistoryPostCommand(final JobChainHistoryFilter jobChainHistoryFilterSchema) {

        JSCmdShowJobChain showJob = Globals.schedulerObjectFactory.createShowJobChain();
        showJob.setMaxOrders(BigInteger.valueOf(0));
        showJob.setMaxOrderHistory(BigInteger.valueOf(jobChainHistoryFilterSchema.getMaxLastHistoryItems()));
        showJob.setWhat(ORDER_HISTORY);
        showJob.setJobChain(normalizePath(jobChainHistoryFilterSchema.getJobChain()));
        return Globals.schedulerObjectFactory.toXMLString(showJob);
    }

}
