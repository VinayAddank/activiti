/**
 * 
 */
package org.rta.service;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.rta.model.Assignee;
import org.rta.model.ProcessUser;
import org.rta.model.RtaProcessInfo;
import org.rta.model.RtaTaskInfo;

/**
 * @author admin
 *
 */
public interface ActivitiService {
	/**
	 * create users to use activity
	 * 
	 * @param users
	 */
	public void createUsers(List<ProcessUser> users);

	/**
	 * Start a process with processId e.g. OTSaleProcess
	 * 
	 * @param userId
	 * @param processId
	 * @param serviceCode
	 * @return
	 */
	public RtaProcessInfo startProcess(String userId, String processId, Map<String, Object> variables);

	/**
	 * Get next task ids by processInstanceId
	 * 
	 * @param processInstanceId
	 * @return
	 */
	public List<RtaTaskInfo> getActiveTaskIds(String processInstanceId);

	/**
	 * get active process instances by task definition key
	 * 
	 * @param taskDefKey
	 * @return
	 */
	public List<RtaTaskInfo> getActiveInstances(String taskDefKey);
	
	/**
	 * Get tasks by userId and variables
	 * 
	 * @param userId
	 * @param variables
	 * @param Integer from optional
	 * @param Integer to optional
	 * @param Integer perPage optional
	 * @return
	 */
	public List<RtaTaskInfo> getTasks(String userId, Map<String, Object> variables, Integer from, Integer to, Integer perPage);
	
	/**
	 * get tasks that are aligned to provided user or usergroup of that user
	 * @param processInstanceId TODO
	 * @param assignee
	 * 
	 * @return
	 */
	public List<RtaTaskInfo> getTasks(String userId, String processInstanceId);

	/**
	 *  get assigned tasks
	 *  
	 * @param userId
	 * @param variables
	 * @return
	 */
	public List<RtaTaskInfo> getAssignedTasks(String userId, Map<String, Object> variables);
	
	/**
	 * get task details like start date, end date
	 * 
	 * @param instanceId
	 * @return
	 */
	public RtaTaskInfo getTaskDetails(String instanceId);

	/**
	 * claim the task
	 * 
	 * @param userId
	 * @param taskId
	 */
	public void claimTask(String assignee, String taskId);

	/**
	 * complete the task
	 * 
	 * @param userId
	 * @param taskId
	 * @param Map<String, Object> variables
	 */
	public void completeTask(String userId, String taskId, Map<String, Object> variables);

	/**
	 * complete the task with action e.g. APPROVED, REJECTED
	 * variable map will be like => ${taskDefKey} = ${action}
	 * 
	 * @param assignee
	 * @param taskId
	 * @param variables
	 */
	public void completeTaskWithAction(Assignee assignee, String taskId, Map<String, Object> variables);

	/**
	 * get task id by userid and task definition id
	 * @param userId
	 * @param taskDef
	 * @param processInstanceId TODO
	 * @return
	 */
	public String getTaskIdByUserIdTaskDefInstanceId(String userId, String taskDef, String processInstanceId);

	/**
	 * cancel a process instance
	 * 
	 * @param instanceId
	 */
	public void deleteInstance(String instanceId, String msg);
	
	/**
	 * get next completed task by task def key and process instance id
	 * 
	 * @param taskDefKey
	 * @param processInstanceId
	 * @return
	 */
	public RtaTaskInfo getNextCompletedTask(String taskDefKey, String processInstanceId, String userId);
	
	/**
     * Get All user tasks in process
     * 
     * @param processId
     * @return BpmnModel
     */
	public BpmnModel getAllUserTasksInProcess(String processId);

	/**
	 * Add Process variable
	 * 
	 * @param executionId
	 * @param variables
	 * @return
	 */
	public boolean setVaribaleToProcess(String executionId, Map<String, Object> variables);

}
