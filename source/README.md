About action pack:
	The Docker Action Pack brings possibility to use all outstanding features of Docker right from CA Automic One Automation Platform, which makes life of developers, application engineers, development operators and others easier to create, deploy, and run applications by utilizing image and container systems. Docker allows users to package up an application with all necessary configurations and all depending parts, such as libraries, then ship all out as one package,namely as an image, and deploy as isolated containers in multiple environments. Isolated containers assures that deployed applications run successfully without regarding to the different settings of different machines and environments.

	All of the Actions provided inside the Docker Action Pack, can be used in a regular way to form end-to-end automation templates to automate series of docker commands including pulling image from docker hub, building a new image from Docker file, creating containers, starting, stopping containers and more. It is flexible to use each action as they accept input parameters both in manual and headless mode.


Available Actions:

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

Supported Platforms:

	1. Oracle JRE 1.7 or later
	2. Docker 1.12 or later on Linux or Windows
	3. The Agent must be able to access the Docker daemon in order to make HTTP(s) requests

Docker Action pack depends on

	- Automation.Engine » AutomationEngine (minimum version Automation.Engine 11.2)
	- Package.Filesystem » PCK.AUTOMIC_FILESYSTEM (minimum version Package.Filesystem 1.1)
	- Package.ITPA.Shared » PCK.ITPA_SHARED (minimum version Package.ITPA.Shared 1.1)
