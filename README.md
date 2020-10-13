## Getting Started:


###### Description

The Docker Action Pack brings possibility to use all outstanding features of Docker right from CA Automic One Automation Platform, which makes life of developers, application engineers, development operators and others easier to create, deploy, and run applications by utilizing image and container systems. Docker allows users to package up an application with all necessary configurations and all depending parts, such as libraries, then ship all out as one package,namely as an image, and deploy as isolated containers in multiple environments. Isolated containers assures that deployed applications run successfully without regarding to the different settings of different machines and environments.

All of the Actions provided inside the Docker Action Pack, can be used in a regular way to form end-to-end automation templates to automate series of docker commands including pulling image from docker hub, building a new image from Docker file, creating containers, starting, stopping containers and more. It is flexible to use each action as they accept input parameters both in manual and headless mode. 
		
###### Actions

1. Search Image
2. Inspect Image
3. List Images
4. Create Image
5. Configure Connection
6. Create Container
7. Start Container
8. Change Container State
9. List Containers
10. Remove Container
11. Inspect Container
12. Export Container
13. Exec Create
14. Exec Start
15. Remove Image
16. Build Image from Dockerfile
17. Get Image history from Docker
18. Import Container

		
###### Compatibility:

1. Oracle JRE 1.7 or later
2. Docker 1.12 or later on Linux or Windows
3. The Agent must be able to access the Docker daemon in order to make HTTP(s) requests

###### Prerequisite:

1. Automation Engine should be installed.
2. Automic Package Manager should be installed.
3. ITPA Shared and FILESYSTEM action packs should be installed.

###### Steps to install action pack source code:

1. Clone the code to your machine.
2. Go to the directory inside the package directory containg the pom.xml file.(source/tools/)
3. Open the terminal and run the maven package command. Example: **mvn clean package -DskipTests**
4. Run the command apm upload in the directory which contains package.yml (source/):
   Ex. **apm upload -force -u <Name>/<Department> -c <Client-id> -H <Host> -pw <Password> -S AUTOMIC -y -ia -ru**


###### Package/Action Documentation

Please refer to the link for [package documentation](source/ae/DOCUMENTATION/PCK.AUTOMIC_DOCKER.PUB.DOC.xml)

###### Third party licenses:

The third-party library and license document reference.[Third party licenses](source/ae/DOCUMENTATION/PCK.AUTOMIC_DOCKER.PUB.LICENSES.xml)

###### Useful References

1. [About Packs and Plug-ins](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#PluginManager/PM_AboutPacksandPlugins.htm?Highlight=Action%20packs)
2. [Working with Packs and Plug-ins](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#PluginManager/PM_WorkingWith.htm#link10)
3. [Actions and Action Packs](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#_Common/ReleaseHighlights/RH_Plugin_PackageManager.htm?Highlight=Action%20packs)
4. [PACKS Compatibility Mode](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#AWA/Variables/UC_CLIENT_SETTINGS/UC_CLIENT_PACKS_COMPATIBILITY_MODE.htm?Highlight=Action%20packs)
5. [Working with actions](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#ActionBuilder/AB_WorkingWith.htm#link4)
6. [Installing and Configuring the Action Builder](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#ActionBuilder/install_configure_plugins_AB.htm?Highlight=Action%20packs)

###### Distribution: 

In the distribution process, we can download the existing or updated action package from the Automation Engine by using the apm build command.
Example: **apm build -y -H AE_HOST -c 106 -u TEST/TEST -pw password -d /directory/ -o zip -v action_pack_name**
			
			
###### Copyright and License: 

Broadcom does not support, maintain or warrant Solutions, Templates, Actions and any other content published on the Community and is subject to Broadcom Community [Terms and Conditions](https://community.broadcom.com/termsandconditions)
