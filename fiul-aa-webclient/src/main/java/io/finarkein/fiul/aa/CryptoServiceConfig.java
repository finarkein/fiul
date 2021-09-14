package io.finarkein.fiul.aa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;


@Configuration
@ConfigurationProperties(prefix = "forwardsecrecy")
public class CryptoServiceConfig {

    @Value("${forwardsecrecy.ecc.curve:Curve25519}")
    private String curve;
    @Value("${forwardsecrecy.ecc.algorithm:EC}")
    private String algorithm;
    @Value("${forwardsecrecy.ecc.keyDerivationAlgorithm:ECDH}")
    private String keyDerivationAlgorithm;
    @Value("${forwardsecrecy.ecc.provider:BC}")
    private String provider;
    @Value("${forwardsecrecy.ecc.keyExpiryHrs:24}")
    private String keyExpiry;

    public Properties getAllProperties() {
        //TODO fill properties, find better way
        Properties properties = new Properties();
        return properties;
    }
}
