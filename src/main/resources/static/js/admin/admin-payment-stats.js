// /js/admin/admin-payment-stats.js

document.addEventListener("DOMContentLoaded", function () {
    // Thymeleaf에서 전달된 dailyStats 데이터를 안전하게 가져오기
    const dailyData = window.CHART_DATA?.dailyStats || [];

    if (!dailyData.length) {
        console.warn("dailyStats 데이터가 없습니다.");
        return; // 데이터 없으면 차트 그리지 않음
    }

    // 라벨과 데이터 생성
    const labels = dailyData.map(d => d.date);
    const dataAmount = dailyData.map(d => d.dailyTotalAmount);
    const dataCount = dailyData.map(d => d.dailyCount);

    // 1. 일별 매출액 차트 (라인)
    const ctxAmount = document.getElementById("amountChart").getContext("2d");
    new Chart(ctxAmount, {
        type: "line",
        data: {
            labels: labels,
            datasets: [{
                label: "매출액 (원)",
                data: dataAmount,
                borderColor: "#007bff",
                backgroundColor: "rgba(0, 123, 255, 0.1)",
                borderWidth: 2,
                tension: 0.3,
                fill: true,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.parsed.y.toLocaleString() + "원";
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function (value) {
                            return value.toLocaleString();
                        }
                    }
                }
            }
        }
    });

    // 2. 일별 결제 건수 차트 (바)
    const ctxCount = document.getElementById("countChart").getContext("2d");
    new Chart(ctxCount, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [{
                label: "결제 건수 (건)",
                data: dataCount,
                backgroundColor: "rgba(233, 196, 106, 0.8)",
                borderColor: "#e9c46a",
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { stepSize: 1 }
                }
            }
        }
    });
});
