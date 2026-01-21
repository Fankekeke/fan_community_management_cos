package cc.mrbird.febs.cos.controller;

import cc.mrbird.febs.common.domain.AlipayBean;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.*;
import cc.mrbird.febs.cos.service.*;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.RoundingMode;
import java.util.Date;

@RestController
@RequestMapping("/cos/pay")
public class PayController {

    @Autowired
    private IPayService payService;

    @Autowired
    private IMerchandiseOrdersService merchandiseOrdersService;

    @Autowired
    private IOfflineEventOrdersService offlineEventOrdersService;

    @Autowired
    private IMerchandiseService merchandiseService;

    @Autowired
    private IUserInfoService userInfoService;

    @Autowired
    private IOfflineEventsService offlineEventsService;

    /**
     * 阿里支付
     *
     * @param subject
     * @param body
     * @return
     * @throws AlipayApiException
     */
    @PostMapping(value = "/alipay")
    public R alipay(String outTradeNo, String subject, String totalAmount, String body) throws AlipayApiException {
        AlipayBean alipayBean = new AlipayBean();
        alipayBean.setOut_trade_no(outTradeNo);
        alipayBean.setSubject(subject);
        alipayBean.setTotal_amount(totalAmount);
        alipayBean.setBody(body);
        String result = payService.aliPay(alipayBean);
        return R.ok(result);
    }


    /**
     * 订单支付状态修改
     *
     * @param orderCode
     * @return
     */
    @GetMapping("/orderAudit")
    public R audit(String orderCode) {
        // 判断订单所属
        if (orderCode.contains("ORDM-")) {
            MerchandiseOrders scenicOrder = merchandiseOrdersService.getOne(Wrappers.<MerchandiseOrders>lambdaQuery().eq(MerchandiseOrders::getOrderSn, orderCode));
            if (scenicOrder != null) {
                Merchandise merchandise = merchandiseService.getById(scenicOrder.getMerchandiseId());
                merchandise.setStock(merchandise.getStock() - 1);
                merchandiseService.updateById(merchandise);
                scenicOrder.setStatus(3);
                scenicOrder.setPaidAt(DateUtil.formatDateTime(new Date()));
                return R.ok(merchandiseOrdersService.updateById(scenicOrder));
            }
        } else if (orderCode.contains("ORDOFF-")) {
            OfflineEventOrders orderInfo = offlineEventOrdersService.getOne(Wrappers.<OfflineEventOrders>lambdaQuery().eq(OfflineEventOrders::getOrderSn, orderCode));
            if (orderInfo != null) {
                orderInfo.setPaymentStatus(1);
                // 定义二维码保存路径
                String filePath = "G:/Project/20260115智能粉丝社群数字化运营平台/db/";
                String fileName = "order_" + orderCode + ".png";
                orderInfo.setCheckCode(fileName);
                String fullPath = filePath + fileName;
                // 创建目录（如果不存在）
                java.io.File directory = new java.io.File(filePath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                // 生成二维码
                com.google.zxing.Writer writer = new com.google.zxing.qrcode.QRCodeWriter();
                com.google.zxing.common.BitMatrix bitMatrix = null;
                try {
                    bitMatrix = writer.encode(orderCode,
                            com.google.zxing.BarcodeFormat.QR_CODE, 300, 300);

                    // 保存为图片文件
                    java.nio.file.Path path = java.nio.file.Paths.get(fullPath);
                    java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(300, 300, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    for (int x = 0; x < 300; x++) {
                        for (int y = 0; y < 300; y++) {
                            image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                        }
                    }
                    javax.imageio.ImageIO.write(image, "PNG", path.toFile());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                OfflineEvents eventInfo = offlineEventsService.getOne(Wrappers.<OfflineEvents>lambdaQuery().eq(OfflineEvents::getId, orderInfo.getEventId()));
                eventInfo.setRemainingCapacity(eventInfo.getRemainingCapacity() - 1);
                offlineEventsService.updateById(eventInfo);
                return R.ok(offlineEventOrdersService.updateById(orderInfo));
            }

        }
        return R.ok(false);
    }

    /**
     * 商品订单支付
     *
     * @param merchandiseOrders
     * @return
     */
    @PostMapping("/merchandise")
    public R scenicAlipay(MerchandiseOrders merchandiseOrders) throws AlipayApiException {
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, merchandiseOrders.getUserId()));
        merchandiseOrders.setUserId(userInfo.getId());
        merchandiseOrders.setOrderSn("ORDM-" + System.currentTimeMillis());
        merchandiseOrders.setStatus(0);
        merchandiseOrders.setCreatedAt(DateUtil.formatDateTime(new Date()));
        merchandiseOrdersService.save(merchandiseOrders);
        // 支付
        AlipayBean alipayBean = new AlipayBean();
        alipayBean.setOut_trade_no(merchandiseOrders.getOrderSn());
        alipayBean.setSubject("商品价格");
        alipayBean.setTotal_amount(merchandiseOrders.getTotalAmount().setScale(1, RoundingMode.HALF_UP).toString());
        alipayBean.setBody("商品价格");
        String result = payService.aliPay(alipayBean);
        return R.ok(result);
    }

    /**
     * 线下活动门票订单支付
     *
     * @param orderInfo
     * @return
     * @throws AlipayApiException
     */
    @PostMapping("/offline")
    public R roomAlipay(OfflineEventOrders orderInfo) throws AlipayApiException {
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, orderInfo.getUserId()));
        orderInfo.setUserId(userInfo.getId());
        orderInfo.setOrderSn("ORDOFF-" + System.currentTimeMillis());
        orderInfo.setCreatedAt(DateUtil.formatDateTime(new Date()));
        orderInfo.setPaymentStatus(0);
        offlineEventOrdersService.save(orderInfo);
        // 支付
        AlipayBean alipayBean = new AlipayBean();
        alipayBean.setOut_trade_no(orderInfo.getOrderSn());
        alipayBean.setSubject("线下活动门票");
        alipayBean.setTotal_amount(orderInfo.getOrderPrice().setScale(1, RoundingMode.HALF_UP).toString());
        alipayBean.setBody("线下活动门票");
        String result = payService.aliPay(alipayBean);
        return R.ok(result);
    }

}
