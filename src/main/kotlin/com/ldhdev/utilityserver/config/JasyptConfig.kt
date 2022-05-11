package com.ldhdev.utilityserver.config

import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JasyptConfig {

    @Bean
    fun jasyptStringEncryptor(): StringEncryptor {
        val config = SimpleStringPBEConfig().apply {
            password = System.getProperty("jasypt.password")
            algorithm = "PBEWithMD5AndDES"
            keyObtentionIterations = 1000
            poolSize = 1
            providerName = "SunJCE"
            setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator")
            stringOutputType = "base64"
        }

        return PooledPBEStringEncryptor().apply { setConfig(config) }
    }
}