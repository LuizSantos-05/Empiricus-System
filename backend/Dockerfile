# Imagem com Maven e Java 25 para compilação
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copia primeiro o arquivo de dependências
COPY pom.xml .

# Baixa as dependências
RUN mvn dependency:go-offline

# Copia o código-fonte
COPY src ./src

# Compila e empacota a aplicação
RUN mvn clean package -DskipTests

# Imagem final apenas com o Java necessário para executar
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
