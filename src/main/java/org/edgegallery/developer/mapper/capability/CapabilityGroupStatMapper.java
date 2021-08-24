package org.edgegallery.developer.mapper.capability;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.capability.CapabilityGroupStat;

public interface CapabilityGroupStatMapper {
	public List<CapabilityGroupStat> selectAll();
	
	public List<CapabilityGroupStat> selectByType(@Param("type")String type);
}
