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

// ===== METHOD SELECTION =====
document.querySelectorAll('.method-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        const method = this.dataset.method;
        showStage(method.toLowerCase());
        
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

// ===== TOTP =====
document.getElementById('totp-input').disabled = true;

// Formatação automática + envio opcional
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

function enviarCodigoTOTP() {
    document.getElementById('totp-input').disabled = false;

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

// ===== PASSWORD =====
document.getElementById('password-form')?.addEventListener('submit', function(e) {
    e.preventDefault();
    submitPassword(e);
});

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
        
        // Adicionando token no cookie
        document.cookie = `Authorization=${token}; path=/; secure; SameSite=Lax`;
        
        // Redirecionar para home
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
