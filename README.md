# Vault Java SDK Sample - vsdk-document-sample

**Please see the [project wiki](https://gitlab.com/veeva/vsdk-document-sample/wikis/home) for a detailed walkthrough**.

The **vsdk-document-sample** project covers the use of custom actions on a document of type **vSDK Document**. The actions will step through:

-   Updating fields on the vSDK Document and on its related vSDK Documents with a user action.
-   Creating related vSDK Document Task records with a state change entry action.

## How to import

Import the project as a Maven project. This will automatically pull in the required Vault Java SDK dependencies. 

For Intellij this is done by:
-	File -> Open -> Navigate to project folder -> Select the 'pom.xml' file -> Open as Project

For Eclipse this is done by:
-	File -> Import -> Maven -> Existing Maven Projects -> Navigate to project folder -> Select the 'pom.xml' file


## Setup

For this project, the custom actions and necessary vault components are contained in the two separate vault packages (VPK). The VPKs are located in the project's **deploy-vpk** directory  and **need to be deployed to your vault** prior to debugging these use cases:

1.  Clone or download the sample Maven project [vSDK Document Sample project](https://gitlab.com/veeva/vsdk-document-sample) from Gitlab.
2.  Run through the [Getting Started](https://dev-developer.veevavault.com/sdk/#Getting_Started) guide to setup your development environment.
3.  Log in to your vault and navigate to **Admin > Deployment > Inbound Packages** and click **Import**:
4.  Locate and select the following file in your downloaded project file:

    > **Document Action** code: **\deploy-vpk\code\vsdk-document-sample-code.vpk** file.
 
5.  From the **Actions** menu (gear icon), select **Review & Deploy**. Vault displays a list of all components in the package.   
6.  Review the prompts to deploy the package. You will receive an email when vault completes the deployment.
7.  Repeat steps 3-6 for the vault components, select the package that matches your vault type:

    > **Base** Vault: **\deploy-vpk\components\Base_vsdk-document-sample-components\Base_vsdk-document-sample-components.vpk** file.

    > **Multichannel** Vault: **\deploy-vpk\components\Multichannel_vsdk-document-sample-components\Multichannel_vsdk-document-sample-components.vpk** file
    
    > **Clinical** Vault: **\deploy-vpk\components\Clinical_vsdk-document-sample-components\Clinical_vsdk-document-sample-components.vpk** file.
    
    > **RIM** Vault: **\deploy-vpk\components\RIM_vsdk-document-sample-components\RIM_vsdk-document-sample-components.vpk** file.
    
    > **Quality** Vault: **\deploy-vpk\components\Quality_vsdk-document-sample-components\Quality_vsdk-document-sample-components.vpk** file.

8.  Create two documents with the  **vSDK Document** type:
    -   From the main page select  **Create -> Upload**
    -   Upload the  **Parent vSDK Document.txt** and  **Related vSDK Document.txt**  files in the  **deploy-vpk**  directory.            
9.  Navigate to the  **Parent vSDK Document**, select the blue  **+**  button next to the  **Supporting Documents**  section and add (**+**) the  **Related vSDK Document**.  
	  
	     
	    
## License

This code serves as an example and is not meant to be used for production use.

Copyright 2018 Veeva Systems Inc.
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
  