# 🚀 Guia de Uso - Easy Password Interface

## 📖 Índice
1. [Como Executar](#como-executar)
2. [Usando a Tela de Login](#usando-a-tela-de-login)
3. [Usando a Tela de Home](#usando-a-tela-de-home)
4. [Atalhos de Teclado](#atalhos-de-teclado)
5. [Troubleshooting](#troubleshooting)

---

## Como Executar

### Opção 1: Development Mode (Recomendado)

```bash
# Navegar até o projeto
cd /home/tiagolofi/Documentos/projetos/easy-password

# Executar Quarkus em modo dev
mvn quarkus:dev

# Abrir no navegador
# http://localhost:8080
```

**Vantagens:**
- Hot reload automático (não precisa reiniciar ao editar código)
- Logs em tempo real
- Modo debug ativo

### Opção 2: Production Build

```bash
# Compilar
mvn clean package

# Executar JAR
java -jar target/quarkus-app/quarkus-run.jar

# Servidor em
# http://localhost:8080
```

### Opção 3: Docker (se configurado)

```bash
mvn clean package -DskipTests -Dquarkus.container-image.build=true
docker run -p 8080:8080 easy-password:1.0.0
```

---

## Usando a Tela de Login

### 1️⃣ Tela de Seleção de Método

Quando você acessa `http://localhost:8080/login`, verá:

```
         🔐
    Easy Password
    
Escolha seu método de autenticação

┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│     📱      │  │     ⏱️      │  │     🔑      │
│   QRCODE    │  │    TOTP     │  │   SENHA     │
│  Autent...  │  │  6 dígitos  │  │ Tradicional │
└─────────────┘  └─────────────┘  └─────────────┘
```

**Ações:**
- Clique em um dos 3 botões para selecionar o método
- Cada método leva a uma tela diferente

---

### 2️⃣ Método QRCODE

```
         ← Voltar

Autenticação QRCODE

┌──────────────────┐
│   ██ ██ ██ ██   │  ← QR Code gerado
│   ██    ██ ██   │
│   ██ ██ ██ ██   │
└──────────────────┘

Escaneie o código QR com seu autenticador

[        Confirmar        ]
```

**Como usar:**
1. Abra seu autenticador (Google Authenticator, Authy, etc)
2. Escaneie o QR Code exibido
3. Clique em "Confirmar"

---

### 3️⃣ Método TOTP

```
         ← Voltar

Autenticação TOTP

┌──────────────────┐
│  Digite os 6 dígitos
│  
│  [000000        ]  ← Auto-formata, aceita só números
│  ─────────────── ✨ 
└──────────────────┘

[        Entrar        ]
```

**Como usar:**
1. Abra seu autenticador e copie os 6 dígitos
2. Cole ou digite os 6 números (auto-completa)
3. Quando atingir 6 dígitos, pode pressionar Enter ou clicar em Entrar

**Dica:** Se digitar letra ou símbolo, será ignorado automaticamente

---

### 4️⃣ Método SENHA

```
         ← Voltar

Autenticação por Senha

┌──────────────────┐
│  Usuário
│  
│  [seu usuário  ]
│  ──────────────
│
│  Senha
│  
│  [sua senha    ] 👁️  ← Clique para mostrar/ocultar
│  ──────────────
│
│  [        Entrar        ]
└──────────────────┘
```

**Como usar:**
1. Digite seu nome de usuário
2. Digite sua senha
3. Clique no ícone 👁️ para mostrar/ocultar a senha
4. Clique em "Entrar"

**Dica:** Suporta autofill do navegador (Ctrl+Shift+L ou ⌘+Shift+L no Mac)

---

## Usando a Tela de Home

### 1️⃣ Layout Principal

```
🔐 Easy Password                                  [Sair]

Seus Serviços                                [+ Adicionar]

┌────────────────────────────────┐
│ 🔒 Gmail                   [✏️ ][🗑️]
│
│ Senha
│ [••••••••] [👁️] [📋]
└────────────────────────────────┘

┌────────────────────────────────┐
│ 🔒 GitHub                  [✏️ ][🗑️]
│
│ Senha
│ [••••••••] [👁️] [📋]
└────────────────────────────────┘
```

### 2️⃣ Interagindo com Cartões

#### 👁️ **Mostrar/Ocultar Senha**
```
Antes:
[••••••••] [👁️] 

Depois:
[senha123!@#] [👁️‍🗨️]

Clique novamente para ocultar
```

#### 📋 **Copiar para Clipboard**
```
[••••••••] [📋]
            ↓
         [✓ Copiado!]  (2 segundos)
            ↓
         [📋]  (volta ao normal)
```

#### ✏️ **Editar Serviço**
1. Clique em ✏️
2. Modal abre com dados preenchidos
3. Edite e clique em "Salvar"

#### 🗑️ **Deletar Serviço**
1. Clique em 🗑️
2. Confirmação: "Tem certeza que deseja deletar?"
3. Clique em "OK" para confirmar

### 3️⃣ Adicionar Novo Serviço

**Botão "+ Adicionar"** (canto superior direito)

```
Modal abre:
┌─────────────────────────────┐
│  Adicionar Novo Serviço    [×]
│
│  Nome do Serviço
│  [ex: Gmail, GitHub, etc.]
│
│  Senha
│  [Digite a senha        ]
│
│  [Cancelar] [Salvar]
└─────────────────────────────┘
```

**Como usar:**
1. Digite o nome do serviço (ex: Netflix)
2. Digite a senha
3. Clique em "Salvar"
4. Novo item aparece na lista
5. Modal fecha automaticamente

---

## Atalhos de Teclado

### Na Tela de Login

| Atalho | Ação |
|--------|------|
| `ESC` | Voltar para seleção de método |
| `Enter` | Submit do formulário (em TOTP/SENHA) |

### Na Tela de Home

| Atalho | Ação |
|--------|------|
| `Ctrl + K` ou `⌘ + K` | Abrir modal de adicionar novo item |
| `ESC` | Fechar modal |

---

## Troubleshooting

### ❌ Problema: "Erro ao fazer login"

**Solução:**
1. Verifique se o backend está respondendo
2. Abra o Console do Navegador (F12 → Console)
3. Veja qual foi o erro exato
4. Verifique se o servidor está rodando: `http://localhost:8080/login`

### ❌ Problema: Página em branco após login

**Solução:**
1. Verifique se o endpoint `/home` está implementado
2. Certifique-se de que você tem autenticação configurada
3. Verifique os logs do servidor

### ❌ Problema: QR Code não aparece

**Solução:**
1. Verifique se o navegador suporta Canvas (todos modernos suportam)
2. Abra o Console (F12 → Console) e procure por erros
3. O QR Code é gerado lado do cliente, então não depende do servidor

### ❌ Problema: Copiar para clipboard não funciona

**Solução:**
1. O navegador deve estar em HTTPS ou localhost
2. Se em HTTP, o navegador pode pedir permissão
3. Certifique-se de que o navegador suporta Clipboard API

### ❌ Problema: Modal não fecha

**Solução:**
1. Clique novamente no ×
2. Pressione ESC
3. Clique fora do modal
4. Se nada funcionar, recarregue a página (F5)

---

## 🔍 Testando com Curl

### Login TOTP
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "method": "TOTP",
    "totp": "123456"
  }'
```

### Login SENHA
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "method": "SENHA",
    "username": "admin",
    "password": "senha123"
  }'
```

### Login QRCODE
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "method": "QRCODE"
  }'
```

---

## 📱 Testando Responsividade

### No Chrome DevTools:
1. Abra DevTools (F12)
2. Clique no ícone de dispositivo (📱)
3. Selecione diferentes tamanhos:
   - iPhone SE (375px)
   - iPhone 12 (390px)
   - iPad (768px)
   - Desktop (1920px)

### Tamanhos de tela testados:
- ✅ Mobile: 320px - 480px
- ✅ Tablet: 481px - 768px
- ✅ Desktop: 769px+

---

## 🎨 Personalizando Cores

### Para alterar as cores, edite `login.css` e `home.css`:

```css
:root {
    --primary-dark: #0D0D0D;      /* Preto fosco */
    --accent-green: #00FF9C;      /* Verde neon */
    --text-light: #E0E0E0;        /* Texto */
}
```

Exemplos de cores alternativas:
- Verde claro: `#00FF9C`, `#00E676`, `#1DE9B6`
- Azul: `#00BCD4`, `#2196F3`, `#0D47A1`
- Roxo: `#9C27B0`, `#7B1FA2`, `#E040FB`
- Rosa: `#E91E63`, `#C2185B`, `#FF4081`

---

## 🐛 Debug Mode

### Ativar logs do navegador:

```javascript
// Abra o Console (F12) e cole:

// Ativar debug
localStorage.debug = '*'

// Ver todas as requisições
console.log = function(...args) {
  console.info(...args)
}

// Ver dados de items
console.table(document.querySelectorAll('.item-card'))
```

---

## ✨ Dicas e Truques

1. **Copiar múltiplas senhas:** Clique em 📋 várias vezes
2. **Editar rápido:** Clique em ✏️ e pressione Ctrl+K quando terminar
3. **Buscar item:** Use Ctrl+F do navegador para filtrar
4. **Modo escuro:** Já está implementado! (o app é todo dark mode)
5. **Tela inteira:** Pressione F11 para modo tela cheia

---

## 📞 Suporte

Se encontrar problemas:

1. Verifique o Console do Navegador (F12)
2. Verifique os Logs do Servidor (`mvn quarkus:dev`)
3. Verifique a conectividade com o backend
4. Tente limpar cache (Ctrl+Shift+Del)
5. Recarregue a página (Ctrl+F5 para hard refresh)

---

**Divirta-se usando o Easy Password! 🔐✨**
