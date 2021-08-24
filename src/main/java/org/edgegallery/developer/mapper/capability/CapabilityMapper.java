package org.edgegallery.developer.mapper.capability;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.capability.Capability;

@Mapper
public interface CapabilityMapper {
	public int insert(Capability capability);
	public int updateById(Capability capability);
	public int deleteById(@Param("id")String id);
	public List<Capability> selectAll();
	public List<Capability> selectByType(@Param("type")String type);
	public Capability selectById(@Param("id")String id);
	public List<Capability> selectByGroupId(@Param("groupId")String groupId);
	public List<Capability> selectByNameWithFuzzy(@Param("name")String name);
	public List<Capability> selectByNameEnWithFuzzy(@Param("nameEn")String nameEn);
	public Capability selectByName(@Param("name")String name);
	public List<Capability> selectByNameOrNameEn(@Param("name")String name,@Param("nameEn")String nameEn);
	public List<Capability> selectByProjectId(@Param("projectId")String projectId);
	public int updateSelectCountByIds(List<String> ids);
}
