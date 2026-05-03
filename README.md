# full-password

Autenticação fácil para quem tem dificuldade de lembrar senhas

## Variáveis necessárias

Api do Telegram
- quarkus.rest-client.telegram.url=https://api.telegram.org/

Trava do dispositivo no telegram
- easy.password.telegram.chat-id=.........

Token do Telegram
- easy.password.telegram.bot-token=bot........................

Palavra secreta para encriptar as senhas nos payloads (16 bytes)
- easy.password.pass.phrase=secret1234567890 

- Par de chaves RSA
private.key=MIIJQwIBAD
public.key=MIICIjANBgk

## Conceitos envolvidos neste projeto

- DDD para desacoplamento de bancos de dados (InMemory)
- Hashing e Criptografia
- Autenticação com JWT (Encriptação, Decriptação e Papéis de Acesso)
- One Time Password (TOTP)
- Segurança Adaptativa

## Ideias para evoluir

- Armazenamento seguro de senhas
- Análise de risco no login
- Counter de PIN Incorreto
- Autenticação com Padrão, QRCODE ou Facial