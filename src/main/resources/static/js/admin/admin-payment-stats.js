// ===========================
// 1. 데이터 로딩
// ===========================
const dailyData = (window.CHART_DATA && window.CHART_DATA.dailyStats) || [];

if (!dailyData.length) {
    console.warn("차트 데이터가 없습니다.");
}

// ===========================
// 2. 데이터 가공
// ===========================
const labels = dailyData.map(d => d.date);
const dataAmount = dailyData.map(d => d.dailyTotalAmount);
const dataCount = dailyData.map(d => d.dailyCount);

// ===========================
// 3. 매출액 차트 (Line)
// ===========================
const amountCanvas = document.getElementById('amountChart');
if (amountCanvas) {
    const ctxAmount = amountCanvas.getContext('2d');

    new Chart(ctxAmount, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                label: '매출액 (원)',
                data: dataAmount,
                borderColor: '#007bff',
                backgroundColor: 'rgba(0, 123, 255, 0.1)',
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
                        label: ctx =>
                            ctx.parsed.y.toLocaleString() + '원'
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: v => v.toLocaleString()
                    }
                }
            }
        }
    });
}

// ===========================
// 4. 결제 건수 차트 (Bar)
// ===========================
const countCanvas = document.getElementById('countChart');
if (countCanvas) {
    const ctxCount = countCanvas.getContext('2d');

    new Chart(ctxCount, {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label: '결제 건수 (건)',
                data: dataCount,
                backgroundColor: 'rgba(233, 196, 106, 0.8)',
                borderColor: '#e9c46a',
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
}
