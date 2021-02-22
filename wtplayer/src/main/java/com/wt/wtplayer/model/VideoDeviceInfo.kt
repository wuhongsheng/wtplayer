package com.wt.wtplayer.model

/**
 * description
 *
 * @author whs
 * @date 2020/10/12
 */
class VideoDeviceInfo {
    /**
     * id : 1366a3d3c2c545dabf47efbbe5201a47
     * cateCode : 10-001-0001
     * name : NVRD23
     * ip : 192.168.180.56
     * port : 80
     * sort : 10638
     * createDt : 2020-07-30 00:39:56
     * createUser : 402201ce606e92c501606e93c4ec0000
     * updateDt : 2020-08-22 03:31:08
     * updateUser : 402201ce606e92c501606e93c4ec0000
     * basePositionList : null
     * baseCategory : null
     * relateObject : null
     * posCodeList : ["43100111012160000002"]
     * tbDeviceBaseInfo : null
     * deviceState : 1
     * manufacture : null
     * playType : null
     * decodeType : null
     * deviceUniformID : 43100111011320000024
     */
    var id: String? = null
    var cateCode: String? = null
    var name: String? = null
    var ip: String? = null
    var port = 0
    var sort = 0
    var createDt: String? = null
    var createUser: String? = null
    var updateDt: String? = null
    var updateUser: String? = null
    var basePositionList: Any? = null
    var baseCategory: Any? = null
    var relateObject: Any? = null
    var tbDeviceBaseInfo: Any? = null
    var deviceState = 0
    var manufacture: Any? = null
    var playType: Any? = null
    var decodeType: Any? = null
    var deviceUniformID: String? = null
    var posCodeList: List<String>? = null

}