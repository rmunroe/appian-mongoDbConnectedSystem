package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;


@TemplateId(name = "WriteFindIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class WriteFindIntegrationTemplate extends FindIntegrationTemplate {

}
