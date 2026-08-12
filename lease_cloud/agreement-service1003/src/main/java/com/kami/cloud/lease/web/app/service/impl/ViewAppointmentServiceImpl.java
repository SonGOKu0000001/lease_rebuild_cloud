package com.kami.cloud.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kami.cloud.lease.common.feign.ApartmentFeignApi;
import com.kami.cloud.lease.common.feign.dto.ApartmentInfoDTO;
import com.kami.cloud.lease.common.feign.dto.ApartmentItemDTO;
import com.kami.cloud.lease.common.feign.dto.GraphDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.model.entity.ViewAppointment;
import com.kami.cloud.lease.model.enums.ItemType;
import com.kami.cloud.lease.web.app.mapper.ViewAppointmentMapper;
import com.kami.cloud.lease.web.app.service.ViewAppointmentService;
import com.kami.cloud.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.kami.cloud.lease.web.app.vo.appointment.AppointmentItemVo;
import com.kami.cloud.lease.web.app.vo.graph.GraphVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kami
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {
    @Autowired
    private ViewAppointmentMapper viewAppointmentMapper;
    @Autowired
    private ApartmentFeignApi apartmentFeignApi;

    @Override
    public List<AppointmentItemVo> listItemByUserId(Long userId) {
        LambdaQueryWrapper<ViewAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ViewAppointment::getUserId, userId);
        queryWrapper.orderByDesc(ViewAppointment::getCreateTime);
        List<ViewAppointment> appointments = viewAppointmentMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(appointments)) {
            return Collections.emptyList();
        }

        Set<Long> apartmentIds = appointments.stream()
                .map(ViewAppointment::getApartmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ApartmentInfoDTO> apartmentMap = apartmentByIds(apartmentIds);
        Map<Long, List<GraphDTO>> graphMap = graphMap(ItemType.APARTMENT, apartmentIds);

        return appointments.stream().map(appointment -> {
            AppointmentItemVo itemVo = new AppointmentItemVo();
            itemVo.setId(appointment.getId());
            itemVo.setAppointmentTime(appointment.getAppointmentTime());
            itemVo.setAppointmentStatus(appointment.getAppointmentStatus());
            ApartmentInfoDTO apartmentInfo = apartmentMap.get(appointment.getApartmentId());
            if (apartmentInfo != null) {
                itemVo.setApartmentName(apartmentInfo.getName());
            }
            itemVo.setGraphVoList(convertGraphs(graphMap.get(appointment.getApartmentId())));
            return itemVo;
        }).collect(Collectors.toList());
    }

    @Override
    public AppointmentDetailVo getDetailById(Long id) {
        ViewAppointment viewAppointment = viewAppointmentMapper.selectById(id);
        if (viewAppointment == null) {
            return null;
        }
        AppointmentDetailVo detailVo = new AppointmentDetailVo();
        BeanUtils.copyProperties(viewAppointment, detailVo);
        if (viewAppointment.getApartmentId() != null) {
            Result<ApartmentItemDTO> result = apartmentFeignApi.apartmentItemById(viewAppointment.getApartmentId());
            detailVo.setApartmentItemVo(result == null ? null : result.getData());
        }
        return detailVo;
    }

    private Map<Long, ApartmentInfoDTO> apartmentByIds(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Result<List<ApartmentInfoDTO>> result = apartmentFeignApi.apartmentByIds(new ArrayList<>(ids));
        List<ApartmentInfoDTO> list = result == null ? null : result.getData();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(ApartmentInfoDTO::getId, Function.identity()));
    }

    private Map<Long, List<GraphDTO>> graphMap(ItemType itemType, Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Result<List<GraphDTO>> result = apartmentFeignApi.graphList(itemType.getCode(), new ArrayList<>(ids));
        List<GraphDTO> list = result == null ? null : result.getData();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().filter(g -> g.getItemId() != null)
                .collect(Collectors.groupingBy(GraphDTO::getItemId));
    }

    private List<GraphVo> convertGraphs(List<GraphDTO> graphs) {
        if (CollectionUtils.isEmpty(graphs)) {
            return Collections.emptyList();
        }
        return graphs.stream().map(graph -> GraphVo.builder()
                .name(graph.getName()).url(graph.getUrl()).build()).collect(Collectors.toList());
    }
}