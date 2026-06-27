const API = '';
let currentUser = null;
let currentStock = null;
let refreshInterval = null;

// ─── Auth ────────────────────────────────────────────────────────────────────

function switchTab(tab) {
    document.querySelectorAll('.auth-tabs .tab-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
    document.getElementById('loginForm').style.display = tab === 'login' ? 'block' : 'none';
    document.getElementById('registerForm').style.display = tab === 'register' ? 'block' : 'none';
    document.getElementById('authError').textContent = '';
}

async function register() {
    const username = document.getElementById('regUsername').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    if (!username || !email) return setAuthError('Please fill all fields');

    const res = await fetch(`${API}/api/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, email })
    });
    const data = await res.json();
    if (!res.ok) return setAuthError(data.error);
    loginAs(data);
}

async function login() {
    const username = document.getElementById('loginUsername').value.trim();
    if (!username) return setAuthError('Enter your username');

    const res = await fetch(`${API}/api/users/username/${username}`);
    if (!res.ok) return setAuthError('User not found. Please register first.');
    const data = await res.json();
    loginAs(data);
}

function loginAs(user) {
    currentUser = user;
    localStorage.setItem('userId', user.id);
    document.getElementById('authScreen').classList.remove('active');
    document.getElementById('appScreen').classList.add('active');
    document.getElementById('navUsername').textContent = user.username;
    updateNavBalance(user.balance);
    startApp();
}

function setAuthError(msg) {
    document.getElementById('authError').textContent = msg;
}

// ─── App ─────────────────────────────────────────────────────────────────────

function startApp() {
    loadMarket();
    refreshInterval = setInterval(loadMarket, 10000); // Refresh every 10s
}

function showTab(tab) {
    document.querySelectorAll('.tabs .tab-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    document.getElementById(tab + 'Tab').classList.add('active');
    if (tab === 'portfolio') loadPortfolio();
    if (tab === 'history') loadHistory();
}

// ─── Market ──────────────────────────────────────────────────────────────────

async function loadMarket() {
    const res = await fetch(`${API}/api/stocks`);
    const stocks = await res.json();
    const grid = document.getElementById('marketGrid');
    grid.innerHTML = stocks.map(s => {
        const change = s.change || (s.currentPrice - s.openPrice);
        const changePct = s.changePercent || (s.openPrice ? ((change / s.openPrice) * 100) : 0);
        const cls = change >= 0 ? 'positive' : 'negative';
        const sign = change >= 0 ? '+' : '';
        return `
        <div class="stock-card" onclick="openTradeModal('${s.symbol}', ${s.currentPrice})">
            <div class="stock-symbol">${s.symbol}</div>
            <div class="stock-name">${s.name}</div>
            <div class="stock-price">$${s.currentPrice.toFixed(2)}</div>
            <div class="stock-change ${cls}">${sign}$${change.toFixed(2)} (${sign}${changePct.toFixed(2)}%)</div>
            <div class="stock-sector">${s.sector}</div>
        </div>`;
    }).join('');
}

// ─── Trade Modal ─────────────────────────────────────────────────────────────

function openTradeModal(symbol, price) {
    currentStock = { symbol, price };
    document.getElementById('modalTitle').textContent = `Trade ${symbol}`;
    document.getElementById('modalPrice').textContent = `Current Price: $${price.toFixed(2)}`;
    document.getElementById('modalBalance').textContent = `Cash Balance: $${currentUser.balance.toFixed(2)}`;
    document.getElementById('tradeQty').value = '';
    document.getElementById('tradeTotal').textContent = '';
    document.getElementById('tradeError').textContent = '';
    document.getElementById('tradeModal').style.display = 'flex';

    document.getElementById('tradeQty').oninput = () => {
        const qty = parseInt(document.getElementById('tradeQty').value) || 0;
        const total = qty * price;
        document.getElementById('tradeTotal').textContent = qty > 0 ? `Total: $${total.toFixed(2)}` : '';
    };
}

function closeModal() {
    document.getElementById('tradeModal').style.display = 'none';
}

async function confirmTrade(type) {
    const qty = parseInt(document.getElementById('tradeQty').value);
    if (!qty || qty <= 0) return setTradeError('Enter a valid quantity');

    const endpoint = type === 'buy' ? '/api/trade/buy' : '/api/trade/sell';
    const res = await fetch(`${API}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: currentUser.id, symbol: currentStock.symbol, quantity: qty })
    });
    const data = await res.json();
    if (!res.ok) return setTradeError(data.error);

    // Refresh user balance
    const userRes = await fetch(`${API}/api/users/${currentUser.id}`);
    currentUser = await userRes.json();
    updateNavBalance(currentUser.balance);

    closeModal();
    showSuccessToast(`${type === 'buy' ? 'Bought' : 'Sold'} ${qty} share(s) of ${currentStock.symbol}`);
}

