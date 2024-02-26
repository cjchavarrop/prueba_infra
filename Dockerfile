FROM openjdk:17.0-slim
# Agrega el usuario y el grupo
RUN groupadd devopsc \
    && useradd -g devopsc javams
USER javams:devopsc
ENV JAVA_OPTS=""
ARG JAR_FILE
ADD ${JAR_FILE} app.jar

VOLUME /tmp
EXPOSE 7280
ENTRYPOINT [ "sh", "-C", "java -Dspring.profiles.active=production -jar /app.jar"]

# ENTRYPOINT [ "sh", "-C", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]

# RUN apt update && \
#     apt install -y curl unzip

# RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
#     unzip awscliv2.zip && \
#     ./aws/install

# COPY target/*.jar app.jar

# ENTRYPOINT ["java","-jar","app.jar"]

# EXPOSE 3000