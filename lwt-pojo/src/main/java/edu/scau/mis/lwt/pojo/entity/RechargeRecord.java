package edu.scau.mis.lwt.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge_record")
public class RechargeRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer hours;

    private BigDecimal amount;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
