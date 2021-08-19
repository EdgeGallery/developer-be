package org.edgegallery.developer.service.capability.impl;

import java.util.List;

import org.edgegallery.developer.mapper.capability.CapabilityGroupStatMapper;
import org.edgegallery.developer.model.capability.CapabilityGroupStat;
import org.edgegallery.developer.service.capability.CapabilityGroupStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CapabilityGroupStatServiceImpl implements CapabilityGroupStatService{
	@Autowired
	private CapabilityGroupStatMapper capabilityGroupStatMapper;
	@Override
	public List<CapabilityGroupStat> findAll() {
		return capabilityGroupStatMapper.selectAll();
	}
}
