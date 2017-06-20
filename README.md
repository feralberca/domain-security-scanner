domain-security-scanner
==============

Check if any of the specified domains was pwned.

#Building the source code

- Modify application.conf as needed including the domains that you want to check.
- Run 'sbt assembly'

#Building the docker image
- Run 'docker build -t domain-scanner .'

#Running the app
- set the environment variable SLACK_WEB_HOOK with the id of the Slack Web Hook. e.g: SLACK_WEB_HOOK="T5GGBARGW/B5F2UAJSU/opUuqiuO8AMICqcvEtpncWWW"
- run the docker container as daemon with 'docker run -d -e SLACK_WEB_HOOK --name domain-scanner-instance domain-scanner:latest
- additionally include any JAVA_OPTS to the previous command line.