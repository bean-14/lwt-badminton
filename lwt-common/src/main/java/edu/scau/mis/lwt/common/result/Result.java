package edu.scau.mis.lwt.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果封装类
 * 用于规范化所有接口的返回格式
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> implements Serializable {

    /**
     * 状态码：200-成功，500-服务器错误，400-客户端错误，404-资源不存在等
     */
    private Integer code;

    /**
     * 响应消息（成功或错误的描述信息）
     */
    private String msg;

    /**
     * 响应数据（泛型，可以是任意类型）
     */
    private T data;

    /**
     * 成功响应（无数据）
     * @param <T> 数据类型
     * @return Result 统一响应对象
     */
    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        return result;
    }

    /**
     * 成功响应（带数据）
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result 统一响应对象
     */
    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 错误响应（默认500错误）
     * @param <T> 数据类型
     * @return Result 统一响应对象
     */
    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg("error");
        return result;
    }

    /**
     * 错误响应（自定义错误消息）
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return Result 统一响应对象
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    /**
     * 错误响应（自定义状态码和错误消息）
     * @param code 状态码
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return Result 统一响应对象
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
