package com.sos.joc.order.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.LogOrderContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.LogMime;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.order.resource.IOrderLogHtmlResource;

@Path("order")
public class OrderLogHtmlResourceImpl extends JOCResourceImpl implements IOrderLogHtmlResource {

	private static final String API_CALL = "./order/log/html";

	@Override
	public JOCDefaultResponse getOrderLogHtml(String xAccessToken, String accessToken, String queryAccessToken,
			String jobschedulerId, String orderId, String jobChain, String historyId) throws Exception {
		return getOrderLogHtml(getAccessToken(xAccessToken, accessToken), queryAccessToken, jobschedulerId, orderId,
				jobChain, historyId);
	}

	public JOCDefaultResponse getOrderLogHtml(String accessToken, String queryAccessToken, String jobschedulerId,
			String orderId, String jobChain, String historyId) throws Exception {
		OrderHistoryFilter orderHistoryFilter = new OrderHistoryFilter();

		try {

			orderHistoryFilter.setHistoryId(historyId);
			orderHistoryFilter.setJobChain(normalizePath(jobChain));
			orderHistoryFilter.setOrderId(orderId);
			orderHistoryFilter.setJobschedulerId(jobschedulerId);
			orderHistoryFilter.setMime(LogMime.HTML);

			if (accessToken == null) {
				accessToken = queryAccessToken;
			}

			JOCDefaultResponse jocDefaultResponse = init(API_CALL, orderHistoryFilter, accessToken, jobschedulerId,
					getPermissonsJocCockpit(jobschedulerId, accessToken).getOrder().getView().isOrderLog());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			checkRequiredParameter("jobschedulerId", orderHistoryFilter.getJobschedulerId());
			checkRequiredParameter("jobChain", orderHistoryFilter.getJobChain());
			checkRequiredParameter("orderId", orderHistoryFilter.getOrderId());
			checkRequiredParameter("historyId", orderHistoryFilter.getHistoryId());

			LogOrderContent logOrderContent = new LogOrderContent(orderHistoryFilter, dbItemInventoryInstance,
					accessToken);
			String log = logOrderContent.getLog();

			return JOCDefaultResponse.responseHtmlStatus200(
					logOrderContent.htmlPageWithColouredLogContent(log, "Order " + orderHistoryFilter.getOrderId()));
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseHTMLStatusJSError(e);

		} catch (Exception e) {
			return JOCDefaultResponse.responseHTMLStatusJSError(e, getJocError());
		}
	}

}
