/**
 * 
 */
package org.rta.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rta.model.Assignee;
import org.rta.model.ProcessUser;
import org.rta.model.RtaProcessInfo;
import org.rta.model.RtaTaskInfo;
import org.rta.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author admin
 *
 */
@Service
public class ActivitiServiceImpl implements ActivitiService {

    private static final Log log = LogFactory.getLog(ActivitiServiceImpl.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RepositoryService repositoryService;
    
    @Override
    public void createUsers(List<ProcessUser> users) {
        // create users and groups
        for (ProcessUser ug : users) {
            String userId = ug.getUserId();
            String groupId = ug.getGroupId();
            log.info("Going for user : " + userId + " for group : " + groupId);
            User user = identityService.createUserQuery().userId(userId).singleResult();
            if (user == null) {
                log.info("creating user with id : " + userId);
                user = identityService.newUser(userId);
                user.setFirstName(ug.getFirstName());
                user.setLastName(ug.getLastName());
                user.setPassword(ug.getPassword());
                identityService.saveUser(user);
            } else {
                log.info("User already exist with id : " + userId);
            }
            Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
            if (group == null) {
                log.info("Creating group with id : " + groupId);
                group = identityService.newGroup(groupId);
                group.setName(ug.getGroupName());
                group.setType(ug.getGroupType());
                identityService.saveGroup(group);
            } else {
                log.info("Group already exist with id : " + groupId);
            }
            List<Group> userPreviousGroup = identityService.createGroupQuery().groupMember(userId).list();
            if (userPreviousGroup == null) {
                identityService.createMembership(user.getId(), group.getId());
                log.info("User : " + userId + " linkd with group : " + groupId);
            } else {
                boolean isLinked = false;
                for(Group g : userPreviousGroup){
                    if(g.getId().equalsIgnoreCase(group.getId())){
                        isLinked = true;
                    }
                }
                if(!isLinked){
                    identityService.createMembership(user.getId(), group.getId());
                    log.info("User : " + userId + " linkd with group : " + groupId);
                }
            }
        }
    }

    @Override
    public RtaProcessInfo startProcess(String userId, String processId, Map<String, Object> variables) {
        User user = identityService.createUserQuery().userId(userId).singleResult();
        log.info("Starting process with userId " + user.getId());
        variables.put("user", user);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processId, variables);
        RtaProcessInfo pInfo = new RtaProcessInfo();
        pInfo.setId(pi.getProcessDefinitionId());
        pInfo.setName(pi.getProcessDefinitionName());
        pInfo.setInstanceId(pi.getProcessInstanceId());
        return pInfo;
    }

    @Override
    public List<RtaTaskInfo> getActiveTaskIds(String processInstanceId) {
        List<RtaTaskInfo> taskList = new ArrayList<>();
        try {
            List<String> ids = runtimeService.getActiveActivityIds(processInstanceId);
            ProcessInstance processInstance =
                    runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            for (String id : ids) {
                RtaTaskInfo task = new RtaTaskInfo();
                task.setTaskDefKey(id);
                task.setProcessDefId(processInstance.getProcessDefinitionId());
                task.setProcessInstanceId(processInstance.getProcessInstanceId());
                taskList.add(task);
            }
        } catch (org.activiti.engine.ActivitiObjectNotFoundException ex) {
        }
        return taskList;
    }

