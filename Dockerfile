# # Use MySQL Docker image as base
# FROM mysql:8.0.36-debian
#
# # Install OpenJDK
# RUN apt-get update && \
#   apt-get install -y default-jre && \
#   rm -rf /var/lib/apt/lists/*
#
# # Set environment variables for MySQL root password
# ENV MYSQL_ROOT_PASSWORD=rootpassword
#
# # Create a directory for your Java application
# RUN mkdir /app
#
# ENV DISPLAY=
#
# # Copy your Java application JAR file to the container
# COPY build/libs/*.jar /app/
#
# # Command to run your Java application
# CMD ["java", "-jar", "/app/proj-1.0-SNAPSHOT.jar"]


FROM maven:3.5.0-jdk-8
RUN mkdir /app

# Copy your Java application JAR file to the container
COPY build/libs/*.jar /app/

CMD ["java", "-cp", "/app/*", "proj.Main"]

# Command to run your Java application
# CMD ["java", "-jar", "/app/proj-1.0-SNAPSHOT.jar"]
