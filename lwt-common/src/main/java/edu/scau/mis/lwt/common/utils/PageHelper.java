package edu.scau.mis.lwt.common.utils;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageHelper<T> implements Serializable {

    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private List<T> list;

    public PageHelper() {
    }

    public PageHelper(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
    }

    public Integer getPages() {
        if (this.pageSize == null || this.pageSize <= 0) {
            return 0;
        }
        return (int) ((this.total + this.pageSize - 1) / this.pageSize);
    }
}
