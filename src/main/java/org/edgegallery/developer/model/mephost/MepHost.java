package org.edgegallery.developer.model.mephost;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
@Getter
@Setter
public class MepHost {

    private String id;

    @NotBlank
    @Length(min = 6, max = 50)
    private String name;

    @NotBlank
    private String lcmIp;

    @NotBlank
    private String lcmProtocol;

    @Range(min = 30000, max = 30400)
    private int lcmPort;

    @NotBlank
    private String architecture;

    @NotNull
    private EnumMepHostStatus status;

    @NotBlank
    private String mecHostIp;

    @NotBlank
    private EnumVimType vimType;

    private String mecHostUserName;

    private String mecHostPassword;

    private int mecHostPort;

    private String userId;

    private String configId;

    private String networkParameter;

    private String resource;

    @NotBlank
    private String address;

}
