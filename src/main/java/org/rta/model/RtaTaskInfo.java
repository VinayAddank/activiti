/**
 * 
 */
package org.rta.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author admin
 *
 */
@JsonInclude(Include.NON_NULL)
public class RtaTaskInfo {

	private String taskId;
	private String taskDefKey;
	private String taskName;
	private String processDefId;
	private String processName;
	private String processInstanceId;
	private Long startDate;
	private Long endDate;

	public RtaTaskInfo() {
	}

	public RtaTaskInfo(String taskId, String taskDefKey, String taskName) {
		this.taskId = taskId;
		this.taskDefKey = taskDefKey;
		this.taskName = taskName;
	}

	public RtaTaskInfo(String taskId, String taskDefKey, String taskName, String processDefId, String processName,
			String processInstanceId) {
		this.taskId = taskId;
		this.taskDefKey = taskDefKey;
		this.taskName = taskName;
		this.processDefId = processDefId;
		this.processName = processName;
		this.processInstanceId = processInstanceId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskDefKey() {
		return taskDefKey;
	}

	public void setTaskDefKey(String taskDefKey) {
		this.taskDefKey = taskDefKey;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getProcessDefId() {
		return processDefId;
	}

	public void setProcessDefId(String processDefId) {
		this.processDefId = processDefId;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

}
