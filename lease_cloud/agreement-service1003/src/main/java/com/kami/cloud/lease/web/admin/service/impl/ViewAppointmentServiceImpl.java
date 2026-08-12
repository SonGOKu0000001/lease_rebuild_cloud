package com.kami.cloud.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kami.cloud.lease.common.feign.ApartmentFeignApi;
import com.kami.cloud.lease.common.feign.dto.ApartmentInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.model.entity.ViewAppointment;
import com.kami.cloud.lease.web.admin.mapper.ViewAppointmentMapper;
import com.kami.cloud.lease.web.admin.service.ViewAppointmentService;
import com.kami.cloud.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.kami.cloud.lease.web.admin.vo.appointment.AppointmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kami
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {
    @Autowired
    private ViewAppointmentMapper mapper;
    @Autowired
    private ApartmentFeignApi apartmentFeignApi;

    @Override
    public IPage<AppointmentVo> pageItem(IPage<AppointmentVo> page, AppointmentQueryVo queryVo) {
        Set<Long> apartmentIds = resolveApartmentIds(queryVo);
        if (apartmentIds != null && apartmentIds.isEmpty()) {
            return page;
        }

        LambdaQueryWrapper<ViewAppointment> wrapper = new LambdaQueryWrapper<>();
        if (queryVo.getApartmentId() != null) {
            wrapper.eq(ViewAppointment::getApartmentId, queryVo.getApartmentId());
        } else if (!CollectionUtils.isEmpty(apartmentIds)) {
            wrapper.in(ViewAppointment::getApartmentId, apartmentIds);
        }
        if (StringUtils.hasText(queryVo.getName())) {
            wrapper.like(ViewAppointment::getName, queryVo.getName());
        }
        if (StringUtils.hasText(queryVo.getPhone())) {
            wrapper.like(ViewAppointment::getPhone, queryVo.getPhone());
        }

        Page<ViewAppointment> entityPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        mapper.selectPage(entityPage, wrapper);

        List<AppointmentVo> records = convert(entityPage.getRecords());
        page.setRecords(records);
        page.setTotal(entityPage.getTotal());
        page.setPages(entityPage.getPages());
        return page;
    }

    private Set<Long> resolveApartmentIds(AppointmentQueryVo queryVo) {
        if (queryVo.getProvinceId() == null && queryVo.getCityId() == null && queryVo.getDistrictId() == null) {
            return null;
        }
        Result<List<Long>> result = apartmentFeignApi.apartmentIdsByRegion(
                queryVo.getProvinceId(), queryVo.getCityId(), queryVo.getDistrictId());
        List<Long> ids = result == null ? null : result.getData();
        return CollectionUtils.isEmpty(ids) ? Collections.emptySet() : new HashSet<>(ids);
    }

    private List<AppointmentVo> convert(List<ViewAppointment> appointments) {
        if (CollectionUtils.isEmpty(appointments)) {
            return Collections.emptyList();
        }
        Set<Long> apartmentIds = appointments.stream()
                .map(ViewAppointment::getApartmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ApartmentInfoDTO> apartmentMap;
        if (CollectionUtils.isEmpty(apartmentIds)) {
            apartmentMap = Collections.emptyMap();
        } else {
            Result<List<ApartmentInfoDTO>> result = apartmentFeignApi.apartmentByIds(new ArrayList<>(apartmentIds));
            List<ApartmentInfoDTO> list = result == null ? null : result.getData();
            apartmentMap = CollectionUtils.isEmpty(list)
                    ? Collections.emptyMap()
                    : list.stream().collect(Collectors.toMap(ApartmentInfoDTO::getId, Function.identity()));
        }

        return appointments.stream().map(appointment -> {
            AppointmentVo vo = new AppointmentVo();
            BeanUtils.copyProperties(appointment, vo);
            vo.setApartmentInfo(apartmentMap.get(appointment.getApartmentId()));
            return vo;
        }).collect(Collectors.toList());
    }
}