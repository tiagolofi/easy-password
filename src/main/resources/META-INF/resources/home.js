// ===== HOME SCRIPT =====

let services = [];
let editingService = null;
let pinModalService = null;
let deleteModalService = null;

// Carrega serviços ao iniciar
document.addEventListener('DOMContentLoaded', () => {
    loadServices();
});

// ===== SHA256 HASH =====
async function sha256(str) {
    const encoder = new TextEncoder();
    const data = encoder.encode(str);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// ===== LOAD SERVICES =====
async function loadServices() {
    try {
        const response = await fetch('/services/listar', {
            method: 'GET'
        });

        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = '/auth';
                return;
            }
            throw new Error('Erro ao carregar serviços');
        }

        services = await response.json();
        renderServices();
    } catch (error) {
        console.error(error);
        showNotification('Erro ao carregar serviços', 'error');
    }
}

// ===== RENDER SERVICES =====
function renderServices() {
    const container = document.getElementById('items-container');

    if (services.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <span class="empty-state-icon"><i class="fa-solid fa-inbox"></i></span>
                <p class="empty-state-text">Nenhum serviço cadastrado</p>
                <p class="empty-state-subtext">Comece adicionando suas primeiras senhas agora</p>
                <button type="button" class="btn-add-item" onclick="openAddModal()">
                    <i class="fa-solid fa-plus"></i> Adicionar Serviço
                </button>
            </div>
        `;
        return;
    }

    container.innerHTML = services.map((service, index) => `
        <div class="item-card" data-service="${escapeHtml(service)}">
            <div class="item-header">
                <div class="item-service">
                    <span class="service-icon"><i class="fa-solid fa-key"></i></span>
                    <span class="service-name">${escapeHtml(service)}</span>
                </div>
                <div class="item-actions">
                    <button type="button" class="item-action-btn btn-view" 
                            onclick="openPinModal('${escapeHtml(service)}')"
                            title="Copiar senha">
                        <i class="fa-solid fa-unlock"></i>
                    </button>
                    <button type="button" class="item-action-btn btn-delete" 
                            onclick="openDeleteModal('${escapeHtml(service)}')"
                            title="Deletar serviço">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </div>
            </div>

            <div class="item-password">
                <span class="password-label">Senha:</span>
                <span class="password-hidden">••••••••</span>
            </div>
        </div>
    `).join('');
}

// ===== ESCAPE HTML =====
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ===== PIN MODAL =====
function openPinModal(serviceName) {
    pinModalService = {
        name: serviceName
    };

    document.getElementById('pin-modal').classList.add('show');
    document.getElementById('pin-input').value = '';
    setTimeout(() => {
        document.getElementById('pin-input').focus();
    }, 100);
}

function closePinModal() {
    document.getElementById('pin-modal').classList.remove('show');
    document.getElementById('pin-input').value = '';
    pinModalService = null;
}

async function validatePinAndDecrypt(event) {
    event.preventDefault();

    if (!pinModalService) {
        showNotification('Erro: Serviço não definido', 'error');
        return;
    }

    const pin = document.getElementById('pin-input').value.trim();

    if (!/^\d{6}$/.test(pin)) {
        showNotification('Digite um PIN válido com 6 dígitos', 'error');
        return;
    }

    try {
        const pinHash = await sha256(pin);
        const name = pinModalService.name

        const response = await fetch(
            `/services/mostrar-senha?name=${encodeURIComponent(name)}`,
            {
                method: 'POST',
                headers: {
                    'X-PIN-SECURITY': pinHash,
                    'Content-Type': 'application/json'
                }
            }
        );

        if (!response.ok) {
            throw new Error('PIN inválido');
        }

        const decryptedPassword = await response.text();

        // Copia automaticamente a senha descriptografada
        await navigator.clipboard.writeText(decryptedPassword);
        showNotification('Senha descriptografada e copiada! ✓', 'success');

        closePinModal();
    } catch (error) {
        console.error(error);
        showNotification('PIN inválido ou erro ao descriptografar', 'error');
    }
}

// ===== ADD/EDIT MODAL =====
function openAddModal() {
    editingService = null;
    document.getElementById('modal-title').textContent = 'Adicionar Novo Serviço';
    document.getElementById('add-item-form').reset();
    document.getElementById('add-item-modal').classList.add('show');
    document.getElementById('service-name').focus();
}

function openEditModal(index) {
    if (index < 0 || index >= services.length) return;

    editingService = index;
    const service = services[index];
    document.getElementById('modal-title').textContent = `Editar: ${escapeHtml(service.name)}`;
    document.getElementById('service-name').value = service.name;
    document.getElementById('service-password').value = service.password;
    document.getElementById('add-item-modal').classList.add('show');
    document.getElementById('service-name').focus();
}

function closeAddModal() {
    document.getElementById('add-item-modal').classList.remove('show');
    document.getElementById('add-item-form').reset();
    editingService = null;
}

async function submitItem(event) {
    event.preventDefault();

    const serviceName = document.getElementById('service-name').value.trim();
    const password = document.getElementById('service-password').value;

    if (!serviceName) {
        showNotification('Digite o nome do serviço', 'error');
        document.getElementById('service-name').focus();
        return;
    }

    if (!password) {
        showNotification('Digite a senha', 'error');
        document.getElementById('service-password').focus();
        return;
    }

    const item = {
        name: serviceName,
        password: { value: password }
    };

    try {
        let response;

        if (editingService !== null) {
            // UPDATE
            response = await fetch('/services/alterar', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(item)
            });
        } else {
            // CREATE
            response = await fetch('/services/adicionar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(item)
            });
        }

        if (!response.ok) {
            throw new Error('Erro ao salvar serviço');
        }

        showNotification('Serviço salvo com sucesso! ✓', 'success');
        closeAddModal();
        loadServices();
    } catch (error) {
        console.error(error);
        showNotification(error.message || 'Erro ao salvar serviço', 'error');
    }
}

// ===== DELETE MODAL =====
function openDeleteModal(serviceName) {
    deleteModalService = serviceName;
    document.getElementById('delete-message').textContent =
        `Tem certeza que deseja deletar "${escapeHtml(serviceName)}"? Esta ação não pode ser desfeita.`;
    document.getElementById('delete-modal').classList.add('show');
}

function closeDeleteModal() {
    document.getElementById('delete-modal').classList.remove('show');
    deleteModalService = null;
}

async function confirmDelete() {
    if (!deleteModalService) return;

    try {
        const name = deleteModalService;
        const response = await fetch(`/services/apagar?name=${encodeURIComponent(name)}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Erro ao deletar serviço');
        }

        showNotification('Serviço deletado com sucesso! ✓', 'success');
        closeDeleteModal();
        loadServices();
    } catch (error) {
        console.error(error);
        showNotification(error.message || 'Erro ao deletar serviço', 'error');
    }
}

