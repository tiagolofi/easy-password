package com.github.tiagolofi.configs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "easy.password")
public interface EasyPasswordConfigs {

    @WithDefault("admin")
    public String admin();

    @WithDefault("admin123")
    public String adminPassword();

    @WithDefault("admin,user")
    public String[] adminRoles();

    public String telegramBotToken();

    public Long telegramChatId();

    @WithDefault("1234")
    public String pin();
}
