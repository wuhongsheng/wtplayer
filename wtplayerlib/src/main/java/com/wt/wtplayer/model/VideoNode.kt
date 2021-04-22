package com.wt.wtplayer.model

import java.io.Serializable

/**
 * 视频树节点
 *
 * @author whs
 * @date 2020/9/18
 */
class VideoNode : Serializable {
    /**
     * id : 8a0cc9f8ee76485f9eea0fd028c329a1
     * code : 43100111012160000005
     * title : 内蒙古公路交通投资发展有限公司
     * expand : true
     * iconSkin : null
     * isVideoTag : null
     * type : 1
     * deviceType : 1
     * status : null
     * videotype : null
     * sort : 1
     * checked : null
     * vcontrol : null
     * equipType : null
     * playType : null
     * payloadType : null
     * deviceUniformID : null
     * isParent : null
     * ext : null
     * loading : false
     * rtsp : null
     * rank : 0
     * isCollection : false
     * height : null
     * width : null
     * infoboardType : null
     * children : []
     * areaCode : 43010102
     * audioEnable : null
     * onLiveCount : null
     * count : null
     * longitude : null
     * latitude : null
     * positionAreaType : 0
     * mapDisplay : 1
     * pcode : null
     * mrtsp : null
     */
    var id: String? = null
    var deviceCode:String? = null
    //用户id
    var userId:String? = null
    //上次播放时间
    var playTime:String? = null
    var code: String? = null
    var title: String? = null
    var isExpand = false
    var iconSkin: Any? = null
    var isVideoTag: Any? = null
    //节点类型 1位置 2设备 3 本地信号源
    var type = 0
    //设备类型 1摄像机 2情报板
    var deviceType = 0
    //1:在线 0:离线
    var status = 0
    // 1 球机 2 枪机  3 半球
    var videotype: Int? = null
    var videoType:Int? = null

    var sort = 0
    var checked: Boolean = false
    //允许云台控制：0-否；1-是
    var vcontrol: Int? = null
    var equipType: Any? = null
    var playType: Any? = null
    var payloadType: Any? = null
    //设备国标ID
    var deviceUniformID: String? = null
    var isParent: Any? = null
    var ext: Any? = null
    var isLoading = false
    var rtsp: String? = null
    var rank = 0
    var isIsCollection = false
        private set
    var height: Any? = null
    var width: Any? = null
    var infoboardType: Any? = null
    var areaCode: String? = null
    var audioEnable: Any? = null
    //在线数量
    var onLiveCount: Int? = null
    //总数量
    var count: Int? = null
    var longitude: Any? = null
    var latitude: Any? = null
    var positionAreaType = 0
    var mapDisplay = 0
    //父节点编码
    var pcode: String? = null
    var mrtsp: Any? = null
    //是否选中
    var isCheck: Boolean = false

    var children: List<VideoNode>? = null
    //收藏列表的设备详情
    var baseDeviceInfo: VideoDeviceInfo? = null
    //历史设备信息
    var tbDeviceBaseInfo: HisVideoDeviceInfo? = null

    fun setIsCollection(isCollection: Boolean) {
        isIsCollection = isCollection
    }
    //是否是最后
    var isLast:Boolean = false
    //搜索显示位置
    var deptName:String?= null



}