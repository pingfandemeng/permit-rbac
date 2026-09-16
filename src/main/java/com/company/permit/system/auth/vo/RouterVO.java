package com.company.permit.system.auth.vo;

import com.company.permit.system.menu.vo.MenuVO;
import lombok.Data;

import java.util.List;

@Data
public class RouterVO {
    private String path;
    private String name;
    private String component;
    private Boolean hidden;
    private MetaVO meta;
    private List<RouterVO> children;

    @Data
    public static class MetaVO {
        private String title;
        private String icon;
        private Boolean noCache;
    }
}
