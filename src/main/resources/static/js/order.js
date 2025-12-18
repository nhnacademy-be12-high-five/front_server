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
if (!TOSS_CLIENT_KEY || typeof TOSS_CLIENT_KEY !== "string") {
    console.error("Toss Client Key 누락", TOSS_CLIENT_KEY);
}

const tossPayments = TossPayments(TOSS_CLIENT_KEY);

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
// 결제 수단 선택
// ===========================
function selectMethod(element) {
    if ($(element).hasClass("disabled")) return;

    if ($(element).hasClass("active")) {
        $(element).removeClass("active");
        selectedMethodCode = null;
        $('#paymentMethodInput').val("");
        $('#method-desc').text("결제 수단을 선택해주세요.");
        return;
    }

    $('.tab').removeClass('active');
    $(element).addClass('active');

    selectedMethodCode = $(element).data("code");
    $('#paymentMethodInput').val(selectedMethodCode);

    const descMap = {
        TOSS: "Toss 위젯을 통해 빠르게 결제 해보세요.",
        BANK_TRANSFER: "실시간 계좌이체를 진행합니다.",
        VIRTUAL_ACCOUNT: "가상계좌를 발급받아 입금 후 결제 완료됩니다."
    };

    $('#method-desc').text(descMap[selectedMethodCode] ?? "선택한 결제 수단으로 진행합니다.");
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
