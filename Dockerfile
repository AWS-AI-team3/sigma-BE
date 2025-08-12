FROM gradle:8.10-jdk17 AS build
WORKDIR /workspace

# 프로젝트 전체 복사
COPY . .

# 테스트 생략하고 빌드 (원하면 -x test 빼고 빌드 가능)
RUN gradle clean bootJar -x test

# Multi-stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=build /workspace/build/libs/*.jar app.jar

# 포트 개방
EXPOSE 8080

# 컨테이너 실행 시 명령
ENTRYPOINT ["java","-jar","/app/app.jar"]
