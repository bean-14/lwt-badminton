package edu.scau.mis.lwt.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterDTO implements Serializable {

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String userType;
}
