# Easy2Drive

Easy2Drive is a web-based management platform designed for driving instructors and their students. It provides tools for scheduling lessons, managing payments, and facilitating communication.

## Features

### Teacher Capabilities
  **Dashboard**: Monitor upcoming lessons, student rosters, and payments.
  **Student Management**: Maintain student profiles and track progress.
  **Schedule Configuration**: Define working hours, breaks, and available lesson slots.
  **Policy Enforcement**: Set custom lesson durations, pricing, and cancellation windows.
  **Communication**: Direct messaging system for interacting with students.

### Student Capabilities
  **Dashboard**: View scheduled lessons and instructor information.
  **Booking System**: Browse instructor availability and book driving lessons.
  **Payment Tracking**: View payment history and account balance.
  **Communication**: Send and receive messages from the instructor.

## Technology Stack

  **Backend**: Java 17, Jakarta EE 10, Java Servlets
  **ORM**: Hibernate 6.2 (JPA)
  **Database**: MySQL 9.0
  **Frontend**: HTML5, CSS3, Vanilla JavaScript
  **Application Server**: WildFly (JBoss)
  **Build Tool**: Maven

## Prerequisites

  Java Development Kit (JDK) 17
  MySQL Server (8.0 or 9.0)
  WildFly Application Server
  Apache Maven

## Setup and Installation

1.  **Extract the Project**
    Extract the provided ZIP file containing the Easy2Drive project source code to your desired directory.

2.  **Database Setup**
    *   Start your MySQL server.
    *   Create a dedicated schema for the application (e.g., easy2drive_db). The application uses Hibernate's auto-update feature to generate the required tables.
    *   Configure a JBoss/WildFly Datasource named java:/jboss/datasources/Easy2DriveDS in your WildFly standalone.xml configuration file, ensuring it points to your newly created MySQL database.

3.  **Build the Project**
    Open a terminal or command prompt, navigate to the extracted project directory (where pom.xml is located), and execute the following Maven command to build the .war package:
   

    mvn clean package
   

4.  **Deployment**
    *   Copy the generated Easy2Drive.war file from the target/ directory.
    *   Paste the file into the standalone/deployments/ directory of your WildFly server installation.
    *   Start or restart the WildFly server to deploy the application.

5.  **Access the Application**
    Once deployment is complete, access the application via your web browser:
   
    http://localhost:8080/Easy2Drive/