// ===== NOTIFICATIONS =====
function showNotification(message, type = 'info') {
    // Implementar toast notification se desejar
    console.log(`[${type.toUpperCase()}] ${message}`);
}

// ===== LOGOUT =====
async function logout() {
    try {
        await fetch('/auth/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        window.location.href = '/auth'
    } catch (error) {
        console.error(error);
    }
    // if (confirm('Tem certeza que deseja sair?')) {

    // }
}

// ===== KEYBOARD SHORTCUTS =====
document.addEventListener('keydown', function (event) {
    // Ctrl+K ou Cmd+K para adicionar novo item
    if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
        event.preventDefault();
        openAddModal();
    }

    // ESC para fechar modais
    if (event.key === 'Escape') {
        closeAddModal();
        closePinModal();
        closeDeleteModal();
    }
});

// ===== MODAL OUTSIDE CLICK =====
document.querySelectorAll('.modal').forEach(modal => {
    modal.addEventListener('click', function (event) {
        if (event.target === this) {
            if (this.id === 'add-item-modal') {
                closeAddModal();
            } else if (this.id === 'pin-modal') {
                closePinModal();
            } else if (this.id === 'delete-modal') {
                closeDeleteModal();
            }
        }
    });
});

// ===== PIN INPUT FORMATTING =====
document.getElementById('pin-input')?.addEventListener('input', function (e) {
    e.target.value = e.target.value.replace(/\D/g, '').slice(0, 6);
});

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', function () {
    console.log('Home page loaded');
    loadServices();
});

// ===== ADICIONAR USUÁRIO ====

// ===== USER MODAL =====
function openUserModal() {
    document.getElementById('add-user-form').reset();
    document.getElementById('add-user-modal').classList.add('show');

    setTimeout(() => {
        document.getElementById('user-username').focus();
    }, 100);
}

function closeUserModal() {
    document.getElementById('add-user-modal').classList.remove('show');
    document.getElementById('add-user-form').reset();
}

// ===== ADD USER ==== 
async function submitUser(event) {
    event.preventDefault();

    const username = document.getElementById('user-username').value.trim();
    const password = document.getElementById('user-password').value;
    const telegramId = document.getElementById('user-telegram-id').value.trim();
    const pin = document.getElementById('user-pin').value.trim();

    if (!username) {
        showNotification('Digite o username', 'error');
        return;
    }

    if (!password) {
        showNotification('Digite a senha', 'error');
        return;
    }

    if (!telegramId) {
        showNotification('Digite o Telegram ID', 'error');
        return;
    }

    if (!/^\d{6}$/.test(pin)) {
        showNotification('O PIN deve conter 6 dígitos', 'error');
        return;
    }

    try {
        const pinHash = await sha256(pin);

        const payload = {
            username: username,
            password: password,
            telegramChatId: telegramId,
            pin: pinHash
        };

        const response = await fetch('/users/adicionar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error('Erro ao adicionar usuário');
        }

        showNotification('Usuário cadastrado com sucesso! ✓', 'success');
        closeUserModal();

    } catch (error) {
        console.error(error);
        showNotification(error.message || 'Erro ao cadastrar usuário', 'error');
    }
}

// ===== USER PIN FORMATTING =====
document.getElementById('user-pin')?.addEventListener('input', function (e) {
    e.target.value = e.target.value.replace(/\D/g, '').slice(0, 6);
});
