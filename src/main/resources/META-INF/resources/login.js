// ===== LOGIN SCRIPT =====

let currentStage = 'selection';

// ===== STAGE MANAGEMENT =====
function showStage(stageName) {
    // Hide all stages
    document.querySelectorAll('.stage').forEach(stage => {
        stage.classList.remove('active');
    });

    // Show selected stage
    const stage = document.getElementById(`stage-${stageName}`);
    if (stage) {
        stage.classList.add('active');
        currentStage = stageName;
        
        // Reset error message
        hideError();
    }
}

function goBack() {
    showStage('selection');
}

// ===== METHOD SELECTION =====
document.querySelectorAll('.method-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        const method = this.dataset.method;
        showStage(method.toLowerCase());
        
        // Generate QR code if QRCODE method
        if (method === 'QRCODE') {
            generateQRCode();
        }
        
        // Focus on input if TOTP
        if (method === 'TOTP') {
            setTimeout(() => {
                document.getElementById('totp-input').focus();
            }, 100);
        }
        
        // Focus on input if SENHA
        if (method === 'SENHA') {
            setTimeout(() => {
                document.getElementById('username-input').focus();
            }, 100);
        }
    });
});

// ===== QRCODE =====
function generateQRCode() {
    // Simulação de geração de QR Code usando canvas
    const canvas = document.getElementById('qrcode-canvas');
    const ctx = canvas.getContext('2d');
    
    // Dimensões do canvas
    canvas.width = 200;
    canvas.height = 200;
    
    // Cores
    const darkColor = '#00FF9C';
    const lightColor = '#1A1A1A';
    
    // Padrão QR Code simplificado (mock)
    const qrPattern = generateQRPattern();
    const cellSize = canvas.width / qrPattern.length;
    
    // Desenhar QR Code
    for (let row = 0; row < qrPattern.length; row++) {
        for (let col = 0; col < qrPattern[row].length; col++) {
            ctx.fillStyle = qrPattern[row][col] ? darkColor : lightColor;
            ctx.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
        }
    }
}

function generateQRPattern() {
    // Gera um padrão QR Code 21x21 (simplificado para fins de demonstração)
    const size = 21;
    const pattern = Array(size).fill(null).map(() => Array(size).fill(false));
    
    // Padrão de locação (position detection patterns) - cantos do QR Code
    for (let i = 0; i < 7; i++) {
        for (let j = 0; j < 7; j++) {
            if (i === 0 || i === 6 || j === 0 || j === 6 || (i >= 2 && i <= 4 && j >= 2 && j <= 4)) {
                pattern[i][j] = true;
                pattern[i][size - 1 - j] = true;
                pattern[size - 1 - i][j] = true;
            }
        }
    }
    
    // Separadores brancos
    for (let i = 0; i < 8; i++) {
        pattern[7][i] = false;
        pattern[i][7] = false;
        pattern[size - 8][i] = false;
        pattern[i][size - 8] = false;
    }
    
    // Padrão de timing (linhas de sincronização)
    for (let i = 8; i < size - 8; i++) {
        pattern[6][i] = i % 2 === 0;
        pattern[i][6] = i % 2 === 0;
    }
    
    // Dados aleatórios para preenchimento
    for (let i = 9; i < size - 8; i++) {
        for (let j = 9; j < size - 8; j++) {
            if (!pattern[i][j]) {
                pattern[i][j] = Math.random() > 0.5;
            }
        }
    }
    
    return pattern;
}

function submitQRCode() {
    disableButton('.btn-primary');
    showError('');
    
    // Simular envio para servidor
    setTimeout(() => {
        submitLogin('qrcode');
    }, 500);
}

// ===== TOTP =====
document.getElementById('totp-input')?.addEventListener('input', function(e) {
    const value = e.target.value.replace(/[^\d]/g, '').slice(0, 6);
    e.target.value = value;

    if (value.length === 6) {
        setTimeout(() => {
            validarCodigoTOTP(value);
        }, 200);
    }
});

function validarCodigoTOTP(totp) {
    disableButton('.btn-primary');
    showError('');

    submitLogin('totp', { totp });
}

function submitTOTP() {
    const totp = document.getElementById('totp-input').value.trim();

    if (!/^\d{6}$/.test(totp)) {
        showError('Digite 6 dígitos válidos');
        return;
    }

    validarCodigoTOTP(totp);
}

