package com.example.high_five.dto;

import com.example.high_five.dto.payment.PaymentConfirmRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentConfirmRequestTest {

    @Test
    @DisplayName("PaymentConfirmRequest Builder 생성 테스트")
    void builderTest() {
        // given
        String paymentKey = "pay_key_123";
        String orderKey = "ord_key_456";
        Long amount = 50000L;

        // when
        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .paymentKey(paymentKey)
                .orderKey(orderKey)
                .amount(amount)
                .paymentMethod("CARD")
                .build();

        // then
        assertThat(request.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(request.getOrderKey()).isEqualTo(orderKey);
        assertThat(request.getAmount()).isEqualTo(amount);
        assertThat(request.getPaymentMethod()).isEqualTo("CARD");
    }

    @Test
    @DisplayName("PaymentConfirmRequest 기본 생성자 및 Getter 테스트")
    void noArgsConstructorTest() {
        // @NoArgsConstructor가 동작하는지 리플렉션이나 프레임워크 사용 시 중요
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        assertThat(request).isNotNull();
        // 초기값은 null
        assertThat(request.getPaymentKey()).isNull();
    }
}