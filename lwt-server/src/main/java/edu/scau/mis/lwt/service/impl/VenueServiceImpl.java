package edu.scau.mis.lwt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.scau.mis.lwt.mapper.VenueMapper;
import edu.scau.mis.lwt.pojo.entity.Venue;
import edu.scau.mis.lwt.service.VenueService;
import org.springframework.stereotype.Service;

@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements VenueService {
}
