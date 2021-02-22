package com.wt.wtplayer.model;

import java.util.List;

/**
 * description
 *
 * @author whs
 * @date 2020/9/18
 */
public class LoginInfoBean {


    /**
     * userId : 402201ce606e92c501606e93c4ec0000
     * username : 管理员
     * account : admin
     * mobile : 13855559999
     * orgName : 陕西1
     * orgId : 1
     * token : 7bdb1e79d37240af84a57e9592e91246
     * orgType : 1
     * type : 1
     * menus : []
     */

    private String userId;
    private String username;
    private String account;
    private String mobile;
    private String orgName;
    private String orgId;
    private String token;
    private int orgType;
    private int type;

    public boolean isControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(boolean controlFlag) {
        this.controlFlag = controlFlag;
    }

    //是否拥有云台控制权限 true-是 false-否
    private boolean controlFlag;
    private List<?> menus;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getOrgType() {
        return orgType;
    }

    public void setOrgType(int orgType) {
        this.orgType = orgType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<?> getMenus() {
        return menus;
    }

    public void setMenus(List<?> menus) {
        this.menus = menus;
    }
}
