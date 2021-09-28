/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceConstants {
    public static final String NOTIFICATION_Q_TYPE_PROPERTY = "fiul.notification-queue-type";
    public static final String CHANNEL_NAME_CONSENT = "VirtualTopic.consent-notification-queue";
    public static final String CHANNEL_NAME_FI = "VirtualTopic.fi-notification-queue";
    public static final String FIUL_CALLBACK_WEB_CLIENT_QUALIFIER = "fiul.notification.callback.webhook-client.qualifier";
}
