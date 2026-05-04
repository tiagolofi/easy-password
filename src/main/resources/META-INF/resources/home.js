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

// ===== COPY =====
function showCopiedMessage(element) {
    // cria o elemento
    const msg = document.createElement('span');
    msg.textContent = 'Copiado!';
    msg.className = 'copy-feedback';

    // adiciona no DOM (próximo ao input)
    element.parentElement.appendChild(msg);

    // animação de entrada
    setTimeout(() => {
        msg.classList.add('show');
    }, 10);

    // remove depois
    setTimeout(() => {
        msg.classList.remove('show');
        setTimeout(() => msg.remove(), 300);
    }, 1500);
}

async function copyToClipboard(service) {
    const input = document.getElementById(`password-text-${service}`);
    if (!input) return;

    try {
        // 🔑 pega o valor atual do input
        const value = input.value || input.textContent;

        await navigator.clipboard.writeText(value);

        showCopiedMessage(input);

    } catch (error) {
        console.error(error);
    }
}

// ===== ADD NEW ITEM =====
function addNewItem() {
    openModal();
    // Limpar dados de edição
    document.getElementById('add-item-form').dataset.editService = '';
}

// ===== EDIT ITEM =====
function editItem(service) {
    const card = document.querySelector(`[data-service="${service}"]`);
    const password = card.dataset.password;

    // Preencher formulário com dados
    document.getElementById('service-input').value = service;
    document.getElementById('password-input').value = password;

    // Abrir modal
    openModal();

    // Armazenar service para atualização
    const form = document.getElementById('add-item-form');
    form.dataset.editService = service;
}

// ===== DELETE ITEM =====
function deleteItem(service) {
    deleteItemFromServer(service);
}

function normalizeService(service) {
    return service
        .toLowerCase()
        .replace(/\s+/g, '-')
        .replace(/[^\w-]/g, '');
}

// ===== SUBMIT NEW ITEM =====
function submitNewItem(event) {
    event.preventDefault();

    const service = document.getElementById('service-input').value.trim();
    const password = document.getElementById('password-input').value;
    const editService = event.target.dataset.editService;

    const newItem = { service, password };

    if (!editService) {
        // Adicionar novo item
        addItemToServer(newItem);
    } else {
        // Atualizar item existente
        updateItemOnServer(editService, newItem);
    }

    closeModal();
}

// ===== SERVER OPERATIONS =====
async function addItemToServer(item) {
    try {
        const response = await fetch('/home/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(item)
        });

        if (!response.ok) {
            throw new Error('Erro ao adicionar item');
        }

        // Recarregar página
        location.reload();
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao adicionar item: ' + error.message);
    }
}

async function updateItemOnServer(service, item) {
    try {
        const response = await fetch('/home/edit', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(item)
        });

        console.log('Resposta do servidor:', response);

        if (!response.ok) {
            throw new Error('Erro ao atualizar item');
        }

        // Recarregar página
        location.reload();
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao atualizar item: ' + error.message);
    }
}

async function deleteItemFromServer(service) {
    try {
        const response = await fetch('/home/delete', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ service: service })
        });

        if (!response.ok) {
            throw new Error('Erro ao deletar item');
        }

        // Recarregar página
        location.reload();
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao deletar item: ' + error.message);
    }
}

// ===== RENDER EMPTY STATE =====
function renderEmptyState() {
    const container = document.getElementById('items-container');
    container.innerHTML = `
        <div class="empty-state">
            <span class="empty-state-icon"><i class="fa-solid fa-plus"></i></span>
            <p class="empty-state-text">Nenhum serviço cadastrado</p>
            <p class="empty-state-subtext">Comece a adicionar suas senhas agora</p>
            <button class="btn-add-item" onclick="addNewItem()">+ Adicionar Serviço</button>
        </div>
    `;
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

        const response = await fetch(
            `/home/view?encryptedPassword=${encodeURIComponent(encryptedPassword)}&pin=${encodeURIComponent(pinHash)}`
        );

        if (!response.ok) throw new Error('Erro ao validar PIN');

        const result = await response.text();

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
        closePinModal();
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
