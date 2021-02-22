package com.wt.wtplayer.model

/**
 * description
 *
 * @author whs
 * @date 2020/10/12
 */
class VideoPlateForm {
    /**
     * ip : 192.162.130.251
     * port : 15130
     * extranetIp : 220.178.67.247
     * isInnerIp : 0
     * extranetPort : 15111
     * username : admin
     * password : 12345678
     * version : 1.0.3.8
     * fileName : WTGB_ActiveX_V1038.exe
     * dwLinkMode : 1
     * hardwaredecode : 0
     * dwStreamType : 1
     */
    var ip: String? = null
    var port = 0
    var extranetIp: String? = null
    var isInnerIp: String? = null
    var extranetPort = 0
    var username: String? = null
    var password: String? = null
    var version: String? = null
    var fileName: String? = null
    var dwLinkMode: String? = null
    var hardwaredecode: String? = null
    var dwStreamType: String? = null
    //网关地址
    var serviceHttpAddr: String? = null
    //网关端口
    var serviceHttpPort: String? = null


}