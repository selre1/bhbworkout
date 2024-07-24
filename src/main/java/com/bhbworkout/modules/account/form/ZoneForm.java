package com.bhbworkout.modules.account.form;


import lombok.Data;

@Data
public class ZoneForm {

    //TODO Seoul(서울)/None
    private String zoneName;

    public String getCityName(){
        return zoneName.substring(0, zoneName.indexOf("("));
    }
    public String getProvinceName(){
        return zoneName.substring(zoneName.indexOf("/")+1);
    }

    public String getLocalNameOfCity(){
        return zoneName.substring(zoneName.indexOf("(")+1, zoneName.indexOf(")"));
    }
}
