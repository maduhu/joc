Each audit log entry has the following fields.<br/>
* Required fields are
    * account
    * request (webservice url)
    * created (timestamp of the request)
    * comment
* Optional fields are
    * parameters (from post body of the request)
    * job (if the object is a job)
    * jobChain (if the object is a job chain or an order)
    * orderId (if the object is an order)
