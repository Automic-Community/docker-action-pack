## Getting Started:


###### Description
 This Docker action pack used to support the Docker feature on CA Automic One Automation Platform. [Check the Feature support by action pack](source/README.md)

###### Prerequisite:
	1. AE (Automation Engine) is installed. 
	2. On AE, Please Install ITPA_SHARED and File System package.
	3. apm (Automic package Manager) also installed.
		a. https://techdocs.broadcom.com/us/en/ca-enterprise-software/it-operations-management/application-performance-management/10-7.html
	4) If package is java action pack then (check for java pack: package-dir/tools/pom.xml)
		a. Java is installed. Reference to install java: https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html
		b. Maven is installed. reference : https://maven.apache.org/install.html 

###### Apm doctor:
As a package manager user, what that the package manager is able to analyse and repair my environment in case that I get system exceptions, so that I don't need to contact the support if my AE client is messed up.

There are two sub-commands:
| Sub Command  | Description                                    | Command            |
|--------------|------------------------------------------------|--------------------|
| doctor check | analyze and show up current environment issues	| apm doctor check   |
| doctor fix   | repair environment issues	                | apm doctor fix     |

###### Steps to install the action package on AE:
	1. Clone the code to your machine.
	2. Go to the package directory.
	3. Run maven command if uses java action pack: mvn clean package
	4. Run the command: 
	apm upload -force -u <Name>/<Department> -c <Client-id> -H <Host> -S AUTOMIC -y -ia -ru
		- Name: Name use the user.
		- Department: Name of the Department.
		- Client-id: Provide the client id.
		- Host: Host on which the AE is running.

###### Verifying the installation:
	1. Login to AE machine
	2. Click on Administration.
	3. Click on packs in corner of left menu.
	4. Check in the displayed list of actions.

###### Action Builder:
In Action builder, Lets try to add new action in the existing action package and the delete process of action.

     > Add an action:
		1. Login to AE.
		2. Go to process assembly.
		3. Click on packs on the left bottom corner.
		4. Select the action package.
		5. Or the right window clicks the add action button.
		6. On the Add action tab:
			a. Select the Action type. E.g. (CLI)
			b. Title of the action. E.g. (Test)
			c. Category: Directory in which action is store. E.g. package name\actions
			d. Provide the name of the action. E.g. (Test)
			e. Click next.
			f. Verify the structure of the action and then click add button.
			g. On the next window “Action created successfully” message appear, Click the close button.
		7. New action will appear in the action list.
    > Delete an action:
		1. Login to AE.
		2. Go to process assembly.
		3. Click on packs on the left bottom corner.
		4. Select the action package.
		5. Select the action to be delete.
		6. Right click on the action.
		7. Click on delete.

###### Distribution: 
In the distribution process, we can download the exiting or updated action package from the Automation Engine by using the below command Apm build and share the zip.
**Command: apm build [option] <package_name>**

Options:
This command has the following option(s):

|Name           |Shortkey|Required |    Arguments	        |                Description                                                                              |
|---------------|--------|---------|----------------------------|---------------------------------------------------------------------------------------------------------|
|--output-format|-o	 | No	   |[solution\|zip\|\tar\|folder] <br>Default: folder|Defines the output format of the package. The possible options are:<br>SOLUTION: Zip-archive with the extension ".solution"<br>ZIP: Zip-archive with the extension ".zip"<br>TAR: gzip-compressed tar-archive with the extension ".tar.gz"<br>FOLDER: Folder in the specified target directory
|--target-dir	|-d	 | No	   |N/A <br>Default:  <current_dir>                    | Path to target directory in local filesystem E.g. /packages/|
|--client	|-c	 | No	   |NA	                        |Automation Engine client number. E.g.106   |
|--host	        |-H	 | No	   |NA	                        |Automation Engine hostname or IP address. E.g.  10.149.132.64         |
|--password	|-pw	 | No	   |NA	                        |Automation Engine password. E.g. password |
|--user	        |-u	 | No	   |NA 	                        |Automation Engine user/department, E.g.: John/Unit1                       |
|--port	        |-p      | No	   |NA	                        |Automation Engine port. E.g. 8080    | 
|--system	|-S	 | No	   |NA	                        |Selected system                      |

**Example: 
apm build -y -H vvieintegr0201.sbb01.spoc.global -c 106 -u TEST/TEST -pw *** -d /directory/ -o zip -v action_pack_name
Output: 
Pack action_pack_name was built successfully at /directory/action_pack_name_version.zip**


###### Do or Don’t:
	> Do
		1. Add action.
		2. Delete action.
	> Don’t
		1. Do not change the vara and include name. It will affect the almost all actions of the action pack.
		2. Do not raise a pull request for delete action.
		
		
###### Copyright and License: 
Broadcom does not support, maintain or warrant Solutions, Templates, Actions and any other content published on the Community and is subject to Broadcom Community Terms and Conditions (https://community.broadcom.com/termsandconditions).
