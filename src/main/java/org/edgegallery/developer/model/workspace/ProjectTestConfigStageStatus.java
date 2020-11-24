package org.edgegallery.developer.model.workspace;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author chenhui
 */
@Getter
@Setter
@NoArgsConstructor
public class ProjectTestConfigStageStatus {

    private EnumTestConfigStatus csar;

    private EnumTestConfigStatus hostInfo;

    private EnumTestConfigStatus instantiateInfo;

    private EnumTestConfigStatus workStatus;

    public static List<String> getOrderedStage(){
        return ImmutableList.of("csar","hostInfo","instantiateInfo","workStatus");
    }

    public static String getNextStage(String currentStage){
        if ("workStatus".equalsIgnoreCase(currentStage) ||
                !getOrderedStage().contains(currentStage)){
            return null;
        }
        int currentIndex = getOrderedStage().indexOf(currentStage);
        return getOrderedStage().get(currentIndex + 1);
    }

}
