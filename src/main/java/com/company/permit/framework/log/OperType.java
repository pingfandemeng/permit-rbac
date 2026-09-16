package com.company.permit.framework.log;

import lombok.Getter;

@Getter
public enum OperType {
    OTHER("0"),
    INSERT("1"),
    UPDATE("2"),
    DELETE("3"),
    QUERY("4"),
    EXPORT("5");

    private final String code;

    OperType(String code) {
        this.code = code;
    }
}
