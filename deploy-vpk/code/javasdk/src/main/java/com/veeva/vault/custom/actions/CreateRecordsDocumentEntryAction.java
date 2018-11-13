package com.veeva.vault.custom.actions;

import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.action.DocumentAction;
import com.veeva.vault.sdk.api.action.DocumentActionContext;
import com.veeva.vault.sdk.api.action.DocumentActionInfo;
import com.veeva.vault.sdk.api.action.Usage;
import com.veeva.vault.sdk.api.data.Record;
import com.veeva.vault.sdk.api.data.RecordService;
import com.veeva.vault.sdk.api.document.*;
import com.veeva.vault.sdk.api.notification.NotificationParameters;
import com.veeva.vault.sdk.api.notification.NotificationService;
import com.veeva.vault.sdk.api.notification.NotificationTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * This class demonstrates how to use a Document Entry Action to create object records when a document passes through a particular state.
 * For this use case, a "vSDK Document" passes into the "Entry Action via SDK" state to initiate the creation of a "vSDK Document Task" object record.
 * This record:
 * 
 * 		- References the document that initiated the action in the "Document" field. 
 * 		- Assigns the current user to the "Assigned To" field of the vSDK Document Task record.
 * 		- On a successful object record insert, the current user is sent a notification telling them that record was created 
 * 			and the Description (title_v) field is updated with the vSDK Document Task ID.
 * 
 * The 'Change state to "Entry Action via SDK"' action will initiate the state change and entry action.
 * 
 */

@DocumentActionInfo(name = "vsdk_docaction_create_records__c", label="Run vSDK Create Records Entry Action")
public class CreateRecordsDocumentEntryAction implements DocumentAction {
	
    public void execute(DocumentActionContext documentActionContext) {

    	RequestContext requestContext = RequestContext.get();
    	RecordService recordService = ServiceLocator.locate(RecordService.class);
    	DocumentService docService = ServiceLocator.locate((DocumentService.class));
    	NotificationService notificationService = ServiceLocator.locate(NotificationService.class);
    
    	DocumentVersion docVersion = documentActionContext.getDocumentVersions().get(0);
    	
    	List<Record> recordList = VaultCollections.newList();
    	List<DocumentVersion> docVersionList = VaultCollections.newList();
    	
    	String id = docVersion.getValue("id", ValueType.STRING) + "_" + 
    				docVersion.getValue("major_version_number__v", ValueType.NUMBER).toString() + "_" + 
    				docVersion.getValue("minor_version_number__v", ValueType.NUMBER).toString();
    	

    	//Creates new vSDK Document Task object records and sets the name__v, document__c, and assigned_to__c fields.
    	//On a successful insert, the current user will be sent a notification using the "vSDK Object Notification Template"
    	Record r = recordService.newRecord("vsdk_document_task__c");
    	r.setValue("name__v", "vSDK Document Task " + Instant.now().toEpochMilli());
    	r.setValue("document__c", id);
    	r.setValue("assigned_to__c", requestContext.getCurrentUserId());
    	recordList.add(r);
    	
    	recordService.batchSaveRecords(recordList)
        .onErrors(batchOperationErrors -> {
            batchOperationErrors.stream().findFirst().ifPresent(error -> {
                String errMsg = error.getError().getMessage();
                int errPosition = error.getInputPosition();
                String name = recordList.get(errPosition).getValue("name__v", ValueType.STRING);
                throw new RollbackException("OPERATION_NOT_ALLOWED", "Unable to create vSDK Document Task records: "
                        + name + " due to " + errMsg);
            });
        }).onSuccesses(successMessage -> {
        	
        	Set<String> notificationUsers = VaultCollections.newSet();
        	successMessage.stream().forEach(positionalRecordId -> {
        		
        		docVersion.setValue("title__v", "vSDK Document Task: " + positionalRecordId.getRecordId());
	    		
	            //NotificationParameters - sets the user who will be receiving the notification.
	            NotificationParameters notificationParameters = notificationService.newNotificationParameters();
	    		notificationUsers.add(requestContext.getCurrentUserId());
	            notificationParameters.setRecipientsByUserIds(notificationUsers);
	            notificationParameters.setSenderId(requestContext.getCurrentUserId());
	
	            //NotificationTemplate - grabs the Notification template that will be used and 
	            //sets a custom token to link the new record Id to the notification.
	            NotificationTemplate template = notificationService.newNotificationTemplate()
	            .setTemplateName("vsdk_object_notification_template__c")
	            .setTokenValue("objectRecordName", positionalRecordId.getRecordId());
	
	            //Sends the Notification to the recipient once the document action has successfully completed.
	            notificationService.send(notificationParameters, template);
        	});
		  })
        .execute();
    	
    	docVersionList.add(docVersion);
    	docService.saveDocumentVersions(docVersionList);
    }

	public boolean isExecutable(DocumentActionContext documentActionContext) {
	    return true;
	}
}