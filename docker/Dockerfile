FROM gradle:7-jdk17-alpine
RUN apk add --no-cache git

WORKDIR /run
COPY launch.sh .
COPY inject.properties .

EXPOSE 80

CMD ["sh", "launch.sh"]
