# Stage 1: Build file .war bằng Maven
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Stage 2: Chạy trên Tomcat 9
FROM tomcat:9.0-jdk17
# Tắt cổng shutdown 8005 để tránh xung đột với Health Check của Render
RUN sed -i 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

ENV PORT=8080
EXPOSE 8080
CMD ["catalina.sh", "run"]