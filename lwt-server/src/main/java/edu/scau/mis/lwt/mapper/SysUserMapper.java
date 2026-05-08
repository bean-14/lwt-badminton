package edu.scau.mis.lwt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表 Mapper
 * <p>
 * 除了 MyBatis-Plus 自带的 CRUD 方法外，
 * 还扩展了一个自定义方法 deductHours 用于原子扣减课时。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 原子扣减用户课时（CAS 风格操作）
     * <p>
     * SQL：UPDATE sys_user SET remaining_hours = remaining_hours - #{hours}
     *      WHERE id = #{id} AND remaining_hours >= #{hours}
     * <p>
     * 为什么不用「查询 → 计算 → 更新」这种三段式？
     *   三段式存在并发问题：
     *     线程 A 查询到 remaining_hours=5
     *     线程 B 查询到 remaining_hours=5
     *     线程 A 更新为 4
     *     线程 B 也更新为 4（应该为 3，丢失了一次扣减！）
     * <p>
     * 用 WHERE remaining_hours >= #{hours} 的条件更新，
     * 数据库会加行锁，保证同一时间只有一个线程能执行成功。
     * 如果影响行数为 0，说明余额不足，上层抛出异常。
     * <p>
     * @param id 用户 ID
     * @param hours 扣除的课时数（通常为 1）
     * @return 影响行数（1 表示成功，0 表示余额不足）
     */
    int deductHours(@Param("id") Long id, @Param("hours") int hours);
}
