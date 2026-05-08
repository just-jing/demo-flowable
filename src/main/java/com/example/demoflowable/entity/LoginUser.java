package com.example.demoflowable.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {

    private Long id;

    private String username;

    public static LoginUser getDefaultUser() {
        return new LoginUser(1000L, "小明");
    }
}
