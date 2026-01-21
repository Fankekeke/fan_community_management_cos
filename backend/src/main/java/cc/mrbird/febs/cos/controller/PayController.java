package cc.mrbird.febs.cos.controller;

import cc.mrbird.febs.common.domain.AlipayBean;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.Merchandise;
import cc.mrbird.febs.cos.entity.MerchandiseOrders;
import cc.mrbird.febs.cos.service.IMerchandiseOrdersService;
import cc.mrbird.febs.cos.service.IMerchandiseService;
import cc.mrbird.febs.cos.service.IOfflineEventOrdersService;
import cc.mrbird.febs.cos.service.IPayService;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/business/pay")
public class PayController {

    @Autowired
    private IPayService payService;

    @Autowired
    private IMerchandiseOrdersService merchandiseOrdersService;

    @Autowired
    private IOfflineEventOrdersService offlineEventOrdersService;

    @Autowired
    private IMerchandiseService merchandiseService;

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
                scenicOrder.setStatus(2);
                scenicOrder.setPaidAt(DateUtil.formatDateTime(new Date()));
                return R.ok(merchandiseOrdersService.updateById(scenicOrder));
            }
        } else if (orderCode.contains("ORDOFF-")) {
            OrderInfo orderInfo = orderInfoService.getOne(Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getCode, orderCode));
            if (orderInfo != null) {
                orderInfo.setDelFlag(0);
                return R.ok(orderInfoService.updateById(orderInfo));
            }
            // 定义二维码内容（可以是订单链接或订单号）
            String content = scenicOrder.getCode().toString(); // 替换为实际域名
            // 定义二维码保存路径
            String filePath = "G:/Project/20251103景区管理服务平台/db/";
            String fileName = "scenic_order_" +  scenicOrder.getCode() + ".png";
            scenicOrder.setQrCode(fileName);
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
                bitMatrix = writer.encode(content,
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
        }
        return R.ok(false);
    }

    /**
     * 景点订单支付
     * @param scenicOrder
     * @return
     */
    @PostMapping("/scenic")
    public R scenicAlipay(ScenicOrder scenicOrder) throws AlipayApiException {
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, scenicOrder.getUserId()));
        scenicOrder.setUserId(userInfo.getId());
        scenicOrder.setCode("ORD-" + System.currentTimeMillis());
        scenicOrder.setOrderStatus(1);
        scenicOrder.setCreateDate(DateUtil.formatDateTime(new Date()));
        scenicOrder.setDelFlag(1);
        scenicOrderService.save(scenicOrder);
        // 支付
        AlipayBean alipayBean = new AlipayBean();
        alipayBean.setOut_trade_no(scenicOrder.getCode());
        alipayBean.setSubject("景区门票");
        alipayBean.setTotal_amount(scenicOrder.getPrice().toString());
        alipayBean.setBody("景区门票");
        String result = payService.aliPay(alipayBean);
        return R.ok(result);
    }

    /**
     * 房间订单支付
     * @param orderInfo
     * @return
     * @throws AlipayApiException
     */
    @PostMapping("/room")
    public R roomAlipay(OrderInfo orderInfo) throws AlipayApiException {
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, orderInfo.getUserId()));
        orderInfo.setUserId(userInfo.getId());
        orderInfo.setCode("ORD-" + System.currentTimeMillis());
        orderInfo.setOrderStatus(0);
        orderInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        orderInfo.setDelFlag(1);
        orderInfo.setStartDate(DateUtil.formatDate(new Date()));
        orderInfo.setEndDate(DateUtil.formatDate(DateUtil.offsetDay(new Date(), orderInfo.getDayNum())));
        orderInfoService.save(orderInfo);
        // 支付
        AlipayBean alipayBean = new AlipayBean();
        alipayBean.setOut_trade_no(orderInfo.getCode());
        alipayBean.setSubject("房间价格");
        alipayBean.setTotal_amount(orderInfo.getPrice().toString());
        alipayBean.setBody("房间价格");
        String result = payService.aliPay(alipayBean);
        return R.ok(result);
    }

}
