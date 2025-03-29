FROM ubuntu:22.04

ARG REPOSITORY_URL=""
ENV REPOSITORY_URL=$REPOSITORY_URL

COPY setup.sh .
COPY run.sh .

RUN ./setup.sh

CMD ["./run.sh"]
