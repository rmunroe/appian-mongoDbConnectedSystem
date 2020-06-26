package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;


@TemplateId(name = "ReadFindIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class ReadFindIntegrationTemplate extends FindIntegrationTemplate {

}