function setTradeError(msg) {
    document.getElementById('tradeError').textContent = msg;
}

// ─── Portfolio ────────────────────────────────────────────────────────────────

async function loadPortfolio() {
    const res = await fetch(`${API}/api/trade/portfolio/${currentUser.id}`);
    const data = await res.json();

    const pnlClass = data.pnl >= 0 ? 'positive' : 'negative';
    const sign = data.pnl >= 0 ? '+' : '';

    document.getElementById('portfolioSummary').innerHTML = `
        <div class="summary-card">
            <div class="summary-label">Cash Balance</div>
            <div class="summary-value">$${data.cashBalance.toFixed(2)}</div>
        </div>
        <div class="summary-card">
            <div class="summary-label">Portfolio Value</div>
            <div class="summary-value">$${data.portfolioValue.toFixed(2)}</div>
        </div>
        <div class="summary-card">
            <div class="summary-label">Total Value</div>
            <div class="summary-value">$${data.totalValue.toFixed(2)}</div>
        </div>
        <div class="summary-card">
            <div class="summary-label">P&L</div>
            <div class="summary-value ${pnlClass}">${sign}$${data.pnl.toFixed(2)} (${sign}${data.pnlPercent.toFixed(2)}%)</div>
        </div>
    `;

    if (data.holdings.length === 0) {
        document.getElementById('holdingsGrid').innerHTML = '<p style="color:#8b949e">No holdings yet. Buy some stocks!</p>';
        return;
    }

    document.getElementById('holdingsGrid').innerHTML = data.holdings.map(h => {
        const glClass = h.gainLoss >= 0 ? 'positive' : 'negative';
        const sign = h.gainLoss >= 0 ? '+' : '';
        return `
        <div class="holding-card">
            <div>
                <div class="holding-symbol">${h.symbol}</div>
                <div class="holding-name">${h.name}</div>
            </div>
            <div><div style="font-size:0.8rem;color:#8b949e">Qty</div><div>${h.quantity}</div></div>
            <div><div style="font-size:0.8rem;color:#8b949e">Avg Buy</div><div>$${h.avgBuyPrice.toFixed(2)}</div></div>
            <div><div style="font-size:0.8rem;color:#8b949e">Current</div><div>$${h.currentPrice.toFixed(2)}</div></div>
            <div class="${glClass}">${sign}$${h.gainLoss.toFixed(2)}<br><span style="font-size:0.8rem">${sign}${h.gainLossPercent.toFixed(2)}%</span></div>
        </div>`;
    }).join('');
}

// ─── History ──────────────────────────────────────────────────────────────────

async function loadHistory() {
    const res = await fetch(`${API}/api/trade/transactions/${currentUser.id}`);
    const txs = await res.json();
    if (txs.length === 0) {
        document.getElementById('txBody').innerHTML = '<tr><td colspan="6" style="color:#8b949e;text-align:center">No transactions yet</td></tr>';
        return;
    }
    document.getElementById('txBody').innerHTML = txs.map(tx => {
        const badge = tx.type === 'BUY' ? 'badge-buy' : 'badge-sell';
        const date = new Date(tx.timestamp).toLocaleString();
        return `
        <tr>
            <td>${date}</td>
            <td><span class="${badge}">${tx.type}</span></td>
            <td><b>${tx.stockSymbol}</b> <span style="color:#8b949e;font-size:0.8rem">${tx.stockName}</span></td>
            <td>${tx.quantity}</td>
            <td>$${tx.pricePerShare.toFixed(2)}</td>
            <td>$${tx.totalAmount.toFixed(2)}</td>
        </tr>`;
    }).join('');
}

// ─── Utils ────────────────────────────────────────────────────────────────────

function updateNavBalance(balance) {
    document.getElementById('navBalance').textContent = `$${balance.toFixed(2)}`;
}

function showSuccessToast(msg) {
    const toast = document.createElement('div');
    toast.textContent = '✅ ' + msg;
    toast.style.cssText = 'position:fixed;bottom:24px;right:24px;background:#238636;color:white;padding:12px 20px;border-radius:8px;font-size:0.9rem;z-index:300;';
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Auto-login if session exists
window.onload = () => {
    const savedId = localStorage.getItem('userId');
    if (savedId) {
        fetch(`${API}/api/users/${savedId}`).then(r => r.json()).then(u => {
            if (u.id) loginAs(u);
        }).catch(() => {});
    }
};
