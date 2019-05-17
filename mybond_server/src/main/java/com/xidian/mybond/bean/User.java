package com.xidian.mybond.bean;

/**
 * Created by Zch On 2019/3/17 22:38
 **/
public class User {

    private String userName;  //用户名
    private String password; //密码
    private String email;  //邮箱
    private String phone;  //电话


    public User(){

    }

    public User(String userName, String password, String email, String phone) {
        this.userName= userName;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}