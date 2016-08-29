package com.sos.joc.orders.impl;
 
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import com.sos.auth.rest.SOSServicePermissionShiro;
import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.common.OkSchema;
import com.sos.joc.orders.post.commands.modify.ModifyOrdersBody;
import com.sos.joc.orders.post.commands.modify.Order;
 
public class OrdersResourceOrderCommandsImplTest {
    private static final String LDAP_PASSWORD = "secret";
    private static final String LDAP_USER = "root";
     
    @Test
    public void postOrdersCommand() throws Exception   {
         
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer) sosServicePermissionShiro.loginGet("", LDAP_USER, LDAP_PASSWORD).getEntity();
        ModifyOrdersBody ordersBody = new ModifyOrdersBody();
        ArrayList<Order> orders = new ArrayList<Order>();
        Order order = new Order();
        order.setOrderId("junit_test");
        order.setJobChain("/test/job_chain1");
        orders.add(order);
     
        Order order2 = new Order();
        order2.setOrderId("test");
        order2.setState("100");
        order2.setJobChain("/test/job_chain1");
        orders.add(order2);

        ordersBody.setOrders(orders);
        ordersBody.setJobschedulerId("scheduler_current");
        OrdersResourceCommandModifyOrderImpl ordersResourceHistoryImpl = new OrdersResourceCommandModifyOrderImpl();
        JOCDefaultResponse ordersResponse = ordersResourceHistoryImpl.postOrdersSetState(sosShiroCurrentUserAnswer.getAccessToken(), ordersBody);
        OkSchema okSchema = (OkSchema) ordersResponse.getEntity();
        assertEquals("postOrdersCommand",true, okSchema.getOk());
     }

}

