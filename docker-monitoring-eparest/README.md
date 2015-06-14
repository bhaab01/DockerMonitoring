# Field Pack Name (Version)
Docker Monitoring

# Description
Docker Monitoring fieldpack allows you to monitor the docker infrastructure after providing the hostname and port number on which the docker apps are running. 
A project overview (including description, sample screenshots, etc.) can be found on the project wiki page at https://cawiki.ca.com/display/APMFIELDPACKS/Docker+Monitoring

## Releases
From time to time, projects may make compiled releases available.  While source code is always available for complete build, releases serve as a "tag" (numbered release) and often contain prepared packages that are prebuilt and ready to use.  Visit `http://github.com/ca-apm/<repo_name>/releases` for details.

## APM version
APM 9.7.1 and above

## Supported third party versions
Java 1.7 and above
Docker Server API version 1.17 and above

## Limitations
No limitation

## License ( To be updated )
Apache 2.0 licenses

BouncyCastle - Already approved in techstack http://techstack.ca.com/techstack-web/content/tpsr/detail.jsp?tpsrId=00000087_19

Please review the 
**LICENSE**
file in this repository.  Licenses may vary by repository.  Your download and use of this software constitutes your agreement to this license.

# Installation Instructions
Install, configure and run an EPAgent on the same or a remote server. See CA APM Environment Performance Agent Implementation Guide. 
Unzip the binaries located at https://cawiki.ca.com/download/attachments/727271081/EPADocker.zip
Provide necessary input under docker.properties
Copy docker.typeviewers.xml to your Enterprise Manager (MOM) <EM_HOME>/ext/xmltv.

## Prerequisites
Docker Application needs to be started with -H parameter so that it can bind Docker to another host/port or a Unix socket ( both http and https ) 
For eg.
	 docker -d --tlsverify --tlscacert=ca.pem --tlscert=server-cert.pem --tlskey=server-key.pem -H=0.0.0.0:2376

## Dependencies
APM EPAgent version 9.7.1 and above

## Configuration
Make sure java path is set and pointing to 1.7 and above 
Run startDockerMonitor.bat

# Usage Instructions


## Metric description


## Custom Management Modules
Nothing is provided OOB

## Custom type viewers
A custom typeview is supplied with the project. For more screenshots, look at "screenshots.docx" 

## Debugging and Troubleshooting


## Support
This document and associated tools are made available from CA Technologies as examples and provided at no charge as a courtesy to the CA APM Community at large. This resource may require modification for use in your environment. However, please note that this resource is not supported by CA Technologies, and inclusion in this site should not be construed to be an endorsement or recommendation by CA Technologies. These utilities are not covered by the CA Technologies software license agreement and there is no explicit or implied warranty from CA Technologies. They can be used and distributed freely amongst the CA APM Community, but not sold. As such, they are unsupported software, provided as is without warranty of any kind, express or implied, including but not limited to warranties of merchantability and fitness for a particular purpose. CA Technologies does not warrant that this resource will meet your requirements or that the operation of the resource will be uninterrupted or error free or that any defects will be corrected. The use of this resource implies that you understand and agree to the terms listed herein.

Although these utilities are unsupported, please let us know if you have any problems or questions by adding a comment to the CA APM Community Site area where the resource is located, so that the Author(s) may attempt to address the issue or question.

Unless explicitly stated otherwise this field pack is only supported on the same platforms as the APM core agent. See [APM Compatibility Guide](http://www.ca.com/us/support/ca-support-online/product-content/status/compatibility-matrix/application-performance-management-compatibility-guide.aspx).


# Contributing
The [CA APM Community](https://communities.ca.com/community/ca-apm) is the primary means of interfacing with other users and with the CA APM product team.  The [developer subcommunity](https://communities.ca.com/community/ca-apm/ca-developer-apm) is where you can learn more about building APM-based assets, find code examples, and ask questions of other developers and the CA APM product team.

If you wish to contribute to this or any other project, please refer to [easy instructions](https://communities.ca.com/docs/DOC-231150910) available on the CA APM Developer Community.


# Change log
Changes for each version of the field pack.

Version | Author | Comment
--------|--------|--------
1.1 | Abhijit Bhadra | Added support for https
1.0 | Abhijit Bhadra | First version of the field pack.
