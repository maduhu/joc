package com.sos.auth.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.TimeZone;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;

import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionCommands;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

public class SOSPermissionsCreator {

	private SOSShiroCurrentUser currentUser;
	private SOSPermissionRoles roles;
	private Ini ini;

	public SOSPermissionsCreator(SOSShiroCurrentUser currentUser) {
		super();
		this.currentUser = currentUser;
	}

	public void loginFromAccessToken(String accessToken) throws JocException, SOSHibernateException {

		if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null
				|| Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(accessToken) == null) {
			Globals.sosShiroProperties = new JocCockpitProperties();
			Globals.setProperties();
		
			SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("JOC: loginFromAccessToken");
			SOSShiroIniShare sosShiroIniShare = new SOSShiroIniShare(sosHibernateSession);
			try {
				sosShiroIniShare.provideIniFile();
			} catch (IOException e) {
				JocError err = new JocError();
	            err.setMessage(String.format("Error: %s", e.getMessage()));
	            JocException ee = new JocException(err);
	            throw ee;
			}
			sosHibernateSession.close();
			
			IniSecurityManagerFactory factory = Globals.getShiroIniSecurityManagerFactory();
			SecurityManager securityManager = factory.getInstance();
			SecurityUtils.setSecurityManager(securityManager);

			Subject subject = new Subject.Builder().sessionId(accessToken).buildSubject();
			if (subject.isAuthenticated()) {
				TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
				currentUser = new SOSShiroCurrentUser(
						(String) subject.getPrincipals().getPrimaryPrincipal(), "", "");

				if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
					Globals.jocWebserviceDataContainer.setCurrentUsersList(new SOSShiroCurrentUsersList());
				}
				Globals.jocWebserviceDataContainer.getCurrentUsersList().removeTimedOutUser(currentUser.getUsername());

				currentUser.setCurrentSubject(subject);
				currentUser.setAccessToken(accessToken);

				SOSPermissionJocCockpit sosPermissionJocCockpit = createJocCockpitPermissionObject(accessToken, "", "");
				currentUser.setSosPermissionJocCockpit(sosPermissionJocCockpit);
				currentUser.initFolders();

				Section section = getIni().getSection("folders");
				if (section != null) {
					for (String role : section.keySet()) {
						currentUser.addFolder(role, section.get(role));
					}
				}

				SOSPermissionCommands sosPermissionCommands = createCommandsPermissionObject(accessToken, "", "");
				currentUser.setSosPermissionCommands(sosPermissionCommands);
				Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);
			}
		}
	}

	protected SOSPermissionJocCockpit getSosPermissionJocCockpit() {

		ObjectFactory o = new ObjectFactory();

		SOSPermissionJocCockpit sosPermissionJocCockpit = o.createSOSPermissionJocCockpit();

		if (currentUser != null && currentUser.getCurrentSubject() != null) {

			sosPermissionJocCockpit.setIsAuthenticated(currentUser.isAuthenticated());
			sosPermissionJocCockpit.setAccessToken(currentUser.getAccessToken());
			sosPermissionJocCockpit.setUser(currentUser.getUsername());

			sosPermissionJocCockpit.setJobschedulerMaster(o.createSOSPermissionJocCockpitJobschedulerMaster());
			sosPermissionJocCockpit
					.setJobschedulerMasterCluster(o.createSOSPermissionJocCockpitJobschedulerMasterCluster());
			sosPermissionJocCockpit
					.setJobschedulerUniversalAgent(o.createSOSPermissionJocCockpitJobschedulerUniversalAgent());
			sosPermissionJocCockpit.setDailyPlan(o.createSOSPermissionJocCockpitDailyPlan());
			sosPermissionJocCockpit.setHistory(o.createSOSPermissionJocCockpitHistory());
			sosPermissionJocCockpit.setOrder(o.createSOSPermissionJocCockpitOrder());
			sosPermissionJocCockpit.setJobChain(o.createSOSPermissionJocCockpitJobChain());
			sosPermissionJocCockpit.setJob(o.createSOSPermissionJocCockpitJob());
			sosPermissionJocCockpit.setProcessClass(o.createSOSPermissionJocCockpitProcessClass());
			sosPermissionJocCockpit.setSchedule(o.createSOSPermissionJocCockpitSchedule());
			sosPermissionJocCockpit.setLock(o.createSOSPermissionJocCockpitLock());
			sosPermissionJocCockpit.setEvent(o.createSOSPermissionJocCockpitEvent());
			sosPermissionJocCockpit.setHolidayCalendar(o.createSOSPermissionJocCockpitHolidayCalendar());
			sosPermissionJocCockpit.setAuditLog(o.createSOSPermissionJocCockpitAuditLog());
			sosPermissionJocCockpit.setMaintenanceWindow(o.createSOSPermissionJocCockpitMaintenanceWindow());
			sosPermissionJocCockpit.setYADE(o.createSOSPermissionJocCockpitYADE());
			sosPermissionJocCockpit.setRuntime(o.createSOSPermissionJocCockpitRuntime());
			sosPermissionJocCockpit.getRuntime().setExecute(o.createSOSPermissionJocCockpitRuntimeExecute());

			sosPermissionJocCockpit.setCalendar(o.createSOSPermissionJocCockpitCalendar());
			sosPermissionJocCockpit.getCalendar().setView(o.createSOSPermissionJocCockpitCalendarView());
			sosPermissionJocCockpit.getCalendar().setEdit(o.createSOSPermissionJocCockpitCalendarEdit());
			sosPermissionJocCockpit.getCalendar().getEdit()
					.setAssign(o.createSOSPermissionJocCockpitCalendarEditAssign());

			sosPermissionJocCockpit.setJOCConfigurations(o.createSOSPermissionJocCockpitJOCConfigurations());
			sosPermissionJocCockpit.getJOCConfigurations()
					.setShare(o.createSOSPermissionJocCockpitJOCConfigurationsShare());
			sosPermissionJocCockpit.getJOCConfigurations().getShare()
					.setView(o.createSOSPermissionJocCockpitJOCConfigurationsShareView());
			sosPermissionJocCockpit.getJOCConfigurations().getShare()
					.setChange(o.createSOSPermissionJocCockpitJOCConfigurationsShareChange());
			sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange()
					.setSharedStatus(o.createSOSPermissionJocCockpitJOCConfigurationsShareChangeSharedStatus());

			sosPermissionJocCockpit.getJobschedulerMaster()
					.setView(o.createSOSPermissionJocCockpitJobschedulerMasterView());
			sosPermissionJocCockpit.getJobschedulerMaster()
					.setAdministration(o.createSOSPermissionJocCockpitJobschedulerMasterAdministration());
			sosPermissionJocCockpit.getJobschedulerMaster()
					.setExecute(o.createSOSPermissionJocCockpitJobschedulerMasterExecute());
			sosPermissionJocCockpit.getJobschedulerMaster().getExecute()
					.setRestart(o.createSOSPermissionJocCockpitJobschedulerMasterExecuteRestart());

			sosPermissionJocCockpit.getJobschedulerMasterCluster()
					.setView(o.createSOSPermissionJocCockpitJobschedulerMasterClusterView());
			sosPermissionJocCockpit.getJobschedulerMasterCluster()
					.setExecute(o.createSOSPermissionJocCockpitJobschedulerMasterClusterExecute());

			sosPermissionJocCockpit.getJobschedulerUniversalAgent()
					.setView(o.createSOSPermissionJocCockpitJobschedulerUniversalAgentView());
			sosPermissionJocCockpit.getJobschedulerUniversalAgent()
					.setExecute(o.createSOSPermissionJocCockpitJobschedulerUniversalAgentExecute());
			sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute()
					.setRestart(o.createSOSPermissionJocCockpitJobschedulerUniversalAgentExecuteRestart());

			sosPermissionJocCockpit.getDailyPlan().setView(o.createSOSPermissionJocCockpitDailyPlanView());
			sosPermissionJocCockpit.getOrder().setView(o.createSOSPermissionJocCockpitOrderView());
			sosPermissionJocCockpit.getOrder().setChange(o.createSOSPermissionJocCockpitOrderChange());
			sosPermissionJocCockpit.getOrder().setDelete(o.createSOSPermissionJocCockpitOrderDelete());
			sosPermissionJocCockpit.getOrder().setExecute(o.createSOSPermissionJocCockpitOrderExecute());

			sosPermissionJocCockpit.getJobChain().setView(o.createSOSPermissionJocCockpitJobChainView());
			sosPermissionJocCockpit.getJobChain().setChange(o.createSOSPermissionJocCockpitJobChainChange());
			sosPermissionJocCockpit.getJobChain().setExecute(o.createSOSPermissionJocCockpitJobChainExecute());

			sosPermissionJocCockpit.getJob().setView(o.createSOSPermissionJocCockpitJobView());
			sosPermissionJocCockpit.getJob().setChange(o.createSOSPermissionJocCockpitJobChange());
			sosPermissionJocCockpit.getJob().setExecute(o.createSOSPermissionJocCockpitJobExecute());

			sosPermissionJocCockpit.getProcessClass().setView(o.createSOSPermissionJocCockpitProcessClassView());
			sosPermissionJocCockpit.getProcessClass().setChange(o.createSOSPermissionJocCockpitProcessClassChange());

			sosPermissionJocCockpit.getSchedule().setView(o.createSOSPermissionJocCockpitScheduleView());
			sosPermissionJocCockpit.getSchedule().setChange(o.createSOSPermissionJocCockpitScheduleChange());

			sosPermissionJocCockpit.getLock().setView(o.createSOSPermissionJocCockpitLockView());
			sosPermissionJocCockpit.getLock().setChange(o.createSOSPermissionJocCockpitLockChange());

			sosPermissionJocCockpit.getEvent().setView(o.createSOSPermissionJocCockpitEventView());
			sosPermissionJocCockpit.getEvent().setExecute(o.createSOSPermissionJocCockpitEventExecute());

			sosPermissionJocCockpit.getHolidayCalendar().setView(o.createSOSPermissionJocCockpitHolidayCalendarView());
			sosPermissionJocCockpit.getAuditLog().setView(o.createSOSPermissionJocCockpitAuditLogView());
			sosPermissionJocCockpit.getMaintenanceWindow()
					.setView(o.createSOSPermissionJocCockpitMaintenanceWindowView());
			sosPermissionJocCockpit.getHistory().setView(o.createSOSPermissionJocCockpitHistoryView());

			sosPermissionJocCockpit.getYADE().setView(o.createSOSPermissionJocCockpitYADEView());
			sosPermissionJocCockpit.getYADE().setExecute(o.createSOSPermissionJocCockpitYADEExecute());

			sosPermissionJocCockpit.getJobschedulerMaster().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:jobscheduler_master:view:status"));
			sosPermissionJocCockpit.getJobschedulerMaster().getView()
					.setMainlog(haveRight("sos:products:joc_cockpit:jobscheduler_master:view:mainlog"));
			sosPermissionJocCockpit.getJobschedulerMaster().getView()
					.setParameter(haveRight("sos:products:joc_cockpit:jobscheduler_master:view:parameter"));
			sosPermissionJocCockpit.getJobschedulerMaster().getExecute().getRestart()
					.setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_master:execute:restart:terminate"));
			sosPermissionJocCockpit.getJobschedulerMaster().getExecute().getRestart()
					.setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_master:execute:restart:abort"));
			sosPermissionJocCockpit.getJobschedulerMaster().getExecute()
					.setPause(haveRight("sos:products:joc_cockpit:jobscheduler_master:execute:pause"));
			sosPermissionJocCockpit.getJobschedulerMaster().getExecute()
					.setContinue(haveRight("sos:products:joc_cockpit:jobscheduler_master:execute:continue"));
			sosPermissionJocCockpit.getJobschedulerMaster().getExecute()
					.setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_master:execute:terminate"));
			sosPermissionJocCockpit.getJobschedulerMaster().getExecute()
					.setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_master:execute:abort"));
			sosPermissionJocCockpit.getJobschedulerMaster().getAdministration().setManageCategories(
					haveRight("sos:products:joc_cockpit:jobscheduler_master:administration:manage_categories"));
			sosPermissionJocCockpit.getJobschedulerMaster().getAdministration().setEditPermissions(
					haveRight("sos:products:joc_cockpit:jobscheduler_master:administration:edit_permissions"));
			sosPermissionJocCockpit.getJobschedulerMaster().getAdministration().setRemoveOldInstances(
					haveRight("sos:products:joc_cockpit:jobscheduler_master:administration:remove_old_instances"));

			sosPermissionJocCockpit.getJobschedulerMasterCluster().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:view:status"));
			sosPermissionJocCockpit.getJobschedulerMasterCluster().getExecute().setTerminateFailSafe(
					haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:execute:terminate_fail_safe"));
			sosPermissionJocCockpit.getJobschedulerMasterCluster().getExecute()
					.setRestart(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:execute:restart"));
			sosPermissionJocCockpit.getJobschedulerMasterCluster().getExecute()
					.setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:execute:terminate"));

			sosPermissionJocCockpit.getJobschedulerUniversalAgent().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:view:status"));
			sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute().getRestart()
					.setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:execute:restart:abort"));
			sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute().getRestart().setTerminate(
					haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:execute:restart:terminate"));
			sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute()
					.setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:execute:abort"));
			sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute()
					.setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:execute:terminate"));

			sosPermissionJocCockpit.getDailyPlan().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:daily_plan:view:status"));

			sosPermissionJocCockpit.getHistory().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:history:view:status"));

			sosPermissionJocCockpit.getOrder().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:order:view:status"));
			sosPermissionJocCockpit.getOrder().getView()
					.setConfiguration(haveRight("sos:products:joc_cockpit:order:view:configuration"));
			sosPermissionJocCockpit.getOrder().getView()
					.setOrderLog(haveRight("sos:products:joc_cockpit:order:view:order_log"));
			sosPermissionJocCockpit.getOrder().getChange()
					.setStartAndEndNode(haveRight("sos:products:joc_cockpit:order:change:start_and_end_node"));
			sosPermissionJocCockpit.getOrder().getChange()
					.setTimeForAdhocOrder(haveRight("sos:products:joc_cockpit:order:change:time_for_adhoc_orders"));
			sosPermissionJocCockpit.getOrder().getChange()
					.setParameter(haveRight("sos:products:joc_cockpit:order:change:parameter"));
			sosPermissionJocCockpit.getOrder().getChange()
					.setRunTime(haveRight("sos:products:joc_cockpit:order:change:run_time"));
			sosPermissionJocCockpit.getOrder().getChange()
					.setState(haveRight("sos:products:joc_cockpit:order:change:state"));
			sosPermissionJocCockpit.getOrder().getChange()
					.setHotFolder(haveRight("sos:products:joc_cockpit:order:change:hot_folder"));
			sosPermissionJocCockpit.getOrder().getExecute()
					.setStart(haveRight("sos:products:joc_cockpit:order:execute:start"));
			sosPermissionJocCockpit.getOrder().getExecute()
					.setUpdate(haveRight("sos:products:joc_cockpit:order:execute:update"));
			sosPermissionJocCockpit.getOrder().getExecute()
					.setSuspend(haveRight("sos:products:joc_cockpit:order:execute:suspend"));
			sosPermissionJocCockpit.getOrder().getExecute()
					.setResume(haveRight("sos:products:joc_cockpit:order:execute:resume"));
			sosPermissionJocCockpit.getOrder().getExecute()
					.setReset(haveRight("sos:products:joc_cockpit:order::execute:reset"));
			sosPermissionJocCockpit.getOrder().getExecute()
					.setRemoveSetback(haveRight("sos:products:joc_cockpit:order:execute:remove_setback"));
			sosPermissionJocCockpit.getOrder().getDelete()
					.setPermanent(haveRight("sos:products:joc_cockpit:order:delete:permanent"));
			sosPermissionJocCockpit.getOrder().getDelete()
					.setTemporary(haveRight("sos:products:joc_cockpit:order:delete:temporary"));

			sosPermissionJocCockpit.getJobChain().getView()
					.setConfiguration(haveRight("sos:products:joc_cockpit:job_chain:view:configuration"));
			sosPermissionJocCockpit.getJobChain().getView()
					.setHistory(haveRight("sos:products:joc_cockpit:job_chain:view:history"));
			sosPermissionJocCockpit.getJobChain().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:job_chain:view:status"));
			sosPermissionJocCockpit.getJobChain().getExecute()
					.setStop(haveRight("sos:products:joc_cockpit:job_chain:execute:stop"));
			sosPermissionJocCockpit.getJobChain().getExecute()
					.setUnstop(haveRight("sos:products:joc_cockpit:job_chain:execute:unstop"));
			sosPermissionJocCockpit.getJobChain().getExecute()
					.setAddOrder(haveRight("sos:products:joc_cockpit:job_chain:execute:add_order"));
			sosPermissionJocCockpit.getJobChain().getExecute()
					.setSkipJobChainNode(haveRight("sos:products:joc_cockpit:job_chain:execute:skip_jobchain_node"));
			sosPermissionJocCockpit.getJobChain().getExecute().setProcessJobChainNode(
					haveRight("sos:products:joc_cockpit:job_chain:execute:process_jobchain_node"));
			sosPermissionJocCockpit.getJobChain().getExecute()
					.setStopJobChainNode(haveRight("sos:products:joc_cockpit:job_chain:execute:stop_jobchain_node"));

			sosPermissionJocCockpit.getJob().getView().setStatus(haveRight("sos:products:joc_cockpit:job:view:status"));
			sosPermissionJocCockpit.getJob().getView()
					.setTaskLog(haveRight("sos:products:joc_cockpit:job:view:task_log"));
			sosPermissionJocCockpit.getJob().getView()
					.setConfiguration(haveRight("sos:products:joc_cockpit:job:view:configuration"));
			sosPermissionJocCockpit.getJob().getView()
					.setHistory(haveRight("sos:products:joc_cockpit:job:view:history"));
			sosPermissionJocCockpit.getJob().getChange()
					.setRunTime(haveRight("sos:products:joc_cockpit:job:change:run_time"));
			sosPermissionJocCockpit.getJob().getExecute()
					.setStart(haveRight("sos:products:joc_cockpit:job:execute:start"));
			sosPermissionJocCockpit.getJob().getExecute()
					.setStop(haveRight("sos:products:joc_cockpit:job:execute:stop"));
			sosPermissionJocCockpit.getJob().getExecute()
					.setUnstop(haveRight("sos:products:joc_cockpit:job:execute:unstop"));
			sosPermissionJocCockpit.getJob().getExecute()
					.setTerminate(haveRight("sos:products:joc_cockpit:job:execute:terminate"));
			sosPermissionJocCockpit.getJob().getExecute()
					.setKill(haveRight("sos:products:joc_cockpit:job:execute:kill"));
			sosPermissionJocCockpit.getJob().getExecute()
					.setEndAllTasks(haveRight("sos:products:joc_cockpit:job:execute:end_all_tasks"));
			sosPermissionJocCockpit.getJob().getExecute()
					.setSuspendAllTasks(haveRight("sos:products:joc_cockpit:job:execute:suspend_all_tasks"));
			sosPermissionJocCockpit.getJob().getExecute()
					.setContinueAllTasks(haveRight("sos:products:joc_cockpit:job:execute:continue_all_tasks"));

			sosPermissionJocCockpit.getProcessClass().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:process_class:view:status"));
			sosPermissionJocCockpit.getProcessClass().getView()
					.setConfiguration(haveRight("sos:products:joc_cockpit:process_class:view:configuration"));

			sosPermissionJocCockpit.getSchedule().getView()
					.setConfiguration(haveRight("sos:products:joc_cockpit:schedule:view:configuration"));
			sosPermissionJocCockpit.getSchedule().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:schedule:view:status"));
			sosPermissionJocCockpit.getSchedule().getChange()
					.setEditContent(haveRight("sos:products:joc_cockpit:schedule:change:edit_content"));
			sosPermissionJocCockpit.getSchedule().getChange()
					.setAddSubstitute(haveRight("sos:products:joc_cockpit:schedule:change:add_substitute"));

			sosPermissionJocCockpit.getLock().getView()
					.setConfiguration(haveRight("sos:products:joc_cockpit:lock:view:configuration"));
			sosPermissionJocCockpit.getLock().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:lock:view:status"));

			sosPermissionJocCockpit.getEvent().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:event:view:status"));
			sosPermissionJocCockpit.getEvent().getExecute()
					.setDelete(haveRight("sos:products:joc_cockpit:event:execute:delete"));
			sosPermissionJocCockpit.getEvent().getExecute()
					.setAdd(haveRight("sos:products:joc_cockpit:event:execute:add"));

			sosPermissionJocCockpit.getHolidayCalendar().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:holiday_calendar:view:status"));
			sosPermissionJocCockpit.getAuditLog().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:audit_log:view:status"));

			sosPermissionJocCockpit.getJOCConfigurations().getShare().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:customization:share:view:status"));
			sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange()
					.setDelete(haveRight("sos:products:joc_cockpit:customization:share:change:delete"));
			sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange()
					.setEditContent(haveRight("sos:products:joc_cockpit:customization:share:change:edit_content"));
			sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().getSharedStatus().setMakePrivate(
					haveRight("sos:products:joc_cockpit:customization:share:change:shared_status:make_private"));
			sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().getSharedStatus().setMakeShared(
					haveRight("sos:products:joc_cockpit:customization:share:change:shared_status:make_share"));

			sosPermissionJocCockpit.getMaintenanceWindow().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:maintenance_window:view:status"));
			sosPermissionJocCockpit.getMaintenanceWindow().setEnableDisableMaintenanceWindow(
					haveRight("sos:products:joc_cockpit:maintenance_window:enable_disable_maintenance_window"));

			sosPermissionJocCockpit.getYADE().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:yade:view:status"));
			sosPermissionJocCockpit.getYADE().getView()
					.setTransfers(haveRight("sos:products:joc_cockpit:yade:view:transfers"));
			sosPermissionJocCockpit.getYADE().getView().setFiles(haveRight("sos:products:joc_cockpit:yade:view:files"));
			sosPermissionJocCockpit.getYADE().getExecute()
					.setTransferStart(haveRight("sos:products:joc_cockpit:yade:execute:transfer_start"));

			sosPermissionJocCockpit.getCalendar().getView()
					.setStatus(haveRight("sos:products:joc_cockpit:calendar:view:status"));

			sosPermissionJocCockpit.getCalendar().getEdit()
					.setChange(haveRight("sos:products:joc_cockpit:calendar:edit:change"));
			sosPermissionJocCockpit.getCalendar().getEdit()
					.setDelete(haveRight("sos:products:joc_cockpit:calendar:edit:delete"));
			sosPermissionJocCockpit.getCalendar().getEdit()
					.setCreate(haveRight("sos:products:joc_cockpit:calendar:edit:create"));

			sosPermissionJocCockpit.getCalendar().getEdit().getAssign()
					.setChange(haveRight("sos:products:joc_cockpit:calendar:assign:change"));
			sosPermissionJocCockpit.getCalendar().getEdit().getAssign()
					.setNonworking(haveRight("sos:products:joc_cockpit:calendar:assign:nonworking"));
			sosPermissionJocCockpit.getCalendar().getEdit().getAssign()
					.setRuntime(haveRight("sos:products:joc_cockpit:calendar:assign:runtime"));

			sosPermissionJocCockpit.getRuntime().getExecute()
					.setEditXml(haveRight("sos:products:joc_cockpit:runtime:execute:edit_xml"));

		}
		return sosPermissionJocCockpit;
	}

	protected SOSPermissionCommands getSosPermissionCommands() {

		ObjectFactory o = new ObjectFactory();

		SOSPermissionCommands sosPermissionCommands = o.createSOSPermissionCommands();

		if (currentUser != null && currentUser.getCurrentSubject() != null) {

			sosPermissionCommands.setIsAuthenticated(currentUser.isAuthenticated());
			sosPermissionCommands.setAccessToken(currentUser.getAccessToken());
			sosPermissionCommands.setUser(currentUser.getUsername());

			sosPermissionCommands.setDailyPlan(o.createSOSPermissionCommandsDailyPlan());
			sosPermissionCommands.getDailyPlan().setView(o.createSOSPermissionCommandsDailyPlanView());
			sosPermissionCommands.setJobschedulerMaster(o.createSOSPermissionCommandsJobschedulerMaster());
			sosPermissionCommands
					.setJobschedulerMasterCluster(o.createSOSPermissionCommandsJobschedulerMasterCluster());
			sosPermissionCommands.setHistory(o.createSOSPermissionCommandsHistory());
			sosPermissionCommands.setOrder(o.createSOSPermissionCommandsOrder());
			sosPermissionCommands.setJobChain(o.createSOSPermissionCommandsJobChain());
			sosPermissionCommands.setJob(o.createSOSPermissionCommandsJob());
			sosPermissionCommands.setProcessClass(o.createSOSPermissionCommandsProcessClass());
			sosPermissionCommands.setSchedule(o.createSOSPermissionCommandsSchedule());
			sosPermissionCommands.setLock(o.createSOSPermissionCommandsLock());

			sosPermissionCommands.getJobschedulerMaster()
					.setView(o.createSOSPermissionCommandsJobschedulerMasterView());
			sosPermissionCommands.getJobschedulerMaster()
					.setExecute(o.createSOSPermissionCommandsJobschedulerMasterExecute());
			sosPermissionCommands.getJobschedulerMaster()
					.setAdministration(o.createSOSPermissionCommandsJobschedulerMasterAdministration());
			sosPermissionCommands.getJobschedulerMaster().getExecute()
					.setRestart(o.createSOSPermissionCommandsJobschedulerMasterExecuteRestart());

			sosPermissionCommands.getJobschedulerMasterCluster()
					.setExecute(o.createSOSPermissionCommandsJobschedulerMasterClusterExecute());

			sosPermissionCommands.getOrder().setView(o.createSOSPermissionCommandsOrderView());
			sosPermissionCommands.getOrder().setChange(o.createSOSPermissionCommandsOrderChange());
			sosPermissionCommands.getOrder().setExecute(o.createSOSPermissionCommandsOrderExecute());

			sosPermissionCommands.getJobChain().setView(o.createSOSPermissionCommandsJobChainView());
			sosPermissionCommands.getJobChain().setExecute(o.createSOSPermissionCommandsJobChainExecute());
			sosPermissionCommands.getJobChain().setChange(o.createSOSPermissionCommandsJobChainChange());

			sosPermissionCommands.getJob().setView(o.createSOSPermissionCommandsJobView());
			sosPermissionCommands.getJob().setExecute(o.createSOSPermissionCommandsJobExecute());
			sosPermissionCommands.getJob().setChange(o.createSOSPermissionCommandsJobChange());

			sosPermissionCommands.getProcessClass().setView(o.createSOSPermissionCommandsProcessClassView());
			sosPermissionCommands.getProcessClass().setChange(o.createSOSPermissionCommandsProcessClassChange());

			sosPermissionCommands.getSchedule().setView(o.createSOSPermissionCommandsScheduleView());
			sosPermissionCommands.getSchedule().setChange(o.createSOSPermissionCommandsScheduleChange());

			sosPermissionCommands.getLock().setView(o.createSOSPermissionCommandsLockView());
			sosPermissionCommands.getLock().setChange(o.createSOSPermissionCommandsLockChange());

			sosPermissionCommands.getJobschedulerMaster().getView()
					.setStatus(haveRight("sos:products:commands:jobscheduler_master:view:status"));
			sosPermissionCommands.getJobschedulerMaster().getView()
					.setParameter(haveRight("sos:products:commands:jobscheduler_master:view:parameter"));
			sosPermissionCommands.getJobschedulerMaster().getExecute().getRestart()
					.setAbort(haveRight("sos:products:commands:jobscheduler_master:execute:restart:terminate"));
			sosPermissionCommands.getJobschedulerMaster().getExecute().getRestart()
					.setTerminate(haveRight("sos:products:commands:jobscheduler_master:execute:restart:abort"));
			sosPermissionCommands.getJobschedulerMaster().getExecute()
					.setPause(haveRight("sos:products:commands:jobscheduler_master:execute:pause"));
			sosPermissionCommands.getJobschedulerMaster().getExecute()
					.setContinue(haveRight("sos:products:commands:jobscheduler_master:execute:continue"));
			sosPermissionCommands.getJobschedulerMaster().getExecute()
					.setTerminate(haveRight("sos:products:commands:jobscheduler_master:execute:terminate"));
			sosPermissionCommands.getJobschedulerMaster().getExecute()
					.setAbort(haveRight("sos:products:commands:jobscheduler_master:execute:abort"));
			sosPermissionCommands.getJobschedulerMaster().getExecute()
					.setStop(haveRight("sos:products:commands:jobscheduler_master:execute:stop"));
			sosPermissionCommands.getJobschedulerMaster().getAdministration()
					.setManageCategories(haveRight("sos:products:commands:jobscheduler_master:manage_categories"));

			sosPermissionCommands.getJobschedulerMasterCluster().getExecute().setTerminateFailSafe(
					haveRight("sos:products:commands:jobscheduler_master_cluster:execute:terminate_fail_safe"));
			sosPermissionCommands.getJobschedulerMasterCluster().getExecute()
					.setRestart(haveRight("sos:products:commands:jobscheduler_master_cluster:execute:restart"));
			sosPermissionCommands.getJobschedulerMasterCluster().getExecute()
					.setTerminate(haveRight("sos:products:commands:jobscheduler_master_cluster:execute:terminate"));

			sosPermissionCommands.getDailyPlan().getView()
					.setStatus(haveRight("sos:products:commands:jobscheduler_master:view:calendar"));

			sosPermissionCommands.getHistory().setView(haveRight("sos:products:commands:history:view"));

			sosPermissionCommands.getOrder().getView().setStatus(haveRight("sos:products:commands:order:view:status"));
			sosPermissionCommands.getOrder().getChange()
					.setStartAndEndNode(haveRight("sos:products:commands:order:change:start_and_end_node"));
			sosPermissionCommands.getOrder().getChange()
					.setTimeForAdhocOrder(haveRight("sos:products:commands:order:change:time_for_adhoc_orders"));
			sosPermissionCommands.getOrder().getChange()
					.setParameter(haveRight("sos:products:commands:order:change:parameter"));
			sosPermissionCommands.getOrder().getChange()
					.setOther(haveRight("sos:products:joc_cockpit:order:change:other"));
			sosPermissionCommands.getOrder().getChange()
					.setRunTime(haveRight("sos:products:commands:order:change:run_time"));
			sosPermissionCommands.getOrder().getChange()
					.setState(haveRight("sos:products:commands:order:change:state"));
			sosPermissionCommands.getOrder().getChange()
					.setHotFolder(haveRight("sos:products:commands:order:change:hot_folder"));
			sosPermissionCommands.getOrder().getExecute()
					.setStart(haveRight("sos:products:commands:order:execute:start"));
			sosPermissionCommands.getOrder().getExecute()
					.setUpdate(haveRight("sos:products:commands:order:execute:update"));
			sosPermissionCommands.getOrder().getExecute()
					.setSuspend(haveRight("sos:products:commands:order:execute:suspend"));
			sosPermissionCommands.getOrder().getExecute()
					.setResume(haveRight("sos:products:commands:order:execute:resume"));
			sosPermissionCommands.getOrder().getExecute()
					.setReset(haveRight("sos:products:commands:order::execute:reset"));
			sosPermissionCommands.getOrder().getExecute()
					.setRemoveSetback(haveRight("sos:products:commands:order:execute:remove_setback"));
			sosPermissionCommands.getOrder().setDelete(haveRight("sos:products:commands:order:delete"));

			sosPermissionCommands.getJobChain().getView()
					.setStatus(haveRight("sos:products:commands:job_chain:view:status"));
			sosPermissionCommands.getJobChain().getExecute()
					.setStop(haveRight("sos:products:commands:job_chain:execute:stop"));
			sosPermissionCommands.getJobChain().getExecute()
					.setUnstop(haveRight("sos:products:commands:job_chain:execute:unstop"));
			sosPermissionCommands.getJobChain().getExecute()
					.setAddOrder(haveRight("sos:products:commands:job_chain:execute:add_order"));
			sosPermissionCommands.getJobChain().getExecute()
					.setSkipJobChainNode(haveRight("sos:products:commands:job_chain:execute:skip_jobchain_node"));
			sosPermissionCommands.getJobChain().getExecute()
					.setProcessJobChainNode(haveRight("sos:products:commands:job_chain:execute:process_jobchain_node"));
			sosPermissionCommands.getJobChain().getExecute()
					.setStopJobChainNode(haveRight("sos:products:commands:job_chain:execute:stop_jobchain_node"));
			sosPermissionCommands.getJobChain().getExecute()
					.setRemove(haveRight("sos:products:commands:job_chain:remove"));
			sosPermissionCommands.getJobChain().getChange()
					.setHotFolder(haveRight("sos:products:commands:job_chain:change:hot_folder"));

			sosPermissionCommands.getJob().getView().setStatus(haveRight("sos:products:commands:job:view:status"));
			sosPermissionCommands.getJob().getChange()
					.setRunTime(haveRight("sos:products:commands:job:change:run_time"));
			sosPermissionCommands.getJob().getChange()
					.setHotFolder(haveRight("sos:products:commands:job:change:hot_folder"));
			sosPermissionCommands.getJob().getExecute().setStart(haveRight("sos:products:commands:job:execute:start"));
			sosPermissionCommands.getJob().getExecute().setStop(haveRight("sos:products:commands:job:execute:stop"));
			sosPermissionCommands.getJob().getExecute()
					.setUnstop(haveRight("sos:products:commands:job:execute:unstop"));
			sosPermissionCommands.getJob().getExecute()
					.setTerminate(haveRight("sos:products:commands:job:execute:terminate"));
			sosPermissionCommands.getJob().getExecute().setKill(haveRight("sos:products:commands:job:execute:kill"));
			sosPermissionCommands.getJob().getExecute()
					.setEndAllTasks(haveRight("sos:products:commands:job:execute:end_all_tasks"));
			sosPermissionCommands.getJob().getExecute()
					.setSuspendAllTasks(haveRight("sos:products:commands:job:execute:suspend_all_tasks"));
			sosPermissionCommands.getJob().getExecute()
					.setContinueAllTasks(haveRight("sos:products:commands:job:execute:continue_all_tasks"));

			sosPermissionCommands.getProcessClass().getView()
					.setStatus(haveRight("sos:products:commands:process_class:view:status"));
			sosPermissionCommands.getProcessClass().setRemove(haveRight("sos:products:commands:process_class:remove"));
			sosPermissionCommands.getProcessClass().getChange()
					.setEditContent(haveRight("sos:products:commands:process_class:change:edit_content"));
			sosPermissionCommands.getProcessClass().getChange()
					.setHotFolder(haveRight("sos:products:commands:process_class:change:hot_folder"));

			sosPermissionCommands.getSchedule().getView()
					.setStatus(haveRight("sos:products:commands:schedule:view:status"));
			sosPermissionCommands.getSchedule().getChange()
					.setAddSubstitute(haveRight("sos:products:commands:schedule:change:add_substitute"));
			sosPermissionCommands.getSchedule().getChange()
					.setHotFolder(haveRight("sos:products:commands:schedule:change:hot_folder"));

			sosPermissionCommands.getLock().getView().setStatus(haveRight("sos:products:commands:lock:view:status"));
			sosPermissionCommands.getLock().setRemove(haveRight("sos:products:commands:lock:remove"));
			sosPermissionCommands.getLock().getChange()
					.setHotFolder(haveRight("sos:products:commands:lock:change:hot_folder"));
		}
		return sosPermissionCommands;
	}

	public SOSPermissionJocCockpit createJocCockpitPermissionObject(String accessToken, String user, String pwd) {

		SOSPermissionJocCockpit sosPermissionJocCockpit = getSosPermissionJocCockpit();
		sosPermissionJocCockpit.setSOSPermissionRoles(getRoles(true));

		currentUser.setSosPermissionJocCockpit(sosPermissionJocCockpit);
		Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);
		return sosPermissionJocCockpit;
	}

	public SOSPermissionCommands createCommandsPermissionObject(String accessToken, String user, String pwd) {

		SOSPermissionJocCockpit sosPermissionJocCockpit = getSosPermissionJocCockpit();
		SOSPermissionCommands sosPermissionCommands = getSosPermissionCommands();
		sosPermissionJocCockpit.setSOSPermissionRoles(getRoles(true));

		currentUser.setSosPermissionJocCockpit(sosPermissionJocCockpit);
		currentUser.setSosPermissionCommands(sosPermissionCommands);
		Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);
		return sosPermissionCommands;
	}

	private boolean isPermitted(String permission) {
		return (currentUser != null && currentUser.isPermitted(permission) && currentUser.isAuthenticated());
	}

	private boolean haveRight(String permission) {
		return isPermitted(permission);
	}

	private void addRole(List<String> sosRoles, String role, boolean forUser) {
		if (currentUser != null && (!forUser || currentUser.hasRole(role)) && currentUser.isAuthenticated()) {
			if (!sosRoles.contains(role)) {
				sosRoles.add(role);
			}
		}
	}

	public SOSPermissionRoles getRoles(boolean forUser) {

		if (roles == null || !forUser) {
			ObjectFactory o = new ObjectFactory();
			roles = o.createSOSPermissionRoles();

			ini = getIni();
			Section s = ini.getSection("roles");

			if (s != null) {
				for (String role : s.keySet()) {
					addRole(roles.getSOSPermissionRole(), role, forUser);
				}
			}

			s = ini.getSection("folders");
			if (s != null) {
				for (String role : s.keySet()) {
					String[] key = role.split("\\|");
					if (key.length == 1) {
						addRole(roles.getSOSPermissionRole(), role, forUser);
					}
					if (key.length == 2) {
						addRole(roles.getSOSPermissionRole(), key[1], forUser);
					}
				}
			}
		}
		return roles;
	}

	public Ini getIni() {

		if (ini == null) {
			return Globals.getIniFromSecurityManagerFactory();
		}
		return ini;
	}

}