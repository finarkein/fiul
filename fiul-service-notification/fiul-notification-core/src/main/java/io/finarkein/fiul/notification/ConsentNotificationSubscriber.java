/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification;

import io.finarkein.api.aa.notification.ConsentNotification;

public interface ConsentNotificationSubscriber {
    void handleConsentNotification(ConsentNotification notification);
}
