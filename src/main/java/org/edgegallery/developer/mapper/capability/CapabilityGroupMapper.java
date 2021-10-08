package org.edgegallery.developer.mapper.capability;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.capability.CapabilityGroup;

@Mapper
public interface CapabilityGroupMapper {
    int insert(CapabilityGroup group);

    int deleteById(@Param("id") String id);

    int updateById(CapabilityGroup group);

    List<CapabilityGroup> selectAll();

    List<CapabilityGroup> selectByType(@Param("type") String type);

    List<CapabilityGroup> selectByNameOrNameEn(@Param("name") String name, @Param("nameEn") String nameEn);

    CapabilityGroup selectById(@Param("id") String id);

    CapabilityGroup selectByName(@Param("name") String name);
}
