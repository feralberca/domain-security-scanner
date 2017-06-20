FROM java:8

MAINTAINER Fernando Alberca noone@noone.com

ENV SD_VERSION=1.0

RUN groupadd -g 494 domain-security-scanner && \
    useradd -m domain-security-scanner -g domain-security-scanner
    


COPY target/scala-2.12/domain-security-scanner-assembly-$SD_VERSION.jar /home/domain-security-scanner/
WORKDIR /home/domain-security-scanner

RUN chmod +x /home/domain-security-scanner/*.jar && \
    chown -R domain-security-scanner:domain-security-scanner /home/domain-security-scanner
    
USER domain-security-scanner

CMD ["bash", "-c", "java -jar /home/domain-security-scanner/domain-security-scanner-assembly-$SD_VERSION.jar"]