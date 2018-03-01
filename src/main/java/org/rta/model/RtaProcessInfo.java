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
public class RtaProcessInfo {

	private String id;
	private String instanceId;
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "RtaProcessInfo [id=" + id + ", instanceId=" + instanceId + ", name=" + name + "]";
	}

}
