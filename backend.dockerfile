# ==================================
# ÉTAPE 1: CONSTRUCTION (BUILDER)
# Le nom "builder" est ajouté ici pour être référencé dans l'étape suivante.
# ==================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /
COPY . .
RUN mvn clean package -DskipTests

# ==================================
# ÉTAPE 2: EXÉCUTION (RUNNER)
# ==================================
FROM eclipse-temurin:21-jdk
WORKDIR /backend
# Référence désormais l'étape nommée "builder"
COPY --from=builder /backend/target/*.jar app.jar 
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
