package com.github.tiagolofi.configs;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "easy.password")
public interface EasyPasswordConfigs {

    public String telegramBotToken();

}
