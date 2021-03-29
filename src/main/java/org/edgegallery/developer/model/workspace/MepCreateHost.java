package org.edgegallery.developer.model.workspace;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
@Getter
@Setter
public class MepCreateHost {

    private String hostId;

    @NotBlank
    @Length(min = 6, max = 50)
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String architecture;

    @NotNull
    private EnumHostStatus status;

    @NotBlank
    private String lcmIp;

    private String mecHost;

    private String protocol;

    @Range(min = 30000, max = 30400)
    private int port;

    private String os;

    private int portRangeMin;

    private int portRangeMax;

    private String userId;

    private String configId;

    private String userName;

    private String password;

}
