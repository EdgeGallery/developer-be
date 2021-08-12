package org.edgegallery.developer.mapper.capability;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.capability.CapabilityGroup;

@Mapper
public interface CapabilityGroupMapper {
	public int insert(CapabilityGroup group);
	public int deleteById(@Param("id")String id);
	public int updateById(CapabilityGroup group);
	public List<CapabilityGroup> selectAll();
	public List<CapabilityGroup> selectByType(@Param("type")String type);
	public List<CapabilityGroup> selectByNameOrNameEn(@Param("name")String name,@Param("nameEn")String nameEn);
	public CapabilityGroup selectById(@Param("id")String id);
	public CapabilityGroup selectByName(@Param("name")String name);
}