    @Override
    public List<RtaTaskInfo> getActiveInstances(String taskDefKey) {
        List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskDefKey).list();
        List<RtaTaskInfo> taskInfoList = new ArrayList<>();
        for (Task task : tasks) {
            RtaTaskInfo rtaTask = new RtaTaskInfo(task.getId(), task.getTaskDefinitionKey(), task.getName(),
                    task.getProcessDefinitionId(), null, task.getProcessInstanceId());
            taskInfoList.add(rtaTask);
        }
        return taskInfoList;
    }

    @Override
    public List<RtaTaskInfo> getTasks(String userId, Map<String, Object> variables, Integer from, Integer to,
            Integer perPage) {
        TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(userId);
        if (variables != null) {
            for (Entry<String, Object> entry : variables.entrySet()) {
                query.processVariableValueEquals(entry.getKey(), entry.getValue());
            }
        }
        List<Task> taskList = query.list();
        List<RtaTaskInfo> taskInfoList = new ArrayList<>();
        for (Task task : taskList) {
            RtaTaskInfo rtaTask = new RtaTaskInfo(task.getId(), task.getTaskDefinitionKey(), task.getName(),
                    task.getProcessDefinitionId(), null, task.getProcessInstanceId());
            taskInfoList.add(rtaTask);
        }
        return taskInfoList;
    }

    @Override
    public List<RtaTaskInfo> getTasks(String userId, String processInstanceId) {
        List<Task> taskList = taskService.createTaskQuery().taskCandidateOrAssigned(userId)
                .processInstanceId(processInstanceId).list();
        List<RtaTaskInfo> taskInfoList = new ArrayList<>();
        for (Task task : taskList) {
            RtaTaskInfo rtaTask = new RtaTaskInfo(task.getId(), task.getTaskDefinitionKey(), task.getName(),
                    task.getProcessDefinitionId(), null, task.getProcessInstanceId());
            taskInfoList.add(rtaTask);
        }
        return taskInfoList;
    }

    @Override
    public List<RtaTaskInfo> getAssignedTasks(String userId, Map<String, Object> variables) {
        TaskQuery query = taskService.createTaskQuery().taskAssignee(userId);
        if (variables != null) {
            for (Entry<String, Object> entry : variables.entrySet()) {
                query.processVariableValueEquals(entry.getKey(), entry.getValue());
            }
        }
        List<Task> taskList = query.list();
        List<RtaTaskInfo> taskInfoList = new ArrayList<>();
        for (Task task : taskList) {
            RtaTaskInfo rtaTask = new RtaTaskInfo(task.getId(), task.getTaskDefinitionKey(), task.getName(),
                    task.getProcessDefinitionId(), null, task.getProcessInstanceId());
            taskInfoList.add(rtaTask);
        }
        return taskInfoList;
    }

    @Override
    public RtaTaskInfo getTaskDetails(String instanceId) {
        RtaTaskInfo taskInfo = new RtaTaskInfo();
        HistoricProcessInstance htry =
                historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if(htry == null){
            return null;
        }
        taskInfo.setProcessDefId(htry.getProcessDefinitionId());
        if(htry.getStartTime() != null){
            taskInfo.setStartDate(htry.getStartTime().getTime());
        }
        if(htry.getEndTime() != null){
            taskInfo.setEndDate(htry.getEndTime().getTime());
        }
        return taskInfo;
    }

    @Override
    public void claimTask(String userId, String taskId) {
        taskService.claim(taskId, userId);
        log.debug("task claimed = > taskId:" + taskId + " userId:" + userId);
    }

    @Override
    public void completeTask(String userId, String taskId, Map<String, Object> variables) {
        if (variables != null && variables.size() > 0) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        log.debug("task completed = > taskId:" + taskId + " userId:" + userId);
    }

    @Override
    public void completeTaskWithAction(Assignee assignee, String taskId, Map<String, Object> variables) {
        if (variables != null && variables.size() > 0) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        log.debug("task completed and set hpa = > taskId:" + taskId + " userId:" + assignee.getUserId());
    }

    @Override
    public String getTaskIdByUserIdTaskDefInstanceId(String userId, String taskDef, String processInstanceId) {
        Task task = taskService.createTaskQuery().taskCandidateOrAssigned(userId).processInstanceId(processInstanceId)
                .taskDefinitionKey(taskDef).singleResult();
        if (task == null) {
            return null;
        }
        return task.getId();
    }

    @Override
    public void deleteInstance(String instanceId, String msg) {
        runtimeService.deleteProcessInstance(instanceId, msg);
        log.debug("Instance Id : " + instanceId + " deleted successfully...");
    }

    @Override
    public RtaTaskInfo getNextCompletedTask(String taskDefKey, String processInstanceId, String userId) {
        RtaTaskInfo nextFinishedTask = new RtaTaskInfo();
        List<HistoricTaskInstance> historyList =
                historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
                        .taskAssignee(userId).orderByHistoricTaskInstanceEndTime().asc().list();
        boolean terminate = false;
        for (HistoricTaskInstance history : historyList) {
            if (terminate) {
                nextFinishedTask.setProcessDefId(history.getProcessDefinitionId());
                nextFinishedTask.setProcessInstanceId(history.getProcessInstanceId());
                nextFinishedTask.setTaskDefKey(history.getTaskDefinitionKey());
            }
            if (taskDefKey.equals(history.getTaskDefinitionKey())) {
                terminate = true;
            }
        }
        return nextFinishedTask;
    }

    @Override
    public BpmnModel getAllUserTasksInProcess(String processId) {
        BpmnModel bpmn =  repositoryService.getBpmnModel(processId);
        return bpmn;
    }

    @Override
    public boolean setVaribaleToProcess(String executionId, Map<String, Object> variables) {
        List<RtaTaskInfo> tasks = getActiveTaskIds(executionId);
        if(null == tasks || tasks.isEmpty() || null == variables || variables.isEmpty()){
            return false;
        }
        runtimeService.setVariables(executionId, variables);
        return true;
    }
}
