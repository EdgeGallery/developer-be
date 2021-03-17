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
public class VmImportStageStatus {

    private EnumTestConfigStatus createImageInfo;

    private EnumTestConfigStatus imageStatus;

    private EnumTestConfigStatus downloadImageInfo;

    public static List<String> getOrderedStage() {

        return ImmutableList.of("createImageInfo", "imageStatus", "downloadImageInfo");
    }

    /**
     * getNextStage.
     */
    public static String getNextStage(String currentStage) {
        if ("downloadImageInfo".equalsIgnoreCase(currentStage) || !getOrderedStage().contains(currentStage)) {
            return null;
        }
        int currentIndex = getOrderedStage().indexOf(currentStage);
        return getOrderedStage().get(currentIndex + 1);
    }

}
