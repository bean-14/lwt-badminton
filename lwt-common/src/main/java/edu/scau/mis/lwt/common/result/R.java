package edu.scau.mis.lwt.common.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> error() {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg("error");
        return r;
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    public static <T> R<T> error(Integer code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
