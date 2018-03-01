/**
 * 
 */
package org.rta.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author admin
 *
 */
@JsonInclude(Include.NON_NULL)
public class Assignee {

	@NotNull(message = "userId can't be null !!!")
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
