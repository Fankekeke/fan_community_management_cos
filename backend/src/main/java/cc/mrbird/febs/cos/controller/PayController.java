package cc.mrbird.febs.cos.controller;

import cc.mrbird.febs.common.domain.AlipayBean;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.service.IPayService;
import com.alipay.api.AlipayApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/pay")
public class PayController {

    @Autowired
    private IPayService payService;

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

    public R updateOrderStatus() {
        return R.ok();
    }

}
