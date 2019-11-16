package com.jeeplus.modules.marguerite.zongwang.common.WxPay;

/**
 * @Function
 * @Author chaihu
 * @Date 2019/11/1 20:13
 * @Place 29
 * @Version 1.0.0
 * @Copyright GGH
 */
public class WxPayConfig {

    /**
     * appId
     */
//    private final static  String  appId = "wx6bad5438ea13df65";
    public static final String appId = "wx6bad5438ea13df65";

    /**
     *
     */
//    private final static String appSecret = "d973431c28dfc07d4aa266430aaa294b";
    public static final String appSecret = "d973431c28dfc07d4aa266430aaa294b";

    /**
     * 商户号
     */
//    private final static String mchId="1561274141";
    public static final String mchId = "1561274141";


//    private final static String partnerKey ="91430321MA4QMJD101zw18711158885X";

    public static final String partnerKey = "zhongwang18711158885ZW91430321MA";

    private final static String certPath ="";
    private final static String notifyUrl = "http://265815d17h.zicp.vip:35520/weixin/wx/notify_url";

    public static String getAppId() {
        return appId;
    }

    public static String getAppSecret() {
        return appSecret;
    }

    public static String getMchId() {
        return mchId;
    }

    public static String getPartnerKey() {
        return partnerKey;
    }

    public static String getCertPath() {
        return certPath;
    }

    public static String getNotifyUrl() {
        return notifyUrl;
    }
}