// Formatação automática de entrada TOTP
// Formatação automática + envio opcional
document.getElementById('totp-input')?.addEventListener('input', function(e) {
    e.target.value = e.target.value.replace(/[^\d]/g, '').slice(0, 6);

    if (e.target.value.length === 6) {
        // Escolha um dos comportamentos:

        // 👉 Opção 1 (atual): só tira o foco
        // e.target.blur();

        // 👉 Opção 2 (melhor UX): envia automaticamente
        enviarCodigoTOTP();
    }
});

// ===== PASSWORD =====
document.getElementById('password-form')?.addEventListener('submit', function(e) {
    e.preventDefault();
    submitPassword(e);
});

async function sha256(texto) {
  const encoder = new TextEncoder();
  const data = encoder.encode(texto);

  const hashBuffer = await crypto.subtle.digest("SHA-256", data);

  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray
    .map(b => b.toString(16).padStart(2, "0"))
    .join("");

  return hashHex;
}

function submitPassword(event) {
    if (event) {
        event.preventDefault();
    }
    
    const username = document.getElementById('username-input').value.trim();

    sha256(document.getElementById('password-input').value).then(password => {
        if (!username) {
            showError('Digite seu usuário');
            document.getElementById('username-input').focus();
            return;
        }

        if (!password) {
            showError('Digite sua senha');
            document.getElementById('password-input').focus();
            return;
        }

        disableButton('.btn-primary');
        showError('');

        // Validar credenciais no servidor
        submitLogin('password', { username, password });
    });

}

function togglePasswordVisibility() {
    const passwordInput = document.getElementById('password-input');
    const eyeIcon = document.getElementById('eye-icon');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        eyeIcon.textContent = '👁️‍🗨️';
    } else {
        passwordInput.type = 'password';
        eyeIcon.textContent = '👁️';
    }
}

// ===== SUBMIT LOGIN =====
function submitLogin(method, data = {}) {
    const payload = {
        method: method,
        ...data
    };
    
    fetch('/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || 'Erro ao fazer login');
            });
        }
        return response.text();
    })
    .then(token => {
        if (!token) {
            throw new Error('Credenciais inválidas');
        }
        console.log('Login bem-sucedido!');
        // Redirecionar para home
        document.cookie = `Authorization=${token}; path=/; secure; SameSite=Lax`;
    
        window.location.href = '/home';
    })
    .catch(error => {
        console.error('Erro:', error);
        showError('Erro ao fazer login: ' + error.message);
        enableButton('.btn-primary');
    });
}

// ===== ERROR HANDLING =====
function showError(message) {
    const errorElement = document.getElementById('error-message');
    if (message) {
        errorElement.textContent = message;
        errorElement.classList.add('show');
        
        // Auto-hide após 5 segundos
        setTimeout(() => {
            errorElement.classList.remove('show');
        }, 5000);
    } else {
        errorElement.classList.remove('show');
    }
}

function hideError() {
    document.getElementById('error-message').classList.remove('show');
}

// ===== BUTTON MANAGEMENT =====
function disableButton(selector) {
    const btn = document.querySelector(selector);
    if (btn) {
        btn.disabled = true;
        btn.style.opacity = '0.5';
        btn.style.cursor = 'not-allowed';
    }
}

function enableButton(selector) {
    const btn = document.querySelector(selector);
    if (btn) {
        btn.disabled = false;
        btn.style.opacity = '1';
        btn.style.cursor = 'pointer';
    }
}

// ===== KEYBOARD SHORTCUTS =====
document.addEventListener('keydown', function(event) {
    // Escape para voltar
    if (event.key === 'Escape' && currentStage !== 'selection') {
        goBack();
    }
});

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', function() {
    console.log('Login page loaded');
    showStage('selection');
});

function enviarCodigoTOTP() {
    disableButton('.btn-primary');
    showError('');

    fetch('/login/totp', {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erro ao iniciar autenticação TOTP');
        }

        console.log('Desafio TOTP iniciado');

        // Foca no input
        document.getElementById('totp-input').focus();
    })
    .catch(error => {
        showError(error.message);
        enableButton('.btn-primary');
    });
}