/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.msf4j.interceptor.common.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.core.utils.config.IdPClientConfiguration;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * component to get the registered IdPClient OSGi service.
 */
@Component(
        name = "ServiceComponent",
        service = ServiceComponent.class,
        immediate = true
)
public class ServiceComponent {

    @Activate
    protected void start(BundleContext bundleContext) throws ConfigurationException {
        ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();
        if (configProvider.getConfigurationObject(IdPClientConstants.SP_AUTH_NAMESPACE) == null) {
            DataHolder.getInstance().setInterceptorEnabled(true);
            DataHolder.getInstance().setExcludeURLList(new ArrayList<>());
        } else {
            IdPClientConfiguration idPClientConfiguration = configProvider.
                    getConfigurationObject(IdPClientConfiguration.class);
            String enableInterceptor = idPClientConfiguration.getProperties()
                    .getOrDefault(IdPClientConstants.ENABLE_INTERCEPTOR, "true");
            String exclude = idPClientConfiguration.getProperties().get(IdPClientConstants.EXCLUDE_INTERCEPTOR);
            Boolean isInterceptorEnabled = Boolean.parseBoolean(enableInterceptor);
            DataHolder.getInstance().setInterceptorEnabled(isInterceptorEnabled);
            List<String> excludeURLList = new ArrayList<>();
            if (exclude != null) {
                excludeURLList =  Arrays.asList(exclude.replaceAll("\\s*", "").split(","));
            }
            DataHolder.getInstance().setExcludeURLList(excludeURLList);
        }
    }

    @Deactivate
    protected void stop() {
    }


    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(configProvider);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(null);
    }

    @Reference(
            name = "org.wso2.carbon.analytics.idp.client.core.api.IdPClient",
            service = IdPClient.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterIdP"
    )
    protected void registerIdP(IdPClient client) {
        DataHolder.getInstance().setIdPClient(client);
    }

    protected void unregisterIdP(IdPClient client) {
        DataHolder.getInstance().setIdPClient(null);
    }
}
