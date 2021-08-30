package org.edgegallery.developer.model.lcm;

import lombok.Getter;
import lombok.Setter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Getter
@Setter
public class LcmResponseBody {

    private Object data;

    private String retCode;

    private String message;

    private String params;

}
