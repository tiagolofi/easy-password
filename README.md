# full-password

Autenticação fácil para quem tem dificuldade de lembrar senhas

## Variáveis necessárias

Api do Telegram
- quarkus.rest-client.telegram.url=https://api.telegram.org/

Trava do dispositivo no telegram
- easy.password.telegram.chat-id=.........

Token do Telegram
- easy.password.telegram.bot-token=bot........................

Palavra secreta para encriptar as senhas nos payloads
- easy.password.pass.phrase=secret1234567890

- Par de chaves RSA
private.key=MIIJQwIBAD
public.key=MIICIjANBgk