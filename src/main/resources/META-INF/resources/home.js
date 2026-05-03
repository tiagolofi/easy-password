// ===== HOME SCRIPT =====

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', function() {
    console.log('Home page loaded');
    initializePasswords();
});

// ===== INITIALIZE PASSWORDS FROM HIDDEN INPUTS =====
function initializePasswords() {
    const itemCards = document.querySelectorAll('.item-card');
    itemCards.forEach((card) => {
        const service = card.dataset.service;
        const passwordInput = document.getElementById(`password-value-${service}`);
        if (passwordInput) {
            const password = passwordInput.value;
            card.dataset.password = password;
            card.dataset.visible = 'false';
        }
    });
}

// ===== TOGGLE PASSWORD VISIBILITY =====
function togglePasswordVisibility(service) {
    const card = document.querySelector(`[data-service="${service}"]`);
    const passwordText = document.getElementById(`password-text-${service}`);
    const toggleBtn = document.getElementById(`toggle-${service}`);
    const isVisible = card.dataset.visible === 'true';
    const password = card.dataset.password;

    if (isVisible) {
        // Ocultar
        passwordText.textContent = '•'.repeat(8);
        toggleBtn.textContent = '👁️';
        card.dataset.visible = 'false';
    } else {
        // Mostrar
        passwordText.textContent = password;
        toggleBtn.textContent = '👁️‍🗨️';
        card.dataset.visible = 'true';
    }
}

// ===== COPY TO CLIPBOARD =====
async function copyToClipboard(service) {
    const card = document.querySelector(`[data-service="${service}"]`);
    const password = card.dataset.password;
    const copyBtn = document.querySelector(`[onclick="copyToClipboard('${service}')"]`);

    try {
        await navigator.clipboard.writeText(password);
        
        // Feedback visual
        const originalText = copyBtn.textContent;
        copyBtn.textContent = '✓ Copiado!';
        copyBtn.classList.add('copied');
        
        setTimeout(() => {
            copyBtn.textContent = originalText;
            copyBtn.classList.remove('copied');
        }, 2000);
    } catch (error) {
        console.error('Erro ao copiar:', error);
        alert('Erro ao copiar para a área de transferência');
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
    const card = document.querySelector(`[data-service="${service}"]`);

    if (confirm(`Tem certeza que deseja deletar "${service}"?`)) {
        // Remover do DOM
        card.remove();

        // Aqui você enviaria requisição para servidor
        // deleteItemFromServer(service);

        // Verificar se está vazio
        const container = document.getElementById('items-container');
        if (container.children.length === 0) {
            renderEmptyState();
        }
    }
}

// ===== RENDER EMPTY STATE =====
function renderEmptyState() {
    const container = document.getElementById('items-container');
    container.innerHTML = `
        <div class="empty-state">
            <span class="empty-state-icon">📭</span>
            <p class="empty-state-text">Nenhum serviço cadastrado</p>
            <p class="empty-state-subtext">Comece a adicionar suas senhas agora</p>
            <button class="btn-add-item" onclick="addNewItem()">+ Adicionar Serviço</button>
        </div>
    `;
}

// ===== MODAL MANAGEMENT =====
function openModal() {
    const modal = document.getElementById('add-item-modal');
    modal.classList.add('show');
    document.getElementById('service-input').focus();
}

function closeModal() {
    const modal = document.getElementById('add-item-modal');
    modal.classList.remove('show');

    // Limpar formulário
    document.getElementById('add-item-form').reset();
    document.getElementById('add-item-form').dataset.editService = '';
}

// Fechar modal ao clicar fora
document.getElementById('add-item-modal')?.addEventListener('click', function(event) {
    if (event.target === this) {
        closeModal();
    }
});

// ===== SUBMIT NEW ITEM =====
function submitNewItem(event) {
    event.preventDefault();

    const service = document.getElementById('service-input').value.trim();
    const password = document.getElementById('password-input').value;
    const editService = event.target.dataset.editService;

    if (!service) {
        alert('Digite o nome do serviço');
        return;
    }

    if (!password) {
        alert('Digite a senha');
        return;
    }

    const newItem = { service, password };

    if (editService === '' || editService === undefined) {
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
        const response = await fetch('/api/items', {
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
        const response = await fetch(`/api/items/${encodeURIComponent(service)}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(item)
        });

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
        const response = await fetch(`/api/items/${encodeURIComponent(service)}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
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

// ===== UTILITIES =====
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ===== KEYBOARD SHORTCUTS =====
document.addEventListener('keydown', function(event) {
    // Ctrl/Cmd + K para adicionar novo item
    if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
        event.preventDefault();
        addNewItem();
    }

    // Escape para fechar modal
    if (event.key === 'Escape') {
        closeModal();
    }
});
