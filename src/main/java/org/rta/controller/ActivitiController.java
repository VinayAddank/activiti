/**
 * Controller used for common activity for all citizen services
 */
package org.rta.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.rta.enums.Status;
import org.rta.model.Assignee;
import org.rta.model.ProcessUser;
import org.rta.model.ResponseModel;
import org.rta.model.RtaProcessInfo;
import org.rta.model.RtaTaskInfo;
import org.rta.model.VariableWrapper;
import org.rta.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
 *
 */

@RestController
public class ActivitiController {

	@Autowired
	private ActivitiService activitiService;

	/**
	 * Create users to use activiti
	 * 
	 * @param users
	 * @return
	 */
	@RequestMapping(value = "/users", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> createUser(@RequestBody List<ProcessUser> users) {
		activitiService.createUsers(users);
		ResponseModel<Object> response = new ResponseModel<>(ResponseModel.SUCCESS);
		return ResponseEntity.ok(response);
	}

	/**
	 * Create a process instance by process id.
	 * e.g. OTSaleProcess, OTAuctionProcess, OTDeathProcess
	 * 
	 * @param assignee
	 * @param processId
	 * @param serviceCode
	 * @return
	 */
	@RequestMapping(value = "/process/{processId}/user/{user_id}", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> startProcess(@RequestBody VariableWrapper wrapper, @PathVariable("processId") String processId,
	        @PathVariable("user_id") String userId) {
	    Map<String, Object> variables = wrapper.getVariables();
	    if(variables == null){
	        variables = new HashMap<>();
	    }
		RtaProcessInfo info = activitiService.startProcess(userId, processId, variables);
		ResponseModel<RtaProcessInfo> response = new ResponseModel<>(ResponseModel.SUCCESS, info);
		response.setActiveTasks(activitiService.getActiveTaskIds(info.getInstanceId()));
		return ResponseEntity.ok(response);
	}

	/**
	 * Get all active tasks definition key by instance id
	 * 
	 * @param instanceId
	 * @return
	 */
	@RequestMapping(value = "/task/active/{instanceId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getActiveTasks(@PathVariable("instanceId") String instanceId) {
		List<RtaTaskInfo> tasks = activitiService.getActiveTaskIds(instanceId);
		ResponseModel<List<RtaTaskInfo>> response = new ResponseModel<>(ResponseModel.SUCCESS, tasks);
		response.setActiveTasks(tasks);
		return ResponseEntity.ok(response);
	}

	/**
	 * Get active instances at a task def node
	 * 
	 * @param taskDef
	 * @return
	 */
	@RequestMapping(value = "/instances/task/{taskDef}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getActiveInstances(@PathVariable("taskDef") String taskDef) {
		List<RtaTaskInfo> tasks = activitiService.getActiveInstances(taskDef);
		ResponseModel<List<RtaTaskInfo>> response = new ResponseModel<>(ResponseModel.SUCCESS, tasks);
		response.setActiveTasks(tasks);
		return ResponseEntity.ok(response);
	}

	/**
	 *  Get task assigned or aligned to a user with variables
	 * @param variables
	 * @param userId
	 * @param from
	 * @param to
	 * @param perPage
	 * @return
	 */
	@RequestMapping(value = "/tasks/{userId}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getTasks(@RequestBody VariableWrapper wrapper, @PathVariable("userId") String userId,
            @RequestParam(name = "from", required = false) Integer from, @RequestParam(name = "to", required = false) Integer to,
            @RequestParam(name = "perPage", required = false) Integer perPage) {
	    Map<String, Object> variables = new HashMap<>();
	    if(wrapper != null){
	        variables = wrapper.getVariables();
	    }
        List<RtaTaskInfo> tasks = activitiService.getTasks(userId, variables, from, to, perPage);
        ResponseModel<List<RtaTaskInfo>> response = new ResponseModel<>(ResponseModel.SUCCESS, tasks);
        response.setActiveTasks(tasks);
        return ResponseEntity.ok(response);
    }
	
	/**
	 * Get tasks assigned to candidate or group by user id and instance id
	 * 
	 * @param userId
	 * @param processInstanceId
	 * @return
	 */
	@RequestMapping(value = "/tasks/{userId}/instance/{processInstanceId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getTasks(@PathVariable("userId") String userId, @PathVariable("processInstanceId") String processInstanceId) {
		List<RtaTaskInfo> tasks = activitiService.getTasks(userId, processInstanceId);
		ResponseModel<List<RtaTaskInfo>> response = new ResponseModel<>(ResponseModel.SUCCESS, tasks);
		response.setActiveTasks(tasks);
		return ResponseEntity.ok(response);
	}

	/**
	 * Get process details by instance id e.g. start date, end date
	 * 
	 * @param instanceId
	 * @return
	 */
	@RequestMapping(value = "/processdetails/{instanceId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getProcessDetails(@PathVariable("instanceId") String instanceId) {
		RtaTaskInfo task = activitiService.getTaskDetails(instanceId);
		ResponseModel<RtaTaskInfo> response = new ResponseModel<>(ResponseModel.SUCCESS, task);
		return ResponseEntity.ok(response);
	}

	/**
	 * Claim a task with instance id and task def key
	 * 
	 * @param assignee
	 * @param taskDef
	 * @param instanceId
	 * @return
	 */
	@RequestMapping(value = "/claim/instance/{instanceId}/task/{taskDef}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> claimTask(@RequestBody Assignee assignee, @PathVariable("taskDef") String taskDef,
			@PathVariable("instanceId") String instanceId) {
		String taskId = activitiService.getTaskIdByUserIdTaskDefInstanceId(assignee.getUserId(), taskDef, instanceId);
		if (taskId == null) {
			ResponseModel<Object> response = new ResponseModel<>(ResponseModel.FAILURE, "Task not found !!!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		activitiService.claimTask(assignee.getUserId(), taskId);
		ResponseModel<Object> response = new ResponseModel<>(ResponseModel.SUCCESS);
		response.setActiveTasks(activitiService.getActiveTaskIds(instanceId));
		return ResponseEntity.ok(response);
	}

	/**
	 * Get Assigned tasks to that user
	 * 
	 * @param variables
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/tasks/user/{userId}/assigned", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getAssignedToUserTasks(@RequestBody VariableWrapper wrapper, @PathVariable("userId") String userId) {
	    Map<String, Object> variables = new HashMap<>();
        if(wrapper != null){
            variables = wrapper.getVariables();
        }
	    List<RtaTaskInfo> tasks = activitiService.getAssignedTasks(userId, variables);
        ResponseModel<List<RtaTaskInfo>> response = new ResponseModel<>(ResponseModel.SUCCESS, tasks);
        response.setActiveTasks(tasks);
        return ResponseEntity.ok(response);
    }
	
	/**
	 * Complete a task by instance id and task def key and userId. with optional claim = true/false.
	 * if already completed return the next completed task
	 * 
	 * @param userId
	 * @param taskDef
	 * @param instanceId
	 * @param isClaim
	 * @param VariableWrapper wrapper
	 * @return
	 */
	@RequestMapping(value = "/complete/instance/{instanceId}/task/{taskDef}/user/{userId}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> completeTask(@RequestBody VariableWrapper wrapper, @PathVariable String userId, 
	        @PathVariable("taskDef") String taskDef, @PathVariable("instanceId") String instanceId, 
	        @RequestParam(name = "claim", required = false) boolean isClaim,
			@RequestParam(name = "rtaOfficeCode", required = false) String rtaOfficeCode) {
		String taskId = activitiService.getTaskIdByUserIdTaskDefInstanceId(userId, taskDef, instanceId);
		List<RtaTaskInfo> taskList = new ArrayList<RtaTaskInfo>();
		if (taskId == null) {
		    RtaTaskInfo rtaTasks = activitiService.getNextCompletedTask(taskDef, instanceId, userId);
		    if(rtaTasks.getTaskDefKey() != null){
		        taskList.add(rtaTasks);
		    } else {
		        taskList = activitiService.getActiveTaskIds(instanceId);
		    }
		} else {
		    if (isClaim) {
		        activitiService.claimTask(userId, taskId);
		    }
		    Map<String, Object> variables = new HashMap<>();
	        if(wrapper != null){
	            variables = wrapper.getVariables();
	        }
		    activitiService.completeTask(userId, taskId, variables);
		    taskList = activitiService.getActiveTaskIds(instanceId);
		}
		ResponseModel<Object> response = new ResponseModel<>(ResponseModel.SUCCESS);
		response.setActiveTasks(taskList);
		return ResponseEntity.ok(response);
	}

	/**
	 * Complete a task by instance id and task def key with action on node action = approved/rejected
	 * with claim = true/false.
	 * 
	 * @param assignee
	 * @param taskDef
	 * @param action
	 * @param instanceId
	 * @param isClaim
	 * @return
	 */
	@RequestMapping(value = "/complete/instance/{instanceId}/task/{taskDef}/action/{action}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> completeTaskWithAction(@RequestBody Assignee assignee,
			@PathVariable("taskDef") String taskDef, @PathVariable("action") String action,
			@PathVariable("instanceId") String instanceId,
			@RequestParam(name = "claim", required = false) boolean isClaim) {
		String taskId = activitiService.getTaskIdByUserIdTaskDefInstanceId(assignee.getUserId(), taskDef, instanceId);
		if (taskId == null) {
			ResponseModel<Object> response = new ResponseModel<>(ResponseModel.FAILURE, "Task not found !!!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		if (!(action.equalsIgnoreCase(Status.APPROVED.getLabel())
				|| action.equalsIgnoreCase(Status.REJECTED.getLabel()))) {
			ResponseModel<Object> response = new ResponseModel<>(ResponseModel.FAILURE, "Invalid action : " + action);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		if (isClaim) {
			activitiService.claimTask(assignee.getUserId(), taskId);
		}
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put(taskDef, action.toUpperCase());
		activitiService.completeTaskWithAction(assignee, taskId, variables);
		ResponseModel<Object> response = new ResponseModel<>(ResponseModel.SUCCESS);
		response.setActiveTasks(activitiService.getActiveTaskIds(instanceId));
		return ResponseEntity.ok(response);
	}

	/**
	 * Delete a running instance.
	 * 
	 * @param instanceId
	 * @return
	 */
	@RequestMapping(value = "/delete/instance/{instanceId}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> deleteProcessInstance(@PathVariable("instanceId") String instanceId) {
		ResponseModel<Object> response = new ResponseModel<>(ResponseModel.SUCCESS, "Instance Deleted Successfully...");
		try{
			activitiService.deleteInstance(instanceId, "application deleted");
		} catch(ActivitiObjectNotFoundException ae){
			response.setMessage("Instance not found in activiti.");
			response.setStatusCode(HttpStatus.NOT_FOUND.toString());
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		return ResponseEntity.ok(response);
	}
	
	/**
     * Delete a running instance.
     * 
     * @param instanceId
     * @return
     */
    @RequestMapping(value = "/completedtask/next/task/{task_def}/instance/{instanceId}/userid/{userid}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getNextCompleteTask(@RequestBody Assignee assignee, @PathVariable("task_def") String taskDef, 
            @PathVariable("instanceId") String instanceId, @PathVariable("userid") String userId) {
        RtaTaskInfo rtaTasks = activitiService.getNextCompletedTask(taskDef, instanceId, userId);
        ResponseModel<RtaTaskInfo> response = new ResponseModel<RtaTaskInfo>(ResponseModel.SUCCESS, rtaTasks);
        return ResponseEntity.ok(response);
    }

    /**
     * Get BpmnModel
     * 
     * @param processId
     * @return
     */
    @RequestMapping(value = "/all/user/tasks/process/{process_id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getAllUserTasksInProcess(@PathVariable("process_id") String processId){
        BpmnModel model = activitiService.getAllUserTasksInProcess(processId);
        ResponseModel<BpmnModel> response = new ResponseModel<BpmnModel>(ResponseModel.SUCCESS, model);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Add Process variable
     * 
     * @param executionId
     * @param variables
     * @return
     */
    @RequestMapping(value = "/set/variables/instance/{executionId}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> setProcessVaribales(@RequestBody VariableWrapper wrapper, @PathVariable("executionId") String executionId) {
        Map<String, Object> variables = wrapper.getVariables();
        if(variables == null){
            variables = new HashMap<>();
        }
        boolean status = activitiService.setVaribaleToProcess(executionId, variables);
        ResponseModel<Boolean> response = new ResponseModel<>(ResponseModel.SUCCESS, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Complete current task and assign next task to a specific user by claiming the task.
     * 
     * @param wrapper
     * @param userId
     * @param nextUserId
     * @param taskDef
     * @param instanceId
     * @param isClaim
     * @return
     */
    @RequestMapping(value = "/complete/instance/{instanceId}/task/{taskDef}/user/{userId}/next/task/{nextTaskDef}/user/{nextUserId}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> completeTask(@RequestBody VariableWrapper wrapper, @PathVariable("userId") String userId,
			@PathVariable("nextTaskDef") String nextTaskDef, @PathVariable("nextUserId") String nextUserId,
	        @PathVariable("taskDef") String taskDef, @PathVariable("instanceId") String instanceId, 
	        @RequestParam(name = "claim", required = false) boolean isClaim) {
		String taskId = activitiService.getTaskIdByUserIdTaskDefInstanceId(userId, taskDef, instanceId);
		List<RtaTaskInfo> taskList = new ArrayList<RtaTaskInfo>();
		if (taskId == null) {
		    RtaTaskInfo rtaTasks = activitiService.getNextCompletedTask(taskDef, instanceId, userId);
		    if(rtaTasks.getTaskDefKey() != null){
		        taskList.add(rtaTasks);
		    } else {
		        taskList = activitiService.getActiveTaskIds(instanceId);
		    }
		} else {
		    if (isClaim) {
		        activitiService.claimTask(userId, taskId);
		    }
		    Map<String, Object> variables = new HashMap<>();
	        if(wrapper != null){
	            variables = wrapper.getVariables();
	        }
		    activitiService.completeTask(userId, taskId, variables);
		    taskList = activitiService.getActiveTaskIds(instanceId);
		}
		if(null != nextTaskDef && null != nextUserId){
			for(RtaTaskInfo tsk : taskList){
				if(tsk.getTaskDefKey().equalsIgnoreCase(nextTaskDef)){
					activitiService.claimTask(nextUserId, tsk.getTaskDefKey());
				}
			}
		}
		ResponseModel<Object> response = new ResponseModel<>(ResponseModel.SUCCESS);
		response.setActiveTasks(taskList);
		return ResponseEntity.ok(response);
	}
}
