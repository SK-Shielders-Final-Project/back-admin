package org.rookies.zdme.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemPropertiesUpdateRequest {
    private String configName;
    private String configValue;
}
