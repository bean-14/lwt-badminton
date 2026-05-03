package edu.scau.mis.lwt.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RechargeDTO implements Serializable {

    private Long userId;

    private Integer hours;
}
