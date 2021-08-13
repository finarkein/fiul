/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.common;

import io.finarkein.api.aa.common.DigitalSigConsumer;
import io.finarkein.api.aa.common.KeyMaterialConsumer;
import io.finarkein.api.aa.common.TimestampConsumer;
import io.finarkein.api.aa.common.TxnIdConsumer;
import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.fiul.CryptoServiceAdapter;
import io.finarkein.fiul.Functions.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Log4j2
public class RequestUpdater {
    protected TimestampUpdater timestampUpdater;
    protected TxnIdUpdater txnIdUpdater;
    @Setter
    @Getter
    protected DigitalSignUpdater digitalSigUpdater;

    public RequestUpdater(String requestTimestampSetter, String requestTxnIdSetter) {
        try {
            if (Strings.isBlank(requestTimestampSetter))
                requestTimestampSetter = "noop";
            timestampUpdater = TimestampUpdaters.get(requestTimestampSetter);
            log.info("Using TimestampUpdater:{}", requestTimestampSetter);
        } catch (Exception e) {
            timestampUpdater = TimestampUpdaters.noop;
            log.warn("Invalid TimestampUpdater:{}, hence using default:noop", requestTimestampSetter);
        }

        try {
            if (Strings.isBlank(requestTxnIdSetter))
                requestTxnIdSetter = "noop";
            txnIdUpdater = TxnIdUpdaters.get(requestTxnIdSetter);
            log.info("Using TxnIdUpdater:{}", requestTxnIdSetter);
        } catch (Exception e) {
            txnIdUpdater = TxnIdUpdaters.noop;
            log.warn("Invalid TxnIdUpdater:{}, hence using default:noop", requestTxnIdSetter);
        }

        digitalSigUpdater = new DigitalSignUpdater() {
            @Override
            public boolean updateIfNeededAndCache(DigitalSigConsumer consumer, String aaName) {
                return false;
            }
        };
    }

    public boolean updateTxnIdIfNeeded(TxnIdConsumer txnIdConsumer) {
        return txnIdUpdater.updateIfNeeded(txnIdConsumer);
    }

    public boolean updateTimestampIfNeeded(TimestampConsumer timestampConsumer) {
        return timestampUpdater.updateIfNeeded(timestampConsumer);
    }

    interface KeyMaterialUpdater {
        default boolean updateIfNeeded(CryptoServiceAdapter crypto, KeyMaterialConsumer consumer) {
            return false;
        }
    }

    private enum KeyMaterialUpdaters implements KeyMaterialUpdater {
        noop,
        generateIfNull {
            @Override
            public boolean updateIfNeeded(CryptoServiceAdapter crypto, KeyMaterialConsumer consumer) {
                final KeyMaterial keyMaterial = consumer.getKeyMaterial();

                if (keyMaterial == null || Strings.isBlank(keyMaterial.getCryptoAlg())) {
                    return generateAuto.updateIfNeeded(crypto, consumer);
                }
                return false;
            }
        },
        generateAuto {
            @Override
            public boolean updateIfNeeded(CryptoServiceAdapter crypto, KeyMaterialConsumer consumer) {
                consumer.setKeyMaterial(crypto.generateKey().getKeyMaterial());
                return true;
            }
        };

        static KeyMaterialUpdaters get(String value) {
            final Optional<KeyMaterialUpdaters> first = Arrays.stream(KeyMaterialUpdaters.values())
                    .filter(updater -> updater.name().equalsIgnoreCase(value))
                    .findFirst();
            return first.orElseThrow(() -> new IllegalArgumentException("Invalid value for KeyMaterialUpdater:" + value));
        }
    }

    private interface TimestampUpdater {
        default boolean updateIfNeeded(TimestampConsumer consumer) {
            return false;
        }
    }

    private interface TxnIdUpdater {
        default boolean updateIfNeeded(TxnIdConsumer consumer) {
            return false;
        }
    }

    public interface DigitalSignUpdater {
        default boolean updateIfNeededAndCache(DigitalSigConsumer consumer, String aaName) {
            return false;
        }
    }

    public enum TimestampUpdaters implements TimestampUpdater {
        noop,
        generateIfNull {
            @Override
            public boolean updateIfNeeded(TimestampConsumer consumer) {
                if (Strings.isBlank(consumer.getTimestamp()))
                    return generateAuto.updateIfNeeded(consumer);
                return false;
            }
        },
        generateAuto {
            @Override
            public boolean updateIfNeeded(TimestampConsumer consumer) {
                consumer.setTimestamp(currentTimestamp());
                return true;
            }
        };

        private static SimpleDateFormat timestampFormat;

        static {
            timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            timestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        static TimestampUpdaters get(String value){
            final Optional<TimestampUpdaters> first = Arrays.stream(TimestampUpdaters.values())
                    .filter(updater -> updater.name().equalsIgnoreCase(value))
                    .findFirst();
            return first.orElseThrow(() -> new IllegalArgumentException("Invalid value for TimestampUpdater:" + value));
        }

        static final Function<java.util.Date, String> dateToUTCString = date -> timestampFormat.format(date);

        public static String currentTimestamp() {
            return dateToUTCString.apply(java.util.Date.from(Instant.now()));
        }
    }

    public enum TxnIdUpdaters implements TxnIdUpdater {
        noop,
        generateIfNull {
            @Override
            public boolean updateIfNeeded(TxnIdConsumer consumer) {
                if (Strings.isBlank(consumer.getTxnid()))
                    return generateAuto.updateIfNeeded(consumer);
                return false;
            }
        },
        generateAuto {
            @Override
            public boolean updateIfNeeded(TxnIdConsumer consumer) {
                consumer.setTxnid(generateTxnId());
                return true;
            }
        };

        protected String generateTxnId() {
            return UUID.randomUUID().toString();
        }

        static TxnIdUpdater get(String value){
            final Optional<TxnIdUpdaters> first = Arrays.stream(TxnIdUpdaters.values())
                    .filter(updater -> updater.name().equalsIgnoreCase(value))
                    .findFirst();
            return first.orElseThrow(() -> new IllegalArgumentException("Invalid value for TxnIdUpdater:" + value));
        }
    }
}
