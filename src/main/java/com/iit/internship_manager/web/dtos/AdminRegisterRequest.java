// AdminRegisterRequest.java
package com.iit.internship_manager.web.dtos;

import lombok.*;

// AdminIT has no extra fields beyond the base.
// The class still needs to exist so Jackson can instantiate it
// and the strategy can be typed against it.
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminRegisterRequest extends RegisterRequest {
}