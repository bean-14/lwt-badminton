package edu.scau.mis.lwt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.scau.mis.lwt.pojo.entity.Venue;

/**
 * 场地服务接口
 * 定义场地管理相关的业务逻辑方法
 * 继承MyBatis-Plus的IService，获得基础CRUD能力
 * 目前没有额外的业务方法，直接使用继承的基础方法即可
 */
public interface VenueService extends IService<Venue> {
}
