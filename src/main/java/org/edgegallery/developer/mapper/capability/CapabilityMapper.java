package org.edgegallery.developer.mapper.capability;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.capability.Capability;

@Mapper
public interface CapabilityMapper {
    int insert(Capability capability);

    int updateById(Capability capability);

    int deleteById(@Param("id") String id);

	int deleteByGroupId(@Param("groupId") String groupId);

    List<Capability> selectAll();

    List<Capability> selectByType(@Param("type") String type);

    Capability selectById(@Param("id") String id);

    List<Capability> selectByGroupId(@Param("groupId") String groupId);

    List<Capability> selectByNameWithFuzzy(@Param("name") String name);

    List<Capability> selectByNameEnWithFuzzy(@Param("nameEn") String nameEn);

    Capability selectByName(@Param("name") String name);

    List<Capability> selectByApiFileId(@Param("apiFileId") String apiFileId);

    List<Capability> selectByNameOrNameEn(@Param("name") String name, @Param("nameEn") String nameEn);

    List<Capability> selectByProjectId(@Param("projectId") String projectId);

    int updateSelectCountByIds(List<String> ids);
}
