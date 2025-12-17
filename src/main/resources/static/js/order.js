// ===========================
// 1. 서버 데이터 로딩
// ===========================
const {
    maxPoint = 0,
    tossClientKey,
    customerEmail,
    freeDeliveryThreshold = 30000,
    standardDeliveryFee = 3000
} = window.ORDER_ENV || {};

// ===========================
// 2. Toss 초기화
// ===========================
const tossPayments = TossPayments(tossClientKey);
let selectedMethodCode = null;

// 초기화
$(document).ready(function () {
    updateTotal();
});

// ===========================
// UI 로직
// ===========================
function updateTotal() {
    let productTotal = 0;
    let wrappingFee = 0;

    $('.order-item-row').each(function () {
        const price = parseInt($(this).find('.item-price').val()) || 0;
        let qty = parseInt($(this).find('.item-qty-input').val()) || 1;

        const rowTotal = price * qty;
        $(this).find('.item-total-price').text(rowTotal.toLocaleString());
        productTotal += rowTotal;

        const wrapperPrice =
            parseInt($(this).find('.wrapper-select option:selected').data('price')) || 0;
        wrappingFee += wrapperPrice * qty;
    });

    let deliveryFee = productTotal >= freeDeliveryThreshold ? 0 : standardDeliveryFee;

    const couponDiscount =
        parseInt($('#couponSelect option:selected').data('discount')) || 0;

    let usedPoint = parseInt($('#usedPoint').val()) || 0;

    if (usedPoint > maxPoint) {
        alert('보유 포인트를 초과할 수 없습니다.');
        usedPoint = maxPoint;
        $('#usedPoint').val(maxPoint);
    }

    const totalDiscount = couponDiscount + usedPoint;
    let finalPrice = productTotal + deliveryFee + wrappingFee - totalDiscount;
    if (finalPrice < 0) finalPrice = 0;

    $('#displayProductPrice').text(productTotal.toLocaleString());
    $('#displayDeliveryFee').text(deliveryFee.toLocaleString());
    $('#displayWrappingFee').text(wrappingFee.toLocaleString());
    $('#displayDiscount').text(totalDiscount.toLocaleString());
    $('#displayFinalPrice').text(finalPrice.toLocaleString());
}

// ===========================
// 포인트 전액 사용
// ===========================
function useAllPoints() {
    $('#usedPoint').val(maxPoint);
    updateTotal();
}

// ===========================
// 결제 요청
// ===========================
function requestPayment() {
    if (!selectedMethodCode) {
        alert("결제 수단을 선택해주세요.");
        return;
    }

    const formData = new FormData(document.getElementById('orderForm'));

    $.ajax({
        url: '/orders/api/create',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success(response) {
            tossPayments.requestPayment("카드", {
                amount: response.totalAmount,
                orderId: response.orderKey,
                orderName: "HIGH-FIVE 도서 주문",
                customerEmail: customerEmail,
                successUrl: location.origin + "/orders/success",
                failUrl: location.origin + "/orders/fail"
            });
        },
        error(xhr) {
            alert("주문 생성 실패");
        }
    });
}
