package com.github.tiagolofi.configs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "easy.password")
public interface EasyPasswordConfigs {

    public String telegramBotToken();

    @WithDefault("1234")
    public String pin();
}
