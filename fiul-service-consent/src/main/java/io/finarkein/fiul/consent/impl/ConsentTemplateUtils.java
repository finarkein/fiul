/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.common.FIDataRange;
import io.finarkein.api.aa.consent.DataLife;
import io.finarkein.api.aa.consent.Frequency;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.consent.model.ConsentTemplateDataRange;
import io.finarkein.fiul.consent.model.TimeDuration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static io.finarkein.api.aa.util.Functions.uuidSupplier;
import static io.finarkein.fiul.Functions.TIMESTAMP_FORMAT;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ConsentTemplateUtils {

    private static final String FINANCIAL_YEAR_START_POST_FIX = "-04-01T00:00:00.000Z";
    private static final String FINANCIAL_YEAR_END_POST_FIX = "-03-31T23:59:59.999Z";

    public static DataLife generateDataLife(String dataLifeString) {
        DataLife dataLife = new DataLife();
        String[] strings = extractUnitValue(dataLifeString, "DataLife");
        dataLife.setUnit(strings[0]);
        dataLife.setValue(Integer.valueOf(strings[1]));
        return dataLife;
    }

    public static Frequency generateFrequency(String frequencyString) {
        Frequency frequency = new Frequency();
        String[] strings = extractUnitValue(frequencyString, "Frequency");
        frequency.setUnit(strings[0]);
        frequency.setValue(Integer.valueOf(strings[1]));
        return frequency;
    }

    public static FIDataRange generateFIDataRange(ConsentTemplateDataRange consentTemplateDataRange, String consentStart, String consentExpiry, int currentYear) {
        switch (consentTemplateDataRange.getDataRangeType()) {
            case FIXED:
                ArgsValidator.validateDateRange(uuidSupplier.get(), consentTemplateDataRange.getFrom(), consentTemplateDataRange.getTo());
                return new FIDataRange(consentTemplateDataRange.getFrom(), consentTemplateDataRange.getTo());
            case SAME_AS_CONSENT:
                ArgsValidator.validateDateRange(uuidSupplier.get(), consentStart, consentExpiry);
                return new FIDataRange(consentStart, consentExpiry);
            case FINANCIAL_YEAR:
                int year = Integer.parseInt(consentTemplateDataRange.getYear());
                var instant = Instant.parse(currentYear + FINANCIAL_YEAR_END_POST_FIX);
                LocalDateTime localDate = LocalDateTime.ofInstant(instant, ZoneOffset.ofHours(0));
                if (Instant.now().isAfter(instant))
                    localDate = localDate.plus(1, ChronoUnit.YEARS);
                var fiDataRange = new FIDataRange();
                fiDataRange.setTo(localDate.format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT)));
                instant = Instant.parse(localDate.getYear() + FINANCIAL_YEAR_START_POST_FIX);
                localDate = LocalDateTime.ofInstant(instant, ZoneOffset.ofHours(0));
                fiDataRange.setFrom(localDate.minus(year + 1L, ChronoUnit.YEARS).format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT)));
                return fiDataRange;
            case CONSENT_START_RELATIVE:
                String[] start = extractUnitValue(consentTemplateDataRange.getFrom(), "FIDataRange");
                String[] expiry = extractUnitValue(consentTemplateDataRange.getTo(), "FIDataRange");
                if (start[0].equals("INF") || expiry[0].equals("INF"))
                    throw Errors.InvalidRequest.with(uuidSupplier.get(), "FIDataRange Start or Expiry cannot be infinite");
                instant = Instant.parse(consentStart);
                localDate = LocalDateTime.ofInstant(instant, ZoneOffset.ofHours(0));
                start[0] = start[0] + "S";
                expiry[0] = expiry[0] + "S";
                var from = localDate.plus(Integer.parseInt(start[1]), ChronoUnit.valueOf(start[0])).toString();
                var to = localDate.plus(Integer.parseInt(expiry[1]), ChronoUnit.valueOf(expiry[0])).toString();
                ArgsValidator.validateDateRange(uuidSupplier.get(), from, to);
                return new FIDataRange(from, to);
            default:
                throw Errors.InvalidRequest.with(uuidSupplier.get(), "Invalid DataRange Type");
        }
    }

    public static String[] generateConsentDateRange(String consentStartOffset, String consentExpiryDuration, String consentStart) {
        String[] start = extractUnitValue(consentStartOffset, "ConsentStartOffset");
        String[] expiry = extractUnitValue(consentExpiryDuration, "ConsentExpiryDuration");
        if (start[0].equals("INF") || expiry[0].equals("INF"))
            throw Errors.InvalidRequest.with(uuidSupplier.get(), "Consent Start and Expiry cannot be infinite");
        Instant parse = Instant.parse(consentStart);
        if (!start[1].equals("0")) {
            parse = addTime(start, parse, "ConsentStartOffset");
            consentStart = parse.toString();
        }
        parse = addTime(expiry, parse, "ConsentExpiryDuration");
        ArgsValidator.validateDateRange(uuidSupplier.get(), consentStart, parse.toString());
        return new String[]{consentStart, parse.toString()};
    }

    private static Instant addTime(String[] duration, Instant parse, String parameterName) {
        switch (duration[0]) {
            case "SECOND":
                parse = parse.plus(Long.parseLong(duration[1]), ChronoUnit.SECONDS);
                return parse;
            case "MINUTE":
                parse = parse.plus(Long.parseLong(duration[1]), ChronoUnit.MINUTES);
                return parse;
            case "HOUR":
                parse = parse.plus(Long.parseLong(duration[1]), ChronoUnit.HOURS);
                return parse;
            case "DAY":
                parse = parse.plus(Long.parseLong(duration[1]), ChronoUnit.DAYS);
                return parse;
            case "WEEK":
                LocalDateTime localDateTime = LocalDateTime.ofInstant(parse, ZoneOffset.ofHours(0));
                localDateTime = localDateTime.plus(Long.parseLong(duration[1]), ChronoUnit.WEEKS);
                parse = localDateTime.toInstant(ZoneOffset.ofHours(0));
                return parse;
            case "MONTH":
                localDateTime = LocalDateTime.ofInstant(parse, ZoneOffset.ofHours(0));
                localDateTime = localDateTime.plus(Long.parseLong(duration[1]), ChronoUnit.MONTHS);
                parse = localDateTime.toInstant(ZoneOffset.ofHours(0));
                return parse;
            case "YEAR":
                localDateTime = LocalDateTime.ofInstant(parse, ZoneOffset.ofHours(0));
                localDateTime = localDateTime.plus(Long.parseLong(duration[1]), ChronoUnit.YEARS);
                parse = localDateTime.toInstant(ZoneOffset.ofHours(0));
                return parse;
            default:
                throw Errors.InvalidRequest.with(uuidSupplier.get(), "Invalid " +  parameterName + " format in ConsentTemplate");
        }
    }

    public static String[] extractUnitValue(String parameter, String parameterName) {
        String[] returnString = new String[3];
        if (parameter.equals("i")) {
            returnString[0] = "INF";
            returnString[1] = "0";
            return returnString;
        }
        returnString[1] = parameter.substring(0, parameter.length() - 1);
        String unit = parameter.substring(parameter.length() - 1);

        String timeDuration = TimeDuration.TIME_DURATION_MAP.get(unit);
        if (timeDuration == null)
            throw Errors.InvalidRequest.with(uuidSupplier.get(), "Invalid " + parameterName + " format in ConsentTemplate");
        returnString[0] = timeDuration;
        return returnString;
    }
}
