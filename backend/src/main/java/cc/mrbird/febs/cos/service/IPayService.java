package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.common.domain.AlipayBean;
import com.alipay.api.AlipayApiException;

public interface IPayService {

    /**
     * 支付宝支付接口
     * @param alipayBean
     * @return
     * @throws AlipayApiException
     */
    String aliPay(AlipayBean alipayBean) throws AlipayApiException;
}
