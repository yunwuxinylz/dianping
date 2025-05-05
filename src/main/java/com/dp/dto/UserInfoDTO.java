package com.dp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserInfoDTO {
    private Long id;
    private String nickName;
    private String icon;
    private LocalDate birthday;
    private Boolean gender;
    private String introduce;
    private String city;
    private String email;
}
