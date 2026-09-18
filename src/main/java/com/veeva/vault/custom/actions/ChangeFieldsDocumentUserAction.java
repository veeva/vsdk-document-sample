package com.veeva.vault.custom.actions;

import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.action.DocumentAction;
import com.veeva.vault.sdk.api.action.DocumentActionContext;
import com.veeva.vault.sdk.api.action.DocumentActionInfo;
import com.veeva.vault.sdk.api.action.Usage;
import com.veeva.vault.sdk.api.document.*;
import com.veeva.vault.sdk.api.query.Query;
import com.veeva.vault.sdk.api.query.QueryExecutionRequest;
import com.veeva.vault.sdk.api.query.QueryService;
import com.veeva.vault.sdk.api.token.TokenRequest;
import com.veeva.vault.sdk.api.token.TokenService;

import java.util.List;

/**
 * This class demonstrates how to use a Document User Action to modify fields on the document and it's related documents.
 * For this use case, the "Run vSDK Change Field Document User Action" user action is run on a "vSDK Document".
 * The user action will perform the following:
 *
 * 		- Run a VQL to query for all the related documents defined on the document.
 * 		- Iterates over the query results and assigned a "vsdk_external_id__c" of "SDK:<doc_id>" to each of the related documents.
 * 		- The parent document is then updated with a "vsdk_external_id__c" of "SDK:<doc_id>:relatedDocIds:<alldocids>"
 *
 */

@DocumentActionInfo(name = "vsdk_docaction_change_fields__c", label="Run vSDK Change Field User Action")
public class ChangeFieldsDocumentUserAction implements DocumentAction {

    public void execute(DocumentActionContext documentActionContext) {

    	DocumentService docService = ServiceLocator.locate((DocumentService.class));
    	DocumentVersion docVersion = documentActionContext.getDocumentVersions().get(0);
    	String parentExternalId = "vSDK:" + docVersion.getValue("id", ValueType.STRING) + ":relatedDocIds";

    	List<DocumentVersion> docVersionList = VaultCollections.newList();

    	String version_id = docVersion.getValue("id", ValueType.STRING) + "_" +
    				docVersion.getValue("major_version_number__v", ValueType.NUMBER).toString() + "_" +
    				docVersion.getValue("minor_version_number__v", ValueType.NUMBER).toString();

    	//Query the related documents and construct a custom "vsdk_external_id__c" value to assign the parent and related documents.
    	QueryService queryService = ServiceLocator.locate(QueryService.class);

    	//Supply the source version ID through a TokenRequest and build the query with newQueryBuilder()
    	//instead of concatenating the ID into a raw query String.
    	TokenService tokenService = ServiceLocator.locate(TokenService.class);
    	TokenRequest tokenRequest = tokenService.newTokenRequestBuilder()
    	    .withValue("Custom.source_version_id", version_id)
    	    .build();

    	List<String> relatedIds = VaultCollections.newList();

    	Query query = queryService.newQueryBuilder()
    	    .withSelect(VaultCollections.asList("relationship_type__v", "target__vr.version_id"))
    	    .withFrom("relationships")
    	    .withWhere("source__vr.version_id = ${Custom.source_version_id}")
    	    .build();

    	QueryExecutionRequest queryRequest = queryService.newQueryExecutionRequestBuilder()
    	    .withQuery(query)
    	    .withTokenRequest(tokenRequest)
    	    .build();

    	//The related document query is parsed through where the "vsdk_external_id__c" field is set equal to "SDK:<doc_id>". The parent external ID
    	//is built as a string with all the IDs of the related documents.
    	queryService.query(queryRequest)
    	    .onSuccess(queryExecutionResponse -> {
    	        queryExecutionResponse.streamResults().forEach(queryExecutionResult -> {
    	            DocumentVersion docVersionQuery = docService.newVersionWithId(
    	                queryExecutionResult.getValue("target__vr.version_id", ValueType.STRING));
    	            docVersionQuery.setValue("vsdk_external_id__c", "vSDK:" + docVersionQuery.getValue("id", ValueType.STRING));
    	            relatedIds.add(docVersionQuery.getValue("id", ValueType.STRING));
    	            docVersionList.add(docVersionQuery);
    	        });
    	    })
    	    .onError(queryOperationError -> {
    	        throw new RollbackException("OPERATION_NOT_ALLOWED", "Failed to query related documents: "
    	                + queryOperationError.getMessage());
    	    })
    	    .execute();

    	for (String relatedId : relatedIds) {
    	    parentExternalId += ":" + relatedId;
    	}

    	docVersion.setValue("vsdk_external_id__c", parentExternalId);
    	docVersionList.add(docVersion);
    	docService.saveDocumentVersions(docVersionList);
    }

	public boolean isExecutable(DocumentActionContext documentActionContext) {
	    return true;
	}
}
