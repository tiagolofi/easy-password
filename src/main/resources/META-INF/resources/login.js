// ===== LOGIN SCRIPT =====

let currentStage = 'selection';
let totpUsername = '';

// ===== STAGE MANAGEMENT =====
function showStage(stageName) {
    document.querySelectorAll('.stage').forEach(stage => {
        stage.classList.remove('active');
    });

    const stage = document.getElementById(`stage-${stageName}`);
    if (stage) {
        stage.classList.add('active');
        currentStage = stageName;
        hideError();

        // Focus no input apropriado
        if (stageName === 'totp') {
            setTimeout(() => {
                document.getElementById('username-totp').focus();
            }, 100);
        } else if (stageName === 'senha') {
            setTimeout(() => {
                document.getElementById('username-senha').focus();
            }, 100);
        }
    }
}

function goBack() {
    showStage('selection');
    totpUsername = '';
    document.getElementById('username-totp').value = '';
    document.getElementById('totp-code').value = '';
    document.getElementById('totp-code').disabled = true;
}

// ===== SHA256 HASH =====
async function sha256(texto) {
    const encoder = new TextEncoder();
    const data = encoder.encode(texto);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// ===== METHOD SELECTION =====
document.querySelectorAll('.method-btn').forEach(btn => {
    btn.addEventListener('click', function () {
        const method = this.dataset.method.toLowerCase();
        showStage(method);
    });
});

// ===== TOTP =====
document.addEventListener('DOMContentLoaded', () => {
    const totpCodeInput = document.getElementById('totp-code');
    if (totpCodeInput) {
        totpCodeInput.addEventListener('input', function (e) {
            const value = e.target.value.replace(/\D/g, '').slice(0, 6);
            e.target.value = value;

            // Auto-submit ao completar 6 dígitos
            if (value.length === 6) {
                setTimeout(() => {
                    submitTOTP({ preventDefault: () => {} });
                }, 300);
            }
        });
    }
});

async function enviarCodigoTOTP() {
    const username = document.getElementById('username-totp').value.trim();
    const password = document.getElementById('password-senha-totp').value;
    const hashedPassword = await sha256(password);

    if (!username) {
        showError('Digite seu usuário');
        document.getElementById('username-totp').focus();
        return;
    }

    if (!password) {
        showError('Digite sua senha para enviar o código TOTP');
        document.getElementById('password-senha-totp').focus();
        return;
    }

    try {
        disableButton('#btn-enviar-totp');
        hideError();

        const response = await fetch(`/auth/totp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: hashedPassword
            })
        });

        if (!response.ok) {
            throw new Error('Usuário ou senha inválidos para envio do código TOTP');
        }

        // Habilita input do código TOTP
        const totpInput = document.getElementById('totp-code');
        const totpGroup = document.getElementById('totp-input-group');

        totpGroup.classList.remove('hidden');

        totpInput.disabled = false;
        totpInput.focus();
        totpUsername = username;

        showError('Código TOTP enviado para seu Telegram! ✓', 'success');
    } catch (error) {
        console.error(error);
        showError(error.message || 'Erro ao enviar código TOTP');
        enableButton('#btn-enviar-totp');
    }
}

async function submitTOTP(event) {
    event.preventDefault();

    const totp = document.getElementById('totp-code').value.trim();

    if (!totp || totp.length !== 6) {
        showError('Digite um código TOTP válido com 6 dígitos');
        return;
    }

    if (!totpUsername) {
        showError('Erro: Usuário não definido. Envie o código novamente.');
        return;
    }

    try {
        disableButton('button[type="submit"]');
        hideError();

        await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                method: 'totp',
                // username: totpUsername,
                totp: totp
            })
        }).then(response => {
            if (response.ok) {
                window.location.href = '/home';
            } else {
                const errorText = response.text();
                showError(errorText || 'Código TOTP inválido ou expirado');
                enableButton('button[type="submit"]');
            }
        });

    } catch (error) {
        console.error(error);
        showError('Erro ao validar código TOTP');
        enableButton('button[type="submit"]');
    }
}

// ===== SENHA =====
function togglePasswordVisibility(inputId) {
    const input = document.getElementById(inputId);
    const button = event.target.closest('button');
    const icon = button.querySelector('i');

    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    } else {
        input.type = 'password';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    }
}

async function submitSenha(event) {
    event.preventDefault();

    const username = document.getElementById('username-senha').value.trim();
    const password = document.getElementById('password-senha').value;

    if (!username) {
        showError('Digite seu usuário');
        document.getElementById('username-senha').focus();
        return;
    }

    if (!password) {
        showError('Digite sua senha');
        document.getElementById('password-senha').focus();
        return;
    }

    try {
        disableButton('button[type="submit"]');
        hideError();

        // Hash da senha
        const hashedPassword = await sha256(password);

        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                method: 'password',
                username: username,
                password: hashedPassword
            })
        });

        if (response.ok) {
            window.location.href = '/home';
        } else {
            const errorText = await response.text();
            showError(errorText || 'Usuário ou senha inválidos');
            enableButton('button[type="submit"]');
        }
    } catch (error) {
        console.error(error);
        showError('Erro ao fazer login');
        enableButton('button[type="submit"]');
    }
}

// ===== ERROR HANDLING =====
function showError(message, type = 'error') {
    const errorElement = document.getElementById('error-message');
    if (message) {
        errorElement.textContent = message;
        errorElement.className = `error-message show ${type}`;

        // Auto-hide após 5 segundos
        setTimeout(() => {
            errorElement.classList.remove('show');
        }, 5000);
    }
}

function hideError() {
    const errorElement = document.getElementById('error-message');
    errorElement.classList.remove('show');
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
document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && currentStage !== 'selection') {
        goBack();
    }
});

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', function () {
    console.log('Login page loaded');
    showStage('selection');
});
