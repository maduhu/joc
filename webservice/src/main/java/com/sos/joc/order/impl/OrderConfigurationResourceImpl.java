package com.sos.joc.order.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JOCXmlCommand;
import com.sos.joc.classes.configuration.ConfigurationUtils;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Configuration200;
import com.sos.joc.model.common.ConfigurationMime;
import com.sos.joc.model.order.OrderConfigurationFilter;
import com.sos.joc.order.resource.IOrderConfigurationResource;

@Path("order")
public class OrderConfigurationResourceImpl extends JOCResourceImpl implements IOrderConfigurationResource {

    private static final String API_CALL = "./order/configuration";

    @Override
    public JOCDefaultResponse postOrderConfiguration(String accessToken, OrderConfigurationFilter orderBody) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, orderBody, accessToken, orderBody.getJobschedulerId(), getPermissonsJocCockpit(
                    accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Configuration200 entity = new Configuration200();
            JOCXmlCommand jocXmlCommand = new JOCXmlCommand(this);
            if (checkRequiredParameter("orderId", orderBody.getOrderId()) && checkRequiredParameter("jobChain", orderBody.getJobChain())) {
                boolean responseInHtml = orderBody.getMime() == ConfigurationMime.HTML;
                String orderCommand = jocXmlCommand.getShowOrderCommand(normalizePath(orderBody.getJobChain()), orderBody.getOrderId(), "source");
                entity = ConfigurationUtils.getConfigurationSchema(jocXmlCommand, orderCommand, "/spooler/answer/order", "order", responseInHtml,
                        accessToken);
            }
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
