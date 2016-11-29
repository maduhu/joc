package com.sos.joc.orders.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.ReportTriggerDBLayer;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderHistoryState;
import com.sos.joc.model.order.OrderHistoryStateText;
import com.sos.joc.model.order.OrderPath;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.orders.resource.IOrdersResourceHistory;

@Path("orders")
public class OrdersResourceHistoryImpl extends JOCResourceImpl implements IOrdersResourceHistory {

    private static final String API_CALL = "./orders/history";

    @Override
    public JOCDefaultResponse postOrdersHistory(String accessToken, OrdersFilter ordersFilter) throws Exception {
        try {
            initLogging(API_CALL, ordersFilter);
            JOCDefaultResponse jocDefaultResponse = init(accessToken, ordersFilter.getJobschedulerId(), getPermissons(accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Globals.beginTransaction();
            
            List<OrderHistoryItem> listHistory = new ArrayList<OrderHistoryItem>();

            ReportTriggerDBLayer reportTriggerDBLayer = new ReportTriggerDBLayer(Globals.sosHibernateConnection);
            reportTriggerDBLayer.getFilter().setSchedulerId(ordersFilter.getJobschedulerId());
            if (ordersFilter.getDateFrom() != null) {
                reportTriggerDBLayer.getFilter().setExecutedFrom(JobSchedulerDate.getDate(ordersFilter.getDateFrom(), ordersFilter.getTimeZone()));
            }
            if (ordersFilter.getDateTo() != null) {
                reportTriggerDBLayer.getFilter().setExecutedTo(JobSchedulerDate.getDate(ordersFilter.getDateTo(), ordersFilter.getTimeZone()));
            }

            if (ordersFilter.getOrders().size() > 0) {
                for (OrderPath orderPath : ordersFilter.getOrders()) {
                    reportTriggerDBLayer.getFilter().addOrderPath(orderPath.getJobChain(), orderPath.getOrderId());
                }
            } else {
                ordersFilter.setRegex("");
            }

            List<DBItemReportTrigger> listOfReportTriggerDBItems = reportTriggerDBLayer.getSchedulerOrderHistoryListFromTo();

            for (DBItemReportTrigger dbItemReportTrigger : listOfReportTriggerDBItems) {

                boolean add = true;
                OrderHistoryItem history = new OrderHistoryItem();
                history.setEndTime(dbItemReportTrigger.getEndTime());
                history.setHistoryId(String.valueOf(dbItemReportTrigger.getHistoryId()));
                history.setJobChain(dbItemReportTrigger.getParentName());
                history.setNode(dbItemReportTrigger.getState());
                history.setOrderId(dbItemReportTrigger.getName());
                history.setPath(dbItemReportTrigger.getFullOrderQualifier());
                history.setStartTime(dbItemReportTrigger.getStartTime());
                OrderHistoryState state = new OrderHistoryState();

                if (dbItemReportTrigger.getStartTime() != null && dbItemReportTrigger.getEndTime() == null) {
                    state.setSeverity(1);
                    state.set_text(OrderHistoryStateText.INCOMPLETE);
                } else {
                    if (dbItemReportTrigger.haveError()) {
                        state.setSeverity(2);
                        state.set_text(OrderHistoryStateText.FAILED);
                    } else {
                        state.setSeverity(0);
                        state.set_text(OrderHistoryStateText.SUCCESSFUL);
                    }

                }
                history.setState(state);
                history.setSurveyDate(dbItemReportTrigger.getCreated());

                if (ordersFilter.getRegex() != null && !ordersFilter.getRegex().isEmpty()) {
                    Matcher regExMatcher = Pattern.compile(ordersFilter.getRegex()).matcher(dbItemReportTrigger.getParentName() + "," + dbItemReportTrigger.getName());
                    add = regExMatcher.find();
                }

                if (add) {
                    listHistory.add(history);
                }

            }

            OrderHistory entity = new OrderHistory();
            entity.setDeliveryDate(new Date());
            entity.setHistory(listHistory);

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.rollback();
        }
    }

}
