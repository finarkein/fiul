/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.config;

import lombok.experimental.UtilityClass;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import javax.jms.MessageListener;
import java.util.UUID;

@UtilityClass
public class JmsUtil {

    public static SimpleJmsListenerEndpoint createSimpleEndpoint(String destination, MessageListener listener) {
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId(UUID.randomUUID().toString());
        endpoint.setDestination(destination);
        endpoint.setMessageListener(listener);
        return endpoint;
    }
}
