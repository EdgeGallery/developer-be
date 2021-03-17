package org.edgegallery.developer.model.vm;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;

@Getter
@Setter
@NoArgsConstructor
public class VmCreateStageStatus {

    private EnumTestConfigStatus hostInfo;

    private EnumTestConfigStatus csar;

    private EnumTestConfigStatus instantiateInfo;

    private EnumTestConfigStatus workStatus;

    public static List<String> getOrderedStage() {
        return ImmutableList.of("hostInfo", "csar", "instantiateInfo", "workStatus");
    }

    /**
     * getNextStage.
     */
    public static String getNextStage(String currentStage) {
        if ("workStatus".equalsIgnoreCase(currentStage) || !getOrderedStage().contains(currentStage)) {
            return null;
        }
        int currentIndex = getOrderedStage().indexOf(currentStage);
        return getOrderedStage().get(currentIndex + 1);
    }

}
