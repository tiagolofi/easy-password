// ===== HOME SCRIPT =====

// ===== SHA256 FUNCTION =====
async function sha256(str) {
    const encoder = new TextEncoder();
    const data = encoder.encode(str);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', function() {
    console.log('Home page loaded');

    initializePasswords();

    // ===== 🔥 FIX PRINCIPAL (PIN FORM) =====
    const pinForm = document.getElementById('pin-form');
    if (pinForm) {
        pinForm.addEventListener('submit', validatePinAndDecrypt);
    } else {
        console.warn('pin-form não encontrado');
    }

    // Setup modal listeners
    const pinModal = document.getElementById('pin-modal');
    if (pinModal) {
        pinModal.addEventListener('click', function(event) {
            if (event.target === this) {
                closePinModal();
            }
        });
    }

    const addItemModal = document.getElementById('add-item-modal');
    if (addItemModal) {
        addItemModal.addEventListener('click', function(event) {
            if (event.target === this) {
                closeModal();
            }
        });
    }

    // ===== UX: força apenas números no PIN =====
    const pinInput = document.getElementById('pin-input');
    if (pinInput) {
        pinInput.addEventListener('input', (e) => {
            e.target.value = e.target.value.replace(/\D/g, '').slice(0, 4);
        });
    }
});

// ===== INITIALIZE PASSWORDS =====
function initializePasswords() {
    const itemCards = document.querySelectorAll('.item-card');

    itemCards.forEach((card) => {
        const service = card.dataset.service;
        const passwordInput = document.getElementById(`password-value-${service}`);

        if (passwordInput) {
            card.dataset.password = passwordInput.value;
            card.dataset.visible = 'false';
        }
    });
}

// ===== TOGGLE PASSWORD =====
function togglePasswordVisibility(service) {
    const card = document.querySelector(`[data-service="${service}"]`);
    const passwordText = document.getElementById(`password-text-${service}`);
    const toggleBtn = document.getElementById(`toggle-${service}`);

    if (!card || !passwordText || !toggleBtn) return;

    const isVisible = card.dataset.visible === 'true';
    const encryptedPassword = card.dataset.password;

    if (isVisible) {
        passwordText.textContent = '•'.repeat(8);
        toggleBtn.textContent = '👁️';
        card.dataset.visible = 'false';
    } else {
        passwordText.textContent = encryptedPassword;
        toggleBtn.textContent = '👁️‍🗨️';
        card.dataset.visible = 'true';
    }
}

// ===== COPY =====
async function copyToClipboard(service) {
    const card = document.querySelector(`[data-service="${service}"]`);
    if (!card) return;

    const encryptedPassword = card.dataset.password;

    try {
        await navigator.clipboard.writeText(encryptedPassword);
        alert('Copiado!');
    } catch (error) {
        console.error(error);
        alert('Erro ao copiar');
    }
}

// ===== PIN MODAL =====
function openPinModal(service, encryptedPassword) {
    const modal = document.getElementById('pin-modal');
    const pinInput = document.getElementById('pin-input');

    if (!modal || !pinInput) {
        console.error('Modal PIN não encontrado');
        return;
    }

    modal.dataset.service = service;
    modal.dataset.encryptedPassword = encryptedPassword;

    modal.classList.add('show');

    pinInput.value = '';
    setTimeout(() => pinInput.focus(), 100);
}

function closePinModal() {
    const modal = document.getElementById('pin-modal');
    const pinInput = document.getElementById('pin-input');

    if (modal) modal.classList.remove('show');
    if (pinInput) pinInput.value = '';
}

async function validatePinAndDecrypt(event) {
    event.preventDefault();

    const pinInput = document.getElementById('pin-input');
    if (!pinInput) return;

    const pin = pinInput.value.trim();

    if (!/^\d{4}$/.test(pin)) {
        alert('Digite um PIN válido com 4 dígitos');
        return;
    }

    try {
        const pinHash = await sha256(pin);

        const modal = document.getElementById('pin-modal');
        const service = modal.dataset.service;
        const encryptedPassword = modal.dataset.encryptedPassword;

        // ✅ UMA ÚNICA CHAMADA
        const response = await fetch(
            `/home/view?encryptedPassword=${encodeURIComponent(encryptedPassword)}&pin=${encodeURIComponent(pinHash)}`
        );

        if (!response.ok) throw new Error('Erro ao validar PIN');

        const result = await response.text();

        // ✅ BACKEND DEVE RETORNAR ASSIM:
        // { valid: true, password: "senha_original" }

        if (result) {

            const passwordText = document.getElementById(`password-text-${service}`);
            const viewBtn = document.getElementById(`view-${service}`);
            const card = document.querySelector(`[data-service="${service}"]`);

            if (!passwordText || !viewBtn || !card) return;

            // Exibe senha
            passwordText.textContent = result;
            viewBtn.classList.add('active');
            card.dataset.showing_original = 'true';

            closePinModal();

            // Auto-hide
            setTimeout(() => {
                if (card.dataset.showing_original === 'true') {
                    passwordText.textContent = '•'.repeat(8);
                    viewBtn.classList.remove('active');
                    card.dataset.showing_original = 'false';
                }
            }, 10000);

        } else {
            alert('PIN inválido');
            pinInput.value = '';
            pinInput.focus();
        }

    } catch (error) {
        console.error(error);
        alert('Erro ao validar PIN');
    }
}

// ===== MODALS =====
function openModal() {
    const modal = document.getElementById('add-item-modal');
    if (modal) modal.classList.add('show');
}

function closeModal() {
    const modal = document.getElementById('add-item-modal');
    if (modal) modal.classList.remove('show');
}

// ===== KEYBOARD =====
document.addEventListener('keydown', function(event) {
    if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
        event.preventDefault();
        openModal();
    }

    if (event.key === 'Escape') {
        closeModal();
        closePinModal(); // ✅ importante
    }
});

// ===== LOGOUT =====
function logout() {
    // Aqui você faria logout no servidor
    document.cookie = 'Authorization=; path=/; Max-Age=0;';
    fetch('/logout', { method: 'POST' })
        .then(() => {
            window.location.href = '/login';
        })
        .catch(() => {
            window.location.href = '/login';
        });
    // if (confirm('Tem certeza que deseja sair?')) {
        
    // }
}
