package com.jeeplus.modules.marguerite.zongwang.web.wxPay;

import com.ijpay.core.enums.SignType;
import com.ijpay.core.enums.TradeType;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.IpKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.WxPayApiConfigKit;
import com.ijpay.wxpay.model.UnifiedOrderModel;
import com.jeeplus.common.utils.Hutool;
import com.jeeplus.common.utils.StringUtils;
import com.jeeplus.modules.marguerite.order.entity.MgltOrderTable;
import com.jeeplus.modules.marguerite.zongwang.common.WxPay.WxPayConfig;
import com.jeeplus.modules.marguerite.zongwang.common.json.Body;
import com.jeeplus.modules.marguerite.zongwang.service.OrderTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Function 微信支付
 * @Author chaihu
 * @Date 2019/11/4 10:27
 * @Place 29
 * @Version 1.0.0
 * @Copyright GGH
 */
@Controller
@RequestMapping("/weixin")
public class WxPayController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OrderTableService mgltOrderTableService;


    /**
     * 微信APP支付
     */
    @RequestMapping(value = "/weiXinAppPay", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Body weiXinAppPay(
            HttpServletRequest request,
            @RequestParam("orderNum") String orderNum
    ) {
        MgltOrderTable orderTable = mgltOrderTableService.findOrderByOrderNum(orderNum);
        if (orderTable == null) {
            return Body.newInstance(5025, "未查询到该订单！");
        }
        if (orderTable.getOrderStatus() == 1) {
            return Body.newInstance(5026, "订单已支付！");
        }

        String ip = IpKit.getRealIp(request);
        if (StringUtils.isBlank(ip)) {
            ip = "127.0.0.1";
        }
        // 获得订单的金额和订单的编号
        String orderPrice = String.valueOf((int) orderTable.getOrderPrice().doubleValue() * 100);
        String payOrderNum = "Wx" + orderTable.getOrderNum();

        Map<String, String> params = UnifiedOrderModel
                .builder()
                .appid(WxPayConfig.getAppId())
                .mch_id(WxPayConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .body("众旺")
//                .out_trade_no(payOrderNum)
                .out_trade_no(WxPayKit.generateStr())
                .total_fee("100")//金额
                .spbill_create_ip(ip)
                .notify_url(WxPayConfig.getNotifyUrl())
                .trade_type(TradeType.APP.getTradeType())
                .attach(orderNum)
                .build()
                .createSign(WxPayConfig.getPartnerKey(), SignType.MD5);
        log.info(params.toString());
        String xmlResult = WxPayApi.pushOrder(false, params);

        log.info("%%%%%%%%%%%%%%%%%%%%" + xmlResult);
        Map<String, String> result = WxPayKit.xmlToMap(xmlResult);

        String returnCode = result.get("return_code");
        String returnMsg = result.get("return_msg");

        log.info(returnCode + "%%%%%%%%%%%" + returnMsg);
        if (!WxPayKit.codeIsOk(returnCode)) {
//            return new AjaxResult().addError(returnMsg);
            return Body.newInstance(5027, returnMsg);
        }
        String resultCode = result.get("result_code");
        if (!WxPayKit.codeIsOk(resultCode)) {
//            return new AjaxResult().addError(returnMsg);
            return Body.newInstance(5027, returnMsg);
        }
        // 以下字段在 return_code 和 result_code 都为 SUCCESS 的时候有返回
        String prepayId = result.get("prepay_id");

        Map<String, String> packageParams = WxPayKit.appPrepayIdCreateSign(WxPayConfig.getAppId(), WxPayConfig.getMchId(), prepayId,
                WxPayConfig.getPartnerKey(), SignType.MD5);

//        String jsonStr = JSON.toJSONString(packageParams);
        log.info("返回apk的参数:" + packageParams);
//        return new AjaxResult().success(packageParams);

        return Body.newInstance(packageParams);
    }

    /**
     * @param request
     * @param response
     * @throws Exception
     */

    //(支付成功后第一步，拿到商家数据包，查询订单信息，废除优惠券,更新商家月销,更改订单状态和支付方式,更新流水)
    @PostMapping("/wx/notify_url")
    public String getTenPayNotif(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String xmlMsg = HttpKit.readData(request);
        log.info("支付通知=" + xmlMsg);
        Map<String, String> params = WxPayKit.xmlToMap(xmlMsg);

        String resultCode = params.get("result_code");


        // 注意重复通知的情况，同一订单号可能收到多次通知，请注意一定先判断订单状态

        if (WxPayKit.verifyNotify(params, WxPayApiConfigKit.getWxPayApiConfig().getPartnerKey())) {
            if (WxPayKit.codeIsOk(resultCode)) {
                //获取数据包
//                Integer foodorderid = Integer.parseInt(params.get("attach"));
                String orderNum = params.get("attach");
                System.out.println("orderNum订单++++++++++" + orderNum);

                // 修改订单状态
                MgltOrderTable orderTable = new MgltOrderTable();
                orderTable.setOrderNum(orderNum);
                orderTable.setOrderStatus(1);
                orderTable.setUpdateDate(new Date());
                if(mgltOrderTableService.updateOrderInfoByOrderNum(orderTable)> 0){
                    // 做佣金给与
                }



                //订单信息
//                EntityWrapper<Foodorder> wrapper = new EntityWrapper<>();
//                wrapper.eq("foodorderid", foodorderid);
//                Foodorder foodorder = new Foodorder();
//                foodorder.selectOne(wrapper);
                //判断订单状态
//                if (foodorder.getBuyerStatus() == 0) {
//                    foodorder.setBuyerStatus(1);//已支付
//                    foodorder.setPaymentMethod(2);//付款方式支付宝，1支付宝，2微信
//                    foodorder.setOrderstatus(1);//订单状态待收货

                //处理优惠券
//                    if (foodorder.getCouponId() != null) {
//                        //处理优惠券
//                        Integer userId = foodorder.getUserid();// 获取用户id
//                        Integer discountcouponid = foodorder.getCouponId();//获取优惠券id
//                        couponsusermapper.couponstatus(discountcouponid, userId);//用户使用过后，废除优惠券
//                    }

//                    //处理商家月销
//                    Shop selectById = shopmapper.selectById(foodorder.getSellerId());
//                    selectById.setYuexiao(selectById.getYuexiao() + 1);
//                    shopmapper.updateById(selectById);

//                    //处理交易流水
//                    Integer update = foodordermapper.updateById(foodorder);
//                    if (update > 0) {
//                        Accountorder accountOrder = new Accountorder();
//                        accountOrder.setOrderno(foodorderid.toString());            //订单号
//                        accountOrder.setOutid(foodorder.getBuyerId() + "");        //付款用户id
//                        accountOrder.setOuttype(0);                            //付款用户类型        付款用户类型 0买家用户 1商家 2骑手 3平台账户
//                        accountOrder.setInId("000000");                        //进账用户id这里是平台
//                        accountOrder.setTransactiontype(3);                    //进账账户类型        进账用户类型 0买家用户 1商家 2骑手 3平台账户4跑腿
//                        accountOrder.setInaccount("18725621293");            //进账号码
//                        accountOrder.setTransactionamount(foodorder.getDisbursements());//交易金额
//                        accountOrder.setPaytype(1);                            //交易方式   交易类型 1微信 2支付宝 3银联
//                        accountOrder.setTransactiontype(0);                    //备注交易类型 0下单 1提现 2充值会员 3退款
//                        accountOrder.setAddtime(Hutool.parseDateToString());//添加时间
//                        accountordermapper.insert(accountOrder);            //存储
//                    }
//                }

                // 发送微信通知
                Map<String, String> xml = new HashMap<String, String>(2);
                xml.put("return_code", "SUCCESS");
                xml.put("return_msg", "OK");
                return WxPayKit.toXml(xml);
            }
        }
        return null;
    }


}